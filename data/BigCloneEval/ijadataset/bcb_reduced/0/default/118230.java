import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import laboratorio.db.dao.utils.IconosAppDAO;

/**
 * @author Martin
 * 
 */
public class ConfigureIconsTool {

    Connection con;

    /**
     * @param dba
     */
    public ConfigureIconsTool() throws SQLException, ClassNotFoundException {
        Class.forName("org.gjt.mm.mysql.Driver");
        con = DriverManager.getConnection("jdbc:mysql:///laboratorio", "root", "");
    }

    private void configure() throws Exception {
        IconosAppDAO xIconosAppDAO = new IconosAppDAO(con);
        con.setAutoCommit(false);
        ImageIcon xImageIcon;
        String xNombre;
        xImageIcon = this.getIcon("icons/Tools.Gif");
        xNombre = "Icon-Tools";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        xImageIcon = this.getIcon("icons/Animp.Gif");
        xNombre = "Icon-Animp";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        xImageIcon = this.getIcon("icons/Idea.Gif");
        xNombre = "Icon-Idea";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        xImageIcon = this.getIcon("icons/Programs.Gif");
        xNombre = "Icon-Programs";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        xImageIcon = this.getIcon("icons/ImageEditor.Gif");
        xNombre = "Icon-ImageEditor";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        xImageIcon = this.getIcon("icons/puerta.Gif");
        xNombre = "Icon-Puerta";
        xIconosAppDAO.insertIcon(xNombre, xImageIcon);
        con.commit();
    }

    private ImageIcon getIcon(String path) {
        try {
            System.out.println(path);
            URL imageURL = ConfigureIconsTool.class.getResource(path);
            ImageIcon icon = new ImageIcon(imageURL);
            return icon;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            new ConfigureIconsTool().configure();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
