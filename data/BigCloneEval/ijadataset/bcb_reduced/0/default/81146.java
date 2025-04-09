import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

/** 
 * Clase InitFrame.java 
 *
 * @author Revolution Software Developers
 * @package drunkenman
 **/
public class InitFrame extends JFrame implements MouseInputListener {

    /**
	 * Layer principal de contenido
	 */
    private JLayeredPane principal = this.getLayeredPane();

    /**
	 * Label de juego rapido del menu principal
	 */
    private JLabel rapido = new JLabel("Juego rapido");

    /**
	 * Label de Cargar partida del juego principal
	 */
    private JLabel cargar = new JLabel("Cargar Juego");

    /**
	 * Label de Opciones del juego
	 */
    private JLabel opciones = new JLabel("Opciones");

    /**
	 * Label de Salir del juego
	 */
    private JLabel salir = new JLabel("Salir");

    /**
	 * Label de Nuevo juego
	 */
    private JLabel nuevo = new JLabel("Juego nuevo");

    /**
	 * Label de Bar de la fama
	 */
    private JLabel highs = new JLabel("Bar de la Fama");

    /**
	 * Constructor inicial de la aplicacion GUI
	 */
    public InitFrame() {
        Main.setDefaults(this);
        JLayeredPane menubg = new JLayeredPane();
        JLabel fondo = new JLabel();
        JPanel menu = new JPanel(new GridLayout(6, 1));
        menubg.setSize(new Dimension(191, 186));
        menubg.setLocation(285, 220);
        ImageIcon f = Main.getImageIcon("menubg.png");
        fondo.setIcon(f);
        fondo.setSize(new Dimension(191, 186));
        menubg.add(fondo, JLayeredPane.DEFAULT_LAYER);
        menu.setOpaque(false);
        menu.add(nuevo);
        menu.add(rapido);
        menu.add(cargar);
        menu.add(highs);
        menu.add(opciones);
        menu.add(salir);
        menu.setSize(200, 200);
        menu.setLocation(10, 0);
        menubg.add(menu, JLayeredPane.MODAL_LAYER);
        principal.add(menubg, JLayeredPane.MODAL_LAYER);
        nuevo.addMouseListener(this);
        rapido.addMouseListener(this);
        cargar.addMouseListener(this);
        opciones.addMouseListener(this);
        highs.addMouseListener(this);
        salir.addMouseListener(this);
    }

    /**
	 * Metodo del mouse clicked
	 */
    public void mouseClicked(MouseEvent e) {
        Object clicked = e.getSource();
        if (clicked == salir) {
            this.dispose();
        } else if (clicked == nuevo) {
            PersonalizeFrame f = new PersonalizeFrame();
            this.setVisible(false);
            f.setVisible(true);
        } else if (clicked == rapido) {
            GameFrame frame = new GameFrame();
            this.setVisible(false);
            frame.setVisible(true);
        } else if (clicked == cargar) {
            this.open();
        } else if (clicked == opciones) {
            PreferencesFrame frame = new PreferencesFrame();
            this.setVisible(false);
            frame.setVisible(true);
        } else if (clicked == highs) {
            HighscoresFrame frame = new HighscoresFrame();
            this.setVisible(false);
            frame.setVisible(true);
        }
    }

    /**
	 * Abre una partida guardada
	 *
	 */
    public void open() {
        JFileChooser fc = new JFileChooser(Main.RUTA);
        fc.showOpenDialog(this);
        File file = fc.getSelectedFile();
        if (file != null) {
        }
    }

    /**
	 * Cambia el cursor del mouse si esta dentro del menu y el color de la letra
	 */
    public void mouseEntered(MouseEvent e) {
        JLabel aux = (JLabel) e.getSource();
        aux.setForeground(Color.RED);
        this.setCursor(HAND_CURSOR);
    }

    /**
	 * Devuelve el cursor a su estado natural
	 */
    public void mouseExited(MouseEvent e) {
        JLabel aux = (JLabel) e.getSource();
        aux.setForeground(Color.BLACK);
        this.setCursor(Cursor.DEFAULT_CURSOR);
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseDragged(MouseEvent arg0) {
    }

    public void mouseMoved(MouseEvent arg0) {
    }
}
