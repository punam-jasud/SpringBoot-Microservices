package com.punam.product.service;

import com.punam.product.model.ProductRequest;
import com.punam.product.model.ProductResponse;

public interface ProductService {

	Long addProduct(ProductRequest request);

	ProductResponse getProductById(long id);

	void reduceQuantity(long productId, long quantity);
}
