package com.medilabo.risk.service;

import com.medilabo.risk.dto.RiskReportDTO;

/**
 * Contrat d'évaluation du risque de diabète d'un patient.
 */
public interface IRiskService {

    /** Croise données démographiques (patient-service) et notes (notes-service). */
    RiskReportDTO assessRisk(Long patId);
}
