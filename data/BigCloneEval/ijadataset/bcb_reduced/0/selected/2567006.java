package compac;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import anima.annotation.Component;

@Component(id = "http://purl.org/NET/dcc/compac.Zipper", provides = { "http://purl.org/NET/dcc/compac.Zipper.IZipper" })
public class Zipper extends JFrame implements IZipper {

    private JTextField jtPath = new JTextField(15);

    private JTextField jtFilePath = new JTextField(15);

    private JTextField jtFileName = new JTextField(7);

    private JButton jbSetCam = new JButton("...");

    private JButton jbFilePath = new JButton("Local");

    private JButton jbZip = new JButton("Compactar");

    public Zipper() {
        super("Zipper");
        Container c = getContentPane();
        c.setLayout(new FlowLayout());
        c.add(new JLabel("Abrir: "));
        c.add(jtPath);
        jbSetCam.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        c.add(jbSetCam);
        c.add(new JLabel("Nome do arquivo: "));
        c.add(jtFileName);
        c.add(new JLabel("Salvar Destino: "));
        c.add(jtFilePath);
        jbFilePath.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        c.add(jbFilePath);
        jbZip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                zip(jtFilePath.getText().toString() + "/" + jtFileName.getText().toString() + ".zip", jtPath.getText().toString());
            }
        });
        c.add(jbZip);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 100);
        setResizable(false);
    }

    public void open() {
        JFileChooser choice = new JFileChooser();
        choice.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        choice.showOpenDialog(this);
        jtPath.setText(choice.getSelectedFile().toString());
    }

    public void save() {
        JFileChooser choice = new JFileChooser();
        choice.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        choice.showOpenDialog(this);
        jtFilePath.setText(choice.getSelectedFile().toString());
    }

    /**
	 * L� um arquivo passado no par�metro para a mem�ria.
	 * @param file Arquivo a ser lido.
	 * @return Arquivo lido em bytes.
	 * @throws Exception
	 */
    private byte[] read(File file) throws Exception {
        byte[] result = null;
        if (file != null && !file.isDirectory()) {
            final long length = file.length();
            result = new byte[(int) length];
            InputStream fi = new FileInputStream(file);
            byte b;
            long count = 0;
            while ((b = (byte) fi.read()) != -1) {
                result[(int) count++] = b;
            }
            fi.close();
        }
        return result;
    }

    /**
	 * Adiciona um diret�rio ou arquivo a um ZipOutputStream instanciado.
	 * @param file - Nome do diret�rio a ser comprimido.
	 * @param out 
	 * @param path
	 * @throws Exception 
	 */
    private void addToZip(ZipOutputStream out, File file, String path) throws Exception {
        byte data[] = null;
        ZipEntry entry = null;
        if (file != null) {
            String name = file.getAbsolutePath();
            name = name.substring(path.length() + 1, name.length()).replace('\\', '/');
            System.out.println(">>>> Adding: " + name);
            if (file != null) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        addToZip(out, f, path);
                    }
                } else {
                    entry = new ZipEntry(name);
                    out.putNextEntry(entry);
                    data = read(file);
                    if (data != null && data.length > 0) {
                        out.write(data, 0, data.length);
                    }
                    out.closeEntry();
                    out.flush();
                }
            }
        }
    }

    /**
	 * Comprime um diret�rio ou arquivo.
	 * @param zipName - Nome no arquivo zip que ser� gerado.
	 * @param dirName - Nome do arquivo ou diret�rio a ser comprimido.
	 */
    public void zip(String zipName, String dirName) {
        ZipOutputStream out = null;
        FileOutputStream dest = null;
        CheckedOutputStream checksum = null;
        try {
            dest = new FileOutputStream(new File(zipName));
            checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            File dir = new File(dirName);
            String parent = dir.getParent();
            int length = parent.length();
            String substring = parent.substring(0, length);
            addToZip(out, dir, substring);
            System.out.println(">>>> checksum: " + checksum.getChecksum().getValue());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error err) {
            err.printStackTrace();
        } finally {
            try {
                out.flush();
                out.finish();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Error err) {
                err.printStackTrace();
            }
        }
    }
}
