package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.COOKIE;
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
    void shouldLoggedInAndReturnListOfCustomers() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        login(loginRequest.username(), loginRequest.password());
        HttpResponse<String> getCustomersResponse = getCustomers();
        assertThat(getCustomersResponse.statusCode()).isEqualTo(200);
        assertThat(getCustomersResponse.body()).contains("content");
    }

    @Test
    void shouldReturn401AndNotSetCookieWhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrong");
        HttpResponse<String> response = login(loginRequest.username(), loginRequest.password());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue(SET_COOKIE)).isNotPresent();
    }

    @Test
    void shouldReturn401WhenSessionIsMissing() throws Exception {
        HttpResponse<String> getCustomersResponse = getCustomers();
        assertThat(getCustomersResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldReturn200AndClearCookieWhenLogoutIsCalled() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        login(loginRequest.username(), loginRequest.password());

        HttpResponse<String> response = logout();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue(SET_COOKIE)).isPresent();
        assertThat(response.headers().firstValue(SET_COOKIE).get()).contains(SESSION_COOKIE_NAME + "=;");
        assertThat(response.body()).contains("loggedOut");
    }

    @Test
    void shouldReturn401WhenUsingSessionCookieAfterLogout() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        HttpResponse<String> loginResponse = login(loginRequest.username(), loginRequest.password());
        String cookieSessionId = loginResponse.headers().firstValue(SET_COOKIE).get().split(";")[0];
        HttpResponse<String> getCustomersResponse = getCustomers();
        assertThat(getCustomersResponse.statusCode()).isEqualTo(200);

        logout();

        HttpClient newHttpClientWithoutCookieManager = HttpClient.newBuilder().build();
        HttpResponse<String> getCustomersResponse2 = newHttpClientWithoutCookieManager.send(HttpRequest.newBuilder()
                .uri(new URI(uri(CUSTOMER_ENDPOINT)))
                .header(COOKIE, cookieSessionId)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(getCustomersResponse2.statusCode()).isEqualTo(401);
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

    private HttpResponse<String> getCustomers() throws Exception {
        HttpRequest getCustomersRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(CUSTOMER_ENDPOINT)))
                .GET()
                .build();

        return httpClient.send(getCustomersRequest, HttpResponse.BodyHandlers.ofString());
    }
}
