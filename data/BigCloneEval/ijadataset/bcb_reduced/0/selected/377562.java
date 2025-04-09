package com.ikarkharkov.dictour.ui;

import com.ikarkharkov.dictour.PathLocator;
import com.ikarkharkov.dictour.data.Cart;
import com.ikarkharkov.dictour.data.CartItem;
import com.ikarkharkov.dictour.data.Download;
import com.ikarkharkov.dictour.data.Phrase;
import com.ikarkharkov.dictour.db.DBService;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Transaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CartDownloadAction extends Action {

    static Logger log = Logger.getLogger(com.ikarkharkov.dictour.ui.CartDownloadAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String audioext = "amr";
        LinkedList<String> audio;
        String audiopath = null;
        String donorFolder = null;
        String link = null;
        String jarpath = null;
        String jarname = null;
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            log.error("Can not read property file  in CartDownloadAction : " + e);
            request.setAttribute("Error", "Can not read property file");
            return mapping.findForward("error");
        }
        jarpath = PathLocator.getInstance().getPath2jar();
        donorFolder = PathLocator.getInstance().getPath2donorfolder();
        audiopath = PathLocator.getInstance().getPath2audio();
        if ((jarpath == null) || (donorFolder == null) || (audiopath == null)) {
            log.error("Can not load pathes from properties in CartDownloadAction");
            request.setAttribute("Error", "Can not load pathes from properties");
            return mapping.findForward("error");
        }
        audiopath += "amr/";
        Cart c = (Cart) request.getSession().getAttribute("cart");
        if (c == null) {
            return mapping.findForward("error");
        }
        List<String> catlist = new LinkedList<String>();
        catlist.add("�����");
        Iterator it = c.getCartList().iterator();
        CartItem ci;
        while (it.hasNext()) {
            ci = (CartItem) it.next();
            if (!catlist.contains(((Phrase) ci.getPhrase()).getCategory().getName())) {
                catlist.add(((Phrase) ci.getPhrase()).getCategory().getName());
            }
        }
        StringBuffer sb = new StringBuffer();
        it = catlist.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append('\r');
            sb.append('\n');
        }
        int counter = 0;
        CartItem[] sorteditems = new CartItem[c.getCartList().size()];
        for (int i = 0; i < catlist.size(); i++) {
            it = c.getCartList().iterator();
            while (it.hasNext()) {
                ci = (CartItem) it.next();
                if (catlist.get(i).equals(ci.getPhrase().getCategory().getName())) {
                    sorteditems[counter] = ci;
                    counter++;
                }
            }
        }
        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < sorteditems.length; i++) {
            sb2.append(String.valueOf(sorteditems[i].getId()));
            if (i < 10) {
                sb2.append("00");
            } else if (i < 100) {
                sb2.append("0");
            }
            sb2.append(String.valueOf(i));
            sb2.append(sorteditems[i].getPhrase().getText());
            sb2.append('=');
            sb2.append(sorteditems[i].getId());
            sb2.append('\r');
            sb2.append('\n');
        }
        Collection items = c.getItemsCollection();
        it = items.iterator();
        audio = new LinkedList<String>();
        while (it.hasNext()) {
            audio.add(String.valueOf(((CartItem) it.next()).getId()) + '.' + audioext);
        }
        jarname = generate(10) + ".jar";
        try {
            compressJar(audio, audiopath, donorFolder, jarpath + jarname, sb.toString(), sb2.toString());
        } catch (IOException e) {
            log.error("Can not compress JAR in CartDownloadAction:" + e);
            request.setAttribute("Error", "Can not compress JAR");
            return mapping.findForward("error");
        }
        link = generate(15);
        Download d = new Download();
        d.setFilename(jarname);
        d.setPhrase_id(link);
        Transaction t = DBService.getSession().getTransaction();
        t.begin();
        DBService.saveDownload(d);
        t.commit();
        log.debug("CartDownloadAction: was download saved - " + t.wasCommitted());
        request.setAttribute("phraseid", link);
        return mapping.findForward("download");
    }

    /** Generate pseudorandom string.
     * @param length - length of generated string
     * @return - generated string
     */
    private String generate(int length) {
        String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        Random rand = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++, sb.append(symbols.charAt(rand.nextInt(symbols.length() - 1)))) ;
        return sb.toString();
    }

    /** Creates jar - file.
     * @param filenames - short names of content audiofiles
     * @param audiopath - path without names for reading audiofiles
     * @param donorFolder (fullname) - content of this folder is copyed to jar-file
     * @param outFilename (fullname) - name of jar-file
     * @param ctg0 - string, that will be rebuilded to ctg0.properties file
     * @param lng0 - string, that will be rebuilded to lng0.properties file
     * @throws IOException - throws if error occured after deleting invalid jar-file
     */
    private void compressJar(LinkedList<String> filenames, String audiopath, String donorFolder, String outFilename, String ctg0, String lng0) throws IOException {
        String filename = null;
        if (outFilename.indexOf(File.separator) != -1) {
            filename = outFilename.substring(outFilename.lastIndexOf(File.separator) + 1, outFilename.length());
        } else {
            filename = outFilename;
        }
        byte[] buf = new byte[1024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
        try {
            Iterator it = filenames.iterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                FileInputStream in = new FileInputStream(audiopath + File.separator + name);
                out.putNextEntry(new ZipEntry("sound" + File.separator + name));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            rewriteFile(donorFolder, out, "", buf);
            byte[] bytes;
            bytes = ctg0.getBytes("Cp1251");
            out.putNextEntry(new ZipEntry("ctg0.properties"));
            for (int i = 0; i < bytes.length; i++) {
                out.write(bytes[i]);
            }
            bytes = lng0.getBytes("Cp1251");
            out.putNextEntry(new ZipEntry("lng0.properties"));
            for (int i = 0; i < bytes.length; i++) {
                out.write(bytes[i]);
            }
            out.close();
        } catch (IOException e) {
            out.putNextEntry(new ZipEntry("not null"));
            out.close();
            File f = new File(outFilename);
            f.delete();
            throw new IOException();
        }
    }

    /** It is recursive function, that copying content of a folder to zipoutputstream.
     *
     * @param folder (fullpath) - folder, that content will be copyed to jar-file
     * @param out - zipoutstream for writing jar - file content
     * @param zipPath - full path to zip file (System variable, will be "" if called)
     * @param buf - filebuffer (new byte[1024];)
     * @throws IOException - if error was occured
     */
    private void rewriteFile(String folder, ZipOutputStream out, String zipPath, byte[] buf) throws IOException {
        File f = new File(folder);
        File[] content;
        FileInputStream in;
        if (f.isDirectory()) {
            content = f.listFiles();
            for (int i = 0; i < content.length; i++) {
                rewriteFile(folder + File.separator + content[i].getName(), out, zipPath + File.separator + content[i].getName(), buf);
            }
            return;
        }
        in = new FileInputStream(f);
        out.putNextEntry(new ZipEntry(zipPath));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
    }
}
