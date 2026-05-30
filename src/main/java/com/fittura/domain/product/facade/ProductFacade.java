package com.fittura.domain.product.facade;

import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithStockResDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.service.ProductService;
import com.fittura.domain.product.product.dto.response.ProductAttributeResDto;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
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
    public ProductResDto getProduct(Long id) {
        return productService.getProduct(id);
    }

    @Transactional(readOnly = true)
    public ProductWithStockResDto getProductWithStock(Long id) {
        return productService.getProductWithStock(id);
    }

    @Transactional
    public Long createProduct(ProductCreateReqDto reqDto) {
        Product product = productService.createProduct(reqDto);
        skuService.createSkus(product, reqDto.skus());

        if (product.isComplete()) {
            skuService.createCompositions(product, reqDto.compositions());
        }

        return product.getId();
    }


    // ========== 속성 ==========

    @Transactional(readOnly = true)
    public List<ProductAttributeResDto> getProductAttributes(Long productId) {
        return productService.getProductAttributes(productId);
    }
}
