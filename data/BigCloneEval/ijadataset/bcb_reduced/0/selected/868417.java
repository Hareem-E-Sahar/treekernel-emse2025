package outputers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

public class OutputLoader {

    IOutputer _loaded;

    String _packageName;

    String _outputerName;

    String _fileName;

    public IOutputer get_Outputer() throws Exception {
        if (_loaded == null) {
            URLClassLoader loader = URLClassLoader.newInstance(new URL[] { new File("").toURI().toURL() });
            Class<?> outClass = Class.forName(_packageName + "." + _outputerName + "." + _outputerName + "Outputer", false, loader);
            Constructor<?> cons = outClass.getConstructor(String.class);
            _loaded = (IOutputer) cons.newInstance(_fileName);
        }
        return _loaded;
    }

    public OutputLoader(String packageName, String outputerName, String fileName) throws Exception {
        _outputerName = outputerName;
        _fileName = fileName;
        _packageName = packageName;
    }
}
