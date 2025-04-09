package at.ssw.coco.ide.features.semanticHighlighting;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import at.ssw.coco.builder.Activator;
import at.ssw.coco.core.CoreUtilities;
import at.ssw.coco.ide.IdeUtilities;
import at.ssw.coco.ide.editor.ATGEditor;
import at.ssw.coco.lib.model.atgmodel.ATGModel;
import at.ssw.coco.lib.model.atgmodel.ATGSegment;

/**
 * 
 * Utility class for synchronizing java / atg files
 * 
 * @author Markus Koppensteiner <mkoppensteiner@users.sf.net>
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 * 
 */
public class DocumentSynchronizer {

    /** scans a production and adds found semantic actions */
    private class ProductionScanner {

        private char EOF = (char) -1;

        private int curPos, atgEndPos, javaStartPos;

        private char ch;

        private char last;

        private boolean sa = false;

        private boolean str = false;

        private boolean chr = false;

        private boolean slc = false;

        private boolean ilc = false;

        int mlc = 0;

        private boolean openpar = false;

        private int semActBegin = -1, semActEnd = -1;

        ProductionScanner(int atgStartPos, int atgEndPos, int javaStartPos) {
            this.atgEndPos = atgEndPos;
            this.javaStartPos = javaStartPos;
            curPos = atgStartPos - 1;
            nextCh();
        }

        void scan() throws BadLocationException {
            while (ch != EOF) {
                if (slc) {
                    if (ch == '\n' || ch == '\r') {
                        slc = false;
                    }
                } else if (mlc > 0) {
                    if (last == '*' && ch == '/') {
                        mlc--;
                    } else if (last == '/' && ch == '*') {
                        mlc++;
                    }
                } else if (str) {
                    if (ch == '"') {
                        str = false;
                    }
                } else if (chr) {
                    if (ch == '\'') {
                        chr = false;
                    }
                } else {
                    switch(ch) {
                        case '/':
                            if (last == '/') {
                                slc = true;
                            }
                            break;
                        case '*':
                            if (last == '/') {
                                mlc++;
                            }
                            break;
                        case '"':
                            str = true;
                            break;
                        case '\'':
                            chr = true;
                            break;
                        case '.':
                            if (last == '(') {
                                ilc = true;
                            }
                            break;
                        case ')':
                            if (last == '.') {
                                semActEnd = curPos - 1;
                                javaStartPos = addSemanticRegion(semActBegin, semActEnd - semActBegin, javaStartPos);
                                sa = false;
                                ilc = false;
                            }
                            break;
                        case '\n':
                            if (sa) {
                                semActEnd = curPos;
                                javaStartPos = addSemanticRegion(semActBegin, semActEnd - semActBegin, javaStartPos);
                                openpar = true;
                            }
                        default:
                            if (ilc && !sa) {
                                if (Character.isWhitespace(ch)) {
                                    break;
                                } else {
                                    semActBegin = curPos;
                                    sa = true;
                                }
                            }
                            if (openpar) {
                                if (Character.isWhitespace(ch)) {
                                    break;
                                }
                                semActBegin = curPos;
                                openpar = false;
                            }
                            break;
                    }
                }
                last = ch;
                nextCh();
            }
        }

        private void nextCh() {
            curPos++;
            if (curPos < atgEndPos) {
                try {
                    ch = atgDocument.getChar(curPos);
                } catch (BadLocationException e) {
                    IdeUtilities.logError(e.getMessage(), e);
                }
            } else {
                ch = EOF;
            }
        }
    }

    /** The Editor */
    private ATGEditor fEditor;

    /** The java file corresponding to the atg file */
    private StringBuffer javaFile;

    /** The used atg file */
    private IDocument atgDocument;

    /** The used frame file */
    private StringBuffer frameFile;

    /** The abstract syntax tree parsed from the java file */
    private CompilationUnit ast;

    /** The last production scanned */
    private String lastProductionName = "";

