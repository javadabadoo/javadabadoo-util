package doo.daba.java.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * Esta clase permite la escritura/lectura de archivos principalmente de texto plano.
 * Cuando la escribí tuve la necesidad de leer/guardar archivos de configuración no basados
 * en properties, debía escribir un archivo XML o archivos CSV y me
 * sirvió bastante para esas cuestiones.
 *
 * @author Gerardo Aquino
 * @since 09/22/2011
 * {@link "http://www.javamexico.org/blogs/javadabadoo/lecturaescritura_basica_de_archivos"}
 */
public final class FileIO {

    /**
     * Constante que define el conjunto de caracteres predefinido en el sistema
     */
    public static Charset DEFAULT = Charset.defaultCharset ();
    /**
     * Conjunto decaracteres UTF-8
     */
    public static Charset UTF_8 = Charset.forName ("UTF-8");



    /**
     * Metodo cntructor de la clase
     * @deprecated  En desuso debido a que esta clase no tiene sentido realizar instancias nuevas
     */
    @Deprecated
    private FileIO(){ }



    /**
     * <p>
     * Obtiene el arreglo de tipo {@code byte} tomando como fuente un objeto de tipo
     * {@code InputStream}
     * </p>
     *
     * @param inputStream   Fuente de lectura para obtener los bytes del archivo
     *
     * @return      Retorna un arreglo de tipo {@code byte} que represena el contenido del archivo leido
     *
     * @throws IOException  Lanzada en caso de presentarse algun problema en tiempo de ejecucion
     *                                              relacionado con la fuente de lectura
     */
    public static byte[] readBytes (InputStream inputStream)
            throws IOException {

        byte[] archivoByte = new byte[inputStream.available ()];
        inputStream.read (archivoByte);
        inputStream.close ();
        return archivoByte;

    }



    /**
     * <p>
     * Obtiene el contenido en un arreglo de tipo {@code byte} tomando como fuente un objeto de tipo
     * {@code File}
     * </p>
     *
     * @param fileToRead    Objeto que encapsula la informacion del archivo del cual se obtiene la el
     *                              contenido representado en un arreglo de tipo {@code byte}
     *
     * @return      Retorna un arreglo de tipo {@code byte} que represena el contenido del archivo leido
     *
     * @throws IOException  Lanzada en caso de presentarse algun problema en tiempo de ejecucion
     *                                              relacionado con la fuente de lectura
     */
    public static byte[] readBytes (File fileToRead)
            throws IOException {

        return readBytes (new FileInputStream (fileToRead));
    }



    /**
     * <p>
     * Obtiene el contenido en un arreglo de tipo {@code byte} tomando como fuente un objeto de tipo
     * {@code String}
     * </p>
     *
     * @param content
     * @param charset
     * @return
     */
    public static byte[] readBytes (String content, Charset charset) {
        return content.getBytes (charset);
    }



    /**
     * <p>
     * Este metodo se encarga de tomar el contenido del parametro {@code content} en la dreccion
     * que se define en el la ubicacion del parametro {@code fileToWrite}
     * </p>
     *
     * @param content       Define e contenido del archivo que se escribe e disco duro
     * @param fileToWrite   Define la ubicacion del archivo que se genera
     * @param charset       Indica el conjunto de caracteres con el que se escribe el archivo
     *
     * @throws IOException  Lanzada cuando ocurre un error en tiempo de ejecucion referente a la
     *                                              escritura del archivo
     */
    public static void writeFile (String content, File fileToWrite, Charset charset)
            throws IOException {

        writeFile (content.getBytes (charset), fileToWrite, charset);

    }



    /**
     * <p>
     * Este metodo se encarga de escribir un archivo en disco duro el contenido del prametro
     * {@code content} en laubicacion definida por el parametro {@code fileToWrite} con el conjunto
     * de catacteres definido por el parametro charset
     * </p>
     *
     * @param content       Define e contenido del archivo que se escribe e disco duro representado en
     *                                      un arreglo de bytes que corresponde a contenido binario del archivo a
     *                                      escribir
     * @param fileToWrite   Define la ubicacion del archivo que se genera
     * @param charset       Indica el conjunto de caracteres con el que se escribe el archivo
     *
     * @throws IOException  Lanzada cuando ocurre un error en tiempo de ejecucion referente a la
     *                                              escritura del archivo
     */
    public static void writeFile (byte[] content, File fileToWrite, Charset charset)
            throws IOException {

        FileOutputStream fos = new FileOutputStream (fileToWrite);
        fos.write (content);
        fos.close ();

    }
}
