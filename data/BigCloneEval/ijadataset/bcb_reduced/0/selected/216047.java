package zpm;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Przemyslaw Kupisz
 */
public class PUSpoolIO implements Runnable {

    /**
     * Define new thread
     */
    Thread t_PUSpoolIO;

    /**
     *Define statements for new DB connection
     */
    Connection conn = null;

    Statement s = null;

    ResultSet rs = null;

    /**
    * Creates new thread & goes to function run()
    */
    public PUSpoolIO() {
        t_PUSpoolIO = new Thread(this, "PUSpoolIO");
        t_PUSpoolIO.start();
    }

    @Override
    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + ZpmApp.ip + ":" + ZpmApp.port + "/" + ZpmApp.dbname, ZpmApp.user, ZpmApp.passwd);
            s = conn.createStatement();
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            BigDecimal read, write;
            String[] cpu = new String[100];
            int i = 0;
            rs = s.executeQuery("SELECT sytsyp_pfxcpuad FROM d0r1 GROUP BY sytsyp_pfxcpuad;");
            while (rs.next()) {
                cpu[++i] = "" + rs.getString("sytsyp_pfxcpuad");
                cpu[0] = "" + i;
            }
            rs.close();
            int e;
            for (e = 1; e < Integer.decode(cpu[0]) + 1; e++) {
                rs = s.executeQuery(" SELECT sytsyp_plspiosr,sytsyp_plspiosw  FROM d0r1 WHERE sytsyp_pfxcpuad='" + cpu[e] + "' ORDER BY ID DESC LIMIT 2");
                rs.next();
                read = rs.getBigDecimal("sytsyp_plspiosr");
                write = rs.getBigDecimal("sytsyp_plspiosw");
                rs.next();
                read = read.subtract(rs.getBigDecimal("sytsyp_plspiosr"));
                write = write.subtract(rs.getBigDecimal("sytsyp_plspiosw"));
                rs.close();
                dataset.addValue(read, "Read", cpu[e]);
                dataset.addValue(write, "Write", cpu[e]);
            }
            conn.close();
            JFreeChart Chart = ChartFactory.createBarChart("I/O operations for spooling by specific processor address", "PU ADDRESS", "Value", dataset, PlotOrientation.VERTICAL, true, true, false);
            JFrame frame = new JFrame("PU SPOOLING I/O");
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            BufferedImage image = Chart.createBufferedImage(1024, 768);
            JLabel label = new JLabel();
            label.setIcon(new ImageIcon(image));
            frame.getContentPane().add(label);
            frame.pack();
            frame.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(PUSpoolIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(PUSpoolIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PUSpoolIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PUSpoolIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
