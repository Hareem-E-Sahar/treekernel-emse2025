package wesodi.logic.update;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import wesodi.entities.transi.DPUpdateData;
import wesodi.entities.transi.Media;
import wesodi.entities.transi.ProductUpdate;
import wesodi.logic.update.Distributor.UpdateDistributorBean;
import wesodi.logic.update.Distributor.UpdateDistributorBeanLocal;
import wesodi.util.conf.ConfigurationInitializer;

public class TestUpdateDistributor {

    URL binURL;

    ArrayList<Media> media;

    UpdateDistributorBeanLocal ud;

    @Before
    public void setUp() {
        try {
            binURL = new URL("http://www.spiegel.de/schlagzeilen/tops/index.rss");
            media = new ArrayList<Media>();
            media.add(new Media("a Video", "http://images.google.de/intl/de_ALL/images/images_hp.gif", "Video", "A video"));
        } catch (Exception e) {
            Assert.fail("wrong url");
        }
    }

    @Test
    public void testDistribute() {
        ConfigurationInitializer.run(false);
        DPUpdateData[] updateData = new DPUpdateData[] { new DPUpdateData("user", "password", "http://localhost:8090/distributionPoint/services/UpdateServiceSOAP", UpdateAction.ADD) };
        Assert.assertNotNull("URL provided", binURL);
        javax.activation.DataHandler binDH = new DataHandler(binURL);
        int l = media.size();
        javax.activation.DataHandler[] mediaDH = new DataHandler[l];
        for (int i = 0; i < l; i++) {
            try {
                mediaDH[i] = new DataHandler(new URL(media.get(i).getUrl()));
            } catch (Exception e) {
                Assert.fail("wrong url");
            }
        }
        DataSource ds = createZipStreamFromDataHandlers(mediaDH);
        javax.activation.DataHandler zipDH = new DataHandler(ds);
        ProductUpdate productUpdate = new ProductUpdate("4711", "MyProduct", "1.0", "0.9", "This is my product", media.toArray(new Media[media.size()]));
        ud = new UpdateDistributorBean(true);
        ud.distribute(updateData, binDH, zipDH, productUpdate);
        ArrayList<String> logData = ud.getDebugLog();
        for (String log : logData) {
            if (log.substring(0, 6).equals("FAILED")) {
                Assert.fail("FAILED");
            }
        }
    }

    private DataSource createZipStreamFromDataHandlers(DataHandler[] mediaDH) {
        Calendar now = new GregorianCalendar();
        String tempDir = "C:\\WesodiTemp\\";
        String nowTime = "" + now.getTimeInMillis();
        File zipOutDir = new File(tempDir);
        if (!zipOutDir.exists()) {
            zipOutDir.mkdirs();
        }
        File zipOutFile = new File(tempDir + "Temp_" + nowTime + ".zip");
        try {
            zipOutFile.createNewFile();
            FileOutputStream dest = new FileOutputStream(zipOutFile);
            ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(dest));
            for (DataHandler dh : mediaDH) {
                ZipEntry entry = new ZipEntry(new File(dh.getName()).getName());
                zipOutStream.putNextEntry(entry);
                dh.writeTo(zipOutStream);
            }
            zipOutStream.close();
            DataSource out = new FileDataSource(zipOutFile);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
