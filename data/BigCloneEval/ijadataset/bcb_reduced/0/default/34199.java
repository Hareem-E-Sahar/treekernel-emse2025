import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ConexionBD {

    private Connection conexion = null;

    /**
     * Permite instanciar un objeto de la clase ConexionBD, registrando el 
     * driver correspondiente para la base de datos MySQL.
     */
    public ConexionBD() {
        if (conexion != null) return;
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se hallo driver para la base de datos.");
        }
    }

    /**
     * Realiza la conexión a la base de datos. En caso de ser la primera vez
     * que se utiliza el programa, se pide la contraseña de MySQL y se crea un 
     * nuevo usuario para uso del programa.
     * @return boolean true Si la conexión se completó sin problemas; false en caso contrario.
     */
    public boolean conectar() {
        boolean hasErrors = false;
        try {
            conexion = DriverManager.getConnection("jdbc:mysql://localhost/mysql", "pof_billetera", "pof123456");
        } catch (java.sql.SQLException sqle) {
            javax.swing.JLabel label = new javax.swing.JLabel("Ingrese la contraseña de \"root\" para mysql: ");
            javax.swing.JPasswordField jpf = new javax.swing.JPasswordField();
            javax.swing.JOptionPane.showMessageDialog(null, new Object[] { label, jpf }, "Password:", javax.swing.JOptionPane.OK_CANCEL_OPTION);
            String contrasena = jpf.getText();
            try {
                conexion = DriverManager.getConnection("jdbc:mysql://localhost/mysql", "root", contrasena);
                Statement instruccionesBD = conexion.createStatement();
                instruccionesBD.execute("GRANT ALL ON *.* TO 'pof_billetera' IDENTIFIED BY 'pof123456'");
            } catch (java.sql.SQLException sqle2) {
                javax.swing.JOptionPane.showMessageDialog(null, "Acceso a la base de datos denegado. Revise que haya colocado su usuario como root y que haya ingresado la contraseña correcta a la base de datos.");
                hasErrors = true;
            }
        } catch (Exception noSQL) {
            javax.swing.JOptionPane.showMessageDialog(null, "Para poder utilizar este programa, debe de tener instalado la base de datos MySQL, además de configurarlo en el localhost.");
            hasErrors = true;
        }
        return !hasErrors;
    }

    /**
     * Permite utilizar la base de datos correspondiente al usuario ingresado 
     * como parámetro.
     * @param usuario Nombre del usuario que se encuentra utilizando el programa.
     * @return boolean True si se logro utilizar la base de datos requerida. False en caso contrario.
     */
    public boolean usarBD(String usuario) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("USE pof_" + usuario);
            return true;
        } catch (Exception sqle) {
            return false;
        }
    }

    /**
     * Permite crear una nueva base de datos en MySQL, la cual será correspondiente
     * al usuario cuyo nombre se recibe como parámetro.
     * @param usuario Nombre del usuario cuya base de datos será creada.
     */
    public void crearBD(String usuario) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("CREATE DATABASE IF NOT EXISTS pof_" + usuario);
            instruccionesBD.execute("USE pof_" + usuario);
        } catch (Exception sqle) {
            System.out.println("Error inesperado en el método crearBD, CoenexionBD. Contacte al proveedor de su programa");
        }
    }

    /**
     * Permite obtener el result set de cierta tabla.
     * @param nombreTabla Nombre de la tabla cuyo resultSet se desea hallar.
     * @return ResultSet con el resultado. Puede obtenerse null si no se concreta la conexión.
     */
    public ResultSet datosDeTabla(String nombreTabla) {
        ResultSet resultado = null;
        try {
            Statement instruccionesBD = conexion.createStatement();
            resultado = instruccionesBD.executeQuery("SELECT * FROM " + nombreTabla);
        } catch (Exception noConecto) {
            System.out.println("Error inesperado en método datosDeTabla, ConexionBD. Contacte al proveedor de su programa.");
        }
        return resultado;
    }

    /**
     * Permite crear la tabla en la base de datos MySQL correspondiente a cierta
     * categoría.
     * @param nombreDeTabla Nombre de la nueva tabla a crear.
     * @param titulos_columnas Títulos de las columnas que llevará dicha tabla.
     * @return boolean true si se creo la tabla correctamente; false en caso contrario.
     */
    public boolean crearTablaCategoria(String nombreDeTabla, ArrayList<String> titulos_columnas) {
        try {
            Statement instruccionesBd = conexion.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS ";
            query += nombreDeTabla;
            query += " (id INT NOT NULL AUTO_INCREMENT, ";
            query += " titulo VARCHAR(80), ";
            for (int i = 2; i < titulos_columnas.size(); i++) {
                query += titulos_columnas.get(i) + " VARCHAR(80), ";
            }
            query += "PRIMARY KEY(id)) ENGINE= InnoDB";
            instruccionesBd.execute(query);
            return true;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se pudo crear la categoría. Intente con otro nombre");
            return false;
        }
    }

    /**
     * Permite conocer si existe una tabla cono cierto nombre o no.
     * @param nombreTabla Nombre de la tabla cuya existencia se desea averiguar.
     * @return boolean true Si existe la tabla; false en caso contrario.
     */
    public boolean existeTabla(String nombreTabla) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("DESCRIBE " + nombreTabla);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Permite agregar un nuevo campo a una tabla. Dicho campo se inicializa
     * vacío (valores null en cada una de sus casillas).
     * @param nombreTabla Nombre de la tabla a modificar.
     * @param nombreColumna Nombre de la nueva columna.
     * @return boolean Devuelve true si se ha agregado la columna correctamente; false en caso conrario.
     */
    public boolean agregarColumna(String nombreTabla, String nombreColumna) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("ALTER TABLE " + nombreTabla + " ADD " + nombreColumna + " VARCHAR(80)");
            return true;
        } catch (Exception noSePudoCambiarTabla) {
            return false;
        }
    }

    /**
     * Elimina una columna de una de las tablas. OJO: Eso elimina todos los
     * datos en la columna.
     * @param nombreTabla Nombre de la tabla a modificar.
     * @param nombreCampo Nombre de la columna a eliminar.
     */
    public void eliminarColumna(String nombreTabla, String nombreColumna) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("ALTER TABLE " + nombreTabla + " " + "DROP COLUMN " + nombreColumna);
        } catch (Exception noSePudoCambiarTabla) {
            System.out.println("No se pudo borrar columna. Método eliminarColumna, ConexionBD.");
        }
    }

    /**
     * Permite eliminar una fila de una tabla en la base de datos que está siendo utilizada.
     * @param nombreTabla Nombre de la tabla en donde se eliminar la fila.
     * @param id String que contiene el id de la fila a borrar.
     */
    public void eliminarFila(String nombreTabla, String id) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("DELETE FROM " + nombreTabla + " WHERE id = " + id);
        } catch (Exception noSePudoCambiarTabla) {
            System.out.println("No se pudo borrar fila. Método eliminarFila, ConexionBD.");
        }
    }

    /**
     * Permite ejecutar una instrucción genérica en MySQL.
     * @param instrucción String con la instrucción a ser ejecutada por MySQL.
     */
    public void ejecutar(String instruccion) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute(instruccion);
        } catch (Exception noSePudoQuery) {
            System.out.println("No se pudo ejecutar la instrucción. Método ejecutar, ConexionBD.");
        }
    }

    /**
     * Permite eliminar una tabla de la base de datos que se está utilizando.
     * @param nombreTabla Nombre de la tabla a eliminar.
     */
    public void eliminarTabla(String nombreTabla) {
        try {
            Statement instruccionesBD = conexion.createStatement();
            instruccionesBD.execute("DROP TABLE " + nombreTabla);
        } catch (Exception noSePudoCambiarTabla) {
            System.out.println("No se pudo eliminar tabla. Método eliminarTabla, ConexionBD.");
        }
    }

    /**
     * Permite cerrar la conexión a la base de datos
     */
    public void desconectar() {
        try {
            conexion.close();
        } catch (Exception noCerro) {
            System.out.println("ERROR FATAL: No se pudo cerrar la conexión a la base de datos.");
        }
    }
}
