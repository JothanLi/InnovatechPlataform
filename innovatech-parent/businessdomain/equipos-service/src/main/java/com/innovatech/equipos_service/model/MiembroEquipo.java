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

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolEquipo rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMiembro estado;
}