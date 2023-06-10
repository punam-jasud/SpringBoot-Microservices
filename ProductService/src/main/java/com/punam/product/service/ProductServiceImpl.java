package com.punam.product.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.punam.product.entity.Product;
import com.punam.product.exception.ProductServiceCustomException;
import com.punam.product.model.ProductRequest;
import com.punam.product.model.ProductResponse;
import com.punam.product.repository.ProductRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	ProductRepository productRepository;

	@Override
	public Long addProduct(ProductRequest request) {
		log.info("Adding Product..");
		Product product  = Product.builder()
							.productName(request.getName())
							.price(request.getPrice())
							.quantity(request.getQuantity())
							.build();
		productRepository.save(product);
		log.info("Product Added..");
		return product.getProductId();
	}

	@Override
	public ProductResponse getProductById(long id) {
		log.info("Getting Product for Id {} ", id);
		
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductServiceCustomException("Product with Given Id is Not Found", "PRODUCT_NOT_FOUND"));
		ProductResponse productResponse = new ProductResponse();
		BeanUtils.copyProperties(product, productResponse);
		
		return productResponse;
	}

	@Override
	public void reduceQuantity(long productId, long quantity) {
		log.info("Reducing Quantity {} for Product Id : {}",quantity, productId);
		Product product = productRepository.findById(productId)
											.orElseThrow(() -> new ProductServiceCustomException("Product with given Id is not Found","PRODUCT_NOT_FOUND"));
		if(product.getQuantity() < quantity){
			throw new ProductServiceCustomException("Product quantity is insufficient","INSUFFICIENT_QUANTITY");
		}
		product.setQuantity(product.getQuantity() - quantity);
		productRepository.save(product);
		log.info("Product quantity updated successfully!");
	}
}
