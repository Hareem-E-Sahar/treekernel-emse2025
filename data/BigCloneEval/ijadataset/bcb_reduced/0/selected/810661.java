package net.sf.lightbound.opencms.autointegration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.lightbound.util.LightBoundUtil;

public class JSPGenerator {

    private static final String JSP_FILE_CHARSET = "ISO-8859-1";

    private static final String RES_TEMPLATE = "/net/sf/lightbound/opencms/autointegration/autointegrationtemplate.jsp";

    private static final String TEMPLATE_MODULE_NAME = "%%MODULE_NAME%%";

    private static final String TEMPLATE_PAGE_PATH = "%%STATIC_PAGE_PATH%%";

    public static void main(String[] args) throws IOException {
        String moduleName = args[0];
        String pagesRoot = args[1];
        String jspPackagePathname = args[2];
        InputStream in = JSPGenerator.class.getResourceAsStream(RES_TEMPLATE);
        if (in == null) {
            throw new FileNotFoundException(RES_TEMPLATE);
        }
        Reader reader = new InputStreamReader(in);
        StringBuffer buf = new StringBuffer();
        char[] readBuf = new char[2048];
        int readLength = 0;
        while ((readLength = reader.read(readBuf)) > 0) {
            buf.append(readBuf, 0, readLength);
        }
        reader.close();
        String template = buf.toString();
        template = template.replace(TEMPLATE_MODULE_NAME, moduleName);
        File webRoot = new File(pagesRoot);
        if (!webRoot.exists()) {
            throw new FileNotFoundException("web root not found: '" + pagesRoot + "'");
        }
        ZipOutputStream os = new ZipOutputStream(new FileOutputStream(jspPackagePathname));
        try {
            process(webRoot, webRoot, template, os);
        } finally {
            os.close();
        }
        System.out.println("Saved JSP files in: " + jspPackagePathname);
    }

    private static void process(File webRoot, File dir, String template, ZipOutputStream os) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                process(webRoot, file, template, os);
                continue;
            }
            Matcher htmlFileMatcher = Generators.HTML_EXT_MATCH.matcher(file.getName());
            if (htmlFileMatcher.matches()) {
                String fileRelativePath = LightBoundUtil.removePrefix(file.getCanonicalPath(), webRoot.getCanonicalPath());
                while (fileRelativePath.startsWith("/")) {
                    fileRelativePath = fileRelativePath.substring(1);
                }
                String jspContents = template.replace(TEMPLATE_PAGE_PATH, fileRelativePath);
                Matcher relativePathMatcher = Generators.HTML_EXT_MATCH.matcher(fileRelativePath);
                relativePathMatcher.matches();
                String jspFilename = relativePathMatcher.group(Generators.HTML_FILENAME_GROUP) + ".jsp";
                System.out.println("Generated JSP file: '" + jspFilename + "' which " + "refers to '" + fileRelativePath + "'");
                os.putNextEntry(new ZipEntry(jspFilename));
                os.write(jspContents.getBytes(JSP_FILE_CHARSET));
                os.closeEntry();
            }
        }
    }
}
