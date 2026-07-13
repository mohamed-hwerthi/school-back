package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private StudentImportRowSaver rowSaver;

    @Mock
    private CurrentUserContext currentUser;

    @Mock
    private ClasseRepository classeRepository;

    @InjectMocks
    private StudentService studentService;

    private Student sampleStudent;
    private StudentRequestDTO sampleRequest;
    private StudentResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(studentService, "entityManager", entityManager);

        // Default to an unrestricted (admin) scope; teacher/parent scoping is covered elsewhere.
        lenient().when(currentUser.hasUnrestrictedAccess()).thenReturn(true);

        sampleStudent = Student.builder()
                .id(new UUID(0, 1))
                .firstName("Ahmed")
                .lastName("Benali")
                .firstNameAr("أحمد")
                .lastNameAr("بن علي")
                .sex("M")
                .dateOfBirth(LocalDate.of(2015, 9, 15))
                .birthPlace("Tunis")
                .address("10 Rue de la Liberté")
                .classe("3A")
                .niveau("3ème année")
                .status("Actif")
                .isBlocked(false)
                .matricule("ELV-2026-00001")
                .parentFirstName("Mohamed")
                .parentLastName("Benali")
                .parentPhone("06123456")
                .parentEmail("parent@email.com")
                .build();

        sampleRequest = new StudentRequestDTO();
        sampleRequest.setFirstName("Ahmed");
        sampleRequest.setLastName("Benali");
        sampleRequest.setSex("M");
        sampleRequest.setClasse("3A");
        sampleRequest.setNiveau("3ème année");

        sampleResponse = StudentResponseDTO.builder()
                .id(new UUID(0, 1))
                .firstName("Ahmed")
                .lastName("Benali")
                .sex("M")
                .classe("3A")
                .niveau("3ème année")
                .status("Actif")
                .matricule("ELV-2026-00001")
                .build();
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all students as DTOs")
        void shouldReturnAllStudents() {
            when(studentRepository.findAll(any(Specification.class))).thenReturn(List.of(sampleStudent));
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            List<StudentResponseDTO> result = studentService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Ahmed");
            assertThat(result.get(0).getLastName()).isEqualTo("Benali");
            verify(studentRepository).findAll(any(Specification.class));
            verify(studentMapper).toResponseDTO(sampleStudent);
        }

        @Test
        @DisplayName("should return empty list when no students exist")
        void shouldReturnEmptyListWhenNoStudents() {
            when(studentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            List<StudentResponseDTO> result = studentService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll(paginated)")
    class FindAllPaginated {

        @Test
        @DisplayName("should return paginated results with filters")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Student> page = new PageImpl<>(List.of(sampleStudent), pageable, 1);

            when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            PagedResponse<StudentResponseDTO> result = studentService.findAll(
                    null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("should return empty page when no matching students")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Student> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            PagedResponse<StudentResponseDTO> result = studentService.findAll(
                    "nonexistent", null, null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return student when found")
        void shouldReturnStudentWhenFound() {
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            StudentResponseDTO result = studentService.findById(new UUID(0, 1));

            assertThat(result.getId()).isEqualTo(new UUID(0, 1));
            assertThat(result.getFirstName()).isEqualTo("Ahmed");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(studentRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.findById(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("00000000-0000-0000-0000-0000000003e7");
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create student with generated matricule")
        void shouldCreateStudentWithMatricule() {
            Query nativeQuery = mock(Query.class);
            when(entityManager.createNativeQuery("SELECT nextval('matricule_seq')")).thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(1L);

            when(studentMapper.toEntity(sampleRequest)).thenReturn(sampleStudent);
            when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            StudentResponseDTO result = studentService.create(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Ahmed");
            verify(studentRepository).save(any(Student.class));
            verify(studentMapper).toEntity(sampleRequest);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update existing student and preserve matricule")
        void shouldUpdateExistingStudent() {
            when(studentRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(sampleStudent));
            when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            StudentResponseDTO result = studentService.update(new UUID(0, 1), sampleRequest);

            assertThat(result).isNotNull();
            verify(studentMapper).updateEntity(eq(sampleRequest), eq(sampleStudent));
            verify(studentRepository).save(sampleStudent);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when updating non-existing student")
        void shouldThrowWhenUpdatingNonExisting() {
            when(studentRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.update(new UUID(0, 999), sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("00000000-0000-0000-0000-0000000003e7");

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete existing student")
        void shouldDeleteExistingStudent() {
            when(studentRepository.existsById(new UUID(0, 1))).thenReturn(true);

            studentService.delete(new UUID(0, 1));

            verify(studentRepository).deleteById(new UUID(0, 1));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deleting non-existing student")
        void shouldThrowWhenDeletingNonExisting() {
            when(studentRepository.existsById(new UUID(0, 999))).thenReturn(false);

            assertThatThrownBy(() -> studentService.delete(new UUID(0, 999)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Student")
                    .hasMessageContaining("00000000-0000-0000-0000-0000000003e7");

            verify(studentRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("importBulk()")
    class ImportBulk {

        @Test
        @DisplayName("should import multiple students with generated matricules")
        void shouldImportMultipleStudents() {
            Query nativeQuery = mock(Query.class);
            when(entityManager.createNativeQuery("SELECT nextval('matricule_seq')")).thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(1L);

            when(studentMapper.toEntity(any(StudentRequestDTO.class))).thenReturn(sampleStudent);
            when(studentRepository.saveAll(anyList())).thenReturn(List.of(sampleStudent));
            when(studentMapper.toResponseDTO(sampleStudent)).thenReturn(sampleResponse);

            List<StudentResponseDTO> result = studentService.importBulk(List.of(sampleRequest));

            assertThat(result).hasSize(1);
            verify(studentRepository).saveAll(anyList());
        }
    }
}
