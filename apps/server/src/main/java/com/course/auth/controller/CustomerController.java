package com.course.auth.controller;

import com.course.auth.domain.Customer;
import com.course.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    public static final String CUSTOMER_PATH = "/api/v1/customer";

    private final CustomerService customerService;

    @GetMapping(CUSTOMER_PATH)
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        Page<Customer> customers = customerService.getAllCustomers(name, pageNumber, pageSize);
        return ResponseEntity.ok(customers);
    }
}
