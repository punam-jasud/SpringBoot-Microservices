package com.punam.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.punam.order.entity.Order;
import com.punam.order.exception.CustomException;
import com.punam.order.external.client.PaymentService;
import com.punam.order.external.client.ProductService;
import com.punam.order.external.request.PaymentRequest;
import com.punam.order.external.response.PaymentResponse;
import com.punam.order.external.response.ProductResponse;
import com.punam.order.model.OrderRequest;
import com.punam.order.model.OrderResponse;
import com.punam.order.model.PaymentMode;
import com.punam.order.repository.OrderRepository;

@SpringBootTest
public class OrderServiceImplTest {
	
	@Mock
	private OrderRepository orderRepository;
	
	@Mock
	private ProductService productService;

	@Mock
	private PaymentService paymentService;

	@Mock
	private RestTemplate restTemplate;
	
	@InjectMocks
	OrderService orderService = new OrderServiceImpl();
	
	
	@Test
	@DisplayName("Get Order Details - Success Scenario")
	void test_when_order_success() {
		//Mocking
		Order order = getMockOrder();
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		when(restTemplate.getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), ProductResponse.class))
			.thenReturn(getMockProductResponse());
		when(restTemplate.getForObject("http://PAYMENT-SERVICE/payments/order/" + order.getId(), PaymentResponse.class))
			.thenReturn(getMockPaymentResponse());
		
		//Actual
		OrderResponse orderResponse  = orderService.getOrderDetails(1);
		
		//verification of calls
		verify(orderRepository, times(1)).findById(anyLong());
		verify(restTemplate, times(1)).getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), ProductResponse.class);
		verify(restTemplate, times(1)).getForObject("http://PAYMENT-SERVICE/payments/order/" + order.getId(), PaymentResponse.class);
		
		//Assert
		assertNotNull(orderResponse);
		assertEquals(order.getId(), orderResponse.getOrderId());
		
	}
	
	@Test
	@DisplayName("Get Order Details = Failure Scenario")
	void test_when_order_not_found() {
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		CustomException customException = assertThrows(CustomException.class, () -> orderService.getOrderDetails(1));
		
		verify(orderRepository, times(1)).findById(anyLong());
		
		assertEquals("NOT_FOUND", customException.getErrorCode());
		assertEquals(404, customException.getStatus());
		
	}
	
	@Test
	@DisplayName("Place Order = Success")
	void test_when_place_order_success() {
		
		Order order = getMockOrder();
		OrderRequest orderRequest = getMockOrderRequest();
		
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(productService.reduceQuantity(anyLong(), anyLong())).thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
		when(paymentService.doPayment(any(PaymentRequest.class))).thenReturn(new ResponseEntity<Long>(1L, HttpStatus.OK));
		
		long orderId = orderService.placeOrder(orderRequest);
		
		verify(orderRepository, times(2)).save(any());
		verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
		verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));
		
		assertEquals(order.getId(), orderId);
		
	}
	
	@Test
	@DisplayName("Place Order = Payment Failed Scenario")
	void test_when_place_order_success_payment_failed() {
		
		Order order = getMockOrder();
		OrderRequest orderRequest = getMockOrderRequest();
		
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(productService.reduceQuantity(anyLong(), anyLong())).thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
		when(paymentService.doPayment(any(PaymentRequest.class))).thenThrow(new RuntimeException());
		
		long orderId = orderService.placeOrder(orderRequest);
		
		verify(orderRepository, times(2)).save(any());
		verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
		verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));
		
		assertEquals(order.getId(), orderId);
		
	}


	private OrderRequest getMockOrderRequest() {
		return OrderRequest.builder()
				.productId(1)
				.quantity(10)
				.paymentMode(PaymentMode.CASH)
				.totalAmount(100)
				.build();
	}

	private PaymentResponse getMockPaymentResponse() {
		return PaymentResponse.builder()
				.paymentId(1)
				.paymentDate(Instant.now())
				.paymentMode(PaymentMode.CASH)
				.amount(200)
				.orderId(1)
				.status("ACCEPTED")
				.build();
	}


	private ProductResponse getMockProductResponse() {
		return ProductResponse.builder()
				.productId(2)
				.productName("iPhone")
				.price(100)
				.quantity(200)
				.build();
	}


	private Order getMockOrder() {
		Order order = Order.builder()
				.orderStatus("PLACED")
				.orderDate(Instant.now())
				.id(1)
				.amount(100)
				.quantity(200)
				.productId(2)
				.build();
		return order;
	}

}
