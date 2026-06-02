package com.fittura.domain.product.product.service;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.AttributeCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.response.ProductAttributeResDto;
import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeRepository attributeRepository;

    // ========== 상품 ==========

    public ProductWithAllResDto getProductWithAll(Long id) {
        return productRepository.findWithAllById(id)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public ProductWithSkuResDto getProductWithSku(Long id) {
        return productRepository.findWithSkuById(id)
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
            reqDto.basePrice(),
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


    // ========== 속성 ==========

    public List<ProductAttributeResDto> getProductAttributes(Long productId) {
        return attributeRepository.findByProductId(productId).stream()
            .map(ProductAttributeResDto::from)
            .toList();
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
}
