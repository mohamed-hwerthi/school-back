package com.schoolSys.schooolSys.cloture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One pre-closure verification item (ANN-031). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreCheckDTO {

    /** Stable machine code, e.g. {@code ANNEE_OUVERTE}. */
    private String code;

    /** Human-readable label. */
    private String label;

    /** Whether the check passed. */
    private boolean ok;

    /** When {@code true}, a failed check blocks the closure. */
    private boolean blocking;

    /** Explanation shown to the directeur. */
    private String detail;
}
