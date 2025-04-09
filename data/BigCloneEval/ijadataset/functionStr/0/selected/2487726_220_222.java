public class Test {    public LoadedScriptInfo[] loadScripts(URL url) throws ObolException, IOException {
        return this.loadScripts(url.openStream());
    }
}