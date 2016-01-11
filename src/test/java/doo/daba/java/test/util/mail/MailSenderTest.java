package doo.daba.java.test.util.mail;

import doo.daba.java.util.mail.MailSender;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: java_daba_doo
 * Date: 5/11/13
 */
public class MailSenderTest {

	MailSender mailSender;


	@Before
	public void init() throws FileNotFoundException {

		this.mailSender = new MailSender((Session) null);

		this.mailSender.addRecipient("correo@dominio.com", MailSender.RECIPIENT);
		this.mailSender.setBodyMessage("Este es el cuerpo del mensaje enviado a las: " + new Date());
		this.mailSender.setTitle("Correo desde Java");

	}


	@Test
	public void sendMailTest() throws IOException, MessagingException {
		this.mailSender.send();
	}


}
