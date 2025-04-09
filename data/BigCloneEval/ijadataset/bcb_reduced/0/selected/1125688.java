package be.potame.cavadium;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.shiro.subject.Subject;
import org.xml.sax.SAXException;

public class Cavadium extends JFrame implements ActionListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static final String appName = "Cavadium";

    public static final String appVersion = "0.1.0";

    public static Subject currentUser;

    public static Properties configFile = new Properties();

    public static ILabel iLabel = new ILabel();

    public static SqlSessionFactory sqlSessionFactory;

    private JMenuBar mainMenu;

    private MenuManager menuManager = new MenuManager();

    public Cavadium() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        super(appName + " " + appVersion);
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
        this.setSize(800, 600);
        setVisible(true);
        Security security = new Security(this);
        mainMenu = menuManager.getMainMenu(this);
        this.setJMenuBar(mainMenu);
        SwingUtilities.updateComponentTreeUI(this);
        setVisible(true);
        System.out.println("user authenticated: " + currentUser.isAuthenticated());
        String resource = "Configuration.xml";
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        try {
            configFile.load(Cavadium.class.getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cavadium cavadium = new Cavadium();
    }

    public boolean quitCurrent() {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void actionPerformed(ActionEvent evt) {
        try {
            ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
            Class<?> dynClass = myClassLoader.loadClass("be.potame.cavadium." + evt.getActionCommand());
            java.lang.reflect.Constructor constructeur = dynClass.getConstructor(new Class[] { Class.forName("be.potame.cavadium.Cavadium") });
            constructeur.newInstance(new Object[] { this });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
