package com.logistica.logistica_auth.adapter.out.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BcryptPasswordHasherAdapterTest {

    @Mock private PasswordEncoder passwordEncoder;

    private BcryptPasswordHasherAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new BcryptPasswordHasherAdapter(passwordEncoder);
    }


    @Test
    void dado_contrasenaCorrecta_cuando_matches_entonces_retornaTrue() {
        String raw  = "miClave123";
        String hash = "$2a$10$ABC...hashBcrypt";
        when(passwordEncoder.matches(raw, hash)).thenReturn(true);

        boolean resultado = adapter.matches(raw, hash);

        assertThat(resultado).isTrue();
    }

    @Test
    void dado_contrasenaIncorrecta_cuando_matches_entonces_retornaFalse() {
        String raw  = "claveErronea";
        String hash = "$2a$10$ABC...hashBcrypt";
        when(passwordEncoder.matches(raw, hash)).thenReturn(false);

        boolean resultado = adapter.matches(raw, hash);

        assertThat(resultado).isFalse();
    }


    @Test
    void dado_contrasenaPlana_cuando_encode_entonces_retornaHashDistintoAlInput() {
        String raw  = "contrasena-plana";
        String hash = "$2a$10$HASH_GENERADO_POR_BCRYPT_NO_ES_EL_MISMO_INPUT";
        when(passwordEncoder.encode(raw)).thenReturn(hash);
        String resultado = adapter.encode(raw);
        assertThat(resultado).isNotNull().isNotEqualTo(raw);
    }
}
