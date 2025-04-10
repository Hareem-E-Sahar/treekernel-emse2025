package org.remus.infomngmnt.image.ui;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.remus.InformationUnit;
import org.eclipse.remus.util.InformationUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.remus.infomngmnt.image.ImagePlugin;
import org.remus.infomngmnt.image.gef.ImageLinkEditPartFactory;
import org.remus.infomngmnt.image.internal.ResourceManager;
import org.remus.infomngmnt.image.messages.Messages;

public class CommentImageDialog extends TitleAreaDialog {

    private Composite area;

    private FigureCanvas canvas;

    private ScrollingGraphicalViewer graphicalViewer;

    private final InformationUnit image;

    private ScrolledComposite scrolledComposite;

    private final EditingDomain editingDomain;

    /**
	 * Create the dialog
	 * 
	 * @param parentShell
	 */
    public CommentImageDialog(final Shell parentShell, final InformationUnit image, final EditingDomain editingDomain) {
        super(parentShell);
        this.editingDomain = editingDomain;
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.image = image;
    }

    /**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
    @Override
    protected Control createDialogArea(final Composite parent) {
        this.area = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
        this.area.setLayout(new FillLayout());
        this.area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.scrolledComposite = new ScrolledComposite(this.area, SWT.V_SCROLL | SWT.H_SCROLL);
        this.scrolledComposite.setLayout(new FillLayout());
        this.scrolledComposite.setExpandVertical(true);
        this.scrolledComposite.setExpandHorizontal(true);
        ViewForm vf = new ViewForm(this.scrolledComposite, SWT.BORDER | SWT.FLAT);
        vf.horizontalSpacing = 0;
        vf.verticalSpacing = 0;
        ToolBarManager tbm = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT);
        long width = InformationUtil.getChildByType(this.image, ImagePlugin.NODE_NAME_WIDTH).getLongValue();
        long height = InformationUtil.getChildByType(this.image, ImagePlugin.NODE_NAME_HEIGHT).getLongValue();
        this.graphicalViewer = new ScrollingGraphicalViewer();
        ScalableRootEditPart root = new ScalableRootEditPart();
        this.graphicalViewer.setRootEditPart(root);
        this.graphicalViewer.setEditDomain(new EditDomain());
        this.graphicalViewer.setEditPartFactory(new ImageLinkEditPartFactory(this.editingDomain));
        this.canvas = (FigureCanvas) this.graphicalViewer.createControl(vf);
        this.canvas.getHorizontalBar().setVisible(true);
        this.canvas.getVerticalBar().setVisible(true);
        this.graphicalViewer.setContents(this.image);
        DeleteCommentAction deleteLinkAction = new DeleteCommentAction(this.image);
        deleteLinkAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
        CreateCommentAction createLinkAction = new CreateCommentAction(this.image);
        createLinkAction.setEditingDomain(this.editingDomain);
        deleteLinkAction.setEditingDomain(this.editingDomain);
        tbm.add(createLinkAction);
        tbm.add(deleteLinkAction);
        this.scrolledComposite.setContent(vf);
        this.graphicalViewer.addSelectionChangedListener(deleteLinkAction);
        vf.setTopLeft(tbm.createControl(vf));
        vf.setContent(this.canvas);
        GridData gd = new GridData(SWT.BEGINNING, SWT.TOP);
        gd.widthHint = (int) width;
        gd.heightHint = (int) height;
        this.canvas.setLayoutData(gd);
        vf.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                CommentImageDialog.this.canvas.redraw();
                super.controlResized(e);
            }
        });
        setTitle(Messages.CommentImageDialog_Title);
        setMessage(Messages.CommentImageDialog_Message);
        setTitleImage(ResourceManager.getPluginImage(ImagePlugin.getDefault(), "icons/iconexperience/comment_wizard_title.png"));
        return this.area;
    }

    /**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
    }

    /**
	 * Return the initial size of the dialog
	 */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.CommentImageDialog_Title);
    }
}
