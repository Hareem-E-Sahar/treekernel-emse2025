import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class FileChooserDemo1 implements ActionListener
{
    JFrame f = null;
    JLabel label = null;
    JTextArea textarea = null;
    JFileChooser fileChooser = null;

    public FileChooserDemo1()
    {
        f = new JFrame("FileChooser Example");
        Container contentPane = f.getContentPane();
        textarea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textarea);
        scrollPane.setPreferredSize(new Dimension(350,300));
        
        JPanel panel = new JPanel();
        JButton b1 = new JButton("新建文件");
        b1.addActionListener(this);
        JButton b2 = new JButton("存储文件");
        b2.addActionListener(this);
        panel.add(b1);
        panel.add(b2);
        
        label = new JLabel(" ",JLabel.CENTER);
        
        fileChooser = new JFileChooser("D:\\");
        
        contentPane.add(label,BorderLayout.NORTH);
        contentPane.add(scrollPane,BorderLayout.CENTER);
        contentPane.add(panel,BorderLayout.SOUTH);
        
        f.pack();
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    public static void main(String[] args) {
        new FileChooserDemo1();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        File file = null;
        int result;
        
        if (e.getActionCommand().equals("新建文件"))
        {
            fileChooser.setApproveButtonText("确定");
            fileChooser.setDialogTitle("打开文件");
            result = fileChooser.showOpenDialog(f);
            
            textarea.setText("");
    
            if (result == JFileChooser.APPROVE_OPTION)
            {
                file = fileChooser.getSelectedFile();
                label.setText("您选择打开的文件名称为："+file.getName());
            }
            else if(result == JFileChooser.CANCEL_OPTION)
            {
                label.setText("您没有选择任何文件");
            }
            
            FileInputStream fileInStream = null;
            
            if(file != null)
            {
                try{
                    fileInStream = new FileInputStream(file);
                }catch(FileNotFoundException fe){
                    label.setText("File Not Found");
                    return;
                }
                
                int readbyte;
        
                try{
                    while( (readbyte = fileInStream.read()) != -1)
                    {
                        textarea.append(String.valueOf((char)readbyte));
                    }
                }catch(IOException ioe){
                    label.setText("读取文件错误");
                }
                finally{
                    try{
                        if(fileInStream != null)
                            fileInStream.close();
                    }catch(IOException ioe2){}
                }
            }
        }
        
        if (e.getActionCommand().equals("存储文件"))
        {
            result = fileChooser.showSaveDialog(f);
            file = null;
            String fileName;
        
            if (result == JFileChooser.APPROVE_OPTION)
            {
                file = fileChooser.getSelectedFile();
                label.setText("您选择存储的文件名称为："+file.getName());
            }
            else if(result == JFileChooser.CANCEL_OPTION)
            {
                label.setText("您没有选择任何文件");
            }
            
            FileOutputStream fileOutStream = null;
            
            if(file != null)
            {
                try{
                    fileOutStream = new FileOutputStream(file);
                }catch(FileNotFoundException fe){
                    label.setText("File Not Found");
                    return;
                }

                String content = textarea.getText();
                
                try{
                    fileOutStream.write(content.getBytes());
                }catch(IOException ioe){
                    label.setText("写入文件错误");
                }
                finally{
                    try{
                        if(fileOutStream != null)
                            fileOutStream.close();
                    }catch(IOException ioe2){}
                }
            }
        }
    }
}
