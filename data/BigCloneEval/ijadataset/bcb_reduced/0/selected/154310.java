package jiv;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JPanel;

/**
 * Provides the orientation-independent functionality of displaying 2D
 * slice image data in a viewport, and of allowing the user to
 * interact with it. The orientation-specific functionality is in its
 * 3 direct subclasses: <code>TransverseSlice2DViewport</code>,
 * <code>CoronalSlice2DViewport</code>, and
 * <code>SagittalSlice2DViewport</code>.
 *
 * In this class, all the references to x,y,z are not to the real
 * x,y,z but to "virtual" (private) ones: x is horizontal, y is
 * vertical, z is orthogonal to the slice plane.  The "translation"
 * between our private x,y,z and the true ones in the outside world is
 * done by the abstract methods: <code>positionChanged</code>,
 * <code>_firePositionEvent</code>, <code>_world2voxel</code> and
 * <code>_voxel2world</code> which are implemented differently by the
 * 3 different subclasses.
 *
 * @author Chris Cocosco (crisco@bic.mni.mcgill.ca)
 * @version $Id: Slice2DViewport.java,v 1.7 2003/12/21 17:32:17 crisco Exp $ 
 */
public abstract class Slice2DViewport extends JPanel implements PositionListener, PositionGenerator {

    /** For development/testing only. Should be set to false in
        production code. */
    protected static final boolean DEBUG = false;

    static final boolean ALWAYS_DOUBLE_BUFFER = true;

    static final boolean USE_SEPARATE_SCALE_FILTER = false;

    static final boolean USE_NEW_DRAW_IMAGE = false;

    static final boolean CONSUME_INPUT_EVENTS = true;

    static final boolean DEBUG_INPUT_EVENTS = false;

    /** Mask specifying the "other/secondary" mouse button (i.e. not
     the main/primary one). */
    public static final int OTHER_BUTTON_MASK = MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK;

    /** Mask specifying the mouse button "modifier" key(s). */
    public static final int BUTTON_MODIFIER_MASK = MouseEvent.SHIFT_MASK | MouseEvent.CTRL_MASK;

    static int MAX_SCALE_FACTOR;

    static {
        try {
            String os = System.getProperty("os.name").trim().toLowerCase();
            if (os.startsWith("windows 95") || os.startsWith("windows 98")) {
                MAX_SCALE_FACTOR = 10;
                System.out.println("Windows 95/98 OS detected: " + "max zoom factor limited to 10.");
            } else MAX_SCALE_FACTOR = 25;
        } catch (Exception e) {
            MAX_SCALE_FACTOR = 10;
            System.out.println("unknown OS: max zoom factor limited to 10.");
        }
        if (DEBUG) System.out.println("MAX_SCALE_FACTOR: " + MAX_SCALE_FACTOR);
    }

    ImageProducer image_source;

    int max_slice_number;

    float ortho_step;

    Image original_image;

    int original_image_width;

    int original_image_height;

    Vector event_listeners;

    Dimension preferred_size;

    Dimension vport_dims;

    Point image_origin = new Point(0, 0);

    int scaled_image_width;

    int scaled_image_height;

    int min_scaled_image_width;

    int max_scaled_image_width;

    double scale_factor;

    Image scaled_image;

    Image offscreen_buffer = null;

    Graphics offscreen_gc;

    /** current cursor in world coordinates (3D) */
    protected Point3Dfloat cursor;

    ViewportCursor vport_cursor = new ViewportCursor(-100, -100);

    Point last_position = new Point();

    Point3Dfloat distance_origin = null;

    ViewportDistanceDisplay distance_display = null;

