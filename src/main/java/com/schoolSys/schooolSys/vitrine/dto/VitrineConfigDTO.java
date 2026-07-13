package com.schoolSys.schooolSys.vitrine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrineConfigDTO {

    private UUID id;
    private String schoolDisplayName;
    private String slogan;
    private String logoUrl;
    private String heroImageUrl;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String themeTemplate;
    private String contactPhone;
    private String contactEmail;
    private String contactAddress;
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappNumber;
    private String metaDescription;

    // Hero
    private String heroBadgeLabel;
    private String heroBadgeValue;
    private String heroStat1Value;
    private String heroStat1Label;
    private String heroStat2Value;
    private String heroStat2Label;
    private String heroStat3Value;
    private String heroStat3Label;

    // Marquee
    private String marqueeItem1;
    private String marqueeItem2;
    private String marqueeItem3;
    private String marqueeItem4;
    private String marqueeItem5;
    private String marqueeItem6;

    // Trust strip
    private String trustStat1Value;
    private String trustStat1Label;
    private String trustStat2Value;
    private String trustStat2Label;
    private String trustStat3Value;
    private String trustStat3Label;
    private String trustStat4Value;
    private String trustStat4Label;

    // About
    private String aboutEyebrow;
    private String aboutTitle;
    private String aboutTitleAccent;
    private String aboutDescription;
    private String aboutFeature1;
    private String aboutFeature2;
    private String aboutFeature3;
    private String aboutFeature4;
    private String aboutBadgeValue;
    private String aboutBadgeLabel;

    // Values
    private String value1Title;
    private String value1Text;
    private String value2Title;
    private String value2Text;
    private String value3Title;
    private String value3Text;

    // Programs
    private String program1Title;
    private String program1Level;
    private String program1Text;
    private String program2Title;
    private String program2Level;
    private String program2Text;
    private String program3Title;
    private String program3Level;
    private String program3Text;

    // Testimonial
    private String testimonialQuote;
    private String testimonialAuthor;
    private String testimonialRole;

    // CTA
    private String ctaEyebrow;
    private String ctaTitle;
    private String ctaDescription;
    private String ctaPrimaryLabel;
    private String ctaPrimaryUrl;

    // Localisation
    private java.math.BigDecimal contactLatitude;
    private java.math.BigDecimal contactLongitude;
    private String contactHours;

    private boolean published;
}
