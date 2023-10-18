package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.springframework.data.domain.Page;

public interface UserService {


    UserDto saveUser(UserDto userDto, CustomUserDetails loggedUser);

    Page<UserDto> getAllUsers( UserRequest userRequest);

    UserDto getById(String id, CustomUserDetails loggedUser);

    UserDto updateById(String id, UserDto userDto, CustomUserDetails loggedUser);

    String deleteById(String id);

    UserDocument getUserByEmail(String email);


}
