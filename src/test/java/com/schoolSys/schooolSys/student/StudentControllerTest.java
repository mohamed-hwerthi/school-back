package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.JwtAuthenticationFilter;
import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@WithMockUser
@DisplayName("StudentController Integration Tests")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private StudentResponseDTO sampleResponse;
    private StudentRequestDTO sampleRequest;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        sampleResponse = StudentResponseDTO.builder()
                .id(new UUID(0, 1))
                .firstName("Ahmed")
                .lastName("Benali")
                .sex("M")
                .classe("3A")
                .niveau("3ème année")
                .status("Actif")
                .matricule("ELV-2026-00001")
                .isBlocked(false)
                .build();

        sampleRequest = new StudentRequestDTO();
        sampleRequest.setFirstName("Ahmed");
        sampleRequest.setLastName("Benali");
        sampleRequest.setSex("M");
        sampleRequest.setClasse("3A");
        sampleRequest.setNiveau("3ème année");
    }

    @Nested
    @DisplayName("GET /api/students")
    class GetAllStudents {

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("should return 200 with paginated student list")
        void shouldReturn200WithStudentList() throws Exception {
            PagedResponse<StudentResponseDTO> pagedResponse = PagedResponse.<StudentResponseDTO>builder()
                    .content(List.of(sampleResponse))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(studentService.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/students")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].firstName").value("Ahmed"))
                    .andExpect(jsonPath("$.data.content[0].matricule").value("ELV-2026-00001"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("should return empty list when no students")
        void shouldReturnEmptyList() throws Exception {
            PagedResponse<StudentResponseDTO> emptyResponse = PagedResponse.<StudentResponseDTO>builder()
                    .content(List.of())
                    .page(0)
                    .size(20)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();

            when(studentService.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(emptyResponse);

            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/students/{id}")
    class GetStudentById {

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("should return 200 with student when found")
        void shouldReturn200WhenFound() throws Exception {
            when(studentService.findById(new UUID(0, 1))).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/students/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data.firstName").value("Ahmed"));
        }

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("should return 404 when student not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(studentService.findById(new UUID(0, 999)))
                    .thenThrow(new ResourceNotFoundException("Student", new UUID(0, 999)));

            mockMvc.perform(get("/api/students/00000000-0000-0000-0000-0000000003e7"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/students")
    class CreateStudent {

        @Test
        @WithMockUser(authorities = "WRITE_STUDENTS")
        @DisplayName("should return 201 with valid request body")
        void shouldReturn201WithValidBody() throws Exception {
            when(studentService.create(any(StudentRequestDTO.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Ahmed"))
                    .andExpect(jsonPath("$.data.matricule").value("ELV-2026-00001"));
        }

        @Test
        @WithMockUser(authorities = "WRITE_STUDENTS")
        @DisplayName("should return 400 with invalid request body (missing required fields)")
        void shouldReturn400WithInvalidBody() throws Exception {
            StudentRequestDTO invalidRequest = new StudentRequestDTO();
            // Missing firstName, lastName, sex - all required

            mockMvc.perform(post("/api/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "WRITE_STUDENTS")
        @DisplayName("should return 400 with invalid sex value")
        void shouldReturn400WithInvalidSex() throws Exception {
            sampleRequest.setSex("X"); // Only M or F allowed

            mockMvc.perform(post("/api/students")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id}")
    class UpdateStudent {

        @Test
        @WithMockUser(authorities = "WRITE_STUDENTS")
        @DisplayName("should return 200 when updating existing student")
        void shouldReturn200WhenUpdating() throws Exception {
            when(studentService.update(eq(new UUID(0, 1)), any(StudentRequestDTO.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/students/00000000-0000-0000-0000-000000000001")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("00000000-0000-0000-0000-000000000001"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id}")
    class DeleteStudent {

        @Test
        @WithMockUser(authorities = "DELETE_STUDENTS")
        @DisplayName("should return 204 when deleting existing student")
        void shouldReturn204WhenDeleting() throws Exception {
            mockMvc.perform(delete("/api/students/00000000-0000-0000-0000-000000000001")
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = "DELETE_STUDENTS")
        @DisplayName("should return 404 when deleting non-existing student")
        void shouldReturn404WhenDeletingNonExisting() throws Exception {
            doThrow(new ResourceNotFoundException("Student", new UUID(0, 999)))
                    .when(studentService).delete(new UUID(0, 999));

            mockMvc.perform(delete("/api/students/00000000-0000-0000-0000-0000000003e7")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
