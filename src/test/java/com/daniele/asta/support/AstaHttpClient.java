package com.daniele.asta.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AstaHttpClient implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CookieManager cookieManager = new CookieManager();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();
    private final String baseUrl;

    public AstaHttpClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public Map<String, Object> init() throws Exception {
        return getJson("/fantaasta/init");
    }

    public void fetchPage(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " for " + path);
        }
    }

    public String getJsessionId() {
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> "JSESSIONID".equalsIgnoreCase(c.getName()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("JSESSIONID mancante"));
    }

    public Map<String, Object> postJson(String path, Map<String, Object> body) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " for " + path + ": " + response.body());
        }
        if (response.body() == null || response.body().isBlank()) {
            return Map.of();
        }
        return MAPPER.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    private Map<String, Object> getJson(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " for " + path + ": " + response.body());
        }
        return MAPPER.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public void close() {
        cookieManager.getCookieStore().removeAll();
    }
}
