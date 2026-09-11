package com.medicare.practice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 60)
    private String firstName;

    @Column(nullable = false, length = 60)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(nullable = false, length = 50)
    private String licenseNumber;

    @Column(length = 25)
    private String phoneNumber;

    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String biography;

    private BigDecimal consultationFee;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorAvailability> availabilities = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getFullName() {
        return "Dr. " + (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}
