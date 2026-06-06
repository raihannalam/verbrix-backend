package com.verbrix.service;

import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import com.verbrix.service.helpers.EmailTemplateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailTemplateHelper templateHelper;

    @Value("${resend.api.key:${RESEND_API_KEY}}")
    private String resendApiKey;

    @Value("${spring.app.mail.from-address}")
    private String fromEmail;

    @Value("${spring.app.mail.from-name}")
    private String fromName;

    /* =========================
       APPLICATION STATUS EMAILS
       ========================= */

    @Async
    public void sendApprovalEmail(String to, String name) {
        String content = templateHelper.getApprovalBody(name);
        String html = templateHelper.wrapInLayout("Application Approved", content);
        send(to, "Welcome to the Team!", html);
    }

    @Async
    public void sendChangesRequestedEmail(String to, String name, String reason) {
        String content = templateHelper.getChangesRequestedBody(name, reason);
        String html = templateHelper.wrapInLayout("Changes Requested", content);
        send(to, "Action Required: Update Profile", html);
    }

    @Async
    public void sendRejectionEmail(String to, String name, String reason) {
        String content = templateHelper.getRejectionBody(name, reason);
        String html = templateHelper.wrapInLayout("Application Status", content);
        send(to, "Status Update: Application", html);
    }

    /* =========================
       SECURITY EMAILS
       ========================= */

    @Async
    public void sendNewLoginAlert(String toEmail,
                                  String firstName,
                                  String location,
                                  String device) {

        String content = String.format("""
            <p style="margin-bottom: 24px;">Hi %s,</p>
            <p>A new login was detected on your Verbrix account from a device we don't recognize.</p>

            <div class="alert-box warning">
                <strong style="display:block;margin-bottom:4px;color:#92400e;">
                    Login Details:
                </strong>
                <b>Location:</b> %s<br>
                <b>Device:</b> %s<br>
                <b>Time:</b> %s
            </div>

            <p>
                If this was you, you can safely ignore this email.
                If not, please change your password immediately.
            </p>

            <div style="margin-top:32px;text-align:center;">
                <a href="https://verbrix.com/security" class="button">
                    Secure Account
                </a>
            </div>
            """,
                firstName,
                location,
                device,
                Instant.now().toString()
        );

        String html = templateHelper.wrapInLayout(
                "Security Alert: New Login Detected",
                content
        );

        send(toEmail, "Security Alert: New Login Detected", html);
    }

    /* =========================
       AUTH / OTP EMAIL
       ========================= */

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Resend resend = new Resend(resendApiKey);

            String htmlBody = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Your Verification Code</title>
                  <style>
                    body {
                      margin: 0;
                      padding: 0;
                      background-color: #f3f4f6;
                      font-family: 'Segoe UI', Roboto, Arial, sans-serif;
                    }
                    .container {
                      max-width: 600px;
                      margin: 30px auto;
                      background: #ffffff;
                      border-radius: 12px;
                      border: 1px solid #e5e7eb;
                      overflow: hidden;
                    }
                    .header {
                      background: linear-gradient(135deg, #0ea5a5, #047481);
                      padding: 20px;
                      text-align: center;
                      color: white;
                    }
                    .content {
                      padding: 30px 25px;
                      line-height: 1.6;
                    }
                    .otp-box {
                      display: inline-block;
                      background: linear-gradient(180deg, #0ea5a5, #047481);
                      color: white;
                      font-size: 32px;
                      font-weight: bold;
                      padding: 18px 36px;
                      border-radius: 10px;
                      letter-spacing: 6px;
                      margin: 20px 0;
                      font-family: monospace;
                    }
                    .footer {
                      background-color: #f9fafb;
                      text-align: center;
                      padding: 18px;
                      font-size: 13px;
                      color: #6b7280;
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>Verbrix Verification</h1>
                    </div>
                    <div class="content">
                      <p>Hi there,</p>
                      <p>Your verification code (valid for 10 minutes):</p>
                      <div class="otp-box">%s</div>
                      <p>If you didn’t request this, ignore this email.</p>
                    </div>
                    <div class="footer">
                      &copy; %d Verbrix. All rights reserved.
                    </div>
                  </div>
                </body>
                </html>
            """.formatted(otp, Year.now().getValue());

            SendEmailRequest request = SendEmailRequest.builder()
                    .from(String.format("%s <%s>", fromName, fromEmail))
                    .to(toEmail)
                    .subject("Your Verbrix verification code")
                    .html(htmlBody)
                    .build();

            SendEmailResponse response = resend.emails().send(request);
            log.info("✅ OTP email sent to {} — Message ID: {}", toEmail, response.getId());

        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /* =========================
       CORE SENDER
       ========================= */

    private void send(String to, String subject, String html) {
        try {
            Resend resend = new Resend(resendApiKey);

            SendEmailRequest request = SendEmailRequest.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            resend.emails().send(request);
            log.info("📧 Email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
