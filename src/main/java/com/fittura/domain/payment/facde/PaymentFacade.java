package com.fittura.domain.payment.facde;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @Transactional
    public PaymentPrepareResDto preparePayment(Long memberId, PaymentPrepareReqDto reqDto) {
        Order order = orderService.getOrder(reqDto.orderId(), memberId);
        return paymentService.createPayment(order, reqDto);
    }
}
