package com.ikubinfo.plumbershop.user.mapper;

import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDto toUserDto(UserDocument document);

    UserDocument toUserDocument(UserDto dto);

    default UserDocument updateUserFromDto(UserDto dto, UserDocument document){
        if ( dto == null ) {
            return document;
        }

        document.setCreatedDate( dto.getCreatedDate() );
        document.setLastModifiedDate( dto.getLastModifiedDate() );
        document.setCreatedBy( dto.getCreatedBy() );
        document.setLastModifiedBy( dto.getLastModifiedBy() );
        document.setFirstName( dto.getFirstName() );
        document.setLastName( dto.getLastName() );
        document.setEmail( dto.getEmail() );

        if ( dto.getDepartment() != null ) {
            document.setDepartment( Enum.valueOf( Department.class, dto.getDepartment() ) );
        }
        else {
            document.setDepartment( null );
        }
        document.setPhone( dto.getPhone() );

        return document;
    }
}
