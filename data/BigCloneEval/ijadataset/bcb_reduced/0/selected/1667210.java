package movepaint.controller;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import movepaint.view.APropos;
import movepaint.GestureRecognition;
import movepaint.stateMachine.InkMachine;
import movepaint.utils.Utilitaire;
import movepaint.utils.FiltreData;

/**
 * Classe qui regroupe les methodes pour effectuer les actions du menu.
 *
 * @author Paulino A.
 * @version 1.0
 */
public class MenuAction {

    /** Frame principale **/
    private GestureRecognition _frameMain = null;

    /** Filtre de données pour les options Ouvrir/Enregistrer **/
    private FiltreData _fd = null;

    /** Liste des extensions acceptées **/
    private static final String[] acceptedExtensions = { "GIF", "TIF", "TIFF", "JPEG", "JPG", "PNG" };

    /** Description des extension acceptées **/
    private static final String DESCRIPTION_EXTENSION = "Images (gif, tif, tiff, jpeg, jpg, png)";

    private static final String PATH_TMP_FILE = System.getProperty("java.io.tmpdir") + "tmpFileToPrint.jpg";

    private static final String TEXT_URL_APPLICATION = "http://code.google.com/p/movepaint/";

    private static final String TEXT_OUVERTURE_URL_APPLICATION = "Ouverture de " + TEXT_URL_APPLICATION + " ...";

    private static final String TEXT_ENREGISTREMENT_FICHIER = "Enregistrement du fichier ";

    private static final String TEXT_ENREGISTREMENT = "Enregistrement en cours...";

    private static final String TEXT_EN_COURS = " en cours...";

    private static final String TEXT_ENREGISTREMENT_OK = "Le fichier a été correctement enregistré!";

    private static final String TEXT_OUVERTURE_DOC = "Ouverture de la documentation ...";

    private static final String TEXT_IMPRESSION = "Impression en cours...";

    private static final String TEXT_NOUVEAU_DOCUMENT = "Nouveau document créé!";

    private static final String TEXT_OUVERTURE_IMAGE = "Ouverture d'une image...";

    private static final String TXT_OUVERTURE_IMAGE_DEBUT = "L'image ";

    private static final String TXT_OUVERTURE_IMAGE_FIN = " a été chargée!";

    private static final String ERROR_LAF = "Impossible de charger le look&feel";

    private static final String ERROR_NON_DISPONIBLE = "La fonction n'a pas encore été développée..";

    private static final String ERROR_ENTETE_NON_DISPONIBLE = "Fonction non développée";

    private static final String ERROR_FORMAT = "Ce format n'est pas accepté!";

    private static final String ERROR_FORMAT_ENTETE = "Erreur de format";

    /**
	 *	Constructeur du MenuAction
	 *
	 *	@param frameMain
	 *			Référence a la frame principale de l'application
	 **/
    public MenuAction(GestureRecognition frameMain) {
        _frameMain = frameMain;
        ArrayList<String> listAcceptedData = new ArrayList<String>();
        for (String ext : acceptedExtensions) listAcceptedData.add(ext);
        _fd = new FiltreData(DESCRIPTION_EXTENSION, listAcceptedData);
    }

