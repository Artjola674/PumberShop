package com.ikubinfo.plumbershop.security;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private String id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

    public static CustomUserDetails fromUserDocumentToCustomUserDetails(UserDocument userDocument) {
        CustomUserDetails c = new CustomUserDetails();
        c.id = userDocument.getId();
        c.email = userDocument.getEmail();
        c.password = userDocument.getPassword();
        c.grantedAuthorities = Collections
                .singletonList(new SimpleGrantedAuthority(String.valueOf(userDocument.getRole())));
        return c;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
