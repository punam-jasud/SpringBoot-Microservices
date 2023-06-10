package com.punam.order.service;

import com.punam.order.model.OrderRequest;
import com.punam.order.model.OrderResponse;

public interface OrderService {

	long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
