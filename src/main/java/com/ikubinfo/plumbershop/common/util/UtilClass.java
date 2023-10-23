package com.ikubinfo.plumbershop.common.util;

import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.enums.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;
import static java.util.Arrays.stream;

public class UtilClass {

    public static <T> String getSortField(Class<T> tClass, String field) {
        return fieldExistsInClass(tClass,field) ? field : ID;
    }

    public static boolean userHasGivenRole(CustomUserDetails loggedUser, Role role) {
        return loggedUser.getAuthorities()
                .contains(new SimpleGrantedAuthority(String.valueOf(role)));
    }

    public static Authentication getAuthentication(){
        return SecurityContextHolder
                        .getContext()
                        .getAuthentication();
    }

    public static boolean userIsNotLogged(Authentication authentication){
        return  (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken);
    }

    private static <T> boolean fieldExistsInClass(Class<T> tClass, String field) {
        return stream(tClass.getDeclaredFields())
                .anyMatch(f -> f.getName().equals(field));
    }

    private UtilClass() {
    }
}
