package aurora.ide.search.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uncertain.composite.CompositeMap;
import uncertain.schema.Attribute;
import uncertain.schema.Element;
import uncertain.schema.IType;
import uncertain.schema.SimpleType;
import aurora.ide.bm.BMUtil;
import aurora.ide.builder.BuildContext;
import aurora.ide.editor.textpage.ColorManager;
import aurora.ide.editor.textpage.IColorConstants;
import aurora.ide.editor.textpage.scanners.XMLTagScanner;
import aurora.ide.helpers.ApplicationException;
import aurora.ide.helpers.CompositeMapUtil;
import aurora.ide.project.propertypage.ProjectPropertyPage;

public class Util {

    private static XMLTagScanner tagScanner;

    public static XMLTagScanner getXMLTagScanner() {
        if (tagScanner == null) {
            ColorManager manager = new ColorManager();
            tagScanner = new XMLTagScanner(manager);
            tagScanner.setDefaultReturnToken(new Token(new TextAttribute(manager.getColor(IColorConstants.TAG))));
        }
        return tagScanner;
    }

    public static List getMapAttributes(CompositeMap map) {
        Element element = CompositeMapUtil.getElement(map);
        if (element != null) return element.getAllAttributes();
        return null;
    }

    public static IRegion getAttributeRegion(int offset, int length, String name, IDocument document) throws BadLocationException {
        return getDocumentRegion(offset, length, name, document, IColorConstants.ATTRIBUTE);
    }

    /**
	 * String s = "<a replace />"; s =
	 * s.replace("replace",this.getReplaceWith()); Util.checkXMLForm(s);
	 * 用来检查xml是否是格式良好的。
	 */
    public static boolean checkXMLForm(String s) {
        SAXParserFactory parser_factory = SAXParserFactory.newInstance();
        parser_factory.setNamespaceAware(false);
        parser_factory.setValidating(false);
        SAXParser parser = null;
        InputStream is = null;
        try {
            parser = parser_factory.newSAXParser();
            is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            parser.parse(is, new DefaultHandler());
            return true;
        } catch (UnsupportedEncodingException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static IRegion getDocumentRegion(int offset, int length, String name, IDocument document, RGB reginRGB) throws BadLocationException {
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                int tokenOffset = scanner.getTokenOffset();
                int tokenLength = scanner.getTokenLength();
                if (text.getForeground().getRGB().equals(reginRGB) && name.equalsIgnoreCase(document.get(tokenOffset, tokenLength))) {
                    return new Region(tokenOffset, tokenLength);
                }
            }
        }
        return null;
    }

