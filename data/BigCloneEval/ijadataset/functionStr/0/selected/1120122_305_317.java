public class Test {    public void loadFromFile(String sFilename) {
        try {
            ByteArrayOutputStream bsOut = new ByteArrayOutputStream();
            FileInputStream fiIn = new FileInputStream(sFilename);
            int iData = 0;
            while ((iData = fiIn.read()) > -1) bsOut.write(iData);
            String sDataString = bsOut.toString();
            setDataString(sDataString);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            setDataString("");
        }
    }
}