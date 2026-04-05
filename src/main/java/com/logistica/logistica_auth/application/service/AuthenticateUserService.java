package com.logistica.logistica_auth.application.service;

import com.logistica.logistica_auth.domain.model.AuthTokens;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.in.AuthenticateUserUseCase;
import com.logistica.logistica_auth.domain.port.out.AccessTokenPort;
import com.logistica.logistica_auth.domain.port.out.AuthenticationAuditPort;
import com.logistica.logistica_auth.domain.port.out.PasswordHasherPort;
import com.logistica.logistica_auth.domain.port.out.RefreshTokenPort;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import com.logistica.logistica_auth.adapter.out.security.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserAuthenticationPort userAuthenticationPort;
    private final PasswordHasherPort passwordHasherPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuthenticationAuditPort authenticationAuditPort;
    private final long accessTokenSeconds;

    public AuthenticateUserService(
            UserAuthenticationPort userAuthenticationPort,
            PasswordHasherPort passwordHasherPort,
            AccessTokenPort accessTokenPort,
            RefreshTokenPort refreshTokenPort,
            AuthenticationAuditPort authenticationAuditPort,
            JwtProperties jwtProperties
    ) {
        this.userAuthenticationPort = userAuthenticationPort;
        this.passwordHasherPort = passwordHasherPort;
        this.accessTokenPort = accessTokenPort;
        this.refreshTokenPort = refreshTokenPort;
        this.authenticationAuditPort = authenticationAuditPort;
        this.accessTokenSeconds = jwtProperties.getAccessTokenMinutes() * 60L;
    }

    @Override
    @Transactional
    public AuthTokens login(String email, String rawPassword, String ip, String userAgent) {
        var userOpt = userAuthenticationPort.findActiveByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            authenticationAuditPort.record(null, "login_fail", ip, userAgent, "Usuario no encontrado");
            throw new AuthCredentialsException("Credenciales inválidas");
        }
        UserAccount user = userOpt.get();
        if (!passwordHasherPort.matches(rawPassword, user.passwordHash())) {
            authenticationAuditPort.record(user.id(), "login_fail", ip, userAgent, "Contraseña incorrecta");
            throw new AuthCredentialsException("Credenciales inválidas");
        }
        authenticationAuditPort.record(user.id(), "login_ok", ip, userAgent, null);
        String access = accessTokenPort.createAccessToken(user);
        var refresh = refreshTokenPort.issue(user);
        return new AuthTokens(access, refresh.jwt(), accessTokenSeconds);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshTokenJwt, String ip, String userAgent) {
        UserAccount user = refreshTokenPort.validateAndConsume(refreshTokenJwt)
                .orElseThrow(() -> new AuthCredentialsException("Refresh token inválido o revocado"));
        String access = accessTokenPort.createAccessToken(user);
        var refresh = refreshTokenPort.issue(user);
        authenticationAuditPort.record(user.id(), "refresh", ip, userAgent, null);
        return new AuthTokens(access, refresh.jwt(), accessTokenSeconds);
    }

    public static class AuthCredentialsException extends RuntimeException {
        public AuthCredentialsException(String message) {
            super(message);
        }
    }
}
