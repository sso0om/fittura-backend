package com.fittura.domain.product.product.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductAttributeFixture;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductCompositionFixture;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
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


    // ========== 상품 목록 조회 ==========

    @Test
    @DisplayName("상품 목록 조회 성공 - ACTIVE, DISCONTINUED 상품 반환")
    void getProductsSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product activeProduct = ProductFixture.component(category, "Active Desk");
        activeProduct.activate();
        productRepository.save(activeProduct);

        Product discontinuedProduct = ProductFixture.component(category, "Discontinued Chair");
        discontinuedProduct.discontinue();
        productRepository.save(discontinuedProduct);

        // when & then
        mockMvc.perform(get(PRODUCT_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("상품 목록 조회 - 품절 제외 (ACTIVE만 조회)")
    void getProducts_excludesDiscontinued() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product activeProduct = ProductFixture.component(category, "Active Desk");
        activeProduct.activate();
        productRepository.save(activeProduct);

        Product discontinuedProduct = ProductFixture.component(category, "Discontinued Chair");
        discontinuedProduct.discontinue();
        productRepository.save(discontinuedProduct);

        // when & then
        mockMvc.perform(get(PRODUCT_URL).param("statuses", "ACTIVE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].name").value("Active Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - keyword 필터링")
    void getProducts_keyword() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product desk = ProductFixture.component(category, "A Desk");
        desk.activate();
        productRepository.save(desk);

        Product chair = ProductFixture.component(category, "A Chair");
        chair.activate();
        productRepository.save(chair);

        // when & then
        mockMvc.perform(get(PRODUCT_URL).param("keyword", "Desk"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].name").value("A Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - categoryId 필터링")
    void getProducts_categoryId() throws Exception {
        // given
        Category category1 = categoryRepository.save(CategoryFixture.rootActive());
        Category category2 = categoryRepository.save(CategoryFixture.rootActive());

        Product product1 = ProductFixture.component(category1, "A Desk");
        product1.activate();
        productRepository.save(product1);

        Product product2 = ProductFixture.component(category2, "A Chair");
        product2.activate();
        productRepository.save(product2);

        // when & then
        mockMvc.perform(get(PRODUCT_URL).param("categoryId", String.valueOf(category1.getId())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].name").value("A Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - colors, materials 필터링")
    void getProducts_colorsAndMaterials() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product woodProduct = ProductFixture.component(category, "Wood White Desk");
        woodProduct.activate();
        productRepository.save(woodProduct);
        productSkuRepository.save(ProductSkuFixture.sku(woodProduct, 50000L, 100, "White", "Wood"));

        Product woodBrownProduct = ProductFixture.component(category, "Wood Brown Desk");
        woodBrownProduct.activate();
        productRepository.save(woodBrownProduct);
        productSkuRepository.save(ProductSkuFixture.sku(woodBrownProduct, 60000L, 100, "Brown", "Wood"));

        Product metalProduct = ProductFixture.component(category, "Metal Black Chair");
        metalProduct.activate();
        productRepository.save(metalProduct);
        productSkuRepository.save(ProductSkuFixture.sku(metalProduct, 70000L, 100, "Black", "Metal"));

        // when & then
        mockMvc.perform(get(PRODUCT_URL)
                .param("colors", "White", "Brown")
                .param("materials", "Wood"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[0].name").value("Wood Brown Desk"))
            .andExpect(jsonPath("$.data.content[1].name").value("Wood White Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 - DISABLED 상품은 조회 안 됨")
    void getProducts_excludesDisabled() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        // DISABLED는 Product.create()의 기본 상태
        productRepository.save(ProductFixture.component(category, "Hidden Desk"));

        // when & then
        mockMvc.perform(get(PRODUCT_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("상품 목록 조회 - 기본 정렬은 최신순")
    void getProducts_defaultSortByLatest() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product first = ProductFixture.component(category, "First Desk");
        first.activate();
        productRepository.save(first);

        Product second = ProductFixture.component(category, "Second Chair");
        second.activate();
        productRepository.save(second);

        // when & then
        mockMvc.perform(get(PRODUCT_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].name").value("Second Chair"))
            .andExpect(jsonPath("$.data.content[1].name").value("First Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 가격 오름차순 정렬")
    void getProducts_sortByPriceAsc() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product expensive = ProductFixture.component(category, "Expensive Desk");
        expensive.activate();
        ProductSkuFixture.sku(expensive, 100000L, 100);
        expensive.syncBasePrice();
        productRepository.save(expensive);
        productSkuRepository.save(expensive.getProductSkus().get(0));

        Product cheap = ProductFixture.component(category, "Cheap Chair");
        cheap.activate();
        ProductSkuFixture.sku(cheap, 45000L, 100);
        cheap.syncBasePrice();
        productRepository.save(cheap);
        productSkuRepository.save(cheap.getProductSkus().get(0));

        // when & then
        mockMvc.perform(get(PRODUCT_URL).param("sort", "basePrice,asc"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].name").value("Cheap Chair"))
            .andExpect(jsonPath("$.data.content[1].name").value("Expensive Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - SKU 전체 품절 시 isSoldOut true")
    void getProductsSuccess_soldOut() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "Chair"));
        product.activate();
        ProductSku sku = productSkuRepository.save(ProductSkuFixture.sku(product, 5000L, 0));
        sku.soldOut();
        productSkuRepository.save(sku);

        // when & then
        mockMvc.perform(get(PRODUCT_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].isSoldOut").value(true));
    }


    // ========== 상품 조회 ==========

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = ProductFixture.component(category, "A Desk");
        product.activate();
        productRepository.save(product);
        productSkuRepository.save(ProductSkuFixture.sku(product, 45000L, 100));

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("A Desk"))
            .andExpect(jsonPath("$.data.skus").isArray())
            .andExpect(jsonPath("$.data.skus[0].color").value("White"));
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - SKU 전체 품절 시 isSoldOut true")
    void getProductSuccess_soldOut() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = ProductFixture.component(category, "Chair");
        product.activate();
        productRepository.save(product);

        ProductSku sku = productSkuRepository.save(ProductSkuFixture.sku(product, 5000L, 0));
        sku.soldOut();
        productSkuRepository.save(sku);

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isSoldOut").value(true));
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
        Product product = productRepository.save(
            ProductFixture.component(category, "A Desk")
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
        Product product = ProductFixture.component(category, "A Desk");
        product.activate();
        productRepository.save(product);
        ProductAttributeFixture.productAttribute(product, AttributeKey.SIZE_LABEL, "XL");
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
        Product product = ProductFixture.component(category, "A Desk");
        product.activate();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId() + "/attributes"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("상품 고시 정보 조회 실패 - 상품 없음")
    void getProductAttributesFail_notFound() throws Exception {
        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/9999/attributes"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_PRODUCT.getCode()));
    }


    // ========== 구성품 조회 ==========

    @Test
    @DisplayName("상품 구성 정보 조회 성공")
    void getProductCompositionsSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        Product childProduct = ProductFixture.component(category, "의자 다리");
        childProduct.activate();
        productRepository.save(childProduct);

        ProductSku childSku = ProductSkuFixture.skuWithNoOption(childProduct);
        productSkuRepository.save(childSku);

        Product parentProduct = ProductFixture.complete(category, "완제품 의자");
        parentProduct.activate();
        productRepository.save(parentProduct);

        compositionRepository.save(ProductCompositionFixture.composition(parentProduct, childSku, 4, 0));

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
        Product product = ProductFixture.complete(category, "완제품 의자");
        product.activate();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/" + product.getId() + "/compositions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("상품 구성 정보 조회 실패 - 상품 없음")
    void getProductCompositionsFail_notFound() throws Exception {
        // when & then
        mockMvc.perform(get(PRODUCT_URL + "/9999/compositions"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_PRODUCT.getCode()));
    }
}