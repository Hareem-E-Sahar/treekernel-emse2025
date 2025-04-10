public class Test {    public static String getMultipartSignature(String sharedSecret, List params) {
        List ignoreParameters = new ArrayList();
        ignoreParameters.add("photo");
        addAuthToken(params);
        StringBuffer buffer = new StringBuffer();
        buffer.append(sharedSecret);
        Collections.sort(params, new ParameterAlphaComparator());
        Iterator iter = params.iterator();
        while (iter.hasNext()) {
            Parameter param = (Parameter) iter.next();
            if (!ignoreParameters.contains(param.getName().toLowerCase())) {
                buffer.append(param.getName());
                buffer.append(param.getValue());
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return ByteUtilities.toHexString(md.digest(buffer.toString().getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException u) {
            throw new RuntimeException(u);
        }
    }
}