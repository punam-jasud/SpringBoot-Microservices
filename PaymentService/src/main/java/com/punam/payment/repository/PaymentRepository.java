package com.punam.payment.repository;

import com.punam.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Transaction, Long> {

    Transaction findByOrderId(long orderId);
}
