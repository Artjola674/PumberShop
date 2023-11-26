package com.ikubinfo.plumbershop;

import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;


public class BaseTest {

    protected static final String BASE_URL = "http://localhost:8080/api/v1";
    protected static final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    protected String getTokenForSeller(){
        UserDocument user = createUserDocument(Role.SELLER, "seller@gmail.com");
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForUser(){
        UserDocument user = createUserDocument(Role.USER, "user@gmail.com");
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForPlumber(){
        UserDocument user = createUserDocument(Role.PLUMBER, "plumber@gmail.com");
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForAdmin(){
        UserDocument user = createUserDocument(Role.ADMIN, "admin@gmail.com");
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected void deleteUsers(){
        userRepository.deleteAll();
    }

    protected AuthResponse doLogin(String email ){
        AuthRequest authRequest= AuthRequest.builder()
                .email(email)
                .password("password")
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, headers);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                BASE_URL + "/auth/login", entity, AuthResponse.class);
        return response.getBody();
    }

    protected HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + token);
        return headers;
    }


    private UserDocument createUserDocument(Role role, String email) {
        UserDocument user = new UserDocument();
        user.setFirstName("Artjola");
        user.setLastName("Kotorri");
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("password"));
        return user;
    }

}
