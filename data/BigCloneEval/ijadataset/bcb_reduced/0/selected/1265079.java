package photospace.meta.rdf;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.w3c.tools.jpeg.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

public class JpegRdfTest extends TestCase {

    private static final Log log = LogFactory.getLog(JpegRdfTest.class);

    public void testPhotospaceModel() throws Exception {
        Model model = getPhotoModel();
        model.write(System.out);
    }

    private Model getPhotoModel() {
        Model model = ModelFactory.createDefaultModel();
        Resource asset = model.createResource();
        asset.addProperty(DC.title, "test title");
        asset.addProperty(DC.description, "test description");
        asset.addProperty(DC.type, DCTypes.Image);
        asset.addProperty(DC.creator, model.createResource("http://photospace.org/people/alon").addProperty(VCARD.FN, "Alon Salant").addProperty(VCARD.EMAIL, "alon@salant.org"));
        asset.addProperty(DC.date, new Date());
        return model;
    }

    public void testReadWriteRdf() throws Exception {
        File jpeg = new File(System.getProperty("project.root"), "build/test/exif-rdf.jpg");
        Model model = getPhotoModel();
        ByteArrayOutputStream rdf = new ByteArrayOutputStream();
        model.write(rdf);
        log.debug("RDF to write to jpeg:\n" + rdf);
        ByteArrayOutputStream jpegOS = new ByteArrayOutputStream();
        JpegCommentWriter jcw = new JpegCommentWriter(jpegOS, new FileInputStream(jpeg));
        jcw.write(rdf.toString());
        jcw.close();
        FileOutputStream fos = new FileOutputStream(jpeg);
        fos.write(jpegOS.toByteArray());
        fos.close();
        FileInputStream jpegIn = new FileInputStream(jpeg);
        JpegHeaders jh = new JpegHeaders(jpegIn, new org.w3c.tools.jpeg.Exif());
        String comments = StringUtils.join(jh.getComments(), "");
        jpegIn.close();
        assertNotNull(comments);
        log.info("EXIF comments from jpeg:\n" + comments);
        Model fromJpeg = ModelFactory.createDefaultModel();
        fromJpeg.read(new StringReader(comments), "");
        fromJpeg.write(System.out);
        log.info("RDF from EXIF comments:\n" + comments);
    }

    public void dump() throws Exception {
        File jpeg = new File(System.getProperty("project.root"), "build/test/exif-rdf.jpg");
        photospace.meta.Reader reader = new photospace.meta.Reader(jpeg);
        log.info(reader.getDump());
    }
}
