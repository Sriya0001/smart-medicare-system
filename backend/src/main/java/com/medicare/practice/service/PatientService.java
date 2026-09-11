package com.medicare.practice.service;

import com.medicare.practice.dto.PatientDTOs.*;
import com.medicare.practice.entity.Patient;
import com.medicare.practice.entity.Role;
import com.medicare.practice.entity.User;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.PatientRepository;
import com.medicare.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        return mapToDTO(patient);
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        return mapToDTO(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientRepository.searchPatients(query.trim()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByDoctorId(Long doctorId) {
        return patientRepository.findPatientsByDoctorId(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDTO updatePatientProfile(Long id, PatientProfileRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setEmergencyContact(request.getEmergencyContact());

        patient = patientRepository.save(patient);
        return mapToDTO(patient);
    }

    public PatientDTO mapToDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .userId(patient.getUser() != null ? patient.getUser().getId() : null)
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phoneNumber(patient.getPhoneNumber())
                .address(patient.getAddress())
                .bloodGroup(patient.getBloodGroup())
                .emergencyContact(patient.getEmergencyContact())
                .createdAt(patient.getCreatedAt())
                .build();
    }
}
