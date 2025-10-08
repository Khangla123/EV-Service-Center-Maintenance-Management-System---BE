package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateVehicleRequest;
import com.swp391.EV.service.dto.request.UpdateVehicleRequest;
import com.swp391.EV.service.dto.response.VehicleResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.Vehicle;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        // Kiểm tra customer tồn tại
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra VIN đã tồn tại chưa
        if (vehicleRepository.findByVin(request.getVin()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED); // Có thể tạo ErrorCode mới cho VIN_EXISTED
        }

        // Kiểm tra biển số đã tồn tại chưa
        if (request.getLicensePlate() != null &&
            vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED); // Có thể tạo ErrorCode mới cho LICENSE_PLATE_EXISTED
        }

        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .vin(request.getVin())
                .licensePlate(request.getLicensePlate())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .purchaseDate(request.getPurchaseDate())
                .mileage(request.getMileage() != null ? request.getMileage() : 0)
                .batteryCapacity(request.getBatteryCapacity())
                .rangeKm(request.getRangeKm())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(savedVehicle);
    }

    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return convertToResponse(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getLicensePlate() != null) {
            vehicle.setLicensePlate(request.getLicensePlate());
        }
        if (request.getManufacturer() != null) {
            vehicle.setManufacturer(request.getManufacturer());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }
        if (request.getPurchaseDate() != null) {
            vehicle.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getMileage() != null) {
            vehicle.setMileage(request.getMileage());
        }
        if (request.getLastMaintenanceDate() != null) {
            vehicle.setLastMaintenanceDate(request.getLastMaintenanceDate());
        }
        if (request.getNextMaintenanceDate() != null) {
            vehicle.setNextMaintenanceDate(request.getNextMaintenanceDate());
        }
        if (request.getBatteryCapacity() != null) {
            vehicle.setBatteryCapacity(request.getBatteryCapacity());
        }
        if (request.getRangeKm() != null) {
            vehicle.setRangeKm(request.getRangeKm());
        }

        vehicle.setUpdatedAt(LocalDateTime.now());
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(updatedVehicle);
    }

    @Transactional
    public void deleteVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Soft delete
        vehicle.setIsActive(false);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);
    }

    public List<VehicleResponse> getVehiclesByCustomerId(UUID customerId) {
        return vehicleRepository.findActiveVehiclesByCustomerId(customerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleResponse> getMyVehicles(UUID customerId) {
        return getVehiclesByCustomerId(customerId);
    }

    @Transactional
    public VehicleResponse registerVehicleForCustomer(UUID customerId, CreateVehicleRequest request) {
        request.setCustomerId(customerId);
        return createVehicle(request);
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setCustomerId(vehicle.getCustomer().getId());
        response.setCustomerName(vehicle.getCustomer().getFullName());
        response.setVin(vehicle.getVin());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setManufacturer(vehicle.getManufacturer());
        response.setModel(vehicle.getModel());
        response.setYear(vehicle.getYear());
        response.setColor(vehicle.getColor());
        response.setPurchaseDate(vehicle.getPurchaseDate());
        response.setMileage(vehicle.getMileage());
        response.setLastMaintenanceDate(vehicle.getLastMaintenanceDate());
        response.setNextMaintenanceDate(vehicle.getNextMaintenanceDate());
        response.setBatteryCapacity(vehicle.getBatteryCapacity());
        response.setRangeKm(vehicle.getRangeKm());
        response.setIsActive(vehicle.getIsActive());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }
}
