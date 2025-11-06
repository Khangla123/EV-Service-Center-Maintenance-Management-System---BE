package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.model.ServiceAppointment;
import com.swp391.EV.service.model.Staff;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.ServiceAppointmentRepository;
import com.swp391.EV.service.repository.StaffRepository;
import com.swp391.EV.service.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Tag(name = "Debug", description = "Debug endpoints để kiểm tra data")
public class DebugController {

    @Autowired
    private final ServiceAppointmentRepository appointmentRepository;
    
    @Autowired
    private final StaffRepository staffRepository;
    
    @Autowired
    private final UserRepository userRepository;

    @GetMapping("/appointments")
    @Operation(summary = "Debug: Tất cả appointments")
    public ApiResponse<Map<String, Object>> debugAppointments() {
        List<ServiceAppointment> allAppointments = appointmentRepository.findAll();
        List<ServiceAppointment> appointmentsWithTechnician = appointmentRepository.findAll()
                .stream()
                .filter(apt -> apt.getTechnician() != null)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalAppointments", allAppointments.size());
        result.put("appointmentsWithTechnician", appointmentsWithTechnician.size());
        result.put("appointments", allAppointments.stream().limit(5).map(apt -> {
            Map<String, Object> aptInfo = new HashMap<>();
            aptInfo.put("id", apt.getId());
            aptInfo.put("status", apt.getStatus());
            aptInfo.put("appointmentDate", apt.getAppointmentDate());
            aptInfo.put("technicianId", apt.getTechnician() != null ? apt.getTechnician().getId() : null);
            aptInfo.put("customerName", apt.getCustomer() != null ? apt.getCustomer().getFullName() : null);
            return aptInfo;
        }).toList());
        
        return ApiResponse.<Map<String, Object>>builder()
                .message("Debug appointments data")
                .result(result)
                .build();
    }

    @GetMapping("/staff")
    @Operation(summary = "Debug: Tất cả staff")
    public ApiResponse<Map<String, Object>> debugStaff() {
        List<Staff> allStaff = staffRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalStaff", allStaff.size());
        result.put("staff", allStaff.stream().map(staff -> {
            Map<String, Object> staffInfo = new HashMap<>();
            staffInfo.put("id", staff.getId());
            staffInfo.put("userId", staff.getUser() != null ? staff.getUser().getId() : null);
            staffInfo.put("fullName", staff.getUser() != null ? staff.getUser().getFullName() : null);
            staffInfo.put("email", staff.getUser() != null ? staff.getUser().getEmail() : null);
            staffInfo.put("role", staff.getUser() != null ? staff.getUser().getRole() : null);
            return staffInfo;
        }).toList());
        
        return ApiResponse.<Map<String, Object>>builder()
                .message("Debug staff data")
                .result(result)
                .build();
    }

    @GetMapping("/technician-user")
    @Operation(summary = "Debug: Technician user")
    public ApiResponse<Map<String, Object>> debugTechnicianUser() {
        User technicianUser = userRepository.findByEmail("technician@evservice.vn").orElse(null);
        Staff technicianStaff = null;
        
        if (technicianUser != null) {
            technicianStaff = staffRepository.findByUserId(technicianUser.getId()).orElse(null);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userExists", technicianUser != null);
        if (technicianUser != null) {
            result.put("userId", technicianUser.getId());
            result.put("fullName", technicianUser.getFullName());
            result.put("email", technicianUser.getEmail());
            result.put("role", technicianUser.getRole());
        }
        
        result.put("staffExists", technicianStaff != null);
        if (technicianStaff != null) {
            result.put("staffId", technicianStaff.getId());
        }
        
        return ApiResponse.<Map<String, Object>>builder()
                .message("Debug technician user data")
                .result(result)
                .build();
    }
}