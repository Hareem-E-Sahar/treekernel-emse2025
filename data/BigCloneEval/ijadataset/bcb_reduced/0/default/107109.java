import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import java.awt.geom.*;

/** The zoomTool is used to control and adjust the canvas's zooming size.
 * It records the current canvas's zooming size and adjusts the size by listening for a MouseEvent.
 * The state of zoomTool depends on the current canvas's zooming size.
 *
 * It should work with all operating systems and hardware.
 * There are no variances and no security constraints.
 *
 * @author TerpPaint
 * @version 2.0
 */
public class zoomTool implements ourTool {

    /** The current zooming size of the canvas.	 Default value is 1.
     */
    double theZoom;

    /** The x of a mouse click.
     */
    int x;

    /** The y of a mouse click.
     */
    int y;

    /** Whether the user dragged or not. if true the user has dragged and so done a box zoom
     */
    boolean dragged;

    /** Holds the current, backup, selected and pasted image
     */
    BufferedImage backupImage;

    /** Holds the current, backup, selected and pasted image
	     */
    BufferedImage curImage;

    /**Holds a Graphics2D
	     */
    private Graphics2D g2D;

    /** Holds the stroke that is selected
     */
    private BasicStroke selectStroke;

    /** Creates a zoomTool object and sets the zoom to 1.
     * It takes in no parameters or null arguments.  It does not return anything.
     * There are no algorithms of any kind and no variances and OS dependencies.
     * There should not be any exceptions or security constraints.
     */
    public zoomTool() {
        super();
        _prof.prof.cnt[11004]++;
        _prof.prof.cnt[11005]++;
        {
            _prof.prof.cnt[11006]++;
            theZoom = 1;
        }
        {
            _prof.prof.cnt[11007]++;
            selectStroke = new BasicStroke(1.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.f, new float[] { 6.f, 6.f }, 0.f);
        }
    }

    /** Adjusts the canvas's zooming size by listening for the MouseEvent.
     * If the left mouse button is clicked and the current canvas's zooming size
     * is less than 8.0, the new zooming size will be the twice size of the current
     * size.  If the right mouse button is clicked and the current canvas' zooming
     * size is greater than 1.0, the new zooming size will be the half size of the current size.
     * In addition, the new zooming size will be recorded as the state of zoomTool.
     *
     * @param mevt the mouse event which will determine the zooming size.
     * @param theCanvas the reference of main_canvas whose zooming size will be adjusted.
     */
    public void clickAction(MouseEvent mevt, main_canvas theCanvas) {
        _prof.prof.cnt[11008]++;
        {
            _prof.prof.cnt[11009]++;
            theZoom = theCanvas.getZoom();
        }
        {
            _prof.prof.cnt[11010]++;
            dragged = false;
        }
        {
            _prof.prof.cnt[11011]++;
            x = (int) (mevt.getX());
        }
        {
            _prof.prof.cnt[11012]++;
            y = (int) (mevt.getY());
        }
        {
            _prof.prof.cnt[11013]++;
            System.out.println("click at x " + x + " y " + y);
        }
        {
            _prof.prof.cnt[11014]++;
            backupImage = theCanvas.getBufferedImage();
        }
        {
            _prof.prof.cnt[11015]++;
            curImage = theCanvas.getBufferedImage();
        }
        {
            _prof.prof.cnt[11016]++;
            g2D = curImage.createGraphics();
        }
        {
            _prof.prof.cnt[11017]++;
            theCanvas.repaint();
        }
    }

