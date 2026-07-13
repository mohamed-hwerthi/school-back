package com.schoolSys.schooolSys.parent;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.dto.AbsenceResponseDTO;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.emploidutemps.dto.EmploiDuTempsResponseDTO;
import com.schoolSys.schooolSys.note.dto.NoteResponseDTO;
import com.schoolSys.schooolSys.parent.dto.AlertsDTO;
import com.schoolSys.schooolSys.parent.dto.ChildCalendarDTO;
import com.schoolSys.schooolSys.parent.dto.ChildDTO;
import com.schoolSys.schooolSys.parent.dto.ChildProfileDTO;
import com.schoolSys.schooolSys.parent.dto.ChildProgressDTO;
import com.schoolSys.schooolSys.parent.dto.TrendDTO;
import com.schoolSys.schooolSys.parent.dto.UpcomingDTO;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent-portal")
@RequiredArgsConstructor
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    @GetMapping("/children")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<List<ChildDTO>>> getChildren(Authentication auth) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(parentPortalService.getChildren(parentUserId)));
    }

    @GetMapping("/children/{studentId}/notes")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<List<NoteResponseDTO>>> getChildNotes(
            Authentication auth,
            @PathVariable UUID studentId,
            @RequestParam(required = false, defaultValue = "1") Integer trimestre) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildNotes(parentUserId, studentId, trimestre)));
    }

    @GetMapping("/children/{studentId}/absences")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<List<AbsenceResponseDTO>>> getChildAbsences(
            Authentication auth,
            @PathVariable UUID studentId) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildAbsences(parentUserId, studentId)));
    }

    @GetMapping("/children/{studentId}/bulletin")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<BulletinDTO>> getChildBulletin(
            Authentication auth,
            @PathVariable UUID studentId,
            @RequestParam UUID classeId,
            @RequestParam(required = false, defaultValue = "1") Integer trimestre) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildBulletin(parentUserId, studentId, classeId, trimestre)));
    }

    @GetMapping("/children/{studentId}/emploi-du-temps")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<List<EmploiDuTempsResponseDTO>>> getChildEmploiDuTemps(
            Authentication auth,
            @PathVariable UUID studentId) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildEmploiDuTemps(parentUserId, studentId)));
    }

    @GetMapping("/children/{studentId}/upcoming")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<UpcomingDTO>> getChildUpcoming(
            Authentication auth,
            @PathVariable UUID studentId,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildUpcoming(parentUserId, studentId, days)));
    }

    @GetMapping("/children/{studentId}/trend")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<TrendDTO>> getChildTrend(
            Authentication auth,
            @PathVariable UUID studentId) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildTrend(parentUserId, studentId)));
    }

    @GetMapping("/children/{studentId}/alerts")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<AlertsDTO>> getChildAlerts(
            Authentication auth,
            @PathVariable UUID studentId) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildAlerts(parentUserId, studentId)));
    }

    @GetMapping("/children/{studentId}/progress")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<ChildProgressDTO>> getChildProgress(
            Authentication auth,
            @PathVariable UUID studentId) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildProgress(parentUserId, studentId)));
    }

    @GetMapping("/children/{studentId}/calendar")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<ChildCalendarDTO>> getChildCalendar(
            Authentication auth,
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildCalendar(parentUserId, studentId, from, to)));
    }

    @GetMapping("/children/{studentId}/full-profile")
    @PreAuthorize("hasAuthority('PARENT_ACCESS')")
    public ResponseEntity<ApiResponse<ChildProfileDTO>> getChildFullProfile(
            Authentication auth,
            @PathVariable UUID studentId,
            @RequestParam(required = false, defaultValue = "1") Integer trimestre) {
        UUID parentUserId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                parentPortalService.getChildFullProfile(parentUserId, studentId, trimestre)));
    }

    private UUID extractUserId(Authentication auth) {
        try {
            return UUID.fromString(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user authentication");
        }
    }
}
