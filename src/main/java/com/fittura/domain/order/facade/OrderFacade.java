package com.fittura.domain.order.facade;

import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.service.MemberAddressService;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.order.order.dto.request.*;
import com.fittura.domain.order.order.dto.response.OrderPreviewItemResDto;
import com.fittura.domain.order.order.dto.response.OrderPreviewResDto;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import com.fittura.domain.order.order.entity.Claim;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import com.fittura.domain.order.order.util.OrderCalculation;
import com.fittura.domain.order.order.util.OrderCalculator;
import com.fittura.domain.order.order.util.OrderItemCalculation;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CartService cartService;
    private final SkuService skuService;
    private final MemberAddressService addressService;

    @Transactional(readOnly = true)
    public Page<OrderWithDeliveryResDto> getOrders(Long memberId, OrderSearchCondition searchCondition, Pageable pageable) {
        return orderService.getOrders(memberId, searchCondition, pageable);
    }

    @Transactional(readOnly = true)
    public OrderWithAllResDto getOrderByIdAndMember(Long orderId, Long memberId) {
        return orderService.getOrderDetail(orderId, memberId);
    }

    @Transactional(readOnly = true)
    public OrderPreviewResDto getOrderPreviewCart(Long memberId, CartOrderPreviewReqDto reqDto) {
        List<CartItem> cartItems = cartService.getItemsByIdAndMember(reqDto.cartItemIds(), memberId);
        orderService.validateCartItems(cartItems);

        // TODO: 권역 할증 도입 시 배송지 zipCode로 배송비 반영
        if (reqDto.addressId() != null) {
            addressService.getAddress(memberId, reqDto.addressId());
        }

        List<OrderPreviewItemResDto> previewItems = toPreviewItems(cartItems);
        List<OrderItemCalculation> itemCalculations = getItemCalculations(cartItems);
        OrderCalculation calculation = OrderCalculator.calculate(itemCalculations, 0L);

        return OrderPreviewResDto.of(previewItems, calculation);
    }

    @Transactional(readOnly = true)
    public OrderPreviewResDto getOrderPreviewDirect(Long memberId, DirectOrderPreviewReqDto reqDto) {
        orderService.validateNoDuplicateSku(reqDto.orderSkus());

        Map<Long, Integer> quantityBySkuId = toQuantityBySkuId(reqDto.orderSkus());
        List<ProductSku> skus = skuService.getSkusWithDetailById(quantityBySkuId.keySet());
        orderService.validateDirectSkus(skus, quantityBySkuId);

        // TODO: 권역 할증 도입 시 배송지 zipCode로 배송비 반영
        if (reqDto.addressId() != null) {
            addressService.getAddress(memberId, reqDto.addressId());
        }

        List<OrderPreviewItemResDto> previewItems = toPreviewItems(skus, quantityBySkuId);
        List<OrderItemCalculation> itemCalculations = getItemCalculations(skus, quantityBySkuId);
        OrderCalculation calculation = OrderCalculator.calculate(itemCalculations, 0L);

        return OrderPreviewResDto.of(previewItems, calculation);
    }

    @Transactional
    public Long createOrderCart(Long memberId, CartOrderCreateReqDto reqDto) {
        List<CartItem> cartItems = cartService.getItemsByIdAndMemberForUpdate(reqDto.cartItemIds(), memberId);
        orderService.validateCartItems(cartItems);

        MemberAddress memberAddress = addressService.getAddressByIdAndMember(reqDto.addressId(), memberId);

        Order order = orderService.createOrder(memberId, reqDto.pointUsedAmount());
        for (CartItem cartItem : cartItems) {
            orderService.createOrderItem(cartItem, order);
        }
        orderService.createOrderAddress(order, memberAddress, reqDto.deliveryMemo());
        order.calcAmount();
        return order.getId();
    }

    @Transactional
    public Long createOrderDirect(Long memberId, DirectOrderCreateReqDto reqDto) {
        orderService.validateNoDuplicateSku(reqDto.orderSkus());

        // TODO: 권역 할증 도입 시 배송지 zipCode로 배송비 반영
        MemberAddress memberAddress = addressService.getAddressByIdAndMember(reqDto.addressId(), memberId);

        Map<Long, Integer> quantityBySkuId = toQuantityBySkuId(reqDto.orderSkus());
        List<ProductSku> skus = skuService.getSkusWithDetailByIdForUpdate(quantityBySkuId.keySet());
        orderService.validateDirectSkus(skus, quantityBySkuId);

        Order order = orderService.createOrder(memberId, reqDto.pointUsedAmount());
        for (ProductSku sku : skus) {
            orderService.createOrderItem(sku, quantityBySkuId.get(sku.getId()), order);
        }
        orderService.createOrderAddress(order, memberAddress, reqDto.deliveryMemo());
        order.calcAmount();
        return order.getId();
    }

    @Transactional
    public void cancelOrder(Long memberId, Long orderId, ClaimOrderReqDto reqDto) {
        Order order = orderService.getOrder(orderId, memberId);
        order.validateCancel();

        // TODO: Delivery 목록 조회, 상태 검증

        Claim claim = orderService.createCancelClaim(order, reqDto);

        if (order.isPaid()) {
            completeCancellation(order, claim);
        } else {
            orderService.requestCancel(order, claim.getItems());
        }

        // TODO: OrderItem status → CANCEL_REQUESTED
    }

    @Transactional
    public void completeCancellation(Order order, Claim claim) {
        claim.approve();
        skuService.restoreStock(claim.getQuantityBySkuId());

        // TODO: Payment 환불 (PG 취소 API)
        // TODO: Point 환급/회수

        // 상태 전이: OrderItem, Delivery, Order → CANCELLED
        claim.complete();
        orderService.cancelItems(claim.getItems());
        // Delivery
        orderService.cancelIfAllItemsCancelled(order);
    }


    // ========== 헬퍼 메서드 ==========

    private Map<Long, Integer> toQuantityBySkuId(List<OrderSkuReqDto> orderSkus) {
        return orderSkus.stream()
            .collect(Collectors.toMap(OrderSkuReqDto::skuId, OrderSkuReqDto::quantity));
    }

    private List<OrderPreviewItemResDto> toPreviewItems(List<CartItem> cartItems) {
        return cartItems.stream()
            .map(ci -> OrderPreviewItemResDto.from(ci.getProductSku(), ci.getQuantity()))
            .toList();
    }

    private List<OrderPreviewItemResDto> toPreviewItems(List<ProductSku> skus, Map<Long, Integer> quantityBySkuId) {
        return skus.stream()
            .map(sku -> OrderPreviewItemResDto.from(sku, quantityBySkuId.get(sku.getId())))
            .toList();
    }

    private List<OrderItemCalculation> getItemCalculations(List<CartItem> cartItems) {
        return cartItems.stream()
            .map(ci -> OrderItemCalculation.of(ci.getProductSku(), ci.getQuantity()))
            .toList();
    }

    private List<OrderItemCalculation> getItemCalculations(List<ProductSku> skus, Map<Long, Integer> quantityBySkuId) {
        return skus.stream()
            .map(sku -> OrderItemCalculation.of(sku, quantityBySkuId.get(sku.getId())))
            .toList();
    }
}
