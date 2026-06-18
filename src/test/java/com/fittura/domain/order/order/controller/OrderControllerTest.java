package com.fittura.domain.order.order.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.error.CartErrorCode;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.product.product.entity.Product;
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

class OrderControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderAddressRepository addressRepository;

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

    private static final String ORDER_URL = "/api/v1/orders";


    // ========== 주문 생성 ==========

    @Test
    @DisplayName("주문 생성 성공 - 제품 1개")
    void createOrderSuccess() throws Exception {
        // given
        Long memberId = 1L;
        ProductSku sku = savedDefaultSku();
        Cart cart = cartRepository.save(Cart.create(memberId));
        CartItem cartItem = cartItemRepository.save(CartItem.create(cart, sku, 2));

        String reqBody = """
                {
                    "cartItems": [%d],
                    "pointUsedAmount": 1000,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "addressDetail": "시청역",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """.formatted(cartItem.getId());

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("주문이 생성되었습니다."))
            .andExpect(jsonPath("$.data").isNumber());

        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderItemRepository.count()).isEqualTo(1);
        assertThat(addressRepository.count()).isEqualTo(1);
        assertThat(cartItemRepository.findById(cartItem.getId())).isEmpty();
    }

    @Test
    @DisplayName("주문 생성 성공 - 제품 여러개")
    void createOrderSuccess_multipleCartItems() throws Exception {
        // given
        Long memberId = 2L;
        ProductSku sku1 = savedDefaultSku();
        ProductSku sku2 = savedDefaultSku();
        Cart cart = cartRepository.save(Cart.create(memberId));
        CartItem cartItem1 = cartItemRepository.save(CartItem.create(cart, sku1, 1));
        CartItem cartItem2 = cartItemRepository.save(CartItem.create(cart, sku2, 3));

        String reqBody = """
                {
                    "cartItems": [%d, %d],
                    "pointUsedAmount": 500,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """.formatted(cartItem1.getId(), cartItem2.getId());

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"));

        assertThat(orderItemRepository.count()).isEqualTo(2);
        assertThat(cartItemRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 생성 실패 - 다른 회원의 장바구니 아이템으로 주문 시도")
    void createOrderFail_cartItemNotOwnedByMember() throws Exception {
        // given
        Long ownerMemberId = 20L;
        Long attackerMemberId = 21L;
        ProductSku sku = savedDefaultSku();
        Cart ownerCart = cartRepository.save(Cart.create(ownerMemberId));
        CartItem ownerItem = cartItemRepository.save(CartItem.create(ownerCart, sku, 1));

        String reqBody = """
                {
                    "cartItems": [%d],
                    "pointUsedAmount": 0,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """.formatted(ownerItem.getId());

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(attackerMemberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 장바구니 아이템")
    void createOrderFail_cartItemNotFound() throws Exception {
        // given
        Long memberId = 10L;

        String reqBody = """
                {
                    "cartItems": [9999],
                    "pointUsedAmount": 1000,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """;

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(CartErrorCode.NOT_FOUND_ITEM.getCode()));
    }

    @Test
    @DisplayName("주문 생성 실패 - 비활성(품절) SKU 주문 시도")
    void createOrderFail_skuNotActive() throws Exception {
        // given
        Long memberId = 11L;
        ProductSku sku = savedDefaultSku();
        sku.soldOut();
        productSkuRepository.save(sku);

        Cart cart = cartRepository.save(Cart.create(memberId));
        CartItem cartItem = cartItemRepository.save(CartItem.create(cart, sku, 1));

        String reqBody = """
                {
                    "cartItems": [%d],
                    "pointUsedAmount": 1000,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """.formatted(cartItem.getId());

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.SKU_MUST_ACTIVE.getCode()));
    }

    @Test
    @DisplayName("주문 생성 실패 - 재고 부족")
    void createOrderFail_stockNotValid() throws Exception {
        // given
        Long memberId = 12L;
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));
        ProductSku sku = productSkuRepository.save(ProductSku.create(product, 10000L, 2, "White", "Wood"));

        Cart cart = cartRepository.save(Cart.create(memberId));
        CartItem cartItem = cartItemRepository.save(CartItem.create(cart, sku, 5));

        String reqBody = """
                {
                    "cartItems": [%d],
                    "pointUsedAmount": 1000,
                    "orderAddress": {
                        "receiverName": "홍길동",
                        "phoneNumber": "01012341234",
                        "zipCode": "12345",
                        "address": "서울특별시 중구 서소문로 127",
                        "sido": "서울특별시",
                        "sigungu": "중구"
                    }
                }
                """.formatted(cartItem.getId());

        // when & then
        mockMvc.perform(post(ORDER_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.STOCK_NOT_VALID.getCode()));
    }


    // ========== 헬퍼 메서드 ==========

    private ProductSku savedDefaultSku() {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));
        return productSkuRepository.save(ProductSku.create(product, 10000L, 100, "White", "Wood"));
    }
}