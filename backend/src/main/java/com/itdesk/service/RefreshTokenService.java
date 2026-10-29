package com.itdesk.service;

import com.itdesk.entity.RefreshToken;
import com.itdesk.entity.User;
import com.itdesk.exception.ResourceNotFoundException;
import com.itdesk.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // Naya refresh token banao aur DB mein save karo
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Pehle user ke saare purane tokens revoke karo
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // Token string se valid (non-revoked) RefreshToken dhundo
    @Transactional(readOnly = true)
    public RefreshToken findValidToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refresh token not found or already revoked"));
    }

    // Token validate karo — expired hai to revoke karke exception throw karo
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.warn("Refresh token expired for user: {}", token.getUser().getEmail());
            throw new ResourceNotFoundException("Refresh token expired. Please login again.");
        }
        return token;
    }

    // Logout ke liye: specific token revoke karo
    @Transactional
    public void revokeToken(String tokenString) {
        refreshTokenRepository.findByToken(tokenString).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user: {}", token.getUser().getEmail());
        });
    }

    // Security breach pe: user ke SAARE tokens revoke karo
    @Transactional
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.warn("All refresh tokens revoked for user: {}", user.getEmail());
    }
}
