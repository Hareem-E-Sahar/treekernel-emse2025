package org.fudaa.fudaa.hydraulique1d;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.fudaa.ctulu.CtuluLibMessage;
import org.fudaa.dodico.boony.BoonyXmlDeserializer;
import org.fudaa.dodico.boony.BoonyXmlSerializer;
import org.fudaa.dodico.hydraulique1d.metier.MetierBief;
import org.fudaa.dodico.hydraulique1d.metier.MetierCasier;
import org.fudaa.dodico.hydraulique1d.metier.MetierEtude1d;
import org.fudaa.dodico.hydraulique1d.metier.MetierExtremite;
import org.fudaa.dodico.hydraulique1d.metier.MetierLiaison;
import org.fudaa.dodico.hydraulique1d.metier.MetierNoeud;
import org.fudaa.dodico.hydraulique1d.metier.MetierReseau;
import org.fudaa.dodico.hydraulique1d.metier.MetierSingularite;
import org.fudaa.fudaa.hydraulique1d.ihmhelper.Hydraulique1dIHMRepository;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauBarragePrincipal;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauBiefCourbe;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauCasier;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauExtremLibre;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauFrame;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauGridAdapter;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauLiaisonCasier;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauMouseAdapter;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauNoeud;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauSingularite;
import org.fudaa.fudaa.hydraulique1d.reseau.Hydraulique1dReseauVerificateur;
import com.memoire.bu.BuCommonImplementation;
import com.memoire.bu.BuCommonInterface;
import com.memoire.bu.BuDialogError;
import com.memoire.dja.DjaFrame;
import com.memoire.dja.DjaGrid;
import com.memoire.dja.DjaGridInteractive;
import com.memoire.dja.DjaVector;
import com.memoire.yapod.YapodDeserializer;
import com.memoire.yapod.YapodSerializer;
import com.memoire.yapod.YapodXmlDeserializer;

/**
 * Classe qui permet de lire et d’�crire les fichiers ��.masc�� ou XML.
 * @version      $Revision: 1.26 $ $Date: 2008-02-11 10:11:12 $ by $Author: jm_lacombe $
 * @author       Jean-Marc Lacombe
 */
public class Hydraulique1dProjet {

    protected BuCommonImplementation impl_;

    protected MetierEtude1d ietude1d_ = null;

    protected DjaFrame f_ = null;

    protected Hydraulique1dIHMRepository ihmP_ = null;

    protected int etatMenu_ = 0;

    protected File fichier_;

    private static String FORMAT_DJA_VERSION = "0.02";

    private static String FORMAT_DJA_VERSION_OLD_READER = "0.01";

    private static String FORMAT_DJA_VERSION_NEW_READER = "0.02";

    protected Hydraulique1dProjet() {
    }

    public static Hydraulique1dProjet getInstance() {
        if (instance == null) {
            instance = new Hydraulique1dProjet();
        }
        return instance;
    }

    private static Hydraulique1dProjet instance = null;

    public void setCommonImplementation(BuCommonImplementation impl) {
        impl_ = impl;
    }

    public void setEtude1d(MetierEtude1d ietude1d) {
        ietude1d_ = ietude1d;
    }

    public void setIhmRepository(Hydraulique1dIHMRepository ihmP) {
        ihmP_ = ihmP;
    }

    public void setDjaFrame(DjaFrame f) {
        f_ = f;
    }

    public DjaFrame getDjaFrame() {
        return f_;
    }

    public MetierEtude1d getEtude1d() {
        return ietude1d_;
    }

    public File getFile() {
        return fichier_;
    }

    public void enregistre() {
        enregistreSous(fichier_);
    }

