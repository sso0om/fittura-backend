package com.fittura.domain.payment.payment.error;

import com.fittura.global.error.ErrorCode;
import com.fittura.global.exception.ServiceException;

public class PgPaymentFailedException extends ServiceException {

    public PgPaymentFailedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PgPaymentFailedException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }
}
