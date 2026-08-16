package com.medilabo.patient.controllers;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.exceptions.PatientNotFoundException;
import com.medilabo.patient.services.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API REST des données démographiques du patient, exposée sous {@code /patients}.
 *
 * <p>Service interne : aucun port n'est publié vers l'hôte, les appels arrivent
 * par la gateway, qui porte l'authentification pour toute la stack. Rien n'est
 * donc vérifié ici en matière de droits.
 *
 * <p>Les erreurs ne sont pas gérées méthode par méthode. Les exceptions métier
 * remontent et {@code GlobalExceptionHandler} les traduit au format
 * {@code ProblemDetail} (RFC 9457) : {@code 404} pour un patient inconnu,
 * {@code 400} pour un payload invalide. Les codes documentés ci-dessous
 * proviennent tous de ce mécanisme.
 *
 * <p>Les journaux encadrent chaque appel d'un couple {@code [CALL]}/{@code [RESPONSE]}
 * et ne portent jamais de donnée identifiante : ni nom, ni prénom, ni adresse.
 * Seul l'identifiant technique y figure, suffisant pour relier les deux lignes.
 */
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    /**
     * Liste paginée des patients — {@code GET /patients}.
     *
     * <p>Paramètres de requête : {@code ?page=0&size=20&sort=nom,asc}, résolus par
     * Spring Data. À défaut, la page fait 10 éléments triés par nom croissant.
     *
     * <p>La réponse est un {@link PagedModel} et non une {@code Page} sérialisée
     * directement : le format JSON est alors stable ({@code content} plus un bloc
     * {@code page}), là où la sérialisation d'une {@code Page} expose la structure
     * interne de Spring Data et peut changer d'une version à l'autre.
     *
     * @param pageable page, taille et tri demandés, avec les valeurs par défaut
     *                 ci-dessus si le client ne les précise pas
     * @return {@code 200 OK} et la page demandée, dont le contenu peut être vide
     */
    @GetMapping
    public ResponseEntity<PagedModel<PatientDTO>> getPatients(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC)
            Pageable pageable) {
        logger.info("[CALL] GET /patients - page={} size={} sort=[{}]",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<PatientDTO> page = patientService.getPatients(pageable);
        logger.info("[RESPONSE] GET /patients -> {} patient(s) sur {} au total",
                page.getNumberOfElements(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    /**
     * Récupère un patient — {@code GET /patients/{id}}.
     *
     * @param id identifiant technique du patient
     * @return {@code 200 OK} et le patient demandé
     * @throws PatientNotFoundException si l'identifiant est inconnu — rendue au
     *                                  client en {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        logger.info("[CALL] GET /patients/{}", id);
        PatientDTO patient = patientService.getPatientById(id);
        logger.info("[RESPONSE] GET /patients/{} -> OK", id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Crée un patient — {@code POST /patients}.
     *
     * <p>Répond {@code 201 Created} avec un en-tête {@code Location} pointant sur
     * la ressource créée, construit à partir de l'identifiant généré par la base.
     * Un identifiant présent dans le corps de la requête est ignoré.
     *
     * @param dto données du patient à créer ; validées avant l'entrée dans cette
     *            méthode, une violation produisant un {@code 400 Bad Request}
     *            détaillant les champs en échec
     * @return {@code 201 Created}, le patient créé en corps et son URL en
     *         {@code Location}
     */
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto) {
        // Pas de nom ni prenom dans les logs : donnee identifiante. L'id genere,
        // journalise en reponse, suffit a tracer la creation.
        logger.info("[CALL] POST /patients");
        PatientDTO created = patientService.createPatient(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        logger.info("[RESPONSE] POST /patients -> id={}", created.id());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Met à jour un patient existant — {@code PUT /patients/{id}}.
     *
     * <p>Remplacement complet et non fusion partielle : les champs absents du
     * corps sont écrasés, pas conservés. L'identifiant de l'URL fait foi, celui
     * du corps est ignoré.
     *
     * @param id  identifiant du patient à modifier
     * @param dto nouvelles valeurs ; validées avant l'entrée dans cette méthode,
     *            une violation produisant un {@code 400 Bad Request}
     * @return {@code 200 OK} et le patient tel qu'enregistré
     * @throws PatientNotFoundException si l'identifiant est inconnu — rendue au
     *                                  client en {@code 404 Not Found}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO dto) {
        logger.info("[CALL] PUT /patients/{}", id);
        PatientDTO updated = patientService.updatePatient(id, dto);
        logger.info("[RESPONSE] PUT /patients/{} -> OK", id);
        return ResponseEntity.ok(updated);
    }
}
