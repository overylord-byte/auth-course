package com.course.auth.controller;

import com.course.auth.domain.Customer;
import com.course.auth.service.BasicAuthenticationResult;
import com.course.auth.service.BasicAuthenticationService;
import com.course.auth.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
    private final BasicAuthenticationService basicAuthenticationService;

    @GetMapping(CUSTOMER_PATH)
    public ResponseEntity<?> getAll(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        BasicAuthenticationResult authenticationResult = basicAuthenticationService.authenticate(authorizationHeader);

        if (!authenticationResult.authenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"course-api\"")
                    .body("Unauthorized");

        }

        Page<Customer> customers = customerService.getAllCustomers(name, pageNumber, pageSize);
        return ResponseEntity.ok(customers);
    }
}
