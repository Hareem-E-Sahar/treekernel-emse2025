package com.microbrain.cosmos.core.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.logging.Log;
import com.microbrain.cosmos.core.command.CosmosArgumentConverter;
import com.microbrain.cosmos.core.command.CosmosCommand;
import com.microbrain.cosmos.core.command.CosmosExecuter;
import com.microbrain.cosmos.core.command.CosmosMetaCommand;
import com.microbrain.cosmos.core.constants.Constants;
import com.microbrain.cosmos.core.domain.CosmosDomain;
import com.microbrain.cosmos.core.domain.CosmosDomainType;
import com.microbrain.cosmos.core.log.CosmosLogFactory;

/**
 * <p>
 * <code>Configuration</code>��Cosmos��ܵ������ļ������ࡣ������ȡ�����ļ��������������ϵͳ����ĸ������á�
 * ʵ�ֻ���apache��common-configuration��ܡ�
 * </p>
 * 
 * @author Richard Sun (Richard.SunRui@gmail.com)
 * @version 1.0, 08/12/10
 * @see com.microbrain.cosmos.core.config.Plugin
 * @see com.microbrain.cosmos.core.config.ConfigurationException
 * @since CFDK 1.0
 */
public abstract class Configuration {

    /**
	 * common-configuration��ܵ�������ʵ��
	 */
    protected org.apache.commons.configuration.Configuration config = null;

    /**
	 * ���������õĲ��ӳ�䡣
	 */
    protected Map<String, Plugin> plugins = null;

    /**
	 * ���������õ���ӳ�䡣
	 */
    protected Map<String, CosmosDomain> domainMap = null;

    /**
	 * ���������õ�����ת����ӳ�䡣
	 */
    protected Map<Object, CosmosArgumentConverter> converterMap = null;

    /**
	 * ���������õ�����ת�����б?
	 */
    protected List<CosmosArgumentConverter> converters = null;

    /**
	 * ���������õ�ִ������
	 */
    protected Collection<CosmosExecuter> executers = null;

    /**
	 * ���������õ���������ӳ�䡣
	 */
    protected Map<String, CosmosMetaCommand> commandTypeMap = null;

    /**
	 * master�����á�
	 */
    protected CosmosDomain master = null;

    /**
	 * Ĭ�ϵ��������͡�
	 */
    protected CosmosMetaCommand defaultCommandType = null;

    /**
	 * Cosmos��ܵ���Ŀ¼��
	 */
    private String homePath = null;

    /**
	 * ��֤������ʵ�֡�
	 */
    private String authFactory = null;

    /**
	 * ��֤��ʵ�֡�
	 */
    private String authClass = null;

    /**
	 * Ȩ�޹�����ʵ�֡�
	 */
    private String permFactory = null;

    /**
	 * Ȩ����ʵ�֡�
	 */
    private String permClass = null;

    /**
	 * ��־��¼����
	 */
    private static final Log log = CosmosLogFactory.getLog();

    /**
	 * ����Ĭ�Ϲ��캯��ʹ�������޷����á�
	 */
    protected Configuration() {
    }

