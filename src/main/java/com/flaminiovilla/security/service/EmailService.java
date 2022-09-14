package com.flaminiovilla.security.service;

import com.flaminiovilla.security.model.dto.MailResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Configuration freeMarkerConfiguration;


    @Value("${mail.username}")
    private String username;


    // Methods that sends an email using Freemarker specified template.

    // -------------------[sendOnBoardingToolstaffingEmail]----------------------
    public MailResponse sendEmail(String to, String subject, Map<String, Object> model, String ftlFileName) {
        MailResponse response = new MailResponse();
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // Set mediaType
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            // Get the correct template into resources/mail-templates
            Template t = freeMarkerConfiguration.getTemplate(ftlFileName + ".ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            helper.setTo(to);

            // Set up the email
            helper.setText(html, true);
            helper.setSubject(subject);

            //helper.setCc(cc); per mandare in cc

            helper.setFrom(username);
            javaMailSender.send(message);

            response.setMessage(ftlFileName + " | Mail sent to : " + to);
            response.setStatus(Boolean.TRUE);

        } catch (MessagingException | IOException | TemplateException e) {
            response.setMessage(ftlFileName + "Mail Sending failure : "+e.getMessage());
            response.setStatus(Boolean.FALSE);
        }

        return response;
    }



//    Boolean isValidEmail(String to, String username, String domain){
//        String d = "elis.org";
//        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
//                + Pattern.quote(d) + "$";
//        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
//
//        if (to == null){
//            if (username == null || domain == null){
//                return Boolean.FALSE;
//            }else if(username.equals("") || domain.equals("")){
//                return Boolean.FALSE;
//            }else{
//                String email = username + "@" + domain;
//                Matcher matcher = pattern.matcher(email);
//                if (matcher.matches() && !(email.equals("null@elis.org"))){
//                    return Boolean.TRUE;
//                }else{
//                    return Boolean.FALSE;
//                }
//            }
//        }else{
//            Matcher matcher = pattern.matcher(to);
//            if (matcher.matches() && !(to.equals("null@elis.org"))){
//                return Boolean.TRUE;
//            }else{
//                return Boolean.FALSE;
//            }
//        }
//    }


}












