package com.ikubinfo.plumbershop.config;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UserDocument> {
    @Override
    public Optional<UserDocument> getCurrentAuditor() {
        UserDocument document = new UserDocument();
        document.setId("11111111111111111111111111111111111111111");
        return Optional.of(document);
    }
}
