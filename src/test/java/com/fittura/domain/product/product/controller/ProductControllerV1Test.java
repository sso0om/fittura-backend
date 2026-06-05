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
import com.fittura.domain.product.product.support.ProductFixture;
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


    // ========== 상품 목록 조회 ==========

    @Test
    @DisplayName("상품 목록 조회 성공 - ACTIVE, DISCONTINUED 상품 반환")
    void getProductsSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product activeProduct = Product.create(category, "Active Desk", null, ProductType.COMPONENT, dimension);
        activeProduct.activate();
        productRepository.save(activeProduct);

        Product discontinuedProduct = Product.create(category, "Discontinued Chair", null, ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product activeProduct = Product.create(category, "Active Desk", null, ProductType.COMPONENT, dimension);
        activeProduct.activate();
        productRepository.save(activeProduct);

        Product discontinuedProduct = Product.create(category, "Discontinued Chair", null, ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product desk = Product.create(category, "A Desk", null, ProductType.COMPONENT, dimension);
        desk.activate();
        productRepository.save(desk);

        Product chair = Product.create(category, "A Chair", null, ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product product1 = Product.create(category1, "A Desk", null, ProductType.COMPONENT, dimension);
        product1.activate();
        productRepository.save(product1);

        Product product2 = Product.create(category2, "A Chair", null, ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product woodProduct = Product.create(category, "Wood White Desk", null, ProductType.COMPONENT, dimension);
        woodProduct.activate();
        productRepository.save(woodProduct);
        productSkuRepository.save(ProductSku.create(woodProduct, 50000L, 100, "White", "Wood"));

        Product woodBrownProduct = Product.create(category, "Wood Brown Desk", null, ProductType.COMPONENT, dimension);
        woodBrownProduct.activate();
        productRepository.save(woodBrownProduct);
        productSkuRepository.save(ProductSku.create(woodBrownProduct, 60000L, 100, "Brown", "Wood"));

        Product metalProduct = Product.create(category, "Metal Black Chair", null, ProductType.COMPONENT, dimension);
        metalProduct.activate();
        productRepository.save(metalProduct);
        productSkuRepository.save(ProductSku.create(metalProduct, 70000L, 100, "Black", "Metal"));

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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        // DISABLED는 Product.create()의 기본 상태
        productRepository.save(Product.create(category, "Hidden Desk", null, ProductType.COMPONENT, dimension));

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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product first = Product.create(category, "First Desk", null, ProductType.COMPONENT, dimension);
        first.activate();
        productRepository.save(first);

        Product second = Product.create(category, "Second Chair", null, ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product expensive = Product.create(category, "Expensive Desk", null, ProductType.COMPONENT, dimension);
        expensive.activate();
        ProductSku.create(expensive, 100000L, 100, "White", "Wood");
        expensive.syncBasePrice();
        productRepository.save(expensive);
        productSkuRepository.save(expensive.getProductSkus().get(0));

        Product cheap = Product.create(category, "Cheap Chair", null, ProductType.COMPONENT, dimension);
        cheap.activate();
        ProductSku.create(cheap, 45000L, 100, "White", "Wood");
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
        ProductSku sku = productSkuRepository.save(ProductSku.create(product, 5000L, 0, "White", "Wood"));
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, dimension);
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
    @DisplayName("상품 상세 조회 성공 - SKU 전체 품절 시 isSoldOut true")
    void getProductSuccess_soldOut() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = ProductFixture.component(category, "Chair");
        product.activate();
        productRepository.save(product);

        ProductSku sku = productSkuRepository.save(ProductSku.create(product, 5000L, 0, "White", "Wood"));
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, dimension)
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
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, dimension);
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
        Product product = Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, dimension);
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
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product childProduct = Product.create(category, "의자 다리", null, ProductType.COMPONENT, dimension);
        childProduct.activate();
        productRepository.save(childProduct);

        ProductSku childSku = ProductSku.create(childProduct, 10000L, 100, null, null);
        productSkuRepository.save(childSku);

        Product parentProduct = Product.create(category, "완제품 의자", null, ProductType.COMPLETE, dimension);
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
        Product product = Product.create(category, "완제품 의자", null, ProductType.COMPLETE, dimension);
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