    /**
     * Class constructor.
     *
     * @param ip <code>ImageProducer</code> that will be used for
     * feeding <code>original_image</code> (i.e. the 2D image to be
     * displayed).
     * @param pos_listener_for_ip <code>PositionListener</code> that
     * will be used for requesting another slice (2D image) from
     * <code>ip</code>.  
     * @param cursor object that will be used to store the current
     * cursor position (in world coordinates); should be initialized
     * to the desired starting position.
     */
    protected Slice2DViewport(ImageProducer ip, PositionListener pos_listener_for_ip, Point3Dfloat cursor) {
        this.cursor = cursor;
        addPositionListener(pos_listener_for_ip);
        _firePositionEvent(PositionEvent.X | PositionEvent.Y | PositionEvent.Z);
        original_image = createImage(image_source = ip);
        max_slice_number = pos_listener_for_ip.getMaxSliceNumber();
        ortho_step = pos_listener_for_ip.getOrthoStep();
        original_image_width = original_image.getWidth(this);
        original_image_height = original_image.getHeight(this);
        max_scaled_image_width = original_image_width * MAX_SCALE_FACTOR;
        min_scaled_image_width = 1 * ((original_image_width < original_image_height) ? (1) : (int) Math.ceil(original_image_width / (double) original_image_height));
        if (DEBUG) System.out.println(this + " min_scaled_image_width:" + min_scaled_image_width);
        preferred_size = new Dimension(original_image_width, original_image_height);
        vport_dims = new Dimension(preferred_size);
        scaled_image_width = _cappedScaledImageWidth(vport_dims.width);
        scaled_image_height = scaled_image_width * original_image_height / original_image_width;
        scale_factor = ((double) scaled_image_width) / ((double) original_image_width);
        _updateVportCursorPosition();
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    /** 
     * Helper method: validates a suggested value for
     * <code>scaled_image_width</code>; if the value is too low or too
     * high, it "caps" it accordingly.  
     *
     * @param value The suggested value.
     * @return valid (possibly "capped") value.
     */
    final int _cappedScaledImageWidth(int value) {
        if (value < min_scaled_image_width) return min_scaled_image_width;
        if (value > max_scaled_image_width) return max_scaled_image_width;
        return value;
    }

    /** 
     * Note: this needs to return 'true' if you want requestFocus() to
     * have any effect (this was not enforced in Java 1.1...)
     *
     * @see java.awt.Component#isFocusTraversable 
     */
    public final boolean isFocusTraversable() {
        return true;
    }

    /** 
     * Overrides <code>Component#getPreferredSize</code>. Necessary,
     * otherwise <code>Window#pack()</code> will squish us to a tiny
     * size.  
     *
     * @see java.awt.Component#getPreferredSize
     */
    public final Dimension getPreferredSize() {
        return preferred_size;
    }

    /** 
     * @return the vertical dimension of the original (source) image.
     */
    public final int getOriginalImageHeight() {
        return original_image_height;
    }

    /**
     * Called by the AWT input event delivery thread.
     * 
     * @param e The (user-produced) mouse event to process.
     *
     * @see #processMouseMotionEvent
     */
    protected final void processMouseEvent(MouseEvent e) {
        if (DEBUG || DEBUG_INPUT_EVENTS) System.out.println("processMouseEvent: " + e);
        switch(e.getID()) {
            case MouseEvent.MOUSE_ENTERED:
                if (DEBUG) {
                    System.out.println("requesting focus...");
                }
                requestFocus();
                break;
            case MouseEvent.MOUSE_PRESSED:
                if (0 != (e.getModifiers() & (OTHER_BUTTON_MASK | BUTTON_MODIFIER_MASK))) {
                    last_position.x = e.getX();
                    last_position.y = e.getY();
                    if (e.getClickCount() > 1) _clearDistanceMeasurement();
                } else {
                    _newCursor(e.getX(), e.getY());
                    if (e.getClickCount() > 1) _startNewDistanceMeasurement();
                    if (CONSUME_INPUT_EVENTS) e.consume();
                }
                break;
        }
        super.processMouseEvent(e);
    }

    /**
     * Called by the AWT input event delivery thread.
     * 
     * @param e The (user-produced) mouse motion event to process.
     *
     * @see #processMouseEvent
     */
    protected final void processMouseMotionEvent(MouseEvent e) {
        if (MouseEvent.MOUSE_DRAGGED == e.getID()) {
            if (DEBUG || DEBUG_INPUT_EVENTS) System.out.println("processMouseMotionEvent: " + e);
            if (0 != (e.getModifiers() & OTHER_BUTTON_MASK)) {
                final int delta = last_position.y - e.getY();
                last_position.y = e.getY();
                if (0 != (e.getModifiers() & BUTTON_MODIFIER_MASK)) {
                    _doZoom(delta);
                } else {
                    final float multiplication = 0.5f;
                    _newSlice(delta * multiplication * ortho_step);
                }
            } else if (0 != (e.getModifiers() & BUTTON_MODIFIER_MASK)) {
                final int crt_position_x = e.getX();
                final int crt_position_y = e.getY();
                final Point last_position = this.last_position;
                image_origin.translate(crt_position_x - last_position.x, crt_position_y - last_position.y);
                _updateVportCursorPosition();
                last_position.x = crt_position_x;
                last_position.y = crt_position_y;
                repaint();
            } else {
                _newCursor(e.getX(), e.getY());
            }
            if (CONSUME_INPUT_EVENTS) e.consume();
        }
        super.processMouseMotionEvent(e);
    }

    /**
     * Called by the AWT input event delivery thread.
     * 
     * @param e The (user-produced) key event to process.
     */
    protected final void processKeyEvent(KeyEvent e) {
        if (DEBUG || DEBUG_INPUT_EVENTS) System.out.println("processKeyEvent: " + e);
        if (KeyEvent.KEY_PRESSED == e.getID()) {
            switch(e.getKeyChar()) {
                case '+':
                    _newSlice(ortho_step);
                    break;
                case '-':
                    _newSlice(-ortho_step);
                    break;
                case 'd':
                case 'D':
                    _startNewDistanceMeasurement();
                    break;
                case 'c':
                case 'C':
                    _clearDistanceMeasurement();
                    break;
                default:
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_UP:
                            _newSlice(ortho_step);
                            break;
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_DOWN:
                            _newSlice(-ortho_step);
                            break;
                    }
            }
            if (CONSUME_INPUT_EVENTS) e.consume();
        }
        super.processKeyEvent(e);
    }

