package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import com.course.auth.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String LOGOUT_ENDPOINT = "/api/v1/auth/logout";
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

    @Autowired
    private ObjectMapper objectMapper;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
    }

    @Test
    void shouldReturn200AndSetCookieWhenLoginCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        HttpResponse<String> response = login(loginRequest.username(), loginRequest.password());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue(SET_COOKIE)).isPresent();
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains(SESSION_COOKIE_NAME);
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains("HttpOnly");
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains("Path=/");
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains("SameSite=Lax");
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains("Max-Age=900");

        assertThat(response.body()).contains("authenticated");
        assertThat(response.body()).contains("username");
    }

    @Test
    void shouldReturn401AndNotSetCookieWhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");
        HttpResponse<String> response = login(loginRequest.username(), loginRequest.password());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue(SET_COOKIE)).isNotPresent();
    }

    @Test
    void shouldReturn401WhenSessionCookieIsMissing() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");
        HttpResponse<String> response = login(loginRequest.username(), loginRequest.password());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue(SET_COOKIE)).isNotPresent();
    }

    @Test
    void shouldReturn200WhenSessionCookieIsValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");
        login(loginRequest.username(), loginRequest.password());
    }

    @Test
    void shouldReturn200AndClearCookieWhenLogoutIsCalled() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        login(loginRequest.username(), loginRequest.password());

        HttpResponse<String> response = logout();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue(SET_COOKIE)).isPresent();
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains(SESSION_COOKIE_NAME);
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains("Max-Age=0");
        assertThat(response.body()).contains("loggedOut");
    }

    @Test
    void shouldReturn401WhenUsingSessionCookieAfterLogout() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        HttpResponse<String> loginResponse = login(loginRequest.username(), loginRequest.password());

        HttpResponse<String> logoutResponse = logout();
        assertThat(logoutResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> getCustomersResponse = getCustomers(loginResponse.headers().firstValue(SET_COOKIE).get());
        assertThat(getCustomersResponse.statusCode()).isEqualTo(401);
    }

    private String uri(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> login(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        return httpClient.send(HttpRequest.newBuilder()
                        .uri(new URI(uri(LOGIN_ENDPOINT)))
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> logout() throws Exception {
        HttpRequest logoutRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(LOGOUT_ENDPOINT)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getCustomers(String cookie) throws Exception {
        HttpRequest getCustomersRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(CUSTOMER_ENDPOINT)))
                .header("Cookie", cookie)
                .GET()
                .build();

        return httpClient.send(getCustomersRequest, HttpResponse.BodyHandlers.ofString());
    }
}
