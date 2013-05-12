package doo.daba.java.util.mail;

import lombok.Getter;
import lombok.Setter;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Clase encargada de realizar envios de correo electronico.
 *
 * @author Gerardo Aquino
 * @since 08/03/2010
 * @version 1.0
 */
public final class CorreoElectronico {



	@Setter
	private String
			estilo,
			pieDePagina,
			cuerpoDelCorreo,
			tituloDelCorreo;

	@Getter @Setter
	private List<String>
			receptores,
			receptoresCc,
			receptoresCco;

	private List<ArchivoAdjunto> archivosAdjuntos;

	private Session session;
	private Properties props;
	private MimeMessage  mensaje;

	Multipart multipart;

	/**
	 * Define el tipo de receptor como destinatario principal
	 */
	public final static int RECEPTOR = 1;
	/**
	 * Define el tipo de receptor como destinatario de copia
	 */
	public final static int RECEPTOR_CC = 2;
	/**
	 * Define el tipo de receptor como destinatario de copia oculta
	 */
	public final static int RECEPTOR_CCO = 3;



	private void init(){
		this.props = new Properties();
		this.multipart = new MimeMultipart();

		this.receptores = new ArrayList<String>();
		this.receptoresCc = new ArrayList<String>();
		this.receptoresCco = new ArrayList<String>();
		this.archivosAdjuntos = new ArrayList<ArchivoAdjunto>();
	}


	/**
	 * Esta clase está pensada principalmente para funcionar con un servidor de aplicaciones por lo que
	 * es mas funcional obtener la sesion del correo mediante su dirección JNDI desde el componente que
	 * utilice el envio de mensajes. Si se necesita enviar correos fuera de un contexto JEE entonces
	 * la sesión debe ser {@code null} para que pueda cargarse la configuración desde el archivo de
	 * propiedades {@code correo.properties}
	 *
	 * @param session
	 */
	public CorreoElectronico(Session session) {
		super();
		this.session = session;
		init();
	}



	/**
	 * Realiza el envio del correo electronico.
	 *
	 * @throws MessagingException	Lanzada cuando no existe ningun destinatario de
	 * 								correo ya se en receptores, receptores en copia
	 * 								o recepptores en copia oculta.
	 *
	 * @throws MessagingException	Lanzada cuendo exista un error en el procesamiento
	 * 								interno del API de JavaMail.
	 * @throws IOException			Lanzada cuando ocurra un error de lectura del archivo
	 * 								de propiedades que define los datos de conexion al servidor
	 * 								SMTP
	 */
	public void envia() throws MessagingException, IOException {

		if(
				this.validaListaDeReceptores(this.receptores)
				|| this.validaListaDeReceptores(this.receptoresCc)
				|| this.validaListaDeReceptores(this.receptoresCco)){
			throw new IllegalArgumentException("No existen destinatarios para enviar el correo.");
		}

		this.preparaMensaje();

		Transport t = this.session.getTransport("smtp");
		t.connect(
				this.session.getProperty("mail.smtp.user"),
				this.session.getProperty("mail.smtp.password")
		);

		t.sendMessage(this.mensaje, this.mensaje.getAllRecipients());
	}



	/**
	 * Realiza la autenticacion con el servidor de correo electronico
	 * @throws IOException
	 */
	private void login() throws IOException{
		this.props.load(ClassLoader.getSystemResourceAsStream("correo.properties"));

		this.session = Session.getDefaultInstance(props);
		this.session.setDebug(false);
		this.session.setDebugOut(System.out);
	}



	/**
	 * Prepara el objeto de mensaje estableciendo los parametros necesarios para
	 * realizar el envio del correo electronico como son el emisor, los receptores,
	 * encabezados de mensaje y archivos adjuntos
	 *
	 * @throws MessagingException	Lanzada cuendo exista un error en el procesamiento
	 * 								interno del API de JavaMail.
	 * @throws IOException
	 */
	private void preparaMensaje() throws MessagingException, IOException{

		if(this.session == null) {
			this.login();
		}

		this.mensaje = new MimeMessage(this.session);
		this.mensaje.setHeader("X-Mailer", "sendhtml");
		this.mensaje.setFrom(new InternetAddress(this.session.getProperties().getProperty("mail.smtp.user")));
		this.mensaje.setSubject(this.tituloDelCorreo);

		agregaComponenteCorreo(this.estilo, "text/css");
		agregaComponenteCorreo(this.cuerpoDelCorreo, "text/html");
		agregaComponenteCorreo(this.pieDePagina, "text/html");

		agregaDestinatarios(this.receptores, Message.RecipientType.TO);
		agregaDestinatarios(this.receptoresCc, Message.RecipientType.CC);
		agregaDestinatarios(this.receptoresCco, Message.RecipientType.BCC);

		agregaArchivosAdjuntos();


		this.mensaje.setContent(this.multipart);
	}



