package com.medilabo.notes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Normalise les erreurs de l'API au format {@link ProblemDetail} (RFC 9457),
 * comme les autres services de la stack.
 *
 * <p>Un seul cas à traiter ici, contrairement au patient-service : aucune
 * exception métier n'est levée, l'API se limitant à lire un historique — qui
 * peut légitimement être vide — et à y ajouter une note.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traduit un payload invalide en {@code 400 Bad Request}.
     *
     * <p>Les violations sont aplaties dans une propriété {@code errors} sous la
     * forme {@code champ : message}, pour que le client puisse afficher l'erreur
     * au bon endroit du formulaire plutôt qu'un message global. Le message vient
     * des contraintes déclarées sur {@code NoteDTO}.
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
