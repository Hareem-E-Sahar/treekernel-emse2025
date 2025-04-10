public class Test {            public void run() throws IOException {
                FileObject dest = projectFolderFO.getFileObject("build.xml");
                final URL url = AntProjectHelperBuilder.class.getResource("resources/build-impl.xml");
                InputStream in = url.openStream();
                OutputStream out = dest.getOutputStream();
                FileUtil.copy(in, out);
                in.close();
                out.close();
            }
}