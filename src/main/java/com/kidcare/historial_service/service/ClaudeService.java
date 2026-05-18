package com.kidcare.historial_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClaudeService {

    @Value("${claude.api.key:}")
    private String apiKey;

    @Value("${claude.model:gpt-4o-mini}")
    private String model;

    @Value("${claude.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    public String generarResumenClinico(List<String> observaciones, String contextoMenor) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OpenAI API no configurada");
        }

        StringBuilder obsText = new StringBuilder();
        for (int i = 0; i < observaciones.size(); i++) {
            obsText.append((i + 1)).append(". ").append(observaciones.get(i)).append("\n");
        }

        String prompt = "Genera un resumen clínico en español para un médico pediatra " +
            "basado en las siguientes observaciones de un cuidador. " +
            "Incluye únicamente comportamientos y síntomas observables reportados. " +
            "NO incluyas diagnósticos, recomendaciones de tratamiento ni información personal identificable. " +
            "REGLAS DE FORMATO ESTRICTAS: " +
            "1. Escribe ÚNICAMENTE texto plano en español. " +
            "2. NO uses JSON, XML ni ningún formato de datos estructurado. " +
            "3. NO uses markdown (sin #, **, *, >, -, ``` ni similares). " +
            "4. Organiza el texto en EXACTAMENTE cinco secciones con estos encabezados: " +
            "Motivo de consulta, Síntomas, Evolución, Medicación administrada, Antecedentes. " +
            "5. Cada encabezado va seguido de dos puntos y el texto en la misma línea. " +
            "6. Si no hay información para una sección escribe 'Sin registros'. " +
            "Ejemplo de formato: " +
            "Motivo de consulta: El cuidador reporta fiebre y tos persistente. " +
            "Síntomas: Fiebre de 38,5°C, tos seca, pérdida de apetito. " +
            "Evolución: Los síntomas comenzaron hace 2 días y han aumentado progresivamente. " +
            "Medicación administrada: Sin registros. " +
            "Antecedentes: Sin registros.\n\n" +
            "Contexto: " + contextoMenor + "\n\n" +
            "Observaciones registradas:\n" + obsText;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 2048);
        body.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<?> choices = (List<?>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                Map<?, ?> msg = (Map<?, ?>) firstChoice.get("message");
                if (msg != null && msg.get("content") != null) {
                    String raw = msg.get("content").toString().trim();
                    // Eliminar bloques markdown y cualquier envoltorio JSON accidental
                    String limpio = raw
                        .replaceAll("(?s)^```[a-z]*\\s*", "")
                        .replaceAll("```\\s*$", "")
                        .replaceAll("(?m)^#{1,6}\\s*", "")
                        .replaceAll("\\*{1,2}([^*]+)\\*{1,2}", "$1")
                        .trim();
                    return limpio;
                }
            }
        }
        throw new RuntimeException("OpenAI no retornó respuesta válida");
    }
}
