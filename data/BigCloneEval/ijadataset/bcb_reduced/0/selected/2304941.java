package archivewar;

import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.component.RadioButton;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.component.Upload;
import com.sun.webui.jsf.event.TableSelectPhaseListener;
import com.sun.webui.jsf.model.UploadedFile;
import ejb.Archive;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.FacesException;
import javax.servlet.ServletContext;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 *
 * @version main.java
 * @version Created on 03.06.2009, 18:15:40
 * @author RostislavCh
 */
public class main extends AbstractPageBean {

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private TableRowGroup tableRowGroup1 = new TableRowGroup();

    public TableRowGroup getTableRowGroup1() {
        return tableRowGroup1;
    }

    public void setTableRowGroup1(TableRowGroup trg) {
        this.tableRowGroup1 = trg;
    }

    private RadioButton radioButton1 = new RadioButton();

    public RadioButton getRadioButton1() {
        return radioButton1;
    }

    public void setRadioButton1(RadioButton rb) {
        this.radioButton1 = rb;
    }

    private TableColumn tableColumn7 = new TableColumn();

    public TableColumn getTableColumn7() {
        return tableColumn7;
    }

    public void setTableColumn7(TableColumn tc) {
        this.tableColumn7 = tc;
    }

    private Upload fileUpload1 = new Upload();

    public Upload getFileUpload1() {
        return fileUpload1;
    }

    public void setFileUpload1(Upload u) {
        this.fileUpload1 = u;
    }

    private MessageGroup messageGroup1 = new MessageGroup();

    public MessageGroup getMessageGroup1() {
        return messageGroup1;
    }

    public void setMessageGroup1(MessageGroup mg) {
        this.messageGroup1 = mg;
    }

    private TextField archSender = new TextField();

    public TextField getArchSender() {
        return archSender;
    }

    public void setArchSender(TextField tf) {
        this.archSender = tf;
    }

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public main() {
    }

    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    private String realImageFilePath;

    private static final String IMAGE_URL = "/resources/image-file";

    @Override
    public void init() {
        super.init();
        try {
            _init();
        } catch (Exception e) {
            log("main Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
        }
        ServletContext theApplicationsServletContext = (ServletContext) this.getExternalContext().getContext();
        this.realImageFilePath = theApplicationsServletContext.getRealPath(IMAGE_URL);
    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    @Override
    public void preprocess() {
    }

    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    @Override
    public void prerender() {
        getSessionBean1().updateArchives();
    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected SessionBean1 getSessionBean1() {
        return (SessionBean1) getBean("SessionBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected RequestBean1 getRequestBean1() {
        return (RequestBean1) getBean("RequestBean1");
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     *
     * @return reference to the scoped data bean
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }

    private boolean addRequest = false;

    public String btnAdd_action() {
        addRequest = true;
        return null;
    }

    public String btnUpdate_action() {
        return null;
    }

    public String btnDelete_action() {
        if (getTableRowGroup1().getSelectedRowsCount() > 0) {
            RowKey[] selectedRowKeys = getTableRowGroup1().getSelectedRowKeys();
            int rowId = Integer.parseInt(selectedRowKeys[0].getRowId());
            ejb.ArchiveServiceLocator.getInstance().getSessionBean().removeArchive(rowId);
        }
        return null;
    }

    public String btnUpload_action() {
        UploadedFile uploadedFile = fileUpload1.getUploadedFile();
        if (uploadedFile != null) {
            try {
                String uploadedFileName = uploadedFile.getOriginalName();
                String justFileName = getFilename(uploadedFileName);
                Long uploadedFileSize = new Long(uploadedFile.getSize());
                String uploadedFileType = uploadedFile.getContentType();
                info("[" + uploadedFileName + " " + uploadedFileSize + " " + uploadedFileType + "]");
                String uploadDirectory = getExternalContext().getInitParameter("uploadDirectory");
                File dir = new File(uploadDirectory);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(uploadDirectory + File.separatorChar + justFileName);
                uploadedFile.write(file);
                zipFile(uploadDirectory + File.separatorChar + justFileName, uploadDirectory + File.separatorChar + justFileName + ".zip");
                Archive arch = new Archive();
                arch.setArchlink("/file?id=" + arch.getArchId());
                arch.setArchname(justFileName);
                arch.setArchsender((String) archSender.getText());
                int fs = uploadedFileSize.intValue();
                arch.setFilesize(fs);
                ejb.ArchiveServiceLocator.getInstance().getSessionBean().addArchive(arch);
                arch.setArchlink("/file?id=" + arch.getArchId());
                ejb.ArchiveServiceLocator.getInstance().getSessionBean().updateArchive(arch);
            } catch (Exception e) {
                info(e.getMessage());
            }
        } else {
            info("No file uploaded");
        }
        return null;
    }

    public static void zipFile(String from, String to) throws IOException {
        FileInputStream in = new FileInputStream(from);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(to));
        out.putNextEntry(new ZipEntry(getFilename(from)));
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.closeEntry();
        out.close();
    }

    private TableSelectPhaseListener tablePhaseListener = new TableSelectPhaseListener();

    public void setSelected(Object object) {
        RowKey rowKey = (RowKey) getValue("#{currentRow.tableRow}");
        if (rowKey != null) {
            tablePhaseListener.setSelected(rowKey, object);
        }
    }

    public Object getSelected() {
        RowKey rowKey = (RowKey) getValue("#{currentRow.tableRow}");
        return tablePhaseListener.getSelected(rowKey);
    }

    public Object getSelectedValue() {
        RowKey rowKey = (RowKey) getValue("#{currentRow.tableRow}");
        return (rowKey != null) ? rowKey.getRowId() : null;
    }

    public boolean getSelectedState() {
        RowKey rowKey = (RowKey) getValue("#{currentRow.tableRow}");
        return tablePhaseListener.isSelected(rowKey);
    }

    public static String getFilename(String uploadedFileName) {
        int index = uploadedFileName.lastIndexOf('/');
        String justFileName;
        if (index >= 0) {
            justFileName = uploadedFileName.substring(index + 1);
        } else {
            index = uploadedFileName.lastIndexOf('\\');
            if (index >= 0) {
                justFileName = uploadedFileName.substring(index + 1);
            } else {
                justFileName = uploadedFileName;
            }
        }
        return justFileName;
    }
}
