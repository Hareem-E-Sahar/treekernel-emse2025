package d2.util.statevisualizer;

import gatech.mmpm.Entity;
import gatech.mmpm.GameState;
import gatech.mmpm.PhysicalEntity;
import gatech.mmpm.TwoDMap;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StateVisualizer extends JFrame {

    private static final long serialVersionUID = 4182862635710825999L;

    GameState m_state = null;

    Random rand = null;

    Graphics2D m_graph2D = null;

    BufferStrategy m_bufferStrategy = null;

    double m_center_x = 0, m_center_y = 0;

    double m_max_zoom = 64.0, m_min_zoom = 0.015625;

    boolean m_vp_autozoom = true;

    double m_vp_viewed_zoom = 1.0;

    double m_vp_zoom = 1.0;

    HashMap<Class<?>, Color> m_ClassColors = new HashMap<Class<?>, Color>();

    public int toScreenX(double x) {
        return (int) ((((x - m_center_x)) * m_vp_viewed_zoom) + (getWidth()) / 2);
    }

    public int toScreenY(double y) {
        return (int) ((((y - m_center_y)) * m_vp_viewed_zoom) + (getHeight() / 2));
    }

    public int toScreen(double v) {
        return (int) (v * m_vp_viewed_zoom);
    }

    public double fromScreenX(double x) {
        return (int) (((x - (getWidth() / 2)) / m_vp_viewed_zoom) + (getWidth() / 2));
    }

    public double fromScreenY(double y) {
        return (int) (((y - (getHeight() / 2)) / m_vp_viewed_zoom) + (getHeight() / 2));
    }

    public double fromScreen(double v) {
        return (int) (v / m_vp_viewed_zoom);
    }

    public void setGameState(GameState gs) {
        m_state = gs;
    }

    public StateVisualizer(GameState state, int a_width, int a_height) {
        super("D2 Game State Visualization");
        m_state = state;
        rand = new Random();
        {
            JPanel panel = new JPanel();
            getContentPane().add(panel);
            JButton button1 = new JButton("Zoom in");
            button1.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (m_vp_zoom < m_max_zoom) {
                        m_vp_zoom *= 1.25;
                    }
                    m_vp_autozoom = false;
                }
            });
            JButton button2 = new JButton("Zoom out");
            button2.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (m_vp_zoom > m_min_zoom) {
                        m_vp_zoom /= 1.25;
                    }
                    m_vp_autozoom = false;
                }
            });
            JButton button3 = new JButton("Auto zoom");
            button3.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    m_vp_autozoom = true;
                }
            });
            panel.add(button1);
            panel.add(button2);
            panel.add(button3);
        }
        setSize(a_width, a_height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        createBufferStrategy(2);
        m_bufferStrategy = getBufferStrategy();
        m_graph2D = (Graphics2D) m_bufferStrategy.getDrawGraphics();
        {
        }
        class StateVisualizerUpdater extends Thread {

            StateVisualizer v;

            public StateVisualizerUpdater(StateVisualizer a_v) {
                v = a_v;
            }

            public void run() {
                do {
                    try {
                        {
                            float min_x = 0, max_x = 0;
                            float min_y = 0, max_y = 0;
                            min_x = 0;
                            min_y = 0;
                            max_x = 1;
                            max_y = 1;
                            if (m_state.getMap() != null && m_state.getMap() instanceof TwoDMap) {
                                TwoDMap m = (TwoDMap) m_state.getMap();
                                max_x = m.getSizeInDimension(0) * m.getCellSizeInDimension(0);
                                max_y = m.getSizeInDimension(1) * m.getCellSizeInDimension(1);
                            }
                            float border = (max_x - min_x) * 0.05f;
                            min_x -= border;
                            max_x += border;
                            min_y -= border;
                            max_y += border;
                            m_center_x = (min_x + max_x) / 2;
                            m_center_y = (min_y + max_y) / 2;
                            if (m_vp_autozoom && max_x - min_x != 0 && max_y - min_y != 0) {
                                double required_zoom_x = ((double) getWidth()) / (max_x - min_x);
                                double required_zoom_y = ((double) getHeight() - 67) / (max_y - min_y);
                                double required_zoom = Math.min(required_zoom_x, required_zoom_y);
                                m_vp_zoom = required_zoom;
                            }
                            if (m_vp_zoom > m_max_zoom) {
                                m_vp_zoom = m_max_zoom;
                            }
                            if (m_vp_zoom < m_min_zoom) {
                                m_vp_zoom = m_min_zoom;
                            }
                            if (m_vp_viewed_zoom != m_vp_zoom) {
                                m_vp_viewed_zoom = (3 * m_vp_viewed_zoom + m_vp_zoom) / 4;
                            }
                        }
                        v.update(m_graph2D);
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (v.isShowing());
            }
        }
        StateVisualizerUpdater updater = new StateVisualizerUpdater(this);
        new Thread(updater).start();
    }

    Color getEntityColor(PhysicalEntity pe) {
        Class<?> cl = pe.getClass();
        Color co = m_ClassColors.get(cl);
        if (co == null) {
            co = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            m_ClassColors.put(cl, co);
        }
        return co;
    }

    public void paint(Graphics g) {
        try {
            super.paint(g);
            g.setClip(0, 67, getWidth(), getHeight() - 67);
            if (m_state.getMap() instanceof TwoDMap) {
                TwoDMap m = (TwoDMap) m_state.getMap();
                for (int y = 0; y < m.getSizeInDimension(1); y++) {
                    for (int x = 0; x < m.getSizeInDimension(0); x++) {
                        Entity e = m.getCellLocation(new int[] { x, y });
                        if (e != null && e instanceof PhysicalEntity) {
                            PhysicalEntity pe = (PhysicalEntity) e;
                            g.setColor(getEntityColor(pe));
                            int ex = toScreenX(pe.getx());
                            int ey = toScreenY(pe.gety());
                            g.fillRect(ex, ey, toScreen(pe.getwidth()), toScreen(pe.getlength()));
                        }
                    }
                }
            }
            for (Entity e : m_state.getAllEntities()) {
                if (e != null && e instanceof PhysicalEntity) {
                    PhysicalEntity pe = (PhysicalEntity) e;
                    g.setColor(getEntityColor(pe));
                    int ex = toScreenX(pe.getx());
                    int ey = toScreenY(pe.gety());
                    g.fillRect(ex, ey, toScreen(pe.getwidth()), toScreen(pe.getlength()));
                }
            }
        } catch (Exception e) {
        }
        if (m_graph2D != null) {
            m_graph2D.dispose();
        }
        if (m_bufferStrategy != null) {
            try {
                m_bufferStrategy.show();
                m_graph2D = (Graphics2D) m_bufferStrategy.getDrawGraphics();
            } catch (Exception e) {
            }
        }
    }
}
