import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.beans.*;

class ProjectInfo {

    DefaultListModel components = new DefaultListModel();

    DefaultListModel connections = new DefaultListModel();

    DefaultListModel availableComponents;

    String filename;

    double version = 0;

    private Project project;

    ProjectInfo() {
    }

    ProjectInfo(DefaultListModel availableComponents) {
        this.availableComponents = availableComponents;
    }

    ProjectInfo(DefaultListModel availableComponents, String filename) {
        this.availableComponents = availableComponents;
        read(filename, false);
    }

    public void setup(Project project, String filename) {
        this.project = project;
        read(filename, true);
    }

    private void read(String filename, boolean initialise) {
        this.filename = filename;
        StringTokenizer token;
        try {
            RandomAccessFile project_file = new RandomAccessFile(filename, "r");
            String line = " ";
            line = project_file.readLine();
            line = project_file.readLine();
            if (line.indexOf("Version") > 0) {
                String verparts[] = line.split(" ");
                version = Double.parseDouble(verparts[2]);
                if (version == 1.0) {
                    if (JOptionPane.showConfirmDialog(Workbench.mainframe, "Do you want to convert to latest project file version", "Project file format conversion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        project_file.close();
                        upgradeToVersion1dot1();
                        project_file = new RandomAccessFile(filename, "r");
                        line = project_file.readLine();
                        line = project_file.readLine();
                        if (line.indexOf("Version") > 0) {
                            String nverparts[] = line.split(" ");
                            version = Double.parseDouble(nverparts[2]);
                        }
                    }
                }
                line = project_file.readLine();
            }
            while (!line.equals("# Components:")) line = project_file.readLine();
            line = project_file.readLine();
            while (line.charAt(0) != '#') {
                token = new StringTokenizer(line);
                String name;
                name = token.nextToken();
                String data = "";
                if (token.hasMoreTokens()) data = token.nextToken();
                while (token.hasMoreTokens()) data = data + " " + token.nextToken();
                if (initialise) components.addElement(new ComponentInfo(project, name, data)); else {
                    Boolean componentFound = false;
                    ComponentInfo temp;
                    for (int i = 0; i < availableComponents.size(); i++) {
                        temp = (ComponentInfo) availableComponents.getElementAt(i);
                        if (temp.name.equals(name)) {
                            components.addElement(new ComponentInfo(name, temp.disp_name, data, temp.pins));
                            componentFound = true;
                        }
                    }
                    if (!componentFound) {
                        JOptionPane.showMessageDialog(Workbench.mainframe, "We can't find the component " + name, "Project file error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                }
                line = project_file.readLine();
            }
            while (!line.equals("# Connections:")) line = project_file.readLine();
            line = project_file.readLine();
            while (line.charAt(0) != '#') {
                token = new StringTokenizer(line, ":,", false);
                String src = token.nextToken();
                String src_pin = token.nextToken();
                String dest = token.nextToken();
                String dest_pin = token.nextToken();
                connections.addElement(new Connection(Integer.valueOf(src).intValue(), Integer.valueOf(src_pin).intValue(), Integer.valueOf(dest).intValue(), Integer.valueOf(dest_pin).intValue()));
                line = project_file.readLine();
            }
            while (!line.equals("# Connection info:")) line = project_file.readLine();
            line = project_file.readLine();
            for (int i = 0; line.charAt(0) != '#'; i++) {
                ((Connection) connections.getElementAt(i)).setConnectionInfo(line);
                line = project_file.readLine();
            }
            if (version >= 1.1) {
                try {
                    while (!line.equals("# Locations:")) line = project_file.readLine();
                    line = project_file.readLine();
                    while (line.charAt(0) != '#') {
                        ComponentInfo tempComp;
                        token = new StringTokenizer(line, ":,", false);
                        String compID = token.nextToken();
                        String compXPos = token.nextToken();
                        String compYPos = token.nextToken();
                        tempComp = (ComponentInfo) components.getElementAt(Integer.valueOf(compID).intValue());
                        tempComp.setLocation(new Point(Integer.valueOf(compXPos).intValue(), Integer.valueOf(compYPos).intValue()));
                        if (initialise) tempComp.comp.setLocation(tempComp.getLocation().x, tempComp.getLocation().y);
                        line = project_file.readLine();
                    }
                } catch (java.util.NoSuchElementException e) {
                    JOptionPane.showMessageDialog(Workbench.mainframe, "Error in Location information", "Project file format error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            } else for (int i = 0; i < components.size(); i++) ((ComponentInfo) components.getElementAt(Integer.valueOf(i).intValue())).setLocation(new Point(0, 0));
            if (!line.equals("# End")) {
                JOptionPane.showMessageDialog(Workbench.mainframe, "# End line not found", "Project file format error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            project_file.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Workbench.mainframe, "IO error whilst reading project file", "Project file format error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void save() {
        try {
            RandomAccessFile project_file = new RandomAccessFile(filename, "rw");
            project_file.setLength(0);
            project_file.writeBytes("# Do NOT modify the project file manually!!\n");
            project_file.writeBytes("# Version 1.1\n");
            project_file.writeBytes("# Components:\n");
            for (int i = 0; i < components.size(); i++) {
                project_file.writeBytes(((ComponentInfo) components.getElementAt(i)).name + " ");
                project_file.writeBytes(((ComponentInfo) components.getElementAt(i)).data + "\n");
            }
            project_file.writeBytes("# Connections:\n");
            for (int i = 0; i < connections.size(); i++) {
                project_file.writeBytes(String.valueOf(((Connection) connections.getElementAt(i)).getSrcId()));
                project_file.writeBytes(":");
                project_file.writeBytes(String.valueOf(((Connection) connections.getElementAt(i)).getSrcPin()));
                project_file.writeBytes(",");
                project_file.writeBytes(String.valueOf(((Connection) connections.getElementAt(i)).getDestId()));
                project_file.writeBytes(":");
                project_file.writeBytes(String.valueOf(((Connection) connections.getElementAt(i)).getDestPin()));
                project_file.writeBytes("\n");
            }
            project_file.writeBytes("# Connection info:\n");
            for (int i = 0; i < connections.size(); i++) project_file.writeBytes(((Connection) connections.getElementAt(i)).getConnectionInfo() + "\n");
            project_file.writeBytes("# Locations:\n");
            for (int i = 0; i < components.size(); i++) {
                ComponentInfo tempComp = (ComponentInfo) components.getElementAt(i);
                project_file.writeBytes(String.valueOf(i) + ":" + tempComp.getLocation().x + "," + tempComp.getLocation().y + "\n");
            }
            project_file.writeBytes("# End\n");
            project_file.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Workbench.mainframe, "IO error whilst writing project file", "Project file format error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void upgradeToVersion1dot1() {
        File f;
        int components = 0;
        f = new File(filename + ".pd~");
        f.delete();
        try {
            RandomAccessFile old_project_file = new RandomAccessFile(filename, "r");
            RandomAccessFile new_project_file = new RandomAccessFile(filename + ".pd~", "rw");
            String line = " ";
            line = old_project_file.readLine();
            new_project_file.writeBytes(line + "\n");
            line = old_project_file.readLine();
            new_project_file.writeBytes("# Version 1.1\n");
            line = old_project_file.readLine();
            new_project_file.writeBytes(line + "\n");
            line = old_project_file.readLine();
            while (!(line.charAt(0) == '#')) {
                new_project_file.writeBytes(line + "\n");
                line = old_project_file.readLine();
                components += 1;
            }
            while (!line.equals("# End")) {
                new_project_file.writeBytes(line + "\n");
                line = old_project_file.readLine();
            }
            new_project_file.writeBytes("# Locations:\n");
            int deflocx = 40;
            int deflocy = 400;
            for (int i = 0; i < components; i++) {
                new_project_file.writeBytes(String.valueOf(i) + ":" + String.valueOf(deflocx) + "," + String.valueOf(deflocy) + "\n");
                deflocx += 10;
                deflocy += 50;
            }
            new_project_file.writeBytes("# End\n");
            old_project_file.close();
            new_project_file.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Workbench.mainframe, "IO error whilst reading project file", "Project file format error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        f = new File(filename);
        f.delete();
        f = new File(filename + ".pd~");
        f.renameTo(new File(filename));
    }

    public void saveLocations() {
        for (int i = 0; i < components.size(); i++) {
            ComponentInfo tempComp = (ComponentInfo) components.getElementAt(i);
            tempComp.setLocation(new Point(tempComp.comp.getLocation()));
        }
        save();
    }

    public String getFilename() {
        return filename;
    }
}
