package com.schoolSys.schooolSys.student;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@SQLDelete(sql = "UPDATE students SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name_ar")
    private String firstNameAr;

    @Column(name = "last_name_ar")
    private String lastNameAr;

    @Column(nullable = false, length = 1)
    private String sex;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "birth_place")
    private String birthPlace;

    private String address;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(unique = true)
    private String email;

    private String classe;

    private String niveau;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "Actif";

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "parent_last_name")
    private String parentLastName;

    @Column(name = "parent_first_name")
    private String parentFirstName;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Column(name = "parent_email")
    private String parentEmail;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(unique = true, length = 20)
    private String matricule;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