    /**
     * Changes the image magnification (scale) factor, by recomputing
     * the image position and dimension in the viewport such that the
     * field of view's center, in the original image space, remains at
     * the same viewport position. It respects the min/max scale
     * factors given by 'min_scaled_image_width' and
     * 'max_scaled_image_width'.
     *
     * @param delta The number of pixels the mouse was dragged in the
     * vertical direction; can be positive or negative.  
     */
    final void _doZoom(final int delta) {
        final int MULTIPLICATION = 3;
        if (DEBUG) System.out.println("Zoom: " + delta);
        int scaled_image_width = this.scaled_image_width;
        final int old_fov_left = Math.max(0, image_origin.x);
        final int old_fov_right = Math.min(vport_dims.width, image_origin.x + scaled_image_width);
        final int old_fov_top = Math.max(0, image_origin.y);
        final int old_fov_bottom = Math.min(vport_dims.height, image_origin.y + scaled_image_height);
        final int center_old_fov_x = old_fov_left + (old_fov_right - old_fov_left) / 2;
        final int center_old_fov_y = old_fov_top + (old_fov_bottom - old_fov_top) / 2;
        final double old_scale_factor = scale_factor;
        final Point old_image_origin = image_origin;
        final double center_fov_x = ((center_old_fov_x - old_image_origin.x) / old_scale_factor);
        final double center_fov_y = (center_old_fov_y - old_image_origin.y) / old_scale_factor;
        scaled_image_width = _cappedScaledImageWidth(scaled_image_width + delta * MULTIPLICATION);
        scaled_image_height = scaled_image_width * original_image_height / original_image_width;
        scale_factor = ((double) scaled_image_width) / ((double) original_image_width);
        final double new_scale_factor = scale_factor;
        image_origin.x = Math.round((float) (center_old_fov_x - center_fov_x * new_scale_factor));
        image_origin.y = Math.round((float) (center_old_fov_y - center_fov_y * new_scale_factor));
        _updateVportCursorPosition();
        this.scaled_image_width = scaled_image_width;
        if (DEBUG) System.out.println("image_origin:" + image_origin + " scaled_image_width:" + scaled_image_width + " scaled_image_height:" + scaled_image_height);
        repaint();
    }

