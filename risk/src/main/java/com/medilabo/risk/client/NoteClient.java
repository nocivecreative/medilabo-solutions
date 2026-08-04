package com.medilabo.risk.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.medilabo.risk.config.RiskProperties;
import com.medilabo.risk.dto.NoteView;

/**
 * Client HTTP vers notes-service (appel direct interne, cf. {@link PatientClient}).
 */
@Component
public class NoteClient {

    private static final ParameterizedTypeReference<List<NoteView>> NOTE_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public NoteClient(RestClient.Builder builder, RiskProperties properties) {
        this.restClient = builder.baseUrl(properties.getNotesServiceUri()).build();
    }

    /** Historique des notes d'un patient (liste vide si aucune note). */
    public List<NoteView> getNotes(Long patId) {
        return restClient.get()
                .uri("/notes/patient/{patId}", patId)
                .retrieve()
                .body(NOTE_LIST);
    }
}
