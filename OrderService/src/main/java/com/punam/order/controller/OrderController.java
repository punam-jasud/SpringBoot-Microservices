package com.punam.order.controller;

import com.punam.order.model.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.punam.order.model.OrderRequest;
import com.punam.order.service.OrderService;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/orders")
@Log4j2
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@PreAuthorize("hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
	@PostMapping("/placeOrder")
	public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
		long orderId = orderService.placeOrder(orderRequest);
		log.info("Order Created with Id: {}",orderId);
		return new ResponseEntity<Long>(orderId, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId){
		OrderResponse orderResponse = orderService.getOrderDetails(orderId);
		return new ResponseEntity<>(orderResponse, HttpStatus.OK);
	}

}
