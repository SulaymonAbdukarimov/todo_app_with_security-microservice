package uz.file.controller;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.common.enums.FileCategory;
import uz.file.dto.response.ApiResponse;
import uz.file.dto.response.FileResponse;
import uz.file.entity.FileMetadata;
import uz.file.exception.FileAccessDeniedException;
import uz.file.exception.ResourceNotFoundException;
import uz.file.mapper.FileMapper;
import uz.file.repository.FileMetadataRepository;
import uz.file.security.AuthenticatedUser;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMapper mapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private AuthenticatedUser getCurrentUser() {
        return (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") FileCategory category,
            @RequestParam(value = "linkedId", required = false) Long linkedId) throws Exception {

        AuthenticatedUser user = getCurrentUser();
        String objectKey = "%s/%s/%s".formatted(category.name().toLowerCase(), user.id(), UUID.randomUUID());

        try (var inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build());
        }

        FileMetadata metadata = FileMetadata.builder()
                .ownerId(user.id())
                .category(category)
                .linkedId(linkedId)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .bucket(bucketName)
                .objectKey(objectKey)
                .build();

        fileMetadataRepository.save(metadata);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded", mapToResponse(metadata)));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable @NotNull UUID id) throws Exception {
        AuthenticatedUser user = getCurrentUser();
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (!metadata.getOwnerId().equals(user.id())) {
            throw new FileAccessDeniedException("You do not have access to this file");
        }

        GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket(metadata.getBucket())
                .object(metadata.getObjectKey())
                .build());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(metadata.getOriginalFilename()).build().toString())
                .body(new InputStreamResource(object));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<InputStreamResource> viewImage(@PathVariable @NotNull UUID id) throws Exception {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (metadata.getContentType() == null || !metadata.getContentType().startsWith("image/")) {
            throw new FileAccessDeniedException("This file is not a publicly viewable image");
        }

        GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket(metadata.getBucket())
                .object(metadata.getObjectKey())
                .build());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(new InputStreamResource(object));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @NotNull UUID id) throws Exception {
        AuthenticatedUser user = getCurrentUser();
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (!metadata.getOwnerId().equals(user.id())) {
            throw new FileAccessDeniedException("You do not have access to this file");
        }

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(metadata.getBucket())
                .object(metadata.getObjectKey())
                .build());
        fileMetadataRepository.delete(metadata);

        return ResponseEntity.ok(ApiResponse.success("File deleted", null));
    }

    private FileResponse mapToResponse(FileMetadata metadata) {
        boolean isImage = metadata.getContentType() != null && metadata.getContentType().startsWith("image/");
        return FileResponse.builder()
                .id(metadata.getId())
                .originalFilename(metadata.getOriginalFilename())
                .contentType(metadata.getContentType())
                .size(metadata.getSize())
                .category(metadata.getCategory())
                .downloadUrl("/api/v1/files/download/" + metadata.getId())
                .imageUrl(isImage ? "/api/v1/files/public/" + metadata.getId() : null)
                .build();
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getOne(@PathVariable @NotNull UUID id) {
        AuthenticatedUser user = getCurrentUser();
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (!metadata.getOwnerId().equals(user.id())) {
            throw new FileAccessDeniedException("You do not have access to this file");
        }

        return ResponseEntity.ok(ApiResponse.success("File found:",mapToResponse(metadata)));
    }


}