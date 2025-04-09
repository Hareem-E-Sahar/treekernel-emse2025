package jsynoptic.plugins.java3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;
import javax.swing.JFrame;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * A Frame to display a 3D scene
 */
public class Java3dFrame extends JFrame implements MouseListener, MouseMotionListener, KeyListener {

    protected static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private Universe _universe;

    private ViewTransform _viewTransform;

    protected Canvas3D _canvas;

    boolean _rotation;

    boolean _zoom;

    int _button;

    int _pickX;

    int _pickY;

    HashSet<Node> _selection;

    final NodeSelector _selector;

    public Java3dFrame(String title, int x, int y, int width, int height, Universe universe, NodeSelector selector) {
        super(SimpleUniverse.getPreferredConfiguration());
        _selector = selector;
        getContentPane().setLayout(new BorderLayout());
        if (width < 0) {
            width = screenSize.width / 2;
        }
        if (height < 0) {
            height = screenSize.height / 2;
        }
        if (x < 0) {
            x = (screenSize.width - width) / 2;
        }
        if (y < 0) {
            y = (screenSize.height - height) / 2;
        }
        setLocation(x, y);
        setSize(width, height);
        _universe = universe;
        _canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        _universe.addCanvas(_canvas);
        _viewTransform = new ViewTransform(_universe.getViewingPlatform(_canvas));
        _viewTransform.autoZoom();
        getContentPane().add(_canvas, BorderLayout.CENTER);
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                _universe.removeCanvas(_canvas);
            }
        });
        _rotation = true;
        _zoom = false;
        _button = 0;
        _pickX = -1;
        _pickY = -1;
        _selection = new HashSet<Node>();
        _canvas.addMouseListener(this);
        _canvas.addMouseMotionListener(this);
        _canvas.addKeyListener(this);
        setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            _viewTransform.autoZoom();
        } else if (e.getKeyChar() == KeyEvent.VK_SPACE) {
            _viewTransform.changeProjection();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        _button = e.getButton();
        if (_button == MouseEvent.BUTTON1) {
            if ((e.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) {
                pick(e.getX(), e.getY(), true);
            } else {
                pick(e.getX(), e.getY(), false);
            }
        } else if (_button == MouseEvent.BUTTON2) {
            if ((e.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) {
                _rotation = false;
                _zoom = false;
            } else if ((e.getModifiers() & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK) {
                _rotation = false;
                _zoom = true;
            } else {
                _rotation = true;
                _zoom = false;
            }
            _viewTransform.init2DPosition(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        _button = 0;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (_button == MouseEvent.BUTTON1) {
            if ((e.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) {
                pickArea(e.getX(), e.getY(), true);
            } else {
                pickArea(e.getX(), e.getY(), false);
            }
        }
        if (_button == MouseEvent.BUTTON2) {
            if (_zoom) _viewTransform.zoom2D(e.getX(), e.getY()); else if (_rotation) _viewTransform.rotate2D(e.getX(), e.getY()); else _viewTransform.translate2D(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    void select(boolean add, Node... nodes) {
        if (!add) {
            _selection.clear();
        }
        for (Node n : nodes) {
            _selection.add(n);
        }
        if (_selector != null) {
            _selector.select(_selection);
        }
    }

    void pick(int x, int y, boolean add) {
        _pickX = x;
        _pickY = y;
        PickCanvas pickCanvas = new PickCanvas(_canvas, this._universe.getLocale());
        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        pickCanvas.setTolerance(2.0f);
        pickCanvas.setShapeLocation(_pickX, _pickY);
        PickResult result = pickCanvas.pickClosest();
        if (result == null) {
            select(add, new Node[0]);
        } else {
            select(add, result.getObject());
        }
    }

    void pickArea(int x, int y, boolean add) {
        PickCanvas pickCanvas = new PickCanvas(_canvas, this._universe.getLocale());
        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        int cx = (_pickX + x) / 2;
        int cy = (_pickY + y) / 2;
        float t = Math.max(Math.abs(_pickX - x), Math.abs(_pickY - y)) / 2.f;
        pickCanvas.setTolerance(t);
        pickCanvas.setShapeLocation(cx, cy);
        if (t > 2.) {
            PickResult[] result = pickCanvas.pickAll();
            if (result == null) {
                select(add, new Node[0]);
            } else {
                Node[] n = new Node[result.length];
                int i = 0;
                for (PickResult r : result) {
                    n[i++] = r.getObject();
                }
                select(add, n);
            }
        } else {
            PickResult result = pickCanvas.pickClosest();
            if (result == null) {
                select(add, new Node[0]);
            } else {
                select(add, result.getObject());
            }
        }
    }
}
