package com.schoolSys.schooolSys.absence;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "absence_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceSettings {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "seuil_alerte_jaune", nullable = false)
    @Builder.Default
    private Integer seuilAlerteJaune = 3;

    @Column(name = "seuil_alerte_rouge", nullable = false)
    @Builder.Default
    private Integer seuilAlerteRouge = 5;

    @Column(name = "notification_auto", nullable = false)
    @Builder.Default
    private Boolean notificationAuto = true;

    @Column(name = "notification_email", nullable = false)
    @Builder.Default
    private Boolean notificationEmail = true;

    @Column(name = "notification_sms", nullable = false)
    @Builder.Default
    private Boolean notificationSms = false;
}