    /** Allows the user to drag the mouse, but is unnecessary for zooming. It was
     * defined because zoomTool implements OurTool.
     * @param mevt mouse event.
     * @param theCanvas the reference of main_canvas.
     */
    public void dragAction(MouseEvent mevt, main_canvas theCanvas) {
        _prof.prof.cnt[11018]++;
        {
            _prof.prof.cnt[11019]++;
            g2D.drawImage(backupImage, null, 0, 0);
        }
        _prof.prof.cnt[11020]++;
        int endX = mevt.getX();
        _prof.prof.cnt[11021]++;
        int endY = mevt.getY();
        _prof.prof.cnt[11022]++;
        int myX = x;
        _prof.prof.cnt[11023]++;
        int myY = y;
        {
            _prof.prof.cnt[11024]++;
            if (endX < x) {
                {
                    _prof.prof.cnt[11025]++;
                    myX = endX;
                }
            }
        }
        {
            _prof.prof.cnt[11026]++;
            if (endY < y) {
                {
                    _prof.prof.cnt[11027]++;
                    myY = endY;
                }
            }
        }
        _prof.prof.cnt[11028]++;
        int width = Math.abs(x - endX);
        _prof.prof.cnt[11029]++;
        int height = Math.abs(y - endY);
        {
            _prof.prof.cnt[11030]++;
            g2D.setColor(Color.black);
        }
        {
            _prof.prof.cnt[11031]++;
            g2D.setStroke(selectStroke);
        }
        {
            _prof.prof.cnt[11032]++;
            g2D.draw(new Rectangle(x, y, width, height));
        }
        {
            _prof.prof.cnt[11033]++;
            theCanvas.repaint();
        }
    }

