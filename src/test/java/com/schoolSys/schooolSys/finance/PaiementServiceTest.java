package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.PaiementRequestDTO;
import com.schoolSys.schooolSys.finance.dto.PaiementResponseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaiementService Unit Tests")
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private PaiementMapper paiementMapper;

    @Mock
    private TypeFraisRepository typeFraisRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PaiementService paiementService;

    private Student sampleStudent;
    private TypeFrais sampleTypeFrais;
    private Paiement samplePaiement;
    private PaiementRequestDTO sampleRequest;
    private PaiementResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleStudent = Student.builder()
                .id(new UUID(0, 1))
                .firstName("Ahmed")
                .lastName("Benali")
                .classe("3A")
                .build();

        sampleTypeFrais = TypeFrais.builder()
                .id(new UUID(0, 1))
                .nom("Scolarité")
                .montant(new BigDecimal("500.00"))
                .frequence(TypeFrais.Frequence.MENSUEL)
                .actif(true)
                .build();

        samplePaiement = Paiement.builder()
                .id(new UUID(0, 1))
                .student(sampleStudent)
                .typeFrais(sampleTypeFrais)
                .mois("Janvier")
                .anneeScolaire("2025-2026")
                .montantDu(new BigDecimal("500.00"))
                .montantPaye(new BigDecimal("500.00"))
                .datePaiement(LocalDate.now())
                .modePaiement(Paiement.ModePaiement.ESPECES)
                .statut(Paiement.StatutPaiement.PAYE)
                .reference("PAY-ABC12345")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new PaiementRequestDTO();
        sampleRequest.setStudentId(new UUID(0, 1));
        sampleRequest.setTypeFraisId(new UUID(0, 1));
        sampleRequest.setMois("Janvier");
        sampleRequest.setAnneeScolaire("2025-2026");
        sampleRequest.setMontantDu(new BigDecimal("500.00"));
        sampleRequest.setMontantPaye(new BigDecimal("500.00"));
        sampleRequest.setModePaiement(Paiement.ModePaiement.ESPECES);

        sampleResponse = PaiementResponseDTO.builder()
                .id(new UUID(0, 1))
                .studentId(new UUID(0, 1))
                .studentFirstName("Ahmed")
                .studentLastName("Benali")
                .typeFraisId(new UUID(0, 1))
                .typeFraisNom("Scolarité")
                .mois("Janvier")
                .anneeScolaire("2025-2026")
                .montantDu(new BigDecimal("500.00"))
                .montantPaye(new BigDecimal("500.00"))
                .statut(Paiement.StatutPaiement.PAYE)
                .reference("PAY-ABC12345")
                .build();
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return paiement when found")
        void shouldReturnPaiementWhenFound() {
            when(paiementRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(samplePaiement));
            when(paiementMapper.toResponseDTO(samplePaiement)).thenReturn(sampleResponse);

            PaiementResponseDTO result = paiementService.findById(new UUID(0, 1));

            assertThat(result.getId()).isEqualTo(new UUID(0, 1));
            assertThat(result.getStudentFirstName()).isEqualTo("Ahmed");
            assertThat(result.getMontantDu()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(paiementRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paiementService.findById(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Paiement");
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create paiement with PAYE status when fully paid")
        void shouldCreatePaiementWithPayeStatus() {
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenReturn(samplePaiement);
            when(paiementMapper.toResponseDTO(samplePaiement)).thenReturn(sampleResponse);

            PaiementResponseDTO result = paiementService.create(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStatut()).isEqualTo(Paiement.StatutPaiement.PAYE);

            ArgumentCaptor<Paiement> captor = ArgumentCaptor.forClass(Paiement.class);
            verify(paiementRepository).save(captor.capture());

            Paiement saved = captor.getValue();
            assertThat(saved.getStatut()).isEqualTo(Paiement.StatutPaiement.PAYE);
            assertThat(saved.getReference()).isNotBlank();
        }

        @Test
        @DisplayName("should set PARTIEL status when partially paid")
        void shouldSetPartielStatusWhenPartiallyPaid() {
            sampleRequest.setMontantPaye(new BigDecimal("200.00"));

            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
            when(paiementMapper.toResponseDTO(any(Paiement.class))).thenReturn(sampleResponse);

            paiementService.create(sampleRequest);

            ArgumentCaptor<Paiement> captor = ArgumentCaptor.forClass(Paiement.class);
            verify(paiementRepository).save(captor.capture());
            assertThat(captor.getValue().getStatut()).isEqualTo(Paiement.StatutPaiement.PARTIEL);
        }

        @Test
        @DisplayName("should set EN_ATTENTE status when nothing paid")
        void shouldSetEnAttenteStatusWhenNothingPaid() {
            sampleRequest.setMontantPaye(BigDecimal.ZERO);

            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
            when(paiementMapper.toResponseDTO(any(Paiement.class))).thenReturn(sampleResponse);

            paiementService.create(sampleRequest);

            ArgumentCaptor<Paiement> captor = ArgumentCaptor.forClass(Paiement.class);
            verify(paiementRepository).save(captor.capture());
            assertThat(captor.getValue().getStatut()).isEqualTo(Paiement.StatutPaiement.EN_ATTENTE);
        }

        @Test
        @DisplayName("should throw when student not found")
        void shouldThrowWhenStudentNotFound() {
            when(studentRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());
            sampleRequest.setStudentId(new UUID(0, 999));

            assertThatThrownBy(() -> paiementService.create(sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student");
        }

        @Test
        @DisplayName("should throw when typeFrais not found")
        void shouldThrowWhenTypeFraisNotFound() {
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());
            sampleRequest.setTypeFraisId(new UUID(0, 999));

            assertThatThrownBy(() -> paiementService.create(sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("TypeFrais");
        }

        @Test
        @DisplayName("should generate reference when none provided")
        void shouldGenerateReferenceWhenNoneProvided() {
            sampleRequest.setReference(null);

            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
            when(paiementMapper.toResponseDTO(any(Paiement.class))).thenReturn(sampleResponse);

            paiementService.create(sampleRequest);

            ArgumentCaptor<Paiement> captor = ArgumentCaptor.forClass(Paiement.class);
            verify(paiementRepository).save(captor.capture());
            assertThat(captor.getValue().getReference()).startsWith("PAY-");
        }

        @Test
        @DisplayName("should use provided reference when given")
        void shouldUseProvidedReference() {
            sampleRequest.setReference("CUSTOM-REF-001");

            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenAnswer(inv -> inv.getArgument(0));
            when(paiementMapper.toResponseDTO(any(Paiement.class))).thenReturn(sampleResponse);

            paiementService.create(sampleRequest);

            ArgumentCaptor<Paiement> captor = ArgumentCaptor.forClass(Paiement.class);
            verify(paiementRepository).save(captor.capture());
            assertThat(captor.getValue().getReference()).isEqualTo("CUSTOM-REF-001");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update existing paiement")
        void shouldUpdateExistingPaiement() {
            when(paiementRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(samplePaiement));
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(typeFraisRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleTypeFrais));
            when(paiementRepository.save(any(Paiement.class))).thenReturn(samplePaiement);
            when(paiementMapper.toResponseDTO(samplePaiement)).thenReturn(sampleResponse);

            PaiementResponseDTO result = paiementService.update(new UUID(0, 1), sampleRequest);

            assertThat(result).isNotNull();
            verify(paiementRepository).save(samplePaiement);
        }

        @Test
        @DisplayName("should throw when updating non-existing paiement")
        void shouldThrowWhenUpdatingNonExisting() {
            when(paiementRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paiementService.update(new UUID(0, 999), sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Paiement");
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete existing paiement")
        void shouldDeleteExistingPaiement() {
            when(paiementRepository.existsById(new UUID(0, 1))).thenReturn(true);

            paiementService.delete(new UUID(0, 1));

            verify(paiementRepository).deleteById(new UUID(0, 1));
        }

        @Test
        @DisplayName("should throw when deleting non-existing paiement")
        void shouldThrowWhenDeletingNonExisting() {
            when(paiementRepository.existsById(new UUID(0, 999))).thenReturn(false);

            assertThatThrownBy(() -> paiementService.delete(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Paiement");
        }
    }

    @Nested
    @DisplayName("findByStudentId()")
    class FindByStudentId {

        @Test
        @DisplayName("should return paiements for a given student")
        void shouldReturnPaiementsForStudent() {
            when(paiementRepository.findByStudentId(new UUID(0, 1))).thenReturn(List.of(samplePaiement));
            when(paiementMapper.toResponseDTOList(List.of(samplePaiement))).thenReturn(List.of(sampleResponse));

            List<PaiementResponseDTO> result = paiementService.findByStudentId(new UUID(0, 1));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(new UUID(0, 1));
        }
    }

    @Nested
    @DisplayName("getDashboard()")
    class GetDashboard {

        @Test
        @DisplayName("should calculate dashboard metrics correctly")
        void shouldCalculateDashboardMetrics() {
            String annee = "2025-2026";
            when(paiementRepository.sumMontantPayeByAnneeScolaire(annee)).thenReturn(new BigDecimal("10000.00"));
            when(paiementRepository.sumMontantDuByAnneeScolaire(annee)).thenReturn(new BigDecimal("20000.00"));
            when(paiementRepository.sumImpayes(annee)).thenReturn(new BigDecimal("10000.00"));
            when(paiementRepository.findByAnneeScolaire(annee)).thenReturn(List.of(samplePaiement, samplePaiement));
            when(paiementRepository.countByStatutAndAnneeScolaire(Paiement.StatutPaiement.PAYE, annee)).thenReturn(5L);
            when(paiementRepository.countByStatutAndAnneeScolaire(Paiement.StatutPaiement.EN_RETARD, annee)).thenReturn(3L);
            when(paiementRepository.countByStatutAndAnneeScolaire(Paiement.StatutPaiement.PARTIEL, annee)).thenReturn(2L);
            when(paiementRepository.countByStatutAndAnneeScolaire(Paiement.StatutPaiement.EN_ATTENTE, annee)).thenReturn(1L);

            var dashboard = paiementService.getDashboard(annee);

            assertThat(dashboard.getTotalEncaisse()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(dashboard.getTotalDu()).isEqualByComparingTo(new BigDecimal("20000.00"));
            assertThat(dashboard.getTotalImpayes()).isEqualByComparingTo(new BigDecimal("10000.00"));
            // tauxRecouvrement = 10000/20000 * 100 = 50.00
            assertThat(dashboard.getTauxRecouvrement()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(dashboard.getPaiementsPayes()).isEqualTo(5L);
            assertThat(dashboard.getPaiementsEnRetard()).isEqualTo(3L);
        }
    }
}
