package org.azrul.epice.web;

import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.Checkbox;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.component.Html;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.Link;
import com.sun.webui.jsf.component.Page;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.Upload;
import com.sun.webui.jsf.event.TableSelectPhaseListener;
import com.sun.webui.jsf.model.UploadedFile;
import de.schlichtherle.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.azrul.epice.domain.Attachment;
import org.azrul.epice.domain.FileRepository;
import org.azrul.epice.domain.Person;
import org.azrul.epice.dao.AttachmentDAO;
import org.azrul.epice.dao.factory.AttachmentDAOFactory;
import org.azrul.epice.dao.FileRepositoryDAO;
import org.azrul.epice.dao.factory.FileRepositoryDAOFactory;
import org.azrul.epice.exception.FileRepositoryNotExistException;
import org.azrul.epice.util.LogUtil;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 *
 * @author Azrul Hasni MADISA
 */
public class PgFileRepository extends AbstractPageBean {

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Page page1 = new Page();

    public Page getPage1() {
        return page1;
    }

    public void setPage1(Page p) {
        this.page1 = p;
    }

    private Html html1 = new Html();

    public Html getHtml1() {
        return html1;
    }

    public void setHtml1(Html h) {
        this.html1 = h;
    }

    private Head head1 = new Head();

    public Head getHead1() {
        return head1;
    }

    public void setHead1(Head h) {
        this.head1 = h;
    }

    private Link link1 = new Link();

    public Link getLink1() {
        return link1;
    }

    public void setLink1(Link l) {
        this.link1 = l;
    }

    private Body body1 = new Body();

    public Body getBody1() {
        return body1;
    }

    public void setBody1(Body b) {
        this.body1 = b;
    }

    private Form form1 = new Form();

    public Form getForm1() {
        return form1;
    }

    public void setForm1(Form f) {
        this.form1 = f;
    }

    private ImageComponent image1 = new ImageComponent();

    public ImageComponent getImage1() {
        return image1;
    }

    public void setImage1(ImageComponent ic) {
        this.image1 = ic;
    }

    private StaticText sent_item1 = new StaticText();

    public StaticText getSent_item1() {
        return sent_item1;
    }

    public void setSent_item1(StaticText st) {
        this.sent_item1 = st;
    }

    private Upload fuUpload = new Upload();

    public Upload getFuUpload() {
        return fuUpload;
    }

    public void setFuUpload(Upload u) {
        this.fuUpload = u;
    }

    private Table tbAttachments = new Table();

    public Table getTbAttachments() {
        return tbAttachments;
    }

    public void setTbAttachments(Table t) {
        this.tbAttachments = t;
    }

    private TableRowGroup trgAttachments = new TableRowGroup();

    public TableRowGroup getTrgAttachments() {
        return trgAttachments;
    }

    public void setTrgAttachments(TableRowGroup trg) {
        this.trgAttachments = trg;
    }

    private Button btnUpload = new Button();

    public Button getBtnUpload() {
        return btnUpload;
    }

    public void setBtnUpload(Button b) {
        this.btnUpload = b;
    }

    private Button btnBack = new Button();

    public Button getBtnBack() {
        return btnBack;
    }

    public void setBtnBack(Button b) {
        this.btnBack = b;
    }

    private TableColumn tableColumn4 = new TableColumn();

    public TableColumn getTableColumn4() {
        return tableColumn4;
    }

    public void setTableColumn4(TableColumn tc) {
        this.tableColumn4 = tc;
    }

    private Hyperlink hlOpenAttachment = new Hyperlink();

    public Hyperlink getHlOpenAttachment() {
        return hlOpenAttachment;
    }

    public void setHlOpenAttachment(Hyperlink h) {
        this.hlOpenAttachment = h;
    }

    private TableColumn tableColumn5 = new TableColumn();

    public TableColumn getTableColumn5() {
        return tableColumn5;
    }

    public void setTableColumn5(TableColumn tc) {
        this.tableColumn5 = tc;
    }

    private StaticText staticText5 = new StaticText();

    public StaticText getStaticText5() {
        return staticText5;
    }

    public void setStaticText5(StaticText st) {
        this.staticText5 = st;
    }

    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() {
        return staticText1;
    }

    public void setStaticText1(StaticText st) {
        this.staticText1 = st;
    }

    private StaticText stOwner = new StaticText();

