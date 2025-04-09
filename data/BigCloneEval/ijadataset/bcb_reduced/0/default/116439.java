import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasesBean implements java.io.Serializable {

    private Connection con;

    private String nombre;

    private String buque;

    public BasesBean() {
    }

    public void setNombre(String n) {
        this.nombre = n;
    }

    public String getNombre() {
        return (this.nombre);
    }

    public void Inicia() {
        Statement st = null;
        nombre = null;
        buque = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost", "sa", "");
            System.out.println("Conectado con HSQLDB, OK");
        } catch (SQLException ex) {
            Logger.getLogger(BasesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BasesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** *********************************
             * Crea(); Crea la base de datos.
             * Hace una llamada a Inicia(); Para que pueda utilizarse desde
             * la línea de comendos.
             * Crea las tablas de la base de datos
             * Muestra por consola las tablas que ha creado
             * Se cierra la conexión con la tabla
             */
    public void Crea() {
        Statement st = null;
        Inicia();
        try {
            String comando = "create table barcos (nib int, nombre varchar(20), matricula varchar(14), imo int," + "eslora int, esloral int, gt int, trb int, pesca varchar(11));";
            st = con.createStatement();
            st.executeUpdate(comando);
            System.out.println("Creada la tabla de barcos");
            String comando2 = "create table lugares ( lugar varchar(20));";
            st = con.createStatement();
            st.executeUpdate(comando2);
            System.out.println("Creada la tabla de tipos de reconocimiento");
            String comando3 = "create table calidad ( norden varchar(12),nib int, lugar varchar(20),fecha date, recono varchar(20));";
            st.executeUpdate(comando3);
            System.out.println("Creada la tabla de detalles calidad");
            con.commit();
            System.out.println("hecho el commit");
            st.executeUpdate("shutdown");
            con.close();
            System.out.println("Terminada la creación de tablas. Cerrando");
        } catch (SQLException ex) {
            Logger.getLogger(BasesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
