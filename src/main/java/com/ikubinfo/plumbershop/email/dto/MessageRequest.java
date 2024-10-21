package com.ikubinfo.plumbershop.email.dto;

import com.ikubinfo.plumbershop.email.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private MessageType messageType;
    private OrderConfirmationRequest orderConfirmationRequest;
    private PerformanceIssueRequest performanceIssueRequest;
    private PasswordResetRequest passwordResetRequest;
    private ScheduleRequest scheduleRequest;
}
