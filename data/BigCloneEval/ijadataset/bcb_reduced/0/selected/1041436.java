package org.designerator.media.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.designerator.common.data.ProcessorDataList;
import org.designerator.common.interfaces.IThumb;
import org.designerator.common.interfaces.IThumbAlbum;
import org.designerator.common.interfaces.IThumbsContainer;
import org.designerator.common.string.StringUtil;
import org.designerator.common.system.EmailSupport;
import org.designerator.common.system.FileUtil;
import org.designerator.image.algo.util.ImageConversion;
import org.designerator.media.MediaPlugin;
import org.designerator.media.image.util.BatchUtilities;
import org.designerator.media.image.util.IO;
import org.designerator.media.internet.common.SizePage;
import org.designerator.media.internet.common.ThumbsPage;
import org.designerator.media.util.ImageHelper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class ExportZipWizard extends Wizard {

    private static final String INVALID_PATH = Messages.ExportZipWizard_invalid;

    private static final String TITLE = Messages.ExportZipWizard_title;

    public static void createDialog(Shell shell, IThumbsContainer thumbScroller) {
        ExportZipWizard videoWiz = new ExportZipWizard(thumbScroller);
        WizardDialog dialog = new WizardDialog(shell, videoWiz);
        videoWiz.setDialog(dialog);
        dialog.create();
        dialog.open();
    }

    public static Status getCancelStatus() {
        return new Status(Status.CANCEL, MediaPlugin.PLUGIN_ID, Messages.ExportZipWizard_upcancel);
    }

    WizardDialog dialog;

    private ThumbsPage imagesPage;

    protected boolean login;

    private Thread loginThread;

    private SizePage sizePage;

    IThumbsContainer thumbScroller;

    private IThumbAlbum activeDisplayAlbum;

    private OptionsPage optionsPage;

    protected volatile boolean deleteOk;

    private Display display;

    private ZipOutputStream outStream;

    public ExportZipWizard(IThumbsContainer thumbScroller) {
        if (thumbScroller == null) {
            setWindowTitle(Messages.ExportZipWizard_errorread);
        } else {
            setWindowTitle(Messages.ExportZipWizard_export);
            this.thumbScroller = thumbScroller;
            activeDisplayAlbum = this.thumbScroller.getActiveDisplayAlbum();
        }
        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        optionsPage = new OptionsPage(Messages.ExportZipWizard_choosepath, false);
        optionsPage.setTitle(TITLE);
        addPage(optionsPage);
        sizePage = new SizePage(Messages.ExportZipWizard_setsize, this);
        sizePage.setTitle(TITLE);
        addPage(sizePage);
        imagesPage = new ThumbsPage(Messages.ExportZipWizard_images, this);
        imagesPage.setTitle(TITLE);
        if (activeDisplayAlbum != null) {
            List<IThumb> ts = activeDisplayAlbum.getThumbsWithChildren();
            imagesPage.setThumbs(ts.toArray(new IThumb[ts.size()]));
        }
        addPage(imagesPage);
        dialog.addPageChangedListener(new IPageChangedListener() {

            @Override
            public void pageChanged(PageChangedEvent event) {
                Object selectedPage = event.getSelectedPage();
                if (selectedPage.equals(imagesPage)) {
                    imagesPage.setPageComplete(true);
                }
            }
        });
        Image img = ImageHelper.createImage(null, "/icons/exportdir_wiz.gif");
        if (img != null) {
            getShell().setImage(img);
        }
        optionsPage.setPageComplete(false);
    }

    public void close() {
        if (outStream != null) {
            try {
                outStream.close();
                outStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        if (loginThread != null) {
            loginThread = null;
        }
        super.dispose();
    }

    public void error(String message) {
        Shell shell = getAvailableShell();
        if (shell != null) {
            MessageBox m = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            m.setMessage(message);
            m.setText(Messages.ExportZipWizard_error);
            m.open();
        }
    }

    public IStatus export(File outFile, List<IThumb> ts, IProgressMonitor monitor, double resizeFactor, float jpegQuality) {
        if (ts == null || monitor == null) {
            return new Status(Status.ERROR, MediaPlugin.PLUGIN_ID, Messages.ExportZipWizard_uperror);
        }
        monitor.beginTask(Messages.ExportZipWizard_inprogress, ts.size());
        try {
            outStream = new ZipOutputStream(new FileOutputStream(outFile));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return new Status(Status.ERROR, MediaPlugin.PLUGIN_ID, e1.getMessage());
        }
        for (IThumb iThumb : ts) {
            if (iThumb != null) {
                try {
                    if (monitor.isCanceled()) {
                        close();
                        return getCancelStatus();
                    }
                    File sysfile = iThumb.getSysfile();
                    if (sysfile != null) {
                        String name = ExportUtil.getName(iThumb, sysfile, sizePage.isJpeg());
                        ProcessorDataList processorData = null;
                        if (optionsPage.isUseProcessing()) {
                            processorData = iThumb.getProcessorData();
                        }
                        ImageData idata = BatchUtilities.processImageData(sysfile, resizeFactor, processorData);
                        if (monitor.isCanceled()) {
                            close();
                            return getCancelStatus();
                        }
                        byte[] data = ImageConversion.getJpgCompressedArray(idata, (int) (jpegQuality * 100));
                        if (data != null) {
                            writeEntry(data, name, outStream);
                        }
                    }
                    monitor.worked(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                    return new Status(Status.ERROR, MediaPlugin.PLUGIN_ID, Messages.ExportZipWizard_ziperror);
                }
            }
        }
        close();
        return new Status(Status.OK, MediaPlugin.PLUGIN_ID, Messages.ExportZipWizard_finnished);
    }

    public Shell getAvailableShell() {
        Shell shell = getShell();
        if (shell == null) {
            shell = MediaPlugin.getShell();
        }
        return shell;
    }

    public Status getErrorSatus() {
        return new Status(Status.WARNING, MediaPlugin.PLUGIN_ID, Messages.ExportZipWizard_upfail);
    }

    public String getTitle() {
        String name = "";
        if (activeDisplayAlbum != null) {
            name = activeDisplayAlbum.getName();
        }
        return name;
    }

    @Override
    public boolean performFinish() {
        runExportZipJob(imagesPage.getThumbs());
        return true;
    }

    public void runExportZipJob(final IThumb[] thumb) {
        String directory = optionsPage.getDirectory();
        if (StringUtil.isEmpty(directory)) {
            error(INVALID_PATH + ": Null");
            return;
        }
        final File f = new File(directory);
        if (f.exists()) {
            if (f.isDirectory()) {
                error(INVALID_PATH + ":" + f.getAbsolutePath());
                return;
            }
            Shell shell = getAvailableShell();
            if (shell != null) {
                MessageBox m = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                m.setMessage(Messages.ExportZipWizard_file + f.getName() + Messages.ExportZipWizard_existsconfirm);
                m.setText(Messages.ExportZipWizard_confirm);
                if (m.open() == SWT.CANCEL) {
                    return;
                }
            }
        } else {
            try {
                IO.makeFile(f);
            } catch (IOException e) {
                error(INVALID_PATH + ":" + f.getAbsolutePath());
                e.printStackTrace();
                return;
            }
        }
        display = thumbScroller.getControl().getDisplay();
        final boolean email = optionsPage.isOpenEmail();
        final double resizeFactor = sizePage.getResizeFactor();
        final float jpegQuality = sizePage.getJpegQuality();
        final boolean folder = optionsPage.isOpenFolder();
        final Job job = new Job("Zip Export") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                return export(f, Arrays.asList(thumb), monitor, resizeFactor, jpegQuality);
            }
        };
        job.setUser(true);
        job.schedule();
        job.addJobChangeListener(new JobChangeAdapter() {

            public void done(final IJobChangeEvent event) {
                close();
                if (email && display != null && !display.isDisposed()) {
                    display.asyncExec(new Runnable() {

                        public void run() {
                            EmailSupport.email(null, null);
                        }
                    });
                }
                if (folder && display != null && !display.isDisposed()) {
                    display.asyncExec(new Runnable() {

                        public void run() {
                            File out = new File(optionsPage.getDirectory());
                            if (!out.isDirectory()) {
                                out = out.getParentFile();
                            }
                            FileUtil.openWindowsExplorer(out);
                        }
                    });
                }
            }
        });
    }

    private void setDialog(WizardDialog dialog) {
        this.dialog = dialog;
    }

    private void writeEntry(byte[] data, String name, ZipOutputStream outStream) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(data.length);
        entry.setTime(System.currentTimeMillis());
        if (outStream != null) {
            outStream.putNextEntry(entry);
            outStream.write(data);
            outStream.closeEntry();
        }
    }
}
