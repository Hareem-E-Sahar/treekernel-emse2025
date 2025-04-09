package my.hough.packages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Main - 
 * Cr�ation graphique
 * @version 1.0
 * @author  [Bernis FOMAZOU]
 * @copyright (C) GPL
 * @date 25/06/2011
 * @notes  Transform� de Hough
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SplashScreen splash = new SplashScreen(5000);
        splash.showSplashAndExit();
        JFrame frame = new Paint();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}

class Paint extends JFrame implements ActionListener {

    public JMenuBar menuBar = new JMenuBar();

    public JMenu Fichier, Edition, dessiner, aide;

    public JMenuItem New, Ouvrir, vider, deplacer, Quitter, ligne, rectangle, ellipse, main, polygone, triangle, annuler, repeter, apropos, doc;

    public JToolBar standart, barre1;

    public JToolBar standart2, barre2;

    public JButton Main, Ligne, Rectang, Ellipse, Triangle, Triangle_draw, Deplacer, Polygone, rect_draw, rd_rect_draw, vide1, vide2, taille1, taille2, taille3, taille4, tligne1, tligne2, tligne3, tligne4, ellipse_draw, premier_p, arriere_p, color_r, houghspace, linespace;

    public Container contenu = getContentPane();

    public JPanel contenu2 = new JPanel();

    public JSplitPane pane, pane1;

    public JComponent c1, c2;

    public Dessin panel_1, p2;

    public JTextArea info = new JTextArea();

    public File file_image;

    public String nom_image, path, frfr;

    public Image im;

    public Color c = Color.white;

    public JFrame frame = new JFrame();

