package org.mc4j.console;

import org.mc4j.console.connection.ConnectionNode;
import org.mc4j.console.connection.persistence.ConnectionSetDatabase;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.metadata.ConnectionTypeDescriptor;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This type of node holds connection nodes and is the main interface for each
 * server-type to the functionality. This node also acts as the delegation for
 * modules that use custom classloaders to load classes from outside of MC4J.
 *
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), January 2002
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 15:14:16 -0400 (Wed, 12 Apr 2006) $)
 */
public class ManagementNode extends AbstractNode implements Comparable {

    protected ClassLoader delegatedClassLoader;

    protected List classLoaderFileList;

    protected String libraryURI;

    public ManagementNode(String name) {
        super(new Children.SortedArray());
        setIconBase("org/mc4j/console/ManagementNodeIcon");
        setName(name);
        setDisplayName(name);
        setShortDescription(NbBundle.getMessage(ManagementNode.class, "HINT_node"));
    }

    public String getLibraryURI() {
        return this.libraryURI;
    }

    public int compareTo(Object o) {
        Node otherNode = (Node) o;
        return this.getDisplayName().compareTo(otherNode.getDisplayName());
    }

    public Action[] getActions(boolean context) {
        if (context) {
            return null;
        } else {
            return new Action[] { NodeAction.get(ConnectAction.class) };
        }
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void loadConnections(String installType) {
        Set connections = ConnectionSetDatabase.getNodes();
        Iterator iter = connections.iterator();
        while (iter.hasNext()) {
            ConnectionSettings desc = (ConnectionSettings) iter.next();
            ConnectionNode connectionNode = new ConnectionNode(desc);
            this.getChildren().add(new Node[] { (Node) connectionNode });
        }
    }

    /**
     * WARNING: GH - DISGUSTING HACK
     * This is an aweful little hack that allows us to execute under jdk 1.5 (which includes jmx)
     * while utilizing the jmx classes we load from somewhere else. We just override the classloader
     * delegation for cases of the "javax.management" classes.
     */
    public static class MeFirstClassLoader extends URLClassLoader {

        public MeFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c == null) {
                if (name.indexOf("javax.management") >= 0) {
                    try {
                        try {
                            c = findClass(name);
                        } catch (SecurityException se) {
                            int i = name.lastIndexOf('.');
                            String pkgname = name.substring(0, i);
                            Package pkg = getPackage(pkgname);
                            if (pkg == null) {
                                definePackage(pkgname, null, null, null, null, null, null, null);
                            }
                        }
                        if (resolve) {
                            resolveClass(c);
                        }
                    } catch (ClassNotFoundException cnfe) {
                        c = super.loadClass(name, resolve);
                    }
                } else {
                    c = super.loadClass(name, resolve);
                }
            }
            return c;
        }
    }

    public static IConnectionNode buildConnection(ConnectionSettings settings) {
        IConnectionNode node = null;
        String className = settings.getConnectionType().getConnectionNodeClassName();
        try {
            ClassLoader loader = null;
            Class clazz = Class.forName(className, true, loader);
            node = (IConnectionNode) clazz.newInstance();
        } catch (ClassNotFoundException cnfe) {
            ErrorManager.getDefault().notify(cnfe);
        } catch (InstantiationException ie) {
            ErrorManager.getDefault().notify(ie);
        } catch (IllegalAccessException iae) {
            ErrorManager.getDefault().notify(iae);
        }
        node.initialize(settings);
        return node;
    }

    public static File[] getExtras(final ConnectionTypeDescriptor serverType) {
        File mc4jLibDir = new File("mc4jlib");
        File[] commonLibs = mc4jLibDir.listFiles(new FileFilter() {

            public boolean accept(File file) {
                return (!file.isDirectory() && (file.getName().toLowerCase().endsWith(".jar") || file.getName().toLowerCase().endsWith(".zip")));
            }
        });
        File[] tempDirs = mc4jLibDir.listFiles(new FileFilter() {

            public boolean accept(File file) {
                return (file.isDirectory() && file.getName().equals(serverType.getExtrasLibrary()));
            }
        });
        File[] serverLibs = new File[0];
        if (tempDirs.length != 1) {
        } else {
            File serverLibDir = tempDirs[0];
            serverLibs = serverLibDir.listFiles(new FileFilter() {

                public boolean accept(File file) {
                    return (!file.isDirectory() && (file.getName().toLowerCase().endsWith(".jar") || file.getName().toLowerCase().endsWith(".zip")));
                }
            });
        }
        File[] results = new File[commonLibs.length + serverLibs.length + 1];
        System.arraycopy(commonLibs, 0, results, 0, commonLibs.length);
        System.arraycopy(serverLibs, 0, results, commonLibs.length, serverLibs.length);
        results[results.length - 1] = new File("dashboards");
        return results;
    }

    public ClassLoader getDelegatedClassLoader() {
        return delegatedClassLoader;
    }

    public void setDelegatedClassLoader(ClassLoader delegatedClassLoader) {
        this.delegatedClassLoader = delegatedClassLoader;
    }

    public List getClassLoaderFileList() {
        return classLoaderFileList;
    }

    public void setClassLoaderFileList(List classLoaderFileList) {
        this.classLoaderFileList = classLoaderFileList;
    }
}