    /** base position where imports begin in the java file */
    private int importsBegin;

    /** base position where imports end in the java file */
    private int importsEnd;

    /** base position where declarations begin in the java file */
    private int declBegin;

    /** base position where declarations end in the java file */
    private int declEnd;

    /** base position where productions begin in the java file */
    private int productionsBegin;

    /** base position where productions end in the java file */
    private int productionsEnd;

    /** The imports segment of the atg file */
    private ATGSegment importsSegment;

    /** The declarations segment of the atg file */
    private ATGSegment declSegment;

    /** All production segments of the atg file */
    private ATGSegment[] productionsSegments;

    /** Regions in the java file corresponding to semantic actions */
    private List<IRegion> semanticRegions;

    /**
	 * maps a semantic region of the java file to the corresponding position in
	 * the atg file
	 */
    private Map<IRegion, Integer> atgStartMap;

    /**
	 * 
	 * @param editor
	 *            the used ATGEditor
	 * @param sourceViewer
	 *            the used SourceViewer
	 */
    public DocumentSynchronizer(ATGEditor editor, ISourceViewer sourceViewer) {
        fEditor = editor;
        atgDocument = sourceViewer.getDocument();
        javaFile = new StringBuffer();
        compileJavaFile();
        setFrameFile();
        calcSegments();
        createAst();
        semanticRegions = new ArrayList<IRegion>();
        atgStartMap = new HashMap<IRegion, Integer>();
    }

    /**
	 * 
	 * @return The abstract syntax tree, parsed from the java file
	 */
    public CompilationUnit getAst() {
        return ast;
    }

    /**
	 * parses the abstract syntax tree from the actual java file
	 */
    private void createAst() {
        IFile editorFile = (IFile) fEditor.getEditorInput().getAdapter(IFile.class);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(javaFile.toString().toCharArray());
        parser.setProject(JavaCore.create(editorFile.getProject()));
        parser.setUnitName("Parser.java");
        parser.setResolveBindings(true);
        ast = (CompilationUnit) parser.createAST(null);
    }

    /**
	 * @param offset
	 *            the position in the PARSER_CODE Partitin of the atg file
	 * @return the corresponding position in the java file 
	 */
    public int mapToJava(int offset) {
        documentChanged();
        StringBuffer atgFile = new StringBuffer(atgDocument.get());
        String declarations = javaFile.substring(declBegin, declEnd);
        int startPos = atgFile.indexOf(declarations);
        int relPos = offset - startPos;
        int javaPos = declBegin + relPos;
        return javaPos;
    }

    /**
	 * 
	 * @param offset
	 *            the position in the java file
	 * @param node
	 *            the ASTNode at the specified offset
	 * @return the corresponding position in the atg file or -1 if the node does
	 *         not correspond to any position in the atg file
	 */
    public int mapToATG(int offset, ASTNode node) {
        int res = -1;
        if (offset >= importsBegin && offset < importsEnd) {
            if (importsSegment != null) res = offset - importsBegin + importsSegment.getRegion().getOffset();
        } else if (offset >= declBegin && offset < declEnd) {
            res = offset - declBegin + declSegment.getRegion().getOffset();
        } else if (offset >= productionsBegin && offset < productionsEnd) {
            if (productionsSegments.length == 0) return res;
            MethodDeclaration method;
            ASTNode n = node;
            while (n.getNodeType() != ASTNode.METHOD_DECLARATION) {
                n = n.getParent();
            }
            method = (MethodDeclaration) n;
            String methodName = method.getName().toString();
            if (!methodName.equals(lastProductionName)) {
                int i = 0;
                while (!productionsSegments[i].getName().equals(method.getName().toString())) {
                    if (++i >= productionsSegments.length) return res;
                }
                try {
                    calcSemanticRegions(method, productionsSegments[i]);
                } catch (BadLocationException e) {
                    IdeUtilities.logError(e.getMessage(), e);
                }
                lastProductionName = methodName;
            }
            IRegion r = getSemanticRegion(offset);
            if (r != null) {
                res = atgStartMap.get(r) + node.getStartPosition() - r.getOffset();
            }
        }
        return res;
    }

