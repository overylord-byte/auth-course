package com.course.auth.repositories;

import com.course.auth.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findAllByName(String customerName, Pageable pageable);
}
