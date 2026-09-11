package com.medicare.practice.service;

import com.medicare.practice.dto.DoctorAvailabilityDTOs.*;
import com.medicare.practice.entity.Appointment;
import com.medicare.practice.entity.Doctor;
import com.medicare.practice.entity.DoctorAvailability;
import com.medicare.practice.exception.BadRequestException;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.AppointmentRepository;
import com.medicare.practice.repository.DoctorAvailabilityRepository;
import com.medicare.practice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityDTO> getDoctorAvailabilities(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorAvailabilityDTO setAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time.");
        }

        Optional<DoctorAvailability> existing = availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek());
        DoctorAvailability availability;
        if (existing.isPresent()) {
            availability = existing.get();
            availability.setStartTime(request.getStartTime());
            availability.setEndTime(request.getEndTime());
            availability.setSlotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30);
            availability.setActive(request.getActive() != null ? request.getActive() : true);
        } else {
            availability = DoctorAvailability.builder()
                    .doctor(doctor)
                    .dayOfWeek(request.getDayOfWeek())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .slotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30)
                    .active(request.getActive() != null ? request.getActive() : true)
                    .build();
        }

        availability = availabilityRepository.save(availability);
        return mapToDTO(availability);
    }

    /**
     * Smart Appointment Availability Algorithm:
     * 1. Check if doctor exists and is active.
     * 2. Determine day of week for the target date.
     * 3. Fetch doctor's recurring availability for that day of week.
     * 4. Slice the working hours into discrete slot intervals (e.g., 30 mins).
     * 5. Query active appointments for the doctor on that date (status != CANCELLED).
     * 6. Check each candidate slot against bookings and current local time (if today).
     * 7. Return list of AvailableSlotDTO.
     */
    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> calculateAvailableSlots(Long doctorId, LocalDate targetDate) {
        if (targetDate.isBefore(LocalDate.now())) {
            return List.of(); // Past dates have no available slots
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        if (!doctor.isActive()) {
            return List.of();
        }

        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return List.of(); // Doctor doesn't practice on this day of week
        }

        List<Appointment> existingBookings = appointmentRepository.findActiveDoctorAppointmentsForDate(doctorId, targetDate);

        List<AvailableSlotDTO> slotDTOs = new ArrayList<>();
        LocalTime now = LocalTime.now();
        boolean isToday = targetDate.isEqual(LocalDate.now());

        for (DoctorAvailability availability : availabilities) {
            int durationMinutes = availability.getSlotDurationMinutes() > 0 ? availability.getSlotDurationMinutes() : 30;
            LocalTime currentSlotStart = availability.getStartTime();
            LocalTime workingEnd = availability.getEndTime();

            while (!currentSlotStart.plusMinutes(durationMinutes).isAfter(workingEnd)) {
                LocalTime currentSlotEnd = currentSlotStart.plusMinutes(durationMinutes);

                boolean isPastTime = isToday && currentSlotStart.isBefore(now.plusMinutes(5));

                // Check if any existing non-cancelled appointment overlaps with [currentSlotStart, currentSlotEnd)
                final LocalTime slotStart = currentSlotStart;
                final LocalTime slotEnd = currentSlotEnd;
                boolean isBooked = existingBookings.stream().anyMatch(appt ->
                        appt.getStartTime().isBefore(slotEnd) && appt.getEndTime().isAfter(slotStart)
                );

                boolean isAvailable = !isPastTime && !isBooked;
                String reason = isAvailable ? "Available" : (isPastTime ? "Past time" : "Booked");

                slotDTOs.add(AvailableSlotDTO.builder()
                        .date(targetDate)
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .available(isAvailable)
                        .reason(reason)
                        .build());

                currentSlotStart = currentSlotEnd;
            }
        }

        return slotDTOs;
    }

    public DoctorAvailabilityDTO mapToDTO(DoctorAvailability a) {
        return DoctorAvailabilityDTO.builder()
                .id(a.getId())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .slotDurationMinutes(a.getSlotDurationMinutes())
                .active(a.isActive())
                .build();
    }
}
