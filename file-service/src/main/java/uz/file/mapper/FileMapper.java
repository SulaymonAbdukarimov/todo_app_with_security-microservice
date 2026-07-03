package uz.file.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.file.dto.response.FileResponse;
import uz.file.entity.FileMetadata;


@Mapper(componentModel = "spring")
public interface FileMapper {
    @Mapping(target = "downloadUrl", ignore = true)
    FileResponse toResponse(FileMetadata entity);
}
