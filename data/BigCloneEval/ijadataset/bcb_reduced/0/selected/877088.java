package org.sopera.configuration.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.sopera.configuration.CfgFacade;
import org.sopera.configuration.model.ConfigurationTreeNode;
import org.sopera.exception.AdminFacadeException;
import org.sopware.toolsuite.admintool.common.context.IAdminToolContext;
import org.sopware.toolsuite.admintool.config.delegate.ComponentTO;
import org.sopware.toolsuite.admintool.config.delegate.ComponentTypeEnumTO;
import org.sopware.toolsuite.admintool.config.delegate.ICfgServiceFacade;
import org.sopware.toolsuite.admintool.config.delegate.ScopePathTO;
import org.sopware.toolsuite.admintool.config.delegate.ScopeTO;
import org.sopware.toolsuite.admintool.exceptions.AdminToolException;
import org.sopware.toolsuite.admintool.exceptions.ErrorCodeEnum;

/**
 * Default implementation of the {@link CfgFacade}
 *
 * @author zubairov
 *
 */
public class ConfigurationFacadeImpl implements CfgFacade {

    private static final String ALL_CONFIGURATION_FILENAME = "allConfiguration.xml";

    private static final String TREE_NAME = "SOP";

    private IAdminToolContext delegate;

    public ConfigurationFacadeImpl(IAdminToolContext ctx) {
        this.delegate = ctx;
    }

    /**
	 * {@inheritDoc}
	 */
    public ConfigurationTreeNode getConfigurationTree() throws AdminFacadeException {
        ConfigurationTreeNodeImpl result = null;
        try {
            ICfgServiceFacade facade = delegate.getCfgServiceFacade();
            ScopePathTO root = ScopePathTO.valueOf("%");
            result = new ConfigurationTreeNodeImpl(root);
            buildTree(result, facade);
        } catch (AdminToolException e) {
            throwWrappedException("Could not get configuration tree", e);
        }
        return result;
    }

    /**
	 * Recursive function that populates the tree
	 *
	 * @param result
	 * @param facade
	 * @throws AdminToolException
	 */
    @SuppressWarnings("unchecked")
    private void buildTree(ConfigurationTreeNodeImpl node, ICfgServiceFacade facade) throws AdminToolException {
        Collection<ScopeTO> children = facade.getScopeChilds(TREE_NAME, node.getScopePath());
        for (ScopeTO scope : children) {
            ScopePathTO scopePath = scope.getScopePath();
            ConfigurationTreeNodeImpl child = new ConfigurationTreeNodeImpl(scopePath);
            node.addChild(child);
            buildTree(child, facade);
        }
    }

    /**
	 * {@inheritDoc}
	 */
    public void release() {
        delegate.release();
    }

    /**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public File export(ScopePathTO path) throws AdminFacadeException {
        File result = null;
        ZipOutputStream out = null;
        try {
            ICfgServiceFacade facade = delegate.getCfgServiceFacade();
            String allConfigurations = facade.getAllConfigurations(TREE_NAME, path);
            result = File.createTempFile("ConfigurationExport_", ".zip");
            out = new ZipOutputStream(new FileOutputStream(result));
            out.putNextEntry(new ZipEntry(ALL_CONFIGURATION_FILENAME));
            out.write(allConfigurations.getBytes());
            out.closeEntry();
            Collection<ComponentTO> components = facade.getComponents(TREE_NAME, path);
            for (ComponentTO component : components) {
                ComponentTypeEnumTO type = component.getType();
                if (type == ComponentTypeEnumTO.RESOURCE) {
                    Collection<String> resourceIds = facade.getResourceIds(TREE_NAME, path, component.getName());
                    for (String resourceID : resourceIds) {
                        byte[] bytes = facade.getResource(TREE_NAME, path, component.getName(), resourceID);
                        out.putNextEntry(new ZipEntry("resource_ " + component.getName() + "_" + resourceID + ".bin"));
                        out.write(bytes);
                        out.closeEntry();
                    }
                }
            }
            out.close();
        } catch (AdminToolException e) {
            throwWrappedException("Could not export configuration", e);
        } catch (IOException e) {
            throwWrappedException("Could not export configuration", new AdminToolException(ErrorCodeEnum.FILE_OUTPUT, null, e));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
        return result;
    }

    /**
	 * {@inheritDoc}
	 */
    public void importConfig(ScopePathTO path, File exported) throws AdminFacadeException {
        try {
            ZipFile zip = new ZipFile(exported);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ICfgServiceFacade facade = delegate.getCfgServiceFacade();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (ALL_CONFIGURATION_FILENAME.equals(entry.getName())) {
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                            builder.append(System.getProperty("line.separator"));
                        }
                        reader.close();
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                    String allConfigValue = builder.toString();
                    facade.setAllConfigurations(TREE_NAME, path, allConfigValue);
                }
            }
        } catch (AdminToolException e) {
            throwWrappedException("Could not export configuration", e);
        } catch (IOException e) {
            throwWrappedException("Could not export configuration", new AdminToolException(ErrorCodeEnum.FILE_LOAD, null, e));
        }
    }

    /**
	 * {@inheritDoc}
	 */
    public boolean verify(File file) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            boolean found = false;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (ALL_CONFIGURATION_FILENAME.equals(entry.getName())) {
                    found = true;
                    break;
                }
            }
            zip.close();
            return found;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void throwWrappedException(String message, Exception e) throws AdminFacadeException {
        StackTraceElement[] ste = e.getStackTrace();
        String steString = "";
        for (StackTraceElement element : ste) {
            steString += element + "\n";
        }
        throw new AdminFacadeException(message + e.getMessage() + " " + e.getClass().getName() + " " + steString, e);
    }
}