    /**
     * Ouverture du browser par défaurt de l'user pour le rediriger vers la
     * page d'accueil du site hébergeant l'application.
     */
    public void accueil() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                _frameMain._statusBar.isWork(true);
                try {
                    desktop.browse(new URI(TEXT_URL_APPLICATION));
                    _frameMain._statusBar.setStatusText(TEXT_OUVERTURE_URL_APPLICATION);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, ex);
                }
                _frameMain._statusBar.isWork(false);
            }
        }
    }

    /**
	 *	Méthode qui change le look&feel de l'application
	 *
	 *	@param sourceName Le nom du L&F qui a été choisi
	 */
    public void changeLaF(String sourceName) {
        Map<String, String> mapLaF = Utilitaire.getLookAndFeelsMap();
        final String nameLaF = mapLaF.get(sourceName);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(nameLaF);
                    SwingUtilities.updateComponentTreeUI(_frameMain);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(_frameMain, ERROR_LAF, e.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
	 *	Méthode qui affiche la fenetre donnant des informations sur l'application
	 **/
    public void aPropos() {
        new APropos(_frameMain);
    }

    /**
     * Réalise une copie des figures sélectionnées
     * (chaque copie est translatée à côté de l'originale)
     */
    public void coller() {
        _frameMain.sm.pasteSelectedShapes();
    }

    /**
     * Copie les figures sélectionnées +  active la fonction <i>Coller</i> & <i>Supprimer</i>
     */
    public void copier() {
        _frameMain.sm.copySelectedShapes();
    }

    /**
     * Efface les figures sélectionnées +  active la fonction <i>Coller</i> & <i>Supprimer</i>
     */
    public void effacer() {
        _frameMain.deleteShapes(InkMachine.TAG_SHAPE_HOVERED);
    }

    /**
     * Enregistre le document en cours.
     * <br>
     * Si le document a déjà été enregistré dans un fichier, ce dernier est réutilisé.
     * <br>
     * Sinon, on ouvre une boîte de dialogue pour demander à l'user ou il veut l'enregistrer
     */
    public void enregistrer() {
        _frameMain._statusBar.isWork(true);
        if (_frameMain.file != null) {
            _frameMain._statusBar.setStatusText(TEXT_ENREGISTREMENT_FICHIER + _frameMain.file.getName() + TEXT_EN_COURS);
            sauvegardeFichier();
        } else {
            _frameMain._statusBar.setStatusText(TEXT_ENREGISTREMENT);
            enregistrer_sous();
        }
        _frameMain._statusBar.isWork(false);
    }

    /**
     * Enregistre le document en cours.
     * <br>
     * On ouvre une boîte de dialogue pour demander à l'user ou il veut l'enregistrer.
     */
    public void enregistrer_sous() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(_fd);
        boolean isOk = true;
        do {
            int returnVal = fc.showSaveDialog(_frameMain);
            if (returnVal == JFileChooser.APPROVE_OPTION && _fd.accept(fc.getSelectedFile())) {
                _frameMain.file = fc.getSelectedFile();
                sauvegardeFichier();
                isOk = true;
            } else if (returnVal == JFileChooser.APPROVE_OPTION && (!_fd.accept(fc.getSelectedFile()))) {
                JOptionPane.showMessageDialog(_frameMain, ERROR_FORMAT, ERROR_FORMAT_ENTETE, JOptionPane.ERROR_MESSAGE);
                isOk = false;
            } else {
                System.out.println("Save command cancelled by user.");
                isOk = true;
            }
        } while (!isOk);
    }

    /**
     * Ouvre la Javadoc avec l'application utilisée par défaut par l'user.
     */
    public void manuel() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                _frameMain._statusBar.isWork(true);
                try {
                    desktop.open(new File(GestureRecognition.PATH_DOC));
                    _frameMain._statusBar.setStatusText(TEXT_OUVERTURE_DOC);
                } catch (IOException ex) {
                    Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, ex);
                }
                _frameMain._statusBar.isWork(false);
            }
        }
    }

    /**
	 *	Méthode qui permet de quitter l'application
	 **/
    public void quitter() {
        _frameMain.dispose();
        System.exit(0);
    }

    /**
     * Impression du document actuel.
     * <br>
     * L'application génère un fichier temporaire dans lequel elle stoque le contenu du
     *  document. Ensuite, elle l'utilise pour l'impression et le supprime une fois la tâche effectuée.
     */
    public void imprimer() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.PRINT)) {
                _frameMain._statusBar.isWork(true);
                _frameMain._statusBar.setStatusText(TEXT_IMPRESSION);
                String oldFile = null;
                if (_frameMain.file != null) oldFile = _frameMain.file.getPath();
                _frameMain.file = new File(PATH_TMP_FILE);
                sauvegardeFichier();
                try {
                    desktop.print(_frameMain.file);
                } catch (IOException ex) {
                    Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (oldFile != null) _frameMain.file = new File(oldFile); else {
                        _frameMain.file.delete();
                        _frameMain.file = null;
                    }
                }
                _frameMain._statusBar.isWork(false);
            }
        }
    }

    /**
     * Supprime tous les éléments présents dans le panel
     */
    public void nouveau() {
        _frameMain._statusBar.isWork(true);
        _frameMain._statusBar.setStatusText(TEXT_NOUVEAU_DOCUMENT);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                _frameMain.canvas.removeAllShapes();
                _frameMain.createGestureRecognizedPanel();
                _frameMain.sm.createComponents();
            }
        });
        _frameMain._statusBar.isWork(false);
    }

    /**
     * Ouvre une boîte de dialogue dans laquelle l'user peut choisir
     *  une image à charger dans l'application.
     */
    public void ouvrir() {
        _frameMain._statusBar.isWork(true);
        _frameMain._statusBar.setStatusText(TEXT_OUVERTURE_IMAGE);
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(_fd);
        boolean isOk = true;
        do {
            int returnVal = fc.showOpenDialog(_frameMain);
            if (returnVal == JFileChooser.APPROVE_OPTION && _fd.accept(fc.getSelectedFile())) {
                _frameMain.file = fc.getSelectedFile();
                loadImage();
                _frameMain._statusBar.setStatusText(TXT_OUVERTURE_IMAGE_DEBUT + _frameMain.file.getName() + TXT_OUVERTURE_IMAGE_FIN);
                isOk = true;
            } else if (returnVal == JFileChooser.APPROVE_OPTION && (!_fd.accept(fc.getSelectedFile()))) {
                JOptionPane.showMessageDialog(_frameMain, ERROR_FORMAT, ERROR_FORMAT_ENTETE, JOptionPane.ERROR_MESSAGE);
                isOk = false;
            } else {
                System.out.println("Load command cancelled by user.");
                isOk = true;
            }
        } while (!isOk);
        _frameMain._statusBar.isWork(false);
    }

    /**
     * Ouvre le panel des préférences de l'application
     */
    public void preference() {
        JOptionPane.showMessageDialog(_frameMain, ERROR_NON_DISPONIBLE, ERROR_ENTETE_NON_DISPONIBLE, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Sélectionne tous les shapes du panel de dessin
     */
    public void tousSelectionner() {
        _frameMain.sm.selectAllShapes();
    }

    /**
     * Chargement de l'image choisi par l'user dans le canvas de dessin.
     */
    private void loadImage() {
        _frameMain.canvas.newImage(0, GestureRecognition.HEIGHT_TOOL + 30.0, _frameMain.file.getPath()).addTag(InkMachine.TAG_SHAPE).belowAll();
        _frameMain.createGestureRecognizedPanel();
        _frameMain.sm.createComponents();
    }

    /**
     * Sauvegarde du fichier choisi par l'user.
     */
    private void sauvegardeFichier() {
        _frameMain.hideShapes(GestureRecognition.GESTE_TAG);
        System.out.println("Saving: " + _frameMain.file.getName());
        Image img = _frameMain.canvas.createImage(_frameMain.canvas.getWidth(), _frameMain.canvas.getHeight());
        Graphics g = img.getGraphics();
        _frameMain.canvas.paint(g);
        try {
            ImageIO.write((RenderedImage) img, _fd.getExtension(_frameMain.file), _frameMain.file);
            _frameMain._statusBar.setStatusText(TEXT_ENREGISTREMENT_OK);
        } catch (IOException ex) {
            Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(MenuAction.class.getName()).log(Level.SEVERE, null, e);
        }
        _frameMain.showShapes(GestureRecognition.GESTE_TAG);
    }
}
