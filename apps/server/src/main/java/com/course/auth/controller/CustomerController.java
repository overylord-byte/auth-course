package com.course.auth.controller;

import com.course.auth.domain.Customer;
import com.course.auth.service.CustomerService;
import com.course.auth.service.SessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.course.auth.controller.AuthController.SESSION_COOKIE_NAME;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    public static final String CUSTOMER_PATH = "/api/v1/customer";

    private final CustomerService customerService;
    private final SessionStore sessionStore;

    @GetMapping(CUSTOMER_PATH)
    public ResponseEntity<?> getAll(
            @CookieValue(value = SESSION_COOKIE_NAME, required = false) String sessionId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        if (sessionStore.getSession(sessionId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Page<Customer> customers = customerService.getAllCustomers(name, pageNumber, pageSize);
        return ResponseEntity.ok(customers);
    }
}
