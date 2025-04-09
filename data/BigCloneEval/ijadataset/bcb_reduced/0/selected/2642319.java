package org.datanucleus.plugin;

import org.datanucleus.ClassConstants;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.Localiser;

/**
 * Factory for PluginRegistry. 
 * Creates an instance of PluginRegistry based on the available PluginRegistry implementation in the classpath.
 */
public class PluginRegistryFactory {

    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation", ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /**
     * Instantiates a PluginRegistry.
     * If DN "core" is deployed as an eclipse plugin, uses the Eclipse OSGI Registry to find other DN plug-ins
     * @param registryClassName Name of the registry
     * @param registryBundleCheck What to do on check of bundles (Only for Non-Eclipse)
     * @param allowUserBundles Whether to only load DataNucleus bundles (org.datanucleus) (Only for Non-Eclipse)
     * @param clr the ClassLoaderResolver
     * @return instance of the PluginRegistry
     */
    public static PluginRegistry newPluginRegistry(String registryClassName, String registryBundleCheck, boolean allowUserBundles, ClassLoaderResolver clr) {
        PluginRegistry registry = null;
        if (registryClassName != null) {
            registry = newInstance(registryClassName, registryClassName, clr);
            if (registry != null) {
                if (NucleusLogger.PLUGIN.isDebugEnabled()) {
                    NucleusLogger.PLUGIN.debug("Using PluginRegistry " + registry.getClass().getName());
                }
                return registry;
            }
        }
        registry = newInstance("org.eclipse.core.runtime.RegistryFactory", "org.datanucleus.plugin.EclipsePluginRegistry", clr);
        if (registry != null) {
            if (NucleusLogger.PLUGIN.isDebugEnabled()) {
                NucleusLogger.PLUGIN.debug("Using PluginRegistry " + registry.getClass().getName());
            }
            return registry;
        }
        if (NucleusLogger.PLUGIN.isDebugEnabled()) {
            NucleusLogger.PLUGIN.debug("Using PluginRegistry " + NonManagedPluginRegistry.class.getName());
        }
        return new NonManagedPluginRegistry(clr, registryBundleCheck, allowUserBundles);
    }

    /**
     * Instantiates a PluginRegistry. Only proceed if the testClass is found in the classpath
     * @param testClass A test class
     * @param registryClassName Name of the class that implements {@link PluginRegistry}
     * @return instance of the PluginRegistry
     */
    private static PluginRegistry newInstance(String testClass, String registryClassName, ClassLoaderResolver clr) {
        try {
            if (clr.classForName(testClass, org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER) == null) {
                if (NucleusLogger.PLUGIN.isDebugEnabled()) {
                    NucleusLogger.PLUGIN.debug(LOCALISER.msg("024005", registryClassName));
                }
            }
            return (PluginRegistry) clr.classForName(registryClassName, org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER).getConstructor(new Class[] { ClassConstants.CLASS_LOADER_RESOLVER }).newInstance(new Object[] { clr });
        } catch (Exception e) {
            if (NucleusLogger.PLUGIN.isDebugEnabled()) {
                NucleusLogger.PLUGIN.debug(LOCALISER.msg("024006", registryClassName, e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            }
        }
        return null;
    }
}
