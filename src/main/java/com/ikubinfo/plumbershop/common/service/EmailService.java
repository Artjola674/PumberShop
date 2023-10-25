package com.ikubinfo.plumbershop.common.service;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface EmailService {

    void sendEmailWhenOrderIsCreated(OrderDocument order) throws MessagingException, IOException;

    void sendPerformanceIssueEmail(long executionTime, String methodName);

    void sendForgetPasswordEmail(String email, String token);
}