    /**
	 * calculates base positions needed for mapping
	 */
    private void calcSegments() {
        lastProductionName = "";
        importsBegin = 0;
        importsEnd = 0;
        declBegin = 0;
        declEnd = 0;
        productionsBegin = 0;
        productionsEnd = 0;
        String declarations = null, imports = null, productions = null;
        ATGModel m = fEditor.getATGModelProvider().getATGModel();
        if (m != null) {
            importsSegment = m.getImports();
            declSegment = m.getDeclarations();
            productionsSegments = m.getProductions();
        } else {
            importsSegment = null;
            declSegment = null;
            productionsSegments = null;
        }
        String preCode, postCode;
        int segmentStart, segmentEnd;
        if (importsSegment != null) {
            segmentStart = 0;
            segmentEnd = frameFile.indexOf("-->begin");
            segmentStart = segmentEnd + "-->begin".length();
            segmentEnd = frameFile.indexOf("-->constants");
            postCode = frameFile.substring(segmentStart, segmentEnd);
            imports = javaFile.substring(0, javaFile.indexOf(postCode));
            importsBegin = javaFile.indexOf(imports);
            importsEnd = importsBegin + imports.length();
        }
        if (declSegment != null) {
            segmentStart = frameFile.indexOf("-->constants") + "-->constants".length();
            segmentEnd = frameFile.indexOf("-->declarations");
            preCode = frameFile.substring(segmentStart, segmentEnd);
            segmentStart = segmentEnd + "-->declarations".length();
            segmentEnd = frameFile.indexOf("-->pragmas");
            postCode = frameFile.substring(segmentStart, segmentEnd);
            declarations = javaFile.substring(javaFile.indexOf(preCode) + preCode.length(), javaFile.indexOf(postCode));
            declBegin = javaFile.indexOf(declarations);
            declEnd = declBegin + declarations.length();
        }
        if (productionsSegments != null) {
            segmentStart = frameFile.indexOf("-->pragmas") + "-->pragmas".length();
            segmentEnd = frameFile.indexOf("-->productions");
            preCode = frameFile.substring(segmentStart, segmentEnd);
            segmentStart = segmentEnd + "-->productions".length();
            segmentEnd = frameFile.indexOf("-->parseRoot");
            postCode = frameFile.substring(segmentStart, segmentEnd);
            productions = javaFile.substring(javaFile.indexOf(preCode) + preCode.length(), javaFile.indexOf(postCode));
            productionsBegin = javaFile.indexOf(productions);
            productionsEnd = productionsBegin + productions.length();
        }
    }

    /**
	 * initiates all necessary actions when the atg file was changed
	 */
    public void documentChanged() {
        compileJavaFile();
        createAst();
        calcSegments();
    }

