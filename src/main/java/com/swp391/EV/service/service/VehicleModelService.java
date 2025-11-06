package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateVehicleModelRequest;
import com.swp391.EV.service.dto.request.UpdateVehicleModelRequest;
import com.swp391.EV.service.dto.response.VehicleModelResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.VehicleModel;
import com.swp391.EV.service.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public List<VehicleModelResponse> getAllVehicleModels() {
        return vehicleModelRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleModelResponse createVehicleModel(CreateVehicleModelRequest request) {
        // Kiểm tra xem model này đã tồn tại chưa
        vehicleModelRepository.findByManufacturerAndModelAndYear(
                request.getManufacturer(),
                request.getModel(),
                request.getYear()
        ).ifPresent(vm -> {
            throw new AppException(ErrorCode.USER_EXISTED);
        });

        VehicleModel vehicleModel = VehicleModel.builder()
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .year(request.getYear())
                .batteryCapacity(request.getBatteryCapacity())
                .rangeKm(request.getRangeKm())
                .createdAt(LocalDateTime.now())
                .build();

        VehicleModel saved = vehicleModelRepository.save(vehicleModel);
        return convertToResponse(saved);
    }

    public VehicleModelResponse getVehicleModelById(UUID id) {
        VehicleModel vehicleModel = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return convertToResponse(vehicleModel);
    }

    @Transactional
    public VehicleModelResponse updateVehicleModel(UUID id, UpdateVehicleModelRequest request) {
        VehicleModel vehicleModel = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getManufacturer() != null) {
            vehicleModel.setManufacturer(request.getManufacturer());
        }
        if (request.getModel() != null) {
            vehicleModel.setModel(request.getModel());
        }
        if (request.getYear() != null) {
            vehicleModel.setYear(request.getYear());
        }
        if (request.getBatteryCapacity() != null) {
            vehicleModel.setBatteryCapacity(request.getBatteryCapacity());
        }
        if (request.getRangeKm() != null) {
            vehicleModel.setRangeKm(request.getRangeKm());
        }

        VehicleModel updated = vehicleModelRepository.save(vehicleModel);
        return convertToResponse(updated);
    }

    @Transactional
    public void deleteVehicleModel(UUID id) {
        VehicleModel vehicleModel = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        vehicleModelRepository.delete(vehicleModel);
    }

    private VehicleModelResponse convertToResponse(VehicleModel vehicleModel) {
        VehicleModelResponse response = new VehicleModelResponse();
        response.setId(vehicleModel.getId());
        response.setManufacturer(vehicleModel.getManufacturer());
        response.setModel(vehicleModel.getModel());
        response.setYear(vehicleModel.getYear());
        response.setBatteryCapacity(vehicleModel.getBatteryCapacity());
        response.setRangeKm(vehicleModel.getRangeKm());
        response.setCreatedAt(vehicleModel.getCreatedAt());
        return response;
    }
}

