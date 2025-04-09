package ppltrainer.model.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import ppltrainer.model.Answer;
import ppltrainer.model.Question;
import ppltrainer.model.QuestionCatalog;

/**
 *
 * @author marc
 */
public class CatalogWriter {

    public void wirteCatalog(QuestionCatalog catalog, OutputStream out) throws IOException {
        ZipOutputStream zOut = new ZipOutputStream(out);
        try {
            ZipEntry entry = new ZipEntry("info.xml");
            JAXBContext context = JAXBContext.newInstance(QuestionCatalog.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(m.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            zOut.putNextEntry(entry);
            m.marshal(catalog, zOut);
            for (int i = 0; i < catalog.getQuestions().size(); i++) {
                this.putQuestion(catalog.getQuestions().get(i), zOut);
            }
        } catch (JAXBException ex) {
            Logger.getLogger(CatalogWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        zOut.close();
    }

    private void putQuestion(Question q, ZipOutputStream out) throws IOException, JAXBException {
        ZipEntry entry = new ZipEntry("question" + q.getNumber() + ".xml");
        out.putNextEntry(entry);
        JAXBContext context = JAXBContext.newInstance(Question.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(m.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(q, out);
        if (q.hasImage()) {
            entry = new ZipEntry("question" + q.getNumber() + ".png");
            out.putNextEntry(entry);
            ImageIO.write(q.getImage(), "png", out);
        }
        for (int i = 0; i < q.getAnswers().size(); i++) {
            Answer a = q.getAnswers().get(i);
            if (a.hasImage()) {
                entry = new ZipEntry("question" + q.getNumber() + "_answer" + a.getLetter() + ".png");
                out.putNextEntry(entry);
                ImageIO.write(a.getImage(), "png", out);
            }
        }
    }
}
