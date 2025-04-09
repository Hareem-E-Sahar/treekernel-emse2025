package org.antdepo.common;

import org.antdepo.AntdepoException;
import org.antdepo.types.Opt;
import org.antdepo.utils.ConfigWriter;
import org.antdepo.utils.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a command handler within a module.
 * <p/>
 * ControlTier Software Inc.
 * User: alexh
 * Date: Jun 4, 2004
 * Time: 8:54:57 PM
 */
public class CmdHandler extends FrameworkResource {

    public static final Logger log = Logger.getLogger(CmdHandler.class);

    /**
     * "ant" handler type
     */
    public static final String ANT_TYPE = "ant";

    /**
     * "shell" handler type
     */
    public static final String SHELL_TYPE = "shell";

    /**
     * "bat" handler type
     */
    public static final String BAT_TYPE = "bat";

    /**
     * "daemon" handler type
     */
    public static final String DAEMON_TYPE = "daemon";

    /**
     * "workflow" handler type
     */
    public static final String WORKFLOW_TYPE = "workflow";

    /**
     * All handler types
     */
    public static final String[] TYPES = new String[] { ANT_TYPE, SHELL_TYPE, BAT_TYPE, DAEMON_TYPE, WORKFLOW_TYPE };

    private CmdModule module;

    private final File buildFile;

    /**
     * primary constructor
     *
     * @param name    Name of the handler. e.g., Start.xml
     * @param baseDir Directory where commands reside
     * @param module  Name of the module
     */
    public CmdHandler(final String name, final File baseDir, final CmdModule module) {
        super(name, baseDir, module);
        if (module == null) {
            throw new IllegalArgumentException("module was null");
        }
        this.module = module;
        initialize();
        buildFile = new File(baseDir, name + ".xml");
        if (!buildFile.exists()) {
            throw new IllegalArgumentException("handler file does not exist: " + buildFile);
        }
    }

    /**
     * Returns name of handler minus the ".xml" suffix.
     *
     * @return
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns the CmdModule object in which this handler is contained.
     *
     * @return
     */
    public CmdModule getModule() {
        return module;
    }

    /**
     * Get the handler type string
     */
    public String getType() {
        return commandRep.type;
    }

    /**
     * the internal type-specific object to manage properties
     */
    private CommandRep commandRep;

    /**
     * initialize attributes based on properties
     */
    public void initialize() {
        commandRep = createCommandRep();
    }

    /**
     * Creates a CommandRep of the appropriate type for the handler.
     */
    private CommandRep createCommandRep() {
        final String type;
        Properties props = getModule().getCommandsProperties();
        if (props.containsKey("command." + getName() + ".command-type")) {
            type = props.getProperty("command." + getName() + ".command-type");
        } else {
            type = ANT_TYPE;
        }
        return createCommandRep(type);
    }

    /**
     * Map from handler type to representative class
     */
    private static HashMap commandRepMap = new HashMap();

    static {
        commandRepMap.put(ANT_TYPE, CommandRep.class);
        commandRepMap.put(SHELL_TYPE, ShellCommandRep.class);
        commandRepMap.put(DAEMON_TYPE, DaemonCommandRep.class);
        commandRepMap.put(BAT_TYPE, BatCommandRep.class);
        commandRepMap.put(WORKFLOW_TYPE, WorkflowCommandRep.class);
    }

    /**
     * Creates a CommandRep of the given type for this CmdHandler.
     */
    private CommandRep createCommandRep(final String type) {
        return createCommandRep(this, type);
    }

    /**
     * Factory method to create a CommandRep for the given CmdHandler and handler type
     *
     * @param hdlr
     * @param type
     * @return
     */
    private static CommandRep createCommandRep(final CmdHandler hdlr, final String type) {
        Properties props = new Properties();
        props.putAll(hdlr.getModule().getCommandsProperties());
        return createCommandRepForType(hdlr.getName(), props, type);
    }

