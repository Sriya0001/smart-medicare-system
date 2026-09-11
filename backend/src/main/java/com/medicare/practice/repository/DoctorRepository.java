package com.medicare.practice.repository;

import com.medicare.practice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);

    List<Doctor> findByActiveTrue();

    List<Doctor> findBySpecialtyIgnoreCaseAndActiveTrue(String specialty);

    @Query("SELECT DISTINCT d.specialty FROM Doctor d WHERE d.active = true")
    List<String> findAllSpecialties();

    @Query("SELECT d FROM Doctor d WHERE d.active = true AND (" +
           "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.specialty) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Doctor> searchDoctors(@Param("query") String query);

    long countByActiveTrue();
}
