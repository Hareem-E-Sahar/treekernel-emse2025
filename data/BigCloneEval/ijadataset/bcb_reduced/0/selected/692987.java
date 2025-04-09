package arqueologia.view.menu;

import arqueologia.controller.GestorError;
import arqueologia.view.JLabel;
import arqueologia.view.PopUp;
import arqueologia.view.Ventana;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Arturo
 */
public class JPInformacionJugador extends JPanel implements MouseListener {

    private Image imagen_fondo = new ImageIcon("Imagenes/JPInformacionJugadorFondo.png").getImage();

    private JTextField jTFNombre;

    private boolean isNewGame;

    public JPInformacionJugador(boolean isNewGame) {
        this.isNewGame = isNewGame;
        this.setLayout(null);
        jTFNombre = new JTextField();
        jTFNombre.setBounds(95, 174, 200, 30);
        jTFNombre.setOpaque(false);
        jTFNombre.setBorder(null);
        jTFNombre.requestFocus();
        jTFNombre.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    mouseReleased(null);
                } else {
                    String text = jTFNombre.getText();
                    int limit = 12;
                    if (text.length() > limit) {
                        jTFNombre.setText(text.substring(0, limit));
                    }
                }
            }
        });
        JLabel jBAceptar = new JLabel("");
        jBAceptar.setBounds(80, 224, 85, 17);
        jBAceptar.addMouseListener(this);
        JLabel jBCancelar = new JLabel("");
        jBCancelar.setBounds(223, 224, 98, 17);
        jBCancelar.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                PopUp.close();
            }
        });
        this.add(jTFNombre);
        this.add(jBAceptar);
        this.add(jBCancelar);
        this.setSize(400, 349);
        this.setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(imagen_fondo, 0, 0, null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        String nombre = jTFNombre.getText();
        if (nombre.equals("")) {
            PopUp.showMessageDialog(GestorError.getError("IngresarNombre"), PopUp.ERROR_MESSAGE);
        } else {
            if (this.isNewGame) {
                if (!CrearDirectorioJugador(nombre)) return;
            } else {
                if (!verificarExisteSaveGame(nombre)) return;
            }
            PopUp.close();
            Ventana.finBienvenida(isNewGame);
        }
    }

    private boolean deleteDirectory(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (file.delete());
    }

    private boolean CrearDirectorioJugador(String nombre) {
        File directory = new File("Save/" + nombre);
        if (directory.exists()) {
            int value = PopUp.YES_OPTION;
            if (value == PopUp.YES_OPTION) {
                File files[] = directory.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) deleteDirectory(files[i]); else files[i].delete();
                }
            } else return false;
        } else directory.mkdir();
        return true;
    }

    private boolean verificarExisteSaveGame(String nombre) {
        File directory = new File("Save/" + nombre);
        if (!directory.exists()) {
            PopUp.showMessageDialog("No exite el jugador " + nombre, PopUp.ERROR_MESSAGE);
            return false;
        } else {
            FileListFilter fileFilter = new FileListFilter("bin");
            File files[] = directory.listFiles(fileFilter);
            if (files.length == 0) {
                PopUp.showMessageDialog("No existe un juego guardado para el jugador " + nombre, PopUp.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public String getNombreJugador() {
        return this.jTFNombre.getText();
    }

    class FileListFilter implements FilenameFilter {

        private String extension;

        public FileListFilter(String extension) {
            this.extension = extension;
        }

        public boolean accept(File directory, String filename) {
            boolean fileOK = true;
            if (extension != null) {
                fileOK &= filename.endsWith('.' + extension);
            }
            return fileOK;
        }
    }
}
