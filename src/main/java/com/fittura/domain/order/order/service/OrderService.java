package com.fittura.domain.order.order.service;

import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.order.constant.ClaimType;
import com.fittura.domain.order.order.dto.request.*;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import com.fittura.domain.order.order.entity.*;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.ClaimRepository;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.error.ItemError;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository addressRepository;
    private final ClaimRepository claimRepository;

    // ========== 주문 ==========

    public Page<OrderWithDeliveryResDto> getOrders(Long memberId, OrderSearchCondition searchCondition, Pageable pageable) {
        return orderRepository.findOrders(memberId, searchCondition, pageable);
    }

    public OrderWithAllResDto getOrderDetail(Long orderId, Long memberId) {
        return orderRepository.findWithAllByIdAndMemberId(orderId, memberId)
            .orElseThrow(() -> new ServiceException(OrderErrorCode.NOT_FOUND_ORDER));
    }

    public Order getOrder(Long orderId, Long memberId) {
        return orderRepository.findByIdAndMemberId(orderId, memberId)
            .orElseThrow(() -> new ServiceException(OrderErrorCode.NOT_FOUND_ORDER));
    }

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


    // ========== 취소/환불/교환 ==========

    public Claim createCancelClaim(Order order, ClaimOrderReqDto reqDto) {
        Map<Long, Integer> quantityByItemId = getQuantityByItemId(reqDto);
        List<OrderItem> orderItems = getClaimItems(order, quantityByItemId);

        Claim claim = Claim.create(order, ClaimType.CANCEL, reqDto.reason(), reqDto.reasonDetail());
        for (OrderItem orderItem : orderItems) {
            ClaimItem.create(claim, orderItem, quantityByItemId.get(orderItem.getId()));
        }
        claimRepository.save(claim);

        return claim;
    }

    public void requestCancel(Order order, List<ClaimItem> claimItems) {
        claimItems.forEach(ci -> ci.getOrderItem().requestCancel());
    }

    public void cancelItems(List<ClaimItem> claimItems) {
        claimItems.forEach(ci -> ci.getOrderItem().reflectCancel());
    }

    public void cancelIfAllItemsCancelled(Order order) {
        boolean allCancelled = order.getItems().stream().allMatch(OrderItem::isCanceled);

        if (allCancelled) order.cancel();
    }


    // ========== 유효성 검사 ==========

    public void validateCartItems(List<CartItem> cartItems) {
        List<ItemError> errors = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductSku sku = cartItem.getProductSku();
            String productName = sku.getProduct().getName() + "(" + sku.getSkuIdentifier() + ")";

            if (!sku.isActive()) {
                errors.add(ItemError.of(productName, OrderErrorCode.SKU_MUST_ACTIVE));
            } else if (!sku.getProduct().isActive()) {
                errors.add(ItemError.of(productName, OrderErrorCode.PRODUCT_MUST_ACTIVE));
            } else if (!sku.isStockValid(cartItem.getQuantity())) {
                errors.add(ItemError.of(productName, OrderErrorCode.STOCK_NOT_VALID));
            }
        }

        if (!errors.isEmpty()) {
            throw new ServiceException(OrderErrorCode.CART_ITEMS_NOT_VALID, errors);
        }
    }

    private void validateClaimItems(List<OrderItem> orderItems, Map<Long, Integer> quantityByItemId) {
        if (orderItems.size() != quantityByItemId.size()) {
            throw new ServiceException(OrderErrorCode.NOT_FOUND_ORDER_ITEM);
        }

        List<ItemError> errors = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            if (!orderItem.isOrdered()) {
                errors.add(ItemError.of(orderItem.getProductName(), OrderErrorCode.NOT_VALID_STATUS));
            } else if (!orderItem.isQuantityValid(quantityByItemId.get(orderItem.getId()))) {
                errors.add(ItemError.of(orderItem.getProductName(), OrderErrorCode.QUANTITY_NOT_VALID));
            }
        }

        if (!errors.isEmpty()) {
            throw new ServiceException(OrderErrorCode.CLAIM_ITEMS_NOT_VALID, errors);
        }
    }


    // ========== 헬퍼 메서드 ==========

    private Map<Long, Integer> getQuantityByItemId(ClaimOrderReqDto reqDto) {
        return reqDto.claimItems().stream()
            .collect(Collectors.toMap(
                ClaimItemReqDto::orderItemId,
                ClaimItemReqDto::quantity
            ));
    }

    private List<OrderItem> getClaimItems(Order order, Map<Long, Integer> quantityByItemId) {
        List<OrderItem> orderItems = order.getItems().stream()
            .filter(oi -> quantityByItemId.containsKey(oi.getId()))
            .toList();

        validateClaimItems(orderItems, quantityByItemId);
        return orderItems;
    }
}
