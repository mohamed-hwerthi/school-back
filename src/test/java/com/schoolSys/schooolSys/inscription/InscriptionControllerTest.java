package com.schoolSys.schooolSys.inscription;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.JwtAuthenticationFilter;
import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.inscription.dto.*;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({InscriptionController.class, InscriptionPublicController.class})
@WithMockUser
@DisplayName("Inscription Controller Integration Tests")
class InscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private InscriptionService inscriptionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private InscriptionDTO sampleDTO;
    private CreateInscriptionRequest createRequest;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        sampleDTO = InscriptionDTO.builder()
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
                .niveauNom("3ème année")
                .anneeScolaire("2025-2026")
                .statut("SOUMISE")
                .numeroDossier("INS-2026-ABC12345")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

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
    }

    // =========================================================================
    // Public Endpoints — No authentication required
    // =========================================================================

    @Nested
    @DisplayName("Public Inscription Endpoints (no auth)")
    class PublicEndpoints {

        @Test
        @DisplayName("POST /api/public/inscriptions should create inscription without auth")
        void shouldCreateInscriptionWithoutAuth() throws Exception {
            when(inscriptionService.create(any(CreateInscriptionRequest.class))).thenReturn(sampleDTO);

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nom").value("Benali"))
                    .andExpect(jsonPath("$.data.prenom").value("Ahmed"))
                    .andExpect(jsonPath("$.data.statut").value("SOUMISE"))
                    .andExpect(jsonPath("$.data.numeroDossier").value("INS-2026-ABC12345"));
        }

        @Test
        @DisplayName("POST /api/public/inscriptions should return 400 when nom is missing")
        void shouldReturn400WhenNomMissing() throws Exception {
            CreateInscriptionRequest invalid = CreateInscriptionRequest.builder()
                    .prenom("Ahmed")
                    .dateNaissance(LocalDate.of(2015, 9, 15))
                    .anneeScolaire("2025-2026")
                    // nom is missing
                    .build();

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/public/inscriptions should return 400 when anneeScolaire is missing")
        void shouldReturn400WhenAnneeScolaireMissing() throws Exception {
            CreateInscriptionRequest invalid = CreateInscriptionRequest.builder()
                    .nom("Benali")
                    .prenom("Ahmed")
                    .dateNaissance(LocalDate.of(2015, 9, 15))
                    // anneeScolaire is missing
                    .build();

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/public/inscriptions should return 400 on invalid email")
        void shouldReturn400OnInvalidEmail() throws Exception {
            CreateInscriptionRequest invalidEmail = CreateInscriptionRequest.builder()
                    .nom("Benali")
                    .prenom("Ahmed")
                    .dateNaissance(LocalDate.of(2015, 9, 15))
                    .emailParent("not-an-email")
                    .anneeScolaire("2025-2026")
                    .build();

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidEmail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/public/inscriptions/numero/{numeroDossier} should return inscription")
        void shouldReturnInscriptionByNumeroDossier() throws Exception {
            when(inscriptionService.findByNumeroDossier("INS-2026-ABC12345")).thenReturn(sampleDTO);

            mockMvc.perform(get("/api/public/inscriptions/numero/INS-2026-ABC12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.numeroDossier").value("INS-2026-ABC12345"))
                    .andExpect(jsonPath("$.data.statut").value("SOUMISE"));
        }

        @Test
        @DisplayName("GET /api/public/inscriptions/numero/{numeroDossier} should return 404 when not found")
        void shouldReturn404WhenNumeroDossierNotFound() throws Exception {
            when(inscriptionService.findByNumeroDossier("INVALID"))
                    .thenThrow(new ResourceNotFoundException("Inscription not found"));

            mockMvc.perform(get("/api/public/inscriptions/numero/INVALID"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // Protected Admin Endpoints
    // =========================================================================

    @Nested
    @DisplayName("GET /api/inscriptions — Admin list")
    class GetAll {

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return 200 with paginated inscription list")
        void shouldReturn200WithPaginatedList() throws Exception {
            PagedResponse<InscriptionDTO> pagedResponse = PagedResponse.<InscriptionDTO>builder()
                    .content(List.of(sampleDTO))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(inscriptionService.findAll(any(), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/inscriptions")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].nom").value("Benali"))
                    .andExpect(jsonPath("$.data.content[0].statut").value("SOUMISE"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should support filtering by statut and anneeScolaire")
        void shouldSupportFiltering() throws Exception {
            PagedResponse<InscriptionDTO> pagedResponse = PagedResponse.<InscriptionDTO>builder()
                    .content(List.of(sampleDTO))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(inscriptionService.findAll(eq("SOUMISE"), eq("2025-2026"), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/inscriptions")
                            .param("statut", "SOUMISE")
                            .param("anneeScolaire", "2025-2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].statut").value("SOUMISE"));
        }
    }

    @Nested
    @DisplayName("GET /api/inscriptions/{id}")
    class GetById {

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return 200 with inscription when found")
        void shouldReturn200WhenFound() throws Exception {
            when(inscriptionService.findById(new UUID(0, 1))).thenReturn(sampleDTO);

            mockMvc.perform(get("/api/inscriptions/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data.nom").value("Benali"))
                    .andExpect(jsonPath("$.data.numeroDossier").value("INS-2026-ABC12345"));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(inscriptionService.findById(new UUID(0, 999)))
                    .thenThrow(new ResourceNotFoundException("Inscription", new UUID(0, 999)));

            mockMvc.perform(get("/api/inscriptions/00000000-0000-0000-0000-0000000003e7"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PUT /api/inscriptions/{id}/statut — Status update workflow
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/inscriptions/{id}/statut")
    class UpdateStatut {

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should update inscription to ACCEPTEE")
        void shouldUpdateToAcceptee() throws Exception {
            InscriptionDTO accepted = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("ACCEPTEE")
                    .commentaire("Dossier complet")
                    .numeroDossier("INS-2026-ABC12345")
                    .build();

            UpdateStatutRequest request = new UpdateStatutRequest("ACCEPTEE", "Dossier complet");

            when(inscriptionService.updateStatut(eq(new UUID(0, 1)), any(UpdateStatutRequest.class)))
                    .thenReturn(accepted);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("ACCEPTEE"))
                    .andExpect(jsonPath("$.data.commentaire").value("Dossier complet"));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should update inscription to REFUSEE")
        void shouldUpdateToRefusee() throws Exception {
            InscriptionDTO refused = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("REFUSEE")
                    .commentaire("Places épuisées")
                    .build();

            UpdateStatutRequest request = new UpdateStatutRequest("REFUSEE", "Places épuisées");

            when(inscriptionService.updateStatut(eq(new UUID(0, 1)), any(UpdateStatutRequest.class)))
                    .thenReturn(refused);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("REFUSEE"));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should update inscription to LISTE_ATTENTE")
        void shouldUpdateToListeAttente() throws Exception {
            InscriptionDTO waitlisted = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("LISTE_ATTENTE")
                    .build();

            UpdateStatutRequest request = new UpdateStatutRequest("LISTE_ATTENTE", null);

            when(inscriptionService.updateStatut(eq(new UUID(0, 1)), any(UpdateStatutRequest.class)))
                    .thenReturn(waitlisted);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("LISTE_ATTENTE"));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return 400 when statut is blank")
        void shouldReturn400WhenStatutBlank() throws Exception {
            UpdateStatutRequest invalid = new UpdateStatutRequest("", null);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return 404 when inscription not found")
        void shouldReturn404WhenNotFound() throws Exception {
            UpdateStatutRequest request = new UpdateStatutRequest("ACCEPTEE", null);

            when(inscriptionService.updateStatut(eq(new UUID(0, 999)), any(UpdateStatutRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Inscription", new UUID(0, 999)));

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-0000000003e7/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/inscriptions/stats
    // =========================================================================

    @Nested
    @DisplayName("GET /api/inscriptions/stats")
    class Stats {

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return inscription statistics")
        void shouldReturnStats() throws Exception {
            InscriptionStatsDTO stats = InscriptionStatsDTO.builder()
                    .totalSoumises(25)
                    .totalAcceptees(15)
                    .totalRefusees(5)
                    .totalEnAttente(3)
                    .totalListeAttente(2)
                    .tauxConversion(30.0)
                    .build();

            when(inscriptionService.getStats("2025-2026")).thenReturn(stats);

            mockMvc.perform(get("/api/inscriptions/stats")
                            .param("anneeScolaire", "2025-2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalSoumises").value(25))
                    .andExpect(jsonPath("$.data.totalAcceptees").value(15))
                    .andExpect(jsonPath("$.data.totalRefusees").value(5))
                    .andExpect(jsonPath("$.data.totalEnAttente").value(3))
                    .andExpect(jsonPath("$.data.totalListeAttente").value(2))
                    .andExpect(jsonPath("$.data.tauxConversion").value(30.0));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return stats across all years when no anneeScolaire param")
        void shouldReturnStatsForAllYears() throws Exception {
            InscriptionStatsDTO stats = InscriptionStatsDTO.builder()
                    .totalSoumises(50)
                    .totalAcceptees(30)
                    .totalRefusees(10)
                    .totalEnAttente(5)
                    .totalListeAttente(5)
                    .tauxConversion(30.0)
                    .build();

            when(inscriptionService.getStats(isNull())).thenReturn(stats);

            mockMvc.perform(get("/api/inscriptions/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalSoumises").value(50));
        }
    }

    // =========================================================================
    // GET /api/inscriptions/liste-attente/{niveauId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/inscriptions/liste-attente/{niveauId}")
    class ListeAttente {

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return waiting list for a niveau")
        void shouldReturnWaitingList() throws Exception {
            InscriptionDTO waitlisted = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("LISTE_ATTENTE")
                    .niveauId(new UUID(0, 1))
                    .niveauNom("3ème année")
                    .build();

            when(inscriptionService.getListeAttente(eq(new UUID(0, 1)), any())).thenReturn(List.of(waitlisted));

            mockMvc.perform(get("/api/inscriptions/liste-attente/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].statut").value("LISTE_ATTENTE"))
                    .andExpect(jsonPath("$.data[0].niveauNom").value("3ème année"));
        }

        @Test
        @WithMockUser(authorities = "MANAGE_INSCRIPTIONS")
        @DisplayName("should return empty list when no one is on waiting list")
        void shouldReturnEmptyWaitingList() throws Exception {
            when(inscriptionService.getListeAttente(eq(new UUID(0, 1)), any())).thenReturn(List.of());

            mockMvc.perform(get("/api/inscriptions/liste-attente/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // =========================================================================
    // Full Enrollment Workflow
    // =========================================================================

    @Nested
    @DisplayName("Full Enrollment Workflow")
    class EnrollmentWorkflow {

        @Test
        @DisplayName("complete flow: submit → review → accept inscription")
        void completeEnrollmentFlow() throws Exception {
            // Step 1: Public submission
            when(inscriptionService.create(any(CreateInscriptionRequest.class))).thenReturn(sampleDTO);

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.statut").value("SOUMISE"))
                    .andExpect(jsonPath("$.data.numeroDossier").exists());

            // Step 2: Parent checks status
            when(inscriptionService.findByNumeroDossier("INS-2026-ABC12345")).thenReturn(sampleDTO);

            mockMvc.perform(get("/api/public/inscriptions/numero/INS-2026-ABC12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("SOUMISE"));

            // Step 3: Admin reviews and accepts
            InscriptionDTO accepted = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("ACCEPTEE")
                    .commentaire("Dossier complet, inscription validée")
                    .numeroDossier("INS-2026-ABC12345")
                    .build();

            when(inscriptionService.updateStatut(eq(new UUID(0, 1)), any(UpdateStatutRequest.class)))
                    .thenReturn(accepted);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UpdateStatutRequest("ACCEPTEE", "Dossier complet, inscription validée"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("ACCEPTEE"));
        }

        @Test
        @DisplayName("waitlist flow: submit → waitlist → parent checks")
        void waitlistFlow() throws Exception {
            // Step 1: Submission
            when(inscriptionService.create(any(CreateInscriptionRequest.class))).thenReturn(sampleDTO);

            mockMvc.perform(post("/api/public/inscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());

            // Step 2: Admin puts on waiting list
            InscriptionDTO waitlisted = InscriptionDTO.builder()
                    .id(new UUID(0, 1))
                    .nom("Benali")
                    .prenom("Ahmed")
                    .statut("LISTE_ATTENTE")
                    .numeroDossier("INS-2026-ABC12345")
                    .build();

            when(inscriptionService.updateStatut(eq(new UUID(0, 1)), any(UpdateStatutRequest.class)))
                    .thenReturn(waitlisted);

            mockMvc.perform(put("/api/inscriptions/00000000-0000-0000-0000-000000000001/statut")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UpdateStatutRequest("LISTE_ATTENTE", null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("LISTE_ATTENTE"));

            // Step 3: Parent checks — sees waitlisted status
            when(inscriptionService.findByNumeroDossier("INS-2026-ABC12345")).thenReturn(waitlisted);

            mockMvc.perform(get("/api/public/inscriptions/numero/INS-2026-ABC12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.statut").value("LISTE_ATTENTE"));
        }
    }
}
