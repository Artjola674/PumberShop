package com.ikubinfo.plumbershop.config;

import com.ikubinfo.plumbershop.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger("aspectLogging");


    @Before("com.ikubinfo.plumbershop.aop.PointcutContainer.beforeControllerPointcut()")
    public void beforeController(JoinPoint joinPoint)  {



        String loggedUserId = getLoggedUserId();
        String id = null;
        String requestMethod = null;

        HttpServletRequest request = getCurrentHttpRequest();

        if (request != null) {
            String requestURI = request.getRequestURI();
            id = extractPathVariable(requestURI, ID);
            requestMethod = request.getMethod();
        }

        String methodName = joinPoint.getSignature().getName();

        String logMessage = String.format(
                " --> User id: '%s', method name: '%s', request mapping type: '%s', ID: '%s'",
                loggedUserId, methodName,requestMethod, id
        );
        logger.info(logMessage);

    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if ( attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }

        return null;
    }

    private String extractPathVariable(String requestURI, String pathVariableValue) {
        // Split the request URI and extract the value directly
        String[] uriSegments = requestURI.split("/");
        for (int i = 0; i < uriSegments.length-1; i++) {
            if (uriSegments[i].equals(pathVariableValue)) {
                return uriSegments[i + 1];
            }
        }
        return null;
    }

    private String getLoggedUserId(){
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return "anonymousUser";
        }else {
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            return principal.getId();
        }
    }

}
