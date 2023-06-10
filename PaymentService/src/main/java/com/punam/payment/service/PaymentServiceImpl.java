package com.punam.payment.service;

import com.punam.payment.entity.Transaction;
import com.punam.payment.model.PaymentMode;
import com.punam.payment.model.PaymentRequest;
import com.punam.payment.model.PaymentResponse;
import com.punam.payment.repository.PaymentRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public long doPayment(PaymentRequest request) {

        log.info("Payment Request Received : {}", request);
        Transaction transaction = Transaction.builder()
                .paymentDate(Instant.now())
                .paymentMode(request.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(request.getOrderId())
                .referenceNumber(request.getRefernceNumber())
                .amount(request.getAmount())
                .build();
        paymentRepository.save(transaction);
        log.info("Transaction Completed with Id: {}", transaction.getId());
        return transaction.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Getting Payment Details for Order Id : {}", orderId);

        Transaction transaction = paymentRepository.findByOrderId(orderId);

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId(transaction.getId())
                .paymentDate(transaction.getPaymentDate())
                .amount(transaction.getAmount())
                .status(transaction.getPaymentStatus())
                .orderId(transaction.getOrderId())
                .paymentMode(PaymentMode.valueOf(transaction.getPaymentMode()))
                .build();

        return paymentResponse;
    }
}
