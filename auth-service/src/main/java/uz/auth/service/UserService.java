package uz.auth.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uz.auth.dto.response.ApiResponse;
import uz.auth.dto.response.FileUploadResponse;
import uz.auth.dto.response.UserResponse;
import uz.auth.entity.User;
import uz.auth.exception.UserNotFoundException;
import uz.auth.mapper.UserMapper;
import uz.auth.repository.UserRepository;
import uz.common.enums.FileCategory;
import java.io.IOException;


@Service
@RequiredArgsConstructor

public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final RestTemplate restTemplate;


    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public UserResponse getUserById(@NotNull Long id, String authHeader) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found !!!"));
        UserResponse userResponse = mapper.toResponse(user);

        if (user.getAvatarFileId() != null) {
            FileUploadResponse fileUploadResponse = fetchFile(user.getAvatarFileId(), authHeader);
            if (fileUploadResponse != null) {
                userResponse.setFile(fileUploadResponse);
            }
        }

        return userResponse;
    }

    private FileUploadResponse fetchFile(String fileId, String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);

            ResponseEntity<ApiResponse<FileUploadResponse>> response = restTemplate.exchange(
                    "http://FILE-SERVICE/api/v1/files/internal/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ApiResponse<FileUploadResponse>>() {},
                    fileId
            );
            ApiResponse<FileUploadResponse> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            System.out.println("FILE-SERVICE dan fayl olishda xatolik, fileId={}: {}");
            return null;
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, String authHeader) throws IOException {

        User user = getCurrentUser();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("category", FileCategory.AVATAR.name());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ApiResponse<FileUploadResponse>> response = restTemplate.exchange(
                "http://FILE-SERVICE/api/v1/files",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });

        FileUploadResponse fileResponse = response.getBody().getData();
        user.setAvatarFileId(fileResponse.getId().toString());
        userRepository.save(user);

        return fileResponse.getDownloadUrl();
    }


}
