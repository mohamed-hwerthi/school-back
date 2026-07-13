package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.JwtAuthenticationFilter;
import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.FinanceDashboardDTO;
import com.schoolSys.schooolSys.finance.dto.PaiementRequestDTO;
import com.schoolSys.schooolSys.finance.dto.PaiementResponseDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaiementController.class)
@WithMockUser
@DisplayName("PaiementController Integration Tests")
class PaiementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private PaiementService paiementService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private PaiementResponseDTO sampleResponse;
    private PaiementRequestDTO sampleRequest;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

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
                .modePaiement(Paiement.ModePaiement.ESPECES)
                .datePaiement(LocalDate.now())
                .build();

        sampleRequest = new PaiementRequestDTO();
        sampleRequest.setStudentId(new UUID(0, 1));
        sampleRequest.setTypeFraisId(new UUID(0, 1));
        sampleRequest.setMois("Janvier");
        sampleRequest.setAnneeScolaire("2025-2026");
        sampleRequest.setMontantDu(new BigDecimal("500.00"));
        sampleRequest.setMontantPaye(new BigDecimal("500.00"));
        sampleRequest.setModePaiement(Paiement.ModePaiement.ESPECES);
    }

    // =========================================================================
    // GET /api/paiements — Paginated list
    // =========================================================================

    @Nested
    @DisplayName("GET /api/paiements")
    class GetAll {

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return 200 with paginated paiement list")
        void shouldReturn200WithPaginatedList() throws Exception {
            PagedResponse<PaiementResponseDTO> pagedResponse = PagedResponse.<PaiementResponseDTO>builder()
                    .content(List.of(sampleResponse))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(paiementService.findAll(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/paiements")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].studentFirstName").value("Ahmed"))
                    .andExpect(jsonPath("$.data.content[0].reference").value("PAY-ABC12345"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should support filtering by anneeScolaire and statut")
        void shouldSupportFiltering() throws Exception {
            PagedResponse<PaiementResponseDTO> pagedResponse = PagedResponse.<PaiementResponseDTO>builder()
                    .content(List.of(sampleResponse))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(paiementService.findAll(any(), eq("2025-2026"), any(), eq(Paiement.StatutPaiement.PAYE), any(), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/paiements")
                            .param("anneeScolaire", "2025-2026")
                            .param("statut", "PAYE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].statut").value("PAYE"));
        }

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return empty list when no paiements match")
        void shouldReturnEmptyList() throws Exception {
            PagedResponse<PaiementResponseDTO> emptyResponse = PagedResponse.<PaiementResponseDTO>builder()
                    .content(List.of())
                    .page(0)
                    .size(20)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();

            when(paiementService.findAll(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(emptyResponse);

            mockMvc.perform(get("/api/paiements"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    // =========================================================================
    // GET /api/paiements/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/paiements/{id}")
    class GetById {

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return 200 with paiement when found")
        void shouldReturn200WhenFound() throws Exception {
            when(paiementService.findById(new UUID(0, 1))).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/paiements/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data.studentFirstName").value("Ahmed"))
                    .andExpect(jsonPath("$.data.montantDu").value(500.00));
        }

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return 404 when paiement not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(paiementService.findById(new UUID(0, 999)))
                    .thenThrow(new ResourceNotFoundException("Paiement", new UUID(0, 999)));

            mockMvc.perform(get("/api/paiements/00000000-0000-0000-0000-0000000003e7"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/paiements/eleve/{studentId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/paiements/eleve/{studentId}")
    class GetByStudentId {

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return paiements for a student")
        void shouldReturnPaiementsForStudent() throws Exception {
            when(paiementService.findByStudentId(new UUID(0, 1))).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/paiements/eleve/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].studentId").value("00000000-0000-0000-0000-000000000001"));
        }

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should filter by anneeScolaire when provided")
        void shouldFilterByAnneeScolaire() throws Exception {
            when(paiementService.findByStudentIdAndAnneeScolaire(new UUID(0, 1), "2025-2026"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/paiements/eleve/00000000-0000-0000-0000-000000000001")
                            .param("anneeScolaire", "2025-2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].anneeScolaire").value("2025-2026"));
        }
    }

    // =========================================================================
    // POST /api/paiements
    // =========================================================================

    @Nested
    @DisplayName("POST /api/paiements")
    class Create {

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 201 with created paiement on valid request")
        void shouldReturn201OnValidRequest() throws Exception {
            when(paiementService.create(any(PaiementRequestDTO.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/paiements")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.reference").value("PAY-ABC12345"))
                    .andExpect(jsonPath("$.data.statut").value("PAYE"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 400 when studentId is missing")
        void shouldReturn400WhenStudentIdMissing() throws Exception {
            PaiementRequestDTO invalid = new PaiementRequestDTO();
            invalid.setTypeFraisId(new UUID(0, 1));
            invalid.setMois("Janvier");
            invalid.setAnneeScolaire("2025-2026");
            invalid.setMontantDu(new BigDecimal("500.00"));
            invalid.setMontantPaye(new BigDecimal("500.00"));
            // studentId is null

            mockMvc.perform(post("/api/paiements")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 400 when mois is blank")
        void shouldReturn400WhenMoisBlank() throws Exception {
            PaiementRequestDTO invalid = new PaiementRequestDTO();
            invalid.setStudentId(new UUID(0, 1));
            invalid.setTypeFraisId(new UUID(0, 1));
            invalid.setMois(""); // blank
            invalid.setAnneeScolaire("2025-2026");
            invalid.setMontantDu(new BigDecimal("500.00"));
            invalid.setMontantPaye(new BigDecimal("500.00"));

            mockMvc.perform(post("/api/paiements")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 400 when montantDu is negative")
        void shouldReturn400WhenMontantNegative() throws Exception {
            sampleRequest.setMontantDu(new BigDecimal("-100.00"));

            mockMvc.perform(post("/api/paiements")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // PUT /api/paiements/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/paiements/{id}")
    class Update {

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 200 when updating existing paiement")
        void shouldReturn200OnUpdate() throws Exception {
            when(paiementService.update(eq(new UUID(0, 1)), any(PaiementRequestDTO.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/paiements/00000000-0000-0000-0000-000000000001")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 404 when updating non-existing paiement")
        void shouldReturn404WhenNotFound() throws Exception {
            when(paiementService.update(eq(new UUID(0, 999)), any(PaiementRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Paiement", new UUID(0, 999)));

            mockMvc.perform(put("/api/paiements/00000000-0000-0000-0000-0000000003e7")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // DELETE /api/paiements/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/paiements/{id}")
    class Delete {

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 204 when deleting existing paiement")
        void shouldReturn204OnDelete() throws Exception {
            mockMvc.perform(delete("/api/paiements/00000000-0000-0000-0000-000000000001")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(paiementService).delete(new UUID(0, 1));
        }

        @Test
        @WithMockUser(authorities = "WRITE_FINANCE")
        @DisplayName("should return 404 when deleting non-existing paiement")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Paiement", new UUID(0, 999)))
                    .when(paiementService).delete(new UUID(0, 999));

            mockMvc.perform(delete("/api/paiements/00000000-0000-0000-0000-0000000003e7")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/paiements/dashboard
    // =========================================================================

    @Nested
    @DisplayName("GET /api/paiements/dashboard")
    class Dashboard {

        @Test
        @WithMockUser(authorities = "READ_FINANCE")
        @DisplayName("should return 200 with dashboard metrics")
        void shouldReturn200WithDashboardMetrics() throws Exception {
            FinanceDashboardDTO dashboard = FinanceDashboardDTO.builder()
                    .totalEncaisse(new BigDecimal("50000.00"))
                    .totalDu(new BigDecimal("100000.00"))
                    .totalImpayes(new BigDecimal("50000.00"))
                    .tauxRecouvrement(new BigDecimal("50.00"))
                    .totalPaiements(100)
                    .paiementsPayes(50)
                    .paiementsEnRetard(20)
                    .paiementsPartiels(15)
                    .paiementsEnAttente(15)
                    .build();

            when(paiementService.getDashboard("2025-2026")).thenReturn(dashboard);

            mockMvc.perform(get("/api/paiements/dashboard")
                            .param("anneeScolaire", "2025-2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalEncaisse").value(50000.00))
                    .andExpect(jsonPath("$.data.totalDu").value(100000.00))
                    .andExpect(jsonPath("$.data.tauxRecouvrement").value(50.00))
                    .andExpect(jsonPath("$.data.paiementsPayes").value(50))
                    .andExpect(jsonPath("$.data.paiementsEnRetard").value(20));
        }
    }
}
