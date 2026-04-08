package com.logistica.logistica_auth.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshTokenEntity {

    /**
     * Constructor vacío requerido por JPA para la reflexión y deserialización de entidades.
     * Las instancias de esta clase deben ser creadas a través del constructor por defecto
     * antes de ser inicializadas por el ORM.
     */
    public RefreshTokenEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revocada = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
