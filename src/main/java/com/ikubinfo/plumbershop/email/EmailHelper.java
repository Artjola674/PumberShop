package com.ikubinfo.plumbershop.email;

import com.ikubinfo.plumbershop.email.dto.*;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public class EmailHelper {

    public static MessageRequest createOrderConfirmationEmailRequest(OrderDocument order) {
        OrderConfirmationRequest orderConfirmationRequest = new OrderConfirmationRequest(
                order.getBill().getFileName(),
                order.getBill().getFileLocation(),
                order.getCustomer());

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessageType(MessageType.ORDER_CONFIRMATION);
        messageRequest.setOrderConfirmationRequest(orderConfirmationRequest);
        return messageRequest;
    }

    public static MessageRequest createPerformanceIssueRequest(long executionInSeconds, String methodName) {
        PerformanceIssueRequest performanceIssueRequest = new PerformanceIssueRequest(executionInSeconds,methodName);

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessageType(MessageType.PERFORMANCE_ISSUE);
        messageRequest.setPerformanceIssueRequest(performanceIssueRequest);
        return messageRequest;
    }

    public static MessageRequest createPasswordResetRequest(String email, String token) {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email, token,
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessageType(MessageType.PASSWORD_RESET);
        messageRequest.setPasswordResetRequest(passwordResetRequest);
        return messageRequest;
    }

    public static MessageRequest createScheduleRequest(String documentPath, String filename, List<String> emails) {
        ScheduleRequest scheduleRequest = new ScheduleRequest(documentPath, filename, emails);

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessageType(MessageType.SCHEDULE);
        messageRequest.setScheduleRequest(scheduleRequest);
        return messageRequest;
    }
}
