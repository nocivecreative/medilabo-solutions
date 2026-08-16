package com.medilabo.patient.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.exceptions.PatientNotFoundException;
import com.medilabo.patient.model.Patient;
import com.medilabo.patient.repositories.PatientRepository;

import lombok.RequiredArgsConstructor;

/**
 * Opérations métier sur les patients : consultation, création, mise à jour.
 *
 * <p>C'est la frontière du modèle. Les entités {@link Patient} ne franchissent
 * jamais cette classe vers le haut : tout ce qui entre et sort est un
 * {@link PatientDTO}, converti ici. Les contrôleurs ne manipulent donc aucun
 * objet géré par Hibernate, ce qui rend le contrat d'API indépendant du schéma
 * et évite toute sérialisation hors transaction.
 *
 * <p>Chaque méthode publique porte sa propre transaction, en lecture seule
 * lorsqu'elle ne modifie rien. Comme {@code open-in-view} est désactivé, la
 * connexion est rendue dès la sortie de la méthode et non à la fin du rendu
 * HTTP : le DTO renvoyé est complet et détaché, il n'y a rien à charger
 * paresseusement plus tard.
 *
 * <p>Aucune suppression n'est exposée : le besoin client ne la prévoit pas, et
 * les notes du praticien référencent les patients par identifiant depuis un
 * autre service, sans intégrité référentielle entre les deux bases.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Renvoie une page de patients, convertis en DTO.
     *
     * <p>La pagination est déléguée à la base : {@code findAll(Pageable)} devient
     * un {@code LIMIT}/{@code OFFSET}/{@code ORDER BY} en SQL, donc seule la
     * tranche demandée est chargée en mémoire, quelle que soit la taille de la
     * table — c'est un des leviers Green Code du projet. Une pagination faite en
     * Java après un {@code findAll()} complet aurait le même résultat visible et
     * un coût sans rapport.
     *
     * @param pageable page, taille et tri demandés ; résolu depuis la requête HTTP
     *                 par Spring Data, avec les valeurs par défaut du contrôleur
     * @return la page demandée, éventuellement vide si l'index dépasse le nombre
     *         de patients ; jamais {@code null}
     */
    @Transactional(readOnly = true)
    public Page<PatientDTO> getPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Renvoie un patient par son identifiant.
     *
     * @param id identifiant technique du patient recherché
     * @return le patient correspondant, converti en DTO
     * @throws PatientNotFoundException si aucun patient ne porte cet identifiant ;
     *                                  traduite en {@code 404} par
     *                                  {@code GlobalExceptionHandler}
     */
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        return toDTO(patient);
    }

    /**
     * Crée un patient à partir des données fournies.
     *
     * <p>Un identifiant transmis par le client est ignoré : il n'est pas recopié
     * lors de l'affectation des champs, et c'est la base qui le génère. Le DTO
     * renvoyé porte donc l'identifiant réel, celui que le contrôleur place dans
     * l'en-tête {@code Location}.
     *
     * @param dto données du patient à créer ; déjà validées par {@code @Valid}
     *            en amont, l'identifiant qu'il contient éventuellement est ignoré
     * @return le patient créé, avec son identifiant généré
     */
    @Transactional
    public PatientDTO createPatient(PatientDTO dto) {
        Patient patient = new Patient();
        applyTo(patient, dto);
        return toDTO(patientRepository.save(patient));
    }

    /**
     * Met à jour un patient existant.
     *
     * <p>Remplacement complet, et non fusion partielle : tous les champs
     * modifiables prennent la valeur du DTO, y compris ceux laissés vides. Un
     * client qui n'envoie qu'une partie des champs efface donc les autres — la
     * validation impose de fournir l'ensemble des champs obligatoires.
     *
     * <p>L'identifiant de l'URL fait foi ; celui que porte éventuellement le
     * corps de la requête n'est pas lu, il ne peut donc pas déplacer la mise à
     * jour vers un autre patient.
     *
     * @param id  identifiant du patient à modifier, tel qu'il figure dans l'URL
     * @param dto nouvelles valeurs des champs modifiables
     * @return le patient après mise à jour
     * @throws PatientNotFoundException si aucun patient ne porte cet identifiant ;
     *                                  traduite en {@code 404} par
     *                                  {@code GlobalExceptionHandler}
     */
    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        applyTo(patient, dto);
        return toDTO(patientRepository.save(patient));
    }

    /**
     * Recopie les champs modifiables du DTO vers l'entité — création et mise à jour
     * partagent la même liste, donc ajouter un champ ne se fait qu'à un seul endroit.
     *
     * <p>L'identifiant n'en fait volontairement PAS partie : il est généré par la base
     * à la création et immuable ensuite. Tout id transmis par le client est donc ignoré
     * par construction, sans avoir à le neutraliser après coup.
     */
    private void applyTo(Patient patient, PatientDTO dto) {
        patient.setPrenom(dto.prenom());
        patient.setNom(dto.nom());
        patient.setDateNaissance(dto.dateNaissance());
        patient.setGenre(dto.genre());
        patient.setTelephone(dto.telephone());
        patient.setAdresse(dto.adresse());
    }

    private PatientDTO toDTO(Patient p) {
        return new PatientDTO(p.getId(), p.getPrenom(), p.getNom(), p.getDateNaissance(),
                p.getGenre(), p.getTelephone(), p.getAdresse());
    }
}
