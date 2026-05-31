package com.fittura.domain.product.product.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerV1Test extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSkuRepository productSkuRepository;

    @Autowired
    private ProductAttributeRepository attributeRepository;

    @Autowired
    private CompositionRepository compositionRepository;

    private static final String PRODUCT_URL = "/api/v1/products";


    // ========== 상품 조회 ==========

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, 50000L, dimension);
        product.activate();
        productRepository.save(product);
        productSkuRepository.save(ProductSku.create(product, 45000L, 100, "White", "Wood"));

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("A Desk"))
            .andExpect(jsonPath("$.data.skus").isArray())
            .andExpect(jsonPath("$.data.skus[0].color").value("White"));
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 상품 없음")
    void getProductFail_notFound() throws Exception {
        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/9999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_PRODUCT.getCode()));
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - DISABLED 상품은 조회 불가")
    void getProductFail_disabled() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, 50000L, dimension)
        );

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId()))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    // ========== 속성 조회 ==========

    @Test
    @DisplayName("상품 고시 정보 조회 성공")
    void getProductAttributesSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, 50000L, dimension);
        product.activate();
        productRepository.save(product);
        ProductAttribute.create(product, AttributeKey.SIZE_LABEL, "XL");
        attributeRepository.save(product.getAttributes().get(0));

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId() + "/attributes"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].attributeKey").value("SIZE_LABEL"))
            .andExpect(jsonPath("$.data[0].attributeValue").value("XL"));
    }

    @Test
    @DisplayName("상품 고시 정보 조회 성공 - 속성 없음")
    void getProductAttributesSuccess_empty() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, 50000L, dimension);
        product.activate();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId() + "/attributes"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }


    // ========== 구성품 조회 ==========

    @Test
    @DisplayName("상품 구성 정보 조회 성공")
    void getProductCompositionsSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product childProduct = Product.create(category, "의자 다리", null, ProductType.COMPONENT, 10000L, dimension);
        childProduct.activate();
        productRepository.save(childProduct);

        ProductSku childSku = ProductSku.create(childProduct, 10000L, 100, null, null);
        productSkuRepository.save(childSku);

        Product parentProduct = Product.create(category, "완제품 의자", null, ProductType.COMPLETE, 50000L, dimension);
        parentProduct.activate();
        productRepository.save(parentProduct);

        compositionRepository.save(ProductComposition.create(parentProduct, childSku, 4, 0));

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + parentProduct.getId() + "/compositions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].childProductName").value("의자 다리"))
            .andExpect(jsonPath("$.data[0].quantity").value(4));
    }

    @Test
    @DisplayName("상품 구성 정보 조회 성공 - 구성품 없음")
    void getProductCompositionsSuccess_empty() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = Product.create(category, "완제품 의자", null, ProductType.COMPLETE, 50000L, dimension);
        product.activate();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId() + "/compositions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}