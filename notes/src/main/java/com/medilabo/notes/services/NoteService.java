package com.medilabo.notes.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.model.Note;
import com.medilabo.notes.repositories.NoteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Opérations métier sur les notes d'observation du praticien.
 *
 * <p>Comme dans le patient-service, les documents {@link Note} ne franchissent
 * pas cette classe vers le haut : les contrôleurs ne voient que des
 * {@link NoteDTO}, convertis ici.
 *
 * <p>Le service est délibérément réduit à la consultation et à l'ajout. Ni
 * modification ni suppression ne sont exposées : une observation médicale
 * horodatée est un fait, l'historique s'enrichit mais ne se réécrit pas.
 *
 * <p>Aucune donnée démographique n'est stockée ni recopiée ici — les notes ne
 * connaissent du patient que son identifiant, détenu par le patient-service.
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * Renvoie l'historique des notes d'un patient, de la plus récente à la plus
     * ancienne.
     *
     * <p>L'ordre fait partie du contrat, pas du hasard : c'est celui qu'attend la
     * vue historique, et il est produit par la base plutôt que par un tri en
     * mémoire après lecture.
     *
     * @param patId identifiant du patient dont on veut l'historique
     * @return les notes du patient, triées par date décroissante ; liste vide si
     *         le patient n'a aucune note ou n'existe pas — l'existence du patient
     *         n'est pas vérifiée ici, elle relève du patient-service
     */
    public List<NoteDTO> getNotesByPatId(Long patId) {
        return noteRepository.findByPatIdOrderByDateDesc(patId).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Ajoute une note à l'historique d'un patient.
     *
     * <p>Seuls le patient visé et le texte de l'observation sont repris du DTO.
     * L'identifiant et l'horodatage sont maîtrisés par le serveur : un client ne
     * peut ni imposer un identifiant, ni antidater une observation. Ce que le DTO
     * contiendrait dans ces deux champs est ignoré sans erreur.
     *
     * @param dto note à enregistrer ; seuls {@code patId} et {@code note} sont
     *            lus, et ils ont été validés avant d'arriver ici
     * @return la note enregistrée, avec l'identifiant attribué par MongoDB et la
     *         date posée à l'instant de l'écriture
     */
    public NoteDTO addNote(NoteDTO dto) {
        Note note = Note.builder()
                .patId(dto.patId())
                .note(dto.note())
                .date(Instant.now())
                .build();
        return toDTO(noteRepository.save(note));
    }

    private NoteDTO toDTO(Note n) {
        return new NoteDTO(n.getId(), n.getPatId(), n.getNote(), n.getDate());
    }
}
