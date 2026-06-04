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

    public ProductWithAllResDto getProductWithAll(Long id) {
        return productRepository.findWithAllById(id)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public ProductWithSkuResDto getProductWithSku(Long id) {
        return productRepository.findWithSkuById(id)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public Product getProduct(Long id) {
        return productRepository.findByIdAndStatusNot(id, ProductStatus.ARCHIVED)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public Product createProduct(ProductCreateReqDto reqDto) {
        Category category = getCategory(reqDto.categoryId());

        validateCategory(category);
        validateProductType(reqDto);

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
            reqDto.basePrice(),
            dimension
        );
    }

    public void syncBasePrice(Product product) {
        product.syncBasePrice();
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


    // ===== 유효성 검사 메서드 ====

    public void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT);
        }
    }

    private void validateCategory(Category category) {
        if (category.isArchived()) {
            throw new ServiceException(CategoryErrorCode.ARCHIVED_CATEGORY);
        }

        if (!category.isLeaf()) {
            throw new ServiceException(CategoryErrorCode.NOT_LEAF_CATEGORY);
        }
    }

    private static void validateProductType(ProductCreateReqDto reqDto) {
        if (reqDto.productType() == ProductType.COMPLETE && reqDto.compositions().isEmpty()) {
            throw new ServiceException(ProductErrorCode.COMPLETE_HAVE_COMPOSITIONS);
        }
        if (reqDto.productType() == ProductType.COMPONENT && !reqDto.compositions().isEmpty()) {
            throw new ServiceException(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION);
        }
    }


    // ===== 헬퍼 메서드 ====

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    private List<ProductAttribute> getAttributes(Long productId) {
        return attributeRepository.findByProductId(productId);
    }
}
