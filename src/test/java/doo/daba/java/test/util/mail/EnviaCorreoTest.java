package doo.daba.java.test.util.mail;

import doo.daba.java.util.mail.CorreoElectronico;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: java_daba_doo
 * Date: 5/11/13
 * Time: 6:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnviaCorreoTest {

	CorreoElectronico correo;


	@Before
	public void init() throws FileNotFoundException {

		this.correo = new CorreoElectronico(null);

		this.correo.agragaReceptor("xxxxxxxxx@gmail.com", CorreoElectronico.RECEPTOR);
		this.correo.setCuerpoDelCorreo("Este es el cuerpo del mensaje enviado a las: " + new Date());
		this.correo.setTituloDelCorreo("Correo desde Java");

	}


	@Test
	public void enviaCorreo() throws IOException, MessagingException {
		this.correo.envia();
	}


}
