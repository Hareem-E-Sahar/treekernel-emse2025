package com.ecmdeveloper.plugin.content.util;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import com.ecmdeveloper.plugin.core.model.IObjectStoreItem;

public class ContentCache implements IPartListener2 {

    private static final String CONTENT_CACHE_FOLDER = "content_cache";

    /** 
	 * Filters out the following characters:  / \ [ ] : ; | = , + * ? < > \ " 
	 */
    private static Pattern pattern = Pattern.compile("[/\\\\\\[\\]+*:;|=,><\"]");

    private Set<IPartService> partServicesListeningTo = new HashSet<IPartService>();

    private Map<String, String> cacheFiles = new HashMap<String, String>();

    private IPath parentPath;

    public ContentCache(IPath parentPath) {
        this.parentPath = parentPath;
    }

    public IPath getTempFolderPath(IObjectStoreItem objectStoreItem) {
        IPath cacheLocation = getRootPath().append(getTempFolderName(objectStoreItem));
        if (!cacheLocation.toFile().exists()) {
            cacheLocation.toFile().mkdir();
        }
        return cacheLocation;
    }

    public void clear() {
        for (File file : getRootPath().toFile().listFiles()) {
            deleteDirectory(file);
        }
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public IPath getRootPath() {
        IPath cacheLocation = parentPath.append(CONTENT_CACHE_FOLDER);
        if (!cacheLocation.toFile().exists()) {
            cacheLocation.toFile().mkdir();
        }
        return cacheLocation;
    }

    public void registerFile(String uriString, String filename) {
        cacheFiles.put(uriString, filename);
    }

    private String getTempFolderName(IObjectStoreItem objectStoreItem) {
        StringBuffer path = new StringBuffer();
        path.append(objectStoreItem.getObjectStore().getConnection().getName());
        path.append("_");
        path.append(objectStoreItem.getObjectStore().getName());
        path.append("_");
        path.append(objectStoreItem.getId());
        String pathString = path.toString();
        Matcher matcher = pattern.matcher(pathString);
        return matcher.replaceAll("-");
    }

    public void registerAsListener(IWorkbenchWindow window) {
        partServicesListeningTo.add(window.getPartService());
        window.getPartService().addPartListener(this);
    }

    public void stop() {
        for (IPartService partService : partServicesListeningTo) {
            partService.removePartListener(this);
        }
        partServicesListeningTo.clear();
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        if (partRef instanceof IEditorReference) {
            try {
                deleteEditorInput(partRef);
            } catch (PartInitException e) {
                PluginLog.error(e);
            }
        }
    }

    private void deleteEditorInput(IWorkbenchPartReference partRef) throws PartInitException {
        IEditorInput editorInput = ((IEditorReference) partRef).getEditorInput();
        if (editorInput instanceof FileStoreEditorInput) {
            deleteFileStoreEditorInput(editorInput);
        }
    }

    private void deleteFileStoreEditorInput(IEditorInput editorInput) {
        URI uri = ((FileStoreEditorInput) editorInput).getURI();
        if (cacheFiles.containsKey(uri.toString())) {
            String filename = cacheFiles.get(uri.toString());
            deleteDirectory(new File(filename));
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
    }
}
