package net.sourceforge.ondex.workflow2_old;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.AbstractONDEXExport;
import net.sourceforge.ondex.filter.AbstractONDEXFilter;
import net.sourceforge.ondex.mapping.AbstractONDEXMapping;
import net.sourceforge.ondex.parser.AbstractONDEXParser;
import net.sourceforge.ondex.statistics.AbstractONDEXStatistics;
import net.sourceforge.ondex.transformer.AbstractONDEXTransformer;
import net.sourceforge.ondex.workflow.Parameters.AbstractONDEXPluginInit;
import net.sourceforge.ondex.workflow2_old.support.AbstractWorkerElement;
import net.sourceforge.ondex.workflow2_old.support.ResourcePool;
import net.sourceforge.ondex.AbstractONDEXPlugin;

/**
 * 
 * @author lysenkoa
 *
 */
public class PluginWorkerElement extends AbstractWorkerElement {

    private Class<AbstractONDEXPlugin<?>> pluginClass;

    @SuppressWarnings("unchecked")
    public PluginWorkerElement(Class pluginClass, String uniqueTypeId) {
        this.uniqueTypeId = uniqueTypeId;
        this.pluginClass = pluginClass;
    }

    @Override
    public UUID[] execute(ResourcePool rp) throws Exception {
        UUID[] result = super.execute(rp);
        Object[] in = getResources(rp);
        AbstractONDEXPluginInit plugin;
        AbstractONDEXPlugin<?> p = pluginClass.getConstructor(new Class<?>[] {}).newInstance(new Object[] {});
        if (in[0] != null) plugin = (AbstractONDEXPluginInit) in[0]; else plugin = new AbstractONDEXPluginInit();
        plugin.setPlugin(p);
        AbstractONDEXGraph[] graphs = new AbstractONDEXGraph[2];
        for (int i = 1; i < in.length; i++) graphs[i - 1] = (AbstractONDEXGraph) in[i];
        ONDEXGraph output = getPlugin(pluginClass).run(plugin, graphs);
        for (UUID address : result) {
            rp.addResource(address, output);
        }
        return result;
    }

    interface SimplePlugin {

        public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception;
    }

    public static SimplePlugin getPlugin(Class<?> cls) throws Exception {
        if (AbstractONDEXExport.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    WrappedEngine.getEngine().runExport(plugin, graphs[0]);
                    return null;
                }
            };
        } else if (AbstractONDEXFilter.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    return WrappedEngine.getEngine().runFilter(plugin, graphs[0], graphs[1]);
                }
            };
        } else if (AbstractONDEXMapping.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    return WrappedEngine.getEngine().runMapping(plugin, graphs[0]);
                }
            };
        } else if (AbstractONDEXParser.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    return WrappedEngine.getEngine().runParser(plugin, graphs[0]);
                }
            };
        } else if (AbstractONDEXStatistics.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    WrappedEngine.getEngine().runStatistics(plugin, graphs[0]);
                    return null;
                }
            };
        } else if (AbstractONDEXTransformer.class.isAssignableFrom(cls)) {
            return new SimplePlugin() {

                public ONDEXGraph run(AbstractONDEXPluginInit plugin, AbstractONDEXGraph[] graphs) throws Exception {
                    return WrappedEngine.getEngine().runTransformer(plugin, graphs[0]);
                }
            };
        }
        throw new Exception("Unknown plugin type");
    }

    public Map<String, String> getDefinition() {
        Map<String, String> out = new HashMap<String, String>();
        out.put("class", pluginClass.getCanonicalName());
        return out;
    }

    public String getType() {
        return "plugin";
    }
}
