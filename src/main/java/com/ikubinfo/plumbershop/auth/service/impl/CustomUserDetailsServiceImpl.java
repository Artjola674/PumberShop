package com.ikubinfo.plumbershop.auth.service.impl;

import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static com.ikubinfo.plumbershop.user.constants.UserConstants.USER;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.USERNAME;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDocument user = getUser(username);
        return CustomUserDetails.fromUserDocumentToCustomUserDetails(user);
    }

    private UserDocument getUser(String email){
        return  userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER, USERNAME, email));
    }
}
