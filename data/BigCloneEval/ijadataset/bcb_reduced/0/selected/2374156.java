package com.acarter.scenemonitor;

import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.acarter.jmejtree.JMEComposableTree;
import com.acarter.scenemonitor.dialog.MonitorDialog;
import com.acarter.scenemonitor.information.A_MonitorInformationPanel;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;

/**
 * This exposes all the functionality to the user.
 * 
 * @author Andrew Carter
 */
public class SceneMonitor {

    /** Logger name */
    public static final String LOGGER_NAME = "com.acarter.scenemonitor.SceneMonitor";

    /** Logger reference */
    private static final Logger LOGGER = Logger.getLogger(SceneMonitor.LOGGER_NAME);

    /** The one and only instance if this class */
    private static SceneMonitor instance = null;

    /** Class name of the monitor dialog to instantiate */
    public static String monitorDialogClassName = null;

    /** The dialog displayed to the user */
    private MonitorDialog dialog = null;

    /**
	 * Private constructor.
	 * 
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
    private SceneMonitor() {
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception e) {
                    LOGGER.warning("Could not set cross platform look and feel.");
                    e.printStackTrace();
                }
                if (monitorDialogClassName == null) {
                    dialog = new MonitorDialog(null);
                } else {
                    try {
                        Class<?> dialogClass = Class.forName(monitorDialogClassName);
                        Constructor<?> dialogCtor = dialogClass.getConstructor(Frame.class);
                        Frame temp = null;
                        dialog = (MonitorDialog) dialogCtor.newInstance(temp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Returns the singleton instance.
	 * 
	 * @return The instance
	 */
    public static SceneMonitor getMonitor() {
        if (instance == null) {
            instance = new SceneMonitor();
        }
        return instance;
    }

    /**
	 * Set the visibility of the dialog.
	 * 
	 * @param show
	 *            True to show it, False to hide it.
	 */
    public void showViewer(final boolean show) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.setVisible(show);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Returns whether the dialog is visible or not.
	 * 
	 * @return boolean True if visible, false otherwise.
	 */
    public boolean isVisible() {
        return dialog.isVisible();
    }

    /**
	 * Called by the host application to refresh the tree view in the dialog.
	 * 
	 * @param tpf
	 *            Time per frame
	 */
    public void updateViewer(final float tpf) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.doUpdate(tpf);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Called by the host application to render from the dialog.
	 * 
	 * @param tpf
	 *            Time per frame
	 */
    public void renderViewer(final Renderer renderer) {
        dialog.render(renderer);
    }

    /**
	 * Defines the amount of time between tree updates.
	 * 
	 * @param seconds
	 */
    public void setViewerUpdateInterval(final float seconds) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.setUpdateInterval(seconds);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Adds a node to the list of nodes the user may select using the node name.
	 * 
	 * @param node
	 */
    public void registerNode(final Node node) {
        registerNode(node, node.getName());
    }

    /**
	 * Adds a node to the list of nodes the user may select.
	 * 
	 * @param node
	 * @param displayName
	 *            The name to use in the list
	 */
    public void registerNode(final Object node, final String displayName) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.registerNode(node, displayName);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Removes a node from the list of nodes the user may select.
	 * 
	 * @param node
	 */
    public void unregisterNode(final Object node) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.unregisterNode(node);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Removes all nodes from the list of nodes the user may select.
	 */
    public void unregisterAllNodes() {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.unregisterAllNodes();
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Selects the given object in the tree.
	 * 
	 * @param object
	 *            The object to select.
	 */
    public void selectSceneObject(final Object object) {
        Runnable runnable = new Runnable() {

            public void run() {
                dialog.selectObject(object);
            }
        };
        executeSwingRunnable(runnable);
    }

    /**
	 * Returns the tree instance used by the Monitor dialog.
	 * 
	 * @return The tree instance
	 */
    public JMEComposableTree getMonitorTree() {
        return dialog.getTree();
    }

    /**
	 * Allows the addition of custom descriptor definitions.
	 * 
	 * @param classType
	 * @param descriptor
	 */
    public A_MonitorInformationPanel getMonitorInformationPanel() {
        return dialog.getInformationPanel();
    }

    /**
	 * Returns the Scene Monitor dialog.
	 * 
	 * @return The dialog
	 */
    public JDialog getMonitorDialog() {
        return dialog;
    }

    /**
	 * Executes a runnable in the Swing Event Dispatch thread. Checks if we are
	 * already in the EDT.
	 * 
	 * @param runnable
	 */
    private void executeSwingRunnable(Runnable runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else runnable.run();
    }

    /**
	 * Performs necessary actions on shutdown of the application.
	 */
    public void cleanup() {
        unregisterAllNodes();
        dialog.dispose();
    }
}
