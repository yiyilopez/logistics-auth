package com.logistica.logistica_auth.adapter.out.security;

import com.logistica.logistica_auth.domain.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenAdapterTest {

    private static final String SECRET = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    private JwtAccessTokenAdapter adapter;

    @BeforeEach
    void configurar() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setAccessTokenMinutes(15);
        adapter = new JwtAccessTokenAdapter(props);
    }

    private UserAccount usuarioDePrueba(UUID id, String roleCode, String sede) {
        return new UserAccount(id, "user@test.com", "hash", "Test", "User", roleCode, sede, true);
    }


    @Test
    void dado_usuarioValido_cuando_createAccessToken_entonces_retornaJwtNoNuloNiVacio() {
        UserAccount usuario = usuarioDePrueba(UUID.randomUUID(), "ADMIN", "SEDE-01");

        String token = adapter.createAccessToken(usuario);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void dado_usuarioConRolYSede_cuando_createAccessToken_entonces_tokenContieneClaimsEsperados() {
        UUID id = UUID.randomUUID();
        UserAccount usuario = usuarioDePrueba(id, "OPERADOR", "SEDE-MDE-01");
        String token = adapter.createAccessToken(usuario);

        SecretKey clave = JwtSigningKey.fromConfig(SECRET);
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(id.toString());
        assertThat(claims.get("typ", String.class)).isEqualTo("access");
        assertThat(claims.get("role", String.class)).isEqualTo("OPERADOR");
        assertThat(claims.get("sede", String.class)).isEqualTo("SEDE-MDE-01");
        assertThat(claims.get("email", String.class)).isEqualTo("user@test.com");
    }

    @Test
    void dado_usuarioSinSede_cuando_createAccessToken_entonces_claimSedeNoPresente() {
        UserAccount usuario = usuarioDePrueba(UUID.randomUUID(), "CLIENTE", null);
        String token = adapter.createAccessToken(usuario);
        SecretKey clave = JwtSigningKey.fromConfig(SECRET);
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("sede")).isNull();
        assertThat(claims.get("role", String.class)).isEqualTo("CLIENTE");
    }
}