    /**
     * Factory method to create a CommandRep given a name, properties and a type
     *
     * @param name
     * @param props
     * @param type
     * @return
     */
    private static CommandRep createCommandRepForType(final String name, final Properties props, final String type) {
        if (null == commandRepMap.get(type)) {
            throw new IllegalArgumentException("can't create command for unrecognized type: " + type);
        }
        Class repcls = (Class) commandRepMap.get(type);
        try {
            Constructor c = repcls.getConstructor(new Class[] { String.class, Properties.class });
            Object o = c.newInstance(new Object[] { name, props });
            if (o instanceof CommandRep) {
                return (CommandRep) o;
            } else {
                throw new AntdepoException("Error Creating CommandRep: not a CommandRep class: " + o.getClass().getName());
            }
        } catch (NoSuchMethodException e) {
            throw new AntdepoException("Error Creating CommandRep:" + e, e);
        } catch (InstantiationException e) {
            throw new AntdepoException("Error Creating CommandRep:" + e, e);
        } catch (IllegalAccessException e) {
            throw new AntdepoException("Error Creating CommandRep:" + e, e);
        } catch (InvocationTargetException e) {
            throw new AntdepoException("Error Creating CommandRep:" + e, e);
        }
    }

    /**
     * Returns all the properties associated with this command handler
     * Note: read from the commands.properties
     *
     * @return
     */
    public Properties getProperties() {
        return getProperties(".*", false);
    }

    /**
     * Returns properties associated with this command handler
     *
     * @param regex       an extra filtering pattern and is appended to the prefix pattern
     * @param stripprefix if set true, the original prefix is used in the prop key
     * @return matching properties
     */
    public Properties getProperties(final String regex, final boolean stripprefix) {
        final String prefixPattern = "^command\\." + getName() + "\\.";
        Pattern pattern = Pattern.compile(prefixPattern + "(" + regex + ")");
        final Properties p = new Properties();
        for (Iterator iter = module.getCommandsProperties().keySet().iterator(); iter.hasNext(); ) {
            final String key = (String) iter.next();
            Matcher matcher = pattern.matcher(key);
            if (!matcher.find()) {
                continue;
            }
            if (stripprefix) {
                final String newkey = matcher.group(1);
                p.put(newkey, module.getCommandsProperties().getProperty(key));
            } else {
                p.put(key, module.getCommandsProperties().getProperty(key));
            }
        }
        return p;
    }

    public File getPropertyFile() {
        return module.getPropertyFile();
    }

    /**
     * Returns the Ant build file for the handler.
     *
     * @return
     */
    public File getBuildFile() {
        return buildFile;
    }

    /**
     * Get the CommandRep representing this handler
     *
     * @return
     */
    public CommandRep getCommandRep() {
        return commandRep;
    }

    /**
     * Set the property with the given attribute name for this command handler.
     * The command properties for the module are rewritten after setting this value.
     *
     * @param name  name of the attribute
     * @param value value of the attribute
     * @throws IOException
     */
    public void setAttribute(final String name, final String value) throws IOException {
        commandRep.setProperty(name, value);
        if ("command-type".equals(name)) {
            Properties props = new Properties();
            props.putAll(commandRep.getProperties());
            commandRep = createCommandRepForType(getName(), props, value);
        }
        getModule().updateCommandsProperties(commandRep.getProperties());
    }

    /**
     * Get the property value for the given attribute name.
     *
     * @param name name of the attribute, e.g. "name", "doc", "controller"
     * @return
     */
    public String getAttribute(final String name) {
        return commandRep.getProperty(name);
    }

    /**
     * Generates the command handler file, overwriting any existing one if specified.
     *
     * @param cmd       The CmdHandler to generate
     * @param overwrite if true, overwrite an existing handler
     * @param templates resolver to determine the appropriate template to use
     * @throws IOException
     */
    public static void generateHandler(final CmdHandler cmd, final boolean overwrite, final TemplateResolver templates) throws IOException {
        cmd.commandRep.generateHandler(cmd.getModule(), overwrite, templates);
    }

    /**
     * Deletes the command handler file for the given CmdHandler.  Also deletes the ".template"
     * file for the handler if it is in the Module.
     *
     * @param cmd
     */
    public static void deleteHandlerFile(final CmdHandler cmd) {
        File hdlr = cmd.getBuildFile();
        if (hdlr.exists()) {
            hdlr.delete();
        }
        File templ = new File(hdlr.getParentFile(), cmd.getName() + ".template");
        if (templ.exists()) {
            templ.delete();
        }
    }

