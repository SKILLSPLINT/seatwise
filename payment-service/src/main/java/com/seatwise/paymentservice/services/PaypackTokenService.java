package com.seatwise.paymentservice.services;

import com.seatwise.paymentservice.config.PaypackProperties;
import com.seatwise.paymentservice.dtos.request.PaypackAuthRequest;
import com.seatwise.paymentservice.dtos.response.PaypackAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaypackTokenService {

    private final WebClient paypackWebClient;
    private final PaypackProperties paypackProperties;

    // Thread-safe token storage
    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Get a valid access token.
     * Auto-refreshes if expired. Thread-safe.
     */
    public String getValidAccessToken() {
        // Fast path: token still valid (with 1-min buffer)
        if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return accessToken;
        }

        lock.lock();
        try {
            // Double-check after acquiring a lock
            if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
                return accessToken;
            }

            // Try refresh first (if we have a refresh token)
            if (refreshToken != null) {
                try {
                    return refreshAccessToken();
                } catch (Exception e) {
                    log.warn("Token refresh failed, re-authenticating. Error: {}", e.getMessage());
                }
            }

            // Full re-authentication
            return authenticate();

        } finally {
            lock.unlock();
        }
    }

    /**
     * Force clear token (call this on 401 response)
     */
    public void invalidateToken() {
        lock.lock();
        try {
            accessToken = null;
            refreshToken = null;
            tokenExpiresAt = Instant.EPOCH;
        } finally {
            lock.unlock();
        }
    }

    private String authenticate() {
        log.info("Authenticating with Paypack API...");

        PaypackAuthResponse response = paypackWebClient.post()
                .uri("/auth/agents/authorize")
                .bodyValue(new PaypackAuthRequest(
                        paypackProperties.getClientId(),
                        paypackProperties.getClientSecret()
                ))
                .retrieve()
                .bodyToMono(PaypackAuthResponse.class)
                .block();

        if (response == null || response.getAccess() == null) {
            throw new RuntimeException("Paypack authentication failed — null response");
        }

        storeTokens(response);
        log.info("Paypack authentication successful. Token valid for ~15 minutes.");
        return accessToken;
    }

    private String refreshAccessToken() {
        log.info("Refreshing Paypack access token...");

        PaypackAuthResponse response = paypackWebClient.get()
                .uri("/auth/agents/refresh/" + refreshToken)
                .retrieve()
                .bodyToMono(PaypackAuthResponse.class)
                .block();

        if (response == null || response.getAccess() == null) {
            throw new RuntimeException("Token refresh returned null");
        }

        storeTokens(response);
        log.info("Token refreshed successfully.");
        return accessToken;
    }

    private void storeTokens(PaypackAuthResponse response) {
        this.accessToken = response.getAccess();
        this.refreshToken = response.getRefresh();
        // Paypack tokens last 15 min, store expiry
        this.tokenExpiresAt = Instant.now().plusSeconds(15 * 60);
    }
}