package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateServiceCenterRequest;
import com.swp391.EV.service.dto.request.UpdateServiceCenterRequest;
import com.swp391.EV.service.dto.response.ServiceCenterResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.ServiceCenter;
import com.swp391.EV.service.repository.ServiceCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ServiceCenterService {

    private final ServiceCenterRepository serviceCenterRepository;

    public List<ServiceCenterResponse> getAllServiceCenters() {
        return serviceCenterRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceCenterResponse createServiceCenter(CreateServiceCenterRequest request) {
        // Kiểm tra tên trung tâm đã tồn tại chưa
        if (serviceCenterRepository.findByNameAndIsActiveTrue(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED); // Có thể tạo ErrorCode mới cho SERVICE_CENTER_EXISTED
        }

        ServiceCenter serviceCenter = ServiceCenter.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .operatingHours(request.getOperatingHours())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 10)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceCenter savedServiceCenter = serviceCenterRepository.save(serviceCenter);
        return convertToResponse(savedServiceCenter);
    }

    public ServiceCenterResponse getServiceCenterById(UUID id) {
        ServiceCenter serviceCenter = serviceCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return convertToResponse(serviceCenter);
    }

    @Transactional
    public ServiceCenterResponse updateServiceCenter(UUID id, UpdateServiceCenterRequest request) {
        ServiceCenter serviceCenter = serviceCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getName() != null) {
            serviceCenter.setName(request.getName());
        }
        if (request.getAddress() != null) {
            serviceCenter.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            serviceCenter.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            serviceCenter.setEmail(request.getEmail());
        }
        if (request.getOperatingHours() != null) {
            serviceCenter.setOperatingHours(request.getOperatingHours());
        }
        if (request.getCapacity() != null) {
            serviceCenter.setCapacity(request.getCapacity());
        }
        if (request.getIsActive() != null) {
            serviceCenter.setIsActive(request.getIsActive());
        }

        serviceCenter.setUpdatedAt(LocalDateTime.now());
        ServiceCenter updatedServiceCenter = serviceCenterRepository.save(serviceCenter);
        return convertToResponse(updatedServiceCenter);
    }

    @Transactional
    public void deleteServiceCenter(UUID id) {
        ServiceCenter serviceCenter = serviceCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Soft delete
        serviceCenter.setIsActive(false);
        serviceCenter.setUpdatedAt(LocalDateTime.now());
        serviceCenterRepository.save(serviceCenter);
    }

    public List<ServiceCenterResponse> searchServiceCenters(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllServiceCenters();
        }

        List<ServiceCenter> centersByName = serviceCenterRepository.findByNameContainingAndActiveTrue(keyword);
        List<ServiceCenter> centersByAddress = serviceCenterRepository.findByAddressContainingAndActiveTrue(keyword);

        // Kết hợp kết quả và loại bỏ trùng lặp
        return Stream.concat(centersByName.stream(), centersByAddress.stream())
                .distinct()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ServiceCenterResponse convertToResponse(ServiceCenter serviceCenter) {
        ServiceCenterResponse response = new ServiceCenterResponse();
        response.setId(serviceCenter.getId());
        response.setName(serviceCenter.getName());
        response.setAddress(serviceCenter.getAddress());
        response.setPhone(serviceCenter.getPhone());
        response.setEmail(serviceCenter.getEmail());
        response.setOperatingHours(serviceCenter.getOperatingHours());
        response.setCapacity(serviceCenter.getCapacity());
        response.setIsActive(serviceCenter.getIsActive());
        response.setCreatedAt(serviceCenter.getCreatedAt());
        response.setUpdatedAt(serviceCenter.getUpdatedAt());
        return response;
    }
}
