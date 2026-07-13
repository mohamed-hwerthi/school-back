package com.schoolSys.schooolSys.meeting;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.meeting.dto.MeetingRequestDTO;
import com.schoolSys.schooolSys.meeting.dto.MeetingResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findById(id)));
    }

    @GetMapping("/teacher/{enseignantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getByTeacher(@PathVariable UUID enseignantId) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findByTeacher(enseignantId)));
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getByParent(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findByParent(parentId)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findByStudent(studentId)));
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findByDateRange(start, end)));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.findByStatut(statut)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGE_SYSTEM', 'MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> create(@Valid @RequestBody MeetingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(meetingService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGE_SYSTEM', 'MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> update(@PathVariable UUID id, @Valid @RequestBody MeetingRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(meetingService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGE_SYSTEM', 'MANAGE_COMMUNICATION')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
