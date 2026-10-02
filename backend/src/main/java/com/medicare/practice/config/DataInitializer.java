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

        log.info("Seeding rich, realistic healthcare practice demo data with diverse specialties and patient histories...");

        // ==========================================
        // 1. Practice Administrator
        // ==========================================
        userRepository.save(User.builder()
                .email("admin@medicare.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build());

        // ==========================================
        // 2. Doctors Across 6 Diverse Specialties
        // ==========================================
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
                "Compassionate pediatrician providing comprehensive healthcare from infancy through young adulthood.",
                new BigDecimal("120.00")
        );

        Doctor docJames = createDoctor(
                "james.wilson@medicare.com", "Doctor@123", "James", "Wilson",
                "Orthopedics", "LIC-MD-61234", "+1 (555) 567-8901", 16,
                "Orthopedic surgeon specializing in sports injuries, joint preservation, and rehabilitation.",
                new BigDecimal("160.00")
        );

        Doctor docMarcus = createDoctor(
                "marcus.chen@medicare.com", "Doctor@123", "Marcus", "Chen",
                "Neurology", "LIC-MD-77890", "+1 (555) 678-9012", 12,
                "Neurologist focused on migraine management, peripheral neuropathies, and cognitive neurological care.",
                new BigDecimal("170.00")
        );

        Doctor docPriya = createDoctor(
                "priya.patel@medicare.com", "Doctor@123", "Priya", "Patel",
                "Endocrinology", "LIC-MD-88341", "+1 (555) 789-0125", 11,
                "Endocrinologist specializing in metabolic disorders, comprehensive Type 1/2 diabetes care, and thyroid health.",
                new BigDecimal("155.00")
        );

        // ==========================================
        // 3. Patients With Varied Clinical Trajectories
        // ==========================================
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

        Patient patOlivia = createPatient(
                "olivia.taylor@gmail.com", "Patient@123", "Olivia", "Taylor",
                LocalDate.of(1996, 12, 3), "Female", "+1 (555) 321-6540",
                "19 Newbury Street, Boston, MA", "A-", "Emma Taylor (Sister): +1 (555) 321-6541"
        );

        // ==========================================
        // 4. Completed Historical Consultations & Prescriptions
        // ==========================================

        // --- Patient 1: John Smith (Cardiology & Lipid Care) ---
        // Consultation 1: 30 days ago
        Appointment apptJohn1 = appointmentRepository.save(Appointment.builder()
                .patient(patJohn).doctor(docSarah).appointmentDate(LocalDate.now().minusDays(30))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Exertional chest tightness and elevated blood pressure")
                .notes("Completed initial baseline assessment").build());

        Consultation consJohn1 = consultationRepository.save(Consultation.builder()
                .appointment(apptJohn1).patient(patJohn).doctor(docSarah)
                .consultationDate(LocalDateTime.now().minusDays(30).withHour(10).withMinute(30))
                .symptoms("Patient reported intermittent substernal chest tightness during treadmill runs. Resting BP 142/92 mmHg.")
                .diagnosis("Stage 1 Essential Hypertension; Exercise-Induced Exertional Dyspnea (Non-ischemic)")
                .treatmentPlan("DASH low-sodium diet, moderate aerobic exercise 30 min/day, prescribed Amlodipine 5mg daily.")
                .notes("ECG showed normal sinus rhythm. Baseline lipid and renal panel ordered.")
                .followUpDate(LocalDate.now().minusDays(2))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxJohn1 = prescriptionRepository.save(Prescription.builder()
                .consultation(consJohn1).patient(patJohn).doctor(docSarah)
                .issueDate(LocalDate.now().minusDays(30))
                .notes("Cardioprotective and antihypertensive regimen")
                .items(new ArrayList<>()).build());

        rxJohn1.getItems().add(PrescriptionItem.builder()
                .prescription(rxJohn1).medicationName("Amlodipine Besylate")
                .dosage("5 mg").frequency("Once daily (Morning)")
                .duration("30 days").instructions("Take after breakfast. Avoid grapefruit juice.").build());

        rxJohn1.getItems().add(PrescriptionItem.builder()
                .prescription(rxJohn1).medicationName("Aspirin (CardioProtect)")
                .dosage("81 mg").frequency("Once daily with food")
                .duration("30 days").instructions("Low-dose cardioprotective therapy.").build());
        prescriptionRepository.save(rxJohn1);

        // Consultation 2: 2 days ago (Follow-up)
        Appointment apptJohn2 = appointmentRepository.save(Appointment.builder()
                .patient(patJohn).doctor(docSarah).appointmentDate(LocalDate.now().minusDays(2))
                .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(11, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Cardiology follow-up & Blood Pressure re-evaluation")
                .notes("Good response to Amlodipine").build());

        Consultation consJohn2 = consultationRepository.save(Consultation.builder()
                .appointment(apptJohn2).patient(patJohn).doctor(docSarah)
                .consultationDate(LocalDateTime.now().minusDays(2).withHour(11).withMinute(30))
                .symptoms("Chest tightness resolved during routine exercise. Resting BP improved to 124/80 mmHg. Labs show LDL 148 mg/dL.")
                .diagnosis("Controlled Stage 1 Essential Hypertension; Mixed Primary Hyperlipidemia")
                .treatmentPlan("Continue Amlodipine 5mg. Initiate Atorvastatin 20mg nightly for cardiovascular risk reduction.")
                .notes("Repeat lipid profile in 12 weeks. Patient reports high medication adherence.")
                .followUpDate(LocalDate.now().plusMonths(3))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxJohn2 = prescriptionRepository.save(Prescription.builder()
                .consultation(consJohn2).patient(patJohn).doctor(docSarah)
                .issueDate(LocalDate.now().minusDays(2))
                .notes("Lipid-lowering therapy addition")
                .items(new ArrayList<>()).build());

        rxJohn2.getItems().add(PrescriptionItem.builder()
                .prescription(rxJohn2).medicationName("Atorvastatin Calcium")
                .dosage("20 mg").frequency("Once daily at bedtime")
                .duration("90 days").instructions("Take at night. Avoid excessive alcohol consumption.").build());
        prescriptionRepository.save(rxJohn2);

        // --- Patient 2: Emily Davis (Dermatology) ---
        Appointment apptEmily = appointmentRepository.save(Appointment.builder()
                .patient(patEmily).doctor(docRobert).appointmentDate(LocalDate.now().minusDays(10))
                .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(14, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Facial redness, erythema, and inflammatory papules flare-up")
                .notes("Prescribed topical metronidazole regimen").build());

        Consultation consEmily = consultationRepository.save(Consultation.builder()
                .appointment(apptEmily).patient(patEmily).doctor(docRobert)
                .consultationDate(LocalDateTime.now().minusDays(10).withHour(14).withMinute(30))
                .symptoms("Erythema across nasal bridge and cheeks with small papules triggered by sun exposure and hot beverages.")
                .diagnosis("Erythematotelangiectatic Rosacea (Moderate)")
                .treatmentPlan("Apply Metronidazole 0.75% gel twice daily. Daily broad-spectrum mineral sunscreen SPF 50+.")
                .notes("Advised to avoid hot showers, spicy foods, and harsh chemical exfoliants.")
                .followUpDate(LocalDate.now().plusDays(20))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxEmily = prescriptionRepository.save(Prescription.builder()
                .consultation(consEmily).patient(patEmily).doctor(docRobert)
                .issueDate(LocalDate.now().minusDays(10))
                .notes("Topical dermatological regimen")
                .items(new ArrayList<>()).build());

        rxEmily.getItems().add(PrescriptionItem.builder()
                .prescription(rxEmily).medicationName("Metronidazole Topical Gel")
                .dosage("0.75%").frequency("Twice daily")
                .duration("60 days").instructions("Apply thin layer over cleansed facial skin morning and night.").build());
        prescriptionRepository.save(rxEmily);

        // --- Patient 3: Michael Brown (Type 2 Diabetes & Neuropathy) ---
        // Consultation 1: 45 days ago (Endocrinology)
        Appointment apptMichael1 = appointmentRepository.save(Appointment.builder()
                .patient(patMichael).doctor(docPriya).appointmentDate(LocalDate.now().minusDays(45))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Frequent urination, increased thirst, and fasting blood glucose 180 mg/dL")
                .notes("HbA1c test 8.6%").build());

        Consultation consMichael1 = consultationRepository.save(Consultation.builder()
                .appointment(apptMichael1).patient(patMichael).doctor(docPriya)
                .consultationDate(LocalDateTime.now().minusDays(45).withHour(9).withMinute(30))
                .symptoms("Polyuria, polydipsia, and postprandial fatigue for the past 6 weeks. Fasting blood sugar 178 mg/dL.")
                .diagnosis("Type 2 Diabetes Mellitus with Inadequate Glycemic Control")
                .treatmentPlan("Nutritional counseling (carb counting), daily glucose monitoring, initiate Metformin 500mg BID and Empagliflozin 10mg daily.")
                .notes("Encouraged 150 minutes of weekly moderate walking. Recheck HbA1c in 3 months.")
                .followUpDate(LocalDate.now().minusDays(15))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxMichael1 = prescriptionRepository.save(Prescription.builder()
                .consultation(consMichael1).patient(patMichael).doctor(docPriya)
                .issueDate(LocalDate.now().minusDays(45))
                .notes("Dual oral hypoglycemic regimen")
                .items(new ArrayList<>()).build());

        rxMichael1.getItems().add(PrescriptionItem.builder()
                .prescription(rxMichael1).medicationName("Metformin Hydrochloride")
                .dosage("500 mg").frequency("Twice daily with meals")
                .duration("90 days").instructions("Take with breakfast and dinner to minimize GI upset.").build());

        rxMichael1.getItems().add(PrescriptionItem.builder()
                .prescription(rxMichael1).medicationName("Empagliflozin (Jardiance)")
                .dosage("10 mg").frequency("Once daily (Morning)")
                .duration("90 days").instructions("Take with or without food. Stay well hydrated.").build());
        prescriptionRepository.save(rxMichael1);

        // Consultation 2: 15 days ago (Neurology)
        Appointment apptMichael2 = appointmentRepository.save(Appointment.builder()
                .patient(patMichael).doctor(docMarcus).appointmentDate(LocalDate.now().minusDays(15))
                .startTime(LocalTime.of(15, 0)).endTime(LocalTime.of(15, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Burning sensation and tingling in both feet at night")
                .notes("Monofilament exam abnormal in distal extremities").build());

        Consultation consMichael2 = consultationRepository.save(Consultation.builder()
                .appointment(apptMichael2).patient(patMichael).doctor(docMarcus)
                .consultationDate(LocalDateTime.now().minusDays(15).withHour(15).withMinute(30))
                .symptoms("Bilateral symmetric burning pain, pins-and-needles paresthesia in soles and toes, worse at bedtime.")
                .diagnosis("Diabetic Symmetrical Distal Polyneuropathy")
                .treatmentPlan("Initiate Gabapentin 300mg at bedtime, titrate up to 300mg TID as tolerated. Diabetic foot care education.")
                .notes("Advised daily visual inspection of feet, seamless socks, and proper footwear.")
                .followUpDate(LocalDate.now().plusDays(30))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxMichael2 = prescriptionRepository.save(Prescription.builder()
                .consultation(consMichael2).patient(patMichael).doctor(docMarcus)
                .issueDate(LocalDate.now().minusDays(15))
                .notes("Neuropathic pain management")
                .items(new ArrayList<>()).build());

        rxMichael2.getItems().add(PrescriptionItem.builder()
                .prescription(rxMichael2).medicationName("Gabapentin")
                .dosage("300 mg").frequency("Once daily at bedtime")
                .duration("30 days").instructions("Take at night. May cause drowsiness; avoid operating heavy machinery.").build());
        prescriptionRepository.save(rxMichael2);

        // --- Patient 4: Sophia Martinez (Pediatrics & Asthma) ---
        Appointment apptSophia = appointmentRepository.save(Appointment.builder()
                .patient(patSophia).doctor(docElena).appointmentDate(LocalDate.now().minusDays(5))
                .startTime(LocalTime.of(16, 0)).endTime(LocalTime.of(16, 30))
                .status(AppointmentStatus.COMPLETED)
                .reason("Seasonal wheezing, chest tightness during cold weather, and nocturnal coughing")
                .notes("Spirometry showed mild obstruction").build());

        Consultation consSophia = consultationRepository.save(Consultation.builder()
                .appointment(apptSophia).patient(patSophia).doctor(docElena)
                .consultationDate(LocalDateTime.now().minusDays(5).withHour(16).withMinute(30))
                .symptoms("Wheezing episodes 3 times per week, night awakening with dry cough, triggered by cold air and pollen.")
                .diagnosis("Moderate Persistent Allergic Asthma")
                .treatmentPlan("Fluticasone Propionate daily controller inhaler + Albuterol rescue inhaler as needed for acute shortness of breath.")
                .notes("Demonstrated spacer technique. Provided Asthma Action Plan.")
                .followUpDate(LocalDate.now().plusMonths(1))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxSophia = prescriptionRepository.save(Prescription.builder()
                .consultation(consSophia).patient(patSophia).doctor(docElena)
                .issueDate(LocalDate.now().minusDays(5))
                .notes("Asthma controller and rescue therapy")
                .items(new ArrayList<>()).build());

        rxSophia.getItems().add(PrescriptionItem.builder()
                .prescription(rxSophia).medicationName("Fluticasone Propionate Inhaler")
                .dosage("110 mcg").frequency("2 puffs twice daily")
                .duration("30 days").instructions("Rinse mouth thoroughly with water after each use.").build());

        rxSophia.getItems().add(PrescriptionItem.builder()
                .prescription(rxSophia).medicationName("Albuterol Sulfate HFA Inhaler")
                .dosage("90 mcg").frequency("1-2 puffs every 4-6 hours as needed")
                .duration("30 days").instructions("Use for acute shortness of breath or 15 mins prior to exercise.").build());
        prescriptionRepository.save(rxSophia);

        // --- Patient 5: David Johnson (Orthopedics) ---
        Appointment apptDavid = appointmentRepository.save(Appointment.builder()
                .patient(patDavid).doctor(docJames).appointmentDate(LocalDate.now().minusDays(18))
                .startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(11, 0))
                .status(AppointmentStatus.COMPLETED)
                .reason("Persistent right knee joint pain and morning stiffness")
                .notes("Weight-bearing X-rays show joint space narrowing").build());

        Consultation consDavid = consultationRepository.save(Consultation.builder()
                .appointment(apptDavid).patient(patDavid).doctor(docJames)
                .consultationDate(LocalDateTime.now().minusDays(18).withHour(11).withMinute(0))
                .symptoms("Aching right medial knee pain, stiffness lasting 20 minutes in morning, difficulty walking down stairs.")
                .diagnosis("Primary Osteoarthritis of Right Knee (Kellgren-Lawrence Grade 2)")
                .treatmentPlan("Low-impact quadriceps strengthening physical therapy, weight management, prescribed Meloxicam 15mg daily.")
                .notes("Review response in 4 weeks. Avoid high-impact running or heavy squatting.")
                .followUpDate(LocalDate.now().plusDays(10))
                .prescriptions(new ArrayList<>()).build());

        Prescription rxDavid = prescriptionRepository.save(Prescription.builder()
                .consultation(consDavid).patient(patDavid).doctor(docJames)
                .issueDate(LocalDate.now().minusDays(18))
                .notes("Anti-inflammatory joint care")
                .items(new ArrayList<>()).build());

        rxDavid.getItems().add(PrescriptionItem.builder()
                .prescription(rxDavid).medicationName("Meloxicam")
                .dosage("15 mg").frequency("Once daily with food")
                .duration("30 days").instructions("Take with a full meal or glass of milk. Do not take with other NSAIDs like ibuprofen.").build());
        prescriptionRepository.save(rxDavid);

        // ==========================================
        // 5. Active, Confirmed, Booked & Cancelled Appointments
        // ==========================================

        // Confirmed Upcoming Appointment 1 (John with Dr. Sarah Jenkins)
        appointmentRepository.save(Appointment.builder()
                .patient(patJohn).doctor(docSarah)
                .appointmentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Cardiology follow-up & Blood Pressure check")
                .notes("Review lipid panel results").build());

        // Confirmed Upcoming Appointment 2 (Emily with Dr. Robert Miller)
        appointmentRepository.save(Appointment.builder()
                .patient(patEmily).doctor(docRobert)
                .appointmentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(14, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Dermatology follow-up on Metronidazole response")
                .notes("Assess reduction in facial erythema").build());

        // Booked Appointment 3 (Michael with Dr. Priya Patel - Waiting confirmation)
        appointmentRepository.save(Appointment.builder()
                .patient(patMichael).doctor(docPriya)
                .appointmentDate(LocalDate.now().plusDays(4))
                .startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .reason("Quarterly Diabetes Check & Medication Titration")
                .notes("Bring recent blood glucose log").build());

        // Booked Appointment 4 (Sophia with Dr. Marcus Chen)
        appointmentRepository.save(Appointment.builder()
                .patient(patSophia).doctor(docMarcus)
                .appointmentDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(11, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Frequent tension headaches and sleep disruption")
                .notes("Initial neurological evaluation").build());

        // Booked Appointment 5 (Olivia Taylor with Dr. Elena Rostova - Brand new patient)
        appointmentRepository.save(Appointment.builder()
                .patient(patOlivia).doctor(docElena)
                .appointmentDate(LocalDate.now().plusDays(6))
                .startTime(LocalTime.of(15, 0)).endTime(LocalTime.of(15, 30))
                .status(AppointmentStatus.BOOKED)
                .reason("Annual wellness checkup and preventive immunization review")
                .notes("New patient transfer from out of state").build());

        // Cancelled Appointment 6 (David with Dr. James Wilson)
        appointmentRepository.save(Appointment.builder()
                .patient(patDavid).doctor(docJames)
                .appointmentDate(LocalDate.now().minusDays(3))
                .startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(13, 30))
                .status(AppointmentStatus.CANCELLED)
                .reason("Knee follow-up (Cancelled due to patient travel schedule)")
                .notes("Rescheduled to next month").build());

        log.info("Demo data initialized successfully with 1 Admin, 6 Doctors across diverse specialties, 6 Patients with multi-visit clinical records, Availabilities, and Appointments.");
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
