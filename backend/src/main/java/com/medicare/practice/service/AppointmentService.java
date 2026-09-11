package com.medicare.practice.service;

import com.medicare.practice.dto.AppointmentDTOs.*;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.BadRequestException;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.AppointmentRepository;
import com.medicare.practice.repository.DoctorAvailabilityRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
        return mapToDTO(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getTodayDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, LocalDate.now()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getUpcomingDoctorAppointments(Long doctorId) {
        return appointmentRepository.findUpcomingDoctorAppointments(doctorId, LocalDate.now()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getUpcomingPatientAppointments(Long patientId) {
        return appointmentRepository.findUpcomingPatientAppointments(patientId, LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Book Appointment with Double-Booking Prevention:
     * - Runs with SERIALIZABLE/REPEATABLE_READ isolation or transactional lock check.
     * - Validates date is not in past.
     * - Validates that slot falls inside doctor's configured working hours for that DayOfWeek.
     * - Re-checks that NO conflicting appointment exists for the doctor in that time frame.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public synchronized AppointmentDTO createAppointment(AppointmentCreateRequest request) {
        LocalDate date = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();

        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book an appointment in the past.");
        }

        if (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now().plusMinutes(5))) {
            throw new BadRequestException("Cannot book an appointment slot that has already passed.");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        if (!doctor.isActive()) {
            throw new BadRequestException("Selected doctor is currently not accepting appointments.");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        // Determine slot duration
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(doctor.getId(), dayOfWeek);

        if (availabilities.isEmpty()) {
            throw new BadRequestException("Doctor has no active practice hours scheduled on " + dayOfWeek);
        }

        DoctorAvailability matchedAvailability = availabilities.stream()
                .filter(a -> !startTime.isBefore(a.getStartTime()) && startTime.isBefore(a.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Requested time " + startTime + " is outside the doctor's working hours on " + dayOfWeek));

        int duration = matchedAvailability.getSlotDurationMinutes() > 0 ? matchedAvailability.getSlotDurationMinutes() : 30;
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : startTime.plusMinutes(duration);

        if (endTime.isAfter(matchedAvailability.getEndTime())) {
            throw new BadRequestException("Appointment extends beyond doctor's working hours.");
        }

        // Double-booking check: verify no overlapping active appointment exists
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctor.getId(), date, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new ConflictException("The selected time slot " + startTime + " - " + endTime + " is no longer available. Please select another slot.");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.BOOKED)
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Successfully booked appointment #{} for patient {} with doctor {} on {} at {}",
                appointment.getId(), patient.getFullName(), doctor.getFullName(), date, startTime);

        return mapToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO updateStatus(Long id, AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        AppointmentStatus newStatus = request.getStatus();

        if (appointment.getStatus() == AppointmentStatus.COMPLETED && newStatus == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel an already completed appointment.");
        }

        appointment.setStatus(newStatus);
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment = appointmentRepository.save(appointment);
        return mapToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed appointment.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason != null ? reason : "Cancelled by user");
        appointment = appointmentRepository.save(appointment);
        return mapToDTO(appointment);
    }

    public AppointmentDTO mapToDTO(Appointment a) {
        return AppointmentDTO.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFullName())
                .patientPhone(a.getPatient().getPhoneNumber())
                .patientEmail(a.getPatient().getUser() != null ? a.getPatient().getUser().getEmail() : null)
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .doctorSpecialty(a.getDoctor().getSpecialty())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .reason(a.getReason())
                .notes(a.getNotes())
                .cancellationReason(a.getCancellationReason())
                .consultationId(a.getConsultation() != null ? a.getConsultation().getId() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
