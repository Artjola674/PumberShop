package com.ikubinfo.plumbershop.email;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;

public interface EmailService {

    void sendEmailWhenOrderIsCreated(OrderDocument order) throws MessagingException, IOException;

    void sendPerformanceIssueEmail(long executionTime, String methodName);

    void sendForgetPasswordEmail(String email, String token);

    void sendScheduleToEmail(String documentPath, String filename, List<String> emails);
}