    public static IRegion getValueRegion(int offset, int length, String value, IDocument document, RGB reginRGB) throws BadLocationException {
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                int tokenOffset = scanner.getTokenOffset();
                int tokenLength = scanner.getTokenLength();
                if (text.getForeground().getRGB().equals(reginRGB) && value.equals(document.get(tokenOffset + 1, tokenLength - 2))) {
                    return new Region(tokenOffset, tokenLength);
                }
            }
        }
        return null;
    }

    public static List<IRegion> getValueRegions(int offset, int length, IDocument document) throws BadLocationException {
        List<IRegion> result = new ArrayList<IRegion>();
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                int tokenOffset = scanner.getTokenOffset();
                int tokenLength = scanner.getTokenLength();
                if (text.getForeground().getRGB().equals(IColorConstants.STRING)) {
                    result.add(new Region(tokenOffset, tokenLength));
                }
            }
        }
        return result;
    }

    public static IRegion getValueRegion(int offset, int length, int hintOffset, IDocument document, RGB reginRGB) throws BadLocationException {
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                int tokenOffset = scanner.getTokenOffset();
                int tokenLength = scanner.getTokenLength();
                if (text.getForeground().getRGB().equals(reginRGB) && (tokenOffset <= hintOffset && hintOffset <= tokenOffset + tokenLength)) {
                    return new Region(tokenOffset, tokenLength);
                }
            }
        }
        return null;
    }

    public static IRegion getValuePartRegion(int offset, int length, String name, IDocument document, RGB reginRGB) throws BadLocationException {
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                int tokenOffset = scanner.getTokenOffset();
                int tokenLength = scanner.getTokenLength();
                if (text.getForeground().getRGB().equals(reginRGB)) {
                    if (name.length() == 0) return new Region(tokenOffset, tokenLength);
                    int index = document.get(tokenOffset, tokenLength).indexOf(name);
                    if (index != -1) {
                        return new Region(tokenOffset + index, name.length());
                    }
                }
            }
        }
        return null;
    }

    public static IRegion getFirstWhitespaceRegion(int offset, int length, IDocument document) throws BadLocationException {
        XMLTagScanner scanner = getXMLTagScanner();
        IToken token = null;
        IRegion c_region = null;
        scanner.setRange(document, offset, length);
        while ((token = scanner.nextToken()) != Token.EOF) {
            int tokenOffset = scanner.getTokenOffset();
            int tokenLength = scanner.getTokenLength();
            if (Token.WHITESPACE.equals(token)) {
                c_region = new Region(tokenOffset, tokenLength);
            }
            if (token.getData() instanceof TextAttribute) {
                TextAttribute text = (TextAttribute) token.getData();
                if (text.getForeground().getRGB().equals(IColorConstants.ATTRIBUTE)) {
                    return c_region;
                }
            }
        }
        return null;
    }

    public static Object getReferenceModelPKG(CompositeMap map) {
        if (map == null) return null;
        Element element = CompositeMapUtil.getElement(map);
        if (element != null) {
            List attrib_list = element.getAllAttributes();
            for (Iterator it = attrib_list.iterator(); it.hasNext(); ) {
                Attribute attrib = (Attribute) it.next();
                IType attributeType = attrib.getAttributeType();
                boolean referenceOf = isBMReference(attributeType);
                if (referenceOf) {
                    return Util.getValueIgnoreCase(attrib, map);
                }
            }
        }
        return null;
    }

    public static boolean isBMReference(IType attributeType) {
        if (attributeType instanceof SimpleType) {
            return AbstractSearchService.bmReference.equals(((SimpleType) attributeType).getReferenceTypeQName());
        }
        return false;
    }

    private static class WebInfFinder implements IResourceVisitor {

        private IFolder folder = null;

        public boolean visit(IResource resource) throws CoreException {
            if (folder != null) return false;
            if (resource.getType() == IResource.FOLDER) {
                if ("WEB-INF".equals(resource.getName())) {
                    folder = (IFolder) resource;
                    return false;
                }
                return true;
            }
            if (resource.getType() == IResource.FILE) {
                return false;
            }
            return true;
        }

        public IFolder getFolder() {
            return folder;
        }
    }

    public static IContainer findWebInf(IResource resource) {
        if (null == resource) {
            return null;
        }
        IFolder webINF = null;
        IProject project = resource.getProject();
        try {
            String web = project.getPersistentProperty(ProjectPropertyPage.WebQN);
            if (web != null) {
                IPath webINFPath = new Path(web).append("WEB-INF");
                webINF = project.getParent().getFolder(webINFPath);
            }
        } catch (CoreException e1) {
        }
        if (webINF != null && webINF.exists()) {
            return webINF;
        }
        try {
            WebInfFinder finder = new WebInfFinder();
            project.accept(finder);
            return finder.getFolder();
        } catch (CoreException e) {
        }
        return null;
    }

    public static IContainer findBMHome(IResource resource) {
        if (null == resource) {
            return null;
        }
        IFolder bmHome = null;
        IProject project = resource.getProject();
        try {
            String bm = project.getPersistentProperty(ProjectPropertyPage.BMQN);
            IPath bmPath = new Path(bm);
            bmHome = project.getParent().getFolder(bmPath);
        } catch (CoreException e1) {
        }
        if (bmHome != null && bmHome.exists()) {
            return bmHome;
        }
        IContainer webINF = findWebInf(resource);
        if (webINF != null && webINF.exists()) {
            IFolder classes = webINF.getFolder(new Path("classes"));
            return classes.exists() ? classes : null;
        }
        return null;
    }

    public static IResource getScope(IResource sourceFile) {
        if (sourceFile == null) return null;
        IProject project = sourceFile.getProject();
        IContainer scope = Util.findWebInf(sourceFile);
        if (scope == null) {
            scope = project;
        } else {
            scope = scope.getParent();
        }
        return scope;
    }

    public static IFile findBMFile(CompositeMap map) {
        Object pkg = Util.getReferenceModelPKG(map);
        if (pkg == null) {
            pkg = Util.getReferenceModelPKG(map.getParent());
        }
        if (pkg == null) {
            pkg = Util.getReferenceModelPKG(map.getParent().getParent());
        }
        if (pkg instanceof String) {
            return findBMFileByPKG(pkg);
        }
        return null;
    }

    public static IFile findBMFileByPKG(Object pkg) {
        try {
            IResource file = BMUtil.getBMResourceFromClassPath((String) pkg);
            if (file instanceof IFile && "bm".equalsIgnoreCase(file.getFileExtension())) return (IFile) file;
        } catch (ApplicationException e) {
        }
        return null;
    }

    public static IFile findScreenFile(IFile file, Object pkg) {
        if (pkg instanceof String) {
            IContainer webInf = findWebInf(file);
            if (webInf == null) return null;
            IResource webRoot = webInf.getParent();
            IContainer parent = file.getParent();
            IPath parentPath = parent.getFullPath();
            IPath rootPath = webRoot.getFullPath();
            IPath path = new Path((String) pkg);
            IPath requestPath = new Path("${/request/@context_path}");
            boolean prefixOfRequest = requestPath.isPrefixOf(path);
            if (prefixOfRequest) {
                path = path.makeRelativeTo(requestPath);
            }
            String[] split = path.toString().split("\\?");
            if (split == null || split.length == 0) return null;
            path = new Path(split[0]);
            if (path.segmentCount() == 0) {
                return null;
            }
            IPath relativePath = parentPath.makeRelativeTo(rootPath);
            boolean prefixOf = relativePath.isPrefixOf(path);
            if (prefixOf || prefixOfRequest) {
                IPath sourceFilePath = rootPath.append(path);
                if (sourceFilePath.segmentCount() < 2) return null;
                IFile sourceFile = file.getProject().getParent().getFile(sourceFilePath);
                if (sourceFile.exists()) return sourceFile;
            } else {
                IFile sourceFile = parent.getFile(path);
                if (sourceFile.exists()) return sourceFile;
            }
            IPath sourceFilePath = rootPath.append(path);
            if (sourceFilePath.segmentCount() < 2) return null;
            IFile sourceFile = file.getProject().getParent().getFile(sourceFilePath);
            if (sourceFile.exists()) return sourceFile;
        }
        return null;
    }

    public static String findScreenUrl(IFile file, Object pkg) {
        if (pkg instanceof String) {
            IPath path = new Path((String) pkg);
            IPath requestPath = new Path("${/request/@context_path}");
            boolean prefixOfRequest = requestPath.isPrefixOf(path);
            if (prefixOfRequest) {
                path = path.makeRelativeTo(requestPath);
            }
            String[] split = path.toString().split("\\?");
            if (split == null || split.length == 0) return "";
            path = new Path(split[0]);
            if (path.segmentCount() == 0) {
                return "";
            }
            return path.toString();
        }
        return "";
    }

    public static boolean stringMatch(String pattern, String text, boolean isCaseSensitive, boolean isRegularExpression) {
        if (text == null) {
            return false;
        }
        Pattern jdkPattern = PatternConstructor.createPattern(pattern, isCaseSensitive, isRegularExpression);
        Matcher matcher = jdkPattern.matcher(text);
        return matcher.matches();
    }

    public static String toBMPKG(IFile file) {
        IPath path = file.getProjectRelativePath().removeFileExtension();
        return toPKG(path);
    }

    public static String toPKG(IPath path) {
        String[] segments = path.segments();
        StringBuilder result = new StringBuilder();
        StringBuilder _result = new StringBuilder();
        int classes_idx = -1;
        for (int i = 0; i < segments.length; i++) {
            _result.append(segments[i]);
            if (i != segments.length - 1) _result.append(".");
            if (classes_idx != -1) {
                result.append(segments[i]);
                if (i != segments.length - 1) result.append(".");
            }
            if ("classes".equals(segments[i])) {
                classes_idx = i;
            }
        }
        if (result.length() == 0) {
            result = _result;
        }
        return result.toString();
    }

    public static String toRelativeClassesPKG(IPath path) {
        String[] segments = path.segments();
        StringBuilder result = new StringBuilder();
        int classes_idx = -1;
        for (int i = 0; i < segments.length; i++) {
            if (classes_idx != -1) {
                result.append(segments[i]);
                if (i != segments.length - 1) result.append(".");
            }
            if ("classes".equals(segments[i])) {
                classes_idx = i;
            }
        }
        return result.toString();
    }

    public static boolean bmRefMatch(Object bmPattern, String url) {
        Path path = new Path(url);
        String[] segments = path.segments();
        for (String s : segments) {
            String[] split = s.split("\\?");
            if (split == null || split.length == 0) return false;
            s = split[0];
            if (s.equals(bmPattern)) {
                return true;
            }
        }
        return false;
    }

    public static String getBmAction(Object bmPattern, String url) {
        boolean findModel = false;
        Path path = new Path(url);
        String[] segments = path.segments();
        for (String s : segments) {
            String[] split = s.split("\\?");
            if (split == null || split.length == 0) return "";
            s = split[0];
            if (findModel) {
                return s;
            }
            if (s.equals(bmPattern)) {
                findModel = true;
            }
        }
        return "";
    }

    public static String getUrlLeftString(Object bmPattern, String url) {
        int indexOf = url.indexOf("?");
        if (indexOf != -1) {
            return "'" + url.substring(indexOf) + "'";
        }
        return "";
    }

    public static String getUrlComment(Object bmPattern, String url) {
        int indexOf = url.indexOf("?");
        if (indexOf != -1) {
            return "/*" + url.substring(0, indexOf) + "*/";
        }
        return "/*" + url + "*/";
    }

    public static String convertJS(String source) {
        if (source == null) return null;
        Pattern ptn = Pattern.compile("\\$\\{[^}]+\\}");
        Matcher m = ptn.matcher(source);
        char[] charArray = source.toCharArray();
        while (m.find()) {
            Arrays.fill(charArray, m.start(), m.end(), '1');
        }
        return new String(charArray);
    }

    public static String getValueIgnoreCase(Attribute a, BuildContext bc) {
        return getValueIgnoreCase(a, bc.map);
    }

    public static String getValueIgnoreCase(Attribute a, CompositeMap cMap) {
        String name = a.getName();
        return getValueIgnoreCase(cMap, name);
    }

    public static String getValueIgnoreCase(CompositeMap cMap, String name) {
        Set keySet = cMap.keySet();
        for (Object object : keySet) {
            if (object instanceof String && ((String) object).equalsIgnoreCase(name)) {
                return cMap.getString(object);
            }
        }
        return null;
    }

    public static String getPKG(IPath path) {
        String fileExtension = path.getFileExtension();
        if ("bm".equalsIgnoreCase(fileExtension)) {
            return toPKG(path.removeFileExtension());
        }
        if ("screen".equalsIgnoreCase(fileExtension)) {
            return path.toString();
        }
        return "";
    }
}
