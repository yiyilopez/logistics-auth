package com.logistica.logistica_auth.application.service;

import com.logistica.logistica_auth.domain.exception.AuthCredentialsException;
import com.logistica.logistica_auth.domain.model.AuthTokens;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.out.AccessTokenPort;
import com.logistica.logistica_auth.domain.port.out.AccessTokenTtlPort;
import com.logistica.logistica_auth.domain.port.out.AuthenticationAuditPort;
import com.logistica.logistica_auth.domain.port.out.PasswordHasherPort;
import com.logistica.logistica_auth.domain.port.out.RefreshTokenPort;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock private UserAuthenticationPort userAuthenticationPort;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private AccessTokenPort accessTokenPort;
    @Mock private RefreshTokenPort refreshTokenPort;
    @Mock private AuthenticationAuditPort authenticationAuditPort;
    @Mock private AccessTokenTtlPort accessTokenTtlPort;

    @InjectMocks
    private AuthenticateUserService service;

    private static final UUID USER_ID  = UUID.randomUUID();
    private static final String EMAIL   = "usuario@test.com";
    private static final String RAW_PW  = "clave123";
    private static final String HASH    = "$2a$10$hashedpassword";
    private static final String IP      = "127.0.0.1";
    private static final String UA      = "JUnit-Agent/5";

    private UserAccount usuarioActivo() {
        return new UserAccount(USER_ID, EMAIL, HASH, "Ana", "López", "ADMIN", "SEDE-01", true);
    }

    @Test
    void dado_usuarioExistenteYContrasenaValida_cuando_login_entonces_retornaAuthTokens() {
        UserAccount usuario = usuarioActivo();
        when(userAuthenticationPort.findActiveByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordHasherPort.matches(RAW_PW, HASH)).thenReturn(true);
        when(accessTokenPort.createAccessToken(usuario)).thenReturn("jwt-access");
        RefreshTokenPort.StoredRefresh stored =
                new RefreshTokenPort.StoredRefresh("jwt-refresh", UUID.randomUUID());
        when(refreshTokenPort.issue(usuario)).thenReturn(stored);
        when(accessTokenTtlPort.expiresInSeconds()).thenReturn(900L);
        AuthTokens resultado = service.login(EMAIL, RAW_PW, IP, UA);
        assertThat(resultado.accessToken()).isEqualTo("jwt-access");
        assertThat(resultado.refreshToken()).isEqualTo("jwt-refresh");
        assertThat(resultado.expiresInSeconds()).isEqualTo(900L);
        verify(authenticationAuditPort).record(USER_ID, "login_ok", IP, UA, null);
    }

    @Test
    void dado_usuarioNoEncontrado_cuando_login_entonces_lanzaAuthCredentialsException() {
        when(userAuthenticationPort.findActiveByEmail(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(EMAIL, RAW_PW, IP, UA))
                .isInstanceOf(AuthCredentialsException.class);
        verify(authenticationAuditPort).record(isNull(), eq("login_fail"), eq(IP), eq(UA),
                eq("Usuario no encontrado"));
    }

    @Test
    void dado_contrasenaIncorrecta_cuando_login_entonces_lanzaAuthCredentialsException() {
        when(userAuthenticationPort.findActiveByEmail(EMAIL)).thenReturn(Optional.of(usuarioActivo()));
        when(passwordHasherPort.matches(RAW_PW, HASH)).thenReturn(false);
        assertThatThrownBy(() -> service.login(EMAIL, RAW_PW, IP, UA))
                .isInstanceOf(AuthCredentialsException.class);
        verify(authenticationAuditPort).record(USER_ID, "login_fail", IP, UA, "Contraseña incorrecta");
    }

    @Test
    void dado_usuarioInactivo_cuando_login_entonces_lanzaAuthCredentialsException() {
        when(userAuthenticationPort.findActiveByEmail(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(EMAIL, RAW_PW, IP, UA))
                .isInstanceOf(AuthCredentialsException.class);
        verify(authenticationAuditPort).record(isNull(), eq("login_fail"), eq(IP), eq(UA),
                eq("Usuario no encontrado"));
    }

    @Test
    void dado_refreshTokenValido_cuando_refresh_entonces_retornaAuthTokens() {
        UserAccount usuario = usuarioActivo();
        when(refreshTokenPort.validateAndConsume("token-valido")).thenReturn(Optional.of(usuario));
        when(accessTokenPort.createAccessToken(usuario)).thenReturn("nuevo-access");
        RefreshTokenPort.StoredRefresh stored =
                new RefreshTokenPort.StoredRefresh("nuevo-refresh", UUID.randomUUID());
        when(refreshTokenPort.issue(usuario)).thenReturn(stored);
        when(accessTokenTtlPort.expiresInSeconds()).thenReturn(900L);
        AuthTokens resultado = service.refresh("token-valido", IP, UA);
        assertThat(resultado.accessToken()).isEqualTo("nuevo-access");
        assertThat(resultado.refreshToken()).isEqualTo("nuevo-refresh");
        verify(authenticationAuditPort).record(USER_ID, "refresh", IP, UA, null);
    }

    @Test
    void dado_refreshTokenInvalidoORevocado_cuando_refresh_entonces_lanzaAuthCredentialsException() {
        when(refreshTokenPort.validateAndConsume(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refresh("token-invalido", IP, UA))
                .isInstanceOf(AuthCredentialsException.class)
                .hasMessageContaining("Refresh token inválido o revocado");
    }
}
