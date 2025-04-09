package com.nanosn.web.framework.rest;

import com.nanosn.web.framework.rest.IRest;
import com.nanosn.web.framework.rest.IRestFactory;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author selkhateeb
 */
public class RestFactoryImpl implements IRestFactory {

    public IRest CreateInstance(Class RestImp) {
        try {
            Constructor constructor = RestImp.getConstructor(new Class[] {});
            Object Rest = constructor.newInstance(new Object[] {});
            if (Rest instanceof IRest) {
                return (IRest) Rest;
            }
        } catch (Exception ex) {
            Logger.getLogger(RestFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
