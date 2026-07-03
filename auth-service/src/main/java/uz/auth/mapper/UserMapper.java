package uz.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.auth.dto.response.UserResponse;
import uz.auth.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "file", ignore = true)
    UserResponse toResponse(User entity);
}