    /**
     * Creates the required properties for the command, and a handler, then creates and returns a new CmdHandler.
     *
     * @param module
     * @param name
     * @param type
     * @return
     */
    public static CmdHandler createCmdHandler(final CmdModule module, final String name, final String type, final Properties props, final TemplateResolver templates) throws IOException {
        props.setProperty("command." + name + ".command-type", type);
        props.setProperty("command." + name + ".controller", module.getName());
        final CommandRep rep = createCommandRepForType(name, props, type);
        rep.generateHandler(module, true, templates);
        props.putAll(rep.getProperties());
        module.updateCommandsProperties(props);
        final CmdHandler hdlr = module.createCmdHandler(name);
        return hdlr;
    }

    /**
     * Returns the {@link Opt} values for this command as defined in the commands.properties data
     * @return a Collection of {@link Opt} value representing each command option
     */
    public Collection getOpts() {
        final Map m = getOptsMap();
        return m.values();
    }

    public Map getOptsMap() {
        final Map m = new HashMap();
        final Properties p = getProperties("opts\\..*", true);
        for (Iterator iter = p.keySet().iterator(); iter.hasNext(); ) {
            final String key = (String) iter.next();
            final String arr[] = key.split("\\.");
            if (arr.length != 3) {
                continue;
            }
            final String optName = arr[1];
            final String optAttr = arr[2];
            final Opt opt;
            if (m.containsKey(optName)) {
                opt = (Opt) m.get(optName);
            } else {
                opt = new Opt();
                opt.setParameter(optName);
                m.put(optName, opt);
            }
            if (optAttr.equals("defaultValue")) {
                opt.setDefault(p.getProperty(key));
            } else if (optAttr.equals("description")) {
                opt.setDescription(p.getProperty(key));
            } else if (optAttr.equals("arguments")) {
                opt.setType(p.getProperty(key).equalsIgnoreCase("true") ? "string" : "boolean");
            } else if (optAttr.equals("required")) {
                opt.setRequired(p.getProperty(key).equalsIgnoreCase("true"));
            }
        }
        return m;
    }

    /**
     * Gets the description field for the command from the properties
     * @return value of description field
     */
    public String getDescription() {
        final String descKey = "command." + getName() + ".doc";
        return (getProperties().containsKey(descKey)) ? getProperties().getProperty(descKey) : "";
    }

    /**
     * Tests if a handler file exists for this command
     * @param basedir commands base directory
     * @param cmdName   command name
     * @return true if a handler file exists
     */
    public static boolean existsCmdHandler(final File basedir, final String cmdName) {
        return new File(basedir, cmdName + ".xml").exists();
    }

    /**
     * Interface for providing a set of templates for command handlers.
     */
    public static interface TemplateResolver {

        /**
         * Return the correct template File for the given handler type.
         *
         * @param type
         * @return
         * @throws FileNotFoundException
         */
        public File getTemplate(final String type) throws FileNotFoundException;
    }

    /**
     * This class contains the properties to represent a specific type of command handler.
     * This base class represents the basic "ant" handler, and subclasses represent
     * the other classes.
     * <p/>
     * The Factory method {@link CmdHandler#createCommandRepForType(String,java.util.Properties,String)
     * createCommandRepForType} should be used to create an instance if needed and if the specific subclass
     * is not known beforehand.
     */
    public static class CommandRep {

        String name = "";

        String type = ANT_TYPE;

        String controller = "";

        String doc = "";

        String privateAccess = "false";

        Properties props;

        public CommandRep(final String name, final Properties props) {
            this.name = name;
            this.props = props;
            parseProperties();
        }

        void parseProperties() {
            if (hasProperty("command-type")) {
                setType(getProperty("command-type"));
            }
            if (hasProperty("controller")) {
                controller = getProperty("controller");
            }
            if (hasProperty("doc")) {
                doc = getProperty("doc");
            }
            if (hasProperty("private-access")) {
                privateAccess = getProperty("private-access");
            }
        }

