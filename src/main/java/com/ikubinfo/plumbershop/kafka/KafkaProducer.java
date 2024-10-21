package com.ikubinfo.plumbershop.kafka;

import com.ikubinfo.plumbershop.email.dto.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    public void sendMessage(MessageRequest messageRequest) {

        log.info(String.format("Message sent -> %s", messageRequest.toString()));

        Message<MessageRequest> message = MessageBuilder
                .withPayload(messageRequest)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .build();

        kafkaTemplate.send(message);

    }

}
