package com.medilabo.risk.exceptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * Traduit les échecs des appels sortants (patient-service / notes-service) en
 * réponses HTTP propres, plutôt que de laisser fuiter une 500 générique.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Patient inconnu côté patient-service (404) → 404 côté risque. */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(HttpClientErrorException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(HttpStatus.NOT_FOUND, "Patient introuvable pour le calcul de risque"));
    }

    /** Service amont injoignable ou en erreur → 502 Bad Gateway. */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleUpstreamFailure(RestClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(body(HttpStatus.BAD_GATEWAY,
                        "Un service requis (patient ou notes) est indisponible"));
    }

    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
