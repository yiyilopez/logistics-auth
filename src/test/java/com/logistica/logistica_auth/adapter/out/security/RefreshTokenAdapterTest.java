package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.adapter.out.persistence.entity.RefreshTokenEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import com.logistica.logistica_auth.domain.model.UserAccount;
import com.logistica.logistica_auth.domain.port.out.RefreshTokenPort;
import com.logistica.logistica_auth.domain.port.out.UserAuthenticationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenAdapterTest {

    @Mock private RefreshTokenJpaRepository refreshTokenJpaRepository;
    @Mock private UserAuthenticationPort userAuthenticationPort;

    private RefreshTokenAdapter adapter;
    private static final String SECRET = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    @BeforeEach
    void configurar() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setRefreshTokenDays(7);
        adapter = new RefreshTokenAdapter(props, refreshTokenJpaRepository, userAuthenticationPort);
    }

    private UserAccount usuarioDePrueba() {
        return new UserAccount(UUID.randomUUID(), "u@t.com", "hash", "Ana", "G", "ADMIN", "SEDE-01", true);
    }

    @Test
    void dado_usuarioValido_cuando_issue_entonces_retornaJwtNoNulo() {
        UserAccount usuario = usuarioDePrueba();
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RefreshTokenPort.StoredRefresh resultado = adapter.issue(usuario);
        assertThat(resultado.jwt()).isNotNull().isNotBlank();
        assertThat(resultado.recordId()).isNotNull();
    }

    @Test
    void dado_usuarioValido_cuando_issue_entonces_guardaEntidadEnRepositorio() {
        UserAccount usuario = usuarioDePrueba();
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.issue(usuario);

        verify(refreshTokenJpaRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void dado_tokenValido_cuando_validateAndConsume_entonces_retornaUserAccount() {
        UserAccount usuario = usuarioDePrueba();
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RefreshTokenPort.StoredRefresh stored = adapter.issue(usuario);

        String jwt = stored.jwt();
        UUID tokenId = stored.recordId();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(tokenId);
        entity.setUsuarioId(usuario.id());
        entity.setTokenHash(TokenHashUtils.sha256Hex(jwt));
        entity.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        entity.setRevocada(false);
        entity.setCreatedAt(Instant.now());

        when(refreshTokenJpaRepository.findById(tokenId)).thenReturn(Optional.of(entity));
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userAuthenticationPort.findActiveById(usuario.id())).thenReturn(Optional.of(usuario));

        Optional<UserAccount> resultado = adapter.validateAndConsume(jwt);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().id()).isEqualTo(usuario.id());
        assertThat(resultado.get().email()).isEqualTo(usuario.email());
    }

    @Test
    void dado_tokenRevocado_cuando_validateAndConsume_entonces_retornaVacio() {
        UserAccount usuario = usuarioDePrueba();
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RefreshTokenPort.StoredRefresh stored = adapter.issue(usuario);

        String jwt = stored.jwt();
        UUID tokenId = stored.recordId();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(tokenId);
        entity.setUsuarioId(usuario.id());
        entity.setTokenHash(TokenHashUtils.sha256Hex(jwt));
        entity.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        entity.setRevocada(true);  
        entity.setCreatedAt(Instant.now());

        when(refreshTokenJpaRepository.findById(tokenId)).thenReturn(Optional.of(entity));

        Optional<UserAccount> resultado = adapter.validateAndConsume(jwt);

        assertThat(resultado).isEmpty();
    }

    @Test
    void dado_stringBasura_cuando_validateAndConsume_entonces_retornaVacio() {
        Optional<UserAccount> resultado = adapter.validateAndConsume("no-es-un-jwt-valido");
        assertThat(resultado).isEmpty();
    }

    @Test
    void dado_tokenExpirado_cuando_validateAndConsume_entonces_retornaVacio() {
        UserAccount usuario = usuarioDePrueba();
        when(refreshTokenJpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        RefreshTokenPort.StoredRefresh stored = adapter.issue(usuario);

        String jwt = stored.jwt();
        UUID tokenId = stored.recordId();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(tokenId);
        entity.setUsuarioId(usuario.id());
        entity.setTokenHash(TokenHashUtils.sha256Hex(jwt));
        entity.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));  // <-- expirado
        entity.setRevocada(false);
        entity.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));

        when(refreshTokenJpaRepository.findById(tokenId)).thenReturn(Optional.of(entity));

        Optional<UserAccount> resultado = adapter.validateAndConsume(jwt);

        assertThat(resultado).isEmpty();
    }
}
