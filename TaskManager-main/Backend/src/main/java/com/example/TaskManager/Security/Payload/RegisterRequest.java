package com.example.TaskManager.Security.Payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String username;
    private String firstname;
    private String lastname;
    private String password;

    // NUEVOS CAMPOS
    private String address;   // Dirección textual
    private Double latitude;  // Latitud
    private Double longitude; // Longitud
}