    Point3Dfloat __newCursor_new_world_cursor = new Point3Dfloat();

    /**
     * Gets called in response to this class' own mouse events (so it
     * <i>does</i> perform a range check on the new position); if the
     * new cursor position is within volume's boundaries, it updates
     * the current cursor position and notifies other modules of the
     * change.
     *
     * NOTE: Currently, the cursor is in "grid mode": it automatically
     * "snaps" to voxel centers (i.e. it cannot have arbitrary
     * positions).  This also means that, with the current
     * implementation (19/3/2000) the world position coordinates
     * generated by this class will always be integral values...
     *
     * @param vport_point_x The new viewport X coordinate of the cursor.
     * @param vport_point_y The new viewport Y coordinate of the cursor.
     *
     * @see #_newCursor( float, float, boolean) 
     */
    final synchronized void _newCursor(final int vport_point_x, final int vport_point_y) {
        final int new_cursor_vox_x = (int) ((vport_point_x - image_origin.x) / scale_factor);
        final int new_cursor_vox_y = (int) (original_image_height - (vport_point_y - image_origin.y) / scale_factor);
        if (DEBUG) {
            System.out.println("new_cursor_vox_x:" + new_cursor_vox_x + ", " + "new_cursor_vox_y:" + new_cursor_vox_y);
        }
        if (!(new_cursor_vox_x >= 0 && new_cursor_vox_x < original_image_width && new_cursor_vox_y >= 0 && new_cursor_vox_y < original_image_height)) return;
        _voxel2world(__newCursor_new_world_cursor, new_cursor_vox_x, new_cursor_vox_y, 0f);
        _newCursor(__newCursor_new_world_cursor.x, __newCursor_new_world_cursor.y, true);
    }

    Point __newCursor_old_vport_cursor = new Point();

    Point __newCursor_new_vport_cursor = new Point();

    Rectangle2 __newCursor_bounds1 = new Rectangle2();

    Rectangle2 __newCursor_bounds2 = new Rectangle2();

    /**
     * Gets called by <code>_newCursor( int, int)</code> or by
     * <code>positionChanged</code>. Updates the graphical cursor
     * representation and the distance measurement graphics (if
     * distance mode is active). Optionally, notifies other
     * modules (which registered their interest by means of
     * <code>addPositionListener</code>) of the cursor position
     * change.
     *
     * @param new_world_x The new world "X" coordinate of the
     * cursor. Not checked if it's withins image volume's boundaries!
     * @param new_world_x The new world "Y" coordinate of the
     * cursor. Not checked if it's withins image volume's boundaries!
     * @param notify_others Indicates if it should notify other modules.
     */
    protected final synchronized void _newCursor(final float new_world_x, final float new_world_y, final boolean notify_others) {
        Rectangle2 old_bounds;
        vport_cursor.getPosition(__newCursor_old_vport_cursor);
        vport_cursor.getBounds(old_bounds = __newCursor_bounds1);
        cursor.x = new_world_x;
        cursor.y = new_world_y;
        if (null != distance_origin) {
            Rectangle distance_bounds = __newCursor_bounds2;
            distance_display.getBounds(distance_bounds);
            old_bounds.expandToInclude(distance_bounds);
            distance_display.setLabel(_distanceInSlice(distance_origin, cursor));
        }
        _updateVportCursorPosition();
        vport_cursor.getPosition(__newCursor_new_vport_cursor);
        if (null != distance_origin || !__newCursor_old_vport_cursor.equals(__newCursor_new_vport_cursor)) {
            Rectangle2 new_bounds;
            vport_cursor.getBounds(new_bounds = __newCursor_bounds2);
            new_bounds.expandToInclude(old_bounds);
            if (null != distance_origin) {
                Rectangle distance_bounds = __newCursor_bounds1;
                distance_display.getBounds(distance_bounds);
                new_bounds.expandToInclude(distance_bounds);
            }
            repaint(new_bounds.x, new_bounds.y, new_bounds.width, new_bounds.height);
        }
        if (notify_others) _firePositionEvent(PositionEvent.X | PositionEvent.Y);
    }

    Point3Dint __newSlice_new_voxel = new Point3Dint();

