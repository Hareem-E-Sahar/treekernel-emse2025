package de.fzj.pkikits.ra.servlets.modules;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMWriter;
import pl.edu.icm.pnpca.ca.CertificateArchive;
import pl.edu.icm.pnpca.ca.CertificateInfo;
import pl.edu.icm.pnpca.webapp.RequestUtilities;

public class CreateCertArchive extends HttpServlet {

    private static final long serialVersionUID = 12234234L;

    protected static Logger logger = Logger.getLogger(CreateCertArchive.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache");
        res.setDateHeader("Expires", 0);
        res.setContentType("multipart/x-zip");
        ServletOutputStream out = res.getOutputStream();
        try {
            createArchive(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.flush();
    }

    private void createArchive(OutputStream out) throws Exception {
        try {
            ZipOutputStream zos = new ZipOutputStream(out);
            PEMWriter pw = new PEMWriter(new OutputStreamWriter(zos));
            CertificateArchive arch = RequestUtilities.getCertificateArchive(getServletContext());
            zos.setComment("Certificates archive created on " + new Date().toString());
            List<CertificateInfo> certs = arch.getValidCertificates();
            for (CertificateInfo ci : certs) {
                ZipEntry ze = new ZipEntry(ci.getId() + ".pem");
                if (ci.getIssue().getDate() != null) {
                    ze.setTime(ci.getIssue().getDate().getTime());
                }
                zos.putNextEntry(ze);
                zos.flush();
                pw.writeObject(ci.getCertificate());
                pw.flush();
                zos.closeEntry();
            }
            zos.close();
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
}
