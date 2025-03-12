package iuh.fit.se.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import iuh.fit.se.email.Email;
import iuh.fit.se.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService{
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Override
	public void sendEmail(Email email) {
		MimeMessage  message = javaMailSender.createMimeMessage();
		try {
			System.out.println(email.getToEmail());
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom("sendingemaileventhub@gmail.com");
			helper.setTo(email.getToEmail());
			helper.setSubject(email.getSubject());
			helper.setText(email.getBody());
			javaMailSender.send(message);
		} catch (MessagingException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

}
