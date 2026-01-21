package com.zosh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    public void sendVerificationOtpEmail(
            String userEmail,
            String otp,
            String subject,
            String text
    ) {

        log.info("➡️ Sending OTP email to {}", userEmail);
        log.info("🔑 Brevo API key present: {}", apiKey != null);

        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ Brevo API key is missing");
            throw new RuntimeException("Brevo API key not configured");
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> sender = new HashMap<>();
        sender.put("email", senderEmail);
        sender.put("name", senderName);

        Map<String, Object> to = new HashMap<>();
        to.put("email", userEmail);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", new Object[]{to});
        body.put("subject", subject);
        body.put("htmlContent",
                "<p>Your login OTP is:</p><h2>" + otp + "</h2>");

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            BREVO_URL,
                            request,
                            String.class
                    );

            log.info("📬 Brevo response status: {}", response.getStatusCode());
            log.info("📬 Brevo response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Brevo failed: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error while sending OTP via Brevo", e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }
}
