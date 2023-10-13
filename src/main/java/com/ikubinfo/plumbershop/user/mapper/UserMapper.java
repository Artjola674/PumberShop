package com.ikubinfo.plumbershop.user.mapper;

import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserMapper {

    UserDto toUserDto(UserDocument document);

    UserDocument toUserDocument(UserDto dto);

    UserDocument updateUserFromDto(UserDto dto, @MappingTarget UserDocument document);
}
