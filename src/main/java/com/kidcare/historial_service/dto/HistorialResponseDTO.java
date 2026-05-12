package com.kidcare.historial_service.dto;

import lombok.Data;
import java.time.LocalDate;

// DTO que retorna los datos de un historial
@Data
public class HistorialResponseDTO {

    // Identificador único del historial
    private Integer idHistorial;

    // ID del menor al que pertenece
    private Integer idMenor;

    // Fecha de generación del resumen
    private LocalDate fecha;

    // Resumen generado por Claude
    private String resumen;

    private Boolean generadoPorIA;
}