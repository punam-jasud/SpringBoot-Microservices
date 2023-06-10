package com.punam.product.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.punam.product.model.ProductRequest;
import com.punam.product.model.ProductResponse;
import com.punam.product.service.ProductService;

@RestController
@RequestMapping("/products")
@Log4j2
public class ProductController {
	
	@Autowired
	ProductService productService;

	@PreAuthorize("hasAuthority('Admin') || hasAuthority('SCOPE_internal')")
	@PostMapping
	public ResponseEntity<Long> addProduct(@RequestBody ProductRequest request){
		long productId = productService.addProduct(request);
		return new ResponseEntity<>(productId, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer') || hasAuthority('SCOPE_internal')")
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable long id){
		ProductResponse productResponse = productService.getProductById(id);
		return new ResponseEntity<>(productResponse, HttpStatus.OK);
	}

	@PutMapping("/reduceQuantity/{id}")
	public ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity){
		log.info("Reducing Quantity");
		productService.reduceQuantity(productId,quantity);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
