package com.fittura.domain.order.facade;

import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.order.order.dto.request.ClaimOrderReqDto;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.dto.request.OrderSearchCondition;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public Page<OrderWithDeliveryResDto> getOrders(Long memberId, OrderSearchCondition searchCondition, Pageable pageable) {
        return orderService.getOrders(memberId, searchCondition, pageable);
    }

    @Transactional(readOnly = true)
    public OrderWithAllResDto getOrderByIdAndMember(Long orderId, Long memberId) {
        return orderService.getOrderDetail(orderId, memberId);
    }

    @Transactional
    public Long createOrder(Long memberId, OrderCreateReqDto reqDto) {
        List<CartItem> cartItems = cartService.getItemsByIdAndMember(reqDto.cartItems(), memberId);
        orderService.validateCartItems(cartItems);

        Order order = orderService.createOrder(memberId, reqDto);
        for(CartItem cartItem : cartItems) {
            orderService.createOrderItem(cartItem, order);
        }
        cartService.deleteCartItems(cartItems);
        orderService.createOrderAddress(order, reqDto.orderAddress());
        orderService.calcAmount(order);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(Long memberId, Long orderId, ClaimOrderReqDto reqDto) {
        Order order = orderService.getOrder(orderId, memberId);
        order.validateCancel();

        // TODO: Delivery 목록 조회, 상태 검증

        // OrderClaim 생성 (CANCEL, 사유, 대상 아이템)

        // SKU 잠금 후 재고 롤백

        // TODO: OrderItem status → CANCEL_REQUESTED
        // TODO: Payment 환불 (PG 취소 API)
        // TODO: Point 환급/회수

        // 상태 전이: OrderItem, Delivery, Order → CANCELLED
    }
}
