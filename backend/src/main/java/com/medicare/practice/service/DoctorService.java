package com.medicare.practice.service;

import com.medicare.practice.dto.DoctorAvailabilityDTOs.DoctorAvailabilityDTO;
import com.medicare.practice.dto.DoctorDTOs.*;
import com.medicare.practice.entity.Doctor;
import com.medicare.practice.entity.DoctorAvailability;
import com.medicare.practice.entity.Role;
import com.medicare.practice.entity.User;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.DoctorAvailabilityRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllActiveDoctors() {
        return doctorRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctorsForAdmin() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));
        return mapToDTO(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + userId));
        return mapToDTO(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCaseAndActiveTrue(specialty).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return doctorRepository.findAllSpecialties();
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveDoctors();
        }
        return doctorRepository.searchDoctors(query.trim()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDTO createDoctor(DoctorCreateRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User already exists with email: " + normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .phoneNumber(request.getPhoneNumber())
                .experienceYears(request.getExperienceYears())
                .biography(request.getBiography())
                .consultationFee(request.getConsultationFee())
                .active(true)
                .build();
        doctor = doctorRepository.save(doctor);

        // Default availability Mon-Fri 09:00 - 17:00
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            DoctorAvailability da = DoctorAvailability.builder()
                    .doctor(doctor)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDurationMinutes(30)
                    .active(true)
                    .build();
            availabilityRepository.save(da);
        }

        return mapToDTO(doctor);
    }

    @Transactional
    public DoctorDTO updateDoctor(Long id, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialty(request.getSpecialty());
        if (request.getLicenseNumber() != null) doctor.setLicenseNumber(request.getLicenseNumber());
        if (request.getPhoneNumber() != null) doctor.setPhoneNumber(request.getPhoneNumber());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getBiography() != null) doctor.setBiography(request.getBiography());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getActive() != null) doctor.setActive(request.getActive());

        doctor = doctorRepository.save(doctor);
        return mapToDTO(doctor);
    }

    @Transactional
    public void deactivateDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));
        doctor.setActive(false);
        if (doctor.getUser() != null) {
            doctor.getUser().setEnabled(false);
        }
        doctorRepository.save(doctor);
    }

    public DoctorDTO mapToDTO(Doctor doctor) {
        List<DoctorAvailabilityDTO> availabilities = null;
        if (doctor.getAvailabilities() != null) {
            availabilities = doctor.getAvailabilities().stream()
                    .map(a -> DoctorAvailabilityDTO.builder()
                            .id(a.getId())
                            .doctorId(doctor.getId())
                            .doctorName(doctor.getFullName())
                            .dayOfWeek(a.getDayOfWeek())
                            .startTime(a.getStartTime())
                            .endTime(a.getEndTime())
                            .slotDurationMinutes(a.getSlotDurationMinutes())
                            .active(a.isActive())
                            .build())
                    .collect(Collectors.toList());
        }

        return DoctorDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser() != null ? doctor.getUser().getId() : null)
                .email(doctor.getUser() != null ? doctor.getUser().getEmail() : null)
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .licenseNumber(doctor.getLicenseNumber())
                .phoneNumber(doctor.getPhoneNumber())
                .experienceYears(doctor.getExperienceYears())
                .biography(doctor.getBiography())
                .consultationFee(doctor.getConsultationFee())
                .active(doctor.isActive())
                .availabilities(availabilities)
                .build();
    }
}
