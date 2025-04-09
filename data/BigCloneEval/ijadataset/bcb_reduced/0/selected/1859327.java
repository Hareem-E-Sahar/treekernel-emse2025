package Simulation_01_10_mydrawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * MyShape:
 * �ӿ�.������ɫ���Ƿ�Ϊ�յȺ���
 * @author student
 * @version 1.0 * 
 */
interface MyShape extends Serializable {

    public Color getColor();

    public void setColor(Color c);

    public boolean isFill();

    public void setIsFill(boolean b);
}

/**
 * PaintPanel:
 * ���ڻ�ͼ�İ��棬ͨ�����������ʵ���������ʱ��һЩ��������˫��Ⱦɫ���϶���ͼ��
 * @author student
 * @version 1.0 
 */
class PaintPanel extends JPanel {

    public ArrayList<Shape> list = new ArrayList<Shape>();

    private Shape curShape;

    private int x1, x2, y1, y2;

    private int x[], y[];

    /**
	 * PaintPanel��
	 * ͨ��������������ʵ���������ʱ��һЩ��������˫�����϶���������ɿ����ʱ�Ĳ�����
	 * @param app ����J7�Ķ���
	 */
    public PaintPanel() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {

            /**
			 * mouseClicked��
			 * ˫�����Ⱦɫ����˫��ʱȥ��Ⱦɫ
			 */
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) for (int i = 0; i < list.size(); i++) {
                    if ((list.get(i)).contains(e.getX(), e.getY())) {
                        ((MyShape) list.get(i)).setIsFill(!((MyShape) list.get(i)).isFill());
                        repaint();
                        break;
                    }
                }
            }

            /**
			 * mousePressed��
			 * ������ʱ�õ��������ֵ
			 */
            public void mousePressed(MouseEvent e) {
                x1 = e.getX();
                y1 = e.getY();
            }

            /**
			 *  mouseReleased��
			 *  �ɿ����ʱ����ǰͼ�β�Ϊ�գ��򽫵�ǰͼ����ӵ�list�У�����curShape���
			 */
            public void mouseReleased(MouseEvent e) {
                if (curShape != null) {
                    list.add(curShape);
                    curShape = null;
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {

            /**
			 * mouseDragged��
			 * �϶����ʱ�õ��������ֵ�����type�Ĳ�ͬ������ͬ��ͼ��
			 */
            public void mouseDragged(MouseEvent e) {
                int type = J7.type;
                x2 = e.getX();
                y2 = e.getY();
                switch(type) {
                    case 1:
                        curShape = new MyLine(x1, y1, x2, y2);
                        break;
                    case 2:
                        curShape = new MyArc(x1, y1, x2, y2);
                        break;
                    case 3:
                        curShape = new MyEllipseArc(x1, y1, x2, y2);
                        break;
                    case 4:
                        curShape = new MyRound(x1, y1, x2, y2);
                        break;
                    case 5:
                        curShape = new MyEllipse(x1, y1, x2, y2);
                        break;
                    default:
                        type = type - 5;
                        MyPolygon.getPoint(x1, y1, x2, y2, type);
                        curShape = new MyPolygon(type);
                }
                ((MyShape) curShape).setColor(J7.color);
                repaint();
            }
        });
    }

    /**
	 * paintComponent��
	 * ��list�е�����ͼ�λ������������isFill��ֵ�����������ɫ��񣩣����һ�����ǰͼ��
	 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                g2.setColor(((MyShape) list.get(i)).getColor());
                if (((MyShape) list.get(i)).isFill()) g2.fill((Shape) list.get(i)); else g2.draw((Shape) list.get(i));
            }
        }
        if (curShape != null) {
            g2.setColor(((MyShape) curShape).getColor());
            g2.draw(curShape);
        }
    }
}

/**
 * MyLine��
 * ��ֱ��
 * @author student
 * @version 1.0 
 */
class MyLine extends Line2D.Double implements MyShape {

    private Color color;

    public MyLine(int ax1, int ay1, int ax2, int ay2) {
        super(ax1, ay1, ax2, ay2);
    }

    public boolean isFill() {
        return false;
    }

