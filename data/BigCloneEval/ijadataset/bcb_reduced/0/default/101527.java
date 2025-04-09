import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.ListModel;

/**
 * Used to export the data in an AddressBook into an HTML document suitable for
 * printing. This is simpler than printing itself and is more universal, which
 * is the goal of the program.
 * 
 * @author Robert S. Moore II
 */
public class AddressBookHTMLExporter {

    private AddressBook parent;

    private AddressBookHTMLExporterUI ui;

    private JFileChooser fileChooser;

    private ListModel data;

    public AddressBookHTMLExporter(AddressBook parent) {
        super();
        this.parent = parent;
        ui = new AddressBookHTMLExporterUI(this);
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new HTMLFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
    }

    /**
	 * Returns the extension of the file name.
	 * 
	 * @param f
	 *            A file.
	 * @return The extension of the file name (the part after the '.' separator
	 *         for most file systems).
	 */
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
	 * Exports the data in the vector to HTML interactively.
	 * 
	 * @param data
	 *            A vector containing AddressBookDatum objects.
	 */
    public void exportToHTML(ListModel data) {
        if (!(data == null)) {
            ui.setVisible(true);
            ui.setListModel(data);
            this.data = data;
        } else {
            endExport();
        }
    }

    /**
	 * Restores the parent to working condition. This is an internal method and
	 * should not be invoked by non-member classes.
	 *  
	 */
    protected void endExport() {
        parent.endExport();
    }

    /**
	 * Has the user choose a file to export to. This is an internal method and
	 * should not be invoked by non-member classes.
	 *  
	 */
    protected void chooseFile() {
        int approveValue = fileChooser.showSaveDialog(null);
        File selectedFile;
        if (approveValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            if ((!getExtension(selectedFile).equals("html")) && (!getExtension(selectedFile).equals("htm"))) {
                selectedFile = (new File(selectedFile.getPath() + ".htm"));
            }
            export(selectedFile);
        } else {
            endExport();
        }
    }

    /**
	 * Does the actual exporting to a file. Relies on an
	 * AddressBookHTMLExporterUI class to get input from the user.
	 * 
	 * @param selectedFile
	 *            The file to write to.
	 */
    private void export(File selectedFile) {
        if (!selectedFile.canWrite()) {
            try {
                selectedFile.createNewFile();
            } catch (IOException ioe) {
            }
        }
        if (selectedFile.canWrite()) {
            try {
                OutputStream fout = new FileOutputStream(selectedFile);
                OutputStream bout = new BufferedOutputStream(fout);
                OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
                out.write("<html>\n");
                out.write("\t<head>\n");
                out.write("\t</head>\n");
                out.write("\t<body>\n");
                Vector vec;
                if (data.getSize() >= 1) {
                    out.write("\t\t<ul>\n");
                }
                for (int i = 0; i < data.getSize(); i++) {
                    if (checkIndex(ui.entryList.getSelectedIndices(), i)) {
                        vec = new Vector();
                        vec = loadData((AddressBookDatum) data.getElementAt(i));
                        if (vec.size() >= 1) {
                            out.write("\t\t\t<li>\n");
                            out.write("\t\t\t\t<h3>");
                            out.write(vec.get(0) + "</h3>\n");
                        }
                        if (vec.size() >= 2) {
                            out.write("\t\t\t\t<ul>\n");
                        }
                        for (int j = 1; j < vec.size(); j++) {
                            out.write("\t\t\t\t\t<li>");
                            out.write((String) vec.get(j));
                            out.write("</li>\n");
                        }
                        if (vec.size() >= 2) {
                            out.write("\t\t\t\t</ul>\n");
                        }
                        if (vec.size() >= 1) {
                            out.write("\t\t\t</li>\n");
                        }
                    }
                }
                if (data.getSize() >= 1) {
                    out.write("\t\t</ul>\n");
                }
                out.write("\t</body>\n");
                out.write("</html>\n");
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        endExport();
    }

    /**
	 * Determines whether index is located in indices. Used to invert the
	 * selections.
	 * 
	 * @param indices
	 *            An array of ints.
	 * @param index
	 *            An int.
	 * @return True if index is located in indices, else false.
	 */
    protected boolean checkIndex(int[] indices, int index) {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == index) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Returns a Vector containing the requested/non-default fields for output.
	 * 
	 * @param datum
	 *            The data source to work from.
	 * @return A vector containing some(all) of the data in datum.
	 */
    private Vector loadData(AddressBookDatum datum) {
        Vector vec = new Vector();
        if (ui.businessCheck.isSelected() && !datum.getBusinessName().trim().equals("")) {
            vec.add(new String("<b>Business Name:</b> " + datum.getBusinessName()));
        }
        if (ui.contactCheck.isSelected() && !datum.getContact().trim().equals("")) {
            vec.add(new String("<b>Contact:</b> " + datum.getContact()));
        }
        if (ui.phoneNumberCheck.isSelected() && !datum.getPhoneNumber().trim().equals("")) {
            vec.add(new String("<b>Phone Number:</b> " + datum.getPhoneNumber()));
        }
        if (ui.altPhoneNumberCheck.isSelected() && !datum.getAltPhoneNumber().trim().equals("")) {
            vec.add(new String("<b>Alt. Phone Number:</b> " + datum.getAltPhoneNumber()));
        }
        if (ui.faxNumberCheck.isSelected() && !datum.getFaxNumber().trim().equals("")) {
            vec.add(new String("<b>Fax Number:</b> " + datum.getFaxNumber()));
        }
        if (ui.accountCheck.isSelected() && !datum.getAccount().trim().equals("")) {
            vec.add(new String("<b>Account Number:</b> " + datum.getAccount()));
        }
        if (ui.mailingAddressCheck.isSelected() && !datum.getMailingAddress().trim().equals("")) {
            vec.add(new String("<b>Mailing Address:</b> <pre>" + datum.getMailingAddress() + "</pre>"));
        }
        if (ui.streetAddressCheck.isSelected() && !datum.getStreetAddress().trim().equals("")) {
            vec.add(new String("<b>Street Address:</b> <pre>" + datum.getStreetAddress() + "</pre>"));
        }
        if (ui.emailCheck.isSelected() && !datum.getEmailAddress().trim().equals("")) {
            vec.add(new String("<b>E-mail Address:</b> " + datum.getEmailAddress()));
        }
        if (ui.homePageCheck.isSelected() && !datum.getHomePage().trim().equals("http://") && !datum.getHomePage().trim().equals("")) {
            vec.add(new String("<b>Home Page(URL):</b> " + datum.getHomePage()));
        }
        if (ui.categoryCheck.isSelected() && !datum.getCategory().trim().equals("")) {
            vec.add(new String("<b> Category:</b> " + datum.getCategory()));
        }
        if (ui.commentCheck.isSelected() && !datum.getNotes().trim().equals("")) {
            vec.add(new String("<b>Comments/Notes:</b> <pre>" + datum.getNotes() + "</pre>"));
        }
        return vec;
    }
}
