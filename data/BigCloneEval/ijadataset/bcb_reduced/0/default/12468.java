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
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;

class PMonitorInputStream implements ActionListener
{
    JFrame f = null;
    JLabel label = null;
    JTextArea textarea = null;
    JFileChooser fileChooser = null;

    public PMonitorInputStream()
    {
        f = new JFrame("ProgressMonitorInputStream Example");
        Container contentPane = f.getContentPane();
        textarea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textarea);
        scrollPane.setPreferredSize(new Dimension(350,350));
        JButton b = new JButton("读取文件");
        b.addActionListener(this);
        label = new JLabel(" ",JLabel.CENTER);
        
        fileChooser = new JFileChooser();
        
        contentPane.add(label,BorderLayout.NORTH);
        contentPane.add(scrollPane,BorderLayout.CENTER);
        contentPane.add(b,BorderLayout.SOUTH);
        
        f.pack();
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    public static void main(String[] args) {
        new PMonitorInputStream();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        File file = null;
        int result = fileChooser.showOpenDialog(f);
        
        textarea.setText("");

        if (result == JFileChooser.APPROVE_OPTION)
        {
            file = fileChooser.getSelectedFile();
            label.setText("您选择的文件名称为："+file.getName());
        }
        else if(result == JFileChooser.CANCEL_OPTION)
        {
            label.setText("您没有选择任何文件");
        }
        
        FileInputStream inputStream;
        
        if(file != null)
        {
            try{
                inputStream = new FileInputStream(file);
            }catch(FileNotFoundException fe){
                label.setText("File Not Found");
                return;
            }
            
            ProgressMonitorInputStream pmInputStream = new 
                ProgressMonitorInputStream(f,      //parant component
                                   "Get File Content.....", //message
                                   inputStream);       //input stream
            
            ProgressMonitor pMonitor = 
                pmInputStream.getProgressMonitor();
            pMonitor.setMillisToDecideToPopup(10);
            pMonitor.setMillisToPopup(0);
            int readbyte;
    
            try{
                while( (readbyte = pmInputStream.read()) != -1)
                {
                    textarea.append(String.valueOf((char)readbyte));

                    try{
                        Thread.sleep(10);
                    }catch(InterruptedException ie){}
                    
                    if(pMonitor.isCanceled())
                    {
                        textarea.append("\n\n读取文件中断！！");
                    }
                }
            }catch(IOException ioe){
                label.setText("读取文件错误");
            }
            finally{
                try{
                    if(pmInputStream != null)
                        pmInputStream.close();
                }catch(IOException ioe2){}
            }
        }
    }
}


