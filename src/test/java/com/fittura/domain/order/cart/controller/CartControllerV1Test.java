package com.fittura.domain.order.cart.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepostiory.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository.CartRepository;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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

        Cart cart = cartRepository.save(Cart.create(memberId));
        cartItemRepository.save(CartItem.create(cart, sku, 2));

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


    // ========== 헬퍼 메서드 ==========

    private ProductSku savedDefaultSku() {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));
        return productSkuRepository.save(ProductSku.create(product, 10000L, 100, "White", "Wood"));
    }
}