    /**
	 * װ�������ļ��ķ�����
	 * 
	 * @param file
	 *            װ�ص������ļ�·����
	 * @return �Ѿ������õ������ࡣ
	 * @throws IOException
	 *             ��дIOʱ�׳����쳣��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public static Configuration load(String file) throws IOException, ConfigurationException {
        ConfigurationFactory factory = new ConfigurationFactory(file);
        org.apache.commons.configuration.Configuration conf = null;
        try {
            conf = factory.getConfiguration();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            log.error("initializing config file. ", e);
            throw new ConfigurationException("initializing config file. ", e);
        }
        String configClass = conf.getString(Constants.CONFIGURATION_CLASS_KEY);
        Configuration configuration = null;
        try {
            configuration = (Configuration) Class.forName(configClass).newInstance();
        } catch (Exception e) {
            log.error("initializing config instance. ", e);
            throw new ConfigurationException("initializing config instance. ", e);
        }
        configuration.setConfig(conf);
        configuration.initEnvironment();
        return configuration;
    }

    /**
	 * ͨ��File����װ��һ�������ļ���
	 * 
	 * @param file
	 *            �����ļ���
	 * @return װ�غõ������ļ��ࡣ
	 * @throws IOException
	 *             ��дIOʱ�׳����쳣��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public static Configuration load(File file) throws IOException, ConfigurationException {
        return load(file.getPath());
    }

    /**
	 * ͨ�������ļ���URL��װ��һ�������ļ���
	 * 
	 * @param file
	 *            �����ļ�URL��
	 * @return װ�غõ������ļ��ࡣ
	 * @throws IOException
	 *             ��дIOʱ�׳����쳣��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public static Configuration load(URL file) throws IOException, ConfigurationException {
        return load(file.toString());
    }

    /**
	 * ��ʼ�����еĻ�����
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    private void initEnvironment() throws ConfigurationException {
        loadAuthAndPerm();
        loadConverters();
        loadCommandTypes();
        loadExecuters();
        loadDomains();
        loadPlugins();
        loadExtensions();
    }

    /**
	 * ���Cosmos������ľ���ʵ������ơ�
	 * 
	 * @return Cosmos������ľ���ʵ������ơ�
	 */
    public String getCosmosFactoryClass() {
        return config.getString(Constants.FACTORY_CLASS_KEY);
    }

    /**
	 * ���master������á�
	 * 
	 * @return master�����á�
	 */
    public CosmosDomain getMasterDomain() {
        return this.master;
    }

    /**
	 * ���path��������ļ��е�һ���ַ�
	 * 
	 * @param path
	 *            ·����
	 * @return ��·����Ӧ���ַ�
	 */
    public String getString(String path) {
        return this.config.getString(path);
    }

