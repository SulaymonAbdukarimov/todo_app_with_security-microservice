package uz.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {
    private UUID id;
    private String originalFilename;
    private String contentType;
    private long size;
    private String category;
    private String downloadUrl;
}