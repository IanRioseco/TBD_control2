package com.example.TaskManager.Security.Auth;

import com.example.TaskManager.Entity.Role;
import com.example.TaskManager.Entity.User;
import com.example.TaskManager.Repository.UserRepository;
import com.example.TaskManager.Security.Jwt.JwtService;
import com.example.TaskManager.Security.Payload.JwtResponse;
import com.example.TaskManager.Security.Payload.LoginRequest;
import com.example.TaskManager.Security.Payload.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    public JwtResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.getToken(user);

        return JwtResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name()) // <--- AGREGADO: Envía el rol al frontend
                .build();
    }

    public JwtResponse register(RegisterRequest request) {

        Point location = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            location = geometryFactory.createPoint(
                    new Coordinate(request.getLongitude(), request.getLatitude())
            );
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .role(Role.USER)
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .location(location)
                .build();

        repository.save(user);

        return JwtResponse.builder()
                .token(jwtService.getToken(user))
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name()) // <--- AGREGADO
                .build();
    }
}