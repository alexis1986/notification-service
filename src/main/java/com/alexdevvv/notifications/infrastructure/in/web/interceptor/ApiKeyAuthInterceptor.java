package com.alexdevvv.notifications.infrastructure.in.web.interceptor;

import com.alexdevvv.notifications.application.ports.out.ApiKeyRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private final ApiKeyRepositoryPort apiKeyRepositoryPort;
    private static final String API_KEY_HEADER = "X-API-Key";

    public ApiKeyAuthInterceptor(ApiKeyRepositoryPort apiKeyRepositoryPort) {
        this.apiKeyRepositoryPort = apiKeyRepositoryPort;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing API Key");
            return false;
        }

        Optional<String> clientId = apiKeyRepositoryPort.findClientIdByKey(apiKey);

        if (clientId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return false;
        }

        request.setAttribute("clientId", clientId.get());
        return true;
    }
}
