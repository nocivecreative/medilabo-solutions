package com.medilabo.patient.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Normalise les erreurs de l'API au format {@link ProblemDetail} (RFC 9457).
 *
 * <p>Format standard plutot qu'un corps maison : Spring renseigne {@code status},
 * {@code title} et {@code instance} (le chemin en echec) ; il ne reste qu'a fournir
 * le {@code detail}. Les champs specifiques a l'application passent par
 * {@code setProperty}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traduit un patient introuvable en {@code 404 Not Found}.
     *
     * @param ex exception levée par la couche service ; son message, qui ne cite
     *           que l'identifiant cherché, alimente le {@code detail}
     * @return le corps d'erreur normalisé, sérialisé en {@code application/problem+json}
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handleNotFound(PatientNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Traduit un payload invalide en {@code 400 Bad Request}.
     *
     * <p>Les violations sont aplaties dans une propriété {@code errors} sous la
     * forme {@code champ : message}, pour que le client puisse afficher l'erreur
     * au bon endroit du formulaire plutôt qu'un message global. Le message vient
     * des contraintes déclarées sur {@code PatientDTO}.
     *
     * @param ex exception de validation levée par {@code @Valid} avant l'entrée
     *           dans le contrôleur
     * @return le corps d'erreur normalisé, enrichi de la liste des champs en échec
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation échouée");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
                .toList());
        return problem;
    }
}
