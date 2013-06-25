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

	MailSender correo;


	@Before
	public void init() throws FileNotFoundException {

		this.correo = new MailSender((Session) null);

		this.correo.addRecipient("xxxxxxxxx@gmail.com", MailSender.RECIPIENT);
		this.correo.setBodyMessage("Este es el cuerpo del mensaje enviado a las: " + new Date());
		this.correo.setTitle("Correo desde Java");

	}


	@Test
	public void enviaCorreo() throws IOException, MessagingException {
		this.correo.send();
	}


}
