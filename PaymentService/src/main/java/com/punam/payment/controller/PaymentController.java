package com.punam.payment.controller;

import com.punam.payment.model.PaymentRequest;
import com.punam.payment.model.PaymentResponse;
import com.punam.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
	
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest request){
        return new ResponseEntity<>(paymentService.doPayment(request), HttpStatus.OK);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable long orderId){
        return new ResponseEntity<>(paymentService.getPaymentDetailsByOrderId(orderId), HttpStatus.OK);
    }
}
