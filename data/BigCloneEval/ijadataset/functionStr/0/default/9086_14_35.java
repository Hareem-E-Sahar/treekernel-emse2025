public class Test {    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Pragma", "No-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", -10);
        ServletContext sc = getServletContext();
        String filename = req.getParameter("file");
        String mimeType = DownType.getInst().getDownTypeByFileName(filename);
        if (mimeType == null) {
            sc.log((new StringBuilder("Could not get MIME type of ")).append(filename).toString());
            resp.setStatus(500);
            return;
        }
        resp.setContentType(mimeType);
        String realPath = sc.getRealPath(filename);
        InputStream in = new java.io.FileInputStream(realPath);
        OutputStream out = resp.getOutputStream();
        byte buf[] = new byte[1024];
        for (int count = 0; (count = in.read(buf)) >= 0; ) out.write(buf, 0, count);
        in.close();
        out.close();
        buf = null;
    }
}