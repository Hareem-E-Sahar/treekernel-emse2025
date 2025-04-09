package org.njo.webapp.templates.taglib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.apache.taglibs.standard.tag.common.core.NullAttributeException;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.njo.webapp.templates.config.ConfigLoadHelper;
import org.njo.webapp.templates.config.PatternConfig;
import org.njo.webapp.templates.servlet.wrapper.FileResponseWrapper;
import org.njo.webapp.templates.servlet.wrapper.StringResponseWrapper;

public class Functions {

    /** 用于匹配是否包含表达式的正则表达式 */
    private static final String EXPRESSION_REGEX = "[\\d\\D]*?\\$\\{[\\d\\D]*?\\}[\\d\\D]*?";

    /**
     * 判定指定的字符串是否包含表达式.
     * @param expression
     * @return true包含 false不包含
     */
    private static boolean containsExpression(String expression) {
        if (expression == null) {
            return false;
        }
        return expression.matches(EXPRESSION_REGEX);
    }

    /**
     * 求表达式值.
     * 用递归方法求出指定表达式的值,
     * 也就是说如果求出的结果中包含表达式,
     * 就会继续求值,知道结果中不包含表达式为止.
     * 
     * @param expression
     * @param expectedType
     * @param pageContext
     * @return
     * @throws JspException
     */
    private static Object evaluate(String expression, Class expectedType, PageContext pageContext) throws JspException {
        Object value_;
        try {
            value_ = ExpressionUtil.evalNotNull("functions", "evaluate", expression, expectedType, null, pageContext);
        } catch (NullAttributeException e) {
            return null;
        }
        if (value_ instanceof String) {
            if (containsExpression((String) value_)) {
                return evaluate((String) value_, expectedType, pageContext);
            }
            return value_;
        } else {
            return value_;
        }
    }

    /**
     * 表达式求值.
     * 
     * 这个方法会返回Object类型的实例.
     * 
     * @param expression
     * @param pageContext
     * @return
     * @throws JspException
     */
    public static Object evaluate(String expression, PageContext pageContext) throws JspException {
        return evaluate(expression, Object.class, pageContext);
    }

    /**
     * 递归创建目录.
     * 
     * @param path
     */
    private static void mkPath(String path) {
        File dir = new File(path);
        File parentFile = dir.getParentFile();
        if (parentFile != null) {
            if (!parentFile.exists()) {
                mkPath(parentFile.getPath());
            }
        }
        dir.mkdir();
    }

    /**
     * 根据指定的文件名取得路径名.
     * @param filename
     * @return
     */
    private static String getPath(String filename) {
        File file = new File(filename);
        return file.getParent();
    }