    public Paint() {
        setSize(800, 400);
        setTitle("Transform� de HOUGH");
        setContentPane(new Dessin());
        Fichier = new JMenu("Fichier");
        Edition = new JMenu("Edition");
        dessiner = new JMenu("Dessiner");
        aide = new JMenu("Aide");
        Ouvrir = new JMenuItem("Ouvrir une image");
        New = new JMenuItem("Nouveau dessin");
        deplacer = new JMenuItem("D�placer un objet");
        Quitter = new JMenuItem("Quitter");
        annuler = new JMenuItem("Annuler");
        repeter = new JMenuItem("R�p�ter");
        vider = new JMenuItem("Effacer tous");
        main = new JMenuItem("dessin libre");
        ligne = new JMenuItem("Ligne");
        triangle = new JMenuItem("Triangle");
        rectangle = new JMenuItem("Rectangle");
        ellipse = new JMenuItem("Ellipse");
        doc = new JMenuItem("Documentation");
        apropos = new JMenuItem("A propos");
        Fichier.add(New);
        Fichier.add(Ouvrir);
        Fichier.add(Quitter);
        Edition.add(annuler);
        Edition.add(repeter);
        Edition.add(new JSeparator());
        Edition.add(vider);
        Edition.add(deplacer);
        aide.add(doc);
        aide.add(apropos);
        dessiner.add(main);
        dessiner.add(ligne);
        dessiner.add(rectangle);
        dessiner.add(ellipse);
        dessiner.add(triangle);
        dessiner.add(new JSeparator());
        dessiner.add(deplacer);
        annuler.setEnabled(false);
        repeter.setEnabled(false);
        menuBar.add(Fichier);
        menuBar.add(Edition);
        menuBar.add(dessiner);
        menuBar.add(aide);
        setJMenuBar(menuBar);
        standart = new JToolBar("Barre d'outil");
        barre1 = new JToolBar("Barre1", 1);
        standart2 = new JToolBar("Barre d'outil");
        barre2 = new JToolBar("Barre2", 1);
        Main = new JButton(new ImageIcon("Image\\Dessiner.gif"));
        Main.setToolTipText("main libre");
        Main.addActionListener(this);
        vide1 = new JButton(new ImageIcon("Image\\vide.gif"));
        vide2 = new JButton(new ImageIcon("Image\\vide.gif"));
        vide1.setEnabled(false);
        vide2.setEnabled(false);
        Rectang = new JButton(new ImageIcon("Image\\rect1.gif"));
        Rectang.setToolTipText("Un Rectangle");
        Rectang.addActionListener(this);
        rect_draw = new JButton(new ImageIcon("Image\\rect_draw.gif"));
        rect_draw.setToolTipText("dessiner un Rectangle vide");
        rect_draw.addActionListener(this);
        rd_rect_draw = new JButton(new ImageIcon("Image\\roundrec_draw.gif"));
        rd_rect_draw.setToolTipText("dessiner un roundrectangle vide");
        rd_rect_draw.addActionListener(this);
        Ligne = new JButton(new ImageIcon("Image\\ligne1.gif"));
        Ligne.setToolTipText("Une ligne");
        Ligne.addActionListener(this);
        Ellipse = new JButton(new ImageIcon("Image\\ellipse_fill.gif"));
        Ellipse.setToolTipText("Un Ellipse");
        Ellipse.addActionListener(this);
        Triangle = new JButton(new ImageIcon("Image\\triangle1_draw.gif"));
        Triangle.setToolTipText("Triangle");
        Triangle.addActionListener(this);
        Triangle_draw = new JButton(new ImageIcon("Image\\triangle1_draw.gif"));
        Triangle_draw.setToolTipText("Triangle");
        Triangle_draw.addActionListener(this);
        Deplacer = new JButton(new ImageIcon("Image\\dp.gif"));
        Deplacer.setToolTipText("Deplacer");
        Deplacer.addActionListener(this);
        taille1 = new JButton(new ImageIcon("Image\\taille1.gif"));
        taille1.setToolTipText("changer la taille1");
        taille1.addActionListener(this);
        taille2 = new JButton(new ImageIcon("Image\\taille2.gif"));
        taille2.setToolTipText("changer la taille2");
        taille2.addActionListener(this);
        taille3 = new JButton(new ImageIcon("Image\\taille3.gif"));
        taille3.setToolTipText("changer la taille3");
        taille3.addActionListener(this);
        taille4 = new JButton(new ImageIcon("Image\\taille4.gif"));
        taille4.setToolTipText("changer la taille4");
        taille4.addActionListener(this);
        tligne1 = new JButton(new ImageIcon("Image\\taille1.gif"));
        tligne1.setToolTipText("changer la taille1");
        tligne1.addActionListener(this);
        tligne2 = new JButton(new ImageIcon("Image\\taille2.gif"));
        tligne2.setToolTipText("changer la taille2");
        tligne2.addActionListener(this);
        tligne3 = new JButton(new ImageIcon("Image\\taille3.gif"));
        tligne3.setToolTipText("changer la taille3");
        tligne3.addActionListener(this);
        tligne4 = new JButton(new ImageIcon("Image\\taille4.gif"));
        tligne4.setToolTipText("changer la taille4");
        tligne4.addActionListener(this);
        linespace = new JButton("Espace de Hough");
        linespace.setToolTipText("G�n�rer l'espace de hough");
        linespace.addActionListener(this);
        houghspace = new JButton("D�tecter les droites");
        houghspace.setToolTipText("d�tecter les droites contenues dans l'image");
        houghspace.addActionListener(this);
        ellipse_draw = new JButton(new ImageIcon("Image\\ellipse_draw.gif"));
        ellipse_draw.setToolTipText("Ellipse vide");
        ellipse_draw.addActionListener(this);
        Quitter.setToolTipText("Quitter l'application");
        Quitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        Quitter.addActionListener(this);
        New.setToolTipText("Quitter l'application");
        New.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        New.addActionListener(this);
        Ouvrir.setToolTipText("Quitter l'application");
        Ouvrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        Ouvrir.addActionListener(this);
        annuler.setToolTipText("Annuler la derni�re action");
        annuler.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        annuler.addActionListener(this);
        repeter.setToolTipText("R�p�ter la derni�re action");
        repeter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        repeter.addActionListener(this);
        vider.setToolTipText("Vider l'espace de travail");
        vider.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        vider.addActionListener(this);
        deplacer.setToolTipText("D�placer un objet");
        deplacer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
        deplacer.addActionListener(this);
        ligne.setToolTipText("Dessiner une ligne");
        ligne.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
        ligne.addActionListener(this);
        main.setToolTipText("Dessin libre");
        main.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
        main.addActionListener(this);
        triangle.setToolTipText("Dessiner un triangle");
        triangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
        triangle.addActionListener(this);
        rectangle.setToolTipText("Dessiner un rectangle");
        rectangle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        rectangle.addActionListener(this);
        ellipse.setToolTipText("Desssiner un ellipse");
        ellipse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
        ellipse.addActionListener(this);
        apropos.setToolTipText("A propos de l'application");
        apropos.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.CTRL_MASK));
        apropos.addActionListener(this);
        doc.setToolTipText("La documentation de l'application");
        doc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_MASK));
        doc.addActionListener(this);
        premier_p = new JButton(new ImageIcon("Image\\img.gif"));
        premier_p.setToolTipText("Mettre l'objet en premiere plan");
        premier_p.addActionListener(this);
        arriere_p = new JButton(new ImageIcon("Image\\img.gif"));
        arriere_p.setToolTipText("Mettre l'objet en arri�re plan");
        arriere_p.addActionListener(this);
        barre1.add(vide1);
        barre1.add(vide2);
        standart.add(Main);
        standart.add(Ligne);
        standart.add(Rectang);
        standart.add(Ellipse);
        standart.add(Triangle);
        standart.add(Deplacer);
        standart.setToolTipText("Barre d'outil");
        contenu.add(standart, "North");
        contenu.add(barre1, "West");
        contenu.setBackground(Color.white);
        panel_1 = new Dessin();
        contenu.add(panel_1);
        System.out.print(panel_1.getSize().getWidth());
        c1 = (JComponent) contenu;
        c2 = (JComponent) contenu2;
        c2.setLayout(new BorderLayout());
        c1.setBackground(Color.BLACK);
        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, c1, c2);
        pane.setResizeWeight(0);
        pane.setOneTouchExpandable(false);
        pane.setEnabled(false);
        pane.setAutoscrolls(false);
        setContentPane(pane);
        barre2.add(vide1);
        barre2.add(vide2);
        standart2.add(houghspace);
        standart2.add(linespace);
        contenu2.add(standart2, "North");
        contenu2.add(barre2, "West");
        contenu2.setBackground(Color.gray);
        contenu2.setVisible(true);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
    }

    public Image getImage(JComponent component) {
        if (component == null) {
            return null;
        }
        int width = component.getWidth();
        int height = component.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        component.paintAll(g);
        g.dispose();
        return image;
    }

    BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return ((BufferedImage) image);
        } else {
            image = new ImageIcon(image).getImage();
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return (bufferedImage);
        }
    }

    public void actualiser() {
        info.setText("");
        frfr = new Information(panel_1.getfigure()).getg();
        info.append(frfr);
    }

    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        actualiser();
        if (source == Rectang || source == rectangle) {
            panel_1.setlwn(c);
            annuler.setEnabled(true);
            barre1.removeAll();
            barre1.add(rect_draw);
            barre1.add(rd_rect_draw);
            panel_1.flag = "rect_draw";
            barre1.repaint();
        }
        if (source == rect_draw) {
            panel_1.setlwn(c);
            annuler.setEnabled(true);
            barre1.removeAll();
            barre1.add(rect_draw);
            barre1.add(rd_rect_draw);
            panel_1.flag = "rect_draw";
            barre1.repaint();
        }
        if (source == rect_draw) {
            panel_1.setlwn(c);
            setCursor(JFrame.CROSSHAIR_CURSOR);
            panel_1.flag = "rect_draw";
        }
        if (source == rd_rect_draw) {
            panel_1.setlwn(c);
            setCursor(JFrame.CROSSHAIR_CURSOR);
            panel_1.flag = "rd_rect_draw";
        }
        if (source == apropos) panel_1.msgpropos();
        if (source == doc) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desktop.open(new File("dist\\javadoc\\index.html"));
                    } catch (IOException ex) {
                    }
                }
            }
        }
        if ((source == Triangle) || (source == triangle)) {
            panel_1.setlwn(c);
            annuler.setEnabled(true);
            barre1.removeAll();
            barre1.add(Triangle_draw);
            panel_1.flag = "triangle_draw";
        }
        if ((source == Triangle_draw)) {
            panel_1.setlwn(c);
            panel_1.flag = "triangle_draw";
        }
        if ((source == Main) || (source == main)) {
            panel_1.setlwn(c);
            annuler.setEnabled(true);
            panel_1.flag = "taille1";
            barre1.removeAll();
            barre1.add(taille1);
            barre1.add(taille2);
            barre1.add(taille3);
            barre1.add(taille4);
            barre1.repaint();
        }
        if (source == taille1) {
            panel_1.flag = "taille1";
        }
        if (source == taille2) {
            panel_1.flag = "taille2";
        }
        if (source == taille3) {
            panel_1.flag = "taille3";
        }
        if (source == taille4) {
            panel_1.flag = "taille4";
        }
        if (source == Deplacer || source == deplacer) {
            setCursor(JFrame.MOVE_CURSOR);
            annuler.setEnabled(true);
            barre1.removeAll();
            barre1.add(vide1);
            barre1.add(vide2);
            barre1.repaint();
            panel_1.flag = "deplacer";
        }
        if (source == color_r) {
            c = JColorChooser.showDialog(p2, "S�lection...", getBackground());
            panel_1.setlwn(c);
            color_r.setBackground(c);
        }
        if (source == Ligne || source == ligne) {
            panel_1.setlwn(c);
            setCursor(JFrame.CROSSHAIR_CURSOR);
            annuler.setEnabled(true);
            panel_1.flag = "tligne1";
            barre1.removeAll();
            barre1.add(tligne1);
            barre1.add(tligne2);
            barre1.add(tligne3);
            barre1.add(tligne4);
            barre1.repaint();
        }
        if (source == tligne3) {
            panel_1.flag = "tligne3";
        }
        if (source == tligne1) {
            panel_1.flag = "tligne1";
        }
        if (source == tligne2) {
            panel_1.flag = "tligne2";
        }
        if (source == tligne4) {
            panel_1.flag = "tligne4";
        }
        if ((source == Polygone) || (source == Polygone)) {
            panel_1.setlwn(c);
            annuler.setEnabled(true);
            barre1.removeAll();
            barre1.add(premier_p);
            barre1.add(arriere_p);
            panel_1.flag = "polygone";
        }
        if (source == Ellipse || source == ellipse) {
            panel_1.setlwn(c);
            setCursor(JFrame.CROSSHAIR_CURSOR);
            annuler.setEnabled(true);
            panel_1.flag = "ellipse_draw";
            barre1.removeAll();
            barre1.add(ellipse_draw);
            barre1.repaint();
        }
        if (source == ellipse_draw) {
            panel_1.setlwn(c);
            panel_1.flag = "ellipse_draw";
        }
        if (source == Quitter) {
            int confirmation = JOptionPane.showConfirmDialog(contenu, "Etes-vous sur de vouloir quitter ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
        if (source == houghspace) {
            String filename = "vase.png";
            BufferedImage bimg = toBufferedImage(getImage(panel_1));
            Hough hough, param;
            try {
                panel_1.saveImage(bimg, new FileOutputStream("enter.png"));
                BufferedImage image = javax.imageio.ImageIO.read(new File(filename));
                hough = new Hough(bimg);
                hough.run(bimg);
                ImageIcon Imc = new ImageIcon("droite.png");
                JLabel label1 = new JLabel(Imc);
                contenu2.add(label1);
                contenu2.setVisible(true);
                contenu2.repaint();
                label1.repaint();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            frame.dispose();
            System.out.print("\n D�tection des droites ");
        }
        if (source == linespace) {
            String file = "espace_de_hough.png";
            frame.setTitle("Accumulateur");
            ImageIcon Imc = new ImageIcon("espace_de_hough.png");
            Image imag = Imc.getImage();
            JLabel label2 = new JLabel(Imc);
            frame.getContentPane().add(label2);
            frame.pack();
            frame.setSize(imag.getWidth(null), imag.getHeight(null));
            frame.setVisible(true);
            frame.isPreferredSizeSet();
            label2.repaint();
            frame.repaint();
        }
        if (source == annuler) {
            repeter.setEnabled(true);
            panel_1.figures1.add(panel_1.figures.getLast());
            panel_1.figures.removeLast();
            repaint();
            validate();
        }
        if (source == repeter) {
            if (panel_1.figures1.isEmpty()) {
                annuler.setEnabled(false);
            }
            panel_1.figures.add(panel_1.figures1.getLast());
            panel_1.figures1.removeLast();
            repaint();
            validate();
        }
        if (source == New || source == vider) {
            panel_1.setlwn(c);
            annuler.setEnabled(false);
            repeter.setEnabled(false);
            barre1.removeAll();
            barre1.add(vide1);
            barre1.add(vide2);
            barre1.repaint();
            info.setText("");
            panel_1.vider_dessin();
        }
        if (source == premier_p) {
            setCursor(JFrame.DEFAULT_CURSOR);
            panel_1.flag = "Premier_plan";
        }
        if (source == arriere_p) {
            setCursor(JFrame.DEFAULT_CURSOR);
            panel_1.flag = "Arriere_plan";
        }
        if (source == Ouvrir) {
            panel_1.setlwn(c);
            setCursor(JFrame.DEFAULT_CURSOR);
            JFileChooser chooser = new JFileChooser();
            chooser.setAccessory(new FilePreviewer(chooser));
            chooser.setCurrentDirectory(new File("."));
            FileFilter bmp = new filtre("Images BMP", ".bmp");
            FileFilter gif = new filtre("Image GIF", ".gif");
            FileFilter png = new filtre("Image PNG", ".png");
            FileFilter jpeg = new filtre("Images JPEG", ".jpg");
            chooser.addChoosableFileFilter(bmp);
            chooser.addChoosableFileFilter(gif);
            chooser.addChoosableFileFilter(png);
            chooser.addChoosableFileFilter(jpeg);
            chooser.setDialogTitle("Ouvrir une image");
            chooser.setMultiSelectionEnabled(false);
            int r = chooser.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                file_image = chooser.getSelectedFile();
                nom_image = chooser.getSelectedFile().getName();
                path = file_image.getPath();
                panel_1.loadImage(file_image, nom_image);
                repaint();
                setTitle("Dessiner - " + nom_image);
            }
        }
    }
}
