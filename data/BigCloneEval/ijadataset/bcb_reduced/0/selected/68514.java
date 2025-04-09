package org.designerator.media.importWizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.designerator.common.data.VideoInfo;
import org.designerator.common.xstream.XStreamVideoInfoHandler;
import org.designerator.image.algo.util.ImageConversion;
import org.designerator.media.image.util.IO;
import org.designerator.media.thumbs.ThumbIO;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

public class MediaImportProvider implements IImportStructureProvider {

    private static MediaImportProvider instance;

    public static final String VIDEO = "video";

    private String importMode;

    public static final String SMALL = "small";

    public static final String THUMB = "thumb";

    public static final String COPY = "copy";

    public static final String MEDIUM = "medium";

    public MediaImportProvider() {
    }

    @Override
    public List getChildren(Object element) {
        File folder = (File) element;
        String[] children = folder.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return IO.isValidMediaFile(name);
            }
        });
        int childrenLength = children == null ? 0 : children.length;
        List result = new ArrayList(childrenLength);
        for (int i = 0; i < childrenLength; i++) {
            result.add(new File(folder, children[i]));
        }
        return result;
    }

    @Override
    public InputStream getContents(Object element) {
        File f = (File) element;
        if (IO.isValidImageFile(f.getName())) {
            return getImageStream(f);
        } else if (IO.isValidVideoFile(f.getName())) {
            return getVideoLinkStream(f);
        }
        return null;
    }

    private InputStream getVideoLinkStream(File f) {
        VideoInfo v = new VideoInfo();
        v.file = f.getAbsolutePath();
        v.offline = true;
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        try {
            XStreamVideoInfoHandler.saveVideoInfo(v, out);
            byte[] byteArray = out.toByteArray();
            System.out.println("getVideoLinkStream:length: " + byteArray.length);
            return new ByteArrayInputStream(byteArray);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getImageStream(File f) {
        if (COPY.equals(importMode)) {
            return createFileInputStream(f);
        }
        if (f.length() < ThumbIO.getMinimalThumbSize()) {
            return createFileInputStream(f);
        }
        final Image img = IO.loadImage(f.getAbsolutePath(), Display.getCurrent(), false, true);
        if (img == null) {
            return null;
        }
        final Rectangle bounds = img.getBounds();
        Image thumb = null;
        Point thumbSize = ThumbIO.getThumbSize(bounds.width, bounds.height);
        if (thumbSize == null) {
            img.dispose();
            return createFileInputStream(f);
        }
        if (SMALL.equals(importMode)) {
            if (bounds.width / 4 < thumbSize.x && bounds.height / 4 < thumbSize.y) {
                thumb = ImageConversion.createQualityThumbImage(img, thumbSize.x, thumbSize.y, SWT.HIGH);
            } else {
                thumb = ImageConversion.createQualityResizedImage(img, bounds.width / 4, bounds.height / 4, SWT.HIGH);
            }
        } else if (MEDIUM.equals(importMode)) {
            thumb = ImageConversion.createQualityResizedImage(img, bounds.width / 2, bounds.height / 2, SWT.HIGH);
        } else {
            thumb = ImageConversion.createQualityThumbImage(img, thumbSize.x, thumbSize.y, SWT.HIGH);
        }
        if (thumb == null) {
            img.dispose();
            return null;
        }
        ByteArrayOutputStream outstream = ImageConversion.saveToOutputStream(thumb.getImageData(), SWT.IMAGE_JPEG, 85);
        img.dispose();
        thumb.dispose();
        if (outstream == null) {
            return null;
        }
        return new ByteArrayInputStream(outstream.toByteArray());
    }

    public InputStream createFileInputStream(File f) {
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public String getFullPath(Object element) {
        return ((File) element).getPath();
    }

    public static void main(String[] args) {
        File f = new File("i:/home/my/path/tom.jpg");
        IPath pathname = new Path(f.getPath());
        System.out.println(pathname.setDevice(null));
    }

    public void setImportMode(String mode) {
        this.importMode = mode;
    }

    @Override
    public String getLabel(Object element) {
        File file = (File) element;
        String name = file.getName();
        if (file.isDirectory()) {
            return name;
        }
        if (IO.isValidVideoFile(name)) {
            return name += IO.VIDEOLINK_EXT;
        }
        if (IO.isJpeg(name)) {
            return name;
        }
        if (file.length() < ThumbIO.getMinimalThumbSize()) {
            return name;
        }
        return new Path(name).removeFileExtension().addFileExtension("jpg").toPortableString();
    }

    @Override
    public boolean isFolder(Object element) {
        return FileSystemStructureProvider.INSTANCE.isFolder(element);
    }

    public static MediaImportProvider getInstance() {
        if (instance == null) {
            instance = new MediaImportProvider();
        }
        return instance;
    }

    public String getImportMode() {
        return importMode;
    }
}
