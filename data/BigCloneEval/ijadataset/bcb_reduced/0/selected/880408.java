package org.jdmp.sigmen.editeur;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.jdmp.sigmen.editeur.data.Serveur;
import org.jdmp.sigmen.resources.Sound;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Lanceur du programme. Conserve en mémoire les données et les connexions aux fichiers et à la BDD.
 * @author Ilod
 *
 */
public class Main {

    /**
	 * Le fichier dans lequel est enregistré la BDD.
	 */
    private static File save;

    /**
	 * L'interface graphique afichée.
	 */
    private static Editeur editeur;

    /**
	 * L'importeur servant à vérifier et créer le squelette de la BDD. 
	 */
    private static Import importeur;

    /**
	 * La liste des objets écoutant le changement de fichier de sauvegarde.
	 */
    private static List<SaveFileListener> saveFileListener = new ArrayList<SaveFileListener>();

    /**
	 * Lance le programme.
	 * @param args aucun argument implémenté pour l'instant
	 */
    public static void main(String[] args) {
        importeur = new Import();
        Object[] options = new Object[] { "Importer depuis une BDD", "Charger depuis un fichier", "Nouveau" };
        int result = JOptionPane.showOptionDialog(null, null, "Importation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        switch(result) {
            case JOptionPane.YES_OPTION:
                fromBDD(true);
                break;
            case JOptionPane.NO_OPTION:
                fromFile(true);
                break;
            case JOptionPane.CANCEL_OPTION:
                fromNothing();
                break;
            default:
                System.exit(0);
                break;
        }
    }

