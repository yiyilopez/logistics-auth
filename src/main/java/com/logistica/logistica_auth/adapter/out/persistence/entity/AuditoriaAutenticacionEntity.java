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
@Table(name = "auditoria_autenticacion")
@Getter
@Setter
public class AuditoriaAutenticacionEntity {

    /**
     * Constructor vacío requerido por JPA para la reflexión y deserialización de entidades.
     * Las instancias de esta clase deben ser creadas a través del constructor por defecto
     * antes de ser inicializadas por el ORM.
     */
    public AuditoriaAutenticacionEntity() {
    }

    @Id
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 1000)
    private String detalle;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