    /**
	 * ���path��������ļ��е�һ������ֵ��
	 * 
	 * @param path
	 *            ·����
	 * @return ��·����Ӧ�Ĳ���ֵ��
	 */
    public Boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    /**
	 * ���ϵͳ�����в����
	 * 
	 * @return ϵͳ���õ����в����
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Map<String, Plugin> getPlugins() throws ConfigurationException {
        return this.plugins;
    }

    /**
	 * ���ϵͳ���õ�������
	 * 
	 * @return ������
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Map<String, CosmosDomain> getDomains() throws ConfigurationException {
        return this.domainMap;
    }

    /**
	 * ���ϵͳ���õ���������ת������
	 * 
	 * @return ϵͳ���õ���������ת������
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Map<Object, CosmosArgumentConverter> getConvertersMap() throws ConfigurationException {
        return this.converterMap;
    }

    /**
	 * �����������ת������
	 * 
	 * @return ���е�����ת������
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Collection<CosmosArgumentConverter> getConverters() throws ConfigurationException {
        return this.converters;
    }

    /**
	 * ������е��������͡�
	 * 
	 * @return ���е��������͡�
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Map<String, CosmosMetaCommand> getCommandTypes() throws ConfigurationException {
        return this.commandTypeMap;
    }

    /**
	 * ���Ĭ�ϵ��������͡�
	 * 
	 * @return Ĭ�ϵ��������͡�
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public CosmosMetaCommand getDefaultCommandType() throws ConfigurationException {
        return this.defaultCommandType;
    }

    /**
	 * ������п��õ�ִ������
	 * 
	 * @return ���п��õ�ִ������
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Collection<CosmosExecuter> getExecuters() throws ConfigurationException {
        return this.executers;
    }

    /**
	 * ���ĳ��Ԫ�ص�ĳ����ʼ������
	 * 
	 * @param elementPath
	 *            Ԫ��·����
	 * @param key
	 *            ��ʼ�������
	 * @return ����key��Ӧ�ĳ�ʼ������ֵ��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public String getElementInitParameter(String elementPath, String key) throws ConfigurationException {
        Collection<String> names = getByName(elementPath + ".init-param");
        String value = null;
        int i = 0;
        for (String name : names) {
            if (key.equals(name)) {
                value = this.config.getString(elementPath + ".init-param(" + i + ").[@value]");
                break;
            }
            i++;
        }
        return value;
    }

    /**
	 * ���ĳ��Ԫ�ص����г�ʼ������
	 * 
	 * @param elementPath
	 *            Ԫ��·����
	 * @param key
	 *            ��ʼ�������
	 * @return ����key��Ӧ�ĳ�ʼ������ֵ��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Collection<String> getElementInitParameters(String elementPath, String key) throws ConfigurationException {
        Collection<String> names = getByName(elementPath + ".init-param");
        List<String> values = new ArrayList<String>();
        String value = null;
        int i = 0;
        for (String name : names) {
            if (key.equals(name)) {
                value = this.config.getString(elementPath + ".init-param(" + i + ").[@value]");
                values.add(value);
            }
            i++;
        }
        return values;
    }

    /**
	 * ���ĳ�������ĳ����ʼ������
	 * 
	 * @param plugin
	 *            �����ơ�
	 * @param key
	 *            ��ʼ�������
	 * @return ����key��Ӧ�ĳ�ʼ������ֵ��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public String getPlugInitParameter(String plugin, String key) throws ConfigurationException {
        Plugin plug = this.plugins.get(plugin);
        int index = plug.getIndex();
        String elementPath = "cosmos.plugins.plugin(" + index + ")";
        return getElementInitParameter(elementPath, key);
    }

    /**
	 * ���ĳ����������г�ʼ������
	 * 
	 * @param plugin
	 *            �����ơ�
	 * @param key
	 *            ��ʼ�������
	 * @return ����key��Ӧ�ĳ�ʼ������ֵ��
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Collection<String> getInitParameters(String plugin, String key) throws ConfigurationException {
        Plugin plug = this.plugins.get(plugin);
        int index = plug.getIndex();
        String elementPath = "cosmos.plugins.plugin(" + index + ")";
        return getElementInitParameters(elementPath, key);
    }

    /**
	 * ͨ�����name�����ĳ��Ԫ�ص��б?
	 * 
	 * @param elementPath
	 *            Ԫ��·����
	 * @return Ԫ���б?
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    public Collection<String> getByName(String elementPath) throws ConfigurationException {
        return getByAttribute(elementPath, "name");
    }

    /**
	 * ͨ��ĳ�����������Ԫ���б?
	 * 
	 * @param elementPath
	 *            Ԫ��·����
	 * @param attribute
	 *            ���ԡ�
	 * @return Ԫ���б?
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    @SuppressWarnings("unchecked")
    public Collection<String> getByAttribute(String elementPath, String attribute) throws ConfigurationException {
        Collection<String> attributes = new ArrayList<String>();
        Object elementAttributes = this.config.getProperty(elementPath + ".[@" + attribute + "]");
        if (elementAttributes == null) {
            return attributes;
        }
        if (!(elementAttributes instanceof Collection)) {
            attributes.add((String) elementAttributes);
        } else {
            attributes = (Collection<String>) elementAttributes;
        }
        return attributes;
    }

    /**
	 * ���Cosmos����Ŀ¼��
	 * 
	 * @return Cosmos��ܵ���Ŀ¼��
	 */
    public String getHomePath() {
        return homePath;
    }

    /**
	 * ����Cosmos��ܵ���Ŀ¼��
	 * 
	 * @param homePath
	 *            Cosmos��ܵ���Ŀ¼��
	 */
    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    /**
	 * �����֤�������ʵ�֡�
	 * 
	 * @return ��֤�������ʵ�֡�
	 */
    public String getAuthFactory() {
        return authFactory;
    }

    /**
	 * �����֤���ʵ�֡�
	 * 
	 * @return ��֤���ʵ�֡�
	 */
    public String getAuthClass() {
        return authClass;
    }