    /**
	 * Generate temporary parser file from the current atg
	 */
    private void compileJavaFile() {
        InputStream s = new ByteArrayInputStream(atgDocument.get().getBytes());
        IFile editorFile = (IFile) fEditor.getEditorInput().getAdapter(IFile.class);
        String srcName = editorFile.getLocation().toString();
        String srcDir = editorFile.getParent().getLocation().toString();
        String outDir = System.getProperty("java.io.tmpdir") + Path.SEPARATOR + "coco temp";
        String frameDir = editorFile.getParent().getLocation().toString();
        String nsName = "";
        File temp = new File(outDir);
        temp.mkdir();
        CoreUtilities.executeCoco(s, srcName, srcDir, outDir, frameDir, nsName);
        BufferedReader reader;
        javaFile = new StringBuffer();
        String line;
        try {
            reader = new BufferedReader(new FileReader(temp.getAbsolutePath() + Path.SEPARATOR + "Parser.java"));
            while ((line = reader.readLine()) != null) {
                javaFile.append(line);
                javaFile.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
	 * initiates all necessary actions when the atg file was exchanged
	 */
    protected void inputDocumentChanged() {
        setFrameFile();
    }

    /**
	 * sets the frame file corresponding to the atg file
	 */
    private void setFrameFile() {
        StringBuffer src = new StringBuffer();
        BufferedReader reader;
        String line;
        IFile editorFile = (IFile) fEditor.getEditorInput().getAdapter(IFile.class);
        try {
            String frameDir = getFrameDir(editorFile);
            reader = new BufferedReader(new FileReader(frameDir + Path.SEPARATOR + "Parser.frame"));
            while ((line = reader.readLine()) != null) {
                src.append(line);
                src.append("\n");
            }
        } catch (IOException e) {
            IdeUtilities.logError(e.getMessage(), e);
        } catch (CoreException e) {
            IdeUtilities.logError(e.getMessage(), e);
        }
        frameFile = src;
    }

    /**
	 * 
	 * @param editorFile
	 *            the ATGEditors current file
	 * @return the absolute path to the directory containing the frame files
	 * @throws CoreException
	 */
    private String getFrameDir(IFile editorFile) throws CoreException {
        String frameDir = "";
        frameDir = editorFile.getPersistentProperty(Activator.CUSTOM_COCO_FRAMES_DIR);
        return computeAbsolutePath(frameDir, editorFile);
    }

    /**
	 * 
	 * @param dir
	 *            the directory
	 * @param editorFile
	 *            the ATGEditors current file
	 * @return an absolute path
	 */
    private String computeAbsolutePath(String dir, IFile editorFile) {
        if (dir == null || dir.equals("")) {
            dir = editorFile.getParent().getLocation().toString();
        } else {
            IPath path = Path.fromOSString(dir);
            if (!path.isAbsolute()) {
                dir = editorFile.getParent().getLocation().toString() + Path.SEPARATOR + dir;
            }
        }
        return dir;
    }

    /**
	 * stores a region of the java file that corresponds to a semantic action
	 * 
	 * @param offset
	 *            the offset of the semantic action in the atg file
	 * @param length
	 *            the length of the semantic action
	 * @param javaSearchStart
	 *            the nearest known position in the java file
	 * @return the end position of the region stored
	 * @throws BadLocationException
	 */
    private int addSemanticRegion(int offset, int length, int javaSearchStart) throws BadLocationException {
        String semAct = atgDocument.get(offset, length);
        int javaStartPos = javaFile.indexOf(semAct, javaSearchStart);
        IRegion semanticRegion = new Region(javaStartPos, semAct.length());
        semanticRegions.add(semanticRegion);
        atgStartMap.put(semanticRegion, offset);
        return javaStartPos + semAct.length();
    }

    /**
	 * calculates and stores all semantic action regions of a production
	 * 
	 * @param method
	 *            the method corresponding to the production
	 * @param productionsSegment
	 *            the ATGSegmant corresponding to the production
	 * @throws BadLocationException
	 */
    private void calcSemanticRegions(MethodDeclaration method, ATGSegment productionsSegment) throws BadLocationException {
        semanticRegions.clear();
        atgStartMap.clear();
        int javaStartPos = method.getStartPosition();
        int offset = productionsSegment.getRegion().getOffset();
        int length = productionsSegment.getRegion().getLength();
        ProductionScanner scanner = new ProductionScanner(offset, offset + length, javaStartPos);
        scanner.scan();
    }

    /**
	 * determines if a position corresponds to a semantic action
	 * 
	 * @param offset
	 *            a position of the java file
	 * @return <code>true</code> iff the offset corresponds to a semantic
	 *         action, false otherwise
	 */
    private IRegion getSemanticRegion(int offset) {
        for (int i = 0; i < semanticRegions.size(); i++) {
            IRegion r = semanticRegions.get(i);
            if (offset >= r.getOffset() && offset <= r.getOffset() + r.getLength()) return r;
        }
        return null;
    }
}
