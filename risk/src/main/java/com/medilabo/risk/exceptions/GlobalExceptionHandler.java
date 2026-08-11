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

    /** Patient inconnu côté patient-service (404) → 404 côté risque. */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ProblemDetail handleNotFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "Patient introuvable pour le calcul de risque");
    }

    /** Service amont injoignable ou en erreur → 502 Bad Gateway. */
    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleUpstreamFailure() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Un service requis (patient ou notes) est indisponible");
    }
}
