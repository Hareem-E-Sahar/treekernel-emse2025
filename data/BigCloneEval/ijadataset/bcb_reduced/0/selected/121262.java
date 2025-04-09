package org.fao.waicent.kids.giews.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.fao.waicent.attributes.Attributes;
import org.fao.waicent.attributes.Cell;
import org.fao.waicent.attributes.Extent;
import org.fao.waicent.attributes.ExtentInterface;
import org.fao.waicent.attributes.ExtentManager;
import org.fao.waicent.attributes.Key;
import org.fao.waicent.attributes.Matrix;
import org.fao.waicent.attributes.MatrixExport;
import org.fao.waicent.attributes.MatrixInterface;
import org.fao.waicent.db.LongColumn;
import org.fao.waicent.db.dbConnectionManager;
import org.fao.waicent.db.dbConnectionManagerPool;
import org.fao.waicent.kids.server.kidsRequest;
import org.fao.waicent.kids.server.kidsResponse;
import org.fao.waicent.kids.server.kidsService;
import org.fao.waicent.kids.server.kidsServiceException;
import org.fao.waicent.kids.server.kidsSession;
import org.fao.waicent.util.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class GiewsExportDatasets extends kidsService {

    Connection con;

    String database_ini;

    protected int NEW_DATASET_ID;

    private int DATA_TABLE_TYPE;

    protected String LABEL = "";

    protected String DATA_TABLE_NAME = "datatemplate";

    private Document NAVIGATOR_XML;

    protected static String KeyedExtent_TableName = "keyedextent_template";

    private String Skeyed_extent;

    private String Ikeyed_extent;

    String columns = "";

    String params = "";

    String[] cols = {};

    public GiewsExportDatasets() {
    }

    public GiewsExportDatasets(Document doc, Element ele) throws IOException {
        load(doc, ele);
    }

    public boolean execute(kidsRequest request, kidsResponse response) throws kidsServiceException {
        boolean consumed = false;
        kidsSession session = request.getSession();
        database_ini = session.getConfiguration().getDBIni();
        int comma_index = request.setting.indexOf(",");
        int datasetID = Integer.parseInt(request.setting.substring(0, comma_index));
        String sub = request.setting.substring(comma_index + 1);
        comma_index = sub.indexOf(",");
        String dirTo = sub.substring(0, comma_index);
        sub = sub.substring(comma_index + 1);
        comma_index = sub.indexOf(",");
        String filename2 = sub.substring(comma_index + 1);
        String _filename = "";
        this.setID(datasetID);
        try {
            Attributes attr = this.loadAttributes(this.popConnection());
            ExtentInterface extents = attr.getExtents();
            MatrixInterface matrix = attr.getMatrix();
            PrintStream out = null;
            try {
                _filename = dirTo + File.separatorChar + "Dataset.csv";
                File file = new File(dirTo);
                if (!file.exists()) {
                    file.mkdir();
                }
                file = new File(_filename);
                out = new PrintStream(new FileOutputStream(new File(_filename)));
                MatrixExport m_export = new MatrixExport(out, (ExtentInterface) extents, (MatrixInterface) matrix);
                if (attr.getHeader().equals("")) {
                } else {
                }
                m_export.export();
                out.flush();
                String outFilename = dirTo + File.separator + filename2;
                ZipOutputStream outZip = null;
                try {
                    outZip = new ZipOutputStream(new FileOutputStream(outFilename));
                } catch (FileNotFoundException e) {
                    return false;
                }
                try {
                    this.addZipFile(_filename, outZip);
                    outZip.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    File f = new File(outFilename);
                    f.delete();
                    return false;
                }
            } catch (Exception ex) {
                System.out.println("Error in getting CSV contents for " + _filename + ":" + ex.getMessage());
                ex.printStackTrace();
                out = null;
            }
        } catch (Exception e) {
            System.out.println("Error in GiewsExportDatasets ");
            e.printStackTrace();
        }
        consumed = true;
        return consumed;
    }

    public boolean undo(kidsRequest request, kidsResponse response) throws kidsServiceException {
        boolean consumed = false;
        return consumed;
    }

    /**
        * popConnection
        *
        * @return Connection
        * @version 1, last modified by A. Tamburo, 22/12/05
        */
    private Connection popConnection() {
        dbConnectionManager manager = dbConnectionManagerPool.getConnectionManager(database_ini);
        con = manager.popConnection();
        return con;
    }

    /**
        * pushConnection
        *
        * @param Connection
        * @version 1, last modified by A. Tamburo, 22/12/05
        */
    private void pushConnection(Connection con) {
        dbConnectionManager manager = dbConnectionManagerPool.getConnectionManager(database_ini);
        manager.pushConnection(con);
    }

    private void addZipFile(String filenameIn, ZipOutputStream out) throws FileNotFoundException, IOException {
        byte[] buf = new byte[1024];
        File fileIn = new File(filenameIn);
        out.putNextEntry(new ZipEntry(fileIn.getName()));
        FileInputStream in = new FileInputStream(fileIn);
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
    }

    public Attributes loadAttributes(Connection con) throws SQLException {
        Attributes a = null;
        a = new Attributes();
        loadAttributesDescriptor(con);
        ExtentManager extents = loadExtentManager(con);
        Matrix matrix = loadMatrix(con, extents);
        a.setName("Prova");
        a.setExtents(extents);
        a.setMatrix(matrix);
        return a;
    }

    public void loadAttributesDescriptor(Connection con) throws SQLException {
        Debug.println("GIEWSAttributesExternalizer.loadAttributesDescriptor STRT");
        String SQL = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            SQL = "select Dataset_Name, Dataset_DataTableName, " + "Dataset_DataTableType, XMLData_ID_Navigator " + "from dataset where Dataset_ID = ?";
            pstmt = con.prepareStatement(SQL);
            pstmt.setInt(1, getID());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                LABEL = rs.getString(1);
                DATA_TABLE_NAME = rs.getString(2);
                DATA_TABLE_TYPE = rs.getInt(3);
                int nav_xml_id = rs.getInt(4);
                if (nav_xml_id != -1) {
                    NAVIGATOR_XML = LongColumn.getXML(con, nav_xml_id);
                }
            }
        } catch (ParserConfigurationException e) {
            Debug.println("ParserConfigurationException : " + e.getMessage());
        } catch (SAXException e) {
            Debug.println("SAXException : " + e.getMessage());
        } catch (IOException e) {
            Debug.println("ParserConfigurationException : " + e.getMessage());
        } finally {
            try {
                rs.close();
                pstmt.close();
            } catch (Exception e) {
            }
        }
        setDatasetDimension(DATA_TABLE_TYPE);
        Debug.println("GIEWSAttributesExternalizer.loadAttributesDescriptor END");
    }

    public ExtentManager loadExtentManager(Connection con) throws SQLException {
        Debug.println("GIEWSAttributesExternalizer.loadExtentManager START");
        ExtentManager extents = null;
        String SQL = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            extents = new ExtentManager();
            SQL = "select b.KidsDimension_Label, count(a.DimensionValue_Order), " + "a.KidsDimension_Index " + "from kidsdatasetextent a, kidsdimension b " + "where b.Dataset_ID =? and a.Dataset_ID = b.Dataset_ID and " + "a.KidsDimension_Index = b.KidsDimension_Index " + "group by a.KidsDimension_Index, b.KidsDimension_Label " + "order by a.KidsDimension_Index";
            pstmt = con.prepareStatement(SQL);
            pstmt.setInt(1, getID());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String label = rs.getString(1);
                Extent extent = new Extent(label);
                extent.ensureCapacity(rs.getInt(2));
                extents.add(extent);
            }
            try {
                rs.close();
                pstmt.close();
            } catch (Exception e) {
            }
            SQL = "select KidsDimension_Index, DimensionValue_Code , " + "DimensionValue_Name, DimensionValue_Order  " + "from kidsdatasetextent where Dataset_ID = ? " + "order by KidsDimension_Index, DimensionValue_Order";
            pstmt = con.prepareStatement(SQL);
            pstmt.setInt(1, getID());
            rs = pstmt.executeQuery();
            Extent extent = null;
            while (rs.next()) {
                extents.addExtentEntry(rs.getInt(1), rs.getString(2), rs.getString(3));
                extent = extents.at(rs.getInt(1));
            }
        } finally {
            try {
                rs.close();
                pstmt.close();
            } catch (Exception e) {
            }
        }
        Debug.println("GIEWSAttributesExternalizer.loadExtentManager END");
        return extents;
    }

    public Matrix loadMatrix(Connection con, ExtentManager extents) throws SQLException {
        Debug.println("GIEWSAttributesExternalizer.loadMatrix() START");
        Matrix mtrx = null;
        StringBuffer SQL_buf = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            mtrx = new Matrix(extents.size());
            Key key = new Key(extents.size());
            for (int i = 0; i < key.size(); i++) {
                key.set(extents.at(i).size() - 1, i);
            }
            mtrx.addCell(key, 0);
            mtrx.deleteCell(key);
            SQL_buf = new StringBuffer("select " + columns + "Data_Value, Data_Symbol from ");
            SQL_buf.append(getDataTablename());
            SQL_buf.append(" where dataset_id = ? ");
            pstmt = con.prepareStatement(SQL_buf.toString());
            pstmt.setInt(1, getID());
            rs = pstmt.executeQuery();
            Key new_key = null;
            while (rs.next()) {
                for (int i = 0; i < key.size(); i++) {
                    String extent_code = rs.getString(i + 1);
                    int extent_index = Key.WILD;
                    if (extent_code != null) {
                        extent_index = extents.at(i).getIndexFromCode(extent_code, true);
                    }
                    key.set(extent_index, i);
                    new_key = new Key(key);
                }
                float value = rs.getFloat("Data_Value");
                char symbol = ' ';
                String symbol_str = rs.getString("Data_Symbol");
                if (symbol_str != null && !symbol_str.equals("")) {
                    symbol = symbol_str.charAt(0);
                }
                Cell cell = new Cell(value, symbol);
                mtrx.addCell(new_key, cell);
            }
        } finally {
            try {
                rs.close();
                pstmt.close();
            } catch (Exception e) {
            }
        }
        Debug.println("GIEWSAttributesExternalizer.loadMatrix() END");
        return mtrx;
    }

    private void setDatasetDimension(int size) {
        Debug.println("GIEWSAttributesExternalizer:setDatasetDimension START");
        String dimensions = "" + size + "dimensions";
        DATA_TABLE_NAME = "data_" + dimensions;
        KeyedExtent_TableName = "keyedextent_" + dimensions;
        Debug.println("DATA_TABLE_NAME: " + DATA_TABLE_NAME + " KeyedExtent_TableName: " + KeyedExtent_TableName);
        String dim = "Dimension_";
        columns = "";
        params = "";
        cols = new String[size];
        for (int i = 1; i <= size; i++) {
            columns = columns + dim + i + ", ";
            params = params + "?,";
            cols[(i - 1)] = dim + i;
        }
        setLoadKeyedExtentSQL();
        setInsertKeyedExtentSQL();
        Debug.println("GIEWSAttributesExternalizerMySQL.setDatasetDimension END");
    }

    public int getID() {
        return NEW_DATASET_ID;
    }

    public void setID(int dataset_id) {
        NEW_DATASET_ID = dataset_id;
    }

    public String getDataTablename() {
        return DATA_TABLE_NAME;
    }

    private void setLoadKeyedExtentSQL() {
        this.Skeyed_extent = "select " + columns + "KeyedExtent_Type " + "from " + KeyedExtent_TableName + " where KeyedExtent_Code=? and  " + "Dataset_ID=?";
    }

    private void setInsertKeyedExtentSQL() {
        this.Ikeyed_extent = "insert into " + KeyedExtent_TableName + "(Dataset_ID, " + columns + "KeyedExtent_Code, KeyedExtent_Type) values(?," + params + "?,?)";
    }
}
