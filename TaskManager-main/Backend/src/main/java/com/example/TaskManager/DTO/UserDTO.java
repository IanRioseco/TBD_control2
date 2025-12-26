package com.example.TaskManager.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String role;
    private String address;
    private Double latitude;
    private Double longitude;
}