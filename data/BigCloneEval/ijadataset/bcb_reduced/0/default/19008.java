import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.FileFilter;

public class FenetreMenu extends JFrame {

    private Menu menu;

    private JPanel container = new JPanel();

    private static final int LARGEUR = 558;

    private static final int HAUTEUR = 692;

    private int x, y, click;

    JFileChooser fileChooser = new JFileChooser("sauvegarde/");

    AbFileFilter filtre = new AbFileFilter(".ab", "Fichier Abalone");

    File file;

    public FenetreMenu() {
        super();
        this.fileChooser.addChoosableFileFilter(filtre);
        this.setTitle("Abalone");
        this.setSize(LARGEUR, HAUTEUR);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        container.setLayout(new BorderLayout());
        this.menu = new Menu();
        menu.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent event) {
                x = event.getX();
                y = event.getY();
                if (x > 113 && x < 454 && y > 146 && y < 245 && event.getButton() == MouseEvent.BUTTON1) {
                    ChoixRegles cr = new ChoixRegles(null, "Regles", true);
                    Regles regles = cr.showChoixRegles();
                    menu.rafraichirMenu(0);
                    if (cr.aChoisi()) {
                        Partie p = new Partie(regles);
                        menu.rafraichirMenu(0);
                        dispose();
                    }
                }
                if (x > 113 && x < 454 && y > 280 && y < 379 && event.getButton() == MouseEvent.BUTTON1) {
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                        if (fileChooser.getFileFilter().accept(file)) {
                            try {
                                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                                Partie partie = new Partie((Partie) ois.readObject());
                                ois.close();
                                partie.f.rafraichir(partie.plateau);
                                partie.f.arbre.setModel(partie.coups);
                                partie.coups.reload();
                                dispose();
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (ClassNotFoundException e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            JOptionPane alert = new JOptionPane();
                            alert.showMessageDialog(null, "Erreur d'extension de fichier ! \nVotre chargement a echoue !", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    menu.rafraichirMenu(0);
                }
                if (x > 113 && x < 454 && y > 430 && y < 529 && event.getButton() == MouseEvent.BUTTON1) {
                    menu.rafraichirMenu(0);
                    System.exit(0);
                }
                if (event.getButton() == MouseEvent.BUTTON3) {
                    menu.rafraichirMenu(0);
                }
            }
        });
        container.add(menu, BorderLayout.CENTER);
        this.getContentPane().add(container);
        this.setContentPane(container);
        this.setVisible(true);
    }
}
