package org.isi.monet.core.translators;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.isi.monet.core.constants.ErrorCode;
import org.isi.monet.core.exceptions.SystemException;

public class TranslatorsFactory {

    private static TranslatorsFactory oInstance;

    private HashMap<String, Class<?>> hmTranslators;

    private TranslatorsFactory() {
        this.hmTranslators = new HashMap<String, Class<?>>();
    }

    public static synchronized TranslatorsFactory getInstance() {
        if (oInstance == null) oInstance = new TranslatorsFactory();
        return oInstance;
    }

    public Object get(String sType) {
        Class<?> cTranslator;
        Translator oTranslator = null;
        try {
            cTranslator = (Class<?>) this.hmTranslators.get(sType);
            Constructor<?> oConstructor = cTranslator.getConstructor(String.class);
            oTranslator = (Translator) oConstructor.newInstance(sType);
        } catch (NullPointerException oException) {
            throw new SystemException(ErrorCode.TRANSLATORS_FACTORY, sType, oException);
        } catch (Exception oException) {
            throw new SystemException(ErrorCode.TRANSLATORS_FACTORY, sType, oException);
        }
        return oTranslator;
    }

    public Boolean register(String sType, Class<?> cProducer) throws IllegalArgumentException {
        if ((cProducer == null) || (sType == null)) {
            return false;
        }
        this.hmTranslators.put(sType, cProducer);
        return true;
    }
}
