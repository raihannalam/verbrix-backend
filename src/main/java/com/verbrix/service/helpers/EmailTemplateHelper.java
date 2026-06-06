package com.verbrix.service.helpers;

import org.springframework.stereotype.Component;
import java.time.Year;

@Component
public class EmailTemplateHelper {

    private static final String BRAND_COLOR = "#0ea5a5";
    private static final String BRAND_DARK = "#047481";

    /**
     * The Master Wrapper: Change this to update the entire look of all emails.
     */
    public String wrapInLayout(String headerTitle, String bodyContent) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
              <style>
                * { font-family: 'Manrope', 'Segoe UI', sans-serif; }
                body { margin: 0; padding: 0; background-color: #f8fafc; color: #1e293b; }
                .wrapper { padding: 40px 20px; }
                .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05); }
                .header { background: linear-gradient(135deg, %s, %s); padding: 32px 20px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; font-weight: 800; text-transform: uppercase; }
                .content { padding: 40px 32px; font-size: 16px; line-height: 1.6; color: #334155; }
                .button-container { margin-top: 32px; text-align: center; }
                .button { display: inline-block; padding: 14px 32px; background-color: %s; color: #ffffff !important; text-decoration: none; border-radius: 12px; font-weight: 700; }
                .alert-box { padding: 20px; border-radius: 12px; margin: 24px 0; font-size: 15px; }
                .warning { background-color: #fffbeb; border: 1px solid #fde68a; color: #92400e; }
                .danger { background-color: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
                .footer { padding: 32px; text-align: center; font-size: 13px; color: #94a3b8; background: #f8fafc; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="container">
                  <div class="header"><h1>%s</h1></div>
                  <div class="content">
                    %s
                    <div style="margin-top: 40px; border-top: 1px solid #f1f5f9; padding-top: 24px;">
                      <p style="margin: 0; font-size: 14px; font-weight: 600;">The Verbrix Team</p>
                    </div>
                  </div>
                  <div class="footer">&copy; %d Verbrix. Automated message.</div>
                </div>
              </div>
            </body>
            </html>
            """, BRAND_COLOR, BRAND_DARK, BRAND_COLOR, headerTitle, bodyContent, Year.now().getValue());
    }

    public String getApprovalBody(String name) {
        return String.format("""
            <p>Hi %s,</p>
            <p><strong>Congratulations!</strong> Your application to become a Verbrix Interpreter has been <strong>APPROVED</strong>.</p>
            <p>Your profile is now live. You can start accepting interpretation requests immediately.</p>
            <div class="button-container">
                <a href="https://verbrix.com/dashboard" class="button">Go to Dashboard</a>
            </div>
        """, name);
    }

    public String getChangesRequestedBody(String name, String reason) {
        return String.format("""
            <p>Hi %s,</p>
            <p>Our team has reviewed your profile and requires a few adjustments before we can proceed.</p>
            <div class="alert-box warning">
                <strong>Action Required:</strong> %s
            </div>
            <div class="button-container">
                <a href="https://verbrix.com/my-profile" class="button">Update Profile</a>
            </div>
        """, name, reason);
    }

    public String getRejectionBody(String name, String reason) {
        return String.format("""
            <p>Hi %s,</p>
            <p>After careful consideration, we are unable to approve your application at this time.</p>
            <div class="alert-box danger">
                <strong>Reason:</strong> %s
            </div>
        """, name, reason);
    }
}