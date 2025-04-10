package bpiwowar.experiments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

@TaskDescription(name = "about", project = { "main" })
public class About implements Task {

    Logger logger = Logger.getLogger(About.class);

    /**
	 * @param args
	 * @throws IOException
	 */
    @Override
    public int run() {
        Enumeration<?> e;
        try {
            e = About.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (e.hasMoreElements()) {
                final URL url = (URL) e.nextElement();
                if (url.toString().indexOf("renaissance") != -1) {
                    final InputStream is = url.openStream();
                    Properties p = new Properties();
                    p.load(is);
                    for (Entry<?, ?> entry : p.entrySet()) {
                        System.err.println(entry);
                    }
                }
            }
        } catch (IOException e1) {
            logger.fatal("Caught an exception " + e1);
            return 1;
        }
        System.err.println("Classpath is " + System.getProperty("java.class.path"));
        return 0;
    }

    @Override
    public void init(String[] args) throws Exception {
    }
}
