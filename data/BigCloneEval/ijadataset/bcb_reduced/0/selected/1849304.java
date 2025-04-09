package cn.heapstack.MyScreenSnap;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class TranslucentFrame extends JFrame implements ComponentListener,
		WindowFocusListener, MouseListener, MouseMotionListener {

	private boolean flag_prepare = true;
	private BufferedImage background;
	private Robot robot;
	private Dimension size;
	
	private JFileChooser fcSave ;
	
	private int w = 0;
	private int h = 0;
	private int width = 0;//选择区域的宽度
	private int height = 0;//选择区域的高度

	Point head = new Point();//计算得出的左上角的那个点
	private Point startPoint;//鼠标按下的第一个点
	private Point lastPoint;//鼠标移动的最后一个点
	private Point tmpPoint;//调整时的选择点
	
	//参考了千里冰封的代码，加上八个点的编辑的功能
    private Rectangle selectRect=new Rectangle(0,0,0,0);//表示选中的区域
    private Cursor cs=new Cursor(Cursor.CROSSHAIR_CURSOR);//表示一般情况下的鼠标状态
    private States current=States.DEFAULT;// 表示当前的编辑状态
    private Rectangle[] rec;//表示八个编辑点的区域
    //下面四个常量,分别表示谁是被选中的那条线上的端点
    public static final int START_X=1;
    public static final int START_Y=2;
    public static final int END_X=3;
    public static final int END_Y=4;
    private int currentX,currentY;//当前被选中的X和Y,只有这两个需要改变
    
    
    public TranslucentFrame() {
		init();
		initRecs();
	}

	private void init() {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());		
			size = Toolkit.getDefaultToolkit().getScreenSize();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		startPoint = new Point();
		lastPoint = new Point();
		tmpPoint = new Point();
		
		this.fcSave = new JFileChooser();
		fcSave.setCurrentDirectory(new File("."));
		fcSave.setSelectedFile(null);
		fcSave.addChoosableFileFilter(new GIFfilter());
		fcSave.addChoosableFileFilter(new BMPfilter());
		fcSave.addChoosableFileFilter(new JPGfilter());
		fcSave.addChoosableFileFilter(new PNGfilter());
		
		this.addWindowFocusListener(this);
		this.addComponentListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		this.updateBackground();
	}

    private void initRecs(){
        rec=new Rectangle[8];
        for(int i=0;i<rec.length;i++){
            rec[i]=new Rectangle();
        }
    }
    
	public void updateBackground() {
		background = robot.createScreenCapture(new Rectangle(0, 0, (int) size
				.getWidth(), (int) size.getHeight()));
	}

	public void refresh() {
		this.repaint();
	}

	public void repaint() {

		Graphics g = this.getGraphics();
		g.setColor(Color.red);
		w = lastPoint.x - startPoint.x;
		h = lastPoint.y - startPoint.y;
		width = Math.abs(w);
		height = Math.abs(h);

		// don't need to clear Rect now, just paint the background, it feels good
		//g.clearRect(startPoint.x, startPoint.y, width, height);
		this.paintComponents(g);
		
		if (this.flag_prepare) {
			g.setColor(Color.lightGray);
			g.fillRect(lastPoint.x+20, lastPoint.y, 150, 20);
			g.drawRect(lastPoint.x+20, lastPoint.y, 150, 20);
			g.setColor(Color.red);
			g.drawString("点击鼠标拖动,开始截图!", lastPoint.x+25, lastPoint.y+15);
		}
		else
		{
			if (((w) < 0) && ((h) < 0)) {
				head.x = lastPoint.x;
				head.y = lastPoint.y;
			} else if (((w) > 0) && ((h) < 0)) {
				head.x = startPoint.x;
				head.y = lastPoint.y;

			} else if (((w) < 0) && ((h) > 0)) {
				head.x = lastPoint.x;
				head.y = startPoint.y;
			} else if (((w) > 0) && ((h) > 0)) {
				head.x = startPoint.x;
				head.y = startPoint.y;
			}
			
			selectRect.x = head.x;
			selectRect.y = head.y;
			selectRect.width = width;
			selectRect.height = height;
			
			g.drawString(" "+ width+"*"+height+" ", head.x, head.y-15);
			g.drawRect(head.x, head.y, width, height);
			
			
			g.fillRect(head.x-1, head.y-1, 3, 3);
			g.fillRect(head.x-1, head.y+height-1, 3, 3);
			g.fillRect(head.x-1, head.y+height/2-1, 3, 3);
			g.fillRect(head.x+width-1, head.y-1, 3, 3);
			g.fillRect(head.x+width/2-1, head.y-1, 3, 3);
			g.fillRect(head.x+width-1, head.y+height-1, 3, 3);
			g.fillRect(head.x+width-1, head.y+height/2-1, 3, 3);
			g.fillRect(head.x+width/2-1, head.y+height-1, 3, 3);
			
			//NORTHWEST
			rec[0].x = head.x-5;
			rec[0].y = head.y-5;
			//NORTH
			rec[1].x = head.x+width/2-5;
			rec[1].y = head.y-5;
			//NORTHEAST
			rec[2].x = head.x+width-5;
			rec[2].y = head.y-5;
			//EAST
			rec[3].x = head.x+width-5;
			rec[3].y = head.y+height/2-5;
			//SOUTHEAST
			rec[4].x = head.x+width-5;
			rec[4].y = head.y+height-5;
			//SOUTH
			rec[5].x = head.x+width/2-5;
			rec[5].y = head.y+height-5;
			//SOUTHWEST
			rec[6].x = head.x-5;
			rec[6].y = head.y+height-5;
			//WEST
			rec[7].x = head.x-5;
			rec[7].y = head.y+height/2-5;
			
			for(int i=0;i<rec.length;i++){
	            rec[i].height = 10;
	            rec[i].width = 10;
	        }
		}
	}

	public void paintComponents(Graphics g) {
		Point pos = this.getLocationOnScreen();
		Point offset = new Point(-pos.x, -pos.y);
		g.drawImage(background, offset.x, offset.y, null);
	}

	private static final long serialVersionUID = 3690836343560995785L;

