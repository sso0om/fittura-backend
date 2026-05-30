package com.fittura.domain.product.product.service;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.AttributeCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithStockResDto;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.product.error.ProductErrorCode;
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

    public ProductResDto getProduct(Long id) {
        return productRepository.findWithDetailById(id)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_PRODUCT));
    }

    public ProductWithStockResDto getProductWithStock(Long id) {
        return productRepository.findWithStockById(id)
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


    // ===== 유효성 검사 메서드 ====

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
