package com.course.auth.controller;

import com.course.auth.domain.JwtTokenPayload;
import com.course.auth.dto.LoginRequest;
import com.course.auth.service.CustomerService;
import com.course.auth.service.JwtAuthenticationService;
import com.course.auth.utils.JwtUtil;
import tools.jackson.databind.ObjectMapper;
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

import java.time.Instant;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerControllerIntegrationTest {

    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

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
    void shouldReturn200AndAccessTokenWhenLoginCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldReturn401WhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenAuthorizationSchemeIsInvalid() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Basic test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenFormatIsInvalid() throws Exception {
        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenPayloadIsModified() throws Exception {
        String validToken = loginAndGetAccessToken();
        String[] parts = validToken.split("\\.");

        String attackerPayloadJson = "{\"sub\":\"hacker\",\"iat\":1,\"exp\":9999999999}";
        String modifiedPayload = JwtUtil.base64UrlEncode(attackerPayloadJson);
        String modifiedToken = parts[0] + "." + modifiedPayload + "." + parts[2];

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer " + modifiedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenSignatureIsWrong() throws Exception {
        String validToken = loginAndGetAccessToken();
        String[] parts = validToken.split("\\.");
        String tokenWithWrongSignature = parts[0] + "." + parts[1] + ".wrong-signature";

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer " + tokenWithWrongSignature))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenTokenIsExpired() throws Exception {
        String expiredToken = createExpiredToken("user");

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WhenBearerTokenIsValid() throws Exception {
        when(customerService.getAllCustomers(
                nullable(String.class),
                nullable(Integer.class),
                nullable(Integer.class)))
                .thenReturn(Page.empty());

        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private String loginAndGetAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        MvcResult loginResult = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    private String createExpiredToken(String username) throws Exception {
        long expiredAt = Instant.now().getEpochSecond() - 60;
        long issuedAt = expiredAt - JwtAuthenticationService.TOKEN_EXPIRATION_SECONDS;

        JwtTokenPayload payload = new JwtTokenPayload(username, issuedAt, expiredAt);
        String payloadJson = objectMapper.writeValueAsString(payload);

        String encodedHeader = JwtUtil.base64UrlEncode(HEADER_JSON);
        String encodedPayload = JwtUtil.base64UrlEncode(payloadJson);
        String signingInput = encodedHeader + "." + encodedPayload;
        String signature = JwtUtil.sign(signingInput, JwtAuthenticationService.JWT_SECRET);

        return signingInput + "." + signature;
    }
}