        /**
         * Gets the property of the command for the given attribute name.
         *
         * @param attrib
         * @return
         */
        public String getProperty(final String attrib) {
            return props.getProperty(getPropertyKey(attrib));
        }

        /**
         * Returns true if the property with the given attribute name exists
         */
        boolean hasProperty(final String attrib) {
            return props.containsKey(getPropertyKey(attrib));
        }

        /**
         * Get the properties key for the given attribute.
         *
         * @param attrib
         * @return
         */
        String getPropertyKey(final String attrib) {
            return "command." + name + "." + attrib;
        }

        /**
         * Get the handler type for this CommandRep
         *
         * @return
         */
        String getType() {
            return type;
        }

        /**
         * Set the handler type for this CommandRep
         *
         * @param type
         */
        void setType(final String type) {
            for (int i = 0; i < TYPES.length; i++) {
                if (TYPES[i].equals(type)) {
                    this.type = TYPES[i];
                    return;
                }
            }
            throw new IllegalArgumentException("unrecognized command-type: '" + type + "' command: " + name);
        }

        /**
         * returns the input value if it is not null, "" otherwise
         */
        String getStringNotNull(final String name) {
            return (null == name) ? "" : name;
        }

        /**
         * Sets the property of the command with the given attribute name.
         * Attribute names "name", and "controller" are read-only. IllegalArgumentException will be thrown
         * if they are passed as the attribute name.
         *
         * @param attr  name of the attribute. (e.g. "doc","workflow", etc.)
         * @param value value of the property.
         */
        public void setProperty(final String attr, final String value) {
            if ("name".equals(attr)) {
                throw new IllegalArgumentException("Cannot modify 'name' attribute of command");
            }
            if ("controller".equals(attr)) {
                throw new IllegalArgumentException("Cannot modify 'controller' attribute of command");
            }
            props.setProperty(getPropertyKey(attr), value);
            parseProperties();
        }

        /**
         * Return a Map containing the properties for this Command
         *
         * @return
         */
        Map getProperties() {
            final Map m = new HashMap();
            m.put(getPropertyKey("name"), getStringNotNull(name));
            m.put(getPropertyKey("command-type"), getStringNotNull(type));
            m.put(getPropertyKey("controller"), getStringNotNull(controller));
            m.put(getPropertyKey("doc"), getStringNotNull(doc));
            m.put(getPropertyKey("private-access"), getStringNotNull(privateAccess));
            return m;
        }

        /**
         * Return a Map containing the appropriate properties for template filtering
         *
         * @return
         */
        Map filterProperties() {
            final Map m = new HashMap();
            m.put("command.name", getStringNotNull(name));
            m.put("command.command-type", getStringNotNull(type));
            m.put("command.controller", getStringNotNull(controller));
            m.put("command.doc", getStringNotNull(doc));
            m.put("command.private-access", getStringNotNull(privateAccess));
            m.put("command.author", System.getProperty("user.name"));
            return m;
        }

        /**
         * Generate the handler file for this CommandRep
         *
         * @param module    The CmdModule which owns this command
         * @param overwrite if true, overwrite an existing handler
         * @param templates resolver to determine the appropriate template to use
         * @throws IOException
         */
        void generateHandler(final CmdModule module, final boolean overwrite, final TemplateResolver templates) throws IOException {
            final File template = new File(module.getCommandsBasedir(), name + ".template");
            FileUtils.fileCopy(templates.getTemplate(getType()), template, overwrite);
            final File handlerFile = new File(module.getCommandsBasedir(), name + ".xml");
            final File defaults = File.createTempFile(name + "-command", ".properties");
            final Properties filterProps = new Properties();
            filterProps.putAll(filterProperties());
            filterProps.setProperty("module.name", module.getName());
            filterProps.setProperty("command.template", templates.getTemplate(getType()).getAbsolutePath());
            filterProps.setProperty("command.create.date", new Date().toString());
            filterProps.store(new FileOutputStream(defaults), "");
            final ConfigWriter writer = ConfigWriter.create(module.getCommandsBasedir(), defaults, template.getName(), handlerFile.getName());
            writer.write(overwrite);
        }
    }

