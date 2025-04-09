import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

public class FileIconFilterDemo implements ActionListener
{
    JFrame f = null;
    JLabel label = null;
    JFileChooser fileChooser = null;
    
    public FileIconFilterDemo() 
    {
        f = new JFrame("FileIconFilter Demo");
        Container contentPane = f.getContentPane();
        
        JButton b = new JButton("打开文件");
        b.addActionListener(this);
        
        label = new JLabel(" ",JLabel.CENTER);
        label.setPreferredSize(new Dimension(150,30));
        contentPane.add(label,BorderLayout.CENTER);
        contentPane.add(b,BorderLayout.SOUTH);
        
        f.pack();
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }    
    
    public static void main(String[] args)
    {
        new FileIconFilterDemo();
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        fileChooser = new JFileChooser("c:\\winnt");
        fileChooser.addChoosableFileFilter(new JAVAFileFilter("class"));
        fileChooser.addChoosableFileFilter(new JAVAFileFilter("java"));
        fileChooser.setFileView(new FileIcon());
        int result = fileChooser.showOpenDialog(f);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            label.setText("您选择了："+file.getName()+"文件");
        }else if (result == fileChooser.CANCEL_OPTION){
            label.setText("您没有选择任何文件");
        }
    }
}

class JAVAFileFilter extends FileFilter
{
    String ext;
    
    public JAVAFileFilter(String ext)
    {
        this.ext = ext;
    }
    
    public boolean accept(File file)
    {
        if (file.isDirectory())
            return true;
        
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');

        if (index > 0 && index < fileName.length()-1) {
            String extension = fileName.substring(index+1).toLowerCase();
            if (extension.equals(ext))
                return true;
        }
        return false;
    }
    
    public String getDescription(){
        if (ext.equals("java"))
            return "JAVA Source File (*.java)";
        if (ext.equals("class"))
            return "JAVA Class File (*.class)";
        return "";
    }
}

class FileIcon extends FileView
{
    public String getName(File f) {
	    return null; 
             
    }

    public String getDescription(File f) {
	    return null; 
	
    }

    public String getTypeDescription(File f)
    {
        String extension = getExtensionName(f);
        if(extension.equals("java"))
            return "JAVA Source File";
        if(extension.equals("class"))
            return "JAVA Class File";
        return "";
    }

    public Icon getIcon(File f)
    {
        String extension = getExtensionName(f);
        if(extension.equals("java"))
            return new ImageIcon("java.gif");
        if(extension.equals("class"))
            return new ImageIcon("class.gif");
        return null;
	}

    public Boolean isTraversable(File f) {
    	return null; 
    }
    
    public String getExtensionName(File f)
    {
     	String extension ="";
	    String fileName = f.getName();
        int index = fileName.lastIndexOf('.');

        if (index > 0 && index < fileName.length()-1) {
            extension = fileName.substring(index+1).toLowerCase();
        }
        return extension;
    } 
} 

