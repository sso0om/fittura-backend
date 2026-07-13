package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.dto.response.OrderAddressResDto;
import com.fittura.domain.order.order.dto.response.OrderItemResDto;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.fittura.domain.order.order.entity.QOrder.order;
import static com.fittura.domain.order.order.entity.QOrderAddress.orderAddress;
import static com.fittura.domain.order.order.entity.QOrderItem.orderItem;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
}