    /**
	 * ���Ȩ�޹������ʵ�֡�
	 * 
	 * @return Ȩ�޹������ʵ�֡�
	 */
    public String getPermFactory() {
        return permFactory;
    }

    /**
	 * ���Ȩ�����ʵ�֡�
	 * 
	 * @return Ȩ�����ʵ�֡�
	 */
    public String getPermClass() {
        return permClass;
    }

    /**
	 * ���������ļ��ࡣ
	 * 
	 * @param config
	 *            �����ļ��ࡣ
	 */
    private void setConfig(org.apache.commons.configuration.Configuration config) {
        this.config = config;
    }

    /**
	 * װ����֤��Ȩ�޲��ֵ������ļ���
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    private void loadAuthAndPerm() throws ConfigurationException {
        this.authFactory = this.config.getString(Constants.COSMOS_AUTHENTICATION_FACTORY);
        this.authClass = this.config.getString(Constants.COSMOS_AUTHENTICATION_TOKEN);
        this.permFactory = this.config.getString(Constants.COSMOS_PERMISSION_FACTORY);
        this.permClass = this.config.getString(Constants.COSMOS_PERMISSION_CLASS);
    }

    /**
	 * װ�����е�����ת������
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    @SuppressWarnings("unchecked")
    private void loadConverters() throws ConfigurationException {
        this.converterMap = new LinkedHashMap<Object, CosmosArgumentConverter>();
        this.converters = new ArrayList<CosmosArgumentConverter>();
        Collection<String> names = getByName(Constants.COSMOS_CONVERTERS_CONVERTER);
        try {
            int converterIndex = 0;
            for (String name : names) {
                String clazz = this.config.getString(String.format(Constants.COSMOS_CONVERTERS_CONVERTER_CLASS, converterIndex));
                String label = this.config.getString(String.format(Constants.COSMOS_CONVERTERS_CONVERTER_LABEL, converterIndex));
                Constructor<CosmosArgumentConverter> constructor = ((Class<CosmosArgumentConverter>) Class.forName(clazz)).getConstructor(String.class, String.class);
                CosmosArgumentConverter converter = constructor.newInstance(name, label);
                Collection<String> jdbcTypeValues = this.getByAttribute(String.format(Constants.COSMOS_CONVERTERS_CONVERTER_JDBC_TYPE, converterIndex), "value");
                Collection<Integer> jdbcTypes = new ArrayList<Integer>();
                if (jdbcTypeValues != null) {
                    for (String jdbcTypeValue : jdbcTypeValues) {
                        Integer jdbcType = Integer.valueOf(jdbcTypeValue);
                        jdbcTypes.add(jdbcType);
                        this.converterMap.put(jdbcType, converter);
                    }
                }
                converter.setMappedJdbcTypes(jdbcTypes);
                this.converterMap.put(name, converter);
                this.converters.add(converter);
                converterIndex++;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Loading converters has some errors. ", e);
        }
    }

    /**
	 * װ�����е��������͡�
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    @SuppressWarnings("unchecked")
    private void loadCommandTypes() throws ConfigurationException {
        this.commandTypeMap = new LinkedHashMap<String, CosmosMetaCommand>();
        Collection<String> names = getByName(Constants.COSMOS_COMMAND_TYPES_TYPE);
        try {
            int commandTypeIndex = 0;
            for (String name : names) {
                String classString = this.config.getString(String.format(Constants.COSMOS_COMMAND_TYPES_TYPE_CLASS, commandTypeIndex));
                Class<CosmosCommand> command = (Class<CosmosCommand>) Class.forName(classString);
                String label = this.config.getString(String.format(Constants.COSMOS_COMMAND_TYPES_TYPE_LABEL, commandTypeIndex));
                Boolean composite = this.config.getBoolean(String.format(Constants.COSMOS_COMMAND_TYPES_TYPE_COMPOSITE, commandTypeIndex));
                Boolean defaultCommand = this.config.getBoolean(String.format(Constants.COSMOS_COMMAND_TYPES_TYPE_DEFAULT, commandTypeIndex));
                String description = this.config.getString(String.format(Constants.COSMOS_COMMAND_TYPES_TYPE_DESCRIPTION, commandTypeIndex));
                CosmosMetaCommand type = new CosmosMetaCommand(command, composite, defaultCommand, description, label, name);
                if (defaultCommand) {
                    defaultCommandType = type;
                }
                this.commandTypeMap.put(name, type);
                commandTypeIndex++;
            }
            if (defaultCommandType == null) {
                throw new ConfigurationException("There is no default command type.");
            }
        } catch (Exception e) {
            throw new ConfigurationException("Loading converters has some errors. ", e);
        }
    }

    /**
	 * װ�����е�����ִ������
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    @SuppressWarnings("unchecked")
    private void loadExecuters() throws ConfigurationException {
        this.executers = new ArrayList<CosmosExecuter>();
        Collection<String> names = getByName(Constants.COSMOS_EXECUTERS_EXECUTER);
        try {
            int executerIndex = 0;
            for (String name : names) {
                String clazz = this.config.getString(String.format(Constants.COSMOS_EXECUTERS_EXECUTER_CLASS, executerIndex));
                String label = this.config.getString(String.format(Constants.COSMOS_EXECUTERS_EXECUTER_LABEL, executerIndex));
                String category = this.config.getString(String.format(Constants.COSMOS_EXECUTERS_EXECUTER_CATEGORY, executerIndex));
                String description = this.config.getString(String.format(Constants.COSMOS_EXECUTERS_EXECUTER_DESCRIPTION, executerIndex));
                Constructor<CosmosExecuter> constructor = ((Class<CosmosExecuter>) Class.forName(clazz)).getConstructor(String.class, String.class, String.class, String.class);
                CosmosExecuter converter = constructor.newInstance(name, label, description, category);
                this.executers.add(converter);
                executerIndex++;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Loading converters has some errors. ", e);
        }
    }

    /**
	 * װ�����е���
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    @SuppressWarnings("unchecked")
    private void loadDomains() throws ConfigurationException {
        this.domainMap = new LinkedHashMap<String, CosmosDomain>();
        Collection<String> names = getByName(Constants.COSMOS_DOMAINS_DOMAIN);
        try {
            int domainIndex = 0;
            for (String name : names) {
                String clazz = this.config.getString(String.format(Constants.COSMOS_DOMAINS_DOMAIN_CLASS, domainIndex));
                String type = this.config.getString(String.format(Constants.COSMOS_DOMAINS_DOMAIN_TYPE, domainIndex));
                CosmosDomainType domainType = CosmosDomainType.valueOf(type);
                Constructor<CosmosDomain> constructor = ((Class<CosmosDomain>) Class.forName(clazz)).getConstructor(String.class, CosmosDomainType.class, int.class);
                CosmosDomain domain = constructor.newInstance(name, domainType, domainIndex);
                if (domainType == CosmosDomainType.master) {
                    this.master = domain;
                }
                this.domainMap.put(name, domain);
                domainIndex++;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Loading domains has some errors. ", e);
        }
    }

    /**
	 * װ�����еĲ����
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    private void loadPlugins() throws ConfigurationException {
        this.plugins = new LinkedHashMap<String, Plugin>();
        Collection<String> names = getByName(Constants.COSMOS_PLUGIN);
        try {
            int i = 0;
            for (String name : names) {
                String clazz = this.config.getString(String.format(Constants.COSMOS_PLUGIN_CLASS, i));
                Plugin plugin = (Plugin) Class.forName(clazz).newInstance();
                plugin.setName(name);
                plugin.setIndex(i);
                this.plugins.put(name, plugin);
                i++;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Initializing plugins has some errors. ", e);
        }
    }

    /**
	 * װ�������ļ�����չ��������Ϣ��
	 * 
	 * @throws ConfigurationException
	 *             ���������ļ�ʱ�׳����쳣��
	 */
    protected abstract void loadExtensions() throws ConfigurationException;
}
