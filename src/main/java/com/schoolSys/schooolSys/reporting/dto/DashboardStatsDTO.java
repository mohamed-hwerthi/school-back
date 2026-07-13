package com.schoolSys.schooolSys.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalStudents;
    private long totalTeachers;
    private long totalClasses;

    private BigDecimal totalRevenue;
    private BigDecimal totalPending;
    private BigDecimal tauxRecouvrement;

    private double tauxAbsence;
    private double moyenneGenerale;

    private Map<String, Long> studentsByNiveau;

    private long absencesToday;
    private long newEnrollmentsThisMonth;
    private long eventsThisMonth;

    private List<DayAttendanceDTO> weeklyAttendance;
    private List<UpcomingEventDTO> upcomingEvents;
    private List<RecentStudentDTO> recentStudents;
}
