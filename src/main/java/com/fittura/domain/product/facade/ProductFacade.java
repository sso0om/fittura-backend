package com.fittura.domain.product.facade;

import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.request.ProductUpdateReqDto;
import com.fittura.domain.product.product.dto.response.*;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.service.ProductService;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final SkuService skuService;

    // ========== 상품 ==========

    @Transactional(readOnly = true)
    public Page<ProductResDto> getProducts(ProductSearchCondition searchCondition, Pageable pageable) {
        return productService.getProducts(searchCondition, pageable);
    }

    @Transactional(readOnly = true)
    public ProductWithAllResDto getProductWithAll(Long productId) {
        return productService.getProductWithAll(productId);
    }

    @Transactional(readOnly = true)
    public ProductWithSkuResDto getProductWithSku(Long productId) {
        return productService.getProductWithSku(productId);
    }

    @Transactional
    public Long createProduct(ProductCreateReqDto reqDto) {
        Product product = productService.createProduct(reqDto);
        skuService.createSkus(product, reqDto.skus());
        productService.syncBasePrice(product);

        if (product.isComplete()) {
            skuService.createCompositions(product, reqDto.compositions());
        }

        return product.getId();
    }

    @Transactional
    public void updateProduct(Long productId, ProductUpdateReqDto reqDto) {
        Product product = productService.getProduct(productId);

        productService.updateProduct(product, reqDto);
        productService.updateProductAttribute(product, reqDto.attributes());
        skuService.updateSku(product, reqDto.skus());
        productService.syncBasePrice(product);

        if (product.isComplete()) {
            skuService.updateCompositions(product, reqDto.compositions());
        }
    }

    @Transactional
    public void disableProduct(Long productId) {
        productService.disableProduct(productId);
    }

    @Transactional
    public void discontinueProduct(Long productId) {
        productService.discontinueProduct(productId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productService.getProduct(productId);

        productService.validateModifiableProduct(product);
        skuService.validateDeletableSku(product);

        if (product.isComplete()) {
            skuService.deleteCompositions(product);
        }
        productService.deleteProductAttributes(product);
        skuService.deleteSkus(product);
        productService.deleteProduct(product);
    }


    // ========== SKU ==========

    @Transactional
    public void soldOutSku(Long productId, Long skuId) {
        skuService.soldOutSku(productId, skuId);
    }

    @Transactional
    public void discontinueSku(Long productId, Long skuId) {
        skuService.discontinueSku(productId, skuId);
    }


    // ========== 속성 ==========

    @Transactional(readOnly = true)
    public List<ProductAttributeResDto> getProductAttributes(Long productId) {
        productService.validateProductExists(productId);
        return productService.getProductAttributes(productId);
    }


    // ========== 구성 ==========

    @Transactional(readOnly = true)
    public List<CompositionResDto> getProductCompositions(Long productId) {
        productService.validateProductExists(productId);
        return skuService.getProductCompositionDtos(productId);
    }
}
