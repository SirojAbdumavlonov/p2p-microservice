package com.example.user.repository;

import com.example.user.entity.P2pTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface P2pTransactionRepository extends JpaRepository<P2pTransaction, Long> {
}
