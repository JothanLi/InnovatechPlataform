package com.innovatech.tareas_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTarea estado;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrioridadTarea prioridad;

    @Column(name = "proyecto_id")
    private Long proyectoId;

    @Column(name = "equipo_id")
    private Long equipoId;

    @Column(name = "asignado_a")
    private Long asignadoA;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
