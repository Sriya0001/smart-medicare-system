package com.medicare.practice.config;

import com.medicare.practice.entity.*;
import com.medicare.practice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains users. Skipping demo data initialization.");
            return;
        }

        log.info("Seeding realistic healthcare practice demo data...");

        // 1. Admin
        User adminUser = userRepository.save(User.builder()
                .email("admin@medicare.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build());

        // 2. Doctors
        Doctor docSarah = createDoctor(
                "sarah.jenkins@medicare.com", "Doctor@123", "Sarah", "Jenkins",
                "Cardiology", "LIC-MD-48921", "+1 (555) 234-5678", 14,
                "Board-certified cardiologist specializing in preventive cardiology, hypertension management, and cardiovascular risk assessment.",
                new BigDecimal("150.00")
        );

        Doctor docRobert = createDoctor(
                "robert.miller@medicare.com", "Doctor@123", "Robert", "Miller",
                "Dermatology", "LIC-MD-37812", "+1 (555) 345-6789", 10,
                "Specialist in medical and cosmetic dermatology, acne treatment, and skin cancer screenings.",
                new BigDecimal("130.00")
        );

        Doctor docElena = createDoctor(
                "elena.rostova@medicare.com", "Doctor@123", "Elena", "Rostova",
                "Pediatrics", "LIC-MD-90145", "+1 (555) 456-7890", 8,
                "Dedicated pediatrician providing compassionate care from infancy through adolescence.",
                new BigDecimal("120.00")
        );

        Doctor docJames = createDoctor(
                "james.wilson@medicare.com", "Doctor@123", "James", "Wilson",
                "Orthopedics", "LIC-MD-61234", "+1 (555) 567-8901", 16,
                "Orthopedic surgeon specializing in sports injuries, joint preservation, and rehabilitation.",
                new BigDecimal("160.00")
        );

        // 3. Patients
        Patient patJohn = createPatient(
                "john.smith@gmail.com", "Patient@123", "John", "Smith",
                LocalDate.of(1985, 4, 12), "Male", "+1 (555) 789-0123",
                "124 Elmwood Avenue, Boston, MA", "O+", "Jane Smith (Spouse): +1 (555) 789-0124"
        );

        Patient patEmily = createPatient(
                "emily.davis@gmail.com", "Patient@123", "Emily", "Davis",
                LocalDate.of(1992, 8, 24), "Female", "+1 (555) 890-1234",
                "742 Evergreen Terrace, Springfield, MA", "A+", "Mark Davis (Father): +1 (555) 890-1235"
        );

        Patient patMichael = createPatient(
                "michael.brown@gmail.com", "Patient@123", "Michael", "Brown",
                LocalDate.of(1978, 11, 5), "Male", "+1 (555) 901-2345",
                "88 Beacon Street, Boston, MA", "B-", "Lisa Brown (Wife): +1 (555) 901-2346"
        );

        Patient patSophia = createPatient(
                "sophia.martinez@gmail.com", "Patient@123", "Sophia", "Martinez",
                LocalDate.of(2000, 2, 18), "Female", "+1 (555) 012-3456",
                "45 Commonwealth Ave, Cambridge, MA", "AB+", "Carlos Martinez (Brother): +1 (555) 012-3457"
        );

        Patient patDavid = createPatient(
                "david.johnson@gmail.com", "Patient@123", "David", "Johnson",
                LocalDate.of(1965, 7, 30), "Male", "+1 (555) 123-4567",
                "312 Harvard Street, Brookline, MA", "O-", "Mary Johnson (Wife): +1 (555) 123-4568"
        );

        // 4. Appointments & Historical Consultations
        // Completed Consultation 1 (John with Dr. Sarah Jenkins)
        Appointment pastAppt1 = appointmentRepository.save(Appointment.builder()
                .patient(patJohn)
                .doctor(docSarah)
                .appointmentDate(LocalDate.now().minusDays(14))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Chest tightness and mild shortness of breath during workouts")
                .notes("Follow-up in 2 weeks after initial lab work")
                .build());

        Consultation cons1 = consultationRepository.save(Consultation.builder()
                .appointment(pastAppt1)
                .patient(patJohn)
                .doctor(docSarah)
                .consultationDate(LocalDateTime.now().minusDays(14).withHour(10).withMinute(30))
                .symptoms("Patient reported intermittent substernal chest tightness during high-intensity treadmill runs. No radiation to jaw or left arm.")
                .diagnosis("Stage 1 Essential Hypertension; Exercise-Induced Exertional Dyspnea (Non-ischemic)")
                .treatmentPlan("Initiate lifestyle modifications: low sodium DASH diet, moderate aerobic exercise 30 min/day, prescribed Amlodipine 5mg daily.")
                .notes("ECG showed normal sinus rhythm with no ST changes. Lipid panel ordered.")
                .followUpDate(LocalDate.now().plusDays(16))
                .prescriptions(new ArrayList<>())
                .build());

        Prescription rx1 = prescriptionRepository.save(Prescription.builder()
                .consultation(cons1)
                .patient(patJohn)
                .doctor(docSarah)
                .issueDate(LocalDate.now().minusDays(14))
                .notes("Take in the morning with water")
                .items(new ArrayList<>())
                .build());

        rx1.getItems().add(PrescriptionItem.builder()
                .prescription(rx1)
                .medicationName("Amlodipine Besylate")
                .dosage("5 mg")
                .frequency("Once daily (Morning)")
                .duration("30 days")
                .instructions("Take after breakfast. Avoid grapefruit juice.")
                .build());
        rx1.getItems().add(PrescriptionItem.builder()
                .prescription(rx1)
                .medicationName("Aspirin (CardioProtect)")
                .dosage("81 mg")
                .frequency("Once daily with food")
                .duration("30 days")
                .instructions("Low-dose cardioprotective therapy.")
                .build());
        prescriptionRepository.save(rx1);

        // Completed Consultation 2 (Emily with Dr. Robert Miller)
        Appointment pastAppt2 = appointmentRepository.save(Appointment.builder()
                .patient(patEmily)
                .doctor(docRobert)
                .appointmentDate(LocalDate.now().minusDays(7))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Persistent facial erythema and papules flare-up")
                .notes("Prescribed topical metronidazole")
                .build());

        Consultation cons2 = consultationRepository.save(Consultation.builder()
                .appointment(pastAppt2)
                .patient(patEmily)
                .doctor(docRobert)
                .consultationDate(LocalDateTime.now().minusDays(7).withHour(14).withMinute(30))
                .symptoms("Erythema across malar region and nasal bridge with small inflammatory papules triggered by sun exposure.")
                .diagnosis("Erythematotelangiectatic Rosacea")
                .treatmentPlan("Apply Metronidazole 0.75% gel twice daily. Strict mineral sunscreen SPF 50+ application every 3 hours.")
                .notes("Avoid hot showers, spicy foods, and harsh chemical exfoliants.")
                .followUpDate(LocalDate.now().plusDays(21))
                .prescriptions(new ArrayList<>())
                .build());

        Prescription rx2 = prescriptionRepository.save(Prescription.builder()
                .consultation(cons2)
                .patient(patEmily)
                .doctor(docRobert)
                .issueDate(LocalDate.now().minusDays(7))
                .notes("Dermatological topical regimen")
                .items(new ArrayList<>())
                .build());

        rx2.getItems().add(PrescriptionItem.builder()
                .prescription(rx2)
                .medicationName("Metronidazole Topical Gel")
                .dosage("0.75%")
                .frequency("Twice daily")
                .duration("60 days")
                .instructions("Apply thin layer over cleansed facial skin.")
                .build());
        prescriptionRepository.save(rx2);

        // Upcoming Confirmed Appointments
        appointmentRepository.save(Appointment.builder()
                .patient(patJohn)
                .doctor(docSarah)
                .appointmentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Cardiology follow-up & Blood Pressure check")
                .notes("Review response to Amlodipine")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patMichael)
                .doctor(docJames)
                .appointmentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .reason("Right knee joint pain after tennis match")
                .notes("Possible meniscus sprain")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(patSophia)
                .doctor(docRobert)
                .appointmentDate(LocalDate.now().plusDays(4))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(15, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Annual skin examination & mole check")
                .notes("Routine preventive check")
                .build());

        log.info("Demo data initialized successfully with 1 Admin, 4 Doctors, 5 Patients, Availabilities, and History.");
    }

    private Doctor createDoctor(String email, String rawPassword, String firstName, String lastName,
                                 String specialty, String licenseNumber, String phone, int experienceYears,
                                 String biography, BigDecimal fee) {
        User user = userRepository.save(User.builder()
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .licenseNumber(licenseNumber)
                .phoneNumber(phone)
                .experienceYears(experienceYears)
                .biography(biography)
                .consultationFee(fee)
                .active(true)
                .build());

        // Mon-Fri 09:00 - 17:00 availability
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

        return doctor;
    }

    private Patient createPatient(String email, String rawPassword, String firstName, String lastName,
                                  LocalDate dob, String gender, String phone, String address,
                                  String bloodGroup, String emergencyContact) {
        User user = userRepository.save(User.builder()
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_PATIENT)
                .enabled(true)
                .build());

        return patientRepository.save(Patient.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dob)
                .gender(gender)
                .phoneNumber(phone)
                .address(address)
                .bloodGroup(bloodGroup)
                .emergencyContact(emergencyContact)
                .build());
    }
}
