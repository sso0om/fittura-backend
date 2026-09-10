package com.fittura.domain.order.order.dto.request;

import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "주문 조회 조건 DTO")
public record OrderSearchCondition(
    String orderNumber,
    String productName,
    LocalDate startDate,
    LocalDate endDate
) {
    public OrderSearchCondition {
        if (startDate.isAfter(endDate)) {
            throw new ServiceException(OrderErrorCode.DATE_RANGE_INVALID);
        }
    }

    public LocalDateTime startDateTime() {
        return startDate.atStartOfDay();
    }

    public LocalDateTime endDateTime() {
        return endDate.equals(LocalDate.MAX)
            ? LocalDate.MAX.atTime(LocalTime.MAX)
            : endDate.plusDays(1).atStartOfDay();
    }
}
