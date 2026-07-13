package com.schoolSys.schooolSys.meeting;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.meeting.dto.MeetingRequestDTO;
import com.schoolSys.schooolSys.meeting.dto.MeetingResponseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;

    public List<MeetingResponseDTO> findAll() {
        return meetingRepository.findAllByOrderByDateAscHeureDebutAsc().stream()
                .map(this::enrichResponse)
                .toList();
    }

    public MeetingResponseDTO findById(UUID id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", id));
        return enrichResponse(meeting);
    }

    public List<MeetingResponseDTO> findByTeacher(UUID enseignantId) {
        if (currentUserContext.hasRole(UserRole.ENSEIGNANT)) {
            UUID ownId = currentUserContext.getCurrentTeacher().map(Teacher::getId).orElse(null);
            if (!Objects.equals(ownId, enseignantId)) {
                throw new AccessDeniedException("Vous ne pouvez consulter que vos propres rendez-vous.");
            }
        }
        return meetingRepository.findByEnseignantIdOrderByDateAscHeureDebutAsc(enseignantId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    public List<MeetingResponseDTO> findByParent(UUID parentId) {
        if (currentUserContext.hasRole(UserRole.PARENT)) {
            UUID ownId = currentUserContext.getUserId().orElse(null);
            if (!Objects.equals(ownId, parentId)) {
                throw new AccessDeniedException("Vous ne pouvez consulter que vos propres rendez-vous.");
            }
        }
        return meetingRepository.findByParentIdOrderByDateAscHeureDebutAsc(parentId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    public List<MeetingResponseDTO> findByStudent(UUID studentId) {
        currentUserContext.assertCanAccessStudent(studentId);
        return meetingRepository.findByStudentIdOrderByDateAscHeureDebutAsc(studentId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    public List<MeetingResponseDTO> findByDateRange(LocalDate start, LocalDate end) {
        return meetingRepository.findByDateBetweenOrderByDateAscHeureDebutAsc(start, end).stream()
                .map(this::enrichResponse)
                .toList();
    }

    public List<MeetingResponseDTO> findByStatut(String statut) {
        return meetingRepository.findByStatutOrderByDateAscHeureDebutAsc(statut).stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Transactional
    public MeetingResponseDTO create(MeetingRequestDTO dto) {
        Meeting meeting = meetingMapper.toEntity(dto);
        if (meeting.getStatut() == null || meeting.getStatut().isBlank()) {
            meeting.setStatut("PLANIFIE");
        }
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting = meetingRepository.save(meeting);
        return enrichResponse(meeting);
    }

    @Transactional
    public MeetingResponseDTO update(UUID id, MeetingRequestDTO dto) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", id));
        meetingMapper.updateEntity(dto, meeting);
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting = meetingRepository.save(meeting);
        return enrichResponse(meeting);
    }

    @Transactional
    public void delete(UUID id) {
        if (!meetingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meeting", id);
        }
        meetingRepository.deleteById(id);
    }

    private MeetingResponseDTO enrichResponse(Meeting meeting) {
        MeetingResponseDTO dto = meetingMapper.toResponse(meeting);

        if (meeting.getEnseignantId() != null) {
            teacherRepository.findById(meeting.getEnseignantId())
                    .ifPresent(t -> dto.setEnseignantName(t.getFirstName() + " " + t.getLastName()));
        }

        if (meeting.getParentId() != null) {
            userRepository.findById(meeting.getParentId())
                    .ifPresent(u -> dto.setParentName(u.getFirstName() + " " + u.getLastName()));
        }

        if (meeting.getStudentId() != null) {
            studentRepository.findById(meeting.getStudentId())
                    .ifPresent(s -> dto.setStudentName(s.getFirstName() + " " + s.getLastName()));
        }

        return dto;
    }
}
