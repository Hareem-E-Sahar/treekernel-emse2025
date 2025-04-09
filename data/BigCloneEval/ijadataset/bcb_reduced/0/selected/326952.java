package jp.lnc.DirectxMeshLoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.lnc.MeshLoader.PanMesh;

public class DirectXMeshLoder {

    static Matcher matcher;

    public static XfileMeshTree topMesh = new XfileMeshTree();

    static List<XfileMeshTree> templateList = new ArrayList<XfileMeshTree>();

    protected Pattern spSplit = Pattern.compile(" ");

    protected final Pattern repCRpttern = Pattern.compile("[ \n\r\t]+");

    protected final Pattern keyWord = Pattern.compile("Frame|template|FrameTransformMatrix|AnimationKey|template");

    protected Pattern templatePttern;

    protected final String[] sToken = { "Frame", "template", "FrameTransformMatrix", "AnimationKey" };

    static XfileMeshTree mesh = topMesh;

    protected enum eTypeNo {

        Frame, template, FrameTransformMatrix, AnimationKey
    }

    ;

    public static XType xType = new XType();

    /**
	 * ファイルクラスからPanMeshを作成
	 * 実体はPanMesh XMeshLoader (Reader input)
	 * @param file 読み込みファイル
	 * @return PanMesh
	 */
    public static PanMesh XMeshLoader(File file) {
        FileReader fis;
        PanMesh ret = null;
        try {
            fis = new FileReader(file);
            ret = XMeshLoader(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static void genMesh(PanMesh panMesh) {
        topMesh.meshCompile(panMesh);
    }

    /**
	 * テンプレートを読み取りやすい形式に
	 */
    private static void tmplateCompile() {
        for (int i = 0; i < templateList.size(); i++) {
            XfileMeshTree bean = templateList.get(i);
            bean.compile();
        }
    }

    public static BufferedReader XReader;

    /**
	 * ReaderからPanMeshを作成
	 * 
	 * @param file 読み込みファイル
	 * @return PanMesh
	 */
    public static PanMesh XMeshLoader(Reader input) {
        XReader = new BufferedReader(input);
        PanMesh panMesh = new PanMesh();
        try {
            topMesh.addTmplateList(templateList);
            topMesh.xType.typeNo = 0;
            topMesh.xType.setName("Top");
            paeseSection(panMesh);
            tmplateCompile();
            genMesh(panMesh);
            panMesh.printMesh();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return panMesh;
    }

    private static void paeseSection(PanMesh panMesh) throws IOException {
        String tmp;
        while (XReader.ready()) {
            tmp = XReader.readLine();
            XLinePaser(tmp);
        }
    }

    public static MatchResult object;

    public static String tail = "";

    static void XLinePaser(String str) {
        int startIndex = 0;
        Pattern pattern = Pattern.compile("[;{}]", 1);
        matcher = pattern.matcher(tail + str);
        while (matcher.find()) {
            object = matcher.toMatchResult();
            if (matcher.group().equalsIgnoreCase("}")) {
                mesh.addString(str.subSequence(startIndex, matcher.start()));
                mesh = mesh.getUp();
            } else if (matcher.group().equalsIgnoreCase("{")) {
                xType = new XType();
                xType.setTypeFromString(str.subSequence(startIndex, matcher.start()));
                mesh = mesh.addNewSubTree(xType);
            } else if (matcher.group().equalsIgnoreCase(";")) {
                mesh.addString(str.subSequence(startIndex, matcher.end()));
            }
            startIndex = matcher.end();
        }
        if (startIndex != matcher.regionEnd()) mesh.addString(str.subSequence(startIndex, matcher.regionEnd()));
    }
}
