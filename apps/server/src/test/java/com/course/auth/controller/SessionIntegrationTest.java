package com.course.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SessionIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldReturnCurrentUserWithoutSettingCookieOrCachingResponse() throws Exception {
        String sessionCookie = login();

        HttpResponse<String> response = getSession(sessionCookie);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(response.body())).isEqualTo(
                objectMapper.readTree("{\"authenticated\":true,\"username\":\"user\"}"));
        assertNoCookieAndNoCache(response);
    }

    @Test
    void shouldReturn401WithoutSettingCookieWhenSessionCookieIsMissing() throws Exception {
        HttpResponse<String> response = getSession(null);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).isEmpty();
        assertNoCookieAndNoCache(response);
    }

    @Test
    void shouldReturn401WithoutSettingCookieWhenSessionIsUnknown() throws Exception {
        HttpResponse<String> response = getSession("SESSION_ID=unknown-session");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).isEmpty();
        assertNoCookieAndNoCache(response);
    }

    @Test
    void shouldRejectOldSessionCookieAfterLogout() throws Exception {
        String sessionCookie = login();
        HttpResponse<String> csrfResponse = httpClient.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/csrf"))
                        .header("Cookie", sessionCookie)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(csrfResponse.statusCode()).isEqualTo(200);
        String csrfToken = objectMapper.readTree(csrfResponse.body()).get("token").asText();

        HttpResponse<String> logoutResponse = httpClient.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/logout"))
                        .header("Cookie", sessionCookie)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(logoutResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> response = getSession(sessionCookie);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).isEmpty();
        assertNoCookieAndNoCache(response);
    }

    private String login() throws Exception {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(uri("/api/v1/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"username\":\"user\",\"password\":\"password\"}"))
                        .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        String setCookie = response.headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(setCookie).startsWith("SESSION_ID=");
        return setCookie.split(";", 2)[0];
    }

    private HttpResponse<String> getSession(String sessionCookie) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri("/api/v1/auth/session")).GET();
        if (sessionCookie != null) {
            request.header("Cookie", sessionCookie);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertNoCookieAndNoCache(HttpResponse<String> response) {
        assertThat(response.headers().allValues("Set-Cookie")).isEmpty();
        assertThat(response.headers().allValues("Cache-Control")).containsExactly("no-store");
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
