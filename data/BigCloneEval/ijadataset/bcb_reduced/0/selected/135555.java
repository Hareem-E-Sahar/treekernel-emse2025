package com.googlecode.webduff;

import java.lang.reflect.Constructor;
import javax.servlet.ServletContext;
import org.apache.commons.configuration.Configuration;
import com.googlecode.webduff.io.URI;

public class WebDuffConfiguration {

    private static final long serialVersionUID = 4516605399457338262L;

    private ServletContext theContext;

    private Configuration theConfiguration;

    public WebDuffConfiguration(ServletContext servletContext, Configuration configuration) {
        theContext = servletContext;
        theConfiguration = configuration;
    }

    public ServletContext getContext() {
        return theContext;
    }

    public Configuration getConf() {
        return theConfiguration;
    }

    public WebDuffConfiguration subset(String subset) {
        return new WebDuffConfiguration(getContext(), theConfiguration.subset(subset));
    }

    public URI getPath(String element) {
        URI prefixURI = new URI();
        String pathToElementContent = element;
        if (getConf().getProperty(element + ".context-relative") != null) {
            pathToElementContent = element + ".context-relative";
            prefixURI = new URI(getContext().getRealPath(""), System.getProperty("file.separator"));
        }
        return prefixURI.append(new URI(getConf().getString(pathToElementContent), URI.DEFAULT_SEPARATOR));
    }

    @SuppressWarnings("unchecked")
    public <T extends Configurable> T getConfigurableComponent(WebDuffConfiguration componentConf) {
        T component = null;
        try {
            Class<?> theClass = WebDuffConfiguration.class.getClassLoader().loadClass(componentConf.getConf().getString("class"));
            Constructor<?> aConstructor = theClass.getConstructor();
            component = (T) aConstructor.newInstance();
            component.init(componentConf);
        } catch (Exception e) {
            throw new RuntimeException("Some problem making component", e);
        }
        return component;
    }
}
