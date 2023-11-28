package com.ikubinfo.plumbershop;

import com.ikubinfo.plumbershop.auth.dto.AuthRequest;
import com.ikubinfo.plumbershop.auth.dto.AuthResponse;
import com.ikubinfo.plumbershop.user.dto.Address;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;


public class BaseTest {

    protected static final String BASE_URL = "http://localhost:8080/api/v1";
    protected static final String PASSWORD = "Password@123";
    protected static final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    protected String getTokenForSeller(){
        UserDocument user = createUserDocument("seller@gmail.com", Role.SELLER);
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForUser(){
        UserDocument user = createUserDocument("user@gmail.com", Role.USER);
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForPlumber(){
        UserDocument user = createUserDocument("plumber@gmail.com", Role.PLUMBER);
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected String getTokenForAdmin(){
        UserDocument user = createUserDocument("admin@gmail.com", Role.ADMIN);
        userRepository.save(user);
        return doLogin(user.getEmail()).getAccessToken();
    }

    protected void deleteUsers(){
        userRepository.deleteAll();
    }

    protected AuthResponse doLogin(String email ){
        AuthRequest authRequest= createAuthRequest(email,PASSWORD);

        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                BASE_URL + "/auth/login", entity, AuthResponse.class);
        return response.getBody();
    }

    protected AuthRequest createAuthRequest(String email, String password) {
        return AuthRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    protected HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + token);
        return headers;
    }


    protected UserDocument createUserDocument(String email, Role role) {
        UserDocument user = new UserDocument();
        user.setFirstName("Artjola");
        user.setLastName("Kotorri");
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setAddress(createAddress());
        return user;
    }

    private Address createAddress(){
        Address address = new Address();
        address.setCity("city");
        address.setStreet("street");
        address.setPostalCode(1001);
        return address;
    }

}
