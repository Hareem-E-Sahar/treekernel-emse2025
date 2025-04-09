package lx.ghm.xelerator;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

public class XParameter extends XNode {

    private static final String iconFile = "/lx/ghm/xelerator/resource/XParameter.png";

    public Image getIcon(int type) {
        return new ImageIcon(getClass().getResource(iconFile)).getImage();
    }

    private String name = "XParameter";

    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private ArrayList parametersList = new ArrayList();

    public int size() {
        return parametersList.size();
    }

    public Vector get(int index) {
        return (Vector) parametersList.get(index);
    }

    public ArrayList getParametersList() {
        return parametersList;
    }

    public void resetParameter() {
        this.parametersList = new ArrayList();
    }

    public void addParameter(Vector parameter) {
        parametersList.add(parameter);
    }

    public void setParametersList(ArrayList parametersList) {
        this.parametersList = parametersList;
    }

    public void removeParaeter(Vector paramter) {
    }

    private String regExpPattern = "%([._a-zA-Z0-9-]+)%";

    public String doTranslate(String variable) {
        String content = variable;
        Pattern p = Pattern.compile(regExpPattern);
        Matcher m = p.matcher(variable);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String match = variable.substring(start, end);
            for (int i = 0; i < parametersList.size(); i++) {
                Vector paramter = (Vector) parametersList.get(i);
                if (match.indexOf((String) paramter.get(0)) >= 0) {
                    String value = (String) paramter.get(1);
                    content = content.replaceAll(match, value);
                }
            }
        }
        return content;
    }

    public static void main(String[] args) {
        String content = "1) ENWTPPS109/27.12 2) ENWTPPS109/27.13                                               3) ENWTPPS109/27.2                                                4) ENWTPPS109/27.4                                                5) ENWTPPS109/27.6                                                6) ENWTPPS113/27.13                                               7) ENWTPPS267/SU2R27                                              8) ENWTPPS273/SU2R27                                              Enter the number preceding the owner SPA, or enter 'q' to quit -> ";
        Pattern p = Pattern.compile("([0-9]+)\\) ENWTPPS273/SU2R27");
        Matcher m = p.matcher(content);
        while (m.find()) {
            System.out.println(m.group(1));
        }
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setMultiSelectionEnabled(true);
        int status = fileChooser.showOpenDialog(null);
        if (status == JFileChooser.APPROVE_OPTION) {
            File selectedFiles[] = fileChooser.getSelectedFiles();
            for (int i = 0, n = selectedFiles.length; i < n; i++) {
                System.out.println("Selected: " + selectedFiles[i].getParent() + " --- " + selectedFiles[i].getName());
            }
        }
    }
}
