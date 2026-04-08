package com.logistica.logistica_auth.adapter.in.web;

import com.logistica.logistica_auth.domain.exception.AuthCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void configurar() {
        handler = new GlobalExceptionHandler();
    }



    @Test
    void dado_authCredentialsException_cuando_authError_entonces_retorna401ConMensaje() {
        AuthCredentialsException ex = new AuthCredentialsException("Credenciales inválidas");
        ResponseEntity<Map<String, String>> respuesta = handler.authError(ex);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respuesta.getBody()).containsEntry("error", "Credenciales inválidas");
    }



    @Test
    void dado_methodArgumentNotValidException_cuando_validation_entonces_retorna400ConDetalle() {
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getDefaultMessage()).thenReturn("el email no puede estar vacío");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        ResponseEntity<Map<String, Object>> respuesta = handler.validation(ex);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("error", "Validación");
        assertThat(respuesta.getBody()).containsKey("detalle");
        assertThat(respuesta.getBody().get("detalle").toString())
                .contains("el email no puede estar vacío");
    }

    @Test
    void dado_multipleFieldErrors_cuando_validation_entonces_mensajesConcatenados() {
        FieldError error1 = mock(FieldError.class);
        when(error1.getDefaultMessage()).thenReturn("campo requerido");
        FieldError error2 = mock(FieldError.class);
        when(error2.getDefaultMessage()).thenReturn("formato inválido");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = handler.validation(ex);

        assertThat(respuesta.getBody().get("detalle").toString())
                .contains("campo requerido")
                .contains("formato inválido");
    }
}
