package com.medilabo.risk.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.risk.dto.RiskReportDTO;
import com.medilabo.risk.service.RiskService;

import lombok.RequiredArgsConstructor;

/**
 * API REST du rapport de risque, exposée sous {@code /risk}.
 *
 * <p>Service interne, non publié vers l'hôte : les appels arrivent par la
 * gateway, qui porte l'authentification pour toute la stack.
 *
 * <p>Le rapport n'est pas stocké : il est recalculé à chaque appel depuis le
 * patient-service et le notes-service. Une indisponibilité de l'un des deux se
 * traduit donc par une erreur, non par un rapport partiel — un niveau de risque
 * calculé sur un historique incomplet serait faux sans le dire.
 *
 * <p>Contrainte de journalisation propre à ce service : ni le niveau de risque
 * ni le nombre de déclencheurs ne doivent apparaître dans les logs. Associés à
 * un identifiant de patient, ce sont des données de santé, et les journaux d'un
 * conteneur n'ont pas le contrôle d'accès de la base.
 */
@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskController {

    private static final Logger logger = LoggerFactory.getLogger(RiskController.class);

    private final RiskService riskService;

    /**
     * Rapport de risque de diabète d'un patient —
     * {@code GET /risk/patient/{patId}}.
     *
     * <p>Sert l'US « Générer un rapport de diabète ». Croise l'âge et le genre
     * (patient-service) avec les termes déclencheurs relevés dans les notes
     * (notes-service).
     *
     * @param patId identifiant du patient à évaluer
     * @return {@code 200 OK} et le rapport calculé. Un patient sans note donne un
     *         rapport valide de niveau {@code NONE}, pas une erreur ; en revanche
     *         un identifiant inconnu du patient-service propage son {@code 404}
     */
    @GetMapping("/patient/{patId}")
    public ResponseEntity<RiskReportDTO> getRisk(@PathVariable Long patId) {
        logger.info("[CALL] GET /risk/patient/{}", patId);
        RiskReportDTO report = riskService.assessRisk(patId);
        // Ne JAMAIS journaliser le niveau de risque ni le nombre de declencheurs :
        // associes a un patId, ce sont des donnees de sante. Les logs conteneur
        // n'ont pas le controle d'acces de la base.
        logger.info("[RESPONSE] GET /risk/patient/{} -> rapport calcule", patId);
        return ResponseEntity.ok(report);
    }
}
