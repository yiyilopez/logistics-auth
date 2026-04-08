package com.logistica.logistica_auth.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class TokenHashUtilsTest {


    @Test
    void dado_mismoInput_cuando_sha256Hex_entonces_produceMismoOutput() {

        String entrada = "mi-token-de-prueba-12345";

        String hash1 = TokenHashUtils.sha256Hex(entrada);
        String hash2 = TokenHashUtils.sha256Hex(entrada);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void dado_inputsDistintos_cuando_sha256Hex_entonces_produceOutputsDistintos() {
        String entrada1 = "token-a";
        String entrada2 = "token-b";
        String hash1 = TokenHashUtils.sha256Hex(entrada1);
        String hash2 = TokenHashUtils.sha256Hex(entrada2);
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void dado_input_cuando_sha256Hex_entonces_outputEsDistintoAlInput() {
        String entrada = "texto-plano";
        String hash = TokenHashUtils.sha256Hex(entrada);
        assertThat(hash).isNotEqualTo(entrada);
    }

    @Test
    void dado_input_cuando_sha256Hex_entonces_outputEsHexadecimalDe64Caracteres() {
        String entrada = "cualquier-token";
        String hash = TokenHashUtils.sha256Hex(entrada);
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }
}
