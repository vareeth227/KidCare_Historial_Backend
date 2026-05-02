package com.kidcare.historial_service.service;

import com.kidcare.historial_service.dto.HistorialRequestDTO;
import com.kidcare.historial_service.dto.HistorialResponseDTO;
import com.kidcare.historial_service.model.Historial;
import com.kidcare.historial_service.repository.HistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Servicio que maneja la gestión de historiales
@Service
public class HistorialService {

    @Autowired
    private HistorialRepository historialRepository;

    // Crea un nuevo historial con el resumen generado por Claude
    public HistorialResponseDTO crear(HistorialRequestDTO dto) {

        Historial historial = new Historial();
        historial.setIdMenor(dto.getIdMenor());
        historial.setResumen(dto.getResumen());
        historial.setFecha(LocalDate.now());
        historialRepository.save(historial);

        return mapToDTO(historial);
    }

    // Obtiene todos los historiales de un menor
    public List<HistorialResponseDTO> obtenerPorMenor(Integer idMenor) {
        return historialRepository.findByIdMenorOrderByFechaDesc(idMenor)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Obtiene el historial más reciente de un menor
    public HistorialResponseDTO obtenerUltimo(Integer idMenor) {
        Historial historial = historialRepository.findTopByIdMenorOrderByFechaDesc(idMenor)
                .orElseThrow(() -> new RuntimeException("No hay historial para este menor"));
        return mapToDTO(historial);
    }

    // Convierte una entidad Historial a HistorialResponseDTO
    private HistorialResponseDTO mapToDTO(Historial historial) {
        HistorialResponseDTO dto = new HistorialResponseDTO();
        dto.setIdHistorial(historial.getIdHistorial());
        dto.setIdMenor(historial.getIdMenor());
        dto.setFecha(historial.getFecha());
        dto.setResumen(historial.getResumen());
        return dto;
    }
}