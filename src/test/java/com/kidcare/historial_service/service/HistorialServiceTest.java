package com.kidcare.historial_service.service;

import com.kidcare.historial_service.dto.HistorialRequestDTO;
import com.kidcare.historial_service.dto.HistorialResponseDTO;
import com.kidcare.historial_service.model.Historial;
import com.kidcare.historial_service.repository.HistorialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialServiceTest {

    @Mock HistorialRepository historialRepository;
    @Mock ClaudeService claudeService;
    @InjectMocks HistorialService historialService;

    // ─── crear ────────────────────────────────────────────────────────────────

    @Test
    void crear_persiste_con_fecha_de_hoy() {
        HistorialRequestDTO dto = new HistorialRequestDTO();
        dto.setIdMenor(1);
        dto.setResumen("El menor presenta fiebre desde ayer.");

        when(historialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HistorialResponseDTO resultado = historialService.crear(dto);

        assertThat(resultado.getIdMenor()).isEqualTo(1);
        assertThat(resultado.getResumen()).isEqualTo("El menor presenta fiebre desde ayer.");
        assertThat(resultado.getFecha()).isEqualTo(LocalDate.now());
        verify(historialRepository).save(any(Historial.class));
    }

    // ─── obtenerPorMenor ──────────────────────────────────────────────────────

    @Test
    void obtenerPorMenor_retorna_lista_ordenada_mapeada() {
        Historial h1 = new Historial();
        h1.setIdMenor(2);
        h1.setResumen("Resumen IA");
        h1.setFecha(LocalDate.now());
        h1.setGeneradoPorIA(true);

        Historial h2 = new Historial();
        h2.setIdMenor(2);
        h2.setResumen("Resumen manual");
        h2.setFecha(LocalDate.now().minusDays(1));
        h2.setGeneradoPorIA(false);

        when(historialRepository.findByIdMenorOrderByFechaDesc(2)).thenReturn(List.of(h1, h2));

        List<HistorialResponseDTO> resultado = historialService.obtenerPorMenor(2);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getGeneradoPorIA()).isTrue();
        assertThat(resultado.get(0).getResumen()).isEqualTo("Resumen IA");
        assertThat(resultado.get(1).getGeneradoPorIA()).isFalse();
    }

    @Test
    void obtenerPorMenor_lista_vacia_retorna_lista_vacia() {
        when(historialRepository.findByIdMenorOrderByFechaDesc(99)).thenReturn(List.of());

        List<HistorialResponseDTO> resultado = historialService.obtenerPorMenor(99);

        assertThat(resultado).isEmpty();
    }

    // ─── obtenerUltimo ────────────────────────────────────────────────────────

    @Test
    void obtenerUltimo_sin_historiales_lanza_excepcion() {
        when(historialRepository.findTopByIdMenorOrderByFechaDesc(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historialService.obtenerUltimo(5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay historial");
    }

    @Test
    void obtenerUltimo_con_historial_retorna_dto() {
        Historial h = new Historial();
        h.setIdMenor(3);
        h.setResumen("Último resumen");
        h.setFecha(LocalDate.now());
        h.setGeneradoPorIA(true);

        when(historialRepository.findTopByIdMenorOrderByFechaDesc(3)).thenReturn(Optional.of(h));

        HistorialResponseDTO resultado = historialService.obtenerUltimo(3);

        assertThat(resultado.getResumen()).isEqualTo("Último resumen");
        assertThat(resultado.getGeneradoPorIA()).isTrue();
    }

    // ─── mapToDTO — campo generadoPorIA ───────────────────────────────────────

    @Test
    void mapToDTO_generadoPorIA_null_retorna_false() {
        Historial h = new Historial();
        h.setIdMenor(1);
        h.setResumen("Resumen");
        h.setFecha(LocalDate.now());
        h.setGeneradoPorIA(null);

        when(historialRepository.findByIdMenorOrderByFechaDesc(1)).thenReturn(List.of(h));

        List<HistorialResponseDTO> resultado = historialService.obtenerPorMenor(1);

        assertThat(resultado.get(0).getGeneradoPorIA()).isFalse();
    }

    // ─── generarConClaude — sin interacciones ─────────────────────────────────

    @Test
    void generarConClaude_sin_interacciones_encontradas_lanza_excepcion() {
        // El chatbot-service no está disponible en tests unitarios (RestTemplate real).
        // Cuando falla la llamada HTTP, todasInteracciones queda vacía y el filtro por
        // idInteracciones no encuentra nada → RuntimeException esperada.
        com.kidcare.historial_service.dto.GenerarHistorialRequestDTO dto =
                new com.kidcare.historial_service.dto.GenerarHistorialRequestDTO();
        dto.setIdMenor(1);
        dto.setIdInteracciones(List.of("id_que_no_existe"));

        assertThatThrownBy(() -> historialService.generarConClaude(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontraron las interacciones");
    }
}
