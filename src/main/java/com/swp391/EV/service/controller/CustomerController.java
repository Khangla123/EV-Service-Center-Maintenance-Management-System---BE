package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CustomerCreateRequest;
import com.swp391.EV.service.dto.request.CustomerUpdateRequest;
import com.swp391.EV.service.dto.response.CustomerResponse;
import com.swp391.EV.service.dto.response.CustomerProfileResponse;
import com.swp391.EV.service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    ApiResponse<Page<CustomerResponse>> getAllCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponse> customers = customerService.getAllCustomers(pageable, search);
        return ApiResponse.<Page<CustomerResponse>>builder()
                .message("Danh sách khách hàng")
                .result(customers)
                .build();
    }

    @PostMapping
    ApiResponse<CustomerResponse> createCustomer(@RequestBody CustomerCreateRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return ApiResponse.<CustomerResponse>builder()
                .message("Tạo khách hàng mới thành công")
                .result(customer)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CustomerProfileResponse> getCustomerById(@PathVariable UUID id) {
        CustomerProfileResponse customer = customerService.getCustomerById(id);
        return ApiResponse.<CustomerProfileResponse>builder()
                .message("Chi tiết khách hàng")
                .result(customer)
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<CustomerResponse> updateCustomer(@PathVariable UUID id, @RequestBody CustomerUpdateRequest request) {
        CustomerResponse customer = customerService.updateCustomer(id, request);
        return ApiResponse.<CustomerResponse>builder()
                .message("Cập nhật khách hàng thành công")
                .result(customer)
                .build();
    }

    @GetMapping("/me")
    ApiResponse<CustomerProfileResponse> getMyProfile() {
        CustomerProfileResponse profile = customerService.getMyProfile();
        return ApiResponse.<CustomerProfileResponse>builder()
                .message("Hồ sơ của tôi")
                .result(profile)
                .build();
    }

    @PutMapping("/me")
    ApiResponse<CustomerResponse> updateMyProfile(@RequestBody CustomerUpdateRequest request) {
        CustomerResponse customer = customerService.updateMyProfile(request);
        return ApiResponse.<CustomerResponse>builder()
                .message("Cập nhật hồ sơ thành công")
                .result(customer)
                .build();
    }
}
