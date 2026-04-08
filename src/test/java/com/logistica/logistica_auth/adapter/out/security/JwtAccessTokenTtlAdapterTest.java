package com.logistica.logistica_auth.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenTtlAdapterTest {

    @Test
    void dado_accessTokenMinutes15_cuando_expiresInSeconds_entonces_retorna900() {
        JwtProperties props = new JwtProperties();
        props.setAccessTokenMinutes(15);
        JwtAccessTokenTtlAdapter adapter = new JwtAccessTokenTtlAdapter(props);
        long resultado = adapter.expiresInSeconds();
        assertThat(resultado).isEqualTo(900L);
    }

    @Test
    void dado_accessTokenMinutes60_cuando_expiresInSeconds_entonces_retorna3600() {
        JwtProperties props = new JwtProperties();
        props.setAccessTokenMinutes(60);
        JwtAccessTokenTtlAdapter adapter = new JwtAccessTokenTtlAdapter(props);
        long resultado = adapter.expiresInSeconds();
        assertThat(resultado).isEqualTo(3600L);
    }
}
