package com.punam.order.external.client;

import com.punam.order.exception.CustomException;
import com.punam.order.external.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE/payments")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface PaymentService {
	
    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest request);

    default ResponseEntity<Long> fallback(Exception e){
        throw new CustomException("Payment Service is Unavaliable", "UNAVALIABLE", 500);
    }
}
