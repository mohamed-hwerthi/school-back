package com.schoolSys.schooolSys.vitrine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrineAnalyticsDTO {

    private long totalViews;
    private long viewsLast7Days;
    private long viewsLast30Days;
    private List<Map<String, Object>> viewsByPage;
    private List<Map<String, Object>> viewsByDay;
}
