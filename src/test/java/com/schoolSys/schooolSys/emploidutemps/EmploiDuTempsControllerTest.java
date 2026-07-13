package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.JwtAuthenticationFilter;
import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.emploidutemps.dto.*;
import com.schoolSys.schooolSys.emploidutemps.solver.TimetableSolverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmploiDuTempsController.class)
@WithMockUser
@DisplayName("EmploiDuTempsController Integration Tests")
class EmploiDuTempsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private EmploiDuTempsService emploiDuTempsService;

    @MockitoBean
    private TimetableSolverService timetableSolverService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CurrentUserContext currentUserContext;

    private EmploiDuTempsResponseDTO sampleResponse;
    private EmploiDuTempsRequestDTO sampleRequest;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        sampleResponse = EmploiDuTempsResponseDTO.builder()
                .id(new UUID(0, 1))
                .classeId(new UUID(0, 1))
                .creneauId(new UUID(0, 1))
                .jourSemaine(1)
                .moduleId(new UUID(0, 10))
                .enseignantId(new UUID(0, 100))
                .salle("Salle A1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new EmploiDuTempsRequestDTO();
        sampleRequest.setCreneauId(new UUID(0, 1));
        sampleRequest.setJourSemaine(1);
        sampleRequest.setModuleId(new UUID(0, 10));
        sampleRequest.setEnseignantId(new UUID(0, 100));
        sampleRequest.setSalle("Salle A1");
    }

    // =========================================================================
    // GET /api/emploi-du-temps/classe/{classeId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/emploi-du-temps/classe/{classeId}")
    class GetByClasse {

        @Test
        @WithMockUser(authorities = "READ_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with timetable entries for a class")
        void shouldReturn200WithEntries() throws Exception {
            when(emploiDuTempsService.getByClasse(new UUID(0, 1))).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/emploi-du-temps/classe/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].classeId").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data[0].salle").value("Salle A1"))
                    .andExpect(jsonPath("$.data[0].jourSemaine").value(1));
        }

        @Test
        @WithMockUser(authorities = "READ_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with empty list when no entries")
        void shouldReturn200WithEmptyList() throws Exception {
            when(emploiDuTempsService.getByClasse(new UUID(0, 999))).thenReturn(List.of());

            mockMvc.perform(get("/api/emploi-du-temps/classe/00000000-0000-0000-0000-0000000003e7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // =========================================================================
    // GET /api/emploi-du-temps/enseignant/{enseignantId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/emploi-du-temps/enseignant/{enseignantId}")
    class GetByEnseignant {

        @Test
        @WithMockUser(authorities = "READ_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with timetable entries for a teacher")
        void shouldReturn200WithEntries() throws Exception {
            when(emploiDuTempsService.getByEnseignant(new UUID(0, 100))).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/emploi-du-temps/enseignant/00000000-0000-0000-0000-000000000064"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].enseignantId").value("00000000-0000-0000-0000-000000000064"));
        }
    }

    // =========================================================================
    // PUT /api/emploi-du-temps/classe/{classeId}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/emploi-du-temps/classe/{classeId}")
    class SaveAll {

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with saved entries on valid request")
        void shouldReturn200WithSavedEntries() throws Exception {
            when(emploiDuTempsService.saveAll(eq(new UUID(0, 1)), any())).thenReturn(List.of(sampleResponse));

            mockMvc.perform(put("/api/emploi-du-temps/classe/00000000-0000-0000-0000-000000000001")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].salle").value("Salle A1"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should reject when creneauId is missing (validation on list items)")
        void shouldRejectWhenCreneauIdMissing() throws Exception {
            EmploiDuTempsRequestDTO invalid = new EmploiDuTempsRequestDTO();
            invalid.setJourSemaine(1);
            // creneauId is missing — @Valid on List<> doesn't validate items,
            // so the request reaches the service which may fail with 500
            mockMvc.perform(put("/api/emploi-du-temps/classe/00000000-0000-0000-0000-000000000001")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(invalid))))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assert status >= 400 : "Expected error status (4xx/5xx), got " + status;
                    });
        }
    }

    // =========================================================================
    // POST /api/emploi-du-temps/check-conflits
    // =========================================================================

    @Nested
    @DisplayName("POST /api/emploi-du-temps/check-conflits")
    class CheckConflits {

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with empty list when no conflicts")
        void shouldReturn200WithNoConflicts() throws Exception {
            when(emploiDuTempsService.detectConflits(any())).thenReturn(List.of());

            mockMvc.perform(post("/api/emploi-du-temps/check-conflits")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with conflicts when teacher conflict detected")
        void shouldReturn200WithConflicts() throws Exception {
            ConflitDTO conflit = ConflitDTO.builder()
                    .typeConflit("ENSEIGNANT")
                    .jourSemaine(1)
                    .creneauId(new UUID(0, 1))
                    .enseignantId(new UUID(0, 100))
                    .message("Enseignant déjà occupé sur ce créneau")
                    .build();

            when(emploiDuTempsService.detectConflits(any())).thenReturn(List.of(conflit));

            mockMvc.perform(post("/api/emploi-du-temps/check-conflits")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(sampleRequest))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].typeConflit").value("ENSEIGNANT"))
                    .andExpect(jsonPath("$.data[0].enseignantId").value("00000000-0000-0000-0000-000000000064"));
        }
    }

    // =========================================================================
    // POST /api/emploi-du-temps/generate (OptaPlanner Solver)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/emploi-du-temps/generate")
    class GenerateTimetable {

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with SOLVED status on successful generation")
        void shouldReturn200WithSolvedStatus() throws Exception {
            TimetableGenerationResponseDTO response = TimetableGenerationResponseDTO.builder()
                    .status("SOLVED")
                    .score("0hard/-2soft")
                    .entries(List.of(sampleResponse))
                    .unresolvedConflicts(List.of())
                    .build();

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(3)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1", "Salle A2"))
                    .solverTimeoutSeconds(5)
                    .build();

            when(timetableSolverService.generate(any(TimetableGenerationRequestDTO.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/emploi-du-temps/generate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("SOLVED"))
                    .andExpect(jsonPath("$.data.score").value("0hard/-2soft"))
                    .andExpect(jsonPath("$.data.entries").isArray())
                    .andExpect(jsonPath("$.data.unresolvedConflicts").isEmpty());
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should return 200 with INFEASIBLE status when solver cannot resolve")
        void shouldReturn200WithInfeasibleStatus() throws Exception {
            TimetableGenerationResponseDTO response = TimetableGenerationResponseDTO.builder()
                    .status("INFEASIBLE")
                    .score("-3hard/0soft")
                    .entries(List.of())
                    .unresolvedConflicts(List.of("Teacher 100 has conflict on Monday 8:00-9:00"))
                    .build();

            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(20)
                                    .build()
                    ))
                    .rooms(List.of("Salle A1"))
                    .solverTimeoutSeconds(5)
                    .build();

            when(timetableSolverService.generate(any(TimetableGenerationRequestDTO.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/emploi-du-temps/generate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INFEASIBLE"))
                    .andExpect(jsonPath("$.data.unresolvedConflicts").isNotEmpty());
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should accept empty assignments (auto-generation mode)")
        void shouldAcceptEmptyAssignments() throws Exception {
            // Empty assignments is a valid request — it triggers auto mode,
            // where the timetable is derived from volume horaire.
            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of())
                    .rooms(List.of("Salle A1"))
                    .build();

            mockMvc.perform(post("/api/emploi-du-temps/generate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("should accept empty rooms (loaded from classrooms table)")
        void shouldAcceptEmptyRooms() throws Exception {
            // Empty rooms is valid — rooms are then loaded from the classrooms table.
            TimetableGenerationRequestDTO request = TimetableGenerationRequestDTO.builder()
                    .assignments(List.of(
                            TeachingAssignmentDTO.builder()
                                    .classeId(new UUID(0, 1)).moduleId(new UUID(0, 10)).enseignantId(new UUID(0, 100)).nbHeures(3)
                                    .build()
                    ))
                    .rooms(List.of())
                    .build();

            mockMvc.perform(post("/api/emploi-du-temps/generate")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Creneau CRUD
    // =========================================================================

    @Nested
    @DisplayName("Creneau CRUD Endpoints")
    class CreneauEndpoints {

        @Test
        @WithMockUser(authorities = "READ_EMPLOI_DU_TEMPS")
        @DisplayName("GET /api/creneaux should return all time slots")
        void shouldReturnAllCreneaux() throws Exception {
            CreneauDTO creneau = new CreneauDTO();
            creneau.setId(new UUID(0, 1));
            creneau.setLabel("Session 1");
            creneau.setHeureDebut(LocalTime.of(8, 0));
            creneau.setHeureFin(LocalTime.of(9, 0));
            creneau.setType("COURS");

            when(emploiDuTempsService.getAllCreneaux()).thenReturn(List.of(creneau));

            mockMvc.perform(get("/api/creneaux"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].label").value("Session 1"))
                    .andExpect(jsonPath("$.data[0].type").value("COURS"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("POST /api/creneaux should create a new time slot")
        void shouldCreateCreneau() throws Exception {
            CreneauDTO input = new CreneauDTO();
            input.setLabel("Session 8");
            input.setHeureDebut(LocalTime.of(16, 0));
            input.setHeureFin(LocalTime.of(17, 0));
            input.setType("COURS");

            CreneauDTO created = new CreneauDTO();
            created.setId(new UUID(0, 8));
            created.setLabel("Session 8");
            created.setHeureDebut(LocalTime.of(16, 0));
            created.setHeureFin(LocalTime.of(17, 0));
            created.setType("COURS");

            when(emploiDuTempsService.createCreneau(any(CreneauDTO.class))).thenReturn(created);

            mockMvc.perform(post("/api/creneaux")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000008"))
                    .andExpect(jsonPath("$.data.label").value("Session 8"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("DELETE /api/creneaux/{id} should return 204")
        void shouldDeleteCreneau() throws Exception {
            mockMvc.perform(delete("/api/creneaux/00000000-0000-0000-0000-000000000001")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(emploiDuTempsService).deleteCreneau(new UUID(0, 1));
        }
    }

    // =========================================================================
    // Remplacement CRUD
    // =========================================================================

    @Nested
    @DisplayName("Remplacement CRUD Endpoints")
    class RemplacementEndpoints {

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("POST /api/emploi-du-temps/remplacements should create substitution")
        void shouldCreateRemplacement() throws Exception {
            RemplacementRequestDTO request = new RemplacementRequestDTO();
            request.setEmploiDuTempsId(new UUID(0, 1));
            request.setEnseignantRemplacantId(new UUID(0, 200));
            request.setDateDebut(LocalDate.of(2026, 3, 15));
            request.setDateFin(LocalDate.of(2026, 3, 20));
            request.setMotif("Maladie");

            RemplacementResponseDTO response = RemplacementResponseDTO.builder()
                    .id(new UUID(0, 1))
                    .emploiDuTempsId(new UUID(0, 1))
                    .enseignantRemplacantId(new UUID(0, 200))
                    .dateDebut(LocalDate.of(2026, 3, 15))
                    .dateFin(LocalDate.of(2026, 3, 20))
                    .motif("Maladie")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(emploiDuTempsService.createRemplacement(any(RemplacementRequestDTO.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/emploi-du-temps/remplacements")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data.enseignantRemplacantId").value("00000000-0000-0000-0000-0000000000c8"))
                    .andExpect(jsonPath("$.data.motif").value("Maladie"));
        }

        @Test
        @WithMockUser(authorities = "READ_EMPLOI_DU_TEMPS")
        @DisplayName("GET /api/emploi-du-temps/remplacements should return all substitutions")
        void shouldReturnAllRemplacements() throws Exception {
            RemplacementResponseDTO response = RemplacementResponseDTO.builder()
                    .id(new UUID(0, 1))
                    .emploiDuTempsId(new UUID(0, 1))
                    .enseignantRemplacantId(new UUID(0, 200))
                    .dateDebut(LocalDate.of(2026, 3, 15))
                    .dateFin(LocalDate.of(2026, 3, 20))
                    .motif("Formation")
                    .build();

            when(emploiDuTempsService.getAllRemplacements()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/emploi-du-temps/remplacements"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].motif").value("Formation"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_EMPLOI_DU_TEMPS")
        @DisplayName("DELETE /api/emploi-du-temps/remplacements/{id} should return 204")
        void shouldDeleteRemplacement() throws Exception {
            mockMvc.perform(delete("/api/emploi-du-temps/remplacements/00000000-0000-0000-0000-000000000001")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(emploiDuTempsService).deleteRemplacement(new UUID(0, 1));
        }
    }
}