    /**
     * Changes the "Z" cursor position, provided the new position is
     * not outside volume's boundaries.  This is done by sending a new
     * position event to the other modules (which registered their
     * interest by means of <code>addPositionListener</code>); one of
     * these PositionListener-s is the slice image producer for this
     * viewport, hence the displayed image gets changed if we cross
     * into a different slice.
     *
     * NOTE: Currently, this method _does_not_ operates in
     * "grid-mode", unlike <code>_newCursor()</code> !  This is
     * required in order for sub-unit multiplication factors to work
     * -- otherwise, we'll be at the same voxel even after 100 "steps"
     * (mouse events) of 0.5 each ...
     *
     * @param increment Change in the "Z" world coordinate (cursor
     * movement in a direction orthogonal to this viewport). Can be
     * positive or negative.  
     */
    final synchronized void _newSlice(final float increment) {
        final float new_cursor_z = cursor.z + increment;
        _world2voxel(__newSlice_new_voxel, 0, 0, new_cursor_z);
        if (__newSlice_new_voxel.z >= 0 && __newSlice_new_voxel.z <= max_slice_number) {
            cursor.z = new_cursor_z;
            _firePositionEvent(PositionEvent.Z);
        }
    }

    Rectangle __DistanceMeasurement_old_bounds = new Rectangle();

    Rectangle2 __DistanceMeasurement_new_bounds = new Rectangle2();

    /**
     * Marks the current cursor position as the origin (first point)
     * for the in-slice distance measurement. Also, it enables the
     * distance measurement mode, if not already on. 
     */
    final synchronized void _startNewDistanceMeasurement() {
        Rectangle old_bounds = null;
        if (null == distance_display) distance_display = new ViewportDistanceDisplay(getFontMetrics(getFont())); else if (distance_origin != null) distance_display.getBounds(old_bounds = __DistanceMeasurement_old_bounds);
        distance_origin = new Point3Dfloat(cursor);
        distance_display.setLabel(0f);
        _updateVportCursorPosition();
        Rectangle2 new_bounds;
        distance_display.getBounds(new_bounds = __DistanceMeasurement_new_bounds);
        if (old_bounds != null) new_bounds.expandToInclude(old_bounds);
        repaint(new_bounds.x, new_bounds.y, new_bounds.width, new_bounds.height);
    }

