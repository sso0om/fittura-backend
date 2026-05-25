package com.fittura.domain.product.facade;

import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.service.ProductService;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final SkuService skuService;

    @Transactional
    public Long createProduct(ProductCreateReqDto reqDto) {
        Product product = productService.createProduct(reqDto);
        skuService.createSkus(product, reqDto.skus());

        if (product.isComplete()) {
            skuService.createCompositions(product, reqDto.compositions());
        }

        return product.getId();
    }
}
