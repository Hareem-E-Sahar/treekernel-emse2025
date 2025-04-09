package jezuch.utils.starmapper3.app.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jezuch.utils.Pair;
import jezuch.utils.io.IOUtils;
import jezuch.utils.starmapper3.app.common.FileSystemConfig;
import jezuch.utils.starmapper3.app.common.IniConfig;
import jezuch.utils.starmapper3.app.common.PropertiesConfig;
import jezuch.utils.starmapper3.app.common.XmlConfig;
import jezuch.utils.starmapper3.config.Path;

/**
 * @author ksobolewski
 */
class GenericConfig implements Iterable<Pair<Path, String>> {

    private static final Map<String, Class<? extends Iterable<Pair<Path, String>>>> STRATEGY = new LinkedHashMap<String, Class<? extends Iterable<Pair<Path, String>>>>();

    static {
        STRATEGY.put("ini", IniConfig.class);
        STRATEGY.put("xml", XmlConfig.class);
        STRATEGY.put("properties", PropertiesConfig.class);
        STRATEGY.put("dir", FileSystemConfig.class);
    }

    private static final Pattern SIG_PATTERN = Pattern.compile("(^|/)(([^/.]+)|([^/]+)\\.([^/.]+))$");

    private static final int SIG_PATTERN_SIMPLE = 3;

    private static final int SIG_PATTERN_COMPLEX_SIG = 4;

    private static final int SIG_PATTERN_COMPLEX_EXT = 5;

    private final Iterable<Pair<Path, String>> delegate;

    private final String sig;

    private static Iterable<Pair<Path, String>> create(URL configLoc, Class<? extends Iterable<Pair<Path, String>>> clazz) throws IOException, ParseException {
        try {
            Constructor<? extends Iterable<Pair<Path, String>>> constructor = clazz.getConstructor(URL.class);
            return constructor.newInstance(configLoc);
        } catch (SecurityException ex) {
            throw new Error(ex);
        } catch (NoSuchMethodException ex) {
            throw new Error(ex);
        } catch (IllegalArgumentException ex) {
            throw new Error(ex);
        } catch (InstantiationException ex) {
            throw new Error(ex);
        } catch (IllegalAccessException ex) {
            throw new Error(ex);
        } catch (InvocationTargetException ex) {
            IOUtils.checkCause(IOException.class, ex);
            IOUtils.checkCause(ParseException.class, ex);
            throw new RuntimeException(ex.getCause());
        }
    }

    private static Iterable<Pair<Path, String>> resolve(URL configLoc, String ext) throws IOException, ParseException {
        Class<? extends Iterable<Pair<Path, String>>> clazz = STRATEGY.get(ext);
        return clazz != null ? create(configLoc, clazz) : null;
    }

    public GenericConfig(URL configLoc) throws IOException, ParseException {
        Matcher m = SIG_PATTERN.matcher(configLoc.getPath());
        if (m.find()) {
            if (m.group(SIG_PATTERN_SIMPLE) == null) {
                System.out.println("Using config: " + configLoc.toString());
                this.delegate = resolve(configLoc, m.group(SIG_PATTERN_COMPLEX_EXT));
                this.sig = m.group(SIG_PATTERN_COMPLEX_SIG);
            } else {
                Iterable<Pair<Path, String>> delegate = null;
                for (Map.Entry<String, Class<? extends Iterable<Pair<Path, String>>>> e : STRATEGY.entrySet()) try {
                    URL url = new URL(configLoc.getProtocol(), configLoc.getHost(), configLoc.getPort(), configLoc.getFile() + "." + e.getKey());
                    System.out.println("Trying config: " + url.toString());
                    delegate = create(url, e.getValue());
                    break;
                } catch (FileNotFoundException ex) {
                    continue;
                }
                this.delegate = delegate;
                this.sig = m.group(SIG_PATTERN_SIMPLE);
            }
            if (this.delegate == null) throw new IllegalArgumentException(m.group(SIG_PATTERN_COMPLEX_EXT));
        } else throw new IllegalArgumentException(configLoc.toString());
    }

    public Iterator<Pair<Path, String>> iterator() {
        return delegate.iterator();
    }

    public String getSig() {
        return sig;
    }
}
