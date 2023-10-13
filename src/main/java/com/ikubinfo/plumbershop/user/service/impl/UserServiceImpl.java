package com.ikubinfo.plumbershop.user.service.impl;

import com.ikubinfo.plumbershop.common.dto.Filter;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.mapper.UserMapper;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import com.ikubinfo.plumbershop.user.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.USER;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        UserDocument document = userRepository.save(userMapper.toUserDocument(userDto));
        return userMapper.toUserDto(document);
    }

    @Override
    public Page<UserDto> getAllUsers(Filter filter) {

        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(),
                Sort.by(Sort.Direction.valueOf(filter.getSortType()), filter.getSortBy()));

        return userRepository.findAll(pageable).map(userMapper::toUserDto);
    }

    @Override
    public UserDto getById(String id) {
        UserDocument document = findUserById(id);
        return userMapper.toUserDto(document);
    }

    @Override
    public UserDto updateById(String id, UserDto userDto) {
        UserDocument document = findUserById(id);
        UserDocument updatedUser = userRepository.save(userMapper.updateUserFromDto(userDto, document));
        return userMapper.toUserDto(updatedUser);
    }

    private UserDocument findUserById(String id) {
         return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(USER,ID, id));
    }
}
