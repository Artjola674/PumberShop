package com.ikubinfo.plumbershop.email.service;

import com.ikubinfo.plumbershop.email.dto.OrderConfirmationRequest;
import com.ikubinfo.plumbershop.email.dto.PasswordResetRequest;
import com.ikubinfo.plumbershop.email.dto.PerformanceIssueRequest;
import com.ikubinfo.plumbershop.email.dto.ScheduleRequest;

import java.util.List;

public interface EmailService {

    void sendEmailWhenOrderIsCreated(OrderConfirmationRequest orderConfirmationRequest);

    void sendPerformanceIssueEmail(PerformanceIssueRequest performanceIssueRequest);

    void sendForgetPasswordEmail(PasswordResetRequest passwordResetRequest);

    void sendScheduleToEmail(ScheduleRequest scheduleRequest);
}