	/**
	 * Metodo encargado de generar una lista de receptores de correo electrnico.
	 *
	 * @param listaDeCorreos	Lista tipada con objetos de tipo {@code String} en la
	 * 							que se define en cada �lemento una direccion de correo
	 * 							electronico que servira como remitente del mensaje
	 *
	 * @return	Regresa un array de {@code InternetAddress} requerido para establecer
	 * 			los destinatarios del mensaje.
	 *
	 * @throws MessagingException	Lanzada cuendo exista un error en el procesamiento
	 * 								interno del API de JavaMail.
	 *
	 * @see InternetAddress
	 */
	private InternetAddress[] obtenerListaDeRemitentes(List<String> listaDeCorreos) throws AddressException{
		InternetAddress[] remitentes = new InternetAddress[listaDeCorreos.size()];

		for(int indice = 0; indice < listaDeCorreos.size(); indice++){
			remitentes[indice] = new InternetAddress(listaDeCorreos.get(indice));
		}

		return remitentes;
	}



	/**
	 * Metodo encargado de adjuntar los archivos definidos en el parametro {@code archivosAdjuntos}
	 * al cuerpo del correo
	 *
	 * @throws MessagingException lanzada si existe un error al intentar adjuntar un archivo
	 */
	private void agregaArchivosAdjuntos() throws MessagingException {

		if (this.archivosAdjuntos == null || this.archivosAdjuntos.isEmpty()){
			return;
		}

		for(ArchivoAdjunto archivo : this.archivosAdjuntos){
			MimeBodyPart messageBodyPart  = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(archivo.getContenido(), archivo.getMimeType())));
			messageBodyPart.setFileName(archivo.getNombre());
			multipart.addBodyPart(messageBodyPart);
		}
	}



	/**
	 * Este metodo permite agregar diferentes componentes de texto al correo electronico
	 *
	 * @param componente		Define el contenido del componente a agregar al cuerpo del correo
	 * @param tipoDeContenido	Define el tipo de contenido del componente a agregar al cuerpo del correo
	 *
	 * @throws MessagingException lanzada si ocurre un error al agregar el componente al cuerpo del correo
	 */
	private void agregaComponenteCorreo(String componente, String tipoDeContenido) throws MessagingException{
		if(componente != null && !componente.isEmpty()){
			MimeBodyPart mimeEstilo = new MimeBodyPart();
			mimeEstilo.setContent(componente, tipoDeContenido);
			this.multipart.addBodyPart(mimeEstilo);
		}
	}



	/**
	 * Metodo que agrega las direcciones de correos electronicos de los destinatarios dependiendo
	 * del tipo que se haya definido en el parametro {@code tipoDeReceptor}
	 *
	 * @param listaDeReceptores	Define la lista de las direcciones de los correos electronicos a
	 * 							quienes va dirigido
	 * @param tipoDeReceptor	Define el tipo de receptores a los que se deben agregar las direcciones
	 * 							de correo electronico definidas en el parametro {@code listaDeReceptores}
	 *
	 * @throws AddressException	Lanzada cuando exista una direccion de correo electronico en un formato
	 * 							mal formado
	 * @throws MessagingException	Lanzada si existe un error al agragar los receptores del correo
	 */
	private void agregaDestinatarios(List<String> listaDeReceptores, Message.RecipientType tipoDeReceptor)
			throws
			AddressException,
			MessagingException{

		if(this.receptores != null && !this.receptores.isEmpty()) {
			this.mensaje.setRecipients(
					tipoDeReceptor,
					obtenerListaDeRemitentes(listaDeReceptores));
		}
	}



	/**
	 * Metodo que permite agregar un archivo representado por el tipo {@code ArchivoAdjunto}
	 *
	 * @param archivoAdjunto	Encapsula la informacion del archivo que se debe adjuntar al
	 * 							correo electronico.
	 *
	 * @see ArchivoAdjunto
	 */
	public void agregaArchivo(ArchivoAdjunto archivoAdjunto){
		this.archivosAdjuntos.add(archivoAdjunto);
	}



	/**
	 * Agrega el receptor de correo electronico.
	 *
	 * @param direccionDeCorreo	Define la direccion de correo electronico a la que
	 * 							debe enviarse el mensaje de correo
	 * @param tipoDeReceptor	Define el tipo de receptor del correo
	 */
	public void agragaReceptor(String direccionDeCorreo, int tipoDeReceptor){
		switch(tipoDeReceptor){
			case RECEPTOR:
				this.receptores.add(direccionDeCorreo);
				break;

			case RECEPTOR_CC:
				this.receptoresCc.add(direccionDeCorreo);
				break;

			case RECEPTOR_CCO:
				this.receptoresCco.add(direccionDeCorreo);
				break;
		}
	}


	private boolean validaListaDeReceptores(List<String> listaDeReceptores) {
		return this.receptores == null || this.receptores.isEmpty();
	}
}