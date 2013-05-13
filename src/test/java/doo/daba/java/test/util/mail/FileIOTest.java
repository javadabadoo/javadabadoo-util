package doo.daba.java.test.util.mail;

import doo.daba.java.util.io.FileIO;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Gerardo Aquino
 * Date: 13/05/13
 */
public class FileIOTest {

    private final File archivoEntrada = new File("C:/imagen.png");
    private final File archivoEntradaInvalido = new File("XX:/imagen.png");
    private File archivoSalida;
    private byte[] archivoLeido;

    @Before
    public void init() throws IOException {
        this.archivoSalida = new File(this.archivoEntrada.getParent() + System.getProperty("file.separator") + "copy_" + archivoEntrada.getName());

        if(this.archivoSalida.exists()) {
            this.archivoSalida.delete();
        }

        assert ! this.archivoSalida.exists();
        assert this.archivoEntrada.exists();
    }


    @Test
    public void cargaArchivo() throws IOException {
        this.archivoLeido = FileIO.readBytes(this.archivoEntrada);

        assert this.archivoLeido != null;
    }



    @Test
    public void escrituraArchivos() throws IOException {
        this.cargaArchivo();
        FileIO.writeFile(this.archivoLeido, this.archivoSalida, FileIO.DEFAULT);
    }



    @Test (expected = IOException.class)
    public void lcargarArchivoNoExistente() throws IOException {
        FileIO.readBytes(this.archivoEntradaInvalido);
    }




}
