package com.fittura.domain.order.order.service;

import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.order.dto.request.AddressCreateReqDto;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderItem;
import com.fittura.domain.order.order.entity.OrderAddress;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository addressRepository;

    // ========== 주문 ==========

    public Order createOrder(Long memberId, OrderCreateReqDto reqDto) {
        Order order = Order.create(memberId, reqDto.pointUsedAmount());
        orderRepository.save(order);
        return order;
    }

    public void calcAmount(Order order) {
        // TODO: calcDiscountAmount
        // TODO: calcDeliveryFee
        order.calcFinalAmount();
    }


    // ========== 주문 제품 ==========

    public void createOrderItem(CartItem cartItem, Order order) {
        ProductSku sku = cartItem.getProductSku();
        validateSku(cartItem, sku);

        OrderItem orderItem = OrderItem.create(order, sku, cartItem.getQuantity());
        sku.reserveQuantity(cartItem.getQuantity());
        orderItemRepository.save(orderItem);
    }


    // ========== 주문 주소 ==========

    public void createOrderAddress(Order order, AddressCreateReqDto reqDto) {
        OrderAddress orderAddress = OrderAddress.create(
            order, reqDto.receiverName(), reqDto.phoneNumber(),
            reqDto.zipCode(), reqDto.address(), reqDto.addressDetail(),
            reqDto.sido(), reqDto.sigungu(), reqDto.deliveryMemo()
        );
        addressRepository.save(orderAddress);
    }

    private void validateSku(CartItem cartItem, ProductSku sku) {
        String productName = sku.getProduct().getName() + "(" + sku.getSkuIdentifier() + ")";

        if (!sku.isActive()) {
            throw new ServiceException(OrderErrorCode.SKU_MUST_ACTIVE, productName);
        }
        if (!sku.isStockValid(cartItem.getQuantity())) {
            throw new ServiceException(OrderErrorCode.STOCK_NOT_VALID, productName);
        }
    }
}
