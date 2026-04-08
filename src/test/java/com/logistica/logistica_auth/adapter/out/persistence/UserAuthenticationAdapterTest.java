package com.logistica.logistica_auth.adapter.out.persistence;

import com.logistica.logistica_auth.adapter.out.persistence.entity.RolEntity;
import com.logistica.logistica_auth.adapter.out.persistence.entity.UsuarioEntity;
import com.logistica.logistica_auth.adapter.out.persistence.repository.UsuarioJpaRepository;
import com.logistica.logistica_auth.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationAdapterTest {

    @Mock private UsuarioJpaRepository usuarioJpaRepository;

    private UserAuthenticationAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new UserAuthenticationAdapter(usuarioJpaRepository);
    }

    private UsuarioEntity entityMock(UUID id, String email, boolean activo) {
        RolEntity rol = mock(RolEntity.class);
        when(rol.getCodigo()).thenReturn("ADMIN");

        UsuarioEntity entity = mock(UsuarioEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getEmail()).thenReturn(email);
        when(entity.getPasswordHash()).thenReturn("hash");
        when(entity.getNombre()).thenReturn("Ana");
        when(entity.getApellido()).thenReturn("G");
        when(entity.getRol()).thenReturn(rol);
        when(entity.getCodigoSedeAsignada()).thenReturn("SEDE-01");
        when(entity.isActivo()).thenReturn(activo);
        return entity;
    }


    @Test
    void dado_emailExistente_cuando_findActiveByEmail_entonces_retornaUserAccount() {
        UUID id = UUID.randomUUID();
        UsuarioEntity entity = entityMock(id, "ana@test.com", true);
        when(usuarioJpaRepository.findByEmailIgnoreCaseAndActivoIsTrue("ana@test.com"))
                .thenReturn(Optional.of(entity));

        Optional<UserAccount> resultado = adapter.findActiveByEmail("ana@test.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().id()).isEqualTo(id);
        assertThat(resultado.get().email()).isEqualTo("ana@test.com");
        assertThat(resultado.get().roleCode()).isEqualTo("ADMIN");
    }

    @Test
    void dado_emailInexistente_cuando_findActiveByEmail_entonces_retornaVacio() {
        when(usuarioJpaRepository.findByEmailIgnoreCaseAndActivoIsTrue("noexiste@test.com"))
                .thenReturn(Optional.empty());

        Optional<UserAccount> resultado = adapter.findActiveByEmail("noexiste@test.com");

        assertThat(resultado).isEmpty();
    }


    @Test
    void dado_idExistenteYActivo_cuando_findActiveById_entonces_retornaUserAccount() {
        UUID id = UUID.randomUUID();
        UsuarioEntity entity = entityMock(id, "ana@test.com", true);
        when(usuarioJpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<UserAccount> resultado = adapter.findActiveById(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().id()).isEqualTo(id);
        assertThat(resultado.get().activo()).isTrue();
    }

    @Test
    void dado_idExistentePeroInactivo_cuando_findActiveById_entonces_retornaVacio() {
        UUID id = UUID.randomUUID();
        UsuarioEntity entity = mock(UsuarioEntity.class);
        when(entity.isActivo()).thenReturn(false);
        when(usuarioJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        Optional<UserAccount> resultado = adapter.findActiveById(id);
        assertThat(resultado).isEmpty();
    }

    @Test
    void dado_idInexistente_cuando_findActiveById_entonces_retornaVacio() {
        UUID id = UUID.randomUUID();
        when(usuarioJpaRepository.findById(id)).thenReturn(Optional.empty());
        Optional<UserAccount> resultado = adapter.findActiveById(id);
        assertThat(resultado).isEmpty();
    }
}
