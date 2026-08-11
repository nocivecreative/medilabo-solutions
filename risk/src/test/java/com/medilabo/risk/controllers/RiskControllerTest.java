package com.medilabo.risk.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.medilabo.risk.dto.RiskReportDTO;
import com.medilabo.risk.model.RiskLevel;
import com.medilabo.risk.service.RiskService;

/**
 * Test de tranche web (milieu de pyramide) : seule la couche MVC est chargée,
 * le service est mocké. Vérifie le contrat HTTP et la traduction des erreurs amont.
 */
@WebMvcTest(RiskController.class)
@DisplayName("RiskController (web slice)")
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiskService riskService;

    @Test
    @DisplayName("GET /risk/patient/{id} should return 200 with the risk report")
    void shouldReturnRiskReport() throws Exception {
        // Arrange
        RiskReportDTO report = new RiskReportDTO(3L, RiskLevel.IN_DANGER, 22, 3,
                List.of("fumeu", "anormal", "cholesterol"));
        when(riskService.assessRisk(3L)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/risk/patient/{patId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patId").value(3))
                .andExpect(jsonPath("$.riskLevel").value("IN_DANGER"))
                .andExpect(jsonPath("$.age").value(22))
                .andExpect(jsonPath("$.triggerCount").value(3))
                .andExpect(jsonPath("$.triggersFound").isArray())
                .andExpect(jsonPath("$.triggersFound.length()").value(3));
    }

    @Test
    @DisplayName("GET /risk/patient/{id} should return 404 when the patient is unknown upstream")
    void shouldReturn404WhenPatientUnknown() throws Exception {
        // Arrange — patient-service a répondu 404, propagé par le RestClient.
        when(riskService.assessRisk(anyLong())).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        HttpHeaders.EMPTY, new byte[0], null));

        // Act & Assert
        mockMvc.perform(get("/risk/patient/{patId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /risk/patient/{id} should return 502 when an upstream service is unreachable")
    void shouldReturn502WhenUpstreamDown() throws Exception {
        // Arrange — notes-service injoignable.
        when(riskService.assessRisk(anyLong()))
                .thenThrow(new ResourceAccessException("connection refused"));

        // Act & Assert
        mockMvc.perform(get("/risk/patient/{patId}", 2L))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
