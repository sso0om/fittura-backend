package com.fittura.domain.order.facade;

import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.service.CartService;
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
}
