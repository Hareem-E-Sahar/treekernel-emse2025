package org.jcvi.glk.tools;

import java.io.Console;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jcvi.auth.DefaultJCVIAuthorizer;
import org.jcvi.commonx.auth.tigr.DefaultProjectDbAuthorizer;
import org.jcvi.glk.Extent;
import org.jcvi.glk.Library;
import org.jcvi.glk.ctm.CTMElviraGLKSessionBuilder;
import org.jcvi.glk.ctm.CTMUtil;
import org.jcvi.glk.helpers.GLKHelper;
import org.jcvi.glk.helpers.HibernateGLKHelper;

public class CreateGLKLibrary {

    /**
     * 
     * @param args
     * server
     * project
     * user
     * library name
     * min library size
     * max library size
     * comment
     * will prompt for password
     */
    public static void main(String[] args) {
        String server = args[0];
        String project = args[1];
        String user = args[2];
        String libraryName = args[3];
        int min = Integer.parseInt(args[4]);
        int max = Integer.parseInt(args[5]);
        String comment = args[6];
        int nominal = (min + max) / 2;
        Console cons = System.console();
        char[] passwd;
        if (cons == null || (passwd = cons.readPassword("[%s]", "Password:")) == null) {
            throw new RuntimeException("invalid password");
        }
        Session session = new CTMElviraGLKSessionBuilder(new DefaultProjectDbAuthorizer.Builder(new DefaultJCVIAuthorizer(user, passwd)).server(server).project(project).build()).build();
        GLKHelper helper = new HibernateGLKHelper(session);
        Extent root = helper.getRootExtent();
        Transaction tx = session.beginTransaction();
        try {
            Library library = new Library(libraryName, root, nominal, comment);
            library.setMaxSize(max);
            library.setMinSize(min);
            session.save(library);
            tx.commit();
        } catch (Throwable t) {
            t.printStackTrace();
            tx.rollback();
        }
        session.close();
    }
}
