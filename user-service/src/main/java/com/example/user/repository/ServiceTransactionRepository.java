package com.example.user.repository;

import com.example.user.entity.ServiceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTransactionRepository extends JpaRepository<ServiceTransaction, Integer> {
}
