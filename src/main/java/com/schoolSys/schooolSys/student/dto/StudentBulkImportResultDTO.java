package com.schoolSys.schooolSys.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of a bulk student import. Unlike a strict transaction, the bulk
 * endpoint processes each row independently: rows that fail are reported
 * without aborting the whole batch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBulkImportResultDTO {

    /** Number of rows successfully persisted. */
    private int created;

    /** Number of rows skipped because they conflict with an existing record (matricule already taken, etc.). */
    private int skipped;

    /** Number of rows rejected for any other reason (validation, DB error). */
    private int failed;

    /** All non-success outcomes, in the original input order. */
    private List<RowError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        /** 1-based row index in the original input list. */
        private int row;

        /** Optional offending field; null when the error is row-wide. */
        private String field;

        /** Human-readable explanation. */
        private String message;

        /** "VALIDATION", "DUPLICATE", or "ERROR". */
        private String code;
    }
}
