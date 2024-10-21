package com.ikubinfo.plumbershop.aop;

import com.ikubinfo.plumbershop.email.EmailHelper;
import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.kafka.KafkaProducer;
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

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final KafkaProducer kafkaProducer;
    private final HttpServletRequest request;

    private static final Logger logger =
            LoggerFactory.getLogger("aspectLogging");


    @Around("com.ikubinfo.plumbershop.aop.PointcutContainer.controllerPointcut()")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {

        String loggedUserId = getLoggedUserId();
        String methodName = joinPoint.getSignature().toShortString();
        String id = extractPathVariable(request.getRequestURI(), ID);
        String requestMethod = request.getMethod();

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - startTime;

        logger.info(" --> User id: {}, method name: {}, request mapping type: {}, ID: {}, execution time: {} ms",
                loggedUserId, methodName,requestMethod, id, executionTime);

        long executionInSeconds = executionTime/1000;
        if (executionInSeconds > 5){
            MessageRequest messageRequest = EmailHelper.createPerformanceIssueRequest(executionInSeconds, methodName);
            kafkaProducer.sendMessage(messageRequest);
        }

        return result;

    }

    private String extractPathVariable(String requestURI, String pathVariableValue) {
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
