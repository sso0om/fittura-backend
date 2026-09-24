package com.fittura.domain.product.facade;

import com.fittura.domain.category.service.CategoryService;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.request.*;
import com.fittura.domain.product.product.dto.response.*;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.service.ProductService;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
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
    private final CategoryService categoryService;

    // ========== 상품 ==========

    @Transactional(readOnly = true)
    public Page<ProductResDto> getProductsForAdmin(AdminProductSearchReqDto reqDto, Pageable pageable) {
        List<ProductStatus> statuses = (reqDto.statuses() == null || reqDto.statuses().isEmpty())
            ? List.of(ProductStatus.ACTIVE, ProductStatus.DISABLED, ProductStatus.DISCONTINUED)
            : reqDto.statuses();
        List<Long> categoryIds = categoryService.getCategoryIdWithDescendant(reqDto.categoryId());

        ProductSearchCondition searchCondition = new ProductSearchCondition(
            false,
            statuses,
            categoryIds,
            reqDto.keyword(),
            reqDto.colors(),
            reqDto.materials()
        );
        return productService.getProducts(searchCondition, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> getProducts(ProductSearchReqDto reqDto, Pageable pageable) {
        List<ProductStatus> statuses = Boolean.TRUE.equals(reqDto.inStockOnly())
            ? List.of(ProductStatus.ACTIVE)
            : ProductStatus.PUBLIC_STATUSES;
        List<Long> categoryIds = categoryService.getCategoryIdWithDescendant(reqDto.categoryId());

        ProductSearchCondition searchCondition = new ProductSearchCondition(
            reqDto.inStockOnly(),
            statuses,
            categoryIds,
            reqDto.keyword(),
            reqDto.colors(),
            reqDto.materials()
        );
        return productService.getProducts(searchCondition, pageable);
    }

    @Transactional(readOnly = true)
    public ProductWithAllResDto getProductWithAll(Long productId) {
        return productService.getProductWithAll(productId);
    }

    @Transactional(readOnly = true)
    public ProductWithSkuResDto getProductWithSku(Long productId) {
        ProductWithSkuResDto productDto = productService.getProductWithSku(productId);
        List<SkuResDto> skuDtos = skuService.getProductSkuResDto(productId);

        return productDto.withSkus(skuDtos);
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
    public void activateProduct(Long productId) {
        productService.activateProduct(productId);
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

    @Transactional(readOnly = true)
    public List<SkuResDto> getProductSkus(Long productId) {
        productService.validatePublicProduct(productId);
        return skuService.getProductSkuResDto(productId);
    }

    @Transactional
    public void pauseSku(Long productId, Long skuId) {
        skuService.pauseSku(productId, skuId);
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


    // ========== 필터 ==========

    @Transactional(readOnly = true)
    public ProductFilterResDto getProductFilter() {
        List<ColorResDto> colors = skuService.getColors();
        List<MaterialResDto> materials = skuService.getMaterials();
        return new ProductFilterResDto(colors, materials);
    }
}
