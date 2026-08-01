package com.schoolSys.schooolSys.sms;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_sms_credits", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSmsCredit {

    @Id
    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Column(name = "total_credits", nullable = false)
    @Builder.Default
    private Integer totalCredits = 0;

    @Column(name = "used_credits", nullable = false)
    @Builder.Default
    private Integer usedCredits = 0;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public int getRemaining() {
        return totalCredits - usedCredits;
    }
}
