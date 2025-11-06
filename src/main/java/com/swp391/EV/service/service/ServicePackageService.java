package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateServicePackageRequest;
import com.swp391.EV.service.dto.request.UpdateServicePackageRequest;
import com.swp391.EV.service.dto.response.ServicePackageResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.ServicePackage;
import com.swp391.EV.service.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicePackageService {

    @Autowired
    private final ServicePackageRepository servicePackageRepository;
    @Autowired
    private final ModelMapper modelMapper;

    public List<ServicePackageResponse> getAllServicePackages() {
        List<ServicePackage> servicePackages = servicePackageRepository.findAll();
        return servicePackages.stream()
                .map(sp -> modelMapper.map(sp, ServicePackageResponse.class))
                .collect(Collectors.toList());
    }

    public ServicePackageResponse getServicePackageById(UUID id) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return modelMapper.map(servicePackage, ServicePackageResponse.class);
    }

    public ServicePackageResponse createServicePackage(CreateServicePackageRequest request) {
        ServicePackage servicePackage = ServicePackage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationMinutes(request.getDurationMinutes())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        ServicePackage savedPackage = servicePackageRepository.save(servicePackage);
        return modelMapper.map(savedPackage, ServicePackageResponse.class);
    }

    public ServicePackageResponse updateServicePackage(UUID id, UpdateServicePackageRequest request) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            servicePackage.setName(request.getName());
        }
        if (request.getDescription() != null) {
            servicePackage.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            servicePackage.setPrice(request.getPrice());
        }
        if (request.getDurationMinutes() != null) {
            servicePackage.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIsActive() != null) {
            servicePackage.setIsActive(request.getIsActive());
        }

        ServicePackage updatedPackage = servicePackageRepository.save(servicePackage);
        return modelMapper.map(updatedPackage, ServicePackageResponse.class);
    }

    public void deleteServicePackage(UUID id) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Soft delete - chỉ đặt isActive = false
        servicePackage.setIsActive(false);
        servicePackageRepository.save(servicePackage);
    }
}