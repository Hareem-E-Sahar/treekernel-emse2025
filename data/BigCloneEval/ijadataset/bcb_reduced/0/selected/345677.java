package de.psisystems.dmachinery.assembler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.psisystems.dmachinery.core.Factory;
import de.psisystems.dmachinery.core.exeptions.PrintException;
import de.psisystems.dmachinery.core.types.OutputFormat;
import de.psisystems.dmachinery.outputchannel.OutputChannel;

public class PrintAssemblerFactory extends Factory {

    private static final Log log = LogFactory.getLog(PrintAssemblerFactory.class);

    static final PrintAssemblerFactory instance = new PrintAssemblerFactory();

    private Map<OutputFormat, Set<Class>> assemblers = new HashMap<OutputFormat, Set<Class>>();

    public void register(Set<OutputFormat> outputFormats, Class assembler) {
        if (outputFormats == null || outputFormats.size() == 0) {
            throw new IllegalArgumentException("At least one OutputFormat must be defined");
        }
        if (assembler == null) {
            throw new IllegalArgumentException("engine 1= null");
        }
        for (OutputFormat outputFormat : outputFormats) {
            Set<Class> assemblerSet = assemblers.get(outputFormat);
            if (assemblerSet == null) {
                assemblerSet = new HashSet<Class>();
                assemblers.put(outputFormat, assemblerSet);
            }
            if (!assemblerSet.contains(assembler)) {
                assemblerSet.add(assembler);
            } else {
                log.warn("PrintAssembler already registered " + assembler.getName());
            }
        }
    }

    public void unregister(PrintAssembler assembler) {
        if (assembler == null) {
            throw new IllegalArgumentException("assembler != null");
        }
        for (OutputFormat outputFormat : assembler.getOutputFormats()) {
            Set<Class> assemblerSet = assemblers.get(outputFormat);
            if (assemblerSet == null) {
                log.warn("assembler not registerd " + assembler.getName());
                continue;
            }
            assemblerSet.remove(assembler);
            log.info("assembler sucessfully unregisterd " + assembler.getName());
        }
    }

    public PrintAssembler getAssembler(OutputFormat outputFormat, OutputChannel outputChannel, Map<String, Object> attributes) throws PrintException {
        registerClasses();
        Set<Class> assemblerSet = assemblers.get(outputFormat);
        if (assemblerSet == null || assemblerSet.size() == 0) {
            log.warn("no assembler registered for OutputFormat " + outputFormat);
            throw new PrintException("no assembler registered for OutputFormat " + outputFormat);
        } else {
            Class printAssembler = (Class) assemblerSet.iterator().next();
            log.debug("PrinAssembler found for OutputFormat " + outputFormat + " " + printAssembler.getName());
            return create(printAssembler, outputChannel, attributes);
        }
    }

    private PrintAssembler create(Class assembler, OutputChannel outputChannel, Map<String, Object> attributes) throws PrintException {
        Constructor constructor;
        try {
            constructor = assembler.getConstructor(OutputChannel.class, Map.class);
        } catch (SecurityException e) {
            throw new PrintException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new PrintException(e.getMessage(), e);
        }
        try {
            return (PrintAssembler) constructor.newInstance(outputChannel, attributes);
        } catch (IllegalArgumentException e) {
            throw new PrintException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new PrintException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new PrintException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new PrintException(e.getMessage(), e);
        }
    }

    public static PrintAssemblerFactory getInstance() {
        return instance;
    }
}
