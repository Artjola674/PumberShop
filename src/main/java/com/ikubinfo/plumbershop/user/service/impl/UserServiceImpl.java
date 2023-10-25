package com.ikubinfo.plumbershop.user.service.impl;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.ChangePasswordDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.mapper.UserMapper;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.*;
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

        if (loggedUser != null && !UtilClass.userHasGivenRole(loggedUser,Role.ADMIN)) {
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
    public Page<UserDto> getAllUsers( UserRequest userRequest) {
        Filter filter = userRequest.getFilter();

        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(),
                Sort.by(Sort.Direction.valueOf(filter.getSortType()),
                        UtilClass.getSortField(UserDocument.class, filter.getSortBy())));

//        UserDocument example = UserDocument.builder().email(userRequest.getEmail()).build();
//        ExampleMatcher exampleMatcher = ExampleMatcher
//                .matchingAny()
//                .withMatcher("email",match->match.startsWith());
//        Page<UserDocument> page = userRequest.getEmail() != null
//        ? userRepository.findAll(Example.of(example,exampleMatcher),pageable)
//        : userRepository.findAll(pageable);

        Page<UserDocument> page = userRequest.getEmail() != null
                ? userRepository.findPageByEmailStartingWith(userRequest.getEmail(), pageable)
                : userRepository.findAll(pageable);


        return page.map(userMapper::toUserDto);
    }

    @Override
    public UserDto getById(String id, CustomUserDetails loggedUser) {
        checkIfUserCanAccessUserData(id, loggedUser);

        UserDocument document = findUserById(id);
        return userMapper.toUserDto(document);
    }

    @Override
    public UserDto updateById(String id, UserDto userDto, CustomUserDetails loggedUser) {
        checkIfUserCanAccessUserData(id, loggedUser);
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

    @Override
    public String changePassword(ChangePasswordDto changePasswordDto, CustomUserDetails loggedUser, String userId) {
        if (!userId.equals(loggedUser.getId())){
            throw new BadRequestException(ACTION_NOT_ALLOWED);
        }
        UserDocument user = findUserById(userId);
        validateOldPassword(changePasswordDto.getOldPassword(),user.getPassword());
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);

        return null;
    }

    private void validateOldPassword(String oldPassword, String encodedPass) {
        if (!passwordEncoder.matches(oldPassword,encodedPass)){
            throw new BadRequestException(PASS_NOT_CORRECT);
        }
    }

    private UserDocument findUserById(String id) {
         return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(USER,ID, id));
    }

    private void checkIfUserCanAccessUserData(String id, CustomUserDetails loggedUser) {
        if (!UtilClass.userHasGivenRole(loggedUser,Role.ADMIN)
                && !loggedUser.getId().equals(id)){
            throw new BadRequestException(ACTION_NOT_ALLOWED);
        }
    }



}
