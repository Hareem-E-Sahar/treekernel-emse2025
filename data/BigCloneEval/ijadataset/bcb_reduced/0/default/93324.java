import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TestDesktop {

    public static void main(String[] args) throws Exception {
        Desktop desktop = Desktop.getDesktop();
        testScriptEngine(desktop, "hellojava");
    }

    private static void testBrowser(Desktop desktop) throws IOException, URISyntaxException {
        if (desktop.isDesktopSupported()) desktop.browse(new URI("http://sports.sina.com.cn")); else System.out.println("not support desktop!");
    }

    private static void testFile(Desktop desktop) throws IOException {
        File file = new File("a.txt");
        desktop.open(file);
    }

    private static void testScriptEngine(Desktop desktop, String name) throws ScriptException {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine se = sem.getEngineByName("js");
        se.eval("print('hello" + name + "')");
    }
}
