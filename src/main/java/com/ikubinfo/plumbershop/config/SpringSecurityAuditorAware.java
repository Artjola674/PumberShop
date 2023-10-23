package com.ikubinfo.plumbershop.config;

import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<UserDocument> {

    private final UserService userService;

    @Override
    public Optional<UserDocument> getCurrentAuditor() {
        Authentication authentication = UtilClass.getAuthentication();

        if (UtilClass.userIsNotLogged(authentication)) {
            return Optional.empty();
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();


        UserDocument user = userService.getUserByEmail(principal.getEmail());

        return Optional.of(user);
    }

}
