package com.ikubinfo.plumbershop.user.service.impl;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.email.EmailHelper;
import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.kafka.KafkaProducer;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.ChangePasswordDto;
import com.ikubinfo.plumbershop.user.dto.ResetPasswordDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.enums.Department;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.mapper.UserMapper;
import com.ikubinfo.plumbershop.user.model.QUserDocument;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import com.ikubinfo.plumbershop.user.service.ResetTokenService;
import com.ikubinfo.plumbershop.user.service.UserService;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.*;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducer kafkaProducer;
    private final ResetTokenService resetTokenService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, KafkaProducer kafkaProducer, ResetTokenService resetTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducer = kafkaProducer;
        this.resetTokenService = resetTokenService;
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
        PageParams pageParams = userRequest.getPageParams();

        Pageable pageable = PageRequest.of(pageParams.getPageNumber(), pageParams.getPageSize(),
                Sort.by(Sort.Direction.valueOf(pageParams.getSortType()),
                        UtilClass.getSortField(UserDocument.class, pageParams.getSortBy())));

        QUserDocument qUser = new QUserDocument(USER_DOCUMENT);
        BooleanExpression predicate = hasEmail(userRequest.getEmail(), qUser)
                .and(hasDepartment(userRequest.getDepartment(),qUser))
                .and(hasRole(userRequest.getRole(),qUser));

        return userRepository.findAll(predicate,pageable).map(userMapper::toUserDto);
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

        return PASS_CHANGED_SUCCESSFULLY;
    }

    @Override
    public String forgetPassword(String email) {
        UserDocument user = getUserByEmail(email);

        String token = UUID.randomUUID().toString();

        resetTokenService.createPasswordResetTokenForUser(user, token);

        MessageRequest messageRequest = EmailHelper.createPasswordResetRequest(email, token);
        kafkaProducer.sendMessage(messageRequest);

        return FORGET_PASS;
    }

    @Override
    public String resetPassword(ResetPasswordDto resetPasswordDto, String token) {
        checkIfPasswordsMatch(resetPasswordDto);
        ResetTokenDocument resetTokenDocument = resetTokenService.verifyToken(token);
        UserDocument user = resetTokenDocument.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user);
        return PASS_CHANGED_SUCCESSFULLY;
    }

    @Override
    public List<UserDocument> getAllUsersBasedOnRole(Role role) {
        return userRepository.findAllByRole(role);
    }

    private void checkIfPasswordsMatch(ResetPasswordDto resetPasswordDto) {
        if (!resetPasswordDto.getConfirmPassword().equals(resetPasswordDto.getNewPassword())){
            throw new BadRequestException(PASSWORD_NOT_MATCH);
        }

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

    private BooleanExpression hasEmail(String email, QUserDocument qUser){
        return email == null
                ? qUser.id.isNotNull()
                : qUser.email.likeIgnoreCase(email);
    }

    private BooleanExpression hasDepartment(String department, QUserDocument qUser){
        return department == null
                ? qUser.id.isNotNull()
                : qUser.department.eq(Enum.valueOf(Department.class,department));
    }

    private BooleanExpression hasRole(String role, QUserDocument qUser){
        return role == null
                ? qUser.id.isNotNull()
                : qUser.role.eq(Enum.valueOf( Role.class, role ));
    }

}
