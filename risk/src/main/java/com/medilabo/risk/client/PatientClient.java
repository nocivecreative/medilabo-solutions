package com.medilabo.risk.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.medilabo.risk.config.RiskProperties;
import com.medilabo.risk.dto.PatientView;

/**
 * Client HTTP vers patient-service.
 *
 * <p>Appel DIRECT sur le réseau Docker interne (pas via la gateway) : l'auth est
 * centralisée à la gateway, les services métier se font confiance sur le réseau
 * privé. {@link RestClient} est impératif (bloquant), cohérent avec ce service.
 */
@Component
public class PatientClient {

    private final RestClient restClient;

    public PatientClient(RestClient.Builder builder, RiskProperties properties) {
        this.restClient = builder.baseUrl(properties.getPatientServiceUri()).build();
    }

    /** Récupère le patient ; propage un 404 si l'identifiant est inconnu. */
    public PatientView getPatient(Long patId) {
        return restClient.get()
                .uri("/patients/{id}", patId)
                .retrieve()
                .body(PatientView.class);
    }
}
