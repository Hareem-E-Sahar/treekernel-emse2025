package net.rptools.chartool.ui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import com.jeta.forms.components.panel.FormPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * A controller that will handle a file selection button and field. The text
 * field can contain a url as well if configured that way  
 * 
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public class FileSelectorController implements ActionListener {

    /**
   * Button used for browsing icons
   */
    private AbstractButton browseButton;

    /**
   * Field containing the file name or field.
   */
    private JTextField field;

    /**
   * Flag used to indicate that the text field is formatted as a url.
   */
    private boolean urlFormat;

    /**
   * Filter used to restrict file list.
   */
    private FileFilter filter;

    /**
   * Contents of the field that were last validated. Used to keep from validating a file more than one time.
   */
    private String validatedFile;

    /**
   * Logger instance for this class.
   */
    private static final Logger logger = Logger.getLogger(FileSelectorController.class.getName());

    /**
   * Load the components from the panel.
   * 
   * @param panel Panel that contains the components.
   * @param fieldNames The names of the components used to set an icon. The first
   * is the <code>JTextField</code> that displays the field/url. The second is 
   * the <code>JButton</code> that displays the file chooser.
   * @param isUrlFormat Flag indicating that the text field is formattad as 
   * @param theFilter Restrict files with this filter
   */
    public FileSelectorController(FormPanel panel, String[] fieldNames, boolean isUrlFormat, FileFilter theFilter) {
        field = panel.getTextField(fieldNames[0]);
        browseButton = panel.getButton(fieldNames[1]);
        browseButton.addActionListener(this);
        urlFormat = isUrlFormat;
        filter = theFilter;
    }

    /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
    @SuppressWarnings("all")
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == browseButton) {
            JFileChooser chooser = Utilities.getChooser(filter);
            if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(field)) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    if (urlFormat) {
                        try {
                            field.setText(file.toURI().toURL().toExternalForm());
                        } catch (MalformedURLException e) {
                            logger.log(Level.WARNING, "This should not happen since it is guaranteed to be a file", e);
                        }
                    } else {
                        field.setText(file.getAbsolutePath());
                    }
                    if (validateFile(field.getText())) {
                        validatedFile = field.getText();
                    } else {
                        validatedFile = null;
                    }
                }
            }
        }
    }

    /**
   * Validate a file. This is called after a user selects a file using the browse button and 
   * whenever the file is retrieved using {@link #getFile()} or {@link #getUrl()}.
   * 
   * @param file The file name or URL text of the data to be validated.
   * @return The value <code>true</code> if the file/url is valid.
   */
    protected boolean validateFile(String file) {
        return true;
    }

    /**
   * Get the file currently in the field. 
   * 
   * @return The file contained in the field or <code>null</code> if it is empty.
   * @throws IllegalStateException If the current file is invalid. 
   */
    public File getFile() {
        if (urlFormat) throw new IllegalStateException("Can not fetch files from URL mode");
        String file = field.getText();
        if (file == null || (file = file.trim()).length() == 0) return null;
        if (!file.equals(validatedFile)) {
            if (validateFile(file)) {
                validatedFile = file;
            } else {
                validatedFile = null;
                throw new IllegalStateException("Invalid file: " + file);
            }
        }
        return new File(file);
    }

    /**
   * Get the data from the field as a URL.
   * 
   * @return A valid URL or <code>null</code> if the field is empty.
   * @throws MalformedURLException A URL could not be created because the text in the field is a bad URL.
   * @throws IllegalStateException The URL could not be validated.
   */
    @SuppressWarnings("all")
    public URL getUrl() throws MalformedURLException {
        String file = field.getText();
        if (file == null || (file = file.trim()).length() == 0) return null;
        if (!file.equals(validatedFile)) {
            if (validateFile(file)) {
                validatedFile = file;
            } else {
                validatedFile = null;
                throw new IllegalStateException("Invalid file: " + file);
            }
        }
        if (urlFormat) {
            return new URL(file);
        } else {
            return new File(file).toURI().toURL();
        }
    }

    /**
   * Determine if the file or url that is in the text can be read.
   * 
   * @return The value Boolean.TRUE if the value can be read or 
   * Boolean.FALSE if it can not. The <code>null</code> value is returned 
   * if there isn't a any text in the field.
   */
    public Boolean canRead() {
        if (urlFormat) {
            try {
                URL url = getUrl();
                if (url == null) return null;
                url.openStream().close();
                return Boolean.TRUE;
            } catch (MalformedURLException e) {
                return Boolean.FALSE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        } else {
            File file = getFile();
            if (file == null) return null;
            return file.exists() && file.canRead();
        }
    }

    /**
   * Set the visibility of all of the components.
   * 
   * @param visible The new value of the visibility state.
   * @param row Optional row number within the form panel. Used to actually hide the row so 
   * that it takes up no space. Pass a value &lt; 0 if you don't need that done. 
   */
    public void setVisible(boolean visible, int row) {
        if (row >= 0) {
            FormPanel panel = (FormPanel) SwingUtilities.getAncestorOfClass(FormPanel.class, field);
            if (visible) {
                ((FormLayout) panel.getFormContainer().getLayout()).setRowSpec(row, new RowSpec("pref"));
            } else {
                ((FormLayout) panel.getFormContainer().getLayout()).setRowSpec(row, new RowSpec("0px"));
            }
            field.setVisible(visible);
            browseButton.setVisible(visible);
        }
    }
}
