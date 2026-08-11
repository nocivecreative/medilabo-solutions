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

@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskController {

    private static final Logger logger = LoggerFactory.getLogger(RiskController.class);

    private final RiskService riskService;

    /**
     * Rapport de risque de diabète d'un patient (US « Générer un rapport de diabète »).
     * Croise âge/sexe (patient-service) et déclencheurs des notes (notes-service).
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