    /**
     * Represents a "shell" command.
     */
    public static class ShellCommandRep extends CommandRep {

        String executionString = "bash";

        String argumentString = "echo " + name;

        public ShellCommandRep(final String name, final Properties props) {
            super(name, props);
            type = SHELL_TYPE;
        }

        void parseProperties() {
            super.parseProperties();
            executionString = getProperty("execution-string");
            argumentString = getProperty("argument-string");
        }

        Map getProperties() {
            final Map m = super.getProperties();
            m.put(getPropertyKey("execution-string"), getStringNotNull(executionString));
            m.put(getPropertyKey("argument-string"), getStringNotNull(argumentString));
            return m;
        }

        Map filterProperties() {
            final Map m = super.filterProperties();
            m.put("command.execution-string", getPropertyKey("execution-string"));
            m.put("command.argument-string", getPropertyKey("argument-string"));
            return m;
        }
    }

    /**
     * Represents a "daemon" command.
     */
    public static class DaemonCommandRep extends ShellCommandRep {

        String isDaemon = "true";

        public DaemonCommandRep(final String name, final Properties props) {
            super(name, props);
            type = DAEMON_TYPE;
        }

        void parseProperties() {
            super.parseProperties();
            isDaemon = getProperty("daemon");
        }

        Map getProperties() {
            final Map m = super.getProperties();
            m.put(getPropertyKey("daemon"), getStringNotNull(isDaemon));
            return m;
        }

        Map filterProperties() {
            final Map m = super.filterProperties();
            m.put("command.daemon", getPropertyKey("daemon"));
            return m;
        }
    }

    /**
     * Represents a "bat" command.
     */
    public static class BatCommandRep extends ShellCommandRep {

        String executionString = "cmd.exe";

        public BatCommandRep(final String name, final Properties props) {
            super(name, props);
            type = BAT_TYPE;
        }
    }

    /**
     * Represents a "workflow" command.
     */
    public static class WorkflowCommandRep extends CommandRep {

        String commands;

        public WorkflowCommandRep(final String name, final Properties props) {
            super(name, props);
            type = WORKFLOW_TYPE;
        }

        void parseProperties() {
            super.parseProperties();
            commands = getProperty("workflow");
        }

        Map getProperties() {
            final Map m = super.getProperties();
            m.put(getPropertyKey("workflow"), getStringNotNull(commands));
            return m;
        }

        void generateHandler(final CmdModule module, final boolean overwrite, final TemplateResolver templates) throws IOException {
            super.generateHandler(module, overwrite, templates);
            final File hdlrFile = new File(module.getCommandsBasedir(), name + ".xml");
            final SAXReader xmlReader = new SAXReader();
            final Document document;
            final String[] arrCommands = getStringNotNull(commands).split(",");
            try {
                document = xmlReader.read(hdlrFile);
                final Element ts = (Element) document.selectSingleNode("/project/target[@name='-execute']/controller/execute/workflow/tasksequence");
                if (null == ts) {
                    throw new FrameworkResourceException("handler did not have an element matching xpath: " + "/project/target[@name='-execute']/controller/execute/workflow/tasksequence", null);
                }
                for (int i = 0; i < arrCommands.length; i++) {
                    final String cmd = arrCommands[i];
                    if ("".equals(cmd)) continue;
                    final Element cmdExec = ts.addElement(("controller")).addElement("execute");
                    cmdExec.addElement("context").addAttribute("depot", "${context.depot}").addAttribute("entityClass", "${context.type}").addAttribute("entityName", "${context.name}");
                    cmdExec.addElement("command").addAttribute("name", cmd);
                }
                final XMLWriter writer = new XMLWriter(new FileOutputStream(hdlrFile), OutputFormat.createPrettyPrint());
                writer.write(document);
                writer.flush();
            } catch (DocumentException e) {
                throw new IOException(e.getMessage());
            }
        }

        public String toString() {
            return "WorkflowCommandRep{" + "commands='" + commands + "'" + "}";
        }
    }
}
