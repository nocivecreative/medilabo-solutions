package com.medilabo.risk.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.medilabo.risk.config.RiskProperties;
import com.medilabo.risk.dto.NoteView;

/**
 * Test de tranche du client notes (cf. {@link PatientClientTest}) : vérifie
 * l'appel et la désérialisation d'une liste JSON vers {@code List<NoteView>}.
 */
@DisplayName("NoteClient (RestClient slice)")
class NoteClientTest {

    private static final String BASE_URL = "http://notes:8082";

    private MockRestServiceServer server;
    private NoteClient noteClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RiskProperties properties = new RiskProperties();
        properties.setNotesServiceUri(BASE_URL);
        noteClient = new NoteClient(builder, properties);
    }

    @Test
    @DisplayName("Should call /notes/patient/{id} and deserialize the note texts")
    void shouldFetchAndDeserializeNotes() {
        // Arrange
        server.expect(requestTo(BASE_URL + "/notes/patient/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"a1","patId":2,"note":"stress au travail","date":"2024-01-15T10:30:00Z"},
                         {"id":"a2","patId":2,"note":"reaction aux medicaments","date":"2024-04-20T11:00:00Z"}]
                        """, MediaType.APPLICATION_JSON));

        // Act
        var notes = noteClient.getNotes(2L);

        // Assert
        assertThat(notes)
                .extracting(NoteView::note)
                .containsExactly("stress au travail", "reaction aux medicaments");
        server.verify();
    }

    @Test
    @DisplayName("Should return an empty list when the patient has no note")
    void shouldReturnEmptyList() {
        // Arrange
        server.expect(requestTo(BASE_URL + "/notes/patient/9"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        // Act & Assert
        assertThat(noteClient.getNotes(9L)).isEmpty();
    }
}
