package mangastreamdl.business;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import mangastreamdl.business.ms.MSParser;
import mangastreamdl.gui.Gui;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JOptionPane;
import mangastreamdl.business.ms.ProgressListener;
import org.xml.sax.SAXException;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 *
 * @author Grigor Riskov - riskogri@fit.cvut.cz
 */
public final class Downloader implements Runnable {

    private enum State {

        LIST, SINGLE
    }

    Manga manga;

    String chapter;

    File f;

    Gui gui;

    boolean running;

    MangaParser parser;

    private List<Pair<Manga, String>> list;

    private File dir;

    private State state;

    /**
     * Creates a new Downloader.
     * @param gui The gui to send download status to. Some similarities with the observer pattern.
     * @param manga The manga to be downloaded.
     * @param chapter Name of the chapter to be downloaded.
     * @param f Save directory.
     */
    public Downloader(Gui gui, Manga manga, String chapter, File f) {
        this.gui = gui;
        this.manga = manga;
        this.chapter = chapter;
        this.f = f;
        this.parser = MangaParserFactory.getParser(Sites.MS);
        running = true;
        state = State.SINGLE;
    }

    public Downloader(Gui gui, List<Pair<Manga, String>> l, File currentdir) {
        this.gui = gui;
        this.list = l;
        this.dir = currentdir;
        this.parser = new MSParser();
        running = true;
        state = State.LIST;
    }

    public void run() {
        switch(state) {
            case LIST:
                runList();
                break;
            case SINGLE:
                runSingle();
                break;
        }
        gui.done();
    }

    /**
     * Stops or resumes the Downloader.
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Parses the filename from a given string it has to be in the expected URL format.
     * @param s The string to parse.
     * @return File name.
     */
    private String parseName(String s) {
        String[] sa = s.split("/");
        return sa[6];
    }

    private void runSingle() {
        try {
            int page = 1;
            String url = parser.getMangaLocation(manga.getName(), chapter);
            if (url == null) {
                if (parser.checkManga(manga.getName())) JOptionPane.showMessageDialog(gui, "The chapter " + chapter + " of " + manga.getName() + " is no more available on mangastream.", "Chapter not available.", JOptionPane.ERROR_MESSAGE); else JOptionPane.showMessageDialog(gui, "The manga " + manga.getName() + " was not found. It was probably dropped by mangastream.", "Manga not found.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<String> links = parser.getPageLinks(url);
            gui.setChapProgressBar(links.size());
            gui.nextPage(String.valueOf(page), links.size(), manga, chapter);
            for (String string : links) {
                if (!running) break;
                ImageProperties img = parser.getImageLink(string);
                File out = new File(f.getAbsolutePath() + File.separator + "Page" + (page < 10 ? "0" : "") + page + img.getFileType());
                gui.setPgProgressBar(100);
                float percentage = 0;
                for (String ID : img.getIDs()) {
                    URI uri = new URI(img.getLocation(ID));
                    HttpURLConnection huc = new HttpURLConnection(uri.toURL(), Proxy.NO_PROXY);
                    InputStream is = huc.getInputStream();
                    ImageInputStream iis = ImageIO.createImageInputStream(is);
                    Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
                    ImageReader ir = it.next();
                    float sp = img.getSizePercentage(ID);
                    ir.addIIOReadProgressListener(new ProgressListener(gui, sp, percentage));
                    percentage += sp * 100;
                    ir.setInput(iis);
                    Image image = ir.read(0);
                    img.setData(ID, image);
                    is.close();
                }
                BufferedImage image = img.getFullImage();
                FileOutputStream fos = new FileOutputStream(out);
                String type = img.getFileType().substring(1);
                ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
                Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(type);
                ImageWriter iw = it.next();
                iw.setOutput(ios);
                iw.write(image);
                gui.nextPage(String.valueOf(page), links.size(), manga, chapter);
                page++;
                gui.updateChapProgressBar(page - 1);
                fos.close();
                ios.close();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (gui.isZip()) {
                int BUFFER = 2048;
                BufferedInputStream origin = null;
                File z = new File(f.getParent() + File.separator + f.getName() + ".zip");
                FileOutputStream fos = new FileOutputStream(z);
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
                String files[] = f.list();
                byte data[] = new byte[BUFFER];
                for (int i = 0; i < files.length; i++) {
                    FileInputStream fi = new FileInputStream(f + File.separator + files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry ze = new ZipEntry(files[i]);
                    zos.putNextEntry(ze);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        zos.write(data, 0, count);
                    }
                    origin.close();
                    fi.close();
                }
                zos.close();
                fos.close();
                if (gui.isDel()) {
                    String f2[] = f.list();
                    for (int i = 0; i < f2.length; i++) {
                        File fd = new File(f + File.separator + f2[i]);
                        System.out.println("Deleting " + fd);
                        if (!fd.delete()) System.out.println("Failed to delete " + fd);
                    }
                    f.delete();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void runList() {
        for (Pair<Manga, String> pair : list) {
            if (!running) return;
            manga = pair.first;
            chapter = pair.second;
            f = new File(dir, gui.parseFormat(manga, chapter));
            if (!f.exists()) f.mkdirs();
            runSingle();
        }
    }
}
