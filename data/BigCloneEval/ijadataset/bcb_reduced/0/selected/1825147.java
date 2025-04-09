package com.google.code.gronono.gps.ui.panels.pages.tree;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import com.google.code.gronono.commons.i18n.BundleKey;
import com.google.code.gronono.commons.i18n.BundleName;
import com.google.code.gronono.commons.swing.SwingUtils;
import com.google.code.gronono.commons.swing.listeners.DoubleClickListener;
import com.google.code.gronono.gps.model.SortedFile;

/**
 * Gestionnaire de clic de souris pour les arbres.
 */
@BundleName(value = "com.google.code.gronono.gps.gui")
public class TreeMouseListener extends DoubleClickListener {

    /** Logger. */
    private static final Logger logger = Logger.getLogger(TreeMouseListener.class);

    /** Message d'erreur pour le cas d'un fichier inexistant. */
    @BundleKey(value = "result.page.double.click.file.not.found.err.msg")
    private static String FILE_NOT_FOUND_ERR_MSG;

    /** Message d'erreur pour le cas d'un fichier inexistant. */
    @BundleKey(value = "result.page.double.click.unsupported.api.err.msg")
    private static String UNSUPPORTED_API_ERR_MSG;

    /** Flag indiquant si l'arbre concerné est l'arbre source. */
    private boolean isSrcTree = false;

    /**
	 * Constructeur.
	 * @param isSrcTree Flag indiquant si l'arbre concerné est l'arbre source.
	 */
    public TreeMouseListener(final boolean isSrcTree) {
        this.isSrcTree = isSrcTree;
    }

    @Override
    protected void mouseDoubleClicked(final MouseEvent e) {
        if (logger.isTraceEnabled()) logger.trace("Double click");
        final JTree tree = (JTree) e.getSource();
        final TreeSelectionModel model = tree.getSelectionModel();
        for (final TreePath path : model.getSelectionPaths()) {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            final SortedFile sortedFile = (SortedFile) selectedNode.getUserObject();
            final File srcFile = sortedFile.getSrcFile();
            final File dstFile = sortedFile.getDstFile();
            if (Desktop.isDesktopSupported()) {
                try {
                    if (isSrcTree) {
                        if (srcFile.exists()) Desktop.getDesktop().open(srcFile); else logger.warn(MessageFormat.format(FILE_NOT_FOUND_ERR_MSG, srcFile.getAbsolutePath()));
                    } else {
                        if (dstFile.exists()) Desktop.getDesktop().open(dstFile); else logger.warn(MessageFormat.format(FILE_NOT_FOUND_ERR_MSG, dstFile.getAbsolutePath()));
                    }
                } catch (final Exception ex) {
                    logger.error("Exception : " + ex.getMessage() + (ex.getCause() != null ? ", cause : " + ex.getCause() : ""), ex);
                }
            } else logger.warn(MessageFormat.format(UNSUPPORTED_API_ERR_MSG, SystemUtils.OS_NAME));
        }
        if (tree.isCollapsed(0)) SwingUtils.expandAll(tree);
    }
}
