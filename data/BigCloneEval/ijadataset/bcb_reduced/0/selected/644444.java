package util;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import util.file.FileOperator;
import util.file.SwingFileFilter;
import com.l2fprod.common.swing.JDirectoryChooser;

/**
 * The Class AppUtil for the project, you can find here common operation to the
 * entire application.
 * 
 * @author Juan Timoteo Ponce Ortiz
 */
public final class AppUtil {

    /**
     * Show abrir carpeta.
     * 
     * @param padre
     *            the padre
     * @param titulo
     *            the titulo
     * @param path
     *            the path
     * 
     * @return the file
     */
    public static File showAbrirCarpeta(final Component padre, final String titulo, final String path) {
        final JDirectoryChooser chooser = new JDirectoryChooser();
        chooser.setDialogTitle(titulo);
        chooser.setFileFilter(new SwingFileFilter(""));
        if (path != null && !path.isEmpty() && FileOperator.fileExists(path)) chooser.setCurrentDirectory(new File(path));
        final int result = chooser.showOpenDialog(padre);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Show guardar.
     * 
     * @param titulo
     *            the titulo
     * @param extension
     *            the extension
     * @param padre
     *            the padre
     * 
     * @return the file
     */
    public static File showGuardar(final Component padre, final String titulo, final String extension) {
        final JFileChooser fchooser = new JFileChooser();
        if (extension != null) fchooser.setFileFilter(new SwingFileFilter(""));
        fchooser.setDialogTitle(titulo);
        final int result = fchooser.showSaveDialog(padre);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fchooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Show guardar carpeta.
     * 
     * @param padre
     *            the padre
     * @param titulo
     *            the titulo
     * @param extension
     *            the extension
     * 
     * @return the file
     */
    public static File showGuardarCarpeta(final Component padre, final String titulo, final String extension) {
        final JDirectoryChooser fchooser = new JDirectoryChooser();
        fchooser.setDialogType(JFileChooser.DIRECTORIES_ONLY);
        fchooser.setDialogTitle(titulo);
        fchooser.setFileFilter(new SwingFileFilter(extension));
        fchooser.setMultiSelectionEnabled(false);
        final int result = fchooser.showSaveDialog(padre);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fchooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Show abrir.
     * 
     * @param titulo
     *            the titulo
     * @param extension
     *            the extension
     * @param padre
     *            the padre
     * @param path
     *            the path
     * 
     * @return the file
     */
    public static File showAbrir(final Component padre, final String titulo, final String extension, final String path) {
        final JFileChooser fchooser = new JFileChooser();
        fchooser.setFileFilter(new SwingFileFilter(extension));
        fchooser.setDialogTitle(titulo);
        if (path != null && !path.isEmpty() && FileOperator.fileExists(path)) fchooser.setCurrentDirectory(new File(path));
        final int result = fchooser.showOpenDialog(padre);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fchooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Show confirmar.
     * 
     * @param titulo
     *            the titulo
     * @param texto
     *            the texto
     * @param padre
     *            the padre
     * 
     * @return true, if show confirmar
     */
    public static boolean showConfirmar(final Component padre, final String titulo, final String texto) {
        final int result = JOptionPane.showConfirmDialog(padre, texto, titulo, JOptionPane.YES_NO_OPTION);
        return (result == JOptionPane.YES_OPTION);
    }

    /**
     * Show advertencia.
     * 
     * @param parent
     *            the parent
     * @param string
     *            the string
     */
    public static void showAdvertencia(final Component parent, final String string) {
        System.out.println(string);
        JOptionPane.showMessageDialog(parent, string, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show error.
     * 
     * @param parent
     *            the parent
     * @param string
     *            the string
     */
    public static void showError(final Component parent, final String string) {
        System.err.println(string);
        JOptionPane.showMessageDialog(parent, string, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show input.
     * 
     * @param frmPrincipal
     *            the frm principal
     * @param title
     *            the title
     * @param label
     *            the label
     * 
     * @return the string
     */
    public static String showInput(final Component frmPrincipal, final String title, final String label) {
        return JOptionPane.showInputDialog(frmPrincipal, title, label);
    }

    /**
     * Show mensaje.
     * 
     * @param parent
     *            the parent
     * @param string
     *            the string
     */
    public static void showMensaje(final Component parent, final String string) {
        System.out.println(string);
        JOptionPane.showMessageDialog(parent, string, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show option pane.
     * 
     * @param parent
     *            the parent
     * @param title
     *            the title
     * @param messages
     *            the messages
     * 
     * @return the int
     */
    public static int showOptionPane(final Component parent, final String title, final Object[] messages) {
        final String[] options = new String[] { "Aceptar", "Cancelar" };
        final int result = JOptionPane.showOptionDialog(parent, messages, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        return result;
    }

    /**
     * Class for name.
     * 
     * @param className
     *            the class name
     * @param args
     *            the args
     * 
     * @return the object
     * 
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    @SuppressWarnings("unchecked")
    public static Object classForName(final String className, final Object[] args) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final Class c = Class.forName(className);
        final Constructor constructors[] = c.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            final Constructor constructor = constructors[i];
            final Class types[] = constructor.getParameterTypes();
            if (args == null) {
                return constructor.newInstance();
            } else if (types.length == args.length) {
                return constructor.newInstance(args);
            }
        }
        return null;
    }

    public static FileFilter createFileFilter(final String extension) {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) return false;
                if (pathname.isDirectory()) return true;
                if (pathname.getName().toLowerCase().endsWith(extension)) return true;
                return false;
            }
        };
        return filter;
    }
}
