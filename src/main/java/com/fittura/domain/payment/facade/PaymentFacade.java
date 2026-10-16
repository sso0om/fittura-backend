package com.fittura.domain.payment.facade;

import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.service.OrderService;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.dto.request.PaymentApproveReqDto;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.service.PaymentService;
import com.fittura.domain.payment.pg.PaymentGateway;
import com.fittura.domain.payment.pg.PgConfirmCommand;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import com.fittura.domain.product.sku.service.SkuService;
import com.fittura.domain.payment.payment.error.PgPaymentFailedException;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    @Transactional(noRollbackFor = PgPaymentFailedException.class)
    public Long approvePayment(Long memberId, Long paymentId, PaymentApproveReqDto reqDto) {
        Payment payment = paymentService.getPaymentForUpdate(paymentId);
        payment.validatePayable();

        Order order = orderService.getOrder(payment.getOrderId(), memberId);
        order.validatePayable();

        PgPaymentResponse paymentRes = paymentGateway.confirm(
            new PgConfirmCommand(reqDto.paymentKey(), payment.getPaymentNumber(), payment.getTotalAmount())
        );

        if (paymentRes.status() == PaymentStatus.FAILED) {
            handleRejectedPayment(payment, paymentRes, order);
            throw new PgPaymentFailedException(PaymentErrorCode.PG_REJECTED, paymentRes.errorMessage());
        }
        if (paymentRes.status() == PaymentStatus.UNKNOWN) {
            payment.unknown(paymentRes.rawResponse());
            throw new PgPaymentFailedException(PaymentErrorCode.PG_RESULT_UNKNOWN);
        }

        try {
            paymentService.validatePgResponse(payment, paymentRes);
        } catch (ServiceException e) {
            payment.unknown(paymentRes.rawResponse());
            log.error("토스 승인했으나 결과 확정 실패 orderId={}", paymentRes.paymentNumber(), e);
            throw new PgPaymentFailedException(PaymentErrorCode.PG_RESULT_UNKNOWN);
        }

        paymentService.approvePayment(payment, paymentRes);
        skuService.confirmSku(order.getQuantityBySkuId());
        order.paid();

        cartService.deleteCartItems(memberId, order.getCartItemIds());

        return order.getId();
    }

    private void handleRejectedPayment(Payment payment, PgPaymentResponse paymentRes, Order order) {
        payment.fail(paymentRes.rawResponse());

        order.expired();
        skuService.releaseSku(order.getQuantityBySkuId());
        orderService.deleteOrderAddressByOrder(order);
    }
}
