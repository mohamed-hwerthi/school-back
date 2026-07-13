package com.schoolSys.schooolSys.inscription;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.inscription.dto.CreateInscriptionRequest;
import com.schoolSys.schooolSys.inscription.dto.InscriptionDTO;
import com.schoolSys.schooolSys.inscription.dto.InscriptionStatsDTO;
import com.schoolSys.schooolSys.inscription.dto.UpdateStatutRequest;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InscriptionService Unit Tests")
class InscriptionServiceTest {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private ListeAttenteRepository listeAttenteRepository;

    @Mock
    private NiveauRepository niveauRepository;

    @InjectMocks
    private InscriptionService inscriptionService;

    private CreateInscriptionRequest createRequest;
    private Inscription sampleInscription;

    @BeforeEach
    void setUp() {
        createRequest = CreateInscriptionRequest.builder()
                .nom("Benali")
                .prenom("Ahmed")
                .dateNaissance(LocalDate.of(2015, 9, 15))
                .lieuNaissance("Tunis")
                .sexe("M")
                .adresse("10 Rue de la Liberté")
                .telephoneParent("06123456")
                .emailParent("parent@email.com")
                .nomParent("Benali")
                .prenomParent("Mohamed")
                .niveauId(new UUID(0, 1))
                .anneeScolaire("2025-2026")
                .build();

        sampleInscription = Inscription.builder()
                .id(new UUID(0, 1))
                .nom("Benali")
                .prenom("Ahmed")
                .dateNaissance(LocalDate.of(2015, 9, 15))
                .lieuNaissance("Tunis")
                .sexe("M")
                .adresse("10 Rue de la Liberté")
                .telephoneParent("06123456")
                .emailParent("parent@email.com")
                .nomParent("Benali")
                .prenomParent("Mohamed")
                .niveauId(new UUID(0, 1))
                .anneeScolaire("2025-2026")
                .statut("SOUMISE")
                .numeroDossier("INS-2026-ABC12345")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create inscription with generated numeroDossier")
        void shouldCreateInscriptionWithNumeroDossier() {
            when(inscriptionRepository.save(any(Inscription.class))).thenAnswer(invocation -> {
                Inscription saved = invocation.getArgument(0);
                saved.setId(new UUID(0, 1));
                return saved;
            });

            InscriptionDTO result = inscriptionService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getNom()).isEqualTo("Benali");
            assertThat(result.getPrenom()).isEqualTo("Ahmed");
            assertThat(result.getStatut()).isEqualTo("SOUMISE");

            ArgumentCaptor<Inscription> captor = ArgumentCaptor.forClass(Inscription.class);
            verify(inscriptionRepository).save(captor.capture());

            Inscription saved = captor.getValue();
            assertThat(saved.getNumeroDossier()).isNotBlank();
            assertThat(saved.getNumeroDossier()).startsWith("INS-");
            assertThat(saved.getStatut()).isEqualTo("SOUMISE");
        }

        @Test
        @DisplayName("should set default statut to SOUMISE")
        void shouldSetDefaultStatut() {
            when(inscriptionRepository.save(any(Inscription.class))).thenAnswer(invocation -> {
                Inscription saved = invocation.getArgument(0);
                saved.setId(new UUID(0, 1));
                return saved;
            });

            InscriptionDTO result = inscriptionService.create(createRequest);

            assertThat(result.getStatut()).isEqualTo("SOUMISE");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return inscription when found")
        void shouldReturnInscriptionWhenFound() {
            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));

            InscriptionDTO result = inscriptionService.findById(new UUID(0, 1));

            assertThat(result.getId()).isEqualTo(new UUID(0, 1));
            assertThat(result.getNom()).isEqualTo("Benali");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(inscriptionRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inscriptionService.findById(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inscription");
        }
    }

    @Nested
    @DisplayName("findByNumeroDossier()")
    class FindByNumeroDossier {

        @Test
        @DisplayName("should return inscription by numeroDossier")
        void shouldReturnInscriptionByNumeroDossier() {
            when(inscriptionRepository.findByNumeroDossier("INS-2026-ABC12345"))
                    .thenReturn(Optional.of(sampleInscription));

            InscriptionDTO result = inscriptionService.findByNumeroDossier("INS-2026-ABC12345");

            assertThat(result.getNumeroDossier()).isEqualTo("INS-2026-ABC12345");
        }

        @Test
        @DisplayName("should throw when numeroDossier not found")
        void shouldThrowWhenNumeroDossierNotFound() {
            when(inscriptionRepository.findByNumeroDossier("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> inscriptionService.findByNumeroDossier("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateStatut()")
    class UpdateStatut {

        @Test
        @DisplayName("should update statut correctly")
        void shouldUpdateStatut() {
            UpdateStatutRequest request = new UpdateStatutRequest("ACCEPTEE", "Dossier complet");

            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));
            when(inscriptionRepository.save(any(Inscription.class))).thenReturn(sampleInscription);

            InscriptionDTO result = inscriptionService.updateStatut(new UUID(0, 1), request);

            assertThat(sampleInscription.getStatut()).isEqualTo("ACCEPTEE");
            assertThat(sampleInscription.getCommentaire()).isEqualTo("Dossier complet");
        }

        @Test
        @DisplayName("should add to liste d'attente when statut is LISTE_ATTENTE")
        void shouldAddToListeAttenteWhenStatutIsListeAttente() {
            UpdateStatutRequest request = new UpdateStatutRequest("LISTE_ATTENTE", null);

            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));
            when(inscriptionRepository.save(any(Inscription.class))).thenReturn(sampleInscription);
            when(listeAttenteRepository.findByInscriptionId(new UUID(0, 1))).thenReturn(Optional.empty());
            when(listeAttenteRepository.findMaxPositionByNiveauIdAndAnneeScolaire(new UUID(0, 1), "2025-2026")).thenReturn(0);
            when(listeAttenteRepository.save(any(ListeAttente.class))).thenAnswer(inv -> inv.getArgument(0));

            inscriptionService.updateStatut(new UUID(0, 1), request);

            verify(listeAttenteRepository).save(any(ListeAttente.class));
        }

