package com.swp391.EV.service.controller;

import com.swp391.EV.service.dto.ApiResponse;
import com.swp391.EV.service.dto.request.CreatePartRequest;
import com.swp391.EV.service.dto.request.RestockPartRequest;
import com.swp391.EV.service.dto.request.UpdatePartRequest;
import com.swp391.EV.service.dto.response.PartResponse;
import com.swp391.EV.service.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "Parts Inventory Management APIs")
public class PartController {

    private final PartService partService;

    @GetMapping
    @Operation(summary = "Danh sách phụ tùng", description = "Inventory management - Get all parts")
    public ApiResponse<List<PartResponse>> getAllParts() {
        List<PartResponse> responses = partService.getAllParts();
        return ApiResponse.<List<PartResponse>>builder()
                .message("Danh sách phụ tùng.")
                .result(responses)
                .build();
    }

    @PostMapping
    @Operation(summary = "Thêm phụ tùng mới", description = "Part info - Add new part to inventory (Admin only)")
    public ApiResponse<PartResponse> createPart(@RequestBody CreatePartRequest request) {
        PartResponse response = partService.createPart(request);
        return ApiResponse.<PartResponse>builder()
                .message("Thêm phụ tùng thành công.")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết phụ tùng", description = "Part details - Get part information by ID")
    public ApiResponse<PartResponse> getPartById(@PathVariable UUID id) {
        PartResponse response = partService.getPartById(id);
        return ApiResponse.<PartResponse>builder()
                .message("Chi tiết phụ tùng.")
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phụ tùng", description = "Stock/price update - Update part information (Staff/Admin)")
    public ApiResponse<PartResponse> updatePart(
            @PathVariable UUID id,
            @RequestBody UpdatePartRequest request) {
        PartResponse response = partService.updatePart(id, request);
        return ApiResponse.<PartResponse>builder()
                .message("Cập nhật phụ tùng thành công.")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phụ tùng", description = "Soft delete - Deactivate part (Admin only)")
    public ApiResponse<Void> deletePart(@PathVariable UUID id) {
        partService.deletePart(id);
        return ApiResponse.<Void>builder()
                .message("Xóa phụ tùng thành công.")
                .build();
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Phụ tùng sắp hết", description = "Below min level - Get parts with low stock")
    public ApiResponse<List<PartResponse>> getLowStockParts() {
        List<PartResponse> responses = partService.getLowStockParts();
        return ApiResponse.<List<PartResponse>>builder()
                .message("Danh sách phụ tùng sắp hết.")
                .result(responses)
                .build();
    }

    @PostMapping("/{id}/restock")
    @Operation(summary = "Nhập kho phụ tùng", description = "Stock adjustment - Add stock quantity (Staff/Admin)")
    public ApiResponse<PartResponse> restockPart(
            @PathVariable UUID id,
            @RequestBody RestockPartRequest request) {
        PartResponse response = partService.restockPart(id, request);
        return ApiResponse.<PartResponse>builder()
                .message("Nhập kho thành công.")
                .result(response)
                .build();
    }
}