    public StaticText getStOwner() {
        return stOwner;
    }

    public void setStOwner(StaticText st) {
        this.stOwner = st;
    }

    private Button btnDeleteSelected = new Button();

    public Button getBtnDeleteSelected() {
        return btnDeleteSelected;
    }

    public void setBtnDeleteSelected(Button b) {
        this.btnDeleteSelected = b;
    }

    private TableColumn trgAttachmentsSelectionColumn = new TableColumn();

    public TableColumn getTrgAttachmentsSelectionColumn() {
        return trgAttachmentsSelectionColumn;
    }

    public void setTrgAttachmentsSelectionColumn(TableColumn tc) {
        this.trgAttachmentsSelectionColumn = tc;
    }

    private Checkbox trgAttachmentsSelectionChild = new Checkbox();

    public Checkbox getTrgAttachmentsSelectionChild() {
        return trgAttachmentsSelectionChild;
    }

    public void setTrgAttachmentsSelectionChild(Checkbox c) {
        this.trgAttachmentsSelectionChild = c;
    }

    private TableSelectPhaseListener tablePhaseListener = new TableSelectPhaseListener();

    private TableColumn tableColumn1 = new TableColumn();

    private AttachmentDAO attachmentDAO = AttachmentDAOFactory.getInstance();

    private FileRepositoryDAO fileRepoDAO = FileRepositoryDAOFactory.getInstance();

    public TableColumn getTableColumn1() {
        return tableColumn1;
    }

    public void setTableColumn1(TableColumn tc) {
        this.tableColumn1 = tc;
    }

    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() {
        return staticText2;
    }

    public void setStaticText2(StaticText st) {
        this.staticText2 = st;
    }

    private Button btnAttachFromItems = new Button();

    public Button getBtnAttachFromItems() {
        return btnAttachFromItems;
    }

