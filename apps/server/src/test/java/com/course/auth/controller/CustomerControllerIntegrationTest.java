package com.course.auth.controller;

import com.course.auth.domain.JwtTokenPayload;
import com.course.auth.dto.LoginRequest;
import com.course.auth.service.CustomerService;
import com.course.auth.service.JwtTokenService;
import com.course.auth.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
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

import static com.course.auth.controller.AuthController.REFRESH_COOKIE_NAME;
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
    private static final String ACCESS_TOKEN_ENDPOINT = "/api/v1/auth/accessToken";
    private static final String REFRESH_ENDPOINT = "/api/v1/auth/refresh";
    private static final String LOGOUT_ENDPOINT = "/api/v1/auth/logout";
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
    void shouldReturn200AccessTokenAndRefreshCookieWhenLoginCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        mockMvc.perform(post(ACCESS_TOKEN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().exists(SET_COOKIE))
                .andExpect(header().string(SET_COOKIE, containsString(REFRESH_COOKIE_NAME + "=")))
                .andExpect(header().string(SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(SET_COOKIE, containsString("Max-Age=604800")));
    }

    @Test
    void shouldReturn401WhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");

        mockMvc.perform(post(ACCESS_TOKEN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(SET_COOKIE));
    }

    @Test
    void shouldReturn200AndNewAccessTokenWhenRefreshCookieIsValid() throws Exception {
        Cookie refreshCookie = loginAndGetRefreshCookie();

        mockMvc.perform(post(REFRESH_ENDPOINT).cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldReturn401WhenRefreshCookieIsMissing() throws Exception {
        mockMvc.perform(post(REFRESH_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenRefreshCookieIsInvalid() throws Exception {
        mockMvc.perform(post(REFRESH_ENDPOINT).cookie(new Cookie(REFRESH_COOKIE_NAME, "invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenRefreshCookieIsExpired() throws Exception {
        String expiredRefreshToken = createExpiredToken("user", JwtTokenService.REFRESH_TOKEN_TYPE);

        mockMvc.perform(post(REFRESH_ENDPOINT).cookie(new Cookie(REFRESH_COOKIE_NAME, expiredRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200AndClearRefreshCookieWhenLogoutIsCalled() throws Exception {
        mockMvc.perform(post(LOGOUT_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(REFRESH_COOKIE_NAME, 0))
                .andExpect(header().string(SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(jsonPath("$.loggedOut").value(true));
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

        String attackerPayloadJson =
                "{\"sub\":\"hacker\",\"type\":\"access\",\"iat\":1,\"exp\":9999999999}";
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
    void shouldReturn401WhenAccessTokenIsExpired() throws Exception {
        String expiredToken = createExpiredToken("user", JwtTokenService.ACCESS_TOKEN_TYPE);

        mockMvc.perform(get(CUSTOMER_ENDPOINT).header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenRefreshTokenIsUsedAsAccessToken() throws Exception {
        Cookie refreshCookie = loginAndGetRefreshCookie();

        mockMvc.perform(get(CUSTOMER_ENDPOINT)
                        .header("Authorization", "Bearer " + refreshCookie.getValue()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenAccessTokenIsUsedAsRefreshToken() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(post(REFRESH_ENDPOINT).cookie(new Cookie(REFRESH_COOKIE_NAME, accessToken)))
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

        MvcResult loginResult = mockMvc.perform(post(ACCESS_TOKEN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    private Cookie loginAndGetRefreshCookie() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        MvcResult loginResult = mockMvc.perform(post(ACCESS_TOKEN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie(REFRESH_COOKIE_NAME);

        if (refreshCookie == null) {
            throw new IllegalStateException("Login response did not include REFRESH_TOKEN cookie");
        }

        return refreshCookie;
    }

    private String createExpiredToken(String username, String tokenType) throws Exception {
        long expiredAt = Instant.now().getEpochSecond() - 60;
        long issuedAt = expiredAt - JwtTokenService.ACCESS_TOKEN_EXPIRATION_SECONDS;

        JwtTokenPayload payload = new JwtTokenPayload(username, tokenType, issuedAt, expiredAt);
        String payloadJson = objectMapper.writeValueAsString(payload);

        String encodedHeader = JwtUtil.base64UrlEncode(HEADER_JSON);
        String encodedPayload = JwtUtil.base64UrlEncode(payloadJson);
        String signingInput = encodedHeader + "." + encodedPayload;
        String signature = JwtUtil.sign(signingInput, JwtTokenService.JWT_SECRET);

        return signingInput + "." + signature;
    }
}
