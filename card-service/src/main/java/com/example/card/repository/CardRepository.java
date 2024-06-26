package com.example.card.repository;

import com.example.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    boolean existsByNumber(String number);

    List<Card> findByUserId(Integer userId);
}
