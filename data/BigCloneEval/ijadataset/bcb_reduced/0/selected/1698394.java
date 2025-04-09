package org.freeworld.tiler.builder.common.controls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.freeworld.jmultiplug.intl.IntlString;
import org.freeworld.tiler.builder.common.controls.nativefs.FileExplorer;
import org.freeworld.tiler.builder.ui.ComponentBundle;
import org.freeworld.tiler.engine.base.Resource;
import org.freeworld.tiler.engine.resources.loaders.ResourceStoreLoader;

public class ResourceStoreLoaderUI extends JPanel implements ComponentBundle {

    private static final long serialVersionUID = 6358328672238334892L;

    private static final IntlString INTL_LOADER_NAME = new IntlString(ResourceStoreLoaderUI.class, "INTL_LOADER_NAME", "Load");

    private static final IntlString INTL_LOADER_DESC = new IntlString(ResourceStoreLoaderUI.class, "INTL_LOADER_DESC", "Loads a resource store for editing");

    private FileExplorer tree = null;

    private JButton loadButton = null;

    public ResourceStoreLoaderUI() {
        setLayout(new GridBagLayout());
        add(getFileTree(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        add(getLoadButton(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    }

    protected synchronized FileExplorer getFileTree() {
        if (tree == null) {
            tree = new FileExplorer();
            tree.setSplitSelector(false);
            tree.setAddressBarHidden(true);
            tree.setStatusBarHidden(true);
            tree.setMinimumSize(new Dimension(1, 50));
        }
        return tree;
    }

    protected synchronized Component getLoadButton() {
        if (loadButton == null) {
            loadButton = new JButton();
            loadButton.setText("Load");
            loadButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    InputStream is = null;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Object o = getFileTree().getSelectedFile();
                    if (!(o instanceof File)) {
                        System.out.println("No file selected");
                        return;
                    }
                    File f = (File) o;
                    try {
                        is = new FileInputStream(f);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    byte[] tmp = new byte[1024];
                    while (true) {
                        int count = 0;
                        try {
                            count = is.read(tmp);
                            if (count > 0) bos.write(tmp, 0, count);
                            if (count < 1024) break;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            return;
                        }
                    }
                    byte[] allBytes = bos.toByteArray();
                    ResourceStoreLoader rsl = new ResourceStoreLoader();
                    Resource r = rsl.load(allBytes);
                }
            });
        }
        return loadButton;
    }

    public Component getBundleComponent() {
        return this;
    }

    public IntlString getBundleDescription() {
        return INTL_LOADER_DESC;
    }

    public Icon getBundleIcon() {
        return null;
    }

    public IntlString getBundleName() {
        return INTL_LOADER_NAME;
    }
}
