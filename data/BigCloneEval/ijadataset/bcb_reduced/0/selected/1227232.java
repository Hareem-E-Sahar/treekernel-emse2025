package es.devel.opentrats.booking.util;

import es.devel.opentrats.booking.beans.Employee;
import es.devel.opentrats.booking.service.business.EnvironmentService;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ProgressMonitor;
import org.apache.log4j.Logger;

/**
 *
 * @author Fran Serrano
 */
public class OpenTratsBookingUtil {

    public static final String TYPE_ERROR = "e";

    public static final String TYPE_WARNING = "w";

    public static final String TYPE_INFORMATION = "i";

    /** Creates a new instance of Utilidades */
    public OpenTratsBookingUtil() {
    }

    public static void Mensaje(String mensaje, String titulo, String tipo) {
        try {
            if (tipo.compareToIgnoreCase("error") == 0 || tipo.compareToIgnoreCase("e") == 0) {
                javax.swing.JOptionPane.showMessageDialog(null, mensaje, "OpenTrats.- " + titulo, javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            if (tipo.compareToIgnoreCase("informacion") == 0 || tipo.compareToIgnoreCase("i") == 0) {
                javax.swing.JOptionPane.showMessageDialog(null, mensaje, "OpenTrats.- " + titulo, javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            if (tipo.compareToIgnoreCase("exclamacion") == 0 || tipo.compareToIgnoreCase("w") == 0) {
                javax.swing.JOptionPane.showMessageDialog(null, mensaje, "OpenTrats.- " + titulo, javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
        }
    }

    public static String getNumeroMes(String mes_cadena) {
        try {
            if (mes_cadena.compareTo("Enero") == 0) {
                return "01";
            }
            if (mes_cadena.compareTo("Febrero") == 0) {
                return "02";
            }
            if (mes_cadena.compareTo("Marzo") == 0) {
                return "03";
            }
            if (mes_cadena.compareTo("Abril") == 0) {
                return "04";
            }
            if (mes_cadena.compareTo("Mayo") == 0) {
                return "05";
            }
            if (mes_cadena.compareTo("Junio") == 0) {
                return "06";
            }
            if (mes_cadena.compareTo("Julio") == 0) {
                return "07";
            }
            if (mes_cadena.compareTo("Agosto") == 0) {
                return "08";
            }
            if (mes_cadena.compareTo("Septiembre") == 0) {
                return "09";
            }
            if (mes_cadena.compareTo("Octubre") == 0) {
                return "10";
            }
            if (mes_cadena.compareTo("Noviembre") == 0) {
                return "11";
            }
            if (mes_cadena.compareTo("Diciembre") == 0) {
                return "12";
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return "null";
        }
    }

    public static String getCadenaMes(int numero_mes) {
        try {
            if (numero_mes == 1) {
                return "Enero";
            }
            if (numero_mes == 2) {
                return "Febrero";
            }
            if (numero_mes == 3) {
                return "Marzo";
            }
            if (numero_mes == 4) {
                return "Abril";
            }
            if (numero_mes == 5) {
                return "Mayo";
            }
            if (numero_mes == 6) {
                return "Junio";
            }
            if (numero_mes == 7) {
                return "Julio";
            }
            if (numero_mes == 8) {
                return "Agosto";
            }
            if (numero_mes == 9) {
                return "Septiembre";
            }
            if (numero_mes == 10) {
                return "Octubre";
            }
            if (numero_mes == 11) {
                return "Noviembre";
            }
            if (numero_mes == 12) {
                return "Diciembre";
            } else {
                return "Se ha producido un error en la devolucion de la cadena que representa al mes";
            }
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return "null";
        }
    }

    public static String ConstruirFecha(String dia, String mes, String anyo) {
        try {
            String fecha = anyo + "-" + mes + "-" + dia;
            return fecha;
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return "Se ha producido un error";
        }
    }

    public static String getFechaActual() {
        try {
            java.util.Date Fecha = new java.util.Date();
            java.text.SimpleDateFormat Formateador = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String Fecha_Cadena = Formateador.format(Fecha);
            return Fecha_Cadena.substring(0, 10);
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return "Se ha producido un error";
        }
    }

    public static String getHoraActual() {
        try {
            java.util.Date fecha = new java.util.Date();
            java.text.SimpleDateFormat Formateador = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String Fecha_Cadena = Formateador.format(fecha);
            return Fecha_Cadena.split(" ")[1];
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            OpenTratsBookingUtil.Mensaje("Error durante el c?lculo de la hora del sistema: " + e.toString(), "Error de c?lculo...", "error");
            return "";
        }
    }

    public static String FechaMySQL2FechaCorta(String fecha_MySQL) {
        try {
            String anyo = fecha_MySQL.split("-")[0];
            String mes = fecha_MySQL.split("-")[1];
            String dia = fecha_MySQL.split("-")[2];
            String fecha_corta = dia + "/" + mes + "/" + anyo;
            return fecha_corta;
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return "Se ha producido un error en la conversion de fecha";
        }
    }

    public static float Round(float numero) {
        try {
            float miValor;
            miValor = new java.math.BigDecimal(numero).setScale(2, java.math.BigDecimal.ROUND_HALF_UP).floatValue();
            return miValor;
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return -1;
        }
    }

    public static boolean EnviarCorreo(String asunto, String emisor, String destinatario, String mensaje) {
        try {
            Postman cartero = new Postman(emisor, "Aplicación OpenTratsBooking", destinatario, asunto, mensaje);
            return true;
        } catch (Exception e) {
            OpenTratsBookingUtil.Mensaje("El mensaje de correo no ha podido ser enviado", "Error de envío...", "exclamacion");
            Logger.getRootLogger().error(e.toString());
            return false;
        }
    }

    public static boolean EnviarCorreo(String asunto, Exception e) {
        try {
            Logger.getRootLogger().error(e.toString());
            Postman cartero = new Postman(asunto, e);
            return true;
        } catch (Exception ex) {
            OpenTratsBookingUtil.Mensaje("El mensaje de correo no ha podido ser enviado", "Error de envío...", "exclamacion");
            Logger.getRootLogger().error(e.toString());
            return false;
        }
    }

    public static boolean esNumero(java.awt.event.KeyEvent tecla) {
        if (Character.isDigit(tecla.getKeyChar())) {
            return true;
        }
        return false;
    }

    public static void Beep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static java.awt.Color getColorEmpleado(int refempleado) {
        java.awt.Color color = new java.awt.Color(refempleado + 20, refempleado + 30, refempleado + 150);
        return color;
    }

    public static String getDiaSemana(int dia) {
        try {
            switch(dia) {
                case GregorianCalendar.SUNDAY:
                    {
                        return "Domingo";
                    }
                case GregorianCalendar.MONDAY:
                    {
                        return "Lunes";
                    }
                case GregorianCalendar.TUESDAY:
                    {
                        return "Martes";
                    }
                case GregorianCalendar.WEDNESDAY:
                    {
                        return "Miércoles";
                    }
                case GregorianCalendar.THURSDAY:
                    {
                        return "Jueves";
                    }
                case GregorianCalendar.FRIDAY:
                    {
                        return "Viernes";
                    }
                case GregorianCalendar.SATURDAY:
                    {
                        return "Sábado";
                    }
            }
            return null;
        } catch (Exception e) {
            OpenTratsBookingUtil.Mensaje("Error genérico durante la devolución del día de la semana:\n\n" + e.getMessage(), "Error de aplicacion...", "e");
            Logger.getRootLogger().error(e.toString());
            return null;
        }
    }

    public static String[] DetectarSistema() {
        try {
            String Sistema = System.getProperty("os.name");
            String version = System.getProperty("java.version");
            String arch = System.getProperty("os.arch");
            String versionOS = System.getProperty("os.version");
            String[] sistema = { Sistema, version, arch, versionOS };
            return sistema;
        } catch (SecurityException ex) {
            OpenTratsBookingUtil.Mensaje("Ha ocurrido un error de acceso durante la detección de las propiedades del sistema", "Error de seguridad...", "w");
            EnviarCorreo("(Utilidades)DetectarSistema", ex);
            ex.printStackTrace();
            return null;
        } catch (Exception e) {
            OpenTratsBookingUtil.Mensaje("Ha ocurrido un error genérico durante la detección de las propiedades del sistema", "Error de aplicación...", "w");
            EnviarCorreo("(Utilidades)DetectarSistema", e);
            Logger.getRootLogger().error(e.toString());
            return null;
        }
    }

    public static String getRutaUsuario() {
        try {
            String separador = System.getProperty("file.separator");
            String path = System.getProperty("user.dir");
            return path + separador;
        } catch (Exception e) {
            OpenTratsBookingUtil.Mensaje("Error durante la recuperación de la ruta del usuario actual:\n" + e.toString(), "Error de ruta...", "e");
            Logger.getRootLogger().error(e.toString());
            return null;
        }
    }

    public static String getOS() {
        try {
            String Sistema = System.getProperty("os.name");
            if (Sistema.indexOf("inux") != -1) {
                return "linux";
            }
            if (Sistema.indexOf("indow") != -1) {
                return "windows";
            }
            return "mac";
        } catch (Exception e) {
            OpenTratsBookingUtil.Mensaje("Ha ocurrido un error genérico durante recuperación de la propiedad del sistema", "Error de aplicación...", "w");
            EnviarCorreo("(Utilidades) getOS", e);
            Logger.getRootLogger().error(e.toString());
            return null;
        }
    }

    public static boolean ValidarExpresion(String expresionPatron, String valor_a_validar) {
        Pattern patron = Pattern.compile(expresionPatron);
        Matcher comparador = patron.matcher(valor_a_validar);
        return comparador.matches();
    }

    public static Color getColor(int codigo) {
        String color = ((Employee) EnvironmentService.getInstance().getEmployees().get("" + codigo)).getColor();
        int r = Integer.parseInt(color.split(",")[0]);
        int g = Integer.parseInt(color.split(",")[1]);
        int b = Integer.parseInt(color.split(",")[2]);
        return new Color(r, g, b);
    }

    public static File getPantallazo() {
        try {
            Robot robot = new Robot();
            Toolkit medidor = Toolkit.getDefaultToolkit();
            Dimension dimensiones = medidor.getScreenSize();
            Rectangle pantalla = new Rectangle(dimensiones);
            BufferedImage imagen = robot.createScreenCapture(pantalla);
            File pantallazo = new File("pantallazo.png");
            pantallazo.deleteOnExit();
            ImageIO.write(imagen, "png", pantallazo);
            return pantallazo;
        } catch (AWTException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception e) {
            Logger.getRootLogger().error(e.toString());
            return null;
        }
    }

    public static void showProgress(String message) {
        ProgressMonitor pm = new ProgressMonitor(null, "Cargando aplicación", "Por favor, espere...", 0, 50);
        pm.setMillisToDecideToPopup(0);
        pm.setMillisToPopup(0);
        pm.setProgress(25);
        pm.setNote("Iniciando gestor de gráficos...");
    }
}
