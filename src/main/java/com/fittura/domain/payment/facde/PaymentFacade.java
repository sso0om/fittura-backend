package com.fittura.domain.payment.facde;

import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import com.fittura.domain.payment.payment.dto.request.PaymentApproveReqDto;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.service.PaymentService;
import com.fittura.domain.payment.pg.PaymentGateway;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final SkuService skuService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;

    @Transactional
    public PaymentPrepareResDto preparePayment(Long memberId, PaymentPrepareReqDto reqDto) {
        Order order = orderService.getOrder(reqDto.orderId(), memberId);
        return paymentService.createPayment(order, reqDto);
    }

    @Transactional
    public Long approvePayment(Long memberId, Long paymentId, PaymentApproveReqDto reqDto) {
        Payment payment = paymentService.getPayment(paymentId);
        payment.validatePayable();

        Order order = orderService.getOrder(payment.getOrderId(), memberId);
        order.validatePayable();

        PgPaymentResponse paymentRes = paymentGateway.getPayment(reqDto.paymentKey());
        // TODO: PG 실패 응답 처리

        paymentService.validatePgResponse(payment, paymentRes);

        paymentService.approvePayment(payment, paymentRes);
        skuService.confirmSku(order.getQuantityBySkuId());
        order.paid();

        // TODO: CartItem 제거

        // Order Id 반환
        return order.getId();
    }
}
