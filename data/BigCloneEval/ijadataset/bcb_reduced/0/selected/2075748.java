package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.lang.reflect.Constructor;

/**
 * <a href="ReflectionUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class ReflectionUtil {

    public static Object newInstance(String className, String p1) {
        try {
            Class classObj = Class.forName(className);
            Constructor classConstructor = classObj.getConstructor(new Class[] { String.class });
            Object[] args = new Object[] { p1 };
            return classConstructor.newInstance(args);
        } catch (Exception e) {
            _log.error(e, e);
            return null;
        }
    }

    private static Log _log = LogFactoryUtil.getLog(ReflectionUtil.class);
}