    /**
     * Disables the distance measurement mode.
     */
    final synchronized void _clearDistanceMeasurement() {
        if (distance_origin == null) return;
        Rectangle bounds;
        distance_display.getBounds(bounds = __DistanceMeasurement_old_bounds);
        distance_origin = null;
        repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    Point __updateVportCursorPosition_vport_cursor = new Point(-1, -1);

    /**
     * Updates the viewport positions of <code>vport_cursor</code> and
     * <code>distance_display</code>. They are a function of the
     * following instance fields: 'image_origin', 'scale_factor', and
     * 'cursor' hence this method should be called <i>everytime</i> any
     * of them changes!!
     *
     * @see #_world2viewport 
     */
    final void _updateVportCursorPosition() {
        _world2viewport(__updateVportCursorPosition_vport_cursor, cursor.x, cursor.y);
        vport_cursor.setPosition(__updateVportCursorPosition_vport_cursor);
        if (null != distance_origin) {
            distance_display.setEndPosition(__updateVportCursorPosition_vport_cursor);
            _world2viewport(__updateVportCursorPosition_vport_cursor, distance_origin.x, distance_origin.y);
            distance_display.setStartPosition(__updateVportCursorPosition_vport_cursor);
        }
    }

    Point3Dint __world2viewport_voxel = new Point3Dint();

    /** 
     * Converts a world (X,Y) in-slice position to viewport (X,Y)
     * coordinates, rounded to the nearest voxel center. The world
     * coordinates are assumed to be within volume's boundaries!
     *
     * @param vport_point Output: reference to Point object where to
     * store the result.
     * @param world_x Input: world "X" coordinate.
     * @param world_y Input: world "Y" coordinate.  
     */
    final void _world2viewport(Point vport_point, final float world_x, final float world_y) {
        _world2voxel(__world2viewport_voxel, world_x, world_y, 0);
        int offset = (scaled_image_width / original_image_width) >> 1;
        if (DEBUG) System.out.println("offset: " + offset);
        vport_point.x = offset + image_origin.x + (int) (__world2viewport_voxel.x * scale_factor);
        vport_point.y = -offset + image_origin.y + (int) ((original_image_height - __world2viewport_voxel.y) * scale_factor);
    }

    int __paint_old_scaled_image_width = -1;

    /**
     * Called by AWT when screen (re)drawing is required; it should be
     * able to redraw everything (e.g. for situations when the window
     * got partially covered or corrupted).
     *
     * @param gr The graphics context to draw on.  
     */
    public final void paint(Graphics gr) {
        if (DEBUG) {
            System.out.println("*** paint( " + gr + " )");
            System.out.println("    getClipBounds: " + gr.getClipBounds());
            System.out.println("    image_origin: " + image_origin + ", scaled_image_width: " + scaled_image_width);
        }
        final Point image_origin = this.image_origin;
        final int scaled_image_width = this.scaled_image_width;
        if (USE_SEPARATE_SCALE_FILTER) {
            if (null == scaled_image || scaled_image_width != __paint_old_scaled_image_width) {
                scaled_image = original_image.getScaledInstance(scaled_image_width, -1, Image.SCALE_REPLICATE);
                __paint_old_scaled_image_width = scaled_image_width;
            }
        }
        if (USE_SEPARATE_SCALE_FILTER) {
            if (USE_NEW_DRAW_IMAGE) {
                final int scaled_image_height = this.scaled_image_height;
                gr.drawImage(scaled_image, image_origin.x, image_origin.y, image_origin.x + scaled_image_width, image_origin.y + scaled_image_height, 0, 0, scaled_image_width, scaled_image_height, null);
            } else {
                gr.drawImage(scaled_image, image_origin.x, image_origin.y, null);
            }
        } else {
            if (USE_NEW_DRAW_IMAGE) gr.drawImage(original_image, image_origin.x, image_origin.y, image_origin.x + scaled_image_width, image_origin.y + scaled_image_height, 0, 0, original_image_width, original_image_height, null); else gr.drawImage(original_image, image_origin.x, image_origin.y, scaled_image_width, scaled_image_height, null);
        }
        vport_cursor.draw(gr);
        if (null != distance_origin) distance_display.draw(gr);
        if (false) super.paint(gr);
    }

    final void _doubleBufferedPaint(Graphics gr) {
        paint(offscreen_gc);
        if (USE_NEW_DRAW_IMAGE) gr.drawImage(offscreen_buffer, 0, 0, vport_dims.width, vport_dims.height, 0, 0, vport_dims.width, vport_dims.height, null); else gr.drawImage(offscreen_buffer, 0, 0, null);
    }

    /**
     * Called by AWT when screen redrawing/updating is required (and
     * in response to <code>repaint()</code> requests by the
     * application). It can safely assume that whatever it draw before
     * it's still there (i.e. didn't somehow get erased).
     *
     * @param gr The screen graphics context to draw on.  
     */
    public final void update(Graphics gr) {
        final Dimension vport_dims = this.vport_dims;
        final Point image_origin = this.image_origin;
        final Rectangle clip = gr.getClipBounds();
        if (DEBUG) {
            System.out.println(this + "*** update( " + gr + " )");
            System.out.println("    getClipBounds: " + clip);
            System.out.println("    vport_dims: " + vport_dims);
        }
        boolean offscreen_buffer_cleared = false;
        if (null == offscreen_buffer) {
            offscreen_buffer = createImage(vport_dims.width, vport_dims.height);
            offscreen_gc = offscreen_buffer.getGraphics();
            offscreen_buffer_cleared = true;
        }
        if (clip == null || (clip.x == 0 && clip.y == 0 && clip.width == vport_dims.width && clip.height == vport_dims.height) || clip.x < image_origin.x || (clip.x + clip.width) > (image_origin.x + scaled_image_width) || clip.y < image_origin.y || (clip.y + clip.height) > (image_origin.y + scaled_image_height)) {
            if (clip != null) offscreen_gc.setClip(clip); else offscreen_gc.setClip(0, 0, vport_dims.width, vport_dims.height);
            if (!offscreen_buffer_cleared) {
                if (true) {
                    offscreen_gc.clearRect(0, 0, vport_dims.width, vport_dims.height);
                } else {
                    offscreen_gc.setColor(getBackground());
                    offscreen_gc.fillRect(0, 0, vport_dims.width, vport_dims.height);
                }
            }
            _doubleBufferedPaint(gr);
            return;
        }
        if (ALWAYS_DOUBLE_BUFFER) {
            if (clip != null) offscreen_gc.setClip(clip); else offscreen_gc.setClip(0, 0, vport_dims.width, vport_dims.height);
            _doubleBufferedPaint(gr);
        } else {
            paint(gr);
        }
    }

    /**
     * Called by AWT (e.g. when the viewport/window size changed).
     * Recomputes the image position and dimension (scale factor) in
     * the viewport such that the old field of view (FOV), in the
     * original image space, is preserved and centered.  
     */
    public final synchronized void doLayout() {
        final Dimension new_vport_dims = getSize();
        if (new_vport_dims.height <= 0 || new_vport_dims.width <= 0) return;
        if (DEBUG) {
            System.out.println(this + " vport_dims: " + vport_dims);
            System.out.println("\timage_origin: " + image_origin);
            System.out.println("\tscaled_image_width: " + scaled_image_width);
        }
        if (offscreen_buffer != null) {
            offscreen_gc.dispose();
            offscreen_gc = null;
            offscreen_buffer = null;
        }
        final double old_scale_factor = scale_factor;
        final int old_fov_left = Math.max(0, image_origin.x);
        final int old_fov_right = Math.min(vport_dims.width, image_origin.x + scaled_image_width);
        final int old_fov_top = Math.max(0, image_origin.y);
        final int old_fov_bottom = Math.min(vport_dims.height, image_origin.y + scaled_image_height);
        final int vport_center_old_fov_x = old_fov_left + (old_fov_right - old_fov_left) / 2;
        final int vport_center_old_fov_y = old_fov_top + (old_fov_bottom - old_fov_top) / 2;
        final int old_fov_width = Math.max(1, old_fov_right - old_fov_left);
        final int old_fov_height = Math.max(1, old_fov_bottom - old_fov_top);
        final float old_fov_aspect = ((float) old_fov_width) / old_fov_height;
        final float new_vport_aspect = ((float) new_vport_dims.width) / new_vport_dims.height;
        final double new_scale_factor = old_scale_factor * ((new_vport_aspect > old_fov_aspect) ? (((float) new_vport_dims.height) / old_fov_height) : (((float) new_vport_dims.width) / old_fov_width));
        final Point old_image_origin = image_origin;
        vport_dims = new_vport_dims;
        scaled_image_width = _cappedScaledImageWidth((int) (new_scale_factor * original_image_width));
        scaled_image_height = scaled_image_width * original_image_height / original_image_width;
        scale_factor = ((double) scaled_image_width) / ((double) original_image_width);
        final double center_old_fov_x = (vport_center_old_fov_x - old_image_origin.x) / old_scale_factor;
        final double center_old_fov_y = (vport_center_old_fov_y - old_image_origin.y) / old_scale_factor;
        image_origin.x = Math.round((float) (vport_dims.width / 2.0 - center_old_fov_x * scale_factor));
        image_origin.y = Math.round((float) (vport_dims.height / 2.0 - center_old_fov_y * scale_factor));
        _updateVportCursorPosition();
        if (DEBUG) {
            System.out.println("New values: " + " vport_dims: " + vport_dims);
            System.out.println("\timage_origin: " + image_origin);
            System.out.println("\tscaled_image_width: " + scaled_image_width);
            System.out.println("\tscaled_image_height: " + scaled_image_height);
        }
        repaint();
    }

    /**
     * "Callback" used, by an outside event source, to deliver a
     * <code>PositionEvent</code>. Required by the PositionListener
     * interface.
     *
     * @param e The new (imposed from the outside) world cursor position.
     *
     * @see PositionListener 
     */
    public abstract void positionChanged(PositionEvent e);

    /**
     * Required by the PositionListener interface.
     *
     * @return -1 (that is, a clearly invalid value) to indicate that
     * this class is not an <code>ImageProducer</code>.
     * 
     * @see PositionListener 
     */
    public final int getMaxSliceNumber() {
        return -1;
    }

    /**
     * Required by the PositionListener interface.
     *
     * @return Float.NaN (that is, a clearly invalid value) since
     * this class is not an <code>ImageProducer</code>.
     * 
     * @see PositionListener 
     */
    public final float getOrthoStep() {
        return Float.NaN;
    }

    /**
     * Registers another module who is interested in being notified of
     * cursor position changes originating from this viewport.
     *
     * @param pl Module interested in receiving position events. If
     * the argument is 'null', or if it is already present in the list
     * of event listeners, this method does nothing.  
     */
    public final synchronized void addPositionListener(PositionListener pl) {
        if (null == event_listeners) event_listeners = new Vector();
        if (null == pl || event_listeners.contains(pl)) return;
        event_listeners.addElement(pl);
    }

    /**
     * Undoes what <code>addPositionListener</code> did.
     *
     * @param pl Module (position event listener) to remove from the
     * list of modules to notify.
     * 
     * @see #addPositionListener 
     */
    public final synchronized void removePositionListener(PositionListener pl) {
        if (null != event_listeners && null != pl) event_listeners.removeElement(pl);
    }

    /**
     * Notifies the other (registered) modules about a cursor position
     * change.
     *
     * @param changed_coords_mask Indicates which of (x,y,z) changed
     * (see <code>PositionEvent</code> for legal values).  
     * 
     * @see #__aid_to_firePositionEvent 
     */
    protected abstract void _firePositionEvent(int changed_coords_mask);

    /**
     * Helper method: The orientation-independent functionality of
     * <code>_firePositionEvent</code> (who calls this).
     *
     * @param e The <code>PositionEvent</code> to send out.  
     *
     * @see #_firePositionEvent 
     */
    protected final void __aid_to_firePositionEvent(final PositionEvent e) {
        if (DEBUG) System.out.println(e);
        if (null == event_listeners) return;
        for (int i = 0; i < event_listeners.size(); ++i) ((PositionListener) event_listeners.elementAt(i)).positionChanged(e);
    }

    final float _distanceInSlice(final Point3Dfloat world_a, final Point3Dfloat world_b) {
        final float delta_x = world_a.x - world_b.x;
        final float delta_y = world_a.y - world_b.y;
        return (float) Math.sqrt(delta_x * delta_x + delta_y * delta_y);
    }

    /**
     * Converts from voxel to world coordinates, transparently
     * handling the reordering/reshuffling of the "virtual" (versus
     * "real") coordinates.
     *
     * Note: the original reason for using the voxel2world version
     * that reads voxel coords as floats was to be able to handle
     * arbitrary positions in voxel space. However, currently only the
     * "grid-mode" is supported, so using this voxel2world is kind of
     * a waste...
     *
     * @param world Output: reference to Point3Dfloat object where to
     * store the result ("virtual" world coordinates).
     * @param vx Input: voxel "virtual X" coordinate.
     * @param vy Input: voxel "virtual Y" coordinate.  
     * @param vz Input: voxel "virtual Z" coordinate.  
     */
    protected abstract void _voxel2world(Point3Dfloat world, float vx, float vy, float vz);

    /**
     * Converts from world to voxel coordinates, transparently
     * handling the reordering/reshuffling of the "virtual" (versus
     * "real") coordinates.
     *
     * Note: for supporting arbitrary cursor positions, you'll need an
     * overridden version of this method that returns (outputs) a
     * Point3Dfloat!
     * 
     * @param voxel Output: reference to Point3Dint object where to
     * store the result ("virtual" voxel coordinates).
     * @param wx Input: world "virtual X" coordinate.
     * @param wy Input: world "virtual Y" coordinate.  
     * @param wz Input: world "virtual Z" coordinate.  
     */
    protected abstract void _world2voxel(Point3Dint voxel, float wx, float wy, float wz);
}
