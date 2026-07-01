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
            .andExpect(jsonPath("$.data.items").isEmpty())
            .andExpect(jsonPath("$.data.totalPrice").value(0));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 아이템 포함: 상품 정보 및 총 금액 반환")
    void getCartSuccess_withItems() throws Exception {
        // given
        Long memberId = 11L;
        ProductSku sku = savedDefaultSku();  // price: 10000

        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 3));

        // when & then
        mockMvc.perform(get(CART_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("장바구니가 조회되었습니다."))
            .andExpect(jsonPath("$.data.cartId").isNumber())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items[0].productName").value("A Desk"))
            .andExpect(jsonPath("$.data.items[0].unitPrice").value(10000))
            .andExpect(jsonPath("$.data.items[0].quantity").value(3))
            .andExpect(jsonPath("$.data.items[0].itemTotalPrice").value(30000))
            .andExpect(jsonPath("$.data.totalPrice").value(30000));
    }

    @Test
    @DisplayName("장바구니 조회 성공 - ARCHIVED SKU 아이템은 결과에서 제외")
    void getCartSuccess_archivedSkuExcluded() throws Exception {
        // given
        Long memberId = 12L;
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
            .andExpect(jsonPath("$.data.items").isEmpty())
            .andExpect(jsonPath("$.data.totalPrice").value(0));
    }


    // ========== 장바구니 담기 ==========

    @Test
    @DisplayName("장바구니 담기 성공 - 새 장바구니 생성 후 아이템 추가")
    void createCartItemSuccess_newCart() throws Exception {
        // given
        Long memberId = 1L;
        ProductSku sku = savedDefaultSku();

        String reqBody = """
                {
                    "skuId": %d,
                    "quantity": 3
                }
                """.formatted(sku.getId());

        // when & then
        mockMvc.perform(post(CART_URL + "/items")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("제품이 장바구니에 담겼습니다."));

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
                {
                    "skuId": %d,
                    "quantity": 3
                }
                """.formatted(sku.getId());

        // when & then
        mockMvc.perform(post(CART_URL + "/items")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("제품이 장바구니에 담겼습니다."));

        CartItem updatedItem = cartItemRepository.findByCartAndProductSku(cart, sku).orElseThrow();
        assertThat(updatedItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 담기 실패 - 존재하지 않는 SKU")
    void createCartItemFail_notFoundSku() throws Exception {
        // given
        Long memberId = 3L;

        String reqBody = """
                {
                    "skuId": 9999,
                    "quantity": 1
                }
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

    private ProductSku savedDefaultSku() {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));
        return productSkuRepository.save(ProductSkuFixture.sku(product, 10000L, 100));
    }
}