        @Test
        @DisplayName("should throw when inscription not found")
        void shouldThrowWhenInscriptionNotFound() {
            UpdateStatutRequest request = new UpdateStatutRequest("ACCEPTEE", null);

            when(inscriptionRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inscriptionService.updateStatut(new UUID(0, 999), request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        @DisplayName("should return correct stats for a given annee scolaire")
        void shouldReturnCorrectStatsForAnneeScolaire() {
            String annee = "2025-2026";
            when(inscriptionRepository.countByAnneeScolaireAndStatut(annee, "SOUMISE")).thenReturn(10L);
            when(inscriptionRepository.countByAnneeScolaireAndStatut(annee, "ACCEPTEE")).thenReturn(5L);
            when(inscriptionRepository.countByAnneeScolaireAndStatut(annee, "REFUSEE")).thenReturn(2L);
            when(inscriptionRepository.countByAnneeScolaireAndStatut(annee, "EN_ATTENTE")).thenReturn(3L);
            when(inscriptionRepository.countByAnneeScolaireAndStatut(annee, "LISTE_ATTENTE")).thenReturn(1L);

            InscriptionStatsDTO stats = inscriptionService.getStats(annee);

            assertThat(stats.getTotalSoumises()).isEqualTo(10);
            assertThat(stats.getTotalAcceptees()).isEqualTo(5);
            assertThat(stats.getTotalRefusees()).isEqualTo(2);
            assertThat(stats.getTotalEnAttente()).isEqualTo(3);
            assertThat(stats.getTotalListeAttente()).isEqualTo(1);
            // tauxConversion = 5/21 * 100 ≈ 23.81
            assertThat(stats.getTauxConversion()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return stats across all years when annee is null")
        void shouldReturnStatsAcrossAllYears() {
            when(inscriptionRepository.countByStatut("SOUMISE")).thenReturn(20L);
            when(inscriptionRepository.countByStatut("ACCEPTEE")).thenReturn(15L);
            when(inscriptionRepository.countByStatut("REFUSEE")).thenReturn(5L);
            when(inscriptionRepository.countByStatut("EN_ATTENTE")).thenReturn(0L);
            when(inscriptionRepository.countByStatut("LISTE_ATTENTE")).thenReturn(0L);

            InscriptionStatsDTO stats = inscriptionService.getStats(null);

            assertThat(stats.getTotalSoumises()).isEqualTo(20);
            assertThat(stats.getTotalAcceptees()).isEqualTo(15);
            assertThat(stats.getTotalRefusees()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return zero tauxConversion when no inscriptions")
        void shouldReturnZeroTauxConversionWhenNoInscriptions() {
            when(inscriptionRepository.countByStatut("SOUMISE")).thenReturn(0L);
            when(inscriptionRepository.countByStatut("ACCEPTEE")).thenReturn(0L);
            when(inscriptionRepository.countByStatut("REFUSEE")).thenReturn(0L);
            when(inscriptionRepository.countByStatut("EN_ATTENTE")).thenReturn(0L);
            when(inscriptionRepository.countByStatut("LISTE_ATTENTE")).thenReturn(0L);

            InscriptionStatsDTO stats = inscriptionService.getStats(null);

            assertThat(stats.getTauxConversion()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("addToListeAttente()")
    class AddToListeAttente {

        @Test
        @DisplayName("should add inscription to waiting list")
        void shouldAddToWaitingList() {
            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));
            when(inscriptionRepository.save(any(Inscription.class))).thenReturn(sampleInscription);
            when(listeAttenteRepository.findByInscriptionId(new UUID(0, 1))).thenReturn(Optional.empty());
            when(listeAttenteRepository.findMaxPositionByNiveauIdAndAnneeScolaire(new UUID(0, 1), "2025-2026")).thenReturn(3);
            when(listeAttenteRepository.save(any(ListeAttente.class))).thenAnswer(inv -> inv.getArgument(0));

            inscriptionService.addToListeAttente(new UUID(0, 1));

            assertThat(sampleInscription.getStatut()).isEqualTo("LISTE_ATTENTE");

            ArgumentCaptor<ListeAttente> captor = ArgumentCaptor.forClass(ListeAttente.class);
            verify(listeAttenteRepository).save(captor.capture());

            ListeAttente saved = captor.getValue();
            assertThat(saved.getPosition()).isEqualTo(4); // maxPosition + 1
            assertThat(saved.getNiveauId()).isEqualTo(new UUID(0, 1));
        }

        @Test
        @DisplayName("should throw when inscription has no niveauId")
        void shouldThrowWhenNoNiveauId() {
            sampleInscription.setNiveauId(null);
            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));

            assertThatThrownBy(() -> inscriptionService.addToListeAttente(new UUID(0, 1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("niveau");
        }

        @Test
        @DisplayName("should not duplicate on waiting list")
        void shouldNotDuplicateOnWaitingList() {
            when(inscriptionRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleInscription));
            when(inscriptionRepository.save(any(Inscription.class))).thenReturn(sampleInscription);
            when(listeAttenteRepository.findByInscriptionId(new UUID(0, 1)))
                    .thenReturn(Optional.of(ListeAttente.builder().id(new UUID(0, 1)).build()));

            inscriptionService.addToListeAttente(new UUID(0, 1));

            verify(listeAttenteRepository, never()).save(any(ListeAttente.class));
        }
    }
}
