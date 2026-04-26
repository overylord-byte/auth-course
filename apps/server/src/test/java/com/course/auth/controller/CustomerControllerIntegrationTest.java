package com.course.auth.controller;

import com.course.auth.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerControllerIntegrationTest {

    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String AUTH_CHALLENGE = "Basic realm=\"course-api\"";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(WWW_AUTHENTICATE))
                .andExpect(header().string(WWW_AUTHENTICATE, AUTH_CHALLENGE));
    }

    @Test
    void shouldReturn401WhenAuthSchemeIsInvalid() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT).header(AUTHORIZATION, "Bearer token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(WWW_AUTHENTICATE))
                .andExpect(header().string(WWW_AUTHENTICATE, AUTH_CHALLENGE));
    }

    @Test
    void shouldReturn401WhenBase64IsInvalid() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT).header(AUTHORIZATION, "Basic !!!"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(WWW_AUTHENTICATE))
                .andExpect(header().string(WWW_AUTHENTICATE, AUTH_CHALLENGE));
    }

    @Test
    void shouldReturn401WhenCredentialsAreWrong() throws Exception {
        String wrongCredentials = basicHeader("user", "wrong");

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header(AUTHORIZATION, wrongCredentials))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(WWW_AUTHENTICATE))
                .andExpect(header().string(WWW_AUTHENTICATE, AUTH_CHALLENGE));
    }

    @Test
    void shouldReturn200WhenCredentialsAreValid() throws Exception {
        when(customerService.getAllCustomers(
                nullable(String.class),
                nullable(Integer.class),
                nullable(Integer.class)))
                .thenReturn(Page.empty());

        String validCredentials = basicHeader("user", "password");

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header(AUTHORIZATION, validCredentials))
                .andExpect(status().isOk());
    }

    private String basicHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
