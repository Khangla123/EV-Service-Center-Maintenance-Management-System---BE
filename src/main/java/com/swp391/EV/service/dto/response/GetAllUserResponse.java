package com.swp391.EV.service.dto.response;

import lombok.Builder;
import lombok.Data;

import javax.xml.transform.sax.SAXResult;
import java.util.UUID;

@Data
@Builder
public class GetAllUserResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role;
    private String createdAt;


}
