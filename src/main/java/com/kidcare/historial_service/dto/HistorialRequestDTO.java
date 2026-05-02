package com.kidcare.historial_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO que recibe los datos para crear un historial
@Data
public class HistorialRequestDTO {

    // ID del menor al que pertenece el historial
    @NotNull(message = "El id del menor es obligatorio")
    private Integer idMenor;

    // Resumen generado por Claude
    @NotBlank(message = "El resumen es obligatorio")
    private String resumen;
}