import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.Connection;

/** DmzRepo Class */
public final class DmzRepo {

    private Connection conn;

    private String url;

    private String driverName;

    private String driverVersion;

    private String dbc;

    private boolean debugProcFlag;

    /**
     * Creates new dmzRepo
     * @param p_conn  database connection to use
     **/
    public DmzRepo(String p_conn) {
        this.debugProcFlag = true;
        this.dbc = p_conn;
    }

    /** create the connection **/
    public void connect() {
        DatabaseMetaData dma;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            try {
                this.conn = DriverManager.getConnection(this.dbc);
                dma = conn.getMetaData();
                this.setUrl(dma.getURL());
                this.setDriverName(dma.getDriverName());
                this.setDriverVersion(dma.getDriverVersion());
            } catch (SQLException e) {
                System.out.println(" DriverManager.getConnection() Fail");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class.ForName() Fail");
        }
    }

    /** print the connection metadata **/
    public void printMetdata() {
        System.out.println("\nConnected to " + this.getUrl());
        System.out.println("Driver       " + this.getDriverName());
        System.out.println("Version      " + this.getDriverVersion());
        System.out.println("");
        System.out.flush();
    }

    /** close the connection **/
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Getter for property driverName.
     * @return Value of property driverName.
     **/
    public java.lang.String getDriverName() {
        return this.driverName;
    }

    /**
     * Getter for property driverVersion.
     * @return Value of property driverVersion.
     **/
    public java.lang.String getDriverVersion() {
        return this.driverVersion;
    }

    /**
     * return a database conenction
     * @return  database connection *
     **/
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * Setter for property driverName.
     * @param driverName New value of property driverName.
     **/
    private void setDriverName(java.lang.String driverName) {
        this.driverName = driverName;
    }

    /**
     * Setter for property driverVersion.
     * @param driverVersion New value of property driverVersion.
     **/
    private void setDriverVersion(java.lang.String driverVersion) {
        this.driverVersion = driverVersion;
    }

    /**
     * Getter for property url.
     * @return Value of property url.
     **/
    private java.lang.String getUrl() {
        return this.url;
    }

    /**
     * Setter for property url.
     * @param url New value of property url.
     **/
    private void setUrl(java.lang.String url) {
        this.url = url;
    }

    /** Dataset for Entity Relationships
     * @param iParentId Parent Id of relationship
     * @return SQL String
     */
    public String getSqlEntityRelationship(int iParentId) {
        String parentId = Integer.toString(iParentId);
        return "SELECT 1, eParent.entity_name as RelationshipName, r.parent_verb as RelationshipVerb" + "  FROM relationship r, " + "       entity eParent   " + " WHERE r.parent_id  = eParent.entity_id " + "   AND r.child_id = " + parentId + "  UNION ALL " + "SELECT 2, eChild.entity_name, r.child_verb" + "  FROM relationship r, " + "       entity eChild   " + " WHERE r.child_id  = eChild.entity_id " + "   AND r.parent_id = " + parentId + " ORDER BY 1, 2;";
    }

    /** Dataset for Entity Synonyms
     * @param entityId Entity selector
     * @return SQL String
     */
    public String getSqlEntitySynonym(int entityId) {
        return "SELECT entity_synonym.synonym_seq, synonym.synonym_name" + "  FROM synonym , entity_synonym" + " WHERE synonym.synonym_id = entity_synonym.synonym_id" + "   AND entity_synonym.entity_id = " + Integer.toString(entityId);
    }

    /** Dataset for Entity Attributes
     * @param entityId Entity selector
     * @return SQL String
     */
    public String getSqlEntityAttribute(int entityId) {
        return "SELECT attribute.attribute_id, attribute.attribute_name, " + "       attribute.attribute_type, attribute.attribute_length, " + "       attribute.attribute_decimal, attribute.attribute_description , " + "       synonym_name, synonym.synonym_id " + "  FROM attribute, synonym, entity_synonym " + " WHERE attribute.attribute_id = synonym.attribute_id  " + "   AND synonym.synonym_id = entity_synonym.synonym_id" + "   AND entity_synonym.entity_id = " + Integer.toString(entityId) + " ORDER BY synonym_name ,attribute_name;";
    }

    /** Dataset for all Entites
     * @return SQL String
     */
    public String getAllEntities() {
        return "SELECT entity_id, entity_name, relationship_count, entity.text_block_id, parent_id, entity_type, text_block" + " FROM entity LEFT JOIN text_block ON entity.text_block_id = text_block.text_block_id ORDER BY entity_name";
    }

    /** Dataset for all Relationships
     * @return SQL String
     */
    public String getAllRelationships() {
        return "SELECT relationship_id, parent_id, parent_verb, child_id, child_verb, " + " parent_cardinality, child_cardinality " + " FROM relationship Order by parent_id;";
    }

    /** Dataset for all Synonyms
     * @return SQL String
     */
    public String getAllSynonyms() {
        return "SELECT synonym_name, attribute_name, synonym_id, attribute.attribute_id" + "  FROM attribute, synonym " + " WHERE attribute.attribute_id = synonym.attribute_id  " + " ORDER BY synonym_name ,attribute_name;";
    }
}