    /**
   * Ouverture du fichier masc
   * @param fichier Le nom du fichier � ouvrir avec l'extension.
   * @return true si l'ouveture s'est bien d�roul�e.
   */
    public boolean ouvrir(File fichier) {
        fichier_ = fichier;
        ZipFile zf = null;
        boolean isZip = false;
        try {
            zf = new ZipFile(fichier);
            isZip = true;
        } catch (Exception _exc) {
        }
        if (isZip) {
            try {
                ZipEntry entry;
                entry = zf.getEntry("etude1d.xml");
                byte[] b = new byte[1000];
                zf.getInputStream(entry).read(b);
                String DebutEtude = new String(b);
                int indexVersion = DebutEtude.indexOf("<single type=\"String\">");
                String versionEtude = DebutEtude.substring(indexVersion + 22, indexVersion + 25);
                System.err.println("version de l'Etude lu " + versionEtude);
                ietude1d_ = ietude1d_.readFrom(zf.getInputStream(entry), false, versionEtude);
                entry = zf.getEntry("reseau.xml");
                djaFrameReadFrom(zf.getInputStream(entry), FORMAT_DJA_VERSION);
            } catch (OutOfMemoryError ex) {
                System.err.println("$$$ " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "M�moire insuffisante pour lire le fichier").activate();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Throwable ex) {
                System.err.println("$$$ " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "Fichier incorrect").activate();
                return false;
            } finally {
                try {
                    zf.close();
                } catch (IOException _e) {
                }
            }
        } else {
            try {
                DataInputStream fluxLu = new DataInputStream(new BufferedInputStream(new FileInputStream(fichier_)));
                String versionEtude = "" + fluxLu.readChar() + fluxLu.readChar() + fluxLu.readChar();
                if (versionEtude.equals(MetierEtude1d.FORMAT_VERSION_0_4)) {
                    if (CtuluLibMessage.DEBUG) CtuluLibMessage.debug("ancienne sauvegarde");
                } else if (ietude1d_.versionFormat().compareTo(versionEtude) > 0) {
                    new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "le format de fichier n'est plus support� : \n" + "Version fichier Etude hydraulique1d : " + versionEtude + "\n" + "Version actuel  Etude hydraulique1d : " + ietude1d_.versionFormat()).activate();
                    return false;
                } else if (ietude1d_.versionFormat().compareTo(versionEtude) > 0) {
                    new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "le fichier est incorrect (n� version impossible) : \n" + "Version fichier Etude hydraulique1d : " + versionEtude + "\n" + "Version actuel  Etude hydraulique1d : " + ietude1d_.versionFormat()).activate();
                    return false;
                }
                byte[] bytes = new byte[fluxLu.readInt()];
                fluxLu.read(bytes);
                InputStream bais = new ByteArrayInputStream(bytes);
                ietude1d_ = ietude1d_.readFrom(bais, true, versionEtude);
                bytes = null;
                String versionDJA = "" + fluxLu.readChar() + fluxLu.readChar() + fluxLu.readChar() + fluxLu.readChar();
                if (versionDJA.equals(FORMAT_DJA_VERSION_OLD_READER) && FORMAT_DJA_VERSION.equals(FORMAT_DJA_VERSION_NEW_READER)) {
                    if (CtuluLibMessage.DEBUG) CtuluLibMessage.debug("Dja: Read old version");
                } else if (FORMAT_DJA_VERSION.compareTo(versionDJA) > 0) {
                    new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "le format de fichier est d�suet : \n" + "Version fichier DJA ou Mascaret : " + versionDJA + "\n" + "Version actuel  DJA ou Mascaret : " + FORMAT_DJA_VERSION).activate();
                    return false;
                }
                fluxLu.readInt();
                djaFrameReadFrom(fluxLu, versionDJA);
                fluxLu.close();
            } catch (IOException ex) {
                System.err.println("$$$ " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "Erreur d'entr�es - sorties").activate();
                return false;
            } catch (OutOfMemoryError ex) {
                System.err.println("$$$ " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "M�moire insuffisante pour lire le fichier").activate();
                return false;
            } catch (NullPointerException ex) {
                System.err.println("AAA " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "Fichier incorrect").activate();
                return false;
            } catch (Throwable ex) {
                System.err.println("$$$ " + ex);
                ex.printStackTrace();
                new BuDialogError((BuCommonInterface) Hydraulique1dBaseApplication.FRAME, ((BuCommonInterface) Hydraulique1dBaseApplication.FRAME).getImplementation().getInformationsSoftware(), "Fichier incorrect").activate();
                return false;
            }
        }
        etatMenu_ = ietude1d_.etatMenu();
        initCompteurOrdinalProfils();
        if (f_ instanceof Hydraulique1dReseauFrame) ((Hydraulique1dReseauFrame) f_).ConstructionLiaisonsMetiers(); else Hydraulique1dReseauVerificateur.verifierBijectionMetierGraph(ietude1d_, f_);
        return true;
    }

    public static void traduitMascToXML(File fichierMasc, File fichierXmlReseau, File fichierXmlEtude) {
        try {
            if (!fichierMasc.exists()) return;
            DataInputStream FluxLu = new DataInputStream(new BufferedInputStream(new FileInputStream(fichierMasc)));
            String versionEtude = "" + FluxLu.readChar() + FluxLu.readChar() + FluxLu.readChar();
            System.out.println("version etude :" + versionEtude);
            byte[] binaireEtudeServeur = new byte[FluxLu.readInt()];
            FluxLu.read(binaireEtudeServeur);
            ByteArrayInputStream bais = new ByteArrayInputStream(binaireEtudeServeur);
            InputStream zipIs = new GZIPInputStream(bais);
            OutputStream fluxEcrit = new BufferedOutputStream(new FileOutputStream(fichierXmlEtude));
            int r;
            while ((r = zipIs.read()) != -1) {
                fluxEcrit.write(r);
            }
            zipIs.close();
            fluxEcrit.close();
            binaireEtudeServeur = null;
            String versionDJA = "" + FluxLu.readChar() + FluxLu.readChar() + FluxLu.readChar() + FluxLu.readChar();
            System.out.println("version DJA :" + versionDJA);
            byte[] binaireDja = new byte[FluxLu.readInt()];
            FluxLu.read(binaireDja);
            InputStream is = new ByteArrayInputStream(binaireDja);
            fluxEcrit = new BufferedOutputStream(new FileOutputStream(fichierXmlReseau));
            while ((r = is.read()) != -1) {
                fluxEcrit.write(r);
            }
            is.close();
            fluxEcrit.close();
            binaireDja = null;
            FluxLu.close();
        } catch (IOException ex) {
            System.err.println("$$$ " + ex);
            ex.printStackTrace();
        }
    }

    public void importXML(File fichierEtude, File fichierReseau) {
        try {
            System.out.println("Lecture du fichier XML contenant l'�tude : " + fichierEtude.getName());
            MetierEtude1d etd = null;
            FileInputStream in;
            in = new FileInputStream(fichierEtude);
            etd = ietude1d_.readFrom(in, false, MetierEtude1d.FORMAT_VERSION_0_4);
            in.close();
            if (etd == null) {
                in = new FileInputStream(fichierEtude);
                byte[] b = new byte[1000];
                in.read(b);
                String DebutEtude = new String(b);
                int indexVersion = DebutEtude.indexOf("<single type=\"String\">");
                String versionEtude = DebutEtude.substring(indexVersion + 22, indexVersion + 25);
                in = new FileInputStream(fichierEtude);
                etd = ietude1d_.readFrom(in, false, versionEtude);
                in.close();
            }
            ietude1d_ = etd;
            if (fichierReseau != null) {
                System.out.println("Lecture du fichier XML contenant le r�seau : " + fichierReseau.getName());
                f_ = null;
                try {
                    FileInputStream fos = new FileInputStream(fichierReseau);
                    djaFrameReadFrom(fos, FORMAT_DJA_VERSION);
                    fos.close();
                } catch (Exception e) {
                    try {
                        if (f_ == null) {
                            FileInputStream fos = new FileInputStream(fichierReseau);
                            djaFrameReadFrom(fos, FORMAT_DJA_VERSION);
                            fos.close();
                        }
                    } catch (Exception exep) {
                        System.err.println("$$$ " + exep);
                        exep.printStackTrace();
                    }
                }
            }
            etatMenu_ = ietude1d_.etatMenu();
            initCompteurOrdinalProfils();
        } catch (IOException ex) {
            System.err.println("$$$ " + ex);
            ex.printStackTrace();
        }
    }

    private void initCompteurOrdinalProfils() {
        if (ietude1d_.reseau() != null) {
            MetierBief[] biefs = ietude1d_.reseau().biefs();
            if (biefs != null) {
                int compteur = 0;
                for (int i = 0; i < biefs.length; i++) {
                    MetierBief b = biefs[i];
                    compteur += b.profils().length;
                }
                Hydraulique1dProfilModel.initCompteurOrdinal(compteur);
            }
        }
    }

    /**
   * Exportation de l'�tude sous forme XML.
   * @param nomFichier Le nom du fichier sur lequel sera ecrit la s�rialisation.
   * @throws IOException
   */
    public void exportXMLEtude1d(File nomFichier) throws IOException {
        DataOutputStream fluxEcrit = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nomFichier)));
        ietude1d_.writeTo(fluxEcrit);
        fluxEcrit.close();
    }

    public void exportXMLReseau(File nomFichier) throws IOException {
        DataOutputStream fluxEcrit = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nomFichier)));
        djaFrameWriteTo(fluxEcrit);
        fluxEcrit.close();
    }

    /**
   * Enregistre le fichier .masc sous format zip.
   * @param fichier Le nom du fichier � sauver.
   */
    public void enregistreSous(File fichier) {
        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(fichier));
            ZipEntry entry = new ZipEntry("etude1d.xml");
            zip.putNextEntry(entry);
            ietude1d_.writeTo(zip);
            zip.closeEntry();
            entry = new ZipEntry("reseau.xml");
            zip.putNextEntry(entry);
            djaFrameWriteTo(zip);
            zip.closeEntry();
            zip.close();
            fichier_ = fichier;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void djaFrameWriteTo(OutputStream _out) {
        if (f_ == null) return;
        Object[] infosDja = new Object[4];
        setNumDjaHydrau1d(f_.getGrid());
        infosDja[0] = FORMAT_DJA_VERSION;
        infosDja[1] = f_.getLocation();
        infosDja[2] = f_.getSize();
        infosDja[3] = f_.getGrid().getObjects();
        try {
            BoonyXmlSerializer fluxEcrit = new BoonyXmlSerializer(false, true);
            fluxEcrit.startWriting(_out);
            fluxEcrit.write(infosDja);
            fluxEcrit.endWriting();
        } catch (Exception ex) {
            System.err.println("$$$ " + ex);
            ex.printStackTrace();
        }
    }

    protected void djaFrameReadFrom(InputStream _ins, String _version) throws Exception {
        if (_ins.available() == 0) return;
        try {
            boolean isOldVersionReader = (FORMAT_DJA_VERSION.equals(FORMAT_DJA_VERSION_NEW_READER)) && ((_version.compareTo(FORMAT_DJA_VERSION_OLD_READER) <= 0));
            YapodDeserializer fluxLu = null;
            if (isOldVersionReader) {
                System.out.println("Dja ancien reader");
                fluxLu = new YapodXmlDeserializer();
            } else {
                fluxLu = new BoonyXmlDeserializer(false);
            }
            fluxLu.open(_ins);
            Object[] infosDja = (Object[]) fluxLu.read();
            String version = ((String) infosDja[0]).trim();
            if (!isOldVersionReader && FORMAT_DJA_VERSION.compareTo(version) > 0) {
                System.err.println("le format de fichier est d�suet : ");
                System.err.println(" Version fichier DJA ou Mascaret : " + version);
                System.err.println(" Version actuel  DJA ou Mascaret : " + FORMAT_DJA_VERSION);
            } else if (FORMAT_DJA_VERSION.compareTo(version) < 0) {
                System.err.println("le fichier est incorrect (n� version impossible) : ");
                System.err.println(" Version fichier DJA ou Mascaret : " + version);
                System.err.println(" Version actuel  DJA ou Mascaret : " + FORMAT_DJA_VERSION);
                System.err.println("Contacter le service de maintenance.");
            } else {
                if (infosDja[3] != null) {
                    DjaGridInteractive grille = new DjaGridInteractive(false, (DjaVector) infosDja[3]);
                    f_ = new Hydraulique1dReseauFrame(impl_, "RESEAU", grille, ietude1d_, ihmP_);
                    f_.setLocation((Point) infosDja[1]);
                    f_.setSize((Dimension) infosDja[2]);
                    setDataDjaHydrau1d(grille);
                    Hydraulique1dReseauMouseAdapter mouseAdapter = new Hydraulique1dReseauMouseAdapter((Hydraulique1dReseauFrame) f_);
                    grille.addMouseListener(mouseAdapter);
                    grille.addMouseMotionListener(mouseAdapter);
                    grille.addGridListener(new Hydraulique1dReseauGridAdapter(ietude1d_.reseau()));
                    ((Hydraulique1dReseauFrame) f_).initNumerosSingularites();
                }
            }
        } catch (Exception ex) {
            System.err.println("$$$ " + ex);
            ex.printStackTrace();
            throw new Exception("Exception g�n�r�es lors de la construction de la grille");
        }
    }

    protected void setNumDjaHydrau1d(DjaGrid grid) {
        if (grid != null) {
            MetierReseau reseau = ietude1d_.reseau();
            Enumeration elements = grid.getObjects().elements();
            while (elements.hasMoreElements()) {
                Object o = elements.nextElement();
                if (o instanceof Hydraulique1dReseauLiaisonCasier) {
                    Hydraulique1dReseauLiaisonCasier liaison = (Hydraulique1dReseauLiaisonCasier) o;
                    if (liaison.getProperty("hydraulique1d") == null) {
                        MetierLiaison i = (MetierLiaison) liaison.getData("liaison");
                        liaison.putProperty("hydraulique1d", Integer.toString(reseau.getIndiceLiaison(i)));
                    }
                }
                if (o instanceof Hydraulique1dReseauCasier) {
                    Hydraulique1dReseauCasier casier = (Hydraulique1dReseauCasier) o;
                    if (casier.getProperty("hydraulique1d") == null) {
                        MetierCasier i = (MetierCasier) casier.getData("casier");
                        casier.putProperty("hydraulique1d", Integer.toString(reseau.getIndiceCasier(i)));
                    }
                }
                if (o instanceof Hydraulique1dReseauSingularite) {
                    Hydraulique1dReseauSingularite sing = (Hydraulique1dReseauSingularite) o;
                    if (sing.getProperty("hydraulique1d") == null) {
                        MetierSingularite i = (MetierSingularite) sing.getData("singularite");
                        sing.putProperty("hydraulique1d", Integer.toString(i.id()));
                    }
                } else if (o instanceof Hydraulique1dReseauExtremLibre) {
                    Hydraulique1dReseauExtremLibre extr = (Hydraulique1dReseauExtremLibre) o;
                    if (extr.getProperty("hydraulique1d") == null) {
                        MetierExtremite i = (MetierExtremite) extr.getData("extremite");
                        extr.putProperty("hydraulique1d", Integer.toString(i.numero()));
                    }
                } else if (o instanceof Hydraulique1dReseauNoeud) {
                    Hydraulique1dReseauNoeud n = (Hydraulique1dReseauNoeud) o;
                    if (n.getProperty("hydraulique1d") == null) {
                        MetierNoeud i = (MetierNoeud) n.getData("noeud");
                        n.putProperty("hydraulique1d", Integer.toString(i.numero()));
                    }
                } else if (o instanceof Hydraulique1dReseauBiefCourbe) {
                    Hydraulique1dReseauBiefCourbe b = (Hydraulique1dReseauBiefCourbe) o;
                    if (b.getProperty("hydraulique1d") == null) {
                        MetierBief i = (MetierBief) b.getData("bief");
                        b.putProperty("hydraulique1d", Integer.toString(i.numero()));
                    }
                }
            }
        }
    }

    protected void setDataDjaHydrau1d(DjaGrid grid) throws Exception {
        if (grid != null) {
            MetierReseau reseau = ietude1d_.reseau();
            Enumeration elements = grid.getObjects().elements();
            while (elements.hasMoreElements()) {
                try {
                    Object o = elements.nextElement();
                    if (o instanceof Hydraulique1dReseauBarragePrincipal) {
                        Hydraulique1dReseauBarragePrincipal bar = (Hydraulique1dReseauBarragePrincipal) o;
                        bar.putData("barragePrincipal", ietude1d_.paramGeneraux().barragePrincipal());
                    } else if (o instanceof Hydraulique1dReseauSingularite) {
                        Hydraulique1dReseauSingularite sing = (Hydraulique1dReseauSingularite) o;
                        String prop = sing.getProperty("hydraulique1d");
                        if (prop != null) {
                            MetierSingularite ising = reseau.getSingulariteId(Integer.parseInt(prop));
                            if (ising != null) {
                                sing.putData("singularite", ising);
                            }
                            sing.removeProperty("hydraulique1d");
                        }
                    } else if (o instanceof Hydraulique1dReseauExtremLibre) {
                        Hydraulique1dReseauExtremLibre extr = (Hydraulique1dReseauExtremLibre) o;
                        String prop = extr.getProperty("hydraulique1d");
                        if (prop != null) {
                            extr.putData("extremite", reseau.getExtremiteNumero(Integer.parseInt(prop)));
                            extr.removeProperty("hydraulique1d");
                        }
                    } else if (o instanceof Hydraulique1dReseauNoeud) {
                        Hydraulique1dReseauNoeud n = (Hydraulique1dReseauNoeud) o;
                        String prop = n.getProperty("hydraulique1d");
                        if (prop != null) {
                            n.putData("noeud", reseau.getNoeudNumero(Integer.parseInt(prop)));
                            n.removeProperty("hydraulique1d");
                        }
                    } else if (o instanceof Hydraulique1dReseauBiefCourbe) {
                        Hydraulique1dReseauBiefCourbe b = (Hydraulique1dReseauBiefCourbe) o;
                        String prop = b.getProperty("hydraulique1d");
                        if (prop != null) {
                            b.putData("bief", reseau.getBiefNumero(Integer.parseInt(prop)));
                            b.removeProperty("hydraulique1d");
                        }
                    } else if (o instanceof Hydraulique1dReseauCasier) {
                        Hydraulique1dReseauCasier c = (Hydraulique1dReseauCasier) o;
                        String prop = c.getProperty("hydraulique1d");
                        if (prop != null) {
                            c.putData("casier", reseau.casiers()[Integer.parseInt(prop)]);
                            c.removeProperty("hydraulique1d");
                        }
                    } else if (o instanceof Hydraulique1dReseauLiaisonCasier) {
                        Hydraulique1dReseauLiaisonCasier c = (Hydraulique1dReseauLiaisonCasier) o;
                        String prop = c.getProperty("hydraulique1d");
                        if (prop != null) {
                            int indiceLiaison = Integer.parseInt(prop);
                            if (indiceLiaison < reseau.liaisons().length) c.putData("liaison", reseau.liaisons()[indiceLiaison]); else {
                                System.err.println("indice liaison incorrecte =" + indiceLiaison + " le nombre de liaisons est " + reseau.liaisons().length);
                                c.putData("liaison", reseau.liaisons()[reseau.liaisons().length - 1]);
                            }
                            c.removeProperty("hydraulique1d");
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Exception dans setDataDjaHydrau1d : poursuite de la construction des liens reseau / grille");
                    System.err.println("$$$ " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        traduitMascToXML(new File("Bief_Baix_Logis_Neuf_impossible_acceder_objet_en_cliquant.masc.masc"), new File("Bug_gilbert_reseau.xml"), new File("Bug_gilbert_etude.xml"));
    }
}
