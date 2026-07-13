package com.schoolSys.schooolSys.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayAttendanceDTO {
    private String jour;
    private long presents;
    private long absents;
}
