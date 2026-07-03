package uz.auth.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.auth.dto.response.ApiResponse;
import uz.auth.dto.response.UserResponse;

import uz.auth.service.IUserService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private  final IUserService userService;

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded", userService.uploadAvatar(file,authHeader)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable @NotNull Long id,
            @RequestHeader("Authorization") String authHeader) {
            return ResponseEntity.ok(ApiResponse.success("Successful",this.userService.getUserById(id, authHeader)));
    };
}