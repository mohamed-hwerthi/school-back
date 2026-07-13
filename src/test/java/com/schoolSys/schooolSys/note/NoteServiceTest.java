package com.schoolSys.schooolSys.note;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.note.dto.NoteRequestDTO;
import com.schoolSys.schooolSys.note.dto.NoteResponseDTO;
import com.schoolSys.schooolSys.parentnotif.ParentNotificationService;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("NoteService Unit Tests")
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private BaremeRepository baremeRepository;

    @Mock
    private CompetenceRepository competenceRepository;

    @Mock
    private EvaluationCompetenceRepository evaluationCompetenceRepository;

    @Mock
    private ParentNotificationService parentNotificationService;

    @Mock
    private AnneeScolaireProvider anneeScolaireProvider;

    @Mock
    private CurrentUserContext currentUser;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private NoteService noteService;

    private Student sampleStudent;
    private Examen sampleExamen;
    private Note sampleNote;
    private Module sampleModule;

    @BeforeEach
    void setUp() {
        sampleStudent = Student.builder()
                .id(new UUID(0, 1))
                .firstName("Ahmed")
                .lastName("Benali")
                .classe("3A")
                .build();

        sampleModule = Module.builder()
                .id(new UUID(0, 1))
                .name("Mathématiques")
                .coeffEtatique(2.0)
                .build();

        sampleExamen = Examen.builder()
                .id(new UUID(0, 1))
                .name("Contrôle 1")
                .coeffEtatique(1.0)
                .dateLimiteSaisie(LocalDate.now().plusDays(7))
                .module(sampleModule)
                .build();

        sampleNote = Note.builder()
                .id(new UUID(0, 1))
                .student(sampleStudent)
                .examen(sampleExamen)
                .trimestre(1)
                .valeur(15.0)
                .observation("Bon travail")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("findByExamen()")
    class FindByExamen {

        @Test
        @DisplayName("should return notes for a given examen and trimestre")
        void shouldReturnNotesForExamen() {
            when(anneeScolaireProvider.resolveAnneeScolaire(anyString())).thenReturn("2024-2025");
            when(noteRepository.findByExamenIdAndTrimestreAndAnneeScolaire(new UUID(0, 1), 1, "2024-2025")).thenReturn(List.of(sampleNote));

            List<NoteResponseDTO> result = noteService.findByExamen(new UUID(0, 1), 1, "2024-2025");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(new UUID(0, 1));
            assertThat(result.get(0).getValeur()).isEqualTo(15.0);
            assertThat(result.get(0).getExamenName()).isEqualTo("Contrôle 1");
        }

        @Test
        @DisplayName("should return empty list when no notes exist")
        void shouldReturnEmptyListWhenNoNotes() {
            when(anneeScolaireProvider.resolveAnneeScolaire(anyString())).thenReturn("2024-2025");
            when(noteRepository.findByExamenIdAndTrimestreAndAnneeScolaire(new UUID(0, 1), 1, "2024-2025")).thenReturn(List.of());

            List<NoteResponseDTO> result = noteService.findByExamen(new UUID(0, 1), 1, "2024-2025");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStudent()")
    class FindByStudent {

        @Test
        @DisplayName("should return notes for a given student and trimestre")
        void shouldReturnNotesForStudent() {
            when(anneeScolaireProvider.resolveAnneeScolaire(anyString())).thenReturn("2024-2025");
            when(noteRepository.findByStudentIdAndTrimestreAndAnneeScolaire(new UUID(0, 1), 1, "2024-2025")).thenReturn(List.of(sampleNote));

            List<NoteResponseDTO> result = noteService.findByStudent(new UUID(0, 1), 1, "2024-2025");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentName()).isEqualTo("Ahmed Benali");
        }
    }

    @Nested
    @DisplayName("upsertBulk()")
    class UpsertBulk {

        @Test
        @DisplayName("should create new note when not existing")
        void shouldCreateNewNote() {
            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 1))
                    .trimestre(1)
                    .valeur(14.5)
                    .observation("Assez bien")
                    .build();

            when(examenRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleExamen));
            when(baremeRepository.findByActifTrue()).thenReturn(Optional.empty());
            when(noteRepository.findByStudentIdAndExamenIdAndTrimestre(new UUID(0, 1), new UUID(0, 1), 1))
                    .thenReturn(Optional.empty());
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);

            List<NoteResponseDTO> result = noteService.upsertBulk(List.of(request));

            assertThat(result).hasSize(1);
            verify(noteRepository).save(any(Note.class));
        }

        @Test
        @DisplayName("should update existing note")
        void shouldUpdateExistingNote() {
            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 1))
                    .trimestre(1)
                    .valeur(16.0)
                    .observation("Amélioré")
                    .build();

            when(examenRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleExamen));
            when(baremeRepository.findByActifTrue()).thenReturn(Optional.empty());
            when(noteRepository.findByStudentIdAndExamenIdAndTrimestre(new UUID(0, 1), new UUID(0, 1), 1))
                    .thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);

            List<NoteResponseDTO> result = noteService.upsertBulk(List.of(request));

            assertThat(result).hasSize(1);
            assertThat(sampleNote.getValeur()).isEqualTo(16.0);
            assertThat(sampleNote.getObservation()).isEqualTo("Amélioré");
        }

        @Test
        @DisplayName("should throw when examen not found")
        void shouldThrowWhenExamenNotFound() {
            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 999))
                    .trimestre(1)
                    .valeur(14.0)
                    .build();

            when(examenRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.upsertBulk(List.of(request)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Examen");
        }

        @Test
        @DisplayName("should reject note after deadline (date limite saisie)")
        void shouldRejectNoteAfterDeadline() {
            sampleExamen.setDateLimiteSaisie(LocalDate.now().minusDays(1));

            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 1))
                    .trimestre(1)
                    .valeur(14.0)
                    .build();

            when(examenRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleExamen));

            assertThatThrownBy(() -> noteService.upsertBulk(List.of(request)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("verrouillée");
        }

        @Test
        @DisplayName("should reject note outside bareme range")
        void shouldRejectNoteOutsideBaremeRange() {
            Bareme bareme = Bareme.builder()
                    .id(new UUID(0, 1))
                    .label("Standard")
                    .noteMin(new BigDecimal("0"))
                    .noteMax(new BigDecimal("20"))
                    .notePassage(new BigDecimal("10"))
                    .actif(true)
                    .build();

            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 1))
                    .trimestre(1)
                    .valeur(25.0) // Out of range
                    .build();

            when(examenRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleExamen));
            when(baremeRepository.findByActifTrue()).thenReturn(Optional.of(bareme));

            assertThatThrownBy(() -> noteService.upsertBulk(List.of(request)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("hors barème");
        }

        @Test
        @DisplayName("should allow note within bareme range")
        void shouldAllowNoteWithinBaremeRange() {
            Bareme bareme = Bareme.builder()
                    .id(new UUID(0, 1))
                    .label("Standard")
                    .noteMin(new BigDecimal("0"))
                    .noteMax(new BigDecimal("20"))
                    .notePassage(new BigDecimal("10"))
                    .actif(true)
                    .build();

            NoteRequestDTO request = NoteRequestDTO.builder()
                    .studentId(new UUID(0, 1))
                    .examenId(new UUID(0, 1))
                    .trimestre(1)
                    .valeur(15.0)
                    .build();

            when(examenRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleExamen));
            when(baremeRepository.findByActifTrue()).thenReturn(Optional.of(bareme));
            when(noteRepository.findByStudentIdAndExamenIdAndTrimestre(new UUID(0, 1), new UUID(0, 1), 1))
                    .thenReturn(Optional.empty());
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);

            List<NoteResponseDTO> result = noteService.upsertBulk(List.of(request));

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete existing note")
        void shouldDeleteExistingNote() {
            when(noteRepository.existsById(new UUID(0, 1))).thenReturn(true);

            noteService.delete(new UUID(0, 1));

            verify(noteRepository).deleteById(new UUID(0, 1));
        }

        @Test
        @DisplayName("should throw when deleting non-existing note")
        void shouldThrowWhenDeletingNonExisting() {
            when(noteRepository.existsById(new UUID(0, 999))).thenReturn(false);

            assertThatThrownBy(() -> noteService.delete(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Note");
        }
    }
}
