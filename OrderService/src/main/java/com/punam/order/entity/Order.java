package com.punam.order.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ORDER_DETAILS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "PRODUCT_ID")
	private long productId;
	
	@Column(name = "QUANTITY")
	private long quantity;
	
	@Column(name = "ORDER_DATE")
	private Instant orderDate;
	
	@Column(name = "STATUS")
	private String orderStatus;
	
	@Column(name = "TOTAL_AMOUNT")
	private long amount;

}
