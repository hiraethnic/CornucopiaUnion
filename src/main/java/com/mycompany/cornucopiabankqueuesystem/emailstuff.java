package com.mycompany.cornucopiabankqueuesystem;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to handle email OTP generation and SMTP transmission.
 */
public class emailstuff {
    
    private static final Logger logger = Logger.getLogger(emailstuff.class.getName());
    
    // Sender configuration (Replace with real credentials before testing)
    private static final String SENDER_EMAIL = "your.bank.system@gmail.com"; 
    private static final String SENDER_APP_PASSWORD = "your-16-char-app-password"; 

    /** Generates a random 6-digit numeric OTP */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /** Sends the OTP email using Gmail SMTP */
    public static boolean sendOTPEmail(String recipientEmail, String otpCode) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Cornucopia Bank Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Cornucopia Bank - Password Reset OTP Code");

            String emailContent = "<h3>Cornucopia Bank Security Alert</h3>"
                    + "<p>Your 6-digit One-Time Password (OTP) for resetting your teller password is:</p>"
                    + "<h2 style='color:#0C234A; letter-spacing: 4px;'>" + otpCode + "</h2>"
                    + "<p>This code is valid for <b>5 minutes</b>. Do not share this code with anyone.</p>";

            message.setContent(emailContent, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to send OTP email to " + recipientEmail, ex);
            return false;
        }
    }
}