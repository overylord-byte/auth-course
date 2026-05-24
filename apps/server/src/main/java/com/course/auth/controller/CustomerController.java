package com.course.auth.controller;

import com.course.auth.domain.Customer;
import com.course.auth.service.CustomerService;
import com.course.auth.service.JwtAuthenticationResult;
import com.course.auth.service.JwtAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    public static final String CUSTOMER_PATH = "/api/v1/customer";

    private final CustomerService customerService;
    private final JwtAuthenticationService jwtAuthenticationService;

    @GetMapping(CUSTOMER_PATH)
    public ResponseEntity<?> getAll(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        JwtAuthenticationResult authResult =
                jwtAuthenticationService.validateAccessToken(authorizationHeader);

        if (!authResult.authenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Page<Customer> customers = customerService.getAllCustomers(name, pageNumber, pageSize);
        return ResponseEntity.ok(customers);
    }
}
