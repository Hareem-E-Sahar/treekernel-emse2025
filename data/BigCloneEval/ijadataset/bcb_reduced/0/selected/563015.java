package dash.obtain.provider.builder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import ognl.Ognl;
import ognl.OgnlException;
import dash.obtain.initialize.Config;
import dash.obtain.provider.Provider;

public class ProviderBuilder {

    public static List<Provider> buildProviders(String providerList) {
        List<Provider> results = new ArrayList<Provider>();
        List<String> split = split(providerList);
        for (String providerKey : split) {
            Provider p = buildProviderByKey(providerKey);
            results.add(p);
        }
        return results;
    }

    static Provider buildProviderByKey(String providerKey) {
        int i = providerKey.indexOf(':');
        if (i != -1) {
            String providerKlazz = Config.getProperty(providerKey.substring(0, i));
            return buildProvider(providerKlazz, providerKey.substring(i + 1));
        } else {
            String providerKlazz = Config.getProperty(providerKey);
            return buildProvider(providerKlazz);
        }
    }

    static Provider buildProvider(String providerKlazz) {
        try {
            return (Provider) Class.forName(providerKlazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Provider buildProvider(String providerKlazz, String providerArgExpression) {
        try {
            Object arg = resolveArgExpression(providerArgExpression);
            Class cls = Class.forName(providerKlazz);
            Constructor ctor = cls.getConstructor(new Class[] { arg.getClass() });
            Provider provider = (Provider) ctor.newInstance(new Object[] { arg });
            return provider;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Object resolveArgExpression(String providerArgExpression) {
        try {
            Object result = Ognl.getValue(providerArgExpression, null);
            return result;
        } catch (OgnlException e) {
            throw new RuntimeException(e);
        }
    }

    static List<String> split(String providerList) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(providerList, ",");
        while (tok.hasMoreTokens()) {
            result.add(tok.nextToken().trim());
        }
        return result;
    }
}
