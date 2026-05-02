package com.kidcare.historial_service.repository;

import com.kidcare.historial_service.model.Historial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// Repositorio que maneja el acceso a datos de la entidad Historial
@Repository
public interface HistorialRepository extends JpaRepository<Historial, Integer> {

    // Obtiene todos los historiales de un menor ordenados por fecha
    List<Historial> findByIdMenorOrderByFechaDesc(Integer idMenor);

    // Obtiene el historial más reciente de un menor
    Optional<Historial> findTopByIdMenorOrderByFechaDesc(Integer idMenor);
}