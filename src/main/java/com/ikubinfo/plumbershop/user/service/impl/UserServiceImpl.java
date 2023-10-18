package com.ikubinfo.plumbershop.user.service.impl;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.mapper.UserMapper;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.ACTION_NOT_ALLOWED;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Override
    public UserDto saveUser(UserDto userDto, CustomUserDetails loggedUser) {

        if (loggedUser != null && !isAdmin(loggedUser)) {
            throw new BadRequestException(ACTION_NOT_ALLOWED);
        }

        if (userRepository.existsByEmail(userDto.getEmail())){
            throw new BadRequestException(EMAIL_EXISTS + userDto.getEmail());
        }
        UserDocument document = userMapper.toUserDocument(userDto);

        if (loggedUser == null){
            document.setRole(Role.USER);
        }

        document.setPassword(passwordEncoder.encode(document.getPassword()));
        UserDocument savedUser = userRepository.save(document);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    public Page<UserDto> getAllUsers(Filter filter) {

        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(),
                Sort.by(Sort.Direction.valueOf(filter.getSortType()),
                        UtilClass.getSortField(UserDocument.class, filter.getSortBy())));

        return userRepository.findAll(pageable).map(userMapper::toUserDto);
    }

    @Override
    public UserDto getById(String id, CustomUserDetails loggedUser) {
        checkIfUserCanPerformAction(id, loggedUser);

        UserDocument document = findUserById(id);
        return userMapper.toUserDto(document);
    }

    @Override
    public UserDto updateById(String id, UserDto userDto, CustomUserDetails loggedUser) {
        checkIfUserCanPerformAction(id, loggedUser);
        UserDocument document = findUserById(id);
        if (!userDto.getEmail().equalsIgnoreCase(document.getEmail())
                && userRepository.existsByEmail(userDto.getEmail())){
            throw new BadRequestException(EMAIL_EXISTS + userDto.getEmail());
        }
        UserDocument updatedUser = userRepository.save(userMapper.updateUserFromDto(userDto, document));
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public String deleteById(String id) {
        userRepository.deleteById(id);
        return DELETED_SUCCESSFULLY.replace(DOCUMENT,USER);
    }

    @Override
    public UserDocument getUserByEmail(String email){
        return  userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER, USERNAME, email));
    }

    private UserDocument findUserById(String id) {
         return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(USER,ID, id));
    }

    private void checkIfUserCanPerformAction(String id, CustomUserDetails loggedUser) {
        if (!isAdmin(loggedUser)
                && !loggedUser.getId().equals(id)){
            throw new BadRequestException(ACTION_NOT_ALLOWED);
        }
    }

    private boolean isAdmin(CustomUserDetails loggedUser) {
        return loggedUser.getAuthorities()
                .contains(new SimpleGrantedAuthority(String.valueOf(Role.ADMIN)));
    }

}
