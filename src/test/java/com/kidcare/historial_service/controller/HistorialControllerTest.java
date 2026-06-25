package com.kidcare.historial_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidcare.historial_service.dto.GenerarHistorialRequestDTO;
import com.kidcare.historial_service.dto.GenerarHistorialResponseDTO;
import com.kidcare.historial_service.dto.HistorialRequestDTO;
import com.kidcare.historial_service.dto.HistorialResponseDTO;
import com.kidcare.historial_service.security.JwtFilter;
import com.kidcare.historial_service.security.JwtUtil;
import com.kidcare.historial_service.security.SecurityConfig;
import com.kidcare.historial_service.service.HistorialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, JwtFilter.class})
@WebMvcTest(HistorialController.class)
class HistorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HistorialService historialService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // ─── POST /api/historial ──────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "tutor@test.com", roles = {"TUTOR"})
    void crear_ConDatosValidos_Retorna200Ok() throws Exception {
        HistorialRequestDTO requestDTO = new HistorialRequestDTO();
        requestDTO.setIdMenor(1);
        requestDTO.setResumen("El menor presenta tos leve.");

        HistorialResponseDTO responseDTO = new HistorialResponseDTO();
        responseDTO.setIdHistorial(100);
        responseDTO.setIdMenor(1);
        responseDTO.setResumen("El menor presenta tos leve.");
        responseDTO.setGeneradoPorIA(false);
        responseDTO.setFecha(LocalDate.now());

        when(historialService.crear(any(HistorialRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/historial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorial").value(100))
                .andExpect(jsonPath("$.idMenor").value(1))
                .andExpect(jsonPath("$.resumen").value("El menor presenta tos leve."))
                .andExpect(jsonPath("$.generadoPorIA").value(false));

        verify(historialService).crear(any(HistorialRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "tutor@test.com", roles = {"TUTOR"})
    void crear_ConDatosInvalidos_Retorna400BadRequest() throws Exception {
        HistorialRequestDTO requestDTO = new HistorialRequestDTO();
        // Faltan idMenor y resumen

        mockMvc.perform(post("/api/historial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_SinAutenticacion_Retorna403o401() throws Exception {
        HistorialRequestDTO requestDTO = new HistorialRequestDTO();
        requestDTO.setIdMenor(1);
        requestDTO.setResumen("El menor presenta tos leve.");

        mockMvc.perform(post("/api/historial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    // ─── GET /api/historial/menor/{idMenor} ────────────────────────────────────

    @Test
    @WithMockUser(username = "tutor@test.com", roles = {"TUTOR"})
    void listarPorMenor_RetornaListaHistoriales() throws Exception {
        HistorialResponseDTO r1 = new HistorialResponseDTO();
        r1.setIdHistorial(100);
        r1.setIdMenor(1);
        r1.setResumen("Resumen Clínico");

        when(historialService.obtenerPorMenor(1)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/historial/menor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idHistorial").value(100))
                .andExpect(jsonPath("$[0].resumen").value("Resumen Clínico"));

        verify(historialService).obtenerPorMenor(1);
    }

    // ─── GET /api/historial/medico/{idMenor} (PermitAll) ─────────────────────────

    @Test
    void obtenerUltimo_SinAutenticacion_Permitido_RetornaHistorial() throws Exception {
        HistorialResponseDTO r1 = new HistorialResponseDTO();
        r1.setIdHistorial(102);
        r1.setIdMenor(1);
        r1.setResumen("Último Resumen para Médico");

        when(historialService.obtenerUltimo(1)).thenReturn(r1);

        mockMvc.perform(get("/api/historial/medico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorial").value(102))
                .andExpect(jsonPath("$.resumen").value("Último Resumen para Médico"));

        verify(historialService).obtenerUltimo(1);
    }

    // ─── POST /api/historial/generar ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "tutor@test.com", roles = {"TUTOR"})
    void generarConIA_ConDatosValidos_Retorna200Ok() throws Exception {
        GenerarHistorialRequestDTO requestDTO = new GenerarHistorialRequestDTO();
        requestDTO.setIdMenor(1);
        requestDTO.setIdInteracciones(List.of("int-1", "int-2"));

        GenerarHistorialResponseDTO responseDTO = new GenerarHistorialResponseDTO();
        responseDTO.setIdHistorial(200);
        responseDTO.setIdMenor(1);
        responseDTO.setResumen("Resumen generado con IA.");
        responseDTO.setGeneradoPorIA(true);

        when(historialService.generarConClaude(any(GenerarHistorialRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/historial/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorial").value(200))
                .andExpect(jsonPath("$.resumen").value("Resumen generado con IA."))
                .andExpect(jsonPath("$.generadoPorIA").value(true));

        verify(historialService).generarConClaude(any(GenerarHistorialRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "tutor@test.com", roles = {"TUTOR"})
    void generarConIA_ConDatosInvalidos_Retorna400BadRequest() throws Exception {
        GenerarHistorialRequestDTO requestDTO = new GenerarHistorialRequestDTO();
        requestDTO.setIdMenor(1);
        requestDTO.setIdInteracciones(List.of()); // Lista vacía, invalida @NotEmpty

        mockMvc.perform(post("/api/historial/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/historial/interno/generar (PermitAll) ─────────────────────────

    @Test
    void generarInterno_SinAutenticacion_Permitido_Retorna200Ok() throws Exception {
        GenerarHistorialRequestDTO requestDTO = new GenerarHistorialRequestDTO();
        requestDTO.setIdMenor(1);
        requestDTO.setIdInteracciones(List.of("int-1"));

        GenerarHistorialResponseDTO responseDTO = new GenerarHistorialResponseDTO();
        responseDTO.setIdHistorial(300);
        responseDTO.setIdMenor(1);
        responseDTO.setResumen("Resumen Generado Internamente.");
        responseDTO.setGeneradoPorIA(true);

        when(historialService.generarConClaude(any(GenerarHistorialRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/historial/interno/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorial").value(300))
                .andExpect(jsonPath("$.resumen").value("Resumen Generado Internamente."));

        verify(historialService).generarConClaude(any(GenerarHistorialRequestDTO.class));
    }
}
