package onekin.WSL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class WSL_Util {

    public WSL_Util() {
    }

    public Object[] checkModel(File currentMap) {
        Vector<Object> list = new Vector<Object>();
        ImageIcon warning = new ImageIcon(getClass().getClassLoader().getResource("wsl_resources/images/messagebox_warning.png"));
        ImageIcon error = new ImageIcon(getClass().getClassLoader().getResource("wsl_resources/images/stop-sign.png"));
        InputStream xsltFile = getClass().getClassLoader().getResourceAsStream("wsl_resources/WSL_check_model.xsl");
        String xml = transform(new StreamSource(currentMap), xsltFile);
        String[] token;
        if (xml != null && xml.length() != 0) {
            token = xml.split("/");
            for (int i = 0; i < token.length; i++) {
                if (token[i].contains("ERROR")) {
                    list.add(error);
                } else if (token[i].contains("WARNING")) {
                    list.add(warning);
                }
                list.add(token[i]);
            }
        }
        return list.toArray();
    }

    public String transform(Source xmlSource, InputStream xsltStream) {
        Source xsltSource = new StreamSource(xsltStream);
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xsltSource);
            trans.transform(xmlSource, result);
        } catch (Exception e) {
            freemind.main.Resources.getInstance().logException(e);
            return null;
        }
        return writer.toString();
    }

    /**
	 * This method copies a source folder to a 
	 * target folder
	 * @param srcFolder
	 * @param destFolder
	 * @throws IOException
	 */
    public void copyFolder(File srcFolder, File destFolder) throws IOException {
        if (srcFolder.isDirectory()) {
            if (!destFolder.exists()) {
                destFolder.mkdir();
            }
            String[] oChildren = srcFolder.list();
            for (int i = 0; i < oChildren.length; i++) {
                copyFolder(new File(srcFolder, oChildren[i]), new File(destFolder, oChildren[i]));
            }
        } else {
            if (destFolder.isDirectory()) {
                copyFile(srcFolder, new File(destFolder, srcFolder.getName()));
            } else {
                copyFile(srcFolder, destFolder);
            }
        }
    }

    /**
	 * This method copies a source file to a target file
	 * @param srcFile
	 * @param destFile
	 * @throws IOException
	 */
    public void copyFile(File srcFile, File destFile) throws IOException {
        InputStream oInStream = new FileInputStream(srcFile);
        OutputStream oOutStream = new FileOutputStream(destFile);
        byte[] oBytes = new byte[1024];
        int nLength;
        BufferedInputStream oBuffInputStream = new BufferedInputStream(oInStream);
        while ((nLength = oBuffInputStream.read(oBytes)) > 0) {
            oOutStream.write(oBytes, 0, nLength);
        }
        oInStream.close();
        oOutStream.close();
    }
}
