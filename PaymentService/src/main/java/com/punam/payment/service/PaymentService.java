package com.punam.payment.service;

import com.punam.payment.model.PaymentRequest;
import com.punam.payment.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest request);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);
}
