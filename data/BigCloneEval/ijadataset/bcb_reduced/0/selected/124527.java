package cn.heapstack.MyScreenSnap;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MenuFrame {
	private static String VERSION = "2.0";
	
	private JFrame frame;
	
	private JPanel panel;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private FormListener formListener;
	private Robot robot;
	
	public static 	GraphicsEnvironment graphenv = GraphicsEnvironment.getLocalGraphicsEnvironment ();
	public static 	GraphicsDevice [] screens = graphenv.getScreenDevices ();	
	public static 	DisplayMode mode = screens [0].getDisplayMode ();    
	
	public MenuFrame()
	{
		initComponents();
	}
		
	public void initComponents()
	{
		frame = new JFrame("MyScreenSnap Version:2.0");
		panel = new JPanel();
		
		try {
			
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		} 
		
		button1 = new JButton("全屏截图");
		button2 = new JButton("选择截图");
		button3 = new JButton("退出系统");
		button4 = new JButton("关于程序");
		formListener = new FormListener();
		button1.addActionListener(formListener);	
		button2.addActionListener(formListener);	
		button3.addActionListener(formListener);	
		button4.addActionListener(formListener);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = new Dimension(250,250);
		
		frame.setPreferredSize(size );
		frame.setLocation(250,250);
		frame.setBackground(Color.black);
		
		panel.setLocation(0, 0);
		panel.setPreferredSize(size);
		panel.setOpaque(false);
		
		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		gbLayout.setConstraints(panel,c);
		panel.setLayout(gbLayout);
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2,2,5,5);
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(button1,c);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(button2,c);
		c.gridx = 0;
		c.gridy = 2;
		panel.add(new Panel(),c);
		c.gridx = 0;
		c.gridy = 3;
		panel.add(button4,c);
		c.gridx = 0;
		c.gridy = 4;
		panel.add(button3,c);
		
		frame.getContentPane().add(panel);		
		frame.pack();
		frame.setVisible(true);
	}

	class FormListener implements ActionListener
	{
		
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == button1)
			{
				System.out.println("Just capture the screen using a robot");
				frame.setVisible(false);
				Rectangle bounds = new Rectangle (0, 0, mode.getWidth (), mode.getHeight ());    
				
				BufferedImage bimg = robot.createScreenCapture(bounds);
								
				String fileName = String.valueOf(System.currentTimeMillis())+".png";
				File outputFile = new File(fileName);
				try 
				{
					ImageIO.write(bimg, "png", outputFile);
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				frame.setVisible(true);
				System.out.println("File save as "+fileName);
				
			}
			else if(e.getSource() == button2)
			{
				System.out.println("Create a full screen frame");
				TranslucentFrame tframe = new TranslucentFrame();

				tframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				tframe.setUndecorated(true);

				tframe.setSize(mode.getWidth(), mode.getHeight());
				
				if(!tframe.isShowing())
				{
					frame.setVisible(false);
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					tframe.updateBackground();
					tframe.setVisible(true);
					
					
					frame.setVisible(true);
					frame.toBack();
					
				}
				else
				{
					tframe.setVisible(true);
				}
			}
			else if(e.getSource() == button3)
			{
				frame.dispose();
				System.exit(0);
			}
			else if(e.getSource() == button4)
			{
			  
			        JOptionPane.showMessageDialog(frame, 
			                "A small tool to snap the screen.\r\n\r\n"
			                + "Version "+ MenuFrame.VERSION + "\r\nMade in March 2007 by Hao Ji.\r\n"
			                + " Added resize selected area feature June 2008.\r\n" 
			                + " Added save as *.BMP|*.GIF|*.JPG|*.PNG feature June 2008.\r\n"
			                + "\r\n"
			                + "<html><i>Resize selected area feature refer to 千里冰封</i></html>\r\n"
			                + "\r\n"
			                + "Contact me: jacky.jihao@gmail.com \r\n",
			                "About MyScreenSnap", 
			                JOptionPane.INFORMATION_MESSAGE);
			  
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				new MenuFrame();
			}
		});
	}

}
