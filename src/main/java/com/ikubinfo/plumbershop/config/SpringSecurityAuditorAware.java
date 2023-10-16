package com.ikubinfo.plumbershop.config;

import com.ikubinfo.plumbershop.exception.ResourceNotFoundException;

import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ikubinfo.plumbershop.user.constants.UserConstants.USER;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.USERNAME;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<UserDocument> {

    private final UserRepository userRepository;

    @Override
    public Optional<UserDocument> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();


        UserDocument user = getUser(principal.getEmail());

        return Optional.of(user);
    }

    private UserDocument getUser(String email){
        return  userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER, USERNAME, email));
    }
}