    /** Allows the user to release the mouse, but is unnecessary for zooming. It was
     * defined because zoomTool implements OurTool.
     * @param mevt mouse event
     * @param theCanvas the reference of main_canvas
     */
    public void mouseReleaseAction(MouseEvent mevt, main_canvas theCanvas) {
        _prof.prof.cnt[11034]++;
        _prof.prof.cnt[11035]++;
        int endX = (int) (mevt.getX());
        _prof.prof.cnt[11036]++;
        int endY = (int) (mevt.getY());
        {
            _prof.prof.cnt[11037]++;
            if (endX != x && endY != y) {
                _prof.prof.cnt[11038]++;
                dragged = true;
            }
        }
        {
            _prof.prof.cnt[11039]++;
            if (!dragged) {
                _prof.prof.cnt[11040]++;
                double currentZoom = theCanvas.getZoom();
                {
                    _prof.prof.cnt[11041]++;
                    if (SwingUtilities.isLeftMouseButton(mevt)) {
                        {
                            _prof.prof.cnt[11042]++;
                            if (currentZoom < 8.0) {
                                {
                                    _prof.prof.cnt[11043]++;
                                    theCanvas.setZoom(currentZoom * 2.0);
                                }
                                {
                                    _prof.prof.cnt[11044]++;
                                    theCanvas.setOldZoom(currentZoom);
                                }
                            }
                        }
                    } else {
                        {
                            _prof.prof.cnt[11045]++;
                            if (currentZoom > 1.0) {
                                {
                                    _prof.prof.cnt[11046]++;
                                    theCanvas.setZoom(currentZoom * 0.5);
                                }
                                {
                                    _prof.prof.cnt[11047]++;
                                    theCanvas.setOldZoom(currentZoom);
                                }
                            }
                        }
                    }
                }
                {
                    _prof.prof.cnt[11048]++;
                    theZoom = theCanvas.getZoom();
                }
                _prof.prof.cnt[11049]++;
                Point focus = new Point((int) (x * theZoom), (int) (y * theZoom));
                {
                    _prof.prof.cnt[11050]++;
                    theCanvas.pictureScrollPane.getViewport().setExtentSize(new Dimension(theCanvas.getBufferedImage().getWidth(), theCanvas.getBufferedImage().getHeight()));
                }
                {
                    _prof.prof.cnt[11051]++;
                    theCanvas.pictureScrollPane.getViewport().setViewSize(new Dimension((int) theZoom, (int) theZoom));
                }
                {
                    _prof.prof.cnt[11052]++;
                    theCanvas.pictureScrollPane.getViewport().setViewPosition(theCanvas.pictureScrollPane.getViewport().toViewCoordinates(focus));
                }
                {
                    _prof.prof.cnt[11053]++;
                    System.out.println(x + " " + y + " is " + theCanvas.pictureScrollPane.getViewport().toViewCoordinates(focus));
                }
                {
                    _prof.prof.cnt[11054]++;
                    theCanvas.repaint();
                }
            } else {
                {
                    _prof.prof.cnt[11055]++;
                    theZoom = theCanvas.getZoom();
                }
                _prof.prof.cnt[11056]++;
                int centerX = (x + endX) / 2;
                _prof.prof.cnt[11057]++;
                int centerY = (y + endY) / 2;
                _prof.prof.cnt[11058]++;
                double zoomFactorX = (endX - x);
                {
                    _prof.prof.cnt[11059]++;
                    if (zoomFactorX < 0) {
                        _prof.prof.cnt[11060]++;
                        zoomFactorX = (x - endX);
                    }
                }
                _prof.prof.cnt[11061]++;
                double zoomFactorY = (endY - y);
                {
                    _prof.prof.cnt[11062]++;
                    if (zoomFactorY < 0) {
                        _prof.prof.cnt[11063]++;
                        zoomFactorY = (y - endY);
                    }
                }
                _prof.prof.cnt[11064]++;
                double width = theCanvas.pictureScrollPane.getViewport().getExtentSize().getWidth();
                _prof.prof.cnt[11065]++;
                double height = theCanvas.pictureScrollPane.getViewport().getExtentSize().getHeight();
                _prof.prof.cnt[11066]++;
                double zoomFactor = width / zoomFactorX;
                _prof.prof.cnt[11067]++;
                double check = height / zoomFactorY;
                {
                    _prof.prof.cnt[11068]++;
                    if (check < zoomFactor) {
                        _prof.prof.cnt[11069]++;
                        zoomFactor = check;
                    }
                }
                _prof.prof.cnt[11070]++;
                double currentZoom = theCanvas.getZoom();
                {
                    _prof.prof.cnt[11071]++;
                    zoomFactor = (int) zoomFactor;
                }
                {
                    _prof.prof.cnt[11072]++;
                    theCanvas.setZoom(zoomFactor);
                }
                {
                    _prof.prof.cnt[11073]++;
                    System.out.println("zoomFactor to: " + zoomFactor);
                }
                {
                    _prof.prof.cnt[11074]++;
                    theCanvas.setOldZoom(currentZoom);
                }
                {
                    _prof.prof.cnt[11075]++;
                    theZoom = theCanvas.getZoom();
                }
                _prof.prof.cnt[11076]++;
                int myX = x;
                {
                    _prof.prof.cnt[11077]++;
                    if (endX < x) {
                        _prof.prof.cnt[11078]++;
                        myX = endX;
                    }
                }
                _prof.prof.cnt[11079]++;
                int myY = y;
                {
                    _prof.prof.cnt[11080]++;
                    if (endY < y) {
                        _prof.prof.cnt[11081]++;
                        myY = endY;
                    }
                }
                _prof.prof.cnt[11082]++;
                Point focus = new Point((int) (myX * theZoom), (int) (myY * theZoom));
                {
                    _prof.prof.cnt[11083]++;
                    theCanvas.pictureScrollPane.getViewport().setExtentSize(new Dimension(theCanvas.getBufferedImage().getWidth(), theCanvas.getBufferedImage().getHeight()));
                }
                {
                    _prof.prof.cnt[11084]++;
                    theCanvas.pictureScrollPane.getViewport().setViewSize(new Dimension((int) zoomFactorX, (int) zoomFactorY));
                }
                {
                    _prof.prof.cnt[11085]++;
                    theCanvas.pictureScrollPane.getViewport().setViewPosition(theCanvas.pictureScrollPane.getViewport().toViewCoordinates(focus));
                }
                {
                    _prof.prof.cnt[11086]++;
                    System.out.println(theCanvas.pictureScrollPane.getViewport().getViewPosition().getX() + " " + theCanvas.pictureScrollPane.getViewport().getViewPosition().getY());
                }
                {
                    _prof.prof.cnt[11087]++;
                    if (SwingUtilities.isLeftMouseButton(mevt) || theCanvas.zoomFactor >= 1) {
                        {
                            _prof.prof.cnt[11088]++;
                            if (theCanvas.main_image.getWidth() * theCanvas.zoomFactor * theCanvas.main_image.getHeight() * theCanvas.zoomFactor < 10000000) {
                                _prof.prof.cnt[11089]++;
                                theCanvas.repaint();
                            } else {
                                _prof.prof.cnt[11090]++;
                                theCanvas.zoomFactor = theCanvas.oldZoomFactor;
                            }
                        }
                    }
                }
            }
        }
    }

    /** Returns the value of theZoom, the state recorded by zoomTool, as an int.
     * There are no OS dependencies and variances.  No security constraints.
     * @return theZoom
     */
    public int getZoom() {
        _prof.prof.cnt[11091]++;
        {
            _prof.prof.cnt[11092]++;
            return (int) theZoom;
        }
    }
}
