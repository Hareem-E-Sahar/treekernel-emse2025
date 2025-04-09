import java.io.*;
import java.util.*;
import oracle.sql.*;
import java.sql.*;
import java.math.BigDecimal;
import oracle.CartridgeServices.*;

public class StoredCtx {

    ResultSet rset;

    public StoredCtx(ResultSet rs) {
        rset = rs;
    }
}

public class JdbmsCompressZipEntryImp implements SQLData {

    private BigDecimal key;

    static final BigDecimal SUCCESS = new BigDecimal(0);

    static final BigDecimal ERROR = new BigDecimal(1);

    String sql_type;

    public String getSQLTypeName() throws SQLException {
        return sql_type;
    }

    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        sql_type = typeName;
        key = stream.readBigDecimal();
    }

    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeBigDecimal(key);
    }

    public static BigDecimal ODCITableStart(STRUCT[] sctx, ResultSet rset) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:default:connection:");
        StoredCtx ctx = new StoredCtx(rset);
        int key;
        try {
            key = ContextManager.setContext(ctx);
        } catch (CountException ce) {
            return ERROR;
        }
        Object[] impAttr = new Object[1];
        impAttr[0] = new BigDecimal(key);
        StructDescriptor sd = new StructDescriptor("JdbmsCompressZipEntryImp", conn);
        sctx[0] = new STRUCT(sd, conn, impAttr);
        return SUCCESS;
    }

    public BigDecimal ODCITableFetch(BigDecimal nrows, ARRAY[] outSet) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:default:connection:");
        StoredCtx ctx;
        try {
            ctx = (StoredCtx) ContextManager.getContext(key.intValue());
        } catch (InvalidKeyException ik) {
            return ERROR;
        }
        int nrowsval = nrows.intValue();
        if (nrowsval > 10) nrowsval = 10;
        Vector v = new Vector(nrowsval);
        int i = 0;
        StructDescriptor outDesc = StructDescriptor.createDescriptor("ZIPENTRYTYPE", conn);
        Object[] out_attr = new Object[3];
        while (nrowsval > 0 && ctx.rset.next()) {
            out_attr[0] = (Object) ctx.rset.getString(1);
            out_attr[1] = (Object) new String("O");
            out_attr[2] = (Object) new BigDecimal(ctx.rset.getFloat(2));
            v.add((Object) new STRUCT(outDesc, conn, out_attr));
            out_attr[1] = (Object) new String("C");
            out_attr[2] = (Object) new BigDecimal(ctx.rset.getFloat(3));
            v.add((Object) new STRUCT(outDesc, conn, out_attr));
            i += 2;
            nrowsval -= 2;
        }
        if (i == 0) return SUCCESS;
        Object out_arr[] = v.toArray();
        ArrayDescriptor ad = new ArrayDescriptor("ZIPENTRYTYPESET", conn);
        outSet[0] = new ARRAY(ad, conn, out_arr);
        return SUCCESS;
    }

    public BigDecimal ODCITableClose() throws SQLException {
        StoredCtx ctx;
        try {
            ctx = (StoredCtx) ContextManager.clearContext(key.intValue());
        } catch (InvalidKeyException ik) {
            return ERROR;
        }
        Statement stmt = ctx.rset.getStatement();
        ctx.rset.close();
        if (stmt != null) stmt.close();
        return SUCCESS;
    }
}
