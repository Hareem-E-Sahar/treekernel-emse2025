public class Test {    private String post(String json) throws Exception {
        byte[] bRequest;
        byte[] buffer;
        int len;
        URL servlet;
        StringBuffer headerbuf;
        Socket socket;
        BufferedOutputStream outBuffer;
        bRequest = json.getBytes();
        servlet = new URL("http://" + "localhost" + ":" + "8080" + "/Pepper/client");
        headerbuf = new StringBuffer();
        headerbuf.append("POST ").append(servlet.getFile()).append(" HTTP/1.0\r\n").append("Host: ").append(servlet.getHost()).append("\r\n").append("Content-Length: ").append(bRequest.length).append("\r\n\r\n");
        socket = new Socket(servlet.getHost(), servlet.getPort());
        outBuffer = new BufferedOutputStream(socket.getOutputStream());
        outBuffer.write(headerbuf.toString().getBytes());
        outBuffer.write(bRequest);
        outBuffer.flush();
        socket.getOutputStream().flush();
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream resultBuf = new ByteArrayOutputStream();
        buffer = new byte[1024];
        while ((len = socket.getInputStream().read(buffer)) > 0) resultBuf.write(buffer, 0, len);
        os.close();
        is.close();
        resultBuf.close();
        String temp = resultBuf.toString();
        int pos = temp.indexOf("\r\n\r\n");
        return temp.substring(pos + 4);
    }
}