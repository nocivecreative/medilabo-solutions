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

    /**
     * Récupère l'historique des notes d'un patient.
     *
     * <p>Seul le texte de l'observation est exploité par le calcul de risque, et
     * l'ordre des notes est indifférent : elles sont de toute façon concaténées
     * avant analyse.
     *
     * @param patId identifiant du patient
     * @return les notes du patient ; liste vide s'il n'en a aucune, ce qui n'est
     *         pas une erreur et donne un rapport de niveau {@code NONE}
     * @throws org.springframework.web.client.RestClientException si le
     *         notes-service est injoignable ou répond en erreur
     */
    public List<NoteView> getNotes(Long patId) {
        return restClient.get()
                .uri("/notes/patient/{patId}", patId)
                .retrieve()
                .body(NOTE_LIST);
    }
}
