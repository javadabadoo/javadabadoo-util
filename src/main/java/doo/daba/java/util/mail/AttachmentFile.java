package doo.daba.java.util.mail;

import lombok.*;


/**
 * Encapsula la información de un archivo adjunto al correo electrónico a enviar.
 *
 * @author Gerardo Aquino
 * @since 08/03/2010
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentFile {

    @Getter @Setter
    private String fileName;

    @Getter @Setter
    private byte[] content;

    @Getter @Setter
    private String mimeType;

}