    public void setBtnAttachFromItems(Button b) {
        this.btnAttachFromItems = b;
    }

    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public PgFileRepository() {
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
    @Override
    public void init() {
        super.init();
        try {
            _init();
        } catch (Exception e) {
            log("PgFileRepository Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
        }
        stOwner.setText(getSessionBean1().getCurrentFileRepository().getOwner().getName() + "[" + getSessionBean1().getCurrentFileRepository().getOwner().getEmail() + "]");
        btnUpload.setDisabled(!getSessionBean1().getEnableUpload());
        btnDeleteSelected.setDisabled(!getSessionBean1().getEnableUpload());
        btnAttachFromItems.setDisabled(!getSessionBean1().getEnableUpload());
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

    public String hlOpenAttachment_action() {
        FileInputStream fileToDownload = null;
        ServletOutputStream out = null;
        try {
            RowKey rk = getTrgAttachments().getRowKey();
            Attachment attachment = (Attachment) getSessionBean1().getFileRepositoryDP().getObject(rk);
            java.io.File file = new java.io.File(attachment.getFilePath());
            fileToDownload = new FileInputStream(file);
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            out = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLength(fileToDownload.available());
            int c;
            while ((c = fileToDownload.read()) != -1) {
                out.write(c);
            }
            out.flush();
        } catch (Exception ex) {
            LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
        } finally {
            if (fileToDownload != null) {
                try {
                    fileToDownload.close();
                } catch (IOException ex) {
                    LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
                }
            }
        }
        return null;
    }

    public String btnUpload_action() {
        ResourceBundle props = ResourceBundle.getBundle("epice");
        UploadedFile uploadedFile = fuUpload.getUploadedFile();
        if (uploadedFile == null) {
            return null;
        }
        String uploadedFileName = uploadedFile.getOriginalName();
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
        String fileRepo = props.getString("FILE_REPOSITORY");
        Attachment attachment = null;
        try {
            java.io.File tempDir = java.io.File.createTempFile("EPICE_TMP_", "", new java.io.File(fileRepo));
            if (!tempDir.delete()) {
                throw new IOException();
            }
            if (!tempDir.mkdirs()) {
                throw new IOException();
            }
            java.io.File tempFile = new java.io.File(tempDir.getPath() + "/" + justFileName);
            uploadedFile.write(tempFile);
            attachment = attachmentDAO.createWithoutPersisting(tempDir.getName() + "/" + justFileName, getSessionBean1().getCurrentUser().getEmail());
            FileRepository fr = getSessionBean1().getCurrentFileRepository();
            FileRepository fr2 = fileRepoDAO.addNewAttachment(fr, attachment);
            getSessionBean1().setCurrentFileRepository(fr2);
        } catch (IOException ex) {
            LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
        } catch (FileRepositoryNotExistException ex) {
            if (attachment != null) {
                getSessionBean1().getCurrentFileRepository().getAttachments().add(attachment);
            }
        } catch (Exception ex) {
            LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
        }
        getSessionBean1().getCurrentFileRepository().refreshDataProvider(getSessionBean1().getCurrentUser());
        return null;
    }

    public String btnSave_action() {
        return null;
    }

    public String btnBack_action() {
        if (("PgNewItem").equals(getSessionBean1().getPreviousPage())) {
            getSessionBean1().popPreviousPage();
            return "gotoNewItem";
        } else if (("PgReceivedItem").equals(getSessionBean1().getPreviousPage())) {
            getSessionBean1().popPreviousPage();
            return "gotoReceivedItem";
        } else if (("PgSentItem").equals(getSessionBean1().getPreviousPage())) {
            getSessionBean1().popPreviousPage();
            return "gotoSentItem";
        }
        return null;
    }

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

    public String btnDeleteSelected_action() {
        RowKey[] selectedRowKeys = getTrgAttachments().getSelectedRowKeys();
        List<Attachment> attachmentsToBeDeleted = new ArrayList<Attachment>();
        for (int i = 0; i < selectedRowKeys.length; i++) {
            Attachment attachment = (Attachment) getSessionBean1().getFileRepositoryDP().getObject(selectedRowKeys[i]);
            attachmentsToBeDeleted.add(attachment);
        }
        FileRepository fileRepo = getSessionBean1().getCurrentFileRepository();
        Person currentUser = getSessionBean1().getCurrentUser();
        try {
            FileRepository fr = fileRepoDAO.removeAttachmentFromFileRepository(fileRepo, attachmentsToBeDeleted, currentUser);
            getSessionBean1().setCurrentFileRepository(fr);
        } catch (FileRepositoryNotExistException e) {
            for (int i = 0; i < selectedRowKeys.length; i++) {
                Attachment attachment = (Attachment) getSessionBean1().getFileRepositoryDP().getObject(selectedRowKeys[i]);
                getSessionBean1().getCurrentFileRepository().getAttachments().remove(attachment);
            }
        }
        getSessionBean1().getCurrentFileRepository().refreshDataProvider(getSessionBean1().getCurrentUser());
        tablePhaseListener.clear();
        return null;
    }

    public String btnDeleteRepository_action() {
        if (getSessionBean1().getCurrentUser().equals(getSessionBean1().getCurrentFileRepository().getOwner())) {
            getSessionBean1().setCurrentFileRepository(null);
        }
        return btnBack_action();
    }

    public String btnAttachFromItems_action() {
        getSessionBean1().emptySearchItemsDP();
        return "gotoFileFromItems";
    }

    public String button1_action() {
        RowKey[] selectedRowKeys = getTrgAttachments().getSelectedRowKeys();
        ServletOutputStream out = null;
        FileInputStream fileToDownload = null;
        ZipOutputStream zout = null;
        try {
            byte[] buf = new byte[1024];
            String outFilename = "mewit_files_" + UUID.randomUUID().toString() + ".zip";
            zout = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < selectedRowKeys.length; i++) {
                Attachment attachment = (Attachment) getSessionBean1().getFileRepositoryDP().getObject(selectedRowKeys[i]);
                FileInputStream in = new FileInputStream(attachment.getFilePath());
                zout.putNextEntry(new ZipEntry(attachment.getFileName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    zout.write(buf, 0, len);
                }
                zout.closeEntry();
                in.close();
            }
            zout.close();
            java.io.File zipFile = new java.io.File(outFilename);
            fileToDownload = new FileInputStream(zipFile);
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            out = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
            response.setContentLength(fileToDownload.available());
            int c;
            while ((c = fileToDownload.read()) != -1) {
                out.write(c);
            }
            out.flush();
        } catch (Exception ex) {
            LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
        } finally {
            if (fileToDownload != null) {
                try {
                    fileToDownload.close();
                } catch (IOException ex) {
                    LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LogUtil.getLogger().log(Level.SEVERE, Thread.currentThread().getStackTrace()[3].getMethodName(), ex);
                }
            }
        }
        return null;
    }
}
