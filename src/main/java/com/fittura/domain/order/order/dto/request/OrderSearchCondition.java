package com.fittura.domain.order.order.dto.request;

import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.global.exception.ServiceException;

import java.time.LocalDateTime;

public record OrderSearchCondition(
    String orderNumber,
    String productName,
    LocalDateTime startDate,
    LocalDateTime endDate
) {
    public OrderSearchCondition {
        if (startDate.isAfter(endDate)) {
            throw new ServiceException(OrderErrorCode.DATE_RANGE_INVALID);
        }
    }
}
