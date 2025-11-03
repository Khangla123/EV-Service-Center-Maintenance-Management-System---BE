package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreatePartRequest;
import com.swp391.EV.service.dto.request.RestockPartRequest;
import com.swp391.EV.service.dto.request.UpdatePartRequest;
import com.swp391.EV.service.dto.response.PartResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Part;
import com.swp391.EV.service.model.ServiceCenter;
import com.swp391.EV.service.repository.PartRepository;
import com.swp391.EV.service.repository.ServiceCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final ServiceCenterRepository serviceCenterRepository;

    @Transactional
    public PartResponse createPart(CreatePartRequest request) {
        // Check if part code already exists
        if (partRepository.findByPartCode(request.getPartCode()).isPresent()) {
            throw new AppException(ErrorCode.PART_CODE_EXISTED);
        }

        // Verify service center exists
        ServiceCenter serviceCenter = serviceCenterRepository.findById(request.getServiceCenterId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CENTER_NOT_FOUND));

        Part part = Part.builder()
                .serviceCenter(serviceCenter)
                .partCode(request.getPartCode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .unitPrice(request.getUnitPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .minStockLevel(request.getMinStockLevel() != null ? request.getMinStockLevel() : 0)
                .supplier(request.getSupplier())
                .isActive(true)
                .build();

        part = partRepository.save(part);
        return mapToResponse(part);
    }

    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts() {
        return partRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PartResponse getPartById(UUID id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
        return mapToResponse(part);
    }

    @Transactional
    public PartResponse updatePart(UUID id, UpdatePartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));

        if (request.getName() != null) {
            part.setName(request.getName());
        }
        if (request.getDescription() != null) {
            part.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            part.setCategory(request.getCategory());
        }
        if (request.getUnitPrice() != null) {
            part.setUnitPrice(request.getUnitPrice());
        }
        if (request.getMinStockLevel() != null) {
            part.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getSupplier() != null) {
            part.setSupplier(request.getSupplier());
        }

        part = partRepository.save(part);
        return mapToResponse(part);
    }

    @Transactional
    public void deletePart(UUID id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
        part.setIsActive(false);
        partRepository.save(part);
    }

    public List<PartResponse> getLowStockParts() {
        return partRepository.findLowStockParts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PartResponse restockPart(UUID id, RestockPartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        part.setStockQuantity(part.getStockQuantity() + request.getQuantity());
        part = partRepository.save(part);
        return mapToResponse(part);
    }

    private PartResponse mapToResponse(Part part) {
        return PartResponse.builder()
                .id(part.getId())
                .serviceCenterId(part.getServiceCenter() != null ? part.getServiceCenter().getId() : null)
                .serviceCenterName(part.getServiceCenter() != null ? part.getServiceCenter().getName() : null)
                .partCode(part.getPartCode())
                .name(part.getName())
                .description(part.getDescription())
                .category(part.getCategory())
                .unitPrice(part.getUnitPrice())
                .stockQuantity(part.getStockQuantity())
                .minStockLevel(part.getMinStockLevel())
                .supplier(part.getSupplier())
                .isActive(part.getIsActive())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }
}

