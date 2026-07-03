package uz.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.common.enums.FileCategory;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private UUID id;
    private String originalFilename;
    private String contentType;
    private long size;
    private FileCategory category;
    private String downloadUrl;
    private String imageUrl;
}