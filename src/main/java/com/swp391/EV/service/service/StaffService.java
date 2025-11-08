package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateStaffRequest;
import com.swp391.EV.service.dto.request.UpdateStaffProfileRequest;
import com.swp391.EV.service.dto.request.UpdateStaffRequest;
import com.swp391.EV.service.dto.response.StaffResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.ServiceCenter;
import com.swp391.EV.service.model.Staff;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.ServiceCenterRepository;
import com.swp391.EV.service.repository.ServiceAppointmentRepository;
import com.swp391.EV.service.repository.StaffRepository;
import com.swp391.EV.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final ServiceAppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Validate and normalize role to lowercase
        String role = request.getRole().toLowerCase();
        if (!role.equals("staff") && !role.equals("technician")) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        // Validate service center
        ServiceCenter serviceCenter = null;
        if (request.getServiceCenterId() != null) {
            serviceCenter = serviceCenterRepository.findById(request.getServiceCenterId())
                    .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CENTER_NOT_FOUND));
        }

        // Create User
        User user = User.builder()
                .username(request.getEmail()) // Use email as username
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(role)
                .isActive(true)
                .emailVerified(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Generate staff code
        String staffCode = generateStaffCode(role);

        // Create Staff
        Staff staff = Staff.builder()
                .user(user)
                .serviceCenter(serviceCenter)
                .staffCode(staffCode)
                .specialization(request.getSpecialization())
                .hireDate(request.getHireDate())
                .salary(request.getSalary())
                .isAvailable(true)
                .build();

        staff = staffRepository.save(staff);

        return mapToResponse(staff);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        List<Staff> staffList = staffRepository.findAllStaffAndTechnicians();
        return staffList.stream()
                .map(this::mapToResponse)
                .filter(response -> response != null) // Filter out null responses (staff without user)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToResponse(staff);
    }

    @Transactional
    public StaffResponse updateStaff(UUID id, UpdateStaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = staff.getUser();

        // Update User info
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Update Staff info
        if (request.getServiceCenterId() != null) {
            ServiceCenter serviceCenter = serviceCenterRepository.findById(request.getServiceCenterId())
                    .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CENTER_NOT_FOUND));
            staff.setServiceCenter(serviceCenter);
        }
        if (request.getSpecialization() != null) {
            staff.setSpecialization(request.getSpecialization());
        }
        if (request.getHireDate() != null) {
            staff.setHireDate(request.getHireDate());
        }
        if (request.getSalary() != null) {
            staff.setSalary(request.getSalary());
        }

        staff = staffRepository.save(staff);

        return mapToResponse(staff);
    }

    @Transactional
    public void deleteStaff(UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = staff.getUser();
        user.setActive(false);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getAvailableStaff() {
        List<Staff> staffList = staffRepository.findByIsAvailable(true);
        return staffList.stream()
                .filter(staff -> staff.getUser().isActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffResponse getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Staff staff = staffRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return mapToResponse(staff);
    }

    @Transactional
    public StaffResponse updateMyProfile(UpdateStaffProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Staff staff = staffRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Update User info
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Update Staff specialization
        if (request.getSpecialization() != null) {
            staff.setSpecialization(request.getSpecialization());
        }
        staff = staffRepository.save(staff);

        return mapToResponse(staff);
    }

    @Transactional(readOnly = true)
    public boolean isStaffAvailable(UUID staffId, LocalDateTime appointmentDate) {
        System.out.println("Checking availability for staff: " + staffId);
        System.out.println("Appointment date: " + appointmentDate);
        
        long conflictCount = appointmentRepository.countConflictingAppointments(
                staffId,
                appointmentDate
        );
        
        System.out.println("Conflicting appointments count: " + conflictCount);
        boolean isAvailable = conflictCount == 0;
        System.out.println("Is available: " + isAvailable);
        
        return isAvailable;
    }

    private String generateStaffCode(String role) {
        String prefix = role.equals("technician") ? "TECH" : "STAFF";
        long count = staffRepository.count() + 1;
        return String.format("%s%05d", prefix, count);
    }

    private StaffResponse mapToResponse(Staff staff) {
        User user = staff.getUser();
        
        // Safety check - nếu staff không có user thì skip (data corrupt)
        if (user == null) {
            System.err.println("⚠️ WARNING: Staff " + staff.getId() + " has no associated User! Skipping...");
            return null;
        }
        
        // Calculate current real-time status
        String currentStatus = calculateCurrentStatus(staff);
        
        return StaffResponse.builder()
                .id(staff.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .staffCode(staff.getStaffCode())
                .serviceCenterId(staff.getServiceCenter() != null ? staff.getServiceCenter().getId() : null)
                .serviceCenterName(staff.getServiceCenter() != null ? staff.getServiceCenter().getName() : null)
                .specialization(staff.getSpecialization())
                .hireDate(staff.getHireDate())
                .salary(staff.getSalary())
                .isAvailable(staff.getIsAvailable())
                .isActive(user.isActive())
                .currentStatus(currentStatus)
                .createdAt(staff.getCreatedAt())
                .build();
    }
    
    private String calculateCurrentStatus(Staff staff) {
        // Check if user is active
        if (!staff.getUser().isActive()) {
            return "INACTIVE";
        }
        
        // Check if staff has any active appointments in the current time window (±1 hour)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);
        
        long activeAppointments = appointmentRepository.findAll().stream()
                .filter(appointment -> {
                    if (appointment.getTechnician() == null) return false;
                    if (!appointment.getTechnician().getId().equals(staff.getId())) return false;
                    
                    String status = appointment.getStatus().name();
                    if (status.equals("CANCELLED") || status.equals("COMPLETED")) return false;
                    
                    LocalDateTime apptDate = appointment.getAppointmentDate();
                    // Check if appointment is within ±1 hour window
                    return !apptDate.isBefore(oneHourAgo) && !apptDate.isAfter(oneHourLater);
                })
                .count();
        
        return activeAppointments > 0 ? "BUSY" : "AVAILABLE";
    }
}
