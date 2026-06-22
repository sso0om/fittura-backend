package com.fittura.domain.order.facade;

import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CartService cartService;

    @Transactional
    public Long createOrder(Long memberId, OrderCreateReqDto reqDto) {
        Order order = orderService.createOrder(memberId, reqDto);

        List<CartItem> cartItems = cartService.getItemsByIdAndMember(reqDto.cartItems(), memberId);
        for(CartItem cartItem : cartItems) {
            orderService.createOrderItem(cartItem, order);
        }
        cartService.deleteCartItems(cartItems);
        orderService.createOrderAddress(order, reqDto.orderAddress());
        orderService.calcAmount(order);

        return order.getId();
    }
}
