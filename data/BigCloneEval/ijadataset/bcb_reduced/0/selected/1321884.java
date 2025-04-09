package br.edu.ufcg.lsd.seghidro.server.files;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import br.edu.ufcg.lsd.seghidro.server.autentication.FilesManager;
import br.edu.ufcg.lsd.seghidro.server.persistencia.DownloadProperties;

/**
 * Servlet que faz o download de arquivos zip para ao usuário
 * 
 * @author Romeryto Lira
 * 
 */
public class MyZipFileServlet extends HttpServlet {

    private static final long serialVersionUID = 5485286675963962465L;

    private final String FILE_SEPARATOR = System.getProperty("file.separator");

    DownloadProperties properties = DownloadProperties.getInstance();

    private final String DEFAULT_PATH = properties.getProperty(DownloadProperties.DOWNLOAD_DIRECTORY);

    public static final String DOWNLOAD_DIR_ATRIBUTE = "downloaddir";

    FilesManager filesManager = FilesManager.getInstance();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nomeDoArquivoZip = request.getParameter("filename");
        int idSessao = Integer.parseInt(request.getParameter("idsessao"));
        String loginUsuario = request.getParameter("login");
        List<String> listaDeNomesDosArquivos = filesManager.getFilesNames(idSessao);
        byte b[] = new byte[1024];
        try {
            OutputStream out = response.getOutputStream();
            ZipOutputStream zout = new ZipOutputStream(out);
            response.setHeader("Content-Disposition", "attachment;filename=" + nomeDoArquivoZip);
            response.setContentType("application/zip");
            for (int i = 0; i < listaDeNomesDosArquivos.size(); i++) {
                String nomeArquivoAtual = listaDeNomesDosArquivos.get(i);
                if (nomeArquivoAtual != null) {
                    File arquivo = new File(DEFAULT_PATH + FILE_SEPARATOR + idSessao + "-" + loginUsuario + FILE_SEPARATOR + nomeArquivoAtual);
                    System.out.println(DEFAULT_PATH + FILE_SEPARATOR + idSessao + "-" + loginUsuario + FILE_SEPARATOR + nomeArquivoAtual);
                    InputStream in = new ByteArrayInputStream(getArrayBytes(arquivo));
                    ZipEntry e = new ZipEntry(nomeArquivoAtual);
                    zout.putNextEntry(e);
                    int len = 0;
                    while ((len = in.read(b)) != -1) {
                        zout.write(b, 0, len);
                    }
                }
                zout.closeEntry();
            }
            zout.close();
            out.close();
        } catch (IOException ex) {
            System.err.println("Erro no Download do arquivo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public byte[] getArrayBytes(File file) {
        int size = (int) file.length();
        if (size > Integer.MAX_VALUE) {
            System.out.println("File is to larger");
        }
        byte[] bytes = new byte[size];
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        int read = 0;
        int numRead = 0;
        try {
            while (read < bytes.length && (numRead = dis.read(bytes, read, bytes.length - read)) >= 0) {
                read = read + numRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File size: " + read);
        if (read < bytes.length) {
            System.out.println("Could not completely read: " + file.getName());
        }
        return bytes;
    }
}
