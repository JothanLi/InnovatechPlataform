package com.innovatech.equipos_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "miembros_equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombres;

    @Column(name = "apellido", nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false)
    private String apellidoMaterno;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolEquipo rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMiembro estado;
}
