package com.fittura.domain.order.cart.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fittura.domain.order.cart.error.CartErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerV1Test extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSkuRepository productSkuRepository;

    private static final String CART_URL = "/api/v1/cart";

    // ========== 장바구니 조회 ==========

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 없음: 빈 응답 반환")
    void getCartSuccess_noCart() throws Exception {
        // given
        Long memberId = 10L;

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("장바구니가 조회되었습니다."))
            .andExpect(jsonPath("$.data.cartId").value((Object) null))
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 일반배송 상품: 항목 배송비 0, 할인 없으면 적용가는 정가")
    void getCartSuccess_parcelItem() throws Exception {
        // given
        Long memberId = 11L;
        ProductSku sku = savedSku("A Desk", DeliveryType.PARCEL, 10_000L, null);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 3));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("장바구니가 조회되었습니다."))
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.items[0].productName").value("A Desk"))
            .andExpect(jsonPath("$.data.items[0].deliveryType").value("PARCEL"))
            .andExpect(jsonPath("$.data.items[0].deliveryFee").value(0))
            .andExpect(jsonPath("$.data.items[0].originalPrice").value(10_000))
            .andExpect(jsonPath("$.data.items[0].salePrice").value(10_000))
            .andExpect(jsonPath("$.data.items[0].discountRate").value(0))
            .andExpect(jsonPath("$.data.items[0].itemTotalPrice").value(30_000));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 기사배송 상품: 항목 배송비는 기본 배송비 x 수량")
    void getCartSuccess_installationItem() throws Exception {
        // given
        Long memberId = 12L;
        ProductSku sku = savedSku("A Chair", DeliveryType.INSTALLATION, 200_000L, null);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 3));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].deliveryType").value("INSTALLATION"))
            .andExpect(jsonPath("$.data.items[0].deliveryFee")
                .value(DeliveryType.INSTALLATION.getBaseFee().intValue() * 3))
            .andExpect(jsonPath("$.data.items[0].itemTotalPrice").value(600_000));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 할인 SKU: 적용가/할인율/합계는 할인가 기준")
    void getCartSuccess_discountedSku() throws Exception {
        // given
        Long memberId = 13L;
        ProductSku sku = savedSku("A Desk", DeliveryType.PARCEL, 100_000L, 80_000L);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].originalPrice").value(100_000))
            .andExpect(jsonPath("$.data.items[0].salePrice").value(80_000))
            .andExpect(jsonPath("$.data.items[0].discountRate").value(20))
            .andExpect(jsonPath("$.data.items[0].itemTotalPrice").value(160_000));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - ARCHIVED SKU 아이템은 결과에서 제외")
    void getCartSuccess_archivedSkuExcluded() throws Exception {
        // given
        Long memberId = 14L;
        ProductSku sku = savedDefaultSku();
        sku.archive();
        productSkuRepository.save(sku);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cartId").isNumber())
            .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 구매 불가 상품도 상태와 함께 그대로 반환 (숨기지 않음)")
    void getCartSuccess_discontinuedProductIncluded() throws Exception {
        // given
        Long memberId = 16L;
        ProductSku sku = savedDefaultSku();
        Product product = sku.getProduct();
        product.discontinue();
        productRepository.save(product);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.items[0].productStatus").value("DISCONTINUED"));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 판매 가능 SKU가 앞쪽에 정렬됨")
    void getCartSuccess_activeSkuFirst() throws Exception {
        // given
        Long memberId = 17L;
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));

        ProductSku activeSku = productSkuRepository.save(ProductSkuFixture.sku(product, 10_000L, null, 100));
        ProductSku pausedSku = productSkuRepository.save(ProductSkuFixture.sku(product, 20_000L, null, 100));
        pausedSku.pause();
        productSkuRepository.save(pausedSku);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, pausedSku, 1));
        cartItemRepository.save(CartItemFixture.cartItem(cart, activeSku, 1));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(2))
            .andExpect(jsonPath("$.data.items[0].skuId").value(activeSku.getId().intValue()))
            .andExpect(jsonPath("$.data.items[1].skuId").value(pausedSku.getId().intValue()));
    }


    // ========== 장바구니 담기 ==========

    @Test
    @DisplayName("장바구니 담기 성공 - 새 장바구니 생성 후 아이템 추가")
    void createCartItemSuccess_newCart() throws Exception {
        // given
        Long memberId = 1L;
        ProductSku sku = savedDefaultSku();

        String reqBody = """
            [{
                "skuId": %d,
                "quantity": 3
            }]
            """.formatted(sku.getId());

        // when & then
        mockMvc.perform(post(CART_URL + "/items")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("장바구니에 담겼습니다."));

        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow();
        CartItem cartItem = cartItemRepository.findByCartAndProductSku(cart, sku).orElseThrow();
        assertThat(cartItem.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 기존 장바구니에 동일 아이템 담으면 수량 누적")
    void createCartItemSuccess_existingItem_quantityAccumulates() throws Exception {
        // given
        Long memberId = 2L;
        ProductSku sku = savedDefaultSku();

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        String reqBody = """
            [{
                "skuId": %d,
                "quantity": 3
            }]
            """.formatted(sku.getId());

        // when & then
        mockMvc.perform(post(CART_URL + "/items")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("장바구니에 담겼습니다."));

        CartItem updatedItem = cartItemRepository.findByCartAndProductSku(cart, sku).orElseThrow();
        assertThat(updatedItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 담기 실패 - 존재하지 않는 SKU")
    void createCartItemFail_notFoundSku() throws Exception {
        // given
        Long memberId = 3L;

        String reqBody = """
            [{
                "skuId": 9999,
                "quantity": 1
            }]
            """;

        // when & then
        mockMvc.perform(post(CART_URL + "/items")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_SKU.getCode()));
    }


    // ========== 장바구니 아이템 수량 수정 ==========

    @Test
    @DisplayName("장바구니 아이템 수량 수정 성공")
    void updateCartItemSuccess() throws Exception {
        // given
        Long memberId = 20L;
        ProductSku sku = savedDefaultSku();
        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        String reqBody = """
            {
                "quantity": 7
            }
            """;

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId())
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("제품의 수량이 수정되었습니다."));

        CartItem updated = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 실패 - 존재하지 않는 아이템")
    void updateCartItemFail_notFoundItem() throws Exception {
        // given
        Long memberId = 21L;

        String reqBody = """
            {
                "quantity": 3
            }
            """;

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/9999")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 실패 - 다른 회원의 아이템")
    void updateCartItemFail_otherMembersItem() throws Exception {
        // given
        Long ownerMemberId = 22L;
        Long otherMemberId = 23L;
        ProductSku sku = savedDefaultSku();
        Cart ownerCart = cartRepository.save(CartFixture.cart(ownerMemberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(ownerCart, sku, 2));

        String reqBody = """
            {
                "quantity": 5
            }
            """;

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId())
                .header("Authorization", userBearerToken(otherMemberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }


    // ========== 장바구니 아이템 옵션(SKU) 변경 ==========

    @Test
    @DisplayName("장바구니 아이템 옵션 변경 성공 - 같은 상품의 다른 SKU로 변경, 수량도 함께 변경")
    void updateCartItemSkuSuccess() throws Exception {
        // given
        Long memberId = 30L;
        Product product = savedActiveProduct("A Desk");
        ProductSku sku = productSkuRepository.save(ProductSkuFixture.sku(product, 10_000L, 100));
        ProductSku changeSku = productSkuRepository.save(ProductSkuFixture.sku(product, 12_000L, 100));

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        String reqBody = """
            {
                "skuId": %d,
                "quantity": 5
            }
            """.formatted(changeSku.getId());

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId() + "/sku")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isOk());

        CartItem updated = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(updated.getProductSku().getId()).isEqualTo(changeSku.getId());
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 아이템 옵션 변경 실패 - 이미 장바구니에 담긴 SKU")
    void updateCartItemSkuFail_duplicate() throws Exception {
        // given
        Long memberId = 31L;
        Product product = savedActiveProduct("A Desk");
        ProductSku sku = productSkuRepository.save(ProductSkuFixture.sku(product, 10_000L, 100));
        ProductSku otherSku = productSkuRepository.save(ProductSkuFixture.sku(product, 12_000L, 100));

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));
        cartItemRepository.save(CartItemFixture.cartItem(cart, otherSku, 1));

        String reqBody = """
            {
                "skuId": %d,
                "quantity": 2
            }
            """.formatted(otherSku.getId());

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId() + "/sku")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(CartErrorCode.DUPLICATE_CART_ITEM.getCode()));
    }

    @Test
    @DisplayName("장바구니 아이템 옵션 변경 실패 - 판매 중지된 SKU")
    void updateCartItemSkuFail_notSellable() throws Exception {
        // given
        Long memberId = 32L;
        Product product = savedActiveProduct("A Desk");
        ProductSku sku = productSkuRepository.save(ProductSkuFixture.sku(product, 10_000L, 100));

        ProductSku pausedSku = ProductSkuFixture.sku(product, 12_000L, 100);
        pausedSku.pause();
        productSkuRepository.save(pausedSku);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        String reqBody = """
            {
                "skuId": %d,
                "quantity": 2
            }
            """.formatted(pausedSku.getId());

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId() + "/sku")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_ACTIVE_SKU.getCode()));
    }

    @Test
    @DisplayName("장바구니 아이템 옵션 변경 실패 - 다른 상품의 SKU")
    void updateCartItemSkuFail_otherProductSku() throws Exception {
        // given
        Long memberId = 33L;
        ProductSku sku = savedSku("A Desk", DeliveryType.PARCEL, 10_000L, null);
        ProductSku otherProductSku = savedSku("B Chair", DeliveryType.PARCEL, 20_000L, null);

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        String reqBody = """
            {
                "skuId": %d,
                "quantity": 2
            }
            """.formatted(otherProductSku.getId());

        // when & then
        mockMvc.perform(patch(CART_URL + "/items/" + cartItem.getId() + "/sku")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ProductErrorCode.NOT_FOUND_SKU.getCode()));
    }


    // ========== 장바구니 아이템 삭제 ==========

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void deleteCartItemSuccess() throws Exception {
        // given
        Long memberId = 30L;
        ProductSku sku = savedDefaultSku();
        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 2));

        // when & then
        mockMvc.perform(delete(CART_URL + "/items/" + cartItem.getId())
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("장바구니에서 제품을 삭제하였습니다."));

        assertThat(cartItemRepository.findById(cartItem.getId())).isEmpty();
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패 - 존재하지 않는 아이템")
    void deleteCartItemFail_notFoundItem() throws Exception {
        // given
        Long memberId = 31L;

        // when & then
        mockMvc.perform(delete(CART_URL + "/items/9999")
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패 - 다른 회원의 아이템")
    void deleteCartItemFail_otherMembersItem() throws Exception {
        // given
        Long ownerMemberId = 32L;
        Long otherMemberId = 33L;
        ProductSku sku = savedDefaultSku();
        Cart ownerCart = cartRepository.save(CartFixture.cart(ownerMemberId));
        CartItem cartItem = cartItemRepository.save(CartItemFixture.cartItem(ownerCart, sku, 2));

        // when & then
        mockMvc.perform(delete(CART_URL + "/items/" + cartItem.getId())
                .header("Authorization", userBearerToken(otherMemberId)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }


    // ========== 헬퍼 메서드 ==========

    private Product savedActiveProduct(String name) {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = ProductFixture.product(category, name, ProductType.COMPONENT, DeliveryType.PARCEL);
        product.activate();
        return productRepository.save(product);
    }

    private ProductSku savedDefaultSku() {
        return savedSku("A Desk", DeliveryType.PARCEL, 10_000L, null);
    }

    private ProductSku savedSku(String name, DeliveryType deliveryType, Long price, Long salePrice) {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = ProductFixture.product(category, name, ProductType.COMPONENT, deliveryType);
        product.activate();
        productRepository.save(product);

        return productSkuRepository.save(ProductSkuFixture.sku(product, price, salePrice, 100));
    }
}
