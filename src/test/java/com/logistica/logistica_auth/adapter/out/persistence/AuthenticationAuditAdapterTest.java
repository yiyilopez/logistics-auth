package com.logistica.logistica_auth.adapter.out.persistence;

import com.logistica.logistica_auth.adapter.out.persistence.entity.AuditoriaAutenticacionEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.AuditoriaAutenticacionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationAuditAdapterTest {

    @Mock private AuditoriaAutenticacionJpaRepository repository;

    private AuthenticationAuditAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new AuthenticationAuditAdapter(repository);
    }


    @Test
    void dado_parametrosValidos_cuando_record_entonces_guardaEntidadEnRepositorio() {
        UUID userId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        adapter.record(userId, "login_ok", "192.168.1.1", "Mozilla/5.0", null);
        ArgumentCaptor<AuditoriaAutenticacionEntity> captor =
                ArgumentCaptor.forClass(AuditoriaAutenticacionEntity.class);
        verify(repository).save(captor.capture());
        AuditoriaAutenticacionEntity guardado = captor.getValue();

        assertThat(guardado.getUsuarioId()).isEqualTo(userId);
        assertThat(guardado.getTipoEvento()).isEqualTo("login_ok");
        assertThat(guardado.getIp()).isEqualTo("192.168.1.1");
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getCreatedAt()).isNotNull();
    }

    @Test
    void dado_userIdNull_cuando_record_entonces_guardaEntidadSinUsuario() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        adapter.record(null, "login_fail", "10.0.0.1", "curl/7.0", "Usuario no encontrado");
        ArgumentCaptor<AuditoriaAutenticacionEntity> captor =
                ArgumentCaptor.forClass(AuditoriaAutenticacionEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUsuarioId()).isNull();
        assertThat(captor.getValue().getDetalle()).isEqualTo("Usuario no encontrado");
    }

    @Test
    void dado_ipMasLargaQueLimite_cuando_record_entonces_ipEsTruncada() {
        String ipLarga = "x".repeat(100);  
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        adapter.record(null, "login_fail", ipLarga, "agent", null);
        ArgumentCaptor<AuditoriaAutenticacionEntity> captor =
                ArgumentCaptor.forClass(AuditoriaAutenticacionEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIp()).hasSize(64);
    }

    @Test
    void dado_todosLosValoresNull_cuando_record_entonces_noLanzaExcepcion() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        adapter.record(null, "evento", null, null, null);
        verify(repository).save(any(AuditoriaAutenticacionEntity.class));
    }
}
