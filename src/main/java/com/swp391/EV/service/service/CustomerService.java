package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CustomerCreateRequest;
import com.swp391.EV.service.dto.request.CustomerUpdateRequest;
import com.swp391.EV.service.dto.response.CustomerResponse;
import com.swp391.EV.service.dto.response.CustomerProfileResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<CustomerResponse> getAllCustomers(Pageable pageable, String search) {
        Page<Customer> customers = customerRepository.findCustomersWithSearch(search, pageable);

        List<CustomerResponse> customerResponses = new ArrayList<>();
        for (Customer customer : customers.getContent()) {
            CustomerResponse response = buildCustomerResponse(customer);
            customerResponses.add(response);
        }

        return new PageImpl<>(customerResponses, pageable, customers.getTotalElements());
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        // Validate required fields (no password required)
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    ErrorCode.EMPTY_CREDENTIALS.getStatusCode(),
                    "Email không được để trống"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        String password = request.getPassword() != null && !request.getPassword().isBlank()
                ? request.getPassword() 
                : generateTemporaryPassword();

        String username = request.getEmail().split("@")[0];
        
        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(password))
                .fullName(request.getFullName())
                .role("customer")
                .isActive(true)
                .emailVerified(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);

        Customer customer = Customer.builder()
                .userId(savedUser.getId())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .customerCode(request.getCustomerCode() != null ? request.getCustomerCode() : generateCustomerCode())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .totalSpent(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return buildCustomerResponse(savedCustomer);
    }

    public CustomerProfileResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return buildCustomerProfileResponse(customer);
    }

    public CustomerProfileResponse getCustomerByUserId(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return buildCustomerProfileResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
            }
            customer.setEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!request.getUsername().equals(customer.getUsername()) &&
                customerRepository.existsByUsername(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
            }
            customer.setUsername(request.getUsername());
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            customer.setAddress(request.getAddress());
        }

        if (request.getIsActive() != null) {
            customer.setActive(request.getIsActive());
        }

        if (request.getCustomerCode() != null && !request.getCustomerCode().isBlank()) {
            if (!request.getCustomerCode().equals(customer.getCustomerCode()) &&
                customerRepository.existsByCustomerCode(request.getCustomerCode())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã khách hàng đã tồn tại");
            }
            customer.setCustomerCode(request.getCustomerCode());
        }

        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getSubscriptionExpiry() != null) {
            customer.setSubscriptionExpiry(request.getSubscriptionExpiry());
        }

        customer.setUserUpdatedAt(OffsetDateTime.now());
        Customer updatedCustomer = customerRepository.save(customer);

        return buildCustomerResponse(updatedCustomer);
    }


    private CustomerResponse buildCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .role(customer.getRole())
                .isActive(customer.isActive())
                .emailVerified(customer.isEmailVerified())
                .lastLogin(customer.getLastLogin())
                .dateOfBirth(customer.getDateOfBirth())
                .customerCode(customer.getCustomerCode())
                .subscriptionExpiry(customer.getSubscriptionExpiry())
                .totalSpent(customer.getTotalSpent())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUserUpdatedAt())
                .build();
    }

    private CustomerProfileResponse buildCustomerProfileResponse(Customer customer) {
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .dateOfBirth(customer.getDateOfBirth())
                .subscriptionExpiry(customer.getSubscriptionExpiry())
                .totalSpent(customer.getTotalSpent())
                .createdAt(customer.getCreatedAt())
                .userId(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .role(customer.getRole())
                .isActive(customer.isActive())
                .emailVerified(customer.isEmailVerified())
                .lastLogin(customer.getLastLogin())
                .userCreatedAt(customer.getUserCreatedAt())
                .userUpdatedAt(customer.getUserUpdatedAt())
                .build();
    }

    private String generateCustomerCode() {
        return "CUS" + System.currentTimeMillis();
    }

    private String generateTemporaryPassword() {
        return "Temp" + System.currentTimeMillis();
    }
}
