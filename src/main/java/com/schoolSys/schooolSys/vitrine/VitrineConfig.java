package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "vitrine_config")
@SQLDelete(sql = "UPDATE vitrine_config SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitrineConfig {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "school_display_name")
    private String schoolDisplayName;

    private String slogan;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Builder.Default
    @Column(name = "primary_color", nullable = false)
    private String primaryColor = "#1e40af";

    @Builder.Default
    @Column(name = "secondary_color", nullable = false)
    private String secondaryColor = "#f59e0b";

    @Builder.Default
    @Column(name = "accent_color", nullable = false)
    private String accentColor = "#10b981";

    @Builder.Default
    @Column(name = "theme_template", nullable = false)
    private String themeTemplate = "modern";

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_address")
    private String contactAddress;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "meta_description")
    private String metaDescription;

    // ─── Hero block ───
    @Column(name = "hero_badge_label")    private String heroBadgeLabel;
    @Column(name = "hero_badge_value")    private String heroBadgeValue;
    @Column(name = "hero_stat1_value")    private String heroStat1Value;
    @Column(name = "hero_stat1_label")    private String heroStat1Label;
    @Column(name = "hero_stat2_value")    private String heroStat2Value;
    @Column(name = "hero_stat2_label")    private String heroStat2Label;
    @Column(name = "hero_stat3_value")    private String heroStat3Value;
    @Column(name = "hero_stat3_label")    private String heroStat3Label;

    // ─── Marquee ───
    @Column(name = "marquee_item1") private String marqueeItem1;
    @Column(name = "marquee_item2") private String marqueeItem2;
    @Column(name = "marquee_item3") private String marqueeItem3;
    @Column(name = "marquee_item4") private String marqueeItem4;
    @Column(name = "marquee_item5") private String marqueeItem5;
    @Column(name = "marquee_item6") private String marqueeItem6;

    // ─── Trust strip ───
    @Column(name = "trust_stat1_value") private String trustStat1Value;
    @Column(name = "trust_stat1_label") private String trustStat1Label;
    @Column(name = "trust_stat2_value") private String trustStat2Value;
    @Column(name = "trust_stat2_label") private String trustStat2Label;
    @Column(name = "trust_stat3_value") private String trustStat3Value;
    @Column(name = "trust_stat3_label") private String trustStat3Label;
    @Column(name = "trust_stat4_value") private String trustStat4Value;
    @Column(name = "trust_stat4_label") private String trustStat4Label;

    // ─── About block ───
    @Column(name = "about_eyebrow")      private String aboutEyebrow;
    @Column(name = "about_title")        private String aboutTitle;
    @Column(name = "about_title_accent") private String aboutTitleAccent;
    @Column(name = "about_description", columnDefinition = "TEXT") private String aboutDescription;
    @Column(name = "about_feature1")     private String aboutFeature1;
    @Column(name = "about_feature2")     private String aboutFeature2;
    @Column(name = "about_feature3")     private String aboutFeature3;
    @Column(name = "about_feature4")     private String aboutFeature4;
    @Column(name = "about_badge_value")  private String aboutBadgeValue;
    @Column(name = "about_badge_label")  private String aboutBadgeLabel;

    // ─── Values block (3 fixed cards) ───
    @Column(name = "value1_title") private String value1Title;
    @Column(name = "value1_text", columnDefinition = "TEXT") private String value1Text;
    @Column(name = "value2_title") private String value2Title;
    @Column(name = "value2_text", columnDefinition = "TEXT") private String value2Text;
    @Column(name = "value3_title") private String value3Title;
    @Column(name = "value3_text", columnDefinition = "TEXT") private String value3Text;

    // ─── Programs block (3 fixed cards) ───
    @Column(name = "program1_title") private String program1Title;
    @Column(name = "program1_level") private String program1Level;
    @Column(name = "program1_text", columnDefinition = "TEXT") private String program1Text;
    @Column(name = "program2_title") private String program2Title;
    @Column(name = "program2_level") private String program2Level;
    @Column(name = "program2_text", columnDefinition = "TEXT") private String program2Text;
    @Column(name = "program3_title") private String program3Title;
    @Column(name = "program3_level") private String program3Level;
    @Column(name = "program3_text", columnDefinition = "TEXT") private String program3Text;

    // ─── Testimonial ───
    @Column(name = "testimonial_quote", columnDefinition = "TEXT") private String testimonialQuote;
    @Column(name = "testimonial_author") private String testimonialAuthor;
    @Column(name = "testimonial_role")   private String testimonialRole;

    // ─── CTA ───
    @Column(name = "cta_eyebrow")       private String ctaEyebrow;
    @Column(name = "cta_title")         private String ctaTitle;
    @Column(name = "cta_description", columnDefinition = "TEXT") private String ctaDescription;
    @Column(name = "cta_primary_label") private String ctaPrimaryLabel;
    @Column(name = "cta_primary_url")   private String ctaPrimaryUrl;

    // ─── Localisation (carte + horaires) ───
    @Column(name = "contact_latitude",  precision = 10, scale = 7) private java.math.BigDecimal contactLatitude;
    @Column(name = "contact_longitude", precision = 10, scale = 7) private java.math.BigDecimal contactLongitude;
    @Column(name = "contact_hours")     private String contactHours;

    @Builder.Default
    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
