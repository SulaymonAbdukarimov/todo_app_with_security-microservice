package uz.auth.service;

import jakarta.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;
import uz.auth.dto.response.UserResponse;

import java.io.IOException;

public interface IUserService {
    UserResponse getUserById(@NotNull Long id, String authHeader);

    String uploadAvatar(MultipartFile file,String authHeader) throws IOException;
}
