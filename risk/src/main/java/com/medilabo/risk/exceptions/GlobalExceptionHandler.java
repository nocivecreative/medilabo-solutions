package com.medilabo.risk.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * Traduit les échecs des appels sortants (patient-service / notes-service) en
 * réponses HTTP propres, plutôt que de laisser fuiter une 500 générique.
 *
 * <p>Format {@link ProblemDetail} (RFC 9457), comme les autres services.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Patient inconnu côté patient-service (404) → 404 côté risque.
     *
     * <p>L'exception n'est pas reçue en paramètre : son message viendrait du
     * service amont et décrirait un appel HTTP interne, sans intérêt pour le
     * client. Le {@code detail} est donc reformulé du point de vue du rapport.
     *
     * @return le corps d'erreur normalisé, en {@code 404 Not Found}
     */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail handleNotFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "Patient introuvable pour le calcul de risque");
    }

    /**
     * Service amont injoignable ou en erreur → 502 Bad Gateway.
     *
     * <p>{@code 502} et non {@code 500} : la défaillance vient d'une dépendance,
     * pas de ce service. Le message ne nomme pas lequel des deux est tombé — le
     * client ne peut rien en faire, et la topologie interne n'a pas à transparaître
     * dans une réponse d'API.
     *
     * @return le corps d'erreur normalisé, en {@code 502 Bad Gateway}
     */
    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleUpstreamFailure() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Un service requis (patient ou notes) est indisponible");
    }
}
