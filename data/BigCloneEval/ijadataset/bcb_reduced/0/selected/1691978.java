package mp3.extras;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author user
 */
public class VersionLauncher extends Thread {

    private boolean mostrarFallo = false;

    private Component parent;

    /**
     * Crea un lanzador gráfico para mostrar si existe o no una nueva version
     */
    public VersionLauncher(Component parent) {
        this.parent = parent;
    }

    /**
     * Crea un lanzador gráfico para mostrar si existe o no una nueva version
     * @param con_fallo indica si se muestra notificación o no si no se encuentra una
     * nueva versión
     */
    public VersionLauncher(Component parent, boolean con_fallo) {
        this.parent = parent;
        mostrarFallo = con_fallo;
    }

    @Override
    public void run() {
        Float vact = new Float(Utilidades.actualVersion);
        FutureTask<Void> calli;
        String ver = Utilidades.getLatestVersion();
        Logger.getLogger(VersionLauncher.class.getName()).log(Level.INFO, "{0} {1}", new Object[] { java.util.ResourceBundle.getBundle("Bundle").getString("VersionLauncher.version.text"), ver });
        if (ver != null) {
            Float vlast = new Float(ver);
            if (vlast.floatValue() > vact.floatValue()) {
                calli = new FutureTask<Void>(new CallableImpl(vlast));
                SwingUtilities.invokeLater(calli);
                int val = -1;
                try {
                    calli.get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(VersionLauncher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(VersionLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (val == JOptionPane.OK_OPTION) {
                    try {
                        URI uri = new URI("http://sourceforge.net/projects/jmusicmanager");
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(uri);
                            } catch (IOException ex) {
                                Logger.getLogger(VersionLauncher.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(VersionLauncher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                if (mostrarFallo) {
                    calli = new FutureTask<Void>(new CallableImpl(java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.Fail")));
                    SwingUtilities.invokeLater(calli);
                }
            }
        } else {
            SwingUtilities.invokeLater(new FutureTask<Void>(new CallableImpl(java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.FailConnect"))));
        }
    }

    /**
     * Necesario para invocar la creación del pane en el dispatch event thread
     */
    private class CallableImpl implements Callable<Void> {

        private Float vlast;

        private String mensaje = null;

        public CallableImpl(Float vlast) {
            this.vlast = vlast;
        }

        public CallableImpl(String mess) {
            this.mensaje = mess;
        }

        @Override
        public Void call() throws Exception {
            if (mensaje == null) JOptionPane.showConfirmDialog(parent, java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.Success.1_left") + " " + vlast.floatValue() + " " + java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.Success.1_right") + "\n" + java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.Success.2") + " http://sourceforge.net/projects/jmusicmanager/\n" + java.util.ResourceBundle.getBundle("Bundle").getString("NewJFrame.Updates.Success.3")); else {
                JOptionPane.showMessageDialog(parent, mensaje);
            }
            return null;
        }
    }
}
