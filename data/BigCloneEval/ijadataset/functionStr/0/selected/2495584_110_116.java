public class Test {    public void setSource(URL url) throws IOException {
        m_structure = null;
        m_Buffer = null;
        setRetrieval(NONE);
        setSource(url.openStream());
        m_URL = url.toString();
    }
}