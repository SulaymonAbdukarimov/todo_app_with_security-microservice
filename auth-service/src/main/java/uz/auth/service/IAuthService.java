package uz.auth.service;

import uz.auth.dto.request.LoginRequest;
import uz.auth.dto.request.RegisterRequest;
import uz.auth.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);
}