/*	public static void main(String[] args) {
		TranslucentFrame frame = new TranslucentFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setSize(2000, 2000);
		frame.setVisible(true);
	}*/

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
		this.refresh();
	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
		this.refresh();
	}

	public void windowGainedFocus(WindowEvent e) {
		//this.refresh();
	}

	public void windowLostFocus(WindowEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		doMouseMoved(e);
		initSelect(current);
		if(flag_prepare)
		{
			this.lastPoint.x = e.getX();
			this.lastPoint.y = e.getY();
			repaint();
		}
	}
    //特意定义一个方法处理鼠标移动,是为了每次都能初始化一下所要选择的地区
    private void doMouseMoved(MouseEvent me){
        if(selectRect.contains(me.getPoint())){
            this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            current=States.MOVE;
        } else{
            States[] st=States.values();
            for(int i=0;i<rec.length;i++){
                if(rec[i].contains(me.getPoint())){
                    current=st[i];
                    this.setCursor(st[i].getCursor());
                    return;
                }
            }
            this.setCursor(cs);
            current=States.DEFAULT;
        }
    }

	public void mousePressed(MouseEvent e) {
		//System.out.println("Mouse pressed , set the start point,x:" + e.getX()+ " y:" + e.getY());
		if (e.getButton() == MouseEvent.BUTTON3) {
			//System.out.println("Get right mouse");
			this.setVisible(false);
			this.dispose();
			
		} else {
			this.flag_prepare = false;
			this.tmpPoint.x = e.getX();
			this.tmpPoint.y = e.getY();
			
			if(startPoint.x==0)
				this.startPoint.x = tmpPoint.x;
			if(startPoint.y==0)
				this.startPoint.y = tmpPoint.y;
		}
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println("Mouse released , set the last point,x:" + e.getX()+ " y:" + e.getY());
		if(e.isPopupTrigger()){
            if(current==States.MOVE){
            	
                startPoint.x = 0;
                startPoint.y = 0;
                lastPoint.x = 0;
                lastPoint.y = 0;
                tmpPoint.x = 0;
                tmpPoint.y = 0;
                
                this.flag_prepare = true;
            }
        }
		if (this.isShowing())
			repaint();
	}

	public void mouseClicked(MouseEvent e) {
		 if(e.getClickCount()==2){
			 BufferedImage bimg = robot.createScreenCapture(new Rectangle(head.x+2, head.y+2, width-3, height-3));;
				if (fcSave.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					File file = fcSave.getSelectedFile();
					StringBuilder path = new StringBuilder( file.getAbsolutePath().toLowerCase() );
					String formart = "png";
					javax.swing.filechooser.FileFilter ff = fcSave.getFileFilter();
					
					if(ff instanceof BMPfilter)
					{
						formart = "bmp";
						
					}else if(ff instanceof JPGfilter)
					{
						formart = "jpg";
						
					}else if(ff instanceof GIFfilter)
					{
						formart = "gif";
						
					}
					
					if (!path.toString().endsWith("."+formart))
					{
						path = path.append(".");
						path.append(formart);
						file = new File(path.toString());
					}
					
					try {
						ImageIO.write(bimg, formart.toUpperCase(), file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					this.dispose();
				}
				else
				{
					this.dispose();
					/*
					this.setVisible(false);
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					this.setVisible(true);*/
				}
		 }
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		 int x=e.getX();
         int y=e.getY();
         if(current==States.MOVE){
             startPoint.x+=(x-tmpPoint.x);
             startPoint.y+=(y-tmpPoint.y);
             lastPoint.x+=(x-tmpPoint.x);
             lastPoint.y+=(y-tmpPoint.y);
             tmpPoint.x=x;
             tmpPoint.y=y;
         }else if(current==States.EAST||current==States.WEST){
             if(currentX==START_X){
                 startPoint.x+=(x-tmpPoint.x);
                 tmpPoint.x=x;
             }else{
                 lastPoint.x+=(x-tmpPoint.x);
                 tmpPoint.x=x;
             }
         }else if(current==States.NORTH||current==States.SOUTH){
             if(currentY==START_Y){
                 startPoint.y+=(y-tmpPoint.y);
                 tmpPoint.y=y;
             }else{
                 lastPoint.y+=(y-tmpPoint.y);
                 tmpPoint.y=y;
             }
         }else if(current==States.NORTH_EAST||current==States.NORTH_EAST||
                 current==States.SOUTH_EAST||current==States.SOUTH_WEST){
             if(currentY==START_Y){
                 startPoint.y+=(y-tmpPoint.y);
                 tmpPoint.y=y;
             }else{
                 lastPoint.y+=(y-tmpPoint.y);
                 tmpPoint.y=y;
             }
             if(currentX==START_X){
                 startPoint.x+=(x-tmpPoint.x);
                 tmpPoint.x=x;
             }else{
                 lastPoint.x+=(x-tmpPoint.x);
                 tmpPoint.x=x;
             }
             
         }else{
			this.lastPoint.x = e.getX();
			this.lastPoint.y = e.getY();
	     }
		repaint();
	}
	
	//根据东南西北等八个方向决定选中的要修改的X和Y的座标
    private void initSelect(States state){
        switch(state){
            case DEFAULT:
                currentX=0;
                currentY=0;
                break;
            case EAST:
                currentX=(w>0?END_X:START_X);
                currentY=0;
                break;
            case WEST:
                currentX=(w>0?START_X:END_X);
                currentY=0;
                break;
            case NORTH:
                currentX=0;
                currentY=(h<0?END_Y:START_Y);
                break;
            case SOUTH:
                currentX=0;
                currentY=(h<0?START_Y:END_Y);
                break;
            case NORTH_EAST:
                currentY=(h<0?END_Y:START_Y);
                currentX=(w>0?END_X:START_X);
                break;
            case NORTH_WEST:
                currentY=(h<0?END_Y:START_Y);
                currentX=(w>0?START_X:END_X);
                break;
            case SOUTH_EAST:
                currentY=(h<0?START_Y:END_Y);
                currentX=(w>0?END_X:START_X);
                break;
            case SOUTH_WEST:
                currentY=(h<0?START_Y:END_Y);
                currentX=(w>0?START_X:END_X);
                break;
            default:
                currentX=0;
                currentY=0;
                break;
        }
    }

}

//一些表示状态的枚举
enum States{
    NORTH_WEST(new Cursor(Cursor.NW_RESIZE_CURSOR)),
    NORTH(new Cursor(Cursor.N_RESIZE_CURSOR)),
    NORTH_EAST(new Cursor(Cursor.NE_RESIZE_CURSOR)),
    EAST(new Cursor(Cursor.E_RESIZE_CURSOR)),
    SOUTH_EAST(new Cursor(Cursor.SE_RESIZE_CURSOR)),
    SOUTH(new Cursor(Cursor.S_RESIZE_CURSOR)),
    SOUTH_WEST(new Cursor(Cursor.SW_RESIZE_CURSOR)),
    WEST(new Cursor(Cursor.W_RESIZE_CURSOR)),
    MOVE(new Cursor(Cursor.MOVE_CURSOR)),
    DEFAULT(new Cursor(Cursor.DEFAULT_CURSOR));
    private Cursor cs;
    States(Cursor cs){
        this.cs=cs;
    }
    public Cursor getCursor(){
        return cs;
    }
}

        
