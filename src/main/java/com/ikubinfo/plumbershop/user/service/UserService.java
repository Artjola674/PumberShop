package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {


    UserDto saveUser(UserDto userDto);

    Page<UserDto> getAllUsers(Filter filter);
}
