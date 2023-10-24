package com.ikubinfo.plumbershop.aop;

import com.ikubinfo.plumbershop.common.service.EmailService;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final EmailService emailService;

    private static final Logger logger =
            LoggerFactory.getLogger("aspectLogging");


    @Around("com.ikubinfo.plumbershop.aop.PointcutContainer.controllerPointcut()")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {

        String loggedUserId = getLoggedUserId();
        String methodName = joinPoint.getSignature().toShortString();
        String id = null;
        String requestMethod = null;

        HttpServletRequest request = getCurrentHttpRequest();

        if (request != null) {
            String requestURI = request.getRequestURI();
            id = extractPathVariable(requestURI, ID);
            requestMethod = request.getMethod();
        }


        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - startTime;

        logger.info(" --> User id: {}, method name: {}, request mapping type: {}, ID: {}, execution time: {} ms",
                loggedUserId, methodName,requestMethod, id, executionTime);

        long executionInSeconds = executionTime/1000;
        if (executionInSeconds > 5){
            emailService.sendPerformanceIssueEmail(executionInSeconds,methodName);
        }

        return result;

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
        Authentication authentication = UtilClass.getAuthentication();
        if (UtilClass.userIsNotLogged(authentication)) {
            return "anonymousUser";
        }
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getId();
    }

}
