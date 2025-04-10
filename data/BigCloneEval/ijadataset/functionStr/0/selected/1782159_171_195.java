public class Test {    private boolean parse(Type type, URL url, boolean checkDict) throws Exception {
        boolean ok = true;
        Exception ee = null;
        Element rootElement = null;
        try {
            InputStream in = url.openStream();
            if (type.equals(Type.XOM)) {
                new Builder().build(in);
            } else if (type.equals(Type.CML)) {
                rootElement = new CMLBuilder().build(in).getRootElement();
            }
            in.close();
        } catch (Exception e) {
            ee = e;
        }
        if (ee != null) {
            logger.severe("failed to cmlParse: " + url + "\n..... because: [" + ee + "] [" + ee.getMessage() + "] in [" + url + S_RSQUARE);
            ok = false;
            throw new RuntimeException("Problem in test harness when parsing " + url, ee);
        }
        if (ok && checkDict) {
            ok = checkDict(rootElement);
        }
        return ok;
    }
}