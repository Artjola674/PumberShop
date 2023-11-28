package com.ikubinfo.plumbershop.user.service;

import com.ikubinfo.plumbershop.common.dto.PageParams;
import com.ikubinfo.plumbershop.email.EmailService;
import com.ikubinfo.plumbershop.exception.BadRequestException;
import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.user.dto.ChangePasswordDto;
import com.ikubinfo.plumbershop.user.dto.ResetPasswordDto;
import com.ikubinfo.plumbershop.user.dto.UserDto;
import com.ikubinfo.plumbershop.user.dto.UserRequest;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.mapper.UserMapper;
import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import com.ikubinfo.plumbershop.user.service.impl.UserServiceImpl;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.*;
import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.security.CustomUserDetails.fromUserDocumentToCustomUserDetails;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService underTest;

    @Mock
    private UserRepository userRepository;
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private ResetTokenService resetTokenService;

    @BeforeEach
    void setUp() {
        underTest = new UserServiceImpl(userRepository,passwordEncoder,emailService,resetTokenService);
    }

    @Test
    void givenUser_whenSaveUser_thenReturnUser() {
        UserDto userDto = createUserDto();

        given(userRepository.existsByEmail(userDto.getEmail())).willReturn(false);
        UserDocument userDocument = userMapper.toUserDocument(userDto);
        given(userRepository.save(any(UserDocument.class))).willReturn(userDocument);

        UserDto result = underTest.saveUser(userDto, null);

        assertThat(result).isEqualTo(userDto);

    }

    @Test
    void givenUser_whenSaveUser_thenThrowExceptionCanNotAccess() {
        UserDto userDto = createUserDto();

        UserDocument userDocument = userMapper.toUserDocument(userDto);

        given(userRepository.existsByEmail(userDto.getEmail())).willReturn(false);

        assertThatThrownBy(() ->underTest.saveUser(userDto, fromUserDocumentToCustomUserDetails(userDocument)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ACTION_NOT_ALLOWED);

        verify(userRepository, never()).save(any(UserDocument.class));

    }

    @Test
    void givenUser_whenSaveUser_thenThrowExceptionEmailExists() {
        UserDto userDto = createUserDto();

        given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);

        assertThatThrownBy(() ->underTest.saveUser(userDto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EMAIL_EXISTS + userDto.getEmail());

        verify(userRepository, never()).save(any(UserDocument.class));

    }

    @Test
    void getAllUsers_success() {
        UserDocument user = createUserDocument();
        UserRequest userRequest = new UserRequest();
        userRequest.setPageParams(new PageParams());
        userRequest.setEmail(user.getEmail());

        Page<UserDocument> mockedPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(BooleanExpression.class), any(Pageable.class)))
                .thenReturn(mockedPage);

        Page<UserDto> result = underTest.getAllUsers(userRequest);

        assertThat(result.getContent().get(0).getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getById_success() {
        UserDto userDto = createUserDto();
        userDto.setId("1");
        UserDocument userDocument = userMapper.toUserDocument(userDto);

        given(userRepository.findById(userDto.getId())).willReturn(Optional.of(userDocument));

        UserDto result = underTest.getById(userDto.getId(), fromUserDocumentToCustomUserDetails(userDocument));

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void getById_throwException_NotFound() {
        UserDto userDto = createUserDto();
        userDto.setId("1");
        UserDocument userDocument = userMapper.toUserDocument(userDto);

        given(userRepository.findById(userDto.getId())).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getById(userDto.getId(), fromUserDocumentToCustomUserDetails(userDocument)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",USER,ID, userDto.getId());
    }

    @Test
    void getById_throwException_CanNotAccess() {
        UserDto userDto = createUserDto();
        userDto.setId("1");
        UserDocument userDocument = userMapper.toUserDocument(userDto);
        userDocument.setId("2");

        given(userRepository.findById(userDto.getId())).willReturn(Optional.of(userDocument));

        assertThatThrownBy(() ->underTest.getById(userDto.getId(), fromUserDocumentToCustomUserDetails(userDocument)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ACTION_NOT_ALLOWED);
    }


    @Test
    void updateById_success() {
        UserDto userDto = createUserDto();
        UserDocument userDocument = createUserDocument();
        userDocument.setId("1");

        given(userRepository.findById(userDocument.getId())).willReturn(Optional.of(userDocument));

        UserDocument updatedUser = userMapper.updateUserFromDto(userDto, userDocument);

        given(userRepository.save(updatedUser)).willReturn(updatedUser);

        UserDto result = underTest.updateById(userDocument.getId(), userDto, fromUserDocumentToCustomUserDetails(userDocument));

        assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(result.getRole()).isNotEqualTo(userDto.getRole());
    }

    @Test
    void updateById_throwException_NotFound() {
        UserDto userDto = createUserDto();
        UserDocument userDocument = createUserDocument();
        userDocument.setId("1");

        given(userRepository.findById(userDocument.getId())).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.updateById(userDocument.getId(), userDto,
                fromUserDocumentToCustomUserDetails(userDocument)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",USER,ID, userDocument.getId());
    }

    @Test
    void updateById_throwException_EmailExists() {
        UserDto userDto = createUserDto();
        UserDocument userDocument = createUserDocument();
        userDocument.setId("1");

        given(userRepository.findById(userDocument.getId())).willReturn(Optional.of(userDocument));

        given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);

        assertThatThrownBy(() ->underTest.updateById(userDocument.getId(), userDto,
                fromUserDocumentToCustomUserDetails(userDocument)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(EMAIL_EXISTS + userDto.getEmail());
    }

    @Test
    void deleteById_success() {

        String result = underTest.deleteById("1");

        verify(userRepository).deleteById("1");

        assertThat(result).isEqualTo(DELETED_SUCCESSFULLY.replace(DOCUMENT,USER));

    }

    @Test
    void getUserByEmail_success() {

        UserDocument user = createUserDocument();

        given(userRepository.findUserByEmail(user.getEmail())).willReturn(Optional.of(user));

        UserDocument result = underTest.getUserByEmail(user.getEmail());

        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getUserByEmail_throwException_NotFound() {

        UserDocument user = createUserDocument();

        given(userRepository.findUserByEmail(user.getEmail())).willReturn(Optional.ofNullable(null));

        assertThatThrownBy(() ->underTest.getUserByEmail(user.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("%s not found with %s : '%s' ",USER,USERNAME, user.getEmail());
    }

    @Test
    void changePassword_success() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("oldPass", "newPass");
        UserDocument user = createUserDocument();
        user.setId("1");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(changePasswordDto.getOldPassword(),user.getPassword())).willReturn(true);
        given(userRepository.save(user)).willReturn(user);

        String result = underTest.changePassword(changePasswordDto, fromUserDocumentToCustomUserDetails(user), user.getId());

        assertThat(result).isEqualTo(PASS_CHANGED_SUCCESSFULLY);

    }

    @Test
    void changePassword_throwException_CanNotAccess() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("oldPass", "newPass");
        UserDocument user = createUserDocument();
        user.setId("1");

        assertThatThrownBy(() ->underTest.changePassword(changePasswordDto, fromUserDocumentToCustomUserDetails(user), "2"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ACTION_NOT_ALLOWED);

    }

    @Test
    void changePassword_throwException_passDoesNotMatch() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("oldPass", "newPass");
        UserDocument user = createUserDocument();
        user.setId("1");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(changePasswordDto.getOldPassword(),user.getPassword())).willReturn(false);


        assertThatThrownBy(() ->underTest.changePassword(changePasswordDto, fromUserDocumentToCustomUserDetails(user), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(PASS_NOT_CORRECT);


    }


    @Test
    void forgetPassword_success() {

        UserDocument userDocument = createUserDocument();

        given(userRepository.findUserByEmail(userDocument.getEmail()))
                .willReturn(Optional.of(userDocument));

        String result = underTest.forgetPassword(userDocument.getEmail());

        verify(resetTokenService).createPasswordResetTokenForUser(any(UserDocument.class), anyString());
        verify(emailService).sendForgetPasswordEmail(eq(userDocument.getEmail()),anyString());

        assertThat(result).isEqualTo(FORGET_PASS);
    }

    @Test
    void resetPassword_success() {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("1234","1234");
        UserDocument user = createUserDocument();
        ResetTokenDocument resetTokenDocument = createResetToken(user);

        given(resetTokenService.verifyToken(resetTokenDocument.getToken())).willReturn(resetTokenDocument);

        String result = underTest.resetPassword(resetPasswordDto, resetTokenDocument.getToken());

        verify(userRepository).save(user);

        assertThat(result).isEqualTo(PASS_CHANGED_SUCCESSFULLY);
    }

    @Test
    void resetPassword_throwException_passNotMatch() {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("1234","12345");

        assertThatThrownBy(() ->underTest.resetPassword(resetPasswordDto, "token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(PASSWORD_NOT_MATCH);
    }

    @Test
    void getAllUsersBasedOnRole_success() {
        Role role = Role.SELLER;
        underTest.getAllUsersBasedOnRole(role);

        ArgumentCaptor<Role> roleArgumentCaptor =
                ArgumentCaptor.forClass(Role.class);

        verify(userRepository).findAllByRole(roleArgumentCaptor.capture());

        Role capturedOrder = roleArgumentCaptor.getValue();

        assertThat(capturedOrder).isEqualTo(role);

    }

    private UserDto createUserDto() {
        UserDto userDto = new UserDto();
        userDto.setFirstName("Artjola");
        userDto.setLastName("Kotorri");
        userDto.setEmail("artjolakotorri@gmail.com");
        userDto.setRole(String.valueOf(Role.PLUMBER));
        userDto.setPassword("A@a2345678");
       return userDto;
    }

    private UserDocument createUserDocument() {
        UserDocument userDocument = new UserDocument();
        userDocument.setFirstName("Artjola1");
        userDocument.setLastName("Kotorri1");
        userDocument.setEmail("artjolakotorri1@gmail.com");
        userDocument.setRole(Role.ADMIN);
        userDocument.setPassword("1A@a2345678");
        return userDocument;
    }

    private ResetTokenDocument createResetToken(UserDocument user) {
        return ResetTokenDocument.builder()
                .token("123456789")
                .expirationDate(new Date())
                .user(user)
                .build();
    }
}