    /**
	 * Charge en mémoire une BDD à partir d'un fichier.
	 * @param launch {@code true} si la fonction est exécutée au lancement du programme, et {@code false} sinon.
	 */
    private static void fromFile(boolean launch) {
        File f = openFile();
        if (f == null) {
            if (launch) {
                main(null);
            }
        } else {
            Connection conn = getTemporaryConnection();
            File fsql = unzip(f);
            try {
                RunScript.execute(conn, new FileReader(fsql));
                if (conn == null || !importeur.verifierBDD(conn)) {
                    JOptionPane.showMessageDialog(editeur, "Fichier corrompu.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    closeTemporaryConnection(conn);
                    fromFile(launch);
                } else {
                    setSaveFile(f);
                    Serveur data = new Serveur();
                    data.importer(conn);
                    changeData(data);
                    closeTemporaryConnection(conn);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(editeur, "Fichier corrompu.", "Erreur", JOptionPane.ERROR_MESSAGE);
                closeTemporaryConnection(conn);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(editeur, "Impossible d'ouvrir le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                closeTemporaryConnection(conn);
            }
            fsql.delete();
        }
    }

    public static void closeTemporaryConnection(Connection conn) {
        try {
            final File fdb = new File(conn.getMetaData().getURL().substring(8));
            conn.close();
            fdb.delete();
            DeleteDbFiles.execute(fdb.getParent(), fdb.getName(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Charge en mémoire une BDD à partir d'une BDD distante.
	 * @param launch {@code true} si la fonction est exécutée au lancement du programme, et {@code false} sinon.
	 */
    private static void fromBDD(boolean launch) {
        Connection conn = getSQLConnection();
        if (conn == null) {
            if (launch) {
                main(null);
            }
        } else if (!importeur.verifierBDD(conn)) {
            JOptionPane.showMessageDialog(editeur, "Base de données invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            fromBDD(launch);
        } else {
            Serveur data = new Serveur();
            data.importer(conn);
            changeData(data);
        }
    }

    /**
	 * Demande à l'utilisateur les informations de connexion à une BDD distante, et renvoie cette connexion.
	 * @return la connexion indiquée par l'utilisateur.
	 */
    public static Connection getSQLConnection() {
        SelectionBDD bdd = new SelectionBDD();
        try {
            synchronized (bdd) {
                bdd.wait();
            }
            if (!bdd.connect()) {
                return null;
            } else if (bdd.getConnection() == null) {
                JOptionPane.showMessageDialog(editeur, "Connexion impossible.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return getSQLConnection();
            } else {
                return bdd.getConnection();
            }
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(editeur, "Connexion impossible.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return getSQLConnection();
    }

    /**
	 * Charge en mémoire une BDD à partir d'un fichier.
	 */
    public static void fromFile() {
        fromFile(false);
    }

    /**
	 * Charge en mémoire une BDD à partir d'une BDD distante.
	 */
    public static void fromBDD() {
        fromBDD(false);
    }

    /**
	 * Charge en mémoire une BDD vierge.
	 */
    public static void fromNothing() {
        changeData(new Serveur());
    }

    /**
	 * Change les données en mémoire pour celles contenues dans {@code data}.
	 * @param data les nouvelles données à utiliser.
	 */
    public static void changeData(Serveur data) {
        if (editeur == null) {
            editeur = new Editeur(data);
        } else {
            editeur.setData(data);
        }
    }

    /**
	 * Renvoie l'interface graphique affichée.
	 * @return l'interface graphique affichée.
	 */
    public static Editeur getEditeur() {
        return editeur;
    }

    /**
	 * Renvoie l'importeur utilisé.
	 * @return l'importeur utilisé.
	 */
    public static Import getImport() {
        return importeur;
    }

    /**
	 * Renvoie le gestionnaire de ressources sonores.
	 * @return le gestionnaire de ressources sonores.
	 */
    public static Sound getSound() {
        return Sound.getSound();
    }

    /**
	 * Demande à l'utilisateur d'indiquer un fichier .sgm à ouvrir.
	 * @return le fichier choisi par l'utilisateur.
	 */
    private static File openFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fichier de sauvegarde de BDD Sigmen (*.sgm)", "sgm"));
        if (fc.showOpenDialog(editeur) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    /**
	 * Demande à l'utilisateur de choisir un fichier où sauvegarder, et rajoute l'extension .sgm si nécessaire.
	 * @return le fichier .sgm indiqué par l'utilisateur.
	 */
    private static File saveFile() {
        JFileChooser fc = new JFileChooser() {

            private static final long serialVersionUID = 794566994575209111L;

            @Override
            public void approveSelection() {
                if (!getSelectedFile().getName().toLowerCase().endsWith(".sgm")) {
                    setSelectedFile(new File(getSelectedFile().getAbsolutePath() + ".sgm"));
                }
                super.approveSelection();
            }
        };
        fc.setFileFilter(new FileNameExtensionFilter("Fichier de sauvegarde de BDD Sigmen (*.sgm)", "sgm"));
        if (fc.showSaveDialog(editeur) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                if (f.exists() && !f.delete()) {
                    throw new IOException("Impossible de supprimer le fichier.");
                }
                f.createNewFile();
                return f;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(editeur, "Impossible d'écrire sur le fichier demandé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    /**
	 * Demande à l'utilisateur de choisir un fichier de sauvegarde, et sauvegarde les données dans ce fichier.
	 * @return {@code true} si la sauvegarde a réussi, {@code false} sinon.
	 */
    public static boolean saveAs() {
        File f = saveFile();
        if (f != null) {
            setSaveFile(f);
            return saveFile(f);
        }
        return false;
    }

    /**
	 * Sauvegarde les données dans la connexion indiquée.
	 * @param conn la {@code Connection} où sauvegarder les données.
	 * @return {@code true} si la sauvegarde a réussi, {@code false} sinon.
	 */
    public static boolean save(Connection conn) {
        try {
            importeur.create(conn);
            editeur.getData().exporter(conn);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(editeur, "Erreur lors de la sauvegarde.\r\nAttention, les données risquent d'être corrompues, il est conseillé de refaire une sauvegarde.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
	 * Compresse le fichier {@code temp} en un fichier zippé {@code zip} contenant une unique entrée {@code bdd.sql}.
	 * @param temp le fichier à compresser.
	 * @param zip le fichier compressé.
	 */
    private static void zip(File temp, File zip) {
        try {
            zip.delete();
            zip.createNewFile();
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
            FileInputStream fis = new FileInputStream(temp);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            ZipEntry entry = new ZipEntry("bdd.sql");
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            fis.close();
            zos.close();
        } catch (IOException e) {
            JOptionPane.showConfirmDialog(editeur, "Impossible de créer le fichier.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Renvoit le fichier {@code bdd.sql} contenu dans le fichier zippé {@code f}.
	 * @param f le fichier d'où extraire {@code bdd.sql}
	 * @return le fichier {@code bdd.sql} contenu dans {@code f}, ou {@code null} si ce fichier n'existe pas ou si {@code f} n'est pas une archive zip valide.
	 */
    private static File unzip(File f) {
        ZipFile zip = null;
        File temp = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            zip = new ZipFile(f);
            temp = File.createTempFile("sql", ".sgm.tmp");
            int len = 0;
            byte[] data = new byte[1024];
            bos = new BufferedOutputStream(new FileOutputStream(temp));
            bis = new BufferedInputStream(zip.getInputStream(zip.getEntry("bdd.sql")));
            while ((len = bis.read(data)) != -1) {
                bos.write(data, 0, len);
            }
        } catch (IOException e) {
            temp = null;
        } finally {
            try {
                bis.close();
            } catch (Exception e2) {
            }
            try {
                bos.flush();
            } catch (Exception e2) {
            }
            try {
                bos.close();
            } catch (Exception e2) {
            }
            try {
                zip.close();
            } catch (Exception e2) {
            }
        }
        return temp;
    }

    /**
	 * Sauvegarde les données dans le fichier indiqué.
	 * @param f le fichier dans lequel sauvegarder les données.
	 * @return {@code true} si la sauvegarde a réussi, {@code false} sinon.
	 */
    public static boolean saveFile(File f) {
        Connection conn = getTemporaryConnection();
        try {
            if (save(conn)) {
                conn.commit();
                File temp = File.createTempFile("sql", ".sgm");
                Script.execute(conn.getMetaData().getURL(), conn.getMetaData().getUserName(), "", temp.getAbsolutePath());
                zip(temp, f);
                closeTemporaryConnection(conn);
                temp.delete();
                return true;
            }
        } catch (Exception e) {
            closeTemporaryConnection(conn);
        }
        return false;
    }

    /**
	 * Renvoit une {@code Connection} à partir des informations de connexion contenues dans {@code bds}, ou {@code null} si la connexion est impossible.
	 * @param bds les informations de connexion.
	 * @return la connexion correspondante, ou {@code null} si la connexion échoue.
	 */
    public static Connection tryConnect(BasicDataSource bds) {
        Connection conn;
        try {
            conn = bds.getConnection();
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
	 * Renvoie la connexion à la BDD MySQL correspondant aux données indiquées. 
	 * @param url l'URL de la BDD.
	 * @param login le nom d'utilisateur de connexion à la BDD.
	 * @param password le mot de passe correspondant au login.
	 * @return la connexion correspondante, ou {@code null} si la connexion échoue.
	 */
    public static Connection getSQLConnection(String url, String login, String password) {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("com.mysql.jdbc.Driver");
        bds.setUsername(login);
        bds.setPassword(password);
        bds.setUrl("jdbc:mysql://" + url);
        return tryConnect(bds);
    }

    /**
	 * Renvoit une connexion à une BDD HSQLDB contenue dans un fichier temporaire. 
	 * @return une connexion à une BDD temporaire, où {@code null} si le driver n'est pas disponible.
	 */
    public static Connection getTemporaryConnection() {
        try {
            File f = File.createTempFile("bdd", ".sgm");
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:" + f.getAbsolutePath(), "sa", "");
        } catch (IOException e) {
            return getTemporaryConnection();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (SQLException e) {
            return getTemporaryConnection();
        }
    }

    /**
	 * Change le fichier de sauvegarde.
	 * @param f le nouveau fichier de sauvegarde.
	 */
    private static void setSaveFile(File f) {
        File old = save;
        save = f;
        notifySaveFileListener(old);
    }

    /**
	 * Fonction appelant la méthode {@code saveFileChanged(SaveFileEvent e)} de tous les {@code SaveFileListener}.
	 * @see SaveFileListener
	 */
    private static void notifySaveFileListener(File old) {
        SaveFileEvent e = new SaveFileEvent(old, getSaveFile());
        Iterator<SaveFileListener> iter = saveFileListener.iterator();
        while (iter.hasNext()) {
            iter.next().saveFileChanged(e);
        }
    }

    /**
	 * Ajoute un {@code SaveFileListener}.
	 * @param l le {@code SaveFileListener} à ajouter.
	 * @see SaveFileListener
	 */
    public static void addSaveFileListener(SaveFileListener l) {
        saveFileListener.add(l);
    }

    /**
	 * Enlève un {@code SaveFileListener}.
	 * @param l le {@code SaveFileListener} à enlever.
	 * @see SaveFileListener
	 */
    public static void removeConnectionListener(SaveFileListener l) {
        saveFileListener.remove(l);
    }

    /**
	 * Renvoie le fichier de sauvegarde.
	 * @return le fichier de sauvegarde.
	 */
    public static File getSaveFile() {
        return save;
    }
}
