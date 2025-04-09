package org.commonmap.cmarender;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.batik.transcoder.TranscoderException;
import org.commonmap.util.Tile;
import org.commonmap.turtleeggs.Distribution;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.commonmap.cmarender.imageprocess.NeuQuantImageOp;
import org.commonmap.turtleeggs.TurtleEggs;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.w3c.dom.Document;

/**
 *
 * @author nazotoko <nazotoko@commonmap.info>
 * @version $Id: Request.java 48 2010-02-24 00:29:31Z nazotoko $
 */
public class Request {

    private Tile[] tiles;

    private Tile tile = null;

    private ConfigurationCmarender config;

    private File tempDir;

    private Osmarender osmarender;

    private Cmarender cmarender;

    private Logger logger;

    public Request(Cmarender parent, ConfigurationCmarender config, Logger l) throws TransformerConfigurationException, ParserConfigurationException {
        this.cmarender = parent;
        this.config = config;
        this.logger = l;
        tempDir = new File(config.directory(), config.get("temp"));
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        File styleDirectory = new File(config.directory(), config.get("stylesheets"));
        try {
            updateStyle(styleDirectory);
        } catch (SVNException ex) {
            logger.log(Level.SEVERE, "SVN ERROR", ex);
        }
        logger.log(Level.INFO, "prepering osmarender..");
        osmarender = new Osmarender(l);
        try {
            osmarender.setStyle(styleDirectory, config.get("xslt"));
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "osmarender stylefile cannot open.", ex);
        }
        logger.log(Level.INFO, "..done.");
    }

    /**
     * taking a request from cmarender (or tiles&at;home server) job queue.
     * This uses POST.
     * @return true if it take a request.
     * @throws MalformedURLException
     * @throws IOException
     */
    public boolean take() throws MalformedURLException, IOException {
        URL u = new URL(config.get("requestURL"));
        URLConnection uc = u.openConnection();
        boolean ret = false;
        uc.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
        osw.write("user=" + config.get("username") + "&passwd=" + config.get("password"));
        osw.write("&version=" + config.get("version") + "&max_complexity=" + config.get("MaxComplexity"));
        osw.write("&layerspossible=" + config.get("layerPossible"));
        osw.flush();
        BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
        String line = rd.readLine();
        if (line == null) {
            logger.log(Level.INFO, "no answer");
            ret = false;
        } else {
            String[] split = line.split("\\|");
            if (split[0].equals("OK") && split[1].equals("5")) {
                tile = new Tile();
                tile.setLayer(split[5]);
                tile.setZXY(Integer.parseInt(split[4]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                logger.log(Level.INFO, "get request: " + tile);
                ret = true;
            } else if (split[0].equals("XX") && split[1].equals("5")) {
                logger.log(Level.INFO, "answer: " + line);
                ret = false;
            } else {
                logger.log(Level.INFO, "protocal error\nthe answer: " + line);
                while ((line = rd.readLine()) != null) {
                    logger.log(Level.INFO, line);
                }
                ret = false;
            }
        }
        osw.close();
        rd.close();
        if (ret == false) {
            tile = null;
        }
        return ret;
    }

    /**
     * manual request
     * @param t
     */
    public void setRequest(Tile t) {
        tile = t;
    }

    /**
     * 
     * @param dist
     * @throws MalformedURLException url
     * @throws IOException some io
     * @throws TransformerException saxon
     * @throws TranscoderException batik
     * @throws InterruptedException 
     */
    public void routin(Distribution dist) throws MalformedURLException, IOException, TransformerException, TranscoderException, InterruptedException {
        if (tile != null) {
            logger.log(Level.INFO, "job: " + tile.toString());
            if (tile.getZ() < 11) {
                pngStech(dist);
            } else {
                highZoom(dist);
            }
        }
    }

    private void pngStech(Distribution dist) throws MalformedURLException, InterruptedException {
        int i;
        BufferedImage bi0 = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage bi;
        Graphics2D g = (Graphics2D) bi0.getGraphics();
        Tile[] tset = tile.highLevels();
        try {
            bi = ImageIO.read(dist.get(tset[0]));
            g.drawImage(bi, new AffineTransform(0.5, 0, 0, 0.5, 0, 0), null);
            bi = ImageIO.read(dist.get(tset[1]));
            g.drawImage(bi, new AffineTransform(0.5, 0, 0, 0.5, 0, 128), null);
            bi = ImageIO.read(dist.get(tset[2]));
            g.drawImage(bi, new AffineTransform(0.5, 0, 0, 0.5, 128, 0), null);
            bi = ImageIO.read(dist.get(tset[3]));
            g.drawImage(bi, new AffineTransform(0.5, 0, 0, 0.5, 128, 128), null);
            g.dispose();
            logger.log(Level.INFO, "Steched. Optimizing..");
            BufferedImage bi8 = new NeuQuantImageOp().filter(bi0, null);
            OutputStream original = dist.addByOutputStream(tile);
            ImageIO.write(bi8, "PNG", original);
            logger.log(Level.INFO, "...done!");
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        }
    }

    public boolean update(TurtleEggs turtleEggs) throws MalformedURLException, IOException {
        URL u = new URL(config.get("updateURL"));
        URLConnection uc = u.openConnection();
        boolean ret = false;
        logger.log(Level.INFO, "update request.");
        uc.setDoOutput(true);
        OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
        osw.write("user=" + config.get("username") + "&passwd=" + config.get("password"));
        osw.write("&version=" + config.get("version"));
        osw.write("&id=" + turtleEggs.getID());
        osw.write("&layers=" + tile.getLayer());
        osw.write("&z=" + tile.getZ());
        osw.write("&x=" + tile.getX() + "&y=" + tile.getY());
        osw.flush();
        BufferedReader rd = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
        String line = rd.readLine();
        if (line == null) {
            logger.log(Level.INFO, "no answer");
            ret = false;
        } else {
            logger.log(Level.INFO, "answer: " + line);
            while ((line = rd.readLine()) != null) {
                logger.log(Level.INFO, line);
            }
            ret = true;
        }
        osw.close();
        rd.close();
        return ret;
    }

    /**
     * upload created file
     * @param dist
     * @return
     * @throws FileNotFoundException
     */
    public boolean upload(Distribution dist) throws FileNotFoundException {
        byte[] buf = new byte[4096];
        int i;
        logger.log(Level.INFO, "Zipping...");
        try {
            FileOutputStream fos = new FileOutputStream(new File(tempDir, tile.toString() + ".zip"));
            ZipOutputStream zipout = new ZipOutputStream(fos);
            zipout.setLevel(0);
            int j, k;
            int maxz;
            int num = 1;
            int z = tile.getZ(), x = tile.getX(), y = tile.getY();
            if (z < 11) {
                maxz = z;
            } else {
                maxz = Integer.parseInt(config.get("layers." + tile.getLayer() + ".maxz"));
            }
            Tile t = new Tile();
            t.setLayer(tile.getLayer());
            for (; z <= maxz; z++, num <<= 1, x <<= 1, y <<= 1) {
                for (k = 0; k < num; k++) {
                    for (j = 0; j < num; j++) {
                        t.setLayer(tile.getLayer());
                        t.setZXY(z, x + k, y + j);
                        InputStream is = dist.getFromLocal(t);
                        ZipEntry zipe = new ZipEntry(t.toFileName());
                        zipout.putNextEntry(zipe);
                        while ((i = is.read(buf)) >= 0) {
                            zipout.write(buf, 0, i);
                        }
                        zipout.closeEntry();
                        is.close();
                    }
                }
            }
            zipout.close();
            fos.close();
            logger.log(Level.INFO, "done!");
            logger.log(Level.INFO, "uploading..");
            HttpClient httpclient = new HttpClient();
            PostMethod method = new PostMethod(config.get("uploadURL"));
            Part[] formparam = new Part[] { new StringPart("user", config.get("username")), new StringPart("passwd", config.get("password")), new StringPart("layer", tile.getLayer()), new StringPart("z", Integer.toString(tile.getZ())), new FilePart("file", new File(tempDir, tile.toString() + ".zip")) };
            MultipartRequestEntity m = new MultipartRequestEntity(formparam, method.getParams());
            method.setRequestEntity(m);
            int response = httpclient.executeMethod(method);
            if (response == 200) {
                logger.log(Level.INFO, ".." + method.getStatusText());
                BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    logger.log(Level.FINE, line);
                }
                for (File f : tempDir.listFiles()) {
                    f.delete();
                }
            }
            return true;
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, "Zip exception: " + ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IO exception: " + ex);
        }
        return false;
    }

    private void highZoom(Distribution dist) throws MalformedURLException, IOException, TransformerException, TranscoderException, InterruptedException {
        getDataFromAPI();
        int z = tile.getZ();
        int width = 256;
        int num = 1;
        int i, j;
        int maxz;
        if (z < 11) {
            maxz = z;
        } else {
            maxz = Integer.parseInt(config.get("layers." + tile.getLayer() + ".maxz"));
        }
        int x = tile.getX(), y = tile.getY();
        for (; z <= maxz; z++, num <<= 1, width <<= 1, x <<= 1, y <<= 1) {
            logger.log(Level.INFO, "zoom level " + z);
            Document doc = osmarender.transform(config.get("layers." + tile.getLayer() + ".rule.head") + z + config.get("layers." + tile.getLayer() + ".rule.tail"), new File(tempDir, tile.toString() + ".osm"));
            BufferedImage big = osmarender.rasterize(doc, new Float(width));
            BufferedImage im;
            Tile ct;
            for (i = 0; i < num; i++) {
                for (j = 0; j < num; j++) {
                    im = big.getSubimage(256 * i, 256 * j, 256, 256);
                    ct = new Tile();
                    ct.setLayer(tile.getLayer());
                    ct.setZXY(z, (x + i), (y + j));
                    BufferedImage dest = new NeuQuantImageOp().filter(im, null);
                    OutputStream os = dist.addByOutputStream(ct);
                    ImageIO.write(dest, "PNG", os);
                    os.flush();
                    os.close();
                }
            }
        }
    }

    private void updateStyle(File styleDirectory) throws SVNException {
        DAVRepositoryFactory.setup();
        SVNClientManager clientManager;
        clientManager = SVNClientManager.newInstance();
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        SVNURL svnurl = SVNURL.parseURIEncoded(config.get("stylesheetsSVN"));
        try {
            if (styleDirectory.exists()) {
                logger.log(Level.INFO, "Updating style files.");
                updateClient.doUpdate(styleDirectory, SVNRevision.HEAD, true);
            } else {
                logger.log(Level.INFO, "Downloading style files.");
                updateClient.doCheckout(svnurl, styleDirectory, SVNRevision.HEAD, SVNRevision.HEAD, true);
            }
        } catch (NullPointerException ex) {
        }
    }

    private void getDataFromAPI() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "Downloading data from API ");
        URL api = null;
        api = new URL(config.get("APIURL") + "map?" + tile.bbox());
        File file = new File(tempDir, tile.toString() + ".osm");
        URLConnection connection = api.openConnection();
        logger.log(Level.INFO, "conecting..");
        connection.setConnectTimeout(300);
        logger.log(Level.INFO, "[" + connection.getContentLength() + "byte] ..");
        InputStream is = connection.getInputStream();
        FileOutputStream os = new FileOutputStream(file, false);
        byte[] buf = new byte[4096];
        int i;
        while ((i = is.read(buf)) >= 0) {
            os.write(buf, 0, i);
        }
        os.close();
        is.close();
        logger.log(Level.INFO, "..done");
    }
}
