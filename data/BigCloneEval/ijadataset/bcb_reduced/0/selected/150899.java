package org.fao.waicent.xmap2D;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import org.fao.waicent.util.CSVTableReader;
import org.fao.waicent.util.CodeLabelTableSet;
import org.fao.waicent.util.FileResource;
import org.fao.waicent.util.Log;
import org.fao.waicent.util.TableAttributes;
import org.fao.waicent.util.TableInfo;
import org.fao.waicent.util.TableInfoImpl;
import org.fao.waicent.util.TableReader;

public class MIFFeatureLoader extends FeatureLoader {

    final String sPLINE = "PLINE";

    final String sREGION = "REGION";

    final String sMULTIPLE = "MULTIPLE";

    final String sPEN = "PEN";

    final String sSMOOTH = "SMOOTH";

    final String sBRUSH = "BRUSH";

    final String sCENTER = "CENTER";

    final String sLINE = "LINE";

    final String sRECT = "RECT";

    final String sTEXT = "TEXT";

    final String sPOINT = "POINT";

    final String sTRANSFORM = "TRANSFER";

    final String sCOLUMNS = "COLUMNS";

    final String sVERSION = "VERSION";

    static final byte MIFFeatureNull = 0;

    static final byte MIFFeaturePoint = 1;

    static final byte MIFFeatureLine = 3;

    static final byte MIFFeaturePolygon = 5;

    protected int Column_Count = 0;

    protected String[] Column_Name = null;

    protected String[] Column_Type = null;

    int Xmultiplier = 0, Ymultiplier = 0, Xdisplacement = 0, Ydisplacement = 0;

    FeatureVector TextFeatures = new FeatureVector();

    Vector TextCodes = new Vector();

    int version = -1;

    public MIFFeatureLoader(FileResource feature_filename) throws IOException {
        this(feature_filename, -1, -1, null);
    }

    public MIFFeatureLoader(FileResource feature_filename, int code_col, int label_col, Rectangle2D clip) throws IOException {
        super(feature_filename, code_col, label_col);
        load(clip);
    }

