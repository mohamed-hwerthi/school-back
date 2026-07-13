package com.schoolSys.schooolSys.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCommentRequest {
    private String studentName;
    private Double moyenne;
    private List<String> noteDetails;
    private Tone tone;

    public enum Tone {
        ENCOURAGEANT, NEUTRE, STRICT
    }
}
