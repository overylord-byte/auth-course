package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import com.course.auth.service.CustomerService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerControllerIntegrationTest {

    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String LOGOUT_ENDPOINT = "/api/v1/auth/logout";
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturn200AndSetCookieWhenLoginCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists(SET_COOKIE))
                .andExpect(header().string(SET_COOKIE, containsString(SESSION_COOKIE_NAME + "=")))
                .andExpect(header().string(SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(SET_COOKIE, containsString("Max-Age=900")))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void shouldReturn401AndNotSetCookieWhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(SET_COOKIE));
    }

    @Test
    void shouldReturn401WhenSessionCookieIsMissing() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WhenSessionCookieIsValid() throws Exception {
        when(customerService.getAllCustomers(
                nullable(String.class),
                nullable(Integer.class),
                nullable(Integer.class)))
                .thenReturn(Page.empty());

        Cookie sessionCookie = loginAndGetSessionCookie();

        mockMvc.perform(get(CUSTOMER_ENDPOINT).cookie(sessionCookie))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200AndClearCookieWhenLogoutIsCalled() throws Exception {
        Cookie sessionCookie = loginAndGetSessionCookie();

        mockMvc.perform(post(LOGOUT_ENDPOINT).cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(SESSION_COOKIE_NAME, 0))
                .andExpect(header().string(SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(jsonPath("$.loggedOut").value(true));
    }

    @Test
    void shouldReturn401WhenUsingSessionCookieAfterLogout() throws Exception {
        Cookie sessionCookie = loginAndGetSessionCookie();

        mockMvc.perform(post(LOGOUT_ENDPOINT).cookie(sessionCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get(CUSTOMER_ENDPOINT).cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenOnlyBasicAuthHeaderIsSent() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(status().isUnauthorized());
    }

    private Cookie loginAndGetSessionCookie() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie(SESSION_COOKIE_NAME);

        if (sessionCookie == null) {
            throw new IllegalStateException("Login response did not include SESSION_ID cookie");
        }

        return sessionCookie;
    }
}
