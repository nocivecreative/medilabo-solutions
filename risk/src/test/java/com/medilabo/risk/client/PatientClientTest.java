package com.medilabo.risk.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.medilabo.risk.config.RiskProperties;
import com.medilabo.risk.dto.PatientView;

/**
 * Test de tranche du client (milieu de pyramide) : la couche HTTP est simulée par
 * {@link MockRestServiceServer}, aucun vrai patient-service requis. Valide l'URL
 * appelée ET la désérialisation JSON (dont {@code LocalDate} et l'ignorance des
 * champs inconnus).
 */
@DisplayName("PatientClient (RestClient slice)")
class PatientClientTest {

    private static final String BASE_URL = "http://patient:8081";

    private MockRestServiceServer server;
    private PatientClient patientClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RiskProperties properties = new RiskProperties();
        properties.setPatientServiceUri(BASE_URL);
        patientClient = new PatientClient(builder, properties);
    }

    @Test
    @DisplayName("Should call /patients/{id} and deserialize age and sex, ignoring unknown fields")
    void shouldFetchAndDeserializePatient() {
        // Arrange — la réponse porte des champs non lus (nom, adresse) à ignorer.
        server.expect(requestTo(BASE_URL + "/patients/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"prenom":"Test","nom":"TestNone","dateNaissance":"1966-12-03",
                         "genre":"F","telephone":"100-222-3333","adresse":"1 Brookside St"}
                        """, MediaType.APPLICATION_JSON));

        // Act
        PatientView patient = patientClient.getPatient(1L);

        // Assert
        assertThat(patient)
                .extracting(PatientView::id, PatientView::dateNaissance, PatientView::genre)
                .containsExactly(1L, LocalDate.of(1966, 12, 3), "F");
        server.verify();
    }

    @Test
    @DisplayName("Should propagate a 404 as HttpClientErrorException.NotFound")
    void shouldPropagateNotFound() {
        // Arrange
        server.expect(requestTo(BASE_URL + "/patients/99"))
                .andRespond(withResourceNotFound());

        // Act & Assert
        assertThatThrownBy(() -> patientClient.getPatient(99L))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
