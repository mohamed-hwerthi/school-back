package com.schoolSys.schooolSys.teacher;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.teacher.dto.TeacherHomeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints alimentant les cartes du home enseignant.
 * Toutes les requêtes sont scopées à l'enseignant authentifié.
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherHomeController {

    private final TeacherHomeService teacherHomeService;

    /** MOB-FUNC-012 — devoirs avec soumissions non corrigées + quiz à valider. */
    @GetMapping("/pending-corrections")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<TeacherHomeDTO.PendingCorrections>> getPendingCorrections() {
        return ResponseEntity.ok(ApiResponse.ok(teacherHomeService.getPendingCorrections()));
    }

    /** MOB-FUNC-013 — top N élèves à risque (moyenne faible ou absences répétées). */
    @GetMapping("/students-at-risk")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<TeacherHomeDTO.StudentAtRisk>>> getStudentsAtRisk(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(ApiResponse.ok(teacherHomeService.getStudentsAtRisk(limit)));
    }

    /** MOB-FUNC-014 — examens dont les notes ne sont pas saisies. */
    @GetMapping("/pending-tasks")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<TeacherHomeDTO.PendingTasks>> getPendingTasks() {
        return ResponseEntity.ok(ApiResponse.ok(teacherHomeService.getPendingTasks()));
    }
}
