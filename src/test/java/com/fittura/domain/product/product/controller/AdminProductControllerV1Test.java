package com.fittura.domain.product.product.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.IntegrationTestBase;
import com.fittura.global.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = "ADMIN")
class AdminProductControllerV1Test extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSkuRepository productSkuRepository;

    @Autowired
    private CompositionRepository compositionRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    private static final String PRODUCT_ADMIN_URL = "/api/admin/v1/products";


    // ========== 상품 목록 조회 ==========

    @Test
    @DisplayName("상품 목록 조회 성공 - ACTIVE, DISCONTINUED, DISABLED 상품 모두 반환")
    void getProductsSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product activeProduct = Product.create(category, "Active Desk", null, ProductType.COMPONENT, dimension);
        activeProduct.activate();
        productRepository.save(activeProduct);

        productRepository.save(Product.create(category, "Disabled Chair", null, ProductType.COMPONENT, dimension));

        Product discontinuedProduct = Product.create(category, "Discontinued Sofa", null, ProductType.COMPONENT, dimension);
        ReflectionTestUtils.setField(discontinuedProduct, "status", ProductStatus.DISCONTINUED);
        productRepository.save(discontinuedProduct);

        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    @DisplayName("상품 목록 조회 - ACTIVE 상품만 조회")
    void getProducts_statusFilter() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product activeProduct = Product.create(category, "Active Desk", null, ProductType.COMPONENT, dimension);
        activeProduct.activate();
        productRepository.save(activeProduct);

        productRepository.save(Product.create(category, "Disabled Chair", null, ProductType.COMPONENT, dimension));

        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL).param("statuses", "ACTIVE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].name").value("Active Desk"));
    }

    @Test
    @DisplayName("상품 목록 조회 - DISABLED 상품만 조회")
    void getProducts_includesArchived() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product activeProduct = Product.create(category, "Active Desk", null, ProductType.COMPONENT, dimension);
        activeProduct.activate();
        productRepository.save(activeProduct);

        Product disabledProduct = Product.create(category, "DISABLED Desk", null, ProductType.COMPONENT, dimension);
        productRepository.save(disabledProduct);

        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL).param("statuses", "DISABLED"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].name").value("DISABLED Desk"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("상품 목록 조회 실패 - 인증 없음")
    void getProducts_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }


    // ========== 상품 조회 ==========

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "A Desk", "책상입니다.", ProductType.COMPONENT, dimension)
        );
        productSkuRepository.save(ProductSku.create(product, 45000L, 100, "White", "Wood"));

        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL + "/" + product.getId()))
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
        mockMvc.perform(get(PRODUCT_ADMIN_URL + "/9999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_PRODUCT.getCode()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("상품 상세 조회 실패 - 인증 없음")
    void getProductUnauthorized() throws Exception {
        // when & then
        mockMvc.perform(get(PRODUCT_ADMIN_URL + "/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }


    // ========== 상품 생성 ==========

    @Test
    @DisplayName("완제품 생성 성공")
    void createCompleteSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product componentProduct = productRepository.save(
            Product.create(category, "Chair Leg", null, ProductType.COMPONENT, dimension)
        );
        ProductSku childSku = productSkuRepository.save(
            ProductSku.create(componentProduct, 5000L, 100, "White", "Wood")
        );

        long productCountBefore = productRepository.count();
        long skuCountBefore = productSkuRepository.count();
        long compositionCountBefore = compositionRepository.count();

        String reqBody = """
            {
                "categoryId": %d,
                "name": "A Desk",
                "productType": "COMPLETE",
                "basePrice": 100000,
                "weight": 10.5,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 90000,
                    "stockQuantity": 50,
                    "color": "White",
                    "material": "Wood"
                }],
                "attributes": [],
                "compositions": [{
                    "childSkuId": %d,
                    "quantity": 4,
                    "sortOrder": 0
                }]
            }
        """.formatted(category.getId(), childSku.getId());

        // when
        ResultActions resultActions = mockMvc
            .perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // then
        resultActions
            .andExpect(handler().handlerType(AdminProductControllerV1.class))
            .andExpect(handler().methodName("createProduct"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("제품이 생성되었습니다."))
            .andExpect(jsonPath("$.data").isNumber());

        assertThat(productRepository.count()).isEqualTo(productCountBefore + 1);
        assertThat(productSkuRepository.count()).isEqualTo(skuCountBefore + 1);
        assertThat(compositionRepository.count()).isEqualTo(compositionCountBefore + 1);
    }

    @Test
    @DisplayName("단품 생성 성공")
    void createComponentSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        long productCountBefore = productRepository.count();
        long skuCountBefore = productSkuRepository.count();
        long compositionCountBefore = compositionRepository.count();

        String reqBody = """
            {
                "categoryId": %d,
                "name": "Chair Leg",
                "productType": "COMPONENT",
                "basePrice": 5000,
                "weight": 2.0,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 4500,
                    "stockQuantity": 100,
                    "color": "White",
                    "material": "Wood"
                }],
                "attributes": [],
                "compositions": []
            }
        """.formatted(category.getId());

        // when
        ResultActions resultActions = mockMvc
            .perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print());

        // then
        resultActions
            .andExpect(handler().handlerType(AdminProductControllerV1.class))
            .andExpect(handler().methodName("createProduct"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("제품이 생성되었습니다."))
            .andExpect(jsonPath("$.data").isNumber());

        assertThat(productRepository.count()).isEqualTo(productCountBefore + 1);
        assertThat(productSkuRepository.count()).isEqualTo(skuCountBefore + 1);
        assertThat(compositionRepository.count()).isEqualTo(compositionCountBefore);
    }

    @WithMockUser(roles = "USER")
    @Test
    @DisplayName("상품 생성 실패 - 관리자 아닌 경우")
    void createForbidden() throws Exception {
        // given
        String reqBody = """
            {
                "categoryId": %d,
                "name": "Chair Leg",
                "productType": "COMPONENT",
                "basePrice": 5000,
                "weight": 2.0,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 4500,
                    "stockQuantity": 100
                }],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("상품 생성 실패 - validation 오류 (skus 비어있음)")
    void createFail_validationError() throws Exception {
        // given - skus는 @Size(min=1) 제약이 있으므로 빈 배열은 실패
        String reqBody = """
            {
                "categoryId": 1,
                "name": "A Desk",
                "productType": "COMPLETE",
                "basePrice": 100000,
                "weight": 2.0,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(CommonErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @DisplayName("상품 생성 실패 - 완제품에 compositions 없음")
    void createFail_completeMissingCompositions() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        String reqBody = """
            {
                "categoryId": %d,
                "name": "A Desk",
                "productType": "COMPLETE",
                "basePrice": 100000,
                "weight": 10.5,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 90000,
                    "stockQuantity": 50
                }],
                "attributes": [],
                "compositions": []
            }
        """.formatted(category.getId());

        // when & then
        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.COMPLETE_HAVE_COMPOSITIONS.getCode()))
            .andExpect(jsonPath("$.message").value(ProductErrorCode.COMPLETE_HAVE_COMPOSITIONS.getMessage()));
    }

    @Test
    @DisplayName("상품 생성 실패 - 카테고리 없음")
    void createFail_categoryNotFound() throws Exception {
        // given
        String reqBody = """
            {
                "categoryId": 9999,
                "name": "Chair Leg",
                "productType": "COMPONENT",
                "basePrice": 5000,
                "weight": 2.0,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 4500,
                    "stockQuantity": 100
                }],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("상품 생성 실패 - 리프 카테고리가 아닌 경우")
    void createFail_notLeafCategory() throws Exception {
        // given
        Category root = categoryRepository.save(CategoryFixture.rootActive());
        Category child = categoryRepository.save(CategoryFixture.childActive(root));

        String reqBody = """
        {
            "categoryId": %d,
            "name": "Chair Leg",
            "productType": "COMPONENT",
            "basePrice": 5000,
            "weight": 2.0,
            "width": 10.0,
            "height": 10.0,
            "depth": 10.0,
            "skus": [{
                "price": 4500,
                "stockQuantity": 100
            }],
            "attributes": [],
            "compositions": []
        }
    """.formatted(root.getId());

        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_LEAF_CATEGORY.getCode()))
            .andExpect(jsonPath("$.message").value(CategoryErrorCode.NOT_LEAF_CATEGORY.getMessage()));
    }

    @Test
    @DisplayName("상품 생성 실패 - 단품에 compositions 있는 경우")
    void createFail_componentWithCompositions() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product componentProduct = productRepository.save(ProductFixture.component(category, "Chair Leg"));
        ProductSku childSku = productSkuRepository.save(ProductSkuFixture.sku(componentProduct, 5000L, 100));

        String reqBody = """
        {
            "categoryId": %d,
            "name": "Chair Leg",
            "productType": "COMPONENT",
            "basePrice": 5000,
            "weight": 2.0,
            "width": 10.0,
            "height": 10.0,
            "depth": 10.0,
            "skus": [{
                "price": 4500,
                "stockQuantity": 100
            }],
            "attributes": [],
            "compositions": [
                { "childSkuId": %d, "quantity": 1, "sortOrder": 0 }
            ]
        }
    """.formatted(category.getId(), childSku.getId());

        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION.getCode()))
            .andExpect(jsonPath("$.message").value(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION.getMessage()));
    }

    // ========== 상품 수정 ==========

    @Test
    @DisplayName("단품 수정 성공")
    void updateComponentSuccess() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "Old Name", "설명", ProductType.COMPONENT, dimension)
        );
        ProductSku sku = productSkuRepository.save(ProductSku.create(product, 45000L, 100, "White", "Wood"));

        String reqBody = """
            {
                "categoryId": %d,
                "name": "New Name",
                "description": "새로운 설명",
                "basePrice": 80000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [{
                    "id": %d,
                    "price": 75000,
                    "stockQuantity": 80,
                    "color": "Black",
                    "material": "Metal"
                }],
                "attributes": [],
                "compositions": []
            }
        """.formatted(category.getId(), sku.getId());

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(handler().handlerType(AdminProductControllerV1.class))
            .andExpect(handler().methodName("updateProduct"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("제품이 수정되었습니다."));

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getBasePrice()).isEqualTo(75000);
    }

    @Test
    @DisplayName("완제품 수정 성공 - 구성품 변경")
    void updateCompleteSuccess_withCompositions() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);

        Product componentProduct = productRepository.save(
            Product.create(category, "Chair Leg", null, ProductType.COMPONENT, dimension)
        );
        ProductSku oldChildSku = productSkuRepository.save(ProductSku.create(componentProduct, 5000L, 100, "White", "Wood"));
        ProductSku newChildSku = productSkuRepository.save(ProductSku.create(componentProduct, 5000L, 100, "Black", "Metal"));

        Product completeProduct = productRepository.save(
            Product.create(category, "A Desk", null, ProductType.COMPLETE, dimension)
        );
        ProductSku completeSku = productSkuRepository.save(ProductSku.create(completeProduct, 90000L, 50, "White", "Wood"));
        compositionRepository.save(com.fittura.domain.product.sku.entity.ProductComposition.create(completeProduct, oldChildSku, 4, 0));

        long compositionCountBefore = compositionRepository.count();

        String reqBody = """
            {
                "categoryId": %d,
                "name": "A Desk Updated",
                "basePrice": 120000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [{
                    "id": %d,
                    "price": 110000,
                    "stockQuantity": 40,
                    "color": "White",
                    "material": "Wood"
                }],
                "attributes": [],
                "compositions": [{
                    "childSkuId": %d,
                    "quantity": 2,
                    "sortOrder": 0
                }]
            }
        """.formatted(category.getId(), completeSku.getId(), newChildSku.getId());

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/" + completeProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("제품이 수정되었습니다."));

        assertThat(compositionRepository.count()).isEqualTo(compositionCountBefore);
    }

    @Test
    @DisplayName("상품 수정 성공 - 속성 추가")
    void updateSuccess_withNewAttribute() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "A Desk", null, ProductType.COMPONENT, dimension)
        );
        ProductSku sku = productSkuRepository.save(ProductSku.create(product, 45000L, 100, "White", "Wood"));

        String reqBody = """
            {
                "categoryId": %d,
                "name": "A Desk",
                "basePrice": 50000,
                "weight": 40.5,
                "width": 150.0,
                "height": 100.0,
                "depth": 50.0,
                "skus": [{
                    "id": %d,
                    "price": 45000,
                    "stockQuantity": 100,
                    "color": "White",
                    "material": "Wood"
                }],
                "attributes": [{
                    "attributeKey": "SIZE_LABEL",
                    "attributeValue": "L"
                }],
                "compositions": []
            }
        """.formatted(category.getId(), sku.getId());

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isOk());

        assertThat(productAttributeRepository.findByProductId(product.getId())).hasSize(1);
        assertThat(productAttributeRepository.findByProductId(product.getId()).get(0).getAttributeValue()).isEqualTo("L");
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품 없음")
    void updateFail_productNotFound() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());

        String reqBody = """
            {
                "categoryId": %d,
                "name": "New Name",
                "basePrice": 80000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [{
                    "price": 75000,
                    "stockQuantity": 80,
                    "color": "Black",
                    "material": "Metal"
                }],
                "attributes": [],
                "compositions": []
            }
        """.formatted(category.getId());

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_PRODUCT.getCode()));
    }

    @Test
    @DisplayName("상품 수정 실패 - validation 오류 (skus 비어있음)")
    void updateFail_validationError() throws Exception {
        // given
        String reqBody = """
            {
                "categoryId": 1,
                "name": "New Name",
                "basePrice": 80000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(CommonErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("상품 수정 실패 - 인증 없음")
    void updateFail_unauthorized() throws Exception {
        // given
        String reqBody = """
            {
                "categoryId": 1,
                "name": "New Name",
                "basePrice": 80000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [{"price": 75000, "stockQuantity": 80}],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("상품 수정 실패 - 카테고리 없음")
    void updateFail_categoryNotFound() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Dimension dimension = Dimension.of(40.5, 150.0, 100.0, 50.0);
        Product product = productRepository.save(
            Product.create(category, "A Desk", null, ProductType.COMPONENT, dimension)
        );
        productSkuRepository.save(ProductSku.create(product, 45000L, 100, "White", "Wood"));

        String reqBody = """
            {
                "categoryId": 9999,
                "name": "New Name",
                "basePrice": 80000,
                "weight": 20.0,
                "width": 200.0,
                "height": 120.0,
                "depth": 60.0,
                "skus": [{"price": 75000, "stockQuantity": 80}],
                "attributes": [],
                "compositions": []
            }
        """;

        // when & then
        mockMvc.perform(put(PRODUCT_ADMIN_URL + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode()));
    }


    @Test
    @DisplayName("완제품 생성 실패 - 구성품으로 완제품 SKU 등록")
    void createCompleteFail_childSkuIsComplete() throws Exception {
        // given
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product completeProduct = productRepository.save(ProductFixture.complete(category, "기존 완제품"));
        ProductSku completeSku = productSkuRepository.save(ProductSkuFixture.sku(completeProduct, 10000L, 100));

        String reqBody = """
            {
                "categoryId": %d,
                "name": "A Desk",
                "productType": "COMPLETE",
                "basePrice": 100000,
                "weight": 2.0,
                "width": 10.0,
                "height": 10.0,
                "depth": 10.0,
                "skus": [{
                    "price": 4500,
                    "stockQuantity": 100
                }],
                "attributes": [],
                "compositions": [
                    { "childSkuId": %d, "quantity": 1, "sortOrder": 0 }
                ]
            }
        """.formatted(category.getId(), completeSku.getId());

        mockMvc.perform(post(PRODUCT_ADMIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT.getCode()));
    }
}