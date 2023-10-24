package com.ikubinfo.plumbershop.common.service.impl;

import com.ikubinfo.plumbershop.common.service.EmailService;

import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.ikubinfo.plumbershop.common.constants.Constants.COMPANY_EMAIL;
import static com.ikubinfo.plumbershop.common.constants.Constants.COMPANY_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    @Override
    public void sendEmailWhenOrderIsCreated(OrderDocument order) throws MessagingException, IOException {

           String emailContent = buildOrderCreatedBody(order.getCustomer());

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = createMessageHelper(order.getCustomer().getEmail(),
                    emailContent,"Order Confirmation", message);

            addAttachment(order.getBill().getFileLocation()+ order.getBill().getFileName(),
                    order.getBill().getFileName(), helper);
            mailSender.send(message);

    }

    @Override
    public void sendPerformanceIssueEmail(long executionTime, String methodName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            String emailContent = buildPerformanceIssueBody(methodName, executionTime);
            createMessageHelper(COMPANY_EMAIL,
                    emailContent,"API Performance Alert", message);
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private String buildOrderCreatedBody(UserDocument customer) throws IOException {

        String emailContent = getEmailContentFromFile("create-order.txt");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("FIRST_NAME", customer.getFirstName());
        placeholders.put("LAST_NAME", customer.getLastName());
        placeholders.put("COMPANY_NAME", COMPANY_NAME);

        emailContent =  replacePlaceholders(placeholders,emailContent);
        return emailContent;

    }

    private String buildPerformanceIssueBody(String methodName, long executionTime) throws IOException {

        String emailContent = getEmailContentFromFile("performance-issue.txt");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("METHOD_NAME", methodName);
        placeholders.put("EXECUTION_TIME", Long.toString(executionTime));

        emailContent =  replacePlaceholders(placeholders,emailContent);
        return emailContent;

    }

    private MimeMessageHelper createMessageHelper(String email, String emailContent,
                                                  String subject, MimeMessage message) throws MessagingException {

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(emailContent);
        return helper;

    }

    private void addAttachment(String filePath, String filename, MimeMessageHelper helper) throws MessagingException {

        FileSystemResource file = new FileSystemResource(filePath);
        helper.addAttachment(filename, file);

    }

    private String getEmailContentFromFile(String filename) throws IOException {
        return FileUtils.readFileToString(new File("src/main/resources/templates/"+filename), StandardCharsets.UTF_8);
    }

    private String replacePlaceholders(Map<String,String> placeholders, String emailContent) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue();
            emailContent = emailContent.replace(placeholder, value);
        }
        return emailContent;
    }
}