    public void setIsFill(boolean b) {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
  * MyArc��
  * ��Բ��
  * @author student
  * @version 1.0 
  */
class MyArc extends Arc2D.Double implements MyShape {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Color color;

    private double leftX, topY, width, height, r;

    private double startArc = 40, extentArc = 80;

    public MyArc(int ax1, int ay1, int ax2, int ay2) {
        leftX = ax1 < ax2 ? ax1 : ax2;
        topY = ay1 > ay2 ? ay2 : ay1;
        width = Math.abs(ax1 - ax2);
        height = Math.abs(ay1 - ay2);
        r = (width + height) / 2;
        Arc2D myArc = new Arc2D.Double(leftX, topY, r, r, startArc, extentArc, Arc2D.OPEN);
        this.setArc(myArc);
    }

    public boolean isFill() {
        return false;
    }

    public void setIsFill(boolean b) {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
   * MyEllipseArc��
   * ����Բ��
   * @author student
   * @version 1.0 
   */
class MyEllipseArc extends Arc2D.Double implements MyShape {

    private Color color;

    private double leftX, topY, width, height;

    private double startArc = 30, extentArc = 100;

    public MyEllipseArc(int ax1, int ay1, int ax2, int ay2) {
        leftX = ax1 < ax2 ? ax1 : ax2;
        topY = ay1 > ay2 ? ay2 : ay1;
        width = Math.abs(ax1 - ax2);
        height = Math.abs(ay1 - ay2);
        Arc2D myArc = new Arc2D.Double(leftX, topY, width, height, startArc, extentArc, Arc2D.OPEN);
        this.setArc(myArc);
    }

    public boolean isFill() {
        return false;
    }

    public void setIsFill(boolean b) {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
    * MyRound��
    * ��Բ
    * @author student
    * @version 1.0 
    */
class MyRound extends Ellipse2D.Double implements MyShape {

    private Color color;

    private boolean isFill;

    private double leftX, topY, width, height, d;

    public MyRound(int ax1, int ay1, int ax2, int ay2) {
        leftX = ax1 < ax2 ? ax1 : ax2;
        topY = ay1 > ay2 ? ay2 : ay1;
        width = Math.abs(ax1 - ax2);
        height = Math.abs(ay1 - ay2);
        d = (width + height) / 2;
        this.setFrame(leftX, topY, d, d);
    }

    public boolean isFill() {
        return isFill;
    }

    public void setIsFill(boolean b) {
        isFill = b;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
     * MyEllipse��
     * ����Բ
     * @author student
     * @version 1.0 
     */
class MyEllipse extends Ellipse2D.Double implements MyShape {

    private Color color;

    private boolean isFill;

    private double leftX, topY, width, height;

    public MyEllipse(int ax1, int ay1, int ax2, int ay2) {
        leftX = ax1 < ax2 ? ax1 : ax2;
        topY = ay1 > ay2 ? ay2 : ay1;
        width = Math.abs(ax1 - ax2);
        height = Math.abs(ay1 - ay2);
        this.setFrame(leftX, topY, width, height);
    }

    public boolean isFill() {
        return isFill;
    }

    public void setIsFill(boolean b) {
        isFill = b;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
      * MyPolygon��
      * �������
      * @author student
      * @version 1.0 
      */
class MyPolygon extends Polygon implements MyShape {

    private Color color;

    private boolean isFill;

    private static int x[], y[];

    private static double r, angle, centerX, centerY;

    /**
     	  * getPoint��
     	  * ���������εĶ���
     	  * @param ax1 �����ʼ��ĺ����
     	  * @param ay1 �����ʼ��������
     	  * @param ax2 ����϶���ĺ����
     	  * @param ay2 ����϶���������
     	  * @param n   ����Ķ���εı���
     	  */
    public static void getPoint(int ax1, int ay1, int ax2, int ay2, int n) {
        if (n < 3) JOptionPane.showMessageDialog(null, "��������Ϊ3");
        x = new int[n];
        y = new int[n];
        r = Math.sqrt(Math.pow((ax1 - ax2), 2) + Math.pow((ay1 - ay2), 2)) / 2;
        angle = 2 * Math.PI / n;
        centerX = (ax2 + ax1) / 2;
        centerY = (ay2 + ay1) / 2;
        for (int i = 0; i < n; i++) {
            x[i] = (int) (r * Math.cos(angle * i) + centerX);
            y[i] = (int) (r * Math.sin(angle * i) + centerY);
        }
    }

    public MyPolygon(int n) {
        super(x, y, n);
    }

    public boolean isFill() {
        return isFill;
    }

    public void setIsFill(boolean b) {
        isFill = b;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }
}

/**
 * J7:
 * ���ࣺʵ���˽��棬����ÿ����ť�Լ�ѡɫ��������¼�������
 * @author ����
 * @version 1.0
 *
 */
public class J7 extends JFrame {

    private JColorChooser chooser = new JColorChooser();

    public static Color color;

    private boolean undo = true;

    public static int type;

    private JPanel p1 = new JPanel();

    private JButton undoB = new JButton("undo/redo");

    private JButton saveB = new JButton("����");

    private JPanel p2 = new JPanel();

    private JPanel p20 = new JPanel();

    public PaintPanel paint = new PaintPanel();

    private JButton lineB = new JButton("ֱ��");

    private JButton arcB = new JButton("Բ��");

    private JButton ellipseArcB = new JButton("��Բ��");

    private JButton roundB = new JButton("Բ");

    private JButton ellipseB = new JButton("��Բ");

    private JButton polygonB = new JButton("�����");

    private JLabel label = new JLabel("����");

    private JTextField text = new JTextField("", 2);

    /**
	 * J7:
	 * ���췽����ʵ���˽��棬��ע���¼�������
	 */
    public J7() {
        Container c = getContentPane();
        c.add(p1, BorderLayout.NORTH);
        p1.add(undoB);
        p1.add(saveB);
        c.add(chooser, BorderLayout.WEST);
        c.add(p2, BorderLayout.EAST);
        p2.setLayout(new BorderLayout());
        p2.add(p20, BorderLayout.NORTH);
        p2.add(paint, BorderLayout.CENTER);
        p20.add(lineB);
        p20.add(arcB);
        p20.add(ellipseArcB);
        p20.add(roundB);
        p20.add(ellipseB);
        p20.add(polygonB);
        p20.add(label);
        p20.add(text);
        undoB.addActionListener(new undoHandler());
        saveB.addActionListener(new saveHandler());
        lineB.addActionListener(new paintHandler());
        arcB.addActionListener(new paintHandler());
        ellipseArcB.addActionListener(new paintHandler());
        roundB.addActionListener(new paintHandler());
        ellipseB.addActionListener(new paintHandler());
        polygonB.addActionListener(new paintHandler());
        chooser.getSelectionModel().addChangeListener(new ChangeListener() {

            /**
			 * stateChanged��
			 * Ϊѡɫ����������ڲ��࣬ʵ��ѡɫ�Ĳ���
			 */
            public void stateChanged(ChangeEvent e) {
                color = chooser.getColor();
            }
        });
        setSize(1000, 700);
        setVisible(true);
    }

    /**
	 * undoHandler��
	 * �ڲ��ࣺʵ���˵㡰undo/redo����ť�Ĳ���������������һ��ͼ�Σ��ٵ���ָ����һ��ͼ��
	 * @author ����
	 * @version 1.0
	 */
    private class undoHandler implements ActionListener {

        private Shape lastShape;

        public void actionPerformed(ActionEvent e) {
            if (undo) {
                lastShape = paint.list.remove(paint.list.size() - 1);
                undo = false;
            } else {
                paint.list.add(lastShape);
                undo = true;
            }
            repaint();
        }
    }

    /**
	 * saveHandler��
	 * �ڲ��ࣺʵ���˵㡰���桱��ť�Ĳ���������Ϣ������ָ���ļ���
	 * @author ����
	 * @version 1.0
	 */
    private class saveHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("7.txt"));
                oos.writeObject(paint.list);
                oos.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "�ѱ���");
        }
    }

    /**
	 * paintHandler��
	 * �ڲ��ࣺʵ���˵�6����ʾͼ�εİ�ť�Ĳ����������Ĳ�ͬ�İ�ť����type���費ͬ��ֵ
	 * @author ����
	 * @version 1.0
	 */
    private class paintHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == lineB) type = 1;
            if (e.getSource() == arcB) type = 2;
            if (e.getSource() == ellipseArcB) type = 3;
            if (e.getSource() == roundB) type = 4;
            if (e.getSource() == ellipseB) type = 5;
            if (e.getSource() == polygonB) type = 5 + Integer.parseInt(text.getText());
        }
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        J7 app = new J7();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
