package com.swp391.EV.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/v1/files")
@Slf4j
@CrossOrigin(origins = "*")
public class FileUploadController {

    // Thư mục lưu file upload (có thể config trong application.yaml)
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * Upload một file ảnh
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được vượt quá 5MB"));
            }

            // Check file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên file không hợp lệ"));
            }

            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Chỉ chấp nhận file ảnh: " + String.join(", ", ALLOWED_EXTENSIONS)
                ));
            }

            // Tạo tên file unique
            String uniqueFilename = generateUniqueFilename(originalFilename);

            // Tạo thư mục upload nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully: {}", uniqueFilename);

            // Trả về URL của file
            String fileUrl = "/api/v1/files/" + uniqueFilename;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filename", uniqueFilename,
                "url", fileUrl,
                "size", file.getSize()
            ));

        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi upload file: " + e.getMessage()));
        }
    }

    /**
     * Upload nhiều file ảnh
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                ResponseEntity<?> result = uploadFile(file);
                if (result.getStatusCode() == HttpStatus.OK) {
                    uploadedFiles   .add((Map<String, Object>) result.getBody());
                } else {
                    errors.add(file.getOriginalFilename() + ": " + result.getBody());
                }
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
            "uploadedFiles", uploadedFiles,
            "errors", errors,
            "totalSuccess", uploadedFiles.size(),
            "totalFailed", errors.size()
        ));
    }

    /**
     * Lấy file đã upload
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileContent);

        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Xóa file
     */
    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Files.delete(filePath);
            log.info("File deleted successfully: {}", filename);

            return ResponseEntity.ok(Map.of("success", true, "message", "File đã được xóa"));

        } catch (IOException e) {
            log.error("Error deleting file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa file: " + e.getMessage()));
        }
    }

    // Helper methods
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String generateUniqueFilename(String originalFilename) {
        String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        
        // Clean filename: remove special characters
        nameWithoutExt = nameWithoutExt.replaceAll("[^a-zA-Z0-9-_]", "_");
        
        return String.format("%s_%s_%s.%s", nameWithoutExt, timestamp, random, extension);
    }
}
