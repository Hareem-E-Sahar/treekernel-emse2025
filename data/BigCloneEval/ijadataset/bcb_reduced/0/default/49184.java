import java.io.IOException;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import controller.MainController;

public class ImproveDocs {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        if (improvedocsAlreadyRunning()) {
            JFrame frameMessage = new JFrame();
            JOptionPane.showMessageDialog(frameMessage, "ImproveDocs is already running.");
            frameMessage.dispose();
            return;
        }
        new MainController();
    }

    private static boolean improvedocsAlreadyRunning() {
        if (System.getProperty("os.name").contains("Windows")) {
            System.err.println("Environnement Windows : plusieurs instance d'ImproveDocs peuvent tourner simultanément.");
            return false;
        } else {
            int nbProcess = 0;
            Runtime rt = Runtime.getRuntime();
            Process proc;
            try {
                proc = rt.exec("/bin/ps -axf");
                Scanner sc = new Scanner(proc.getInputStream());
                while (sc.hasNextLine()) {
                    if (sc.nextLine().contains("ImproveDocs")) nbProcess++;
                }
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (nbProcess > 1);
        }
    }
}
