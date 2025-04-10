public class Test {    protected void doBackupOrganize() throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        String strSelQuery = "SELECT organize_id,organize_type_id,organize_name,organize_manager," + "organize_describe,work_type,show_order,position_x,position_y " + "FROM " + Common.ORGANIZE_TABLE;
        String strInsQuery = "INSERT INTO " + Common.ORGANIZE_B_TABLE + " " + "(version_no,organize_id,organize_type_id,organize_name,organize_manager," + "organize_describe,work_type,show_order,position_x,position_y) " + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        DBOperation dbo = factory.createDBOperation(POOL_NAME);
        try {
            try {
                con = dbo.getConnection();
                con.setAutoCommit(false);
                ps = con.prepareStatement(strSelQuery);
                result = ps.executeQuery();
                ps = con.prepareStatement(strInsQuery);
                while (result.next()) {
                    ps.setInt(1, this.versionNO);
                    ps.setString(2, result.getString("organize_id"));
                    ps.setString(3, result.getString("organize_type_id"));
                    ps.setString(4, result.getString("organize_name"));
                    ps.setString(5, result.getString("organize_manager"));
                    ps.setString(6, result.getString("organize_describe"));
                    ps.setString(7, result.getString("work_type"));
                    ps.setInt(8, result.getInt("show_order"));
                    ps.setInt(9, result.getInt("position_x"));
                    ps.setInt(10, result.getInt("position_y"));
                    int resultCount = ps.executeUpdate();
                    if (resultCount != 1) {
                        con.rollback();
                        throw new CesSystemException("Organize_backup.doBackupOrganize(): ERROR Inserting data " + "in T_SYS_ORGANIZE_B INSERT !! resultCount = " + resultCount);
                    }
                }
                con.commit();
            } catch (SQLException se) {
                con.rollback();
                throw new CesSystemException("Organize_backup.doBackupOrganize(): SQLException:  " + se);
            } finally {
                con.setAutoCommit(true);
                close(dbo, ps, result);
            }
        } catch (SQLException se) {
            throw new CesSystemException("Organize_backup.doBackupOrganize(): SQLException while committing or rollback");
        }
    }
}