    public void load(Rectangle2D clip) throws IOException {
        log = new Log(feature_filename.getResource(), Log.LEVEL_DEBUG);
        InputStream stream = null;
        try {
            stream = feature_filename.openInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            load(in);
            in.close();
            FileResource code_filename = (FileResource) feature_filename.clone();
            code_filename.setExtension(".mid");
            if (features.size() > 0) {
                loadCodes(code_filename);
            }
        } catch (IOException e) {
            log.logAbort("failed to process:\"" + feature_filename + "\"");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    protected void load(BufferedReader in) throws IOException {
        String Str = "";
        byte type = 0;
        features = new FeatureVector();
        MultiPolygonFeature p = null;
        Str = in.readLine();
        while ((Str) != null) {
            double label_x = Double.NEGATIVE_INFINITY;
            double label_y = Double.NEGATIVE_INFINITY;
            Str = Str.toUpperCase();
            if (Str.indexOf(sTRANSFORM) != -1) {
                type = 0;
                int transform_index = Str.trim().indexOf(sTRANSFORM);
                Str = Str.substring(transform_index + sTRANSFORM.length()).trim();
                StringTokenizer tok = new StringTokenizer(Str, ",");
                String Str1 = tok.nextToken();
                String Str2 = tok.nextToken();
                String Str3 = tok.nextToken();
                String Str4 = tok.nextToken();
                Xmultiplier = Integer.valueOf(Str1).intValue();
                Ymultiplier = Integer.valueOf(Str2).intValue();
                Xdisplacement = Integer.valueOf(Str3).intValue();
                Ydisplacement = Integer.valueOf(Str4).intValue();
            } else if (Str.indexOf(sVERSION) != -1) {
                type = 0;
                int version_pos = Str.indexOf(sVERSION);
                Str = Str.substring(version_pos + sVERSION.length()).trim();
                version = Integer.valueOf(Str).intValue();
            } else if (Str.indexOf(sCOLUMNS) != -1) {
                type = 0;
                int column_pos = Str.indexOf(sCOLUMNS);
                Str = Str.substring(column_pos + sCOLUMNS.length()).trim();
                Column_Count = 0;
                try {
                    Column_Count = Integer.valueOf(Str).intValue();
                } catch (NumberFormatException e) {
                    log.logError("Expected Column count found: \"" + Str + "\"", Str);
                }
                Column_Name = new String[Column_Count];
                Column_Type = new String[Column_Count];
                for (int j = 0; j < Column_Count; j++) {
                    String ColumnStr = in.readLine().toUpperCase().trim();
                    if (ColumnStr.indexOf("CHAR") != -1) {
                        Column_Type[j] = "Char";
                    } else if (ColumnStr.indexOf("DECIMAL") != -1) {
                        Column_Type[j] = "Decimal";
                        int pos = ColumnStr.indexOf(" ");
                        if (pos == -1) {
                            log.logError("Expected Column count found: \"" + Str + "\"", Str);
                        } else {
                            String NameStr = ColumnStr.substring(0, pos).trim();
                            Column_Name[j] = NameStr;
                        }
                    }
                }
            } else if (Str.indexOf(sCENTER) != -1) {
                type = 0;
                int label_pos = Str.indexOf(sCENTER);
                Str = Str.substring(label_pos + sCENTER.length()).trim();
                label_pos = Str.indexOf(" ");
                label_x = Double.valueOf(Str.substring(0, label_pos).trim()).doubleValue();
                label_y = Double.valueOf(Str.substring(label_pos + 1, Str.length()).trim()).doubleValue() * -1;
                if (label_x != Double.NEGATIVE_INFINITY && label_y != Double.NEGATIVE_INFINITY) {
                    p.setLabelPoint(new Point2D.Float((float) label_x, (float) label_y));
                } else {
                    log.logError("Expected Center found: \"" + Str + "\"");
                }
            } else if (Str.indexOf("POINT") != -1) {
                type = 1;
            } else if (Str.indexOf("TEXT") != -1) {
                type = 2;
            } else if (Str.indexOf("PLINE") != -1) {
                type = 4;
            } else if (Str.indexOf("LINE") != -1) {
                type = 3;
            } else if (Str.indexOf("REGION") != -1) {
                type = 5;
            } else if (Str.indexOf("RECT") != -1) {
                type = 6;
            } else {
                type = 0;
            }
            switch(type) {
                case 1:
                    feature_type = MIFFeaturePoint;
                    features.add(loadPoint(in, Str));
                    break;
                case 2:
                    feature_type = MIFFeaturePoint;
                    TextFeatures.addElement(loadText(in, Str));
                    break;
                case 3:
                    feature_type = MIFFeatureLine;
                    features.add(loadLine(in, Str));
                    break;
                case 4:
                    feature_type = MIFFeatureLine;
                    features.add(loadPolyLine(in, Str));
                    break;
                case 5:
                    feature_type = MIFFeaturePolygon;
                    p = loadPolygon(in, Str);
                    features.add(p);
                    break;
                case 6:
                    feature_type = MIFFeaturePolygon;
                    features.add(loadRectangle(in, Str));
                    break;
                default:
            }
            Str = in.readLine();
        }
    }

    public LineFeature loadPolyLine(BufferedReader in, String Str) throws IOException {
        GeneralPath line = new GeneralPath();
        String DataStr = Str.trim().toUpperCase();
        int pline_index = DataStr.indexOf(sPLINE);
        if (pline_index == -1) {
            log.logError("Expected PLine found: \"" + Str + "\"");
        } else {
            DataStr = DataStr.substring(pline_index + sPLINE.length());
        }
        int num_parts = 0;
        int multiple_index = DataStr.indexOf(sMULTIPLE);
        if (multiple_index == -1) {
            num_parts = 1;
        } else {
            DataStr = DataStr.substring(multiple_index + sMULTIPLE.length()).trim();
            try {
                num_parts = Integer.valueOf(DataStr).intValue();
            } catch (NumberFormatException e) {
                log.logError("Expected PLine count found: \"" + DataStr + "\"", Str);
            }
            DataStr = in.readLine().trim();
        }
        log.logDebug("PLine parts=" + num_parts);
        for (int i = 0; i < num_parts; i++) {
            log.logDebug("part=" + i);
            int num_points = 0;
            if (DataStr == null) {
                DataStr = in.readLine().trim();
            }
            if ((DataStr.indexOf("PEN") != -1) || (DataStr.indexOf("SMOOTH") != -1)) {
                log.logWarning("Ignored line=\"" + DataStr + "\"");
            } else {
                try {
                    num_points = Integer.valueOf(DataStr.trim()).intValue();
                    log.logDebug(i + ": num_point=" + num_points);
                } catch (NumberFormatException e) {
                    log.logError("Expected number of points in feature: " + i + " found \"" + DataStr + "\"");
                }
                for (int j = 0; j < num_points; j++) {
                    DataStr = in.readLine().trim();
                    if (DataStr.indexOf(sPEN) != -1 || (DataStr.indexOf(sSMOOTH) != -1)) {
                        log.logError("Expected x, y coorninate for feature: " + i + " point " + j + " found \"" + DataStr + "\"");
                    } else {
                        int pos = DataStr.indexOf(' ');
                        if (pos == -1) {
                            log.logError("Expected x, y coorninate for feature: " + i + " point " + j + " found \"" + DataStr + "\"");
                        } else {
                            float x = Float.valueOf(DataStr.substring(0, pos)).floatValue();
                            float y = Float.valueOf(DataStr.substring(pos + 1, DataStr.length())).floatValue() * -1;
                            if (j == 0) {
                                line.moveTo(x, y);
                            } else {
                                line.lineTo(x, y);
                            }
                        }
                        DataStr = null;
                    }
                }
            }
        }
        if (line == null) {
            log.logError("No line found");
            return null;
        } else {
            return (new LineFeature(line));
        }
    }

    public MultiPolygonFeature loadPolygon(BufferedReader in, String Str) throws IOException {
        GeneralPath area_to_add = null;
        GeneralPath area_to_subtract = null;
        GeneralPath ring = null;
        double p_points[] = new double[0];
        double tmp_points[];
        boolean FillMark = true;
        String DataStr = Str.trim().toUpperCase();
        int Polygon_index = DataStr.indexOf(sREGION);
        if (Polygon_index == -1) {
            log.logError("Expected Region found: \"" + Str + "\"");
        } else {
            DataStr = DataStr.substring(Polygon_index + sREGION.length()).trim();
        }
        int num_parts = num_parts = Integer.valueOf(DataStr).intValue();
        int p_count = 0;
        int parts_index[] = new int[num_parts];
        Rectangle2D bounds = null;
        for (int i = 0; i < num_parts; i++) {
            int num_points = 0;
            parts_index[i] = p_count / 2;
            DataStr = in.readLine().trim();
            if (((DataStr.indexOf(sPEN) != -1) || (DataStr.indexOf(sCENTER) != -1)) | (DataStr.indexOf(sBRUSH) != -1)) {
                log.logWarning("Ignored line=\"" + DataStr + "\"");
            } else {
                try {
                    num_points = Integer.valueOf(DataStr.trim()).intValue();
                    tmp_points = p_points;
                    p_points = new double[tmp_points.length + num_points * 2];
                    System.arraycopy(tmp_points, 0, p_points, 0, tmp_points.length);
                } catch (NumberFormatException e) {
                    log.logError("Expected number of points in feature: " + i + " found \"" + DataStr + "\"");
                }
                for (int j = 0; j < num_points; j++) {
                    DataStr = in.readLine().trim();
                    if ((DataStr.indexOf(sPEN) != -1) || (DataStr.indexOf(sCENTER) != -1) || (DataStr.indexOf(sBRUSH) != -1)) {
                        log.logError("Expected x, y coorninate for feature: " + i + " point " + j + " found \"" + DataStr + "\"");
                    } else {
                        int pos = DataStr.indexOf(' ');
                        if (pos == -1) {
                            log.logError("Expected x, y coorninate for feature: " + i + " point " + j + " found \"" + DataStr + "\"");
                        } else {
                            float x = Float.valueOf(DataStr.substring(0, pos)).floatValue();
                            float y = Float.valueOf(DataStr.substring(pos + 1, DataStr.length())).floatValue() * -1;
                            p_points[p_count++] = x;
                            p_points[p_count++] = y;
                            if (bounds == null) {
                                bounds = new Rectangle2D.Double(x, y, 0, 0);
                            } else {
                                bounds.add(x, y);
                            }
                            if (j == 0) {
                                ring = new GeneralPath(GeneralPath.WIND_NON_ZERO, num_points);
                                ring.moveTo(x, y);
                            } else {
                                ring.lineTo(x, y);
                            }
                        }
                        DataStr = null;
                    }
                }
                if (ring == null) {
                    log.logError("No polygon found");
                    return null;
                } else {
                    ring.closePath();
                    if (area_to_add == null) {
                        area_to_add = ring;
                    } else {
                        area_to_add.append(ring, false);
                    }
                    ring = null;
                }
            }
        }
        MultiPolygonFeature f = new MultiPolygonFeature(bounds, parts_index, p_points);
        return f;
    }

    public PointFeature loadPoint(BufferedReader in, String Str) throws IOException {
        float x = 0, y = 0;
        String DataStr = "";
        int DataMark = ' ';
        int pos = 0;
        String StrX, StrY;
        int Point_index = Str.trim().indexOf(sPOINT);
        if (Point_index == -1) {
            log.logError("Expected PLine found: \"" + Str + "\"");
        } else {
            DataStr = Str.substring(Point_index + sPOINT.length()).trim();
        }
        pos = DataStr.indexOf(DataMark);
        StrX = DataStr.substring(0, pos).trim();
        StrY = DataStr.substring(pos + 1, DataStr.length()).trim();
        x = Float.valueOf(StrX).floatValue();
        y = Float.valueOf(StrY).floatValue() * -1;
        return new PointFeature(new Point2D.Float(x, y));
    }

    public PointFeature loadText(BufferedReader in, String Str) throws IOException {
        float x = 0, y = 0;
        String DataStr = "";
        int DataMark = ' ';
        int pos = 0;
        String StrX1, StrY1, StrX2, StrY2;
        float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        int Text_index = Str.trim().indexOf(sTEXT);
        if (Text_index == -1) {
            log.logError("Expected PLine found: \"" + Str + "\"");
        } else {
            DataStr = Str.substring(Text_index + sTEXT.length()).trim();
        }
        String TextContent = DataStr.substring(1, DataStr.length() - 1);
        TextCodes.addElement(TextContent);
        DataStr = in.readLine().trim();
        StringTokenizer tok = new StringTokenizer(DataStr, " ");
        int number_tok = tok.countTokens();
        StrX1 = tok.nextToken();
        StrY1 = tok.nextToken();
        StrX2 = tok.nextToken();
        StrY2 = tok.nextToken();
        x1 = Float.valueOf(StrX1).floatValue();
        y1 = Float.valueOf(StrY1).floatValue() * -1;
        x2 = Float.valueOf(StrX2).floatValue();
        y2 = Float.valueOf(StrY2).floatValue() * -1;
        x = (x1 + x2) / 2;
        y = (y1 + y2) / 2;
        return new PointFeature(new Point2D.Float(x, y));
    }

    public LineFeature loadLine(BufferedReader in, String Str) throws IOException {
        String DataStr = "";
        int DataMark = ' ';
        int num_points = 0;
        GeneralPath lines = new GeneralPath();
        int Line_index = Str.trim().indexOf(sLINE);
        if (Line_index == -1) {
            log.logError("Expected PLine found: \"" + Str + "\"");
        } else {
            DataStr = Str.substring(Line_index + sLINE.length()).trim();
        }
        num_points = 2;
        lines = new GeneralPath(GeneralPath.WIND_NON_ZERO, num_points);
        StringTokenizer tok = new StringTokenizer(DataStr, " ");
        int number_tok = tok.countTokens();
        String StrX1 = tok.nextToken();
        String StrY1 = tok.nextToken();
        String StrX2 = tok.nextToken();
        String StrY2 = tok.nextToken();
        float x1 = Float.valueOf(StrX1).floatValue();
        float y1 = Float.valueOf(StrY1).floatValue() * -1;
        float x2 = Float.valueOf(StrX2).floatValue();
        float y2 = Float.valueOf(StrY2).floatValue() * -1;
        lines.moveTo(x1, y1);
        lines.lineTo(x2, y2);
        return new LineFeature(lines);
    }

    public MultiPolygonFeature loadRectangle(BufferedReader in, String Str) throws IOException {
        String DataStr = "";
        int DataMark = ' ';
        int num_points = 0;
        GeneralPath area_to_add = null;
        GeneralPath area_to_subtract = null;
        GeneralPath ring = new GeneralPath();
        int Rect_index = Str.trim().indexOf(sRECT);
        if (Rect_index == -1) {
            log.logError("Expected PLine found: \"" + Str + "\"");
        } else {
            DataStr = Str.substring(Rect_index + sRECT.length()).trim();
        }
        num_points = 5;
        ring = new GeneralPath(GeneralPath.WIND_NON_ZERO, num_points);
        StringTokenizer tok = new StringTokenizer(DataStr, " ");
        int number_tok = tok.countTokens();
        String StrX1 = tok.nextToken();
        String StrY1 = tok.nextToken();
        String StrX2 = tok.nextToken();
        String StrY2 = tok.nextToken();
        float sx1 = Float.valueOf(StrX1).floatValue();
        float sy1 = Float.valueOf(StrY1).floatValue() * -1;
        float sx2 = Float.valueOf(StrX2).floatValue();
        float sy2 = Float.valueOf(StrY2).floatValue() * -1;
        float x1 = Math.min(sx1, sx2);
        float y1 = Math.min(sy1, sy2);
        float x2 = Math.max(sx1, sx2);
        float y2 = Math.max(sy1, sy2);
        ring.moveTo(x1, y1);
        ring.lineTo(x2, y1);
        ring.lineTo(x2, y2);
        ring.lineTo(x1, y2);
        ring.lineTo(x1, y1);
        area_to_add = ring;
        return new MultiPolygonFeature(new double[] { x1, y1, x2, y2 }, new int[] { 0 }, new double[] { x1, y1, x2, y1, x2, y2, x1, y2 });
    }

    public TableAttributes getTableAttributes() {
        return (TableAttributes) table;
    }

    public TableInfo getTableInfo() {
        return ((CodeLabelTableSetShapeImpl) table).getTableInfo();
    }

    private static class CodeLabelTableSetShapeImpl implements CodeLabelTableSet, TableAttributes {

        int code_column;

        int label_column;

        FileResource tablename;

        TableReader r = null;

        String _code = null;

        String _label = null;

        boolean finished = false;

        TableInfo tableinfo;

        public TableInfo getTableInfo() {
            return tableinfo;
        }

        CodeLabelTableSetShapeImpl(FileResource tablename, int code_col, int label_col) throws IOException {
            this(tablename, code_col, label_col, null);
        }

        CodeLabelTableSetShapeImpl(FileResource tablename, int code_col, int label_col, String[] column_Names) throws IOException {
            this.tablename = tablename;
            TableReader r = getTableReader();
            r.readRow();
            if (code_col < 0) {
                code_col = 0;
            }
            if (label_col < 0) {
                label_col = 1;
            }
            if (label_col >= r.getColumnCount()) {
                label_col = r.getColumnCount() - 1;
            }
            String columnNames[];
            int columnTypes[] = new int[r.getColumnCount()];
            int primaryKeyColumns[] = new int[r.getColumnCount()];
            if (column_Names == null) {
                columnNames = new String[r.getColumnCount()];
                for (int j = 0; j < r.getColumnCount(); j++) {
                    columnNames[j] = r.getColumn(j);
                    columnTypes[j] = TableInfo.COLUMN_TYPE_STRING;
                    primaryKeyColumns[j] = j;
                }
            } else {
                columnNames = column_Names;
                for (int j = 0; j < r.getColumnCount(); j++) {
                    columnTypes[j] = TableInfo.COLUMN_TYPE_STRING;
                    primaryKeyColumns[j] = j;
                }
            }
            tableinfo = new TableInfoImpl(tablename.getResource(), columnNames, columnTypes, r.getColumnCount(), primaryKeyColumns);
            for (int j = 0; j < tableinfo.getColumnCount(); j++) {
                ((TableInfoImpl) tableinfo).setColumnLength(j, 32);
            }
            r.close();
            code_column = code_col;
            label_column = label_col;
        }

        public TableReader getTableReader() throws IOException {
            return new CSVTableReader(tablename, false);
        }

        int getCodeColumn() {
            return code_column;
        }

        int getLabelColumn() {
            return label_column;
        }

        public void rewind() {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (Exception e) {
            }
            try {
                r = new CSVTableReader(tablename, false);
                finished = false;
            } catch (IOException e) {
                throw new NullPointerException();
            }
        }

        public void dispose() {
        }

        public boolean readNext() {
            if (finished) {
                return false;
            }
            try {
                if (r.readRow() == -1) {
                    finished = true;
                    return false;
                } else {
                    _code = r.getValue(code_column);
                    _label = r.getValue(label_column);
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }

        public String getValue(int i) {
            try {
                return r.getValue(i);
            } catch (Exception e) {
                return null;
            }
        }

        public String getCode() {
            return _code;
        }

        public String getLabel() {
            return _label;
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (r != null) {
                r.close();
            }
        }
    }

    protected void loadCodes(FileResource filename) throws IOException {
        long t = System.currentTimeMillis();
        if (filename == null || filename.getResource().length() == 0) {
            return;
        }
        {
            TableReader r = null;
            try {
                r = new CSVTableReader(filename, false);
                if (r != null) {
                    boolean complete_column_names = true;
                    for (int i = 0; i < Column_Name.length; i++) {
                        if (Column_Name[i] == null) {
                            complete_column_names = false;
                        }
                    }
                    if (complete_column_names) {
                        table = new CodeLabelTableSetShapeImpl(filename, code_col, label_col, Column_Name);
                    } else {
                        table = new CodeLabelTableSetShapeImpl(filename, code_col, label_col);
                    }
                } else {
                    return;
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
            for (int i = 0; i < TextFeatures.size(); i++) {
                features.add((Feature) TextFeatures.elementAt(i));
            }
        }
    }
}
