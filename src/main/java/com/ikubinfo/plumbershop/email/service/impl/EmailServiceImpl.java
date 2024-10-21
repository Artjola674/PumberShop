package com.ikubinfo.plumbershop.email.service.impl;

import com.ikubinfo.plumbershop.email.dto.OrderConfirmationRequest;
import com.ikubinfo.plumbershop.email.dto.PasswordResetRequest;
import com.ikubinfo.plumbershop.email.dto.PerformanceIssueRequest;
import com.ikubinfo.plumbershop.email.dto.ScheduleRequest;
import com.ikubinfo.plumbershop.email.service.EmailService;

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
    public void sendEmailWhenOrderIsCreated(OrderConfirmationRequest orderConfirmationRequest) {
        try {
            String emailContent = buildOrderCreatedBody(orderConfirmationRequest.getCustomer());

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = createMessageHelper(new String[]{orderConfirmationRequest.getCustomer().getEmail()},
                emailContent,"Order Confirmation", message);

            addAttachment(orderConfirmationRequest.getFileLocation()+ orderConfirmationRequest.getFileName(),
                orderConfirmationRequest.getFileName(), helper);
            mailSender.send(message);

        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendPerformanceIssueEmail(PerformanceIssueRequest performanceIssueRequest) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            String emailContent = buildPerformanceIssueBody(performanceIssueRequest.getMethodName(), performanceIssueRequest.getExecutionTime());
            createMessageHelper(new String[]{COMPANY_EMAIL},
                    emailContent,"API Performance Alert", message);
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendForgetPasswordEmail(PasswordResetRequest passwordResetRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            String emailContent = buildResetPasswordEmailBody(passwordResetRequest.getContextPath(), passwordResetRequest.getToken());
            createMessageHelper(new String[]{passwordResetRequest.getEmail()},
                    emailContent,"Reset password", message);
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendScheduleToEmail(ScheduleRequest scheduleRequest) {

        try {
            String emailContent = getEmailContentFromFile("create-schedule.txt");

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = createMessageHelper(scheduleRequest.getEmails().toArray(new String[scheduleRequest.getEmails().size()]),
                    emailContent,"Schedule", message);

            addAttachment(scheduleRequest.getDocumentPath() + scheduleRequest.getFilename(), scheduleRequest.getFilename(), helper);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            log.error("Something went wrong while sending email");
        }


    }

    private String buildResetPasswordEmailBody(String contextPath, String token) throws IOException {
        String emailContent = getEmailContentFromFile("reset-password.txt");

        String link = contextPath +
                "/api/v1/users/resetPassword?ticket="+token;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("RESET_PASS_LINK", link);

        emailContent =  replacePlaceholders(placeholders,emailContent);
        return emailContent;
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

    private MimeMessageHelper createMessageHelper(String[] emails, String emailContent,
                                                  String subject, MimeMessage message) throws MessagingException {

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(emails);
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