    /**
     * 转换模版到指定的文件中.
     * 使用FileResponseWrapper包装器,
     * 将指定的模版文件转换(输出)到指定的目标文件中. 
     * 目标文件的目录不存在时,自动的创建.
     * 
     * @param src
     * @param dest
     * @param pageContext
     * @throws JspException
     */
    public static String transform(String src, String dest, String targetDirectory, boolean outit, String charSet, PageContext pageContext) throws JspException {
        try {
            ServletContext context = pageContext.getServletContext();
            if (outit) {
                String outputFile = targetDirectory + dest;
                mkPath(getPath(outputFile));
                RequestDispatcher requestDispatcher = context.getRequestDispatcher(src);
                FileResponseWrapper responseWapper = new FileResponseWrapper((HttpServletResponse) pageContext.getResponse(), outputFile, charSet);
                requestDispatcher.include(pageContext.getRequest(), responseWapper);
                responseWapper.flush();
                responseWapper.close();
                return dest;
            } else {
                RequestDispatcher requestDispatcher = context.getRequestDispatcher(src);
                StringResponseWrapper responseWapper = new StringResponseWrapper((HttpServletResponse) pageContext.getResponse(), charSet);
                requestDispatcher.include(pageContext.getRequest(), responseWapper);
                responseWapper.flush();
                responseWapper.close();
                return responseWapper.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new JspException("Functions.transform() - " + e.getMessage(), e);
        } catch (ServletException e) {
            throw new JspException("Functions.transform() - " + e.getMessage(), e);
        }
    }

    private static final int BUFFER = 2048;

    /**
     * 压缩指定的文件或目录到指定的zip文件.
     * @param argFile
     * @param argZipFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File argFile, File argZipFile) throws FileNotFoundException, IOException {
        if (argFile.isDirectory()) {
            directoryToZip(argFile, argZipFile);
        } else {
            fileToZip(argFile, argZipFile);
        }
    }

    private static void fileToZip(File argFile, File argZipFile) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(argZipFile);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            ZipEntry zipEntry = new ZipEntry(argFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
            FileInputStream fileInputStream = new FileInputStream(argFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
            byte data[] = new byte[BUFFER];
            int count;
            while ((count = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
                zipOutputStream.write(data, 0, count);
            }
            bufferedInputStream.close();
        } finally {
            zipOutputStream.close();
            fileOutputStream.close();
        }
    }

    private static void directoryToZip(File argDirectory, File argZipFile) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(argZipFile);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            outToZipStream(argDirectory, zipOutputStream, "");
        } finally {
            zipOutputStream.close();
            fileOutputStream.close();
        }
    }

    private static void outToZipStream(File argDirectory, ZipOutputStream argZipOut, String argPath) throws IOException {
        File file[] = argDirectory.listFiles();
        if (file == null) return;
        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory()) {
                String t = argPath + file[i].getName() + "/";
                ZipEntry zipEntry = new ZipEntry(t);
                argZipOut.putNextEntry(zipEntry);
                outToZipStream(file[i], argZipOut, t);
            } else {
                ZipEntry zipEntry = new ZipEntry(argPath + file[i].getName());
                argZipOut.putNextEntry(zipEntry);
                FileInputStream fileInputStream = new FileInputStream(file[i]);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
                byte data[] = new byte[BUFFER];
                int count;
                while ((count = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
                    argZipOut.write(data, 0, count);
                }
                bufferedInputStream.close();
            }
        }
    }

    /**
     * 根据请求路径取得相应配置对象.
     * TODO:如果使用struts,需要注意后缀,
     *      比如在struts中配置的的action的path是*.template,
     *      请求路径是/java.patterns.adapter.template.tiles,
     *      程序会处理掉.tiles取得java.patterns.adapter.template
     *      但是.template实际上是action的后缀,所以使用struts时,
     *      应该把.template也去掉,得到java.patterns.adapter
     * @param pageContext
     * @return
     * @throws JspException
     */
    public static Object processMapping(PageContext pageContext) throws JspException, UnavailableException {
        ServletContext context = pageContext.getServletContext();
        ConfigLoadHelper helper = ConfigLoadHelper.getInstance();
        if (!helper.isLoaded(context)) {
            String paths = pageContext.getServletConfig().getInitParameter("config");
            helper.loadConfig(context, paths);
        }
        String requestPath = processPath(pageContext);
        String expression = "${" + ConfigLoadHelper.TEMPLATES_CONFIG_APPLICATION_KEY + "." + requestPath + "}";
        PatternConfig patternConfig = null;
        try {
            patternConfig = (PatternConfig) evaluate(expression, pageContext);
        } catch (Exception e) {
        }
        return patternConfig;
    }

    /**
     * <p>Identify and return the path component (from the request URI) that
     * we will use to select an <code>ActionMapping</code> with which to dispatch.
     * If no such path can be identified, create an error response and return
     * <code>null</code>.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     */
    private static String processPath(PageContext pageContext) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String path = null;
        path = (String) request.getAttribute("javax.servlet.include.path_info");
        if (path == null) {
            path = request.getPathInfo();
        }
        if ((path != null) && (path.length() > 0)) {
            return (path);
        }
        path = (String) request.getAttribute("javax.servlet.include.servlet_path");
        if (path == null) {
            path = request.getServletPath();
        }
        int slash = path.lastIndexOf("/");
        int period = path.lastIndexOf(".");
        if ((period >= 0) && (period > slash)) {
            path = path.substring(slash + 1, period);
        }
        return (path);
    }

    /**
     * 判定指定的对象是否为空.
     * 
     * null:返回true
     * String实例:""返回true
     * Collection实例:集合中没有元素存在时返回true
     * Map实例:集合中没有元素存在时返回true
     * 
     * @param o
     * @return
     * @throws JspException
     */
    public static boolean isEmpty(Object o) throws JspException {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            if ("".equals(o)) {
                return true;
            }
            return false;
        } else if (o instanceof Collection) {
            return ((Collection) o).isEmpty();
        } else if (o instanceof Map) {
            return ((Map) o).isEmpty();
        } else {
            return false;
        }
    }
}
