package com.punam.order.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.punam.order.OrderServiceConfig;
import com.punam.order.entity.Order;
import com.punam.order.model.OrderRequest;
import com.punam.order.model.OrderResponse;
import com.punam.order.model.PaymentMode;
import com.punam.order.repository.OrderRepository;
import com.punam.order.service.OrderService;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
public class OrderControllerTest {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private MockMvc mockMvc;
	
	@RegisterExtension
	private static WireMockExtension wireMockServer = WireMockExtension.newInstance()
			.options(WireMockConfiguration.wireMockConfig().port(8080)).build();
	
	private ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	
	@BeforeEach
	void setup() throws IOException {
		getProductDetailsResponse();
		doPayment();
		getPaymentDetails();
		reduceQuantity();
	}

	private void reduceQuantity() {
		// PUT /products/reduceQuantity
		
		wireMockServer.stubFor(put(urlMatching("/products/reduceQuantity/.*"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));		
		
	}

	private void getPaymentDetails() throws IOException {
		//GET /order/{orderId}
		
		wireMockServer.stubFor(get(urlMatching("/payments/.*"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(StreamUtils.
								copyToString(OrderControllerTest.class
										.getClassLoader()
										.getResourceAsStream("mock/GetPayment.json"), 
								Charset.defaultCharset()
				))));
		
	}

	private void doPayment() {
		// POSt /payments
		
		wireMockServer.stubFor(post(urlEqualTo("/payments"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
		
	}

	private void getProductDetailsResponse() throws IOException {
		//GET /products/1
		
		wireMockServer.stubFor(get("/products/1")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(StreamUtils.
								copyToString(OrderControllerTest.class
										.getClassLoader()
										.getResourceAsStream("mock/GetProduct.json"), 
								Charset.defaultCharset()
				))));
	}
	
	private OrderRequest getMockOrderRequest() {
		return OrderRequest.builder()
				.productId(1)
				.quantity(10)
				.paymentMode(PaymentMode.CASH)
				.totalAmount(200)
				.build();
	}
	
	
	@Test
	@DisplayName("Place Order - Success Scenario")
	public void test_placeOrder_doPayment_success() throws JsonProcessingException, Exception {
		
		OrderRequest orderRequest = getMockOrderRequest();
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/orders/placeOrder")
				.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_internal")))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(orderRequest)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		
		String orderId = mvcResult.getResponse().getContentAsString();
		
		Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
		assertTrue(order.isPresent());
		
		Order o = order.get();
		
		assertEquals(Long.parseLong(orderId), o.getId());
		assertEquals("PLACED", o.getOrderStatus());
		assertEquals(orderRequest.getTotalAmount(), o.getAmount());
		assertEquals(orderRequest.getQuantity(), o.getQuantity());
		
		
	}
	
	@Test
	@DisplayName("Place Order - Forbidden Scenario")
	public void test_placeOrder_request_forbidden() throws JsonProcessingException, Exception {
		
		OrderRequest orderRequest = getMockOrderRequest();
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/orders/placeOrder")
				.with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(orderRequest)))
				.andExpect(MockMvcResultMatchers.status().isForbidden())
				.andReturn();
	}
	
	
	@Test
	@DisplayName("Get Order Details - Success Scenario")
	public void test_WhenGetOrder_Success() throws JsonProcessingException, Exception {
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/orders/1")
				.with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		
		String actualResponse = mvcResult.getResponse().getContentAsString();
		Order order = orderRepository.findById(1L).get();
		
		String expectedResponse = getOrderResponse(order);
		
		assertEquals(expectedResponse, actualResponse);
		
	}
	
	@Test
	@DisplayName("Get Order Details - Not Found Scenario")
	public void test_WhenGetOrder_NotFound() throws JsonProcessingException, Exception {
		
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/orders/2")
				.with(jwt().authorities(new SimpleGrantedAuthority("Admin")))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andReturn();
	}

	private String getOrderResponse(Order order) throws JsonMappingException, JsonProcessingException, IOException {
		OrderResponse.PaymentDetails paymentDetails = objectMapper
				.readValue(StreamUtils
						.copyToString(OrderControllerTest.class
								.getClassLoader()
									.getResourceAsStream("mock/GetPayment.json"), Charset.defaultCharset()), 
							OrderResponse.PaymentDetails.class);
		paymentDetails.setStatus("SUCCESS");
		
		OrderResponse.ProductDetails productDetails = objectMapper
				.readValue(StreamUtils
						.copyToString(OrderControllerTest.class
								.getClassLoader()
									.getResourceAsStream("mock/GetProduct.json"), Charset.defaultCharset()), 
							OrderResponse.ProductDetails.class);
		
		OrderResponse orderResponse = OrderResponse.builder()
				.paymentDetails(paymentDetails)
				.productDetails(productDetails)
				.orderStatus(order.getOrderStatus())
				.orderDate(order.getOrderDate())
				.amount(order.getAmount())
				.orderId(order.getId())
				.build();
		
		return objectMapper.writeValueAsString(orderResponse);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
