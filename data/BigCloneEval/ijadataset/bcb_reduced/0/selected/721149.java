package depth.processor;

import java.io.File;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;
import de.tu.depth.fragments.Fragment;
import depth.XmlFileSystemRepository;
import depth.repository.fragment.XfsrFragmentManager;

public abstract class FragmentProcessor {

    protected XmlFileSystemRepository rep;

    protected XfsrFragmentManager fragmentManager;

    protected Fragment fragment;

    public FragmentProcessor(Fragment fragment, XmlFileSystemRepository rep) {
        this.rep = rep;
        this.fragmentManager = rep.getFragmentManager();
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public abstract void createFileRepresentation(String filename);

    public abstract boolean deleteFileRepresentation();

    public IPath getProjectRelativePath() {
        Fragment parent = getFragment().getParent();
        if (parent == null) {
            return new Path(getFragment().getName());
        } else {
            FragmentProcessor fpParent = fragmentManager.getFragmentProcessor(parent);
            return fpParent.getProjectRelativePath().append(getFragment().getName());
        }
    }

    public void register() {
        fragmentManager.registerFragmentProcessor(this);
    }

    public void unregister() {
        fragmentManager.unregisterFragmentProcessor(this);
        fragmentManager.unregisterFragment(fragment);
    }

    public Element createConfigElement(Element parentElement) {
        Element newElement = parentElement.getOwnerDocument().createElement(getFragment().getTypeString());
        newElement.setAttribute("name", fragment.getName());
        newElement.setAttribute("uuid", fragment.getUUID().toString());
        for (Fragment f : getFragment().getChildren()) {
            FragmentProcessor fp = fragmentManager.getFragmentProcessor(f);
            if (fp != null) {
                Element childElement = fp.createConfigElement(newElement);
                newElement.appendChild(childElement);
            }
        }
        return newElement;
    }

    /**
     * Deletes a file or a directory recursively. 
     * 
     * Usually the repository takes care of the order of file deletion.
     * If, for some reason, a file is not deleted properly, the parent folder
     * and all ancestor folders cannot be deleted as well, leaving a partially
     * deleted repository behind. If a FragmentProcessor detects such a case, it
     * can delete the whole subtree using this method.
     * 
     * @param file the file or folder to be deleted
     * @return <code>true</code> if the file or folder could be deleted,
     * otherwise <code>false</code>
     */
    protected boolean recursiveFileDelete(File file) {
        if (file.isDirectory()) {
            for (File fChild : file.listFiles()) {
                recursiveFileDelete(fChild);
            }
        }
        return file.delete();
    }
}
