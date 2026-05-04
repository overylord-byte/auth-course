package com.course.auth.controller;

import com.course.auth.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class CustomerControllerIntegrationTest {

    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String AUTH_CHALLENGE = "Basic realm=\"course-api\", charset=\"UTF-8\"";

    private final MockMvcTester mockMvc;

    @MockitoBean
    private CustomerService customerService;

    CustomerControllerIntegrationTest(@Autowired WebApplicationContext context) {
        this.mockMvc = MockMvcTester.create(
                MockMvcBuilders
                        .webAppContextSetup(context)
                        .apply(springSecurity())
                        .build()
        );
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() {
        mockMvc.get().uri(CUSTOMER_ENDPOINT)
                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(WWW_AUTHENTICATE, AUTH_CHALLENGE);
    }

    @Test
    void shouldReturn401WhenAuthSchemeIsInvalid() {
        mockMvc.get().uri(CUSTOMER_ENDPOINT)
                .header(AUTHORIZATION, "Bearer token")
                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(WWW_AUTHENTICATE, AUTH_CHALLENGE);
    }

    @Test
    void shouldReturn401WhenBase64IsInvalid() {
        mockMvc.get().uri(CUSTOMER_ENDPOINT)
                .header(AUTHORIZATION, "Basic !!!")
                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(WWW_AUTHENTICATE, AUTH_CHALLENGE);
    }

    @Test
    void shouldReturn401WhenCredentialsAreWrong() {
        String wrongCredentials = basicHeader("user", "wrong");

        mockMvc.get().uri(CUSTOMER_ENDPOINT)
                .header(AUTHORIZATION, wrongCredentials)
                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(WWW_AUTHENTICATE, AUTH_CHALLENGE);
    }

    @Test
    void shouldReturn200WhenCredentialsAreValid() {
        when(customerService.getAllCustomers(
                nullable(String.class),
                nullable(Integer.class),
                nullable(Integer.class)))
                .thenReturn(Page.empty());

        String validCredentials = basicHeader("user", "password");

        mockMvc.get().uri(CUSTOMER_ENDPOINT)
                .header(AUTHORIZATION, validCredentials)
                .assertThat()
                .hasStatusOk();
    }

    private String basicHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
