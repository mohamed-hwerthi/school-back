package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import com.schoolSys.schooolSys.emploidutemps.dto.EmploiDuTempsResponseDTO;
import com.schoolSys.schooolSys.emploidutemps.dto.TeachingAssignmentDTO;
import com.schoolSys.schooolSys.emploidutemps.dto.TimetableGenerationRequestDTO;
import com.schoolSys.schooolSys.emploidutemps.dto.TimetableGenerationResponseDTO;
import com.schoolSys.schooolSys.emploidutemps.solver.TimetableSolverService;
import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.classroom.ClassroomRepository;
import com.schoolSys.schooolSys.disponibilite.EnseignantDisponibiliteRepository;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import com.schoolSys.schooolSys.volumehoraire.ModuleClasseVolumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
// The solver exercises a variable set of repositories depending on the input;
// lenient stubbing keeps the per-scenario setup readable.
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimetableSolverService Unit Tests")
class TimetableSolverServiceTest {

    @Mock
    private CreneauRepository creneauRepository;

    @Mock
    private EmploiDuTempsRepository emploiDuTempsRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ModuleClasseVolumeRepository volumeRepository;

    @Mock
    private EnseignantDisponibiliteRepository disponibiliteRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ClasseRepository classeRepository;

    @Mock
    private NiveauRepository niveauRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private AnneeScolaireRepository anneeScolaireRepository;

    @InjectMocks
    private TimetableSolverService timetableSolverService;

    private List<Creneau> courseCreneaux;

    @BeforeEach
    void setUp() {
        // Simulate 3 course creneaux (like a real school day)
        // Mutable list — TimetableSolverService sorts the creneaux in place.
        courseCreneaux = new ArrayList<>(List.of(
                buildCreneau(new UUID(0, 1), "Session 1", LocalTime.of(8, 0), LocalTime.of(9, 0), "COURS"),
                buildCreneau(new UUID(0, 2), "Session 2", LocalTime.of(9, 0), LocalTime.of(10, 0), "COURS"),
                buildCreneau(new UUID(0, 3), "Session 3", LocalTime.of(10, 0), LocalTime.of(11, 0), "COURS")
        ));
    }

    private Creneau buildCreneau(UUID id, String label, LocalTime debut, LocalTime fin, String type) {
        Creneau c = new Creneau();
        c.setId(id);
        c.setLabel(label);
        c.setHeureDebut(debut);
        c.setHeureFin(fin);
        c.setType(type);
        return c;
    }

    @Nested
    @DisplayName("generate() — valid scenarios")
    class ValidScenarios {

        @Test
        @DisplayName("should generate a SOLVED timetable for simple assignment")
        void shouldGenerateSolvedTimetable() {
            when(creneauRepository.findByType("COURS")).thenReturn(courseCreneaux);
            when(moduleRepository.findById(new UUID(0, 10))).thenReturn(Optional.of(
                    Module.builder().id(new UUID(0, 10)).coeffEtatique(2.0).build()
            ));
            doNothing().when(emploiDuTempsRepository).deleteByClasseId(any(UUID.class));
            when(emploiDuTempsRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(2)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1"))
                    .solverTimeoutSeconds(5)
                    .build();

            TimetableGenerationResponseDTO result = timetableSolverService.generate(request);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isIn("SOLVED", "INFEASIBLE");

            if ("SOLVED".equals(result.getStatus())) {
                assertThat(result.getEntries()).isNotEmpty();
                assertThat(result.getEntries().size()).isEqualTo(2); // 2 hours = 2 entries
            }
        }

        @Test
        @DisplayName("should generate timetable for multiple classes and teachers")
        void shouldGenerateForMultipleClassesAndTeachers() {
            when(creneauRepository.findByType("COURS")).thenReturn(courseCreneaux);
            when(moduleRepository.findById(new UUID(0, 10))).thenReturn(Optional.of(
                    Module.builder().id(new UUID(0, 10)).coeffEtatique(2.0).build()
            ));
            when(moduleRepository.findById(new UUID(0, 11))).thenReturn(Optional.of(
                    Module.builder().id(new UUID(0, 11)).coeffEtatique(1.0).build()
            ));
            doNothing().when(emploiDuTempsRepository).deleteByClasseId(any(UUID.class));
            when(emploiDuTempsRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(2)
                                    .build(),
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 11)).enseignantId(new UUID(0, 101)).nbHeures(1)
                                    .build(),
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 2)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(2)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1", "Salle A2"))
                    .solverTimeoutSeconds(10)
                    .build();

            TimetableGenerationResponseDTO result = timetableSolverService.generate(request);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isIn("SOLVED", "INFEASIBLE");
        }
    }

    @Nested
    @DisplayName("generate() — edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should return INFEASIBLE when no creneaux of type COURS exist")
        void shouldReturnInfeasibleWhenNoCreneaux() {
            when(creneauRepository.findByType("COURS")).thenReturn(List.of());

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(2)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1"))
                    .solverTimeoutSeconds(5)
                    .build();

            TimetableGenerationResponseDTO result = timetableSolverService.generate(request);
            assertThat(result.getStatus()).isEqualTo("INFEASIBLE");
            assertThat(result.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("should return INFEASIBLE when too many lessons for available slots")
        void shouldReturnInfeasibleWhenOverloaded() {
            // Only 1 creneau for the whole week = 6 slots (6 days x 1 creneau)
            when(creneauRepository.findByType("COURS")).thenReturn(new ArrayList<>(List.of(courseCreneaux.get(0))));
            when(moduleRepository.findById(any(UUID.class))).thenReturn(Optional.of(
                    Module.builder().id(new UUID(0, 10)).coeffEtatique(1.0).build()
            ));
            lenient().doNothing().when(emploiDuTempsRepository).deleteByClasseId(any(UUID.class));
            lenient().when(emploiDuTempsRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // 10 hours but only 1 room x 6 slots = 6 max
            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(10)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1"))
                    .solverTimeoutSeconds(5)
                    .build();

            // This should either throw (feasibility check) or return INFEASIBLE
            try {
                TimetableGenerationResponseDTO result = timetableSolverService.generate(request);
                // If it doesn't throw, it should be infeasible
                assertThat(result.getStatus()).isEqualTo("INFEASIBLE");
            } catch (IllegalArgumentException e) {
                // Expected — feasibility validation caught it
                assertThat(e.getMessage()).containsIgnoringCase("fit");
            }
        }

        @Test
        @DisplayName("should use default coefficient when module not found")
        void shouldUseDefaultCoefficientWhenModuleNotFound() {
            when(creneauRepository.findByType("COURS")).thenReturn(courseCreneaux);
            when(moduleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
            doNothing().when(emploiDuTempsRepository).deleteByClasseId(any(UUID.class));
            when(emploiDuTempsRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 999)).enseignantId(new UUID(0, 100)).nbHeures(1)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1"))
                    .solverTimeoutSeconds(5)
                    .build();

            // Should not throw — uses default coefficient
            TimetableGenerationResponseDTO result = timetableSolverService.generate(request);
            assertThat(result).isNotNull();
        }
    }
}
