package com.swp391.EV.service.service;

import com.swp391.EV.service.dto.request.CreateVehicleRequest;
import com.swp391.EV.service.dto.request.UpdateVehicleRequest;
import com.swp391.EV.service.dto.response.VehicleResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.Customer;
import com.swp391.EV.service.model.Vehicle;
import com.swp391.EV.service.model.VehicleModel;
import com.swp391.EV.service.repository.CustomerRepository;
import com.swp391.EV.service.repository.VehicleRepository;
import com.swp391.EV.service.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    @Autowired
    private final VehicleRepository vehicleRepository;
    @Autowired
    private final CustomerRepository customerRepository;
    @Autowired
    private final VehicleModelRepository vehicleModelRepository;

    // Lấy danh sách xe của khách hàng (không phải loại xe)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // CUSTOMER tự đăng ký xe - lấy customerId từ user đang đăng nhập
    @Transactional
    public VehicleResponse registerMyVehicle(UUID currentUserId, CreateVehicleRequest request) {
        // Tìm customer từ userId
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra VIN đã tồn tại chưa
        if (vehicleRepository.findByVin(request.getVin()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Kiểm tra biển số đã tồn tại chưa
        if (request.getLicensePlate() != null &&
            vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Tìm VehicleModel nếu có vehicleModelId
        VehicleModel vehicleModel = null;
        if (request.getVehicleModelId() != null) {
            vehicleModel = vehicleModelRepository.findById(request.getVehicleModelId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .vehicleModel(vehicleModel)
                .vin(request.getVin())
                .licensePlate(request.getLicensePlate())
                .color(request.getColor())
                .purchaseDate(request.getPurchaseDate())
                .mileage(request.getMileage() != null ? request.getMileage() : 0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(savedVehicle);
    }

    // STAFF/ADMIN thêm xe cho khách hàng cụ thể
    @Transactional
    public VehicleResponse registerVehicleForCustomer(UUID customerId, CreateVehicleRequest request) {
        // Kiểm tra customer tồn tại
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra VIN đã tồn tại chưa
        if (vehicleRepository.findByVin(request.getVin()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Kiểm tra biển số đã tồn tại chưa
        if (request.getLicensePlate() != null &&
            vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Tìm VehicleModel nếu có vehicleModelId
        VehicleModel vehicleModel = null;
        if (request.getVehicleModelId() != null) {
            vehicleModel = vehicleModelRepository.findById(request.getVehicleModelId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .vehicleModel(vehicleModel)
                .vin(request.getVin())
                .licensePlate(request.getLicensePlate())
                .color(request.getColor())
                .purchaseDate(request.getPurchaseDate())
                .mileage(request.getMileage() != null ? request.getMileage() : 0)
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
        if (request.getVehicleModelId() != null) {
            VehicleModel vehicleModel = vehicleModelRepository.findById(request.getVehicleModelId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            vehicle.setVehicleModel(vehicleModel);
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

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(UUID customerId) {
        List<Vehicle> vehicles = vehicleRepository.findActiveVehiclesByCustomerId(customerId);
        
        return vehicles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // CUSTOMER xem xe của mình
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(UUID currentUserId) {
        if (currentUserId == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        return getVehiclesByCustomerId(customer.getId());
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());

        // Customer info
        if (vehicle.getCustomer() != null) {
            response.setCustomerId(vehicle.getCustomer().getId());
            response.setCustomerName(vehicle.getCustomer().getFullName());
        }

        // VehicleModel info
        if (vehicle.getVehicleModel() != null) {
            response.setVehicleModelId(vehicle.getVehicleModel().getId());
            response.setManufacturer(vehicle.getVehicleModel().getManufacturer());
            response.setModel(vehicle.getVehicleModel().getModel());
            response.setYear(vehicle.getVehicleModel().getYear());
            response.setBatteryCapacity(vehicle.getVehicleModel().getBatteryCapacity());
            response.setRangeKm(vehicle.getVehicleModel().getRangeKm());
        }

        response.setVin(vehicle.getVin());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setColor(vehicle.getColor());
        response.setPurchaseDate(vehicle.getPurchaseDate());
        response.setWarrantyExpiration(vehicle.getWarrantyExpiration());
        response.setMileage(vehicle.getMileage());
        response.setLastMaintenanceDate(vehicle.getLastMaintenanceDate());
        response.setNextMaintenanceDate(vehicle.getNextMaintenanceDate());
        response.setIsActive(vehicle.getIsActive());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }
}
