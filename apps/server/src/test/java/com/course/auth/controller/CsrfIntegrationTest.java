package com.course.auth.controller;

import com.course.auth.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CsrfIntegrationTest {
    private final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private final String LOGOUT_ENDPOINT = "/api/v1/auth/logout";
    private final String CSRF_ENDPOINT = "/api/v1/auth/csrf";
    private final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";
    private final String CUSTOMER_ENDPOINT = "/api/v1/customer";

    @LocalServerPort
    private int port;

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
    void shouldReturnCsrfTokenForAuthenticatedSession() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> csrfToken = getCsrfToken();
        assertThat(csrfToken.statusCode()).isEqualTo(200);

        JsonNode csrfRootTree = objectMapper.readTree(csrfToken.body());

        JsonNode tokenNode = csrfRootTree.get("token");
        assertThat(tokenNode).isNotNull();
        assertThat(tokenNode.isString()).isTrue();

        String token = tokenNode.asText();
        assertThat(token).isNotBlank();

        JsonNode headerNameNode = csrfRootTree.get("headerName");
        assertThat(headerNameNode).isNotNull();
        assertThat(headerNameNode.isString()).isTrue();

        String headerName = headerNameNode.asText();
        assertThat(headerName).isEqualTo(CSRF_HEADER_NAME);

        JsonNode parameterNameNode = csrfRootTree.get("parameterName");
        assertThat(parameterNameNode).isNotNull();
        assertThat(parameterNameNode.isString()).isTrue();

        String parameterName = parameterNameNode.asText();
        assertThat(parameterName).isEqualTo("_csrf");
    }

    @Test
    void shouldReturn403WhenLogoutDoesNotContainCsrfToken() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> logoutResponse = logoutWithoutCsrf();
        assertThat(logoutResponse.statusCode()).isEqualTo(403);

        HttpResponse<String> customersResponse = getCustomers();
        assertThat(customersResponse.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldReturn403WhenLogoutContainsInvalidCsrfToken() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> logoutResponse = logoutWithCsrfToken("Random string", CSRF_HEADER_NAME);
        assertThat(logoutResponse.statusCode()).isEqualTo(403);

        HttpResponse<String> customersResponse = getCustomers();
        assertThat(customersResponse.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldLogoutWhenCsrfTokenIsValid() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> csrfToken = getCsrfToken();
        assertThat(csrfToken.statusCode()).isEqualTo(200);
        JsonNode csrfRootTree = objectMapper.readTree(csrfToken.body());
        JsonNode tokenNode = csrfRootTree.get("token");
        String token = tokenNode.asText();

        HttpResponse<String> logoutResponse = logoutWithCsrfToken(token, CSRF_HEADER_NAME);
        assertThat(logoutResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> customersResponse = getCustomers();
        assertThat(customersResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldReturn403WhenCsrfTokenBelongsToAnotherSession() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        HttpClient newHttpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
        HttpResponse<String> anotherLoginResponse = login("user", "password", newHttpClient);
        assertThat(anotherLoginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> csrfTokenSecond = getCsrfToken(newHttpClient);
        assertThat(csrfTokenSecond.statusCode()).isEqualTo(200);
        JsonNode csrfRootTreeSecond = objectMapper.readTree(csrfTokenSecond.body());
        JsonNode tokenNodeSecond = csrfRootTreeSecond.get("token");
        String tokenSecond = tokenNodeSecond.asText();

        HttpResponse<String> logoutResponse = logoutWithCsrfToken(tokenSecond, CSRF_HEADER_NAME);
        assertThat(logoutResponse.statusCode()).isEqualTo(403);
        HttpResponse<String> customersResponse = getCustomers();
        assertThat(customersResponse.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldAllowGetCustomersWithoutCsrfToken() throws Exception {
        HttpResponse<String> loginResponse = login("user", "password");
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> customersResponse = getCustomers();
        assertThat(customersResponse.statusCode()).isEqualTo(200);
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

    private HttpResponse<String> login(String username, String password, HttpClient httpClient) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        return httpClient.send(HttpRequest.newBuilder()
                        .uri(new URI(uri(LOGIN_ENDPOINT)))
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                        .header("Content-Type", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> logoutWithoutCsrf() throws Exception {
        HttpRequest logoutRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(LOGOUT_ENDPOINT)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> logoutWithCsrfToken(String csrfToken, String headerName) throws Exception {
        HttpRequest logoutRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(LOGOUT_ENDPOINT)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header(headerName, csrfToken)
                .build();

        return httpClient.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> logoutWithCsrfToken(String csrfToken, String headerName, HttpClient httpClient) throws Exception {
        HttpRequest logoutRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(LOGOUT_ENDPOINT)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header(headerName, csrfToken)
                .build();

        return httpClient.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getCsrfToken() throws Exception {
        HttpRequest getCsrfRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(CSRF_ENDPOINT)))
                .GET()
                .build();

        return httpClient.send(getCsrfRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getCsrfToken(HttpClient httpClient) throws Exception {
        HttpRequest getCsrfRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(CSRF_ENDPOINT)))
                .GET()
                .build();

        return httpClient.send(getCsrfRequest, HttpResponse.BodyHandlers.ofString());
    }

    private String uri(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }

    private HttpResponse<String> getCustomers() throws Exception {
        HttpRequest getCustomersRequest = HttpRequest.newBuilder()
                .uri(new URI(uri(CUSTOMER_ENDPOINT)))
                .GET()
                .build();

        return httpClient.send(getCustomersRequest, HttpResponse.BodyHandlers.ofString());
    }
}
