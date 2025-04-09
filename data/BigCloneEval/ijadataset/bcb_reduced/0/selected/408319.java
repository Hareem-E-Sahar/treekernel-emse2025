package org.perfectday.logicengine.core.industry;

import java.io.File;
import org.perfectday.logicengine.core.configuration.Configuration;

/**
 *
 * @author Miguel Angel Lopez Montellano (alakat@gmail.com)
 */
public class EffectedFunctionFactory extends IndexFactory {

    private static EffectedFunctionFactory instance;

    private EffectedFunctionFactory(File f, boolean indexProperties) {
        super(f, indexProperties);
    }

    public static EffectedFunctionFactory getInstance() {
        if (instance == null) instance = new EffectedFunctionFactory(Configuration.getInstance().getEffectFile(), false);
        return instance;
    }

    @Override
    public Object create(String key) throws Exception {
        key = key.trim();
        return ((Class) this.database.get(key)).getConstructor().newInstance();
    }
}
