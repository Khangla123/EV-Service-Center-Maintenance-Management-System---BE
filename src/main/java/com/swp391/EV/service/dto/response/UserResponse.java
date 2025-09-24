package com.swp391.EV.service.dto.response;

import com.swp391.EV.service.entity.UserRole;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private boolean isActive;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserResponse() {}
    
    public UserResponse(Long id, String email, String firstName, String lastName, String phone, 
                       UserRole role, boolean isActive, String avatar, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Builder method (static factory)
    public static UserResponse builder() {
        return new UserResponse();
    }
    
    public UserResponse id(Long id) {
        this.id = id;
        return this;
    }
    
    public UserResponse email(String email) {
        this.email = email;
        return this;
    }
    
    public UserResponse firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public UserResponse lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public UserResponse phone(String phone) {
        this.phone = phone;
        return this;
    }
    
    public UserResponse role(UserRole role) {
        this.role = role;
        return this;
    }
    
    public UserResponse isActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    public UserResponse avatar(String avatar) {
        this.avatar = avatar;
        return this;
    }
    
    public UserResponse createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public UserResponse updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public UserResponse build() {
        return this;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}