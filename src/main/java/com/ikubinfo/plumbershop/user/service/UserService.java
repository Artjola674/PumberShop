package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.springframework.data.domain.Page;

public interface UserService {


    UserDto saveUser(UserDto userDto);

    Page<UserDto> getAllUsers(Filter filter);

    UserDto getById(String id, CustomUserDetails loggedUser);

    UserDto updateById(String id, UserDto userDto);

    String deleteById(String id);

    UserDocument getUserByEmail(String email);


}
