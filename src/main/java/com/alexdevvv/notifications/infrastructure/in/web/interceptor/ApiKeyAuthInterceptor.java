package com.alexdevvv.notifications.infrastructure.in.web.interceptor;

import com.alexdevvv.notifications.application.ports.out.ApiKeyRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private static final String API_KEY_HEADER = "X-API-Key";

    public ApiKeyAuthInterceptor(ApiKeyRepositoryPort apiKeyRepositoryPort) {
        this.apiKeyRepositoryPort = apiKeyRepositoryPort;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            throw new InvalidApiKeyException("Missing API Key");
        }

        String clientId = apiKeyRepositoryPort.findClientIdByKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException("Invalid API Key"));

        request.setAttribute("clientId", clientId);
        return true;
    }
}
