package com.medicare.practice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

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

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 25)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(length = 10)
    private String bloodGroup;

    @Column(length = 100)
    private String emergencyContact;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}
