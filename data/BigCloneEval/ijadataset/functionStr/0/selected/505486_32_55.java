public class Test {    public boolean process() throws ResourceNotFoundException {
        BufferedReader reader = null;
        try {
            StringWriter sw = new StringWriter();
            reader = new BufferedReader(new InputStreamReader(resourceLoader.getResourceStream(name), encoding));
            char buf[] = new char[1024];
            int len = 0;
            while ((len = reader.read(buf, 0, 1024)) != -1) sw.write(buf, 0, len);
            setData(sw.toString());
            return true;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            rsvc.getLog().error("Cannot process content resource", e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}