package com.punam.order.service;

import java.time.Instant;

import com.punam.order.exception.CustomException;
import com.punam.order.external.client.PaymentService;
import com.punam.order.external.client.ProductService;
import com.punam.order.external.request.PaymentRequest;
import com.punam.order.external.response.PaymentResponse;
import com.punam.order.external.response.ProductResponse;
import com.punam.order.model.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.punam.order.entity.Order;
import com.punam.order.model.OrderRequest;
import com.punam.order.repository.OrderRepository;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ProductService productService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public long placeOrder(OrderRequest orderRequest) {
		
		log.info("Placing order for : {}", orderRequest);

		productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
		
		Order order = Order.builder()
						.productId(orderRequest.getProductId())
						.quantity(orderRequest.getQuantity())
						.amount(orderRequest.getTotalAmount())
						.orderDate(Instant.now())
						.orderStatus("CREATED")
						.build();
		
		order = orderRepository.save(order);

		log.info("Calling Payment Service to complete the payment");
		PaymentRequest paymentRequest = PaymentRequest.builder()
				.orderId(order.getId())
				.paymentMode(orderRequest.getPaymentMode())
				.amount(orderRequest.getTotalAmount())
				.build();

		String orderStatus = null;

		try {
			paymentService.doPayment(paymentRequest);
			log.info("Payment Done Successfully");
			orderStatus = "PLACED";
		}catch (Exception e){
			log.info("Error occurred while completing payment");
			orderStatus = "PAYMENT_FAILED";
		}

		order.setOrderStatus(orderStatus);
		orderRepository.save(order);

		log.info("Order created succesfully with Order Id : {}", order.getId());

		return order.getId();
	}

	@Override
	public OrderResponse getOrderDetails(long orderId) {
		log.info("Get Order Details for Order Id : {}", orderId);
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException("Order not found for given Id", "NOT_FOUND", 404));

		log.info("Calling product service to get product details of product with Id: {}",order.getProductId());
		ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), ProductResponse.class);
		OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
				.productId(productResponse.getProductId())
				.productName(productResponse.getProductName())
				.price(productResponse.getPrice())
				.quantity(productResponse.getQuantity())
				.build();

		log.info("Calling Payment Service to get Payment Details for Order Id : {}", order.getId());

		PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payments/order/" + order.getId(), PaymentResponse.class);

		OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
				.paymentId(paymentResponse.getPaymentId())
				.paymentDate(paymentResponse.getPaymentDate())
				.status(paymentResponse.getStatus())
				.orderId(paymentResponse.getOrderId())
				.amount(paymentResponse.getAmount())
				.paymentMode(paymentResponse.getPaymentMode())
				.build();

		OrderResponse orderResponse = OrderResponse.builder()
				.orderId(order.getId())
				.orderStatus(order.getOrderStatus())
				.amount(order.getAmount())
				.orderDate(order.getOrderDate())
				.productDetails(productDetails)
				.paymentDetails(paymentDetails)
				.build();

		return orderResponse;
	}

}
