package com.schoolSys.schooolSys.conseilclasse;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaire;
import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.bulletin.BulletinService;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.conseilclasse.dto.ConseilClasseDTO;
import com.schoolSys.schooolSys.conseilclasse.dto.PropositionPassageDTO;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.note.Bareme;
import com.schoolSys.schooolSys.note.BaremeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConseilClasseService Unit Tests")
class ConseilClasseServiceTest {

    @Mock private BulletinService bulletinService;
    @Mock private ClasseRepository classeRepository;
    @Mock private NiveauRepository niveauRepository;
    @Mock private BaremeRepository baremeRepository;
    @Mock private AnneeScolaireRepository anneeScolaireRepository;

    @InjectMocks private ConseilClasseService service;

    private Classe classe;

    @BeforeEach
    void setUp() {
        Niveau niveau1 = Niveau.builder().id(new UUID(0, 1)).name("1AP").build();
        classe = Classe.builder().id(new UUID(0, 1)).letter("A").niveau(niveau1).build();
    }

    private BulletinDTO bulletin(UUID studentId, String name, double moyenne) {
        return BulletinDTO.builder()
                .studentId(studentId).studentName(name)
                .classe("1A").niveau("1AP")
                .moyenneGenerale(moyenne)
                .build();
    }

    @Test
    @DisplayName("computes annual averages, ranks and proposes PASSAGE / REDOUBLEMENT")
    void computesAnnualAveragesAndProposals() {
        Niveau niveau1 = classe.getNiveau();
        Niveau niveau2 = Niveau.builder().id(new UUID(0, 2)).name("2AP").build();

        when(classeRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(classe));
        when(niveauRepository.findAll()).thenReturn(List.of(niveau1, niveau2));
        when(baremeRepository.findByActifTrue())
                .thenReturn(Optional.of(Bareme.builder().notePassage(new BigDecimal("10.0")).build()));
        when(anneeScolaireRepository.findByActiveTrue())
                .thenReturn(Optional.of(AnneeScolaire.builder().label("2025-2026").build()));

        when(bulletinService.getBulletins(new UUID(0, 1), 1, "etatique")).thenReturn(List.of(
                bulletin(new UUID(0, 1), "Amine Ziani", 14.0),
                bulletin(new UUID(0, 2), "Sara Bennani", 8.0)));
        when(bulletinService.getBulletins(new UUID(0, 1), 2, "etatique")).thenReturn(List.of(
                bulletin(new UUID(0, 1), "Amine Ziani", 16.0),
                bulletin(new UUID(0, 2), "Sara Bennani", 9.0)));
        when(bulletinService.getBulletins(new UUID(0, 1), 3, "etatique")).thenReturn(List.of(
                bulletin(new UUID(0, 1), "Amine Ziani", 12.0),
                bulletin(new UUID(0, 2), "Sara Bennani", 7.0)));

        ConseilClasseDTO result = service.getConseilClasse(new UUID(0, 1));

        assertThat(result.getClasseId()).isEqualTo(new UUID(0, 1));
        assertThat(result.getClasseNom()).isEqualTo("1A");
        assertThat(result.getNiveauNom()).isEqualTo("1AP");
        assertThat(result.getNiveauSuivant()).isEqualTo("2AP");
        assertThat(result.getSeuilPassage()).isEqualTo(10.0);
        assertThat(result.getAnneeScolaire()).isEqualTo("2025-2026");
        assertThat(result.getPropositions()).hasSize(2);

        // Ranked first: annual average (14 + 16 + 12) / 3 = 14.0 → PASSAGE
        PropositionPassageDTO first = result.getPropositions().get(0);
        assertThat(first.getStudentId()).isEqualTo(new UUID(0, 1));
        assertThat(first.getMoyenneAnnuelle()).isEqualTo(14.0);
        assertThat(first.getRang()).isEqualTo(1);
        assertThat(first.getDecisionProposee()).isEqualTo("PASSAGE");
        assertThat(first.getNiveauSuivant()).isEqualTo("2AP");

        // Ranked second: annual average (8 + 9 + 7) / 3 = 8.0 → REDOUBLEMENT
        PropositionPassageDTO second = result.getPropositions().get(1);
        assertThat(second.getStudentId()).isEqualTo(new UUID(0, 2));
        assertThat(second.getMoyenneAnnuelle()).isEqualTo(8.0);
        assertThat(second.getRang()).isEqualTo(2);
        assertThat(second.getDecisionProposee()).isEqualTo("REDOUBLEMENT");
    }

    @Test
    @DisplayName("annual average uses only the trimestres that have grades")
    void annualAverageIgnoresMissingTrimestres() {
        when(classeRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(classe));
        when(niveauRepository.findAll()).thenReturn(List.of(classe.getNiveau()));
        when(baremeRepository.findByActifTrue()).thenReturn(Optional.empty());
        when(anneeScolaireRepository.findByActiveTrue()).thenReturn(Optional.empty());

        // The student only has grades in the first trimestre.
        when(bulletinService.getBulletins(new UUID(0, 1), 1, "etatique"))
                .thenReturn(List.of(bulletin(new UUID(0, 7), "Yassine Alami", 11.0)));
        when(bulletinService.getBulletins(new UUID(0, 1), 2, "etatique")).thenReturn(List.of());
        when(bulletinService.getBulletins(new UUID(0, 1), 3, "etatique")).thenReturn(List.of());

        ConseilClasseDTO result = service.getConseilClasse(new UUID(0, 1));

        assertThat(result.getSeuilPassage()).isEqualTo(10.0); // default when no active barème
        assertThat(result.getPropositions()).hasSize(1);

        PropositionPassageDTO p = result.getPropositions().get(0);
        assertThat(p.getMoyenneT1()).isEqualTo(11.0);
        assertThat(p.getMoyenneT2()).isNull();
        assertThat(p.getMoyenneT3()).isNull();
        assertThat(p.getMoyenneAnnuelle()).isEqualTo(11.0);
        assertThat(p.getDecisionProposee()).isEqualTo("PASSAGE");
    }

    @Test
    @DisplayName("throws ResourceNotFoundException when the class does not exist")
    void throwsWhenClasseNotFound() {
        when(classeRepository.findById(new UUID(0, 404))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConseilClasse(new UUID(0, 404)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
