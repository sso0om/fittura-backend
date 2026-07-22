package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.dto.request.OrderSearchCondition;
import com.fittura.domain.order.order.dto.response.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fittura.domain.delivery.delivery.entitiy.QDelivery.delivery;
import static com.fittura.domain.order.order.entity.QOrder.order;
import static com.fittura.domain.order.order.entity.QOrderAddress.orderAddress;
import static com.fittura.domain.order.order.entity.QOrderItem.orderItem;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderWithDeliveryResDto> findOrders(Long memberId, OrderSearchCondition condition, Pageable pageable) {
        BooleanExpression[] conditions = {
            order.memberId.eq(memberId),
            order.orderDate.goe(condition.startDate()),
            order.orderDate.lt(condition.endDate()),
            orderNumberEq(condition.orderNumber()),
            productNameContains(condition.productName())
        };

        List<OrderWithDeliveryResDto> rows = queryFactory
            .select(Projections.constructor(OrderWithDeliveryResDto.class,
                order.id,
                order.orderNumber,
                order.status,
                order.orderDate,
                order.finalAmount
            ))
            .from(order)
            .where(conditions)
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (rows.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        List<Long> orderIds = rows.stream()
            .map(OrderWithDeliveryResDto::orderId)
            .toList();

        Map<Long, List<DeliveryResDto>> deliveryMap = queryFactory
            .select(Projections.constructor(DeliveryResDto.class,
                delivery.id,
                delivery.orderId,
                delivery.deliveryType,
                delivery.status,
                orderItem.productName.min(),
                orderItem.id.count().intValue()
            ))
            .from(delivery)
            .join(orderItem).on(orderItem.deliveryId.eq(delivery.id))
            .where(delivery.orderId.in(orderIds))
            .groupBy(delivery.id, delivery.orderId, delivery.deliveryType, delivery.status)
            .orderBy(delivery.id.desc())
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(DeliveryResDto::orderId));

        List<OrderWithDeliveryResDto> orders = rows.stream()
            .map(row -> new OrderWithDeliveryResDto(
                row.orderId(),
                row.orderNumber(),
                row.status(),
                row.orderDate(),
                row.finalAmount(),
                deliveryMap.getOrDefault(row.orderId(), List.of())
            ))
            .toList();

        Long total = queryFactory
            .select(order.count())
            .from(order)
            .where(conditions)
            .fetchOne();

        return new PageImpl<>(orders, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<OrderWithAllResDto> findWithAllByIdAndMemberId(Long orderId, Long memberId) {
        OrderWithAllResDto orderRow = queryFactory
            .select(Projections.constructor(OrderWithAllResDto.class,
                order.id,
                order.orderNumber,
                order.status,
                order.orderDate,
                order.totalAmount,
                order.discountAmount,
                order.pointUsedAmount,
                order.deliveryFee,
                order.finalAmount,
                Projections.constructor(OrderAddressResDto.class,
                    orderAddress.receiverName,
                    orderAddress.phoneNumber,
                    orderAddress.zipCode,
                    orderAddress.address,
                    orderAddress.addressDetail,
                    orderAddress.sido,
                    orderAddress.sigungu,
                    orderAddress.deliveryMemo
                )
            ))
            .from(order)
            .join(orderAddress).on(orderAddress.order.id.eq(order.id))
            .where(
                order.id.eq(orderId),
                order.memberId.eq(memberId)
            )
            .fetchOne();

        if (orderRow == null) return Optional.empty();

        List<OrderItemResDto> items = queryFactory
            .select(Projections.constructor(OrderItemResDto.class,
                orderItem.id,
                orderItem.sku.id,
                orderItem.productName,
                orderItem.skuIdentifier,
                orderItem.unitPrice,
                orderItem.quantity,
                orderItem.discountAmount,
                orderItem.itemTotalAmount,
                orderItem.status
            ))
            .from(orderItem)
            .where(orderItem.order.id.eq(orderId))
            .orderBy(orderItem.id.asc())
            .fetch();

        return Optional.of(new OrderWithAllResDto(
            orderRow.orderId(),
            orderRow.orderNumber(),
            orderRow.status(),
            orderRow.orderDate(),
            orderRow.totalAmount(),
            orderRow.discountAmount(),
            orderRow.pointUsedAmount(),
            orderRow.deliveryFee(),
            orderRow.finalAmount(),
            orderRow.address(),
            items
        ));
    }


    // ========== BooleanExpression ==========



    private BooleanExpression orderNumberEq(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) return null;
        return order.orderNumber.eq(orderNumber.trim());
    }

    private BooleanExpression productNameContains(String productName) {
        if (!StringUtils.hasText(productName)) return null;
        return JPAExpressions
            .selectOne()
            .from(orderItem)
            .where(
                orderItem.order.id.eq(order.id),
                orderItem.productName.containsIgnoreCase(productName)
            )
            .exists();
    }
}
