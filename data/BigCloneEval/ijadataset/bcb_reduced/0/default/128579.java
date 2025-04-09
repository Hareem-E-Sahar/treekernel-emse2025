import java.sql.*;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.servlet.jsp.tagext.PageData;

@ManagedBean(name = "userBean")
@ViewScoped
public class User implements Serializable {

    private String id;

    private String password;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String SubmitLoginButton() {
        try {
            String p = null;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/estate_agency_system", "root", "admin");
            String query = "SELECT Password FROM agent WHERE (idAgent = ?)";
            PreparedStatement statement = con.prepareStatement(query);
            System.out.println("amk");
            statement.setString(1, this.getId());
            ResultSet result = statement.executeQuery(query);
            System.out.println("ccc");
            while (result.next()) {
                p = result.getString("Password");
            }
            System.out.println(p);
        } catch (InstantiationException e) {
            System.out.println("111" + e);
        } catch (IllegalAccessException e) {
            System.out.println("222" + e);
        } catch (ClassNotFoundException e) {
            System.out.println("333" + e);
        } catch (SQLException e) {
            System.out.println("444" + e);
        }
        return "agent";
    }
}
