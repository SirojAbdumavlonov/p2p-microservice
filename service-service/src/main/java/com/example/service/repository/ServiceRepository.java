package com.example.service.repository;

import com.example.service.entity.Service;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
@Registered
public interface ServiceRepository extends JpaRepository<Service, Integer> {
}
