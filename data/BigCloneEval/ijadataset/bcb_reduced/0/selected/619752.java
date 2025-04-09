package net.sourceforge.agsyslib.api;

import java.lang.reflect.Constructor;
import net.sourceforge.agsyslib.util.AgSysLibException;
import net.sourceforge.agsyslib.util.ExtendedProperties;
import net.sourceforge.agsyslib.util.MultivaluedProperties;
import net.sourceforge.agsyslib.util.PropertyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractInitializer implements Initializer {

    private static final Log log = LogFactory.getLog(AbstractInitializer.class);

    protected final ExtendedProperties<String, String> extProps;

    protected final int propsRepeat;

    protected int currentPropsIteration = -1;

    protected MultivaluedProperties multivalProps = null;

    protected abstract AgentSystem createAgentSystem(int jobIndexInBatch, int jobCountInBatch, SystemConfiguration systemConfiguration) throws AgSysLibException;

    public AbstractInitializer(ExtendedProperties<String, String> extProps) {
        this.extProps = extProps;
        this.propsRepeat = extProps.getInt("repeat", 1);
    }

    @Override
    public AgentSystem getNextAgentSystem() throws AgSysLibException {
        AgentSystem tspSystem = null;
        SystemConfiguration systemConfiguration = getNextSystemConfiguration();
        if (systemConfiguration != null) {
            tspSystem = createAgentSystem(currentPropsIteration, propsRepeat, systemConfiguration);
            log.debug("AgentSystem created.");
        }
        return tspSystem;
    }

    protected void configureMaximumRunParameters() {
        int maximumIterations = (int) Math.round(multivalProps.getDouble("maximum.iterations", -1));
        AbstractSystemEvolution.setMaximumIterations(maximumIterations);
        int maximumRunMillis = (int) (1000.0 * extProps.getDouble("maximum.run.seconds", 0));
        AbstractSystemEvolution.setMaximumRunMillis(maximumRunMillis);
        long maximumCPUNanos = (long) (1000000000.0 * extProps.getDouble("maximum.cpu.seconds", 0));
        AbstractSystemEvolution.setMaximumCPUNanos(maximumCPUNanos);
    }

    protected boolean createNextMultivalProps() throws PropertyException {
        currentPropsIteration = (currentPropsIteration + 1) % propsRepeat;
        if (currentPropsIteration == 0) {
            if (multivalProps == null) {
                if (extProps instanceof MultivaluedProperties) {
                    multivalProps = (MultivaluedProperties) extProps;
                } else {
                    multivalProps = new MultivaluedProperties(extProps.getMap());
                }
            } else {
                if (!multivalProps.setNextParameterValues()) {
                    return false;
                }
            }
        }
        multivalProps.updateParameterValues();
        configureMaximumRunParameters();
        return true;
    }

    protected SystemConfiguration getNextSystemConfiguration() throws AgSysLibException {
        SystemConfiguration systemConfiguration = null;
        boolean hasNext = createNextMultivalProps();
        if (hasNext) {
            systemConfiguration = createAgentSystemConfiguration(multivalProps);
        }
        return systemConfiguration;
    }

    /**
	 * May be overridden by subclasses
	 */
    protected SystemConfiguration createAgentSystemConfiguration(ExtendedProperties<String, String> systemExtProps) throws AgSysLibException {
        SystemConfiguration systemConfiguration;
        String configurationClassName = extProps.getString("system.configuration.class", null);
        if (configurationClassName == null) {
            systemConfiguration = createSystemConfiguration(systemExtProps);
        } else {
            try {
                Class<?> configurationClass = Class.forName(configurationClassName);
                Constructor<?> initializerCtor = configurationClass.getConstructor(AbstractInitializer.class, ExtendedProperties.class);
                systemConfiguration = (SystemConfiguration) initializerCtor.newInstance(this, systemExtProps);
            } catch (Exception e) {
                throw new AgSysLibException("Cannot create tspSystemConfiguration.", e);
            }
        }
        systemConfiguration.initialize();
        return systemConfiguration;
    }

    /**
	 * May be overridden by subclasses
	 */
    @SuppressWarnings("unused")
    protected SystemConfiguration createSystemConfiguration(ExtendedProperties<String, String> systemExtProps) throws AgSysLibException {
        return new BaseSystemConfiguration(systemExtProps);
    }
}
