package com.fittura.domain.product.product.service;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.*;
import com.fittura.domain.product.product.dto.response.ProductAttributeResDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;
import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeRepository attributeRepository;

    // ========== 상품 ==========

    public Page<ProductResDto> getProducts(ProductSearchCondition searchCondition, Pageable pageable) {
        return productRepository.findProducts(searchCondition, pageable);
    }

    public ProductWithAllResDto getProductWithAll(Long productId) {
        return productRepository.findWithAllById(productId)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public ProductWithSkuResDto getProductWithSku(Long productId) {
        return productRepository.findWithSkuById(productId)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public Product getProduct(Long productId) {
        return productRepository.findByIdAndStatusNot(productId, ProductStatus.ARCHIVED)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public Product createProduct(ProductCreateReqDto reqDto) {
        Category category = getCategory(reqDto.categoryId());

        validateCategory(category);
        validateProductType(reqDto.productType(), reqDto.compositions().isEmpty());

        Dimension dimension = Dimension.of(
            reqDto.weight(),
            reqDto.width(),
            reqDto.height(),
            reqDto.depth()
        );

        Product product = Product.create(
            category,
            reqDto.name(),
            reqDto.description(),
            reqDto.productType(),
            reqDto.deliveryType(),
            dimension
        );

        createAttributes(product, reqDto.attributes());
        productRepository.save(product);

        return product;
    }

    private void createAttributes(Product product, List<AttributeCreateReqDto> attributesDtos) {
        for (AttributeCreateReqDto attributeDto : attributesDtos) {
            ProductAttribute.create(
                product,
                attributeDto.attributeKey(),
                attributeDto.attributeValue()
            );
        }
    }

    public void updateProduct(Product product, ProductUpdateReqDto reqDto) {
        Category category = getCategory(reqDto.categoryId());
        validateCategory(category);
        validateProductType(product.getProductType(), reqDto.compositions().isEmpty());

        Dimension dimension = Dimension.of(
            reqDto.weight(),
            reqDto.width(),
            reqDto.height(),
            reqDto.depth()
        );

        product.update(
            category,
            reqDto.name(),
            reqDto.description(),
            reqDto.deliveryType(),
            dimension
        );
    }

    public void syncBasePrice(Product product) {
        product.syncBasePrice();
    }

    public void activateProduct(Long productId) {
        Product product = getProduct(productId);
        product.activate();
    }

    public void disableProduct(Long productId) {
        Product product = getProduct(productId);
        validateModifiableProduct(product);

        product.disable();
    }

    public void discontinueProduct(Long productId) {
        Product product = getProduct(productId);
        product.discontinue();
    }

    public void deleteProduct(Product product) {
        product.archive();
    }


    // ========== 속성 ==========

    public List<ProductAttributeResDto> getProductAttributes(Long productId) {
        return attributeRepository.findByProductId(productId).stream()
            .map(ProductAttributeResDto::from)
            .toList();
    }

    public void updateProductAttribute(Product product, List<AttributeUpdateReqDto> reqDto) {

        List<ProductAttribute> existing = getAttributes(product.getId());

        Map<AttributeKey, ProductAttribute> existingMap = existing.stream()
            .collect(Collectors.toMap(ProductAttribute::getAttributeKey, a -> a));

        Set<AttributeKey> incomingKeys = reqDto.stream()
            .map(AttributeUpdateReqDto::attributeKey)
            .collect(Collectors.toSet());

        existing.stream()
            .filter(a -> !incomingKeys.contains(a.getAttributeKey()))
            .forEach(attributeRepository::delete);

        for (AttributeUpdateReqDto dto : reqDto) {
            ProductAttribute attribute = existingMap.get(dto.attributeKey());

            if (attribute == null) {
                ProductAttribute newAttribute = ProductAttribute.create(product, dto.attributeKey(), dto.attributeValue());
                attributeRepository.save(newAttribute);
            } else {
                attribute.updateValue(dto.attributeValue());
            }
        }
    }

    public void deleteProductAttributes(Product product) {
        attributeRepository.deleteAllByProductId(product.getId());
    }


    // ===== 유효성 검사 메서드 ====

    public void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT);
        }
    }

    public void validateModifiableProduct(Product product) {
        // TODO: 진행 중인 주문(결제/배송)이 있는 경우 삭제, 비활성화 불가 (주문 도메인 구현 후)
    }

    private void validateCategory(Category category) {
        if (category.isArchived()) {
            throw new ServiceException(CategoryErrorCode.ARCHIVED_CATEGORY);
        }

        if (!category.isLeaf()) {
            throw new ServiceException(CategoryErrorCode.NOT_LEAF_CATEGORY);
        }
    }

    private static void validateProductType(ProductType productType, boolean compositionsEmpty) {
        if (productType == ProductType.COMPLETE && compositionsEmpty) {
            throw new ServiceException(ProductErrorCode.COMPLETE_HAVE_COMPOSITIONS);
        }
        if (productType == ProductType.COMPONENT && !compositionsEmpty) {
            throw new ServiceException(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION);
        }
    }


    // ===== 헬퍼 메서드 ====

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    private List<ProductAttribute> getAttributes(Long productId) {
        return attributeRepository.findByProductId(productId);
    }
}
