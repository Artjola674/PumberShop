package com.ikubinfo.plumbershop.kafka;

import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import com.ikubinfo.plumbershop.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(MessageRequest messageRequest){
        log.info(String.format("Message received -> %s", messageRequest.toString()));

        switch (messageRequest.getMessageType()) {
            case ORDER_CONFIRMATION:
                emailService.sendEmailWhenOrderIsCreated(messageRequest.getOrderConfirmationRequest());
                break;
            case PERFORMANCE_ISSUE:
                emailService.sendPerformanceIssueEmail(messageRequest.getPerformanceIssueRequest());
                break;
            case PASSWORD_RESET:
                emailService.sendForgetPasswordEmail(messageRequest.getPasswordResetRequest());
                break;
            case SCHEDULE:
                emailService.sendScheduleToEmail(messageRequest.getScheduleRequest());
                break;
        }
    }
}
