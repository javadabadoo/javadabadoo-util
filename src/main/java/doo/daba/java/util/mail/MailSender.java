package doo.daba.java.util.mail;

import lombok.Getter;
import lombok.Setter;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
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
public final class MailSender {



	@Setter
	private String
            cssStyle,
            pageFooter,
            bodyMessage,
			title;

	@Getter @Setter
	private List<String>
            recipients,
            recipientsCc,
			recipientsBco;

	private List<AttachmentFile> attachmentFiles;

	private Session session;
	private Properties mailProperties;
	private MimeMessage message;

	Multipart multipart;

	/**
	 * Define el tipo de receptor como destinatario principal
	 */
	public final static int RECIPIENT = 1;
	/**
	 * Define el tipo de receptor como destinatario de copia
	 */
	public final static int RECIPIENT_CC = 2;
	/**
	 * Define el tipo de receptor como destinatario de copia oculta
	 */
	public final static int RECIPIENT_CCO = 3;



	private void init(){
		this.multipart = new MimeMultipart();

		this.recipients = new ArrayList<String>();
		this.recipientsCc = new ArrayList<String>();
		this.recipientsBco = new ArrayList<String>();
		this.attachmentFiles = new ArrayList<AttachmentFile>();

        this.loadMailProperties();
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
	public MailSender(Session session) {
		super();
		this.session = session;
		init();
	}



    public MailSender(Properties mailProperties) {
        super();
        this.mailProperties = mailProperties;
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
	public void send() throws MessagingException, IOException {

		if(
				this.checkRecipientList(this.recipients)
				|| this.checkRecipientList(this.recipientsCc)
				|| this.checkRecipientList(this.recipientsBco)){
			throw new IllegalArgumentException("No existen destinatarios para enviar el correo.");
		}

		this.prepareMailMessage();

		Transport mailTransport = this.session.getTransport("smtp");
		mailTransport.connect(
                this.session.getProperty("mail.smtp.user"),
                this.session.getProperty("mail.smtp.password")
        );

		mailTransport.sendMessage(this.message, this.message.getAllRecipients());
	}



	/**
	 * Realiza la autenticacion con el servidor de correo electronico
	 * @throws IOException
	 */
	private void login() throws IOException{
		this.session = Session.getDefaultInstance(mailProperties);
		this.session.setDebug(false);
		this.session.setDebugOut(System.out);
	}



	/**
	 * Prepara el objeto de message estableciendo los parametros necesarios para
	 * realizar el envio del correo electronico como son el emisor, los receptores,
	 * encabezados de message y archivos adjuntos
	 *
	 * @throws MessagingException	Lanzada cuendo exista un error en el procesamiento
	 * 								interno del API de JavaMail.
	 * @throws IOException
	 */
	private void prepareMailMessage() throws MessagingException, IOException{

		if(this.session == null) {
			this.login();
		}

		this.message = new MimeMessage(this.session);
		this.message.setHeader("X-Mailer", "sendhtml");
		this.message.setFrom(new InternetAddress(this.session.getProperties().getProperty("mail.smtp.user")));
        this.message.setSubject(this.title);

        this.addMailBodyComponent(this.cssStyle, "text/css");
        this.addMailBodyComponent(this.bodyMessage, "text/html");
        this.addMailBodyComponent(this.pageFooter, "text/html");

        this.addMailRecipients(this.recipients, Message.RecipientType.TO);
        this.addMailRecipients(this.recipientsCc, Message.RecipientType.CC);
        this.addMailRecipients(this.recipientsBco, Message.RecipientType.BCC);

        this.addAttachmentFiles();


		this.message.setContent(this.multipart);
	}



	/**
	 * Metodo encargado de generar una lista de receptores de correo electrnico.
	 *
	 * @param mailRecipientsList	Lista tipada con objetos de tipo {@code String} en la
	 * 							que se define en cada elemento una direccion de correo
	 * 							electronico que servira como remitente del message
	 *
	 * @return	Regresa un array de {@code InternetAddress} requerido para establecer
	 * 			los destinatarios del message.
	 *
	 * @throws MessagingException	Lanzada cuendo exista un error en el procesamiento
	 * 								interno del API de JavaMail.
	 *
	 * @see InternetAddress
	 */
	private InternetAddress[] createMailRecipients(List<String> mailRecipientsList) throws AddressException{
		InternetAddress[] mailRecipients = new InternetAddress[mailRecipientsList.size()];

		for(int listIndex = 0; listIndex < mailRecipientsList.size(); listIndex++){
			mailRecipients[listIndex] = new InternetAddress(mailRecipientsList.get(listIndex));
		}

		return mailRecipients;
	}



	/**
	 * Metodo encargado de adjuntar los archivos adjuntos
	 *
	 * @throws MessagingException lanzada si existe un error al intentar adjuntar un archivo
	 */
	private void addAttachmentFiles() throws MessagingException {

		if (this.attachmentFiles == null || this.attachmentFiles.isEmpty()){
			return;
		}

		for(AttachmentFile attachmentFile : this.attachmentFiles){
			MimeBodyPart messageBodyPart  = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachmentFile.getContent(), attachmentFile.getMimeType())));
			messageBodyPart.setFileName(attachmentFile.getFileName());
			multipart.addBodyPart(messageBodyPart);
		}
	}



	/**
	 * Este metodo permite agregar diferentes componentes de texto al correo electronico
	 *
	 * @param component		Define el contenido del componente a agregar al cuerpo del correo
	 * @param componentType	Define el tipo de contenido del componente a agregar al cuerpo del correo
	 *
	 * @throws MessagingException lanzada si ocurre un error al agregar el componente al cuerpo del correo
	 */
	private void addMailBodyComponent(String component, String componentType) throws MessagingException{
		if(component != null && !component.isEmpty()){
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(component, componentType);
			this.multipart.addBodyPart(bodyPart);
		}
	}



	/**
	 * Metodo que agrega las direcciones de correos electronicos de los destinatarios dependiendo
	 * del tipo que se haya definido en el parametro {@code tipoDeReceptor}
	 *
	 * @param mailRecipientsList	Define la lista de las direcciones de los correos electronicos a
	 * 							quienes va dirigido
	 * @param recipientType	Define el tipo de receptores a los que se deben agregar las direcciones
	 * 							de correo electronico definidas en el parametro {@code listaDeReceptores}
	 *
	 * @throws AddressException	Lanzada cuando exista una direccion de correo electronico en un formato
	 * 							mal formado
	 * @throws MessagingException	Lanzada si existe un error al agragar los receptores del correo
	 */
	private void addMailRecipients(List<String> mailRecipientsList, Message.RecipientType recipientType)
			throws MessagingException {

		if(this.recipients != null && !this.recipients.isEmpty()) {
			this.message.setRecipients(
                    recipientType,
                    createMailRecipients(mailRecipientsList));
		}
	}



	/**
	 * Metodo que permite agregar un archivo representado por el tipo {@code AttachmentFile}
	 *
	 * @param attachmentFile	Encapsula la informacion del archivo que se debe adjuntar al
	 * 							correo electronico.
	 *
	 * @see AttachmentFile
	 */
	public void addAttachmentFile(AttachmentFile attachmentFile){
		this.attachmentFiles.add(attachmentFile);
	}



	/**
	 * Agrega el receptor de correo electronico.
	 *
	 * @param mailAddress	Define la direccion de correo electronico a la que
	 * 							debe enviarse el message de correo
	 * @param recipientType	Define el tipo de receptor del correo
	 */
	public void addRecipient(String mailAddress, int recipientType){
		switch(recipientType){
			case RECIPIENT:
				this.recipients.add(mailAddress);
				break;

			case RECIPIENT_CC:
				this.recipientsCc.add(mailAddress);
				break;

			case RECIPIENT_CCO:
				this.recipientsBco.add(mailAddress);
				break;
		}
	}


	private boolean checkRecipientList(List<String> listaDeReceptores) {
		return this.recipients == null || this.recipients.isEmpty();
	}



    private void loadMailProperties() {

        if(mailProperties == null) {
            InputStream placeHolcerInputStream = ClassLoader.getSystemResourceAsStream("correo.properties");
            this.mailProperties = new Properties();
            try {
                this.mailProperties.load(placeHolcerInputStream);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}