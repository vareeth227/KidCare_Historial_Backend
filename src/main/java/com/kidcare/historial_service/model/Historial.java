package com.kidcare.historial_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

// Entidad que almacena los resúmenes generados por Claude para el médico
@Data
@Entity
@Table(name = "HISTORIAL")
public class Historial {

    // Identificador único del historial
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    // Referencia lógica al menor en db_users
    @Column(name = "id_menor", nullable = false)
    private Integer idMenor;

    // Fecha de generación del resumen
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // Resumen estructurado generado por Claude para el médico
    @Column(name = "resumen", nullable = false, columnDefinition = "TEXT")
    private String resumen;
}