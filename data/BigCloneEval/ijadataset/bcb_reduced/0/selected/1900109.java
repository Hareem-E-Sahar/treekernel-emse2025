package game.engine;

import game.assets.AssetManager;
import game.geometry.Box;
import game.geometry.Circle;
import game.geometry.Shape;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * GameLayer provides the core superclass of all layers within the game.
 * <P>
 * A GameLayer is the primary object which is manipulated within a GameEngine
 * instance. In other words, from the perspective of the GameEngine, each game
 * is comprised of a number of different game layers. Examples of game layers
 * can include main menus, help screens, game spaces, screen overlays,
 * backgrounds, etc. whatever makes sense for the game being developed.
 * <P>
 * Each game layer can be visible (i.e. it is to be drawn as part of the
 * update/render loop) or invisible (it is not drawn). In addition, each game
 * layer can also be active (i.e. to be updated as part of the update/render
 * loop) or inactive (i.e. not updated). This means that certain game layers,
 * e.g. a help screen, need only be made active and visible when appropriate,
 * and made inactive and invisible when not needed, etc.
 * <P>
 * Fundamentally, each GameLayer instance is comprised of a number of
 * GameObjects (representing entities within the game, i.e. anything from a
 * player controlled sprite, to a score object, to a piece of text, etc.).
 * <P>
 * GameObjects can be organised into a number of GameObjectSets (i.e. grouping
 * together a number of related game objects, e.g. sprites of a certain type,
 * etc.). Apart from this, a general HashMap of all game objects is also
 * available to permit individual retrieval of game objects.
 * <P>
 * The GameLayer will structure any graphical game objects based on an ordered
 * z-layer approach (i.e. those with the lowest z order (>=0) will be drawn
 * first, followed by those of successively higher z orders. An ordered list of
 * z-order draw position is maintained by the GameLayer.
 * <P>
 * In addition to maintaining an order draw list, the GameLayer also maintains
 * an ordered list based on the either the contained GameObjects x or y
 * locations within the GameLayer. This provides a means of more quickly
 * determining game object overlap, etc.
 * <P>
 * The GameLayer also supports a controllable viewport projecting from the game
 * layer onto a region of the screen.
 * <P>
 * Note: Due to issues of synchronization when adding/removing game objects from
 * a collection that is currently being iterated over, each GameLayer provides
 * method for queuing the addition of new game objects and the removal of
 * current game objects. As such, any game update loop is strongly advised to
 * add/remove game objects using the available add/remove queues.
 * 
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 * 
 * @see GameEngine
 * @see GameObject
 */
public class GameLayer implements Observer {

    /**
	 * Name of this game layer.
	 */
    public String gameLayerName;

    /**
	 * Enumerated type which specifies if this layer is ACTIVE, i.e. to be
	 * updated as part of the update/render loop, or INACTIVE not to be updated.
	 */
    public enum GameLayerActivity {

        ACTIVE, INACTIVE
    }

    /**
	 * Current activity state of this layer
	 */
    protected GameLayerActivity gameLayerActivity;

    /**
	 * Enumerated type which specifies if this layer is VISIBLE, i.e. to be
	 * drawn as part of the update/render loop, or INVISIBLE not to be drawn.
	 */
    public enum GameLayerVisibility {

        VISIBLE, INVISIBLE
    }

    /**
	 * Current visibility state of this layer
	 */
    protected GameLayerVisibility gameLayerVisibility;

    /**
	 * Variables that are used to determine the initial size given to the
	 * various game object and graphical game object stores.
	 */
    private static final int GAMEOBJECT_STORESIZE = 1000;

    private static final int GAMEOBJECTGRAPHICAL_STORESIZE = 1000;

    /**
	 * Variables that are used to determine the initial size given to the
	 * various add/remove game object stores.
	 */
    private static final int GAMEOBJECT_ADD_QUEUE_STORESZIE = 50;

    private static final int GAMEOBJECT_REMOVE_QUEUE_STORESIZE = 50;

    /**
	 * Hash map of all game objects contained within this game layer
	 */
    public HashMap<String, GameObject> gameObjects;

    /**
	 * Hash map of all game object sets contained within this game layer
	 */
    public HashMap<String, GameObjectCollection> gameObjectCollections;

    /**
	 * Lists used to hold game objects to be added to this game layer
	 */
    private ArrayList<GameObject> gameObjectsToAdd;

    private ArrayList<String> gameObjectsToAddSetIdentifier;

    /**
	 * Lists used to hold game objects to be removed from this game layer
	 */
    private ArrayList<GameObject> gameObjectsToRemove;

    /**
	 * Array of game objects sorted in terms of their defined draw order. A game
	 * object collection class is used to hold the sorted game objects as
	 * GameObjectCollection is derived from the ArrayList class but supports
	 * direct access to the elements stored withint the array (i.e. it is
	 * faster)
	 */
    protected GameObjectCollection gameObjectsDrawOrderSorted;

    /**
	 * z-order of this object (relative to other layers)
	 */
    protected int drawOrder;

    /**
	 * Flag determining if game object graphical debug information is to be
	 * drawn
	 */
    protected boolean drawGameObjectDebugInformation = false;

    /**
	 * Width of this game layer.
	 * <P>
	 * Note: The width of a game layer may extend beyond the bounds of the
	 * screen dimension and is used to define the width of the corresponding
	 * game area.
	 */
    public double width;

    /**
	 * Height of this game layer.
	 * <P>
	 * Note: The height of a game layer may extend beyond the bounds of the
	 * screen dimension and is used to define the height of the corresponding
	 * game area.
	 */
    public double height;

    /**
	 * Viewport width as displayed on the screen. The corresponding viewport
	 * layer width is viewPortWidth * drawScaleFactor
	 */
    public double viewPortWidth;

    /**
	 * Viewport height as displayed on the screen. The corresponding viewport
	 * layer height is viewPortHeight * drawScaleFactor
	 */
    public double viewPortHeight;

    /**
	 * x screen location at which the center of this viewport projects onto
	 */
    public double viewPortScreenX;

    /**
	 * y screen location at which the center of this viewport projects onto
	 */
    public double viewPortScreenY;

    /**
	 * x layer location at which the center of this viewport projects onto
	 */
    public double viewPortLayerX;

    /**
	 * y layer location at which the center of this viewport projects onto
	 */
    public double viewPortLayerY;

    /**
	 * Current draw scale factor for this layer. This value determines how much
	 * of the layer is to be rendered within the on-screen viewport. A value of
	 * 1.0 signifies that 1 screen pixel corresponds to a distance of 1 in the
	 * layer. A value of 0.5 signifies that each on-screen pixel will correspond
	 * to a layer distance of 2, etc.
	 */
    public double drawScaleFactor = 1.0;

    /**
	 * Values used by the moveAndScaleViewportOnMouseInteraction method to
	 * determine how much the viewport scale is changed depending upon the mouse
	 * wheel rotation or the viewport to be scrolled depending upon mouse
	 * movement.
	 */
    protected double VIEWPORT_MOUSE_SCALE_SPEED = 1.1;

    protected double VIEWPORT_MOUSE_SCROLL_SPEED = 5.0;

    /**
	 * GameEngine to which this game layer belongs.
	 */
    public GameEngine gameEngine;

    /**
	 * Direct link to the AssetManager instance (permitting more direct access
	 * to asset manager functionality if needed)
	 */
    public AssetManager assetManager;

    /**
	 * Direct link to the GameInputEventManager instance (permitting more direct
	 * access to game input information if needed)
	 */
    public GameInputEventManager inputEvent;

    /**
	 * Create a new and empty GameLayer instance.
	 * <P>
	 * Note: Initially the layer is set to an active/visible state
	 * 
	 * @param gameLayerName
	 *            name of the game layer
	 * @param gameEngine
	 *            GameEngine to which this game layer belongs
	 */
    public GameLayer(String gameLayerName, GameEngine gameEngine) {
        this.gameLayerName = gameLayerName;
        if (gameEngine != null) {
            this.gameEngine = gameEngine;
            assetManager = gameEngine.assetManager;
            inputEvent = gameEngine.inputEvent;
        }
        gameLayerActivity = GameLayerActivity.ACTIVE;
        gameLayerVisibility = GameLayerVisibility.VISIBLE;
        gameObjects = new HashMap<String, GameObject>(GAMEOBJECT_STORESIZE);
        gameObjectCollections = new HashMap<String, GameObjectCollection>();
        gameObjectsDrawOrderSorted = new GameObjectCollection("GameObjectsSortedOnDrawOrder", GAMEOBJECTGRAPHICAL_STORESIZE);
        gameObjectsToAdd = new ArrayList<GameObject>(GAMEOBJECT_ADD_QUEUE_STORESZIE);
        gameObjectsToAddSetIdentifier = new ArrayList<String>(GAMEOBJECT_ADD_QUEUE_STORESZIE);
        gameObjectsToRemove = new ArrayList<GameObject>(GAMEOBJECT_REMOVE_QUEUE_STORESIZE);
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
        viewPortWidth = gameEngine.screenWidth;
        viewPortHeight = gameEngine.screenHeight;
        viewPortScreenX = gameEngine.screenWidth / 2;
        viewPortScreenY = gameEngine.screenHeight / 2;
        viewPortLayerX = gameEngine.screenWidth / 2;
        viewPortLayerY = gameEngine.screenHeight / 2;
    }

    /**
	 * Create a new and empty GameLayer instance with the specified layer width
	 * and height.
	 * <P>
	 * Note: Initially the layer is set to an active/visible state
	 * 
	 * @param gameLayerName
	 *            name of the game layer
	 * @param gameEngine
	 *            GameEngine to which this game layer belongs
	 * @param width
	 *            double initial layer width
	 * @param height
	 *            double initial layer height
	 */
    public GameLayer(String gameLayerName, GameEngine gameEngine, double width, double height) {
        this(gameLayerName, gameEngine);
        this.width = width;
        this.height = height;
    }

    /**
	 * Create a new and empty GameLayer instance with the specified layer width
	 * and height and layer viewport location
	 * <P>
	 * Note: Initially the layer is set to an active/visible state
	 * 
	 * @param gameLayerName
	 *            name of the game layer
	 * @param gameEngine
	 *            GameEngine to which this game layer belongs
	 * @param width
	 *            double initial layer width
	 * @param height
	 *            double initial layer height
	 * @param viewPortLayerX
	 *            x layer viewport offset
	 * @param viewPortLayerY
	 *            y layer viewport offset
	 */
    public GameLayer(String gameLayerName, GameEngine gameEngine, double width, double height, double viewPortLayerX, double viewPortLayerY) {
        this(gameLayerName, gameEngine);
        this.width = width;
        this.height = height;
        this.viewPortLayerX = viewPortLayerX;
        this.viewPortLayerY = viewPortLayerY;
    }

    /**
	 * Create a new and empty GameLayer instance with the specified layer width
	 * and height and layer viewport location and screen viewport location and
	 * size.
	 * <P>
	 * Note: Initially the layer is set to an active/visible state
	 * 
	 * @param gameLayerName
	 *            name of the game layer
	 * @param gameEngine
	 *            GameEngine to which this game layer belongs
	 * @param width
	 *            double initial layer width
	 * @param height
	 *            double initial layer height
	 * @param viewPortLayerX
	 *            x layer viewport offset
	 * @param viewPortLayerY
	 *            y layer viewport offset
	 * @param viewPortScreenX
	 *            x screen viewport offset
	 * @param viewPortScreenY
	 *            y screen viewport offset
	 * @param viewPortWidth
	 *            width of the screen viewport
	 * @param viewPortHeight
	 *            height of the screen viewport
	 */
    public GameLayer(String gameLayerName, GameEngine gameEngine, double width, double height, double viewPortLayerX, double viewPortLayerY, double viewPortScreenX, double viewPortScreenY, double viewPortWidth, double viewPortHeight) {
        this(gameLayerName, gameEngine);
        this.width = width;
        this.height = height;
        this.viewPortLayerX = viewPortLayerX;
        this.viewPortLayerY = viewPortLayerY;
        this.viewPortWidth = viewPortWidth;
        this.viewPortHeight = viewPortHeight;
        this.viewPortScreenX = viewPortScreenX;
        this.viewPortScreenY = viewPortScreenY;
    }

    /**
	 * Set the game layer visibility and activity to that specified.
	 * 
	 * @param gameLayerVisibility
	 *            game layer visibility
	 * @param gameLayerActivity
	 *            game layer activity
	 */
    public void setState(GameLayerVisibility gameLayerVisibility, GameLayerActivity gameLayerActivity) {
        setVisibility(gameLayerVisibility);
        setActivity(gameLayerActivity);
    }

    /**
	 * Set the game layer visibility to that specified.
	 * 
	 * @param gameLayerVisibility
	 *            game layer visibility
	 */
    public void setVisibility(GameLayerVisibility gameLayerVisibility) {
        this.gameLayerVisibility = gameLayerVisibility;
        if (gameLayerVisibility == GameLayerVisibility.VISIBLE) {
            enterVisibleState();
        } else {
            enterInvisibleState();
        }
    }

    /**
	 * Return the visibility of the game layer
	 * 
	 * @return visibility of the game layer
	 */
    public GameLayerVisibility getVisibility() {
        return gameLayerVisibility;
    }

    /**
	 * Set the game layer activity to that specified.
	 * 
	 * @param gameLayerActivity
	 *            game layer activity
	 */
    public void setActivity(GameLayerActivity gameLayerActivity) {
        this.gameLayerActivity = gameLayerActivity;
        if (gameLayerActivity == GameLayerActivity.ACTIVE) {
            enterActiveState();
        } else {
            enterInactiveState();
        }
    }

    /**
	 * Return the activity of the game layer
	 * 
	 * @return activity of the game layer
	 */
    public GameLayerActivity getActivity() {
        return gameLayerActivity;
    }

    /**
	 * Perform actions upon entry to a visible game state
	 */
    protected void enterVisibleState() {
    }

    /**
	 * Perform actions upon entry to an invisible game state
	 */
    protected void enterInvisibleState() {
    }

    /**
	 * Perform actions upon entry to an active game state
	 */
    protected void enterActiveState() {
    }

    /**
	 * Perform actions upon entry to an inactive game state
	 */
    protected void enterInactiveState() {
    }

    /**
	 * Set the layer viewport to the specified x and y layer offsets
	 * 
	 * @param viewPortLayerX
	 *            x layer viewport offset
	 * @param viewPortLayerY
	 *            y layer viewport offset
	 */
    public final void positionViewPort(double viewPortLayerX, double viewPortLayerY) {
        this.viewPortLayerX = viewPortLayerX;
        this.viewPortLayerY = viewPortLayerY;
        validateViewPort();
    }

    /**
	 * Set the view port to the specified screen x and y offsets
	 * 
	 * @param viewPortScreenX
	 *            x screen viewport offset
	 * @param viewPortScreenY
	 *            y screen viewport offset
	 */
    public final void positionViewPortScreenProjection(double viewPortScreenX, double viewPortScreenY) {
        this.viewPortScreenX = viewPortScreenX;
        this.viewPortScreenY = viewPortScreenY;
        validateViewPort();
    }

    /**
	 * Move the layer viewport the specified distance along the x and y axis
	 * 
	 * @param viewPortMoveX
	 *            distance to move along the x axis
	 * @param viewPortMoveY
	 *            distance to move along the y axis
	 */
    public final void moveViewPort(double viewPortMoveX, double viewPortMoveY) {
        this.viewPortLayerX += viewPortMoveX;
        this.viewPortLayerY += viewPortMoveY;
        validateViewPort();
    }

    /**
	 * Move the layer viewport up by the specified distance
	 * <P>
	 * Note: The up direction is relative to the physical screen as opposed to
	 * the y-axis.
	 * 
	 * @param magnitude
	 *            number of pixels by which the viewport is to be moved
	 */
    public final void moveViewPortUp(double magnitude) {
        viewPortLayerY -= magnitude;
        validateViewPort();
    }

    /**
	 * Move the layer viewport down by the specified distance
	 * <P>
	 * Note: The down direction is relative to the physical screen as opposed to
	 * the y-axis.
	 * 
	 * @param magnitude
	 *            number of pixels by which the viewport is to be moved
	 */
    public final void moveViewPortDown(double magnitude) {
        viewPortLayerY += magnitude;
        validateViewPort();
    }

    /**
	 * Move the layer viewport left by the specified distance
	 * 
	 * @param magnitude
	 *            number of pixels by which the viewport is to be moved
	 */
    public final void moveViewPortLeft(double magnitude) {
        viewPortLayerX -= magnitude;
        validateViewPort();
    }

    /**
	 * Move the layer viewport right by the specified distance
	 * 
	 * @param magnitude
	 *            number of pixels by which the viewport is to be moved
	 */
    public final void moveViewPortRight(double magnitude) {
        viewPortLayerX += magnitude;
        validateViewPort();
    }

    /**
	 * Validate the viewport to ensure that the relevant layer viewport offset
	 * is within the bounds of the game layer dimensions
	 */
    public final void validateViewPort() {
        double viewPortHalfWidth = 0.5 * viewPortWidth / drawScaleFactor;
        double viewPortHalfHeight = 0.5 * viewPortHeight / drawScaleFactor;
        if (viewPortHalfWidth > width / 2.0) {
            viewPortHalfWidth = width / 2.0;
        }
        if (viewPortHalfHeight > height / 2.0) {
            viewPortHalfHeight = height / 2.0;
        }
        if (viewPortLayerX - viewPortHalfWidth < 0) {
            viewPortLayerX = viewPortHalfWidth;
        } else if (viewPortLayerX + viewPortHalfWidth > width) {
            viewPortLayerX = width - viewPortHalfWidth;
        }
        if (viewPortLayerY - viewPortHalfHeight < 0) {
            viewPortLayerY = viewPortHalfHeight;
        } else if (viewPortLayerY + viewPortHalfHeight > height) {
            viewPortLayerY = height - viewPortHalfHeight;
        }
    }

    /**
	 * Center the layer viewport onto the specified game object within the
	 * specified bounds of freedom of movement
	 * <P>
	 * Note: the freeMoveX and freeMoveY offsets are relative to the screen
	 * viewport dimensions, i.e. they will scale in accordance with the layer
	 * draw scale factor.
	 * 
	 * @param targetObject
	 *            GameObject that will be subject to viewport focus
	 * @param offsetX
	 *            double x offset to use relative to the viewport center
	 * @param offsetY
	 *            double y offset to use relative to the viewport center
	 * @param freeMoveX
	 *            double distance from the offset viewport center before a
	 *            change of viewport will be enforced
	 * @param freeMoveY
	 *            double distance from the offset viewport center before a
	 *            change of viewport will be enforced
	 */
    public final void centerViewportOnGameObject(GameObject targetObject, double offsetX, double offsetY, double freeMoveX, double freeMoveY) {
        double targetX = targetObject.x - offsetX;
        double targetY = targetObject.y - offsetY;
        double targetFocusHalfWidth = 0.5 * freeMoveX / drawScaleFactor;
        double targetFocusHalfHeight = 0.5 * freeMoveY / drawScaleFactor;
        if (targetX + targetObject.getWidth() / 2.0 < viewPortLayerX - targetFocusHalfWidth) {
            moveViewPortLeft((viewPortLayerX - targetFocusHalfWidth) - (targetX + targetObject.getWidth() / 2.0));
        } else if (targetX - targetObject.getWidth() / 2.0 > viewPortLayerX + targetFocusHalfWidth) {
            moveViewPortRight((targetX - targetObject.getWidth() / 2.0) - (viewPortLayerX + targetFocusHalfWidth));
        }
        if (targetY + targetObject.getHeight() / 2.0 < viewPortLayerY - targetFocusHalfHeight) {
            moveViewPortUp((viewPortLayerY - targetFocusHalfHeight) - (targetY + targetObject.getHeight() / 2.0));
        } else if (targetY - targetObject.getHeight() / 2.0 > viewPortLayerY + targetFocusHalfHeight) {
            moveViewPortDown((targetY - targetObject.getHeight() / 2.0) - (viewPortLayerY + targetFocusHalfHeight));
        }
    }

    /**
	 * Center the layer viewport onto the specified layer position within the
	 * specified bounds of freedom of movement
	 * <P>
	 * Note: the freeMoveX and freeMoveY offsets are relative to the screen
	 * viewport dimensions, i.e. they will scale in accordance with the layer
	 * draw scale factor.
	 * 
	 * @param targetX
	 *            double x target to use
	 * @param targetY
	 *            double y target to use
	 * @param freeMoveX
	 *            double distance from the offset viewport center before a
	 *            change of viewport will be enforced
	 * @param freeMoveY
	 *            double distance from the offset viewport center before a
	 *            change of viewport will be enforced
	 */
    public final void centerViewportOnPosition(double targetX, double targetY, double freeMoveX, double freeMoveY) {
        double targetFocusHalfWidth = 0.5 * freeMoveX / drawScaleFactor;
        double targetFocusHalfHeight = 0.5 * freeMoveY / drawScaleFactor;
        if (targetX < viewPortLayerX - targetFocusHalfWidth) {
            moveViewPortLeft((viewPortLayerX - targetFocusHalfWidth) - targetX);
        } else if (targetX > viewPortLayerX + targetFocusHalfWidth) {
            moveViewPortRight(targetX - (viewPortLayerX + targetFocusHalfWidth));
        }
        if (targetY < viewPortLayerY - targetFocusHalfHeight) {
            moveViewPortUp((viewPortLayerY - targetFocusHalfHeight) - targetY);
        } else if (targetY > viewPortLayerY + targetFocusHalfHeight) {
            moveViewPortDown(targetY - (viewPortLayerY + targetFocusHalfHeight));
        }
    }

    /**
	 * Based upon detected mouse interaction either move and/or scale the
	 * viewport.
	 * <P>
	 * Note: Moving the mouse to the edges of the screen will result in the
	 * viewport being scrolled. Moving the mouse wheel will result in the draw
	 * scale factor changing.
	 * <P>
	 * This method would benefit from extension to provide a greater degree of
	 * customisation, i.e. permitting mouse scrolling should the view leave the
	 * confines of the viewport (and not only touching the edges of the screen)
	 * or imposing limits on the mouse wheel scaling.
	 */
    public final void moveAndScaleViewportOnMouseInteraction() {
        int rotation = inputEvent.mouseWheelRotation();
        if (rotation < 0) {
            drawScaleFactor *= VIEWPORT_MOUSE_SCALE_SPEED;
        } else if (rotation > 0) {
            drawScaleFactor /= VIEWPORT_MOUSE_SCALE_SPEED;
        }
        if (GameInputEventManager.mouseXCoordinate == 0) {
            moveViewPortLeft((int) (Math.ceil(VIEWPORT_MOUSE_SCROLL_SPEED / drawScaleFactor)));
        } else if (GameInputEventManager.mouseXCoordinate == gameEngine.screenWidth - 1) {
            moveViewPortRight((int) (Math.ceil(VIEWPORT_MOUSE_SCROLL_SPEED / drawScaleFactor)));
        }
        if (GameInputEventManager.mouseYCoordinate == 0) {
            moveViewPortUp((int) (Math.ceil(VIEWPORT_MOUSE_SCROLL_SPEED / drawScaleFactor)));
        } else if (GameInputEventManager.mouseYCoordinate == gameEngine.screenHeight - 1) {
            moveViewPortDown((int) (Math.ceil(VIEWPORT_MOUSE_SCROLL_SPEED / drawScaleFactor)));
        }
    }

    /**
	 * Return the x layer location that corresponds to the specified on screen x
	 * location.
	 * 
	 * @param screenX
	 *            integer screen x location to be converted
	 * @return double corresponding x layer position
	 */
    public final double getLayerPositionFromScreenX(int screenX) {
        return ((double) screenX - viewPortScreenX) / drawScaleFactor + viewPortLayerX;
    }

    /**
	 * Return the y layer location that corresponds to the specified on screen y
	 * location.
	 * 
	 * @param screenY
	 *            integer screen y location to be converted
	 * @return double corresponding y layer position
	 */
    public final double getLayerPositionFromScreenY(int screenY) {
        return ((double) screenY - viewPortScreenY) / drawScaleFactor + viewPortLayerY;
    }

    /**
	 * Return the x screen location that corresponds to the specified on layer x
	 * location.
	 * <P>
	 * Note: This method can be invoked by game objects when they wish to obtain
	 * a on screen draw position that will correspond to a specified layer
	 * location.
	 * 
	 * @param layerX
	 *            double layer x location to be converted
	 * @return double corresponding x screen position
	 */
    public final int getScreenDrawPositionFromLayerX(double layerX) {
        return (int) (viewPortScreenX / drawScaleFactor + (layerX - viewPortLayerX));
    }

    /**
	 * Return the y screen location that corresponds to the specified on layer y
	 * location.
	 * <P>
	 * Note: This method can be invoked by game objects when they wish to obtain
	 * a on screen draw position that will correspond to a specified layer
	 * location.
	 * 
	 * @param layerY
	 *            double layer y location to be converted
	 * @return double corresponding y screen position
	 */
    public final int getScreenDrawPositionFromLayerY(double layerY) {
        return (int) (viewPortScreenY / drawScaleFactor + (layerY - viewPortLayerY));
    }

    /**
	 * Create a new game object set referred by the specific set name
	 * 
	 * @param gameObjectSetname
	 *            referral name for the created game object set
	 * 
	 * @exception IllegalArgumentException
	 *                if a game set already exists with the specified name
	 */
    public final void addGameObjectCollection(String gameObjectSetname) {
        if (gameObjectCollections.containsKey(gameObjectSetname)) {
            throw new IllegalArgumentException("GameLayer.addGameObjectSet: " + "Set already exists: " + gameObjectSetname);
        }
        gameObjectCollections.put(gameObjectSetname, new GameObjectCollection(gameObjectSetname));
    }

    /**
	 * Delete the specified game object set
	 * 
	 * @param gameObjectSetname
	 *            referral name of the game object set to be removed
	 * 
	 * @exception IllegalArgumentException
	 *                if a game object set does not exist
	 */
    public final void removeGameObjectCollection(String gameObjectSetname) {
        if (gameObjectCollections.containsKey(gameObjectSetname) == false) {
            throw new IllegalArgumentException("GameLayer.removeGameObjectSet: " + "Set does not exist: " + gameObjectSetname);
        }
        gameObjectCollections.remove(gameObjectSetname);
    }

    /**
	 * Retrieve the specified game object set
	 * 
	 * @param gameObjectSetname
	 *            referral name of the game object set to be retreived
	 * 
	 * @exception IllegalArgumentException
	 *                if a game object set does not exist
	 */
    public final GameObjectCollection getGameObjectCollection(String gameObjectSetname) {
        if (gameObjectCollections.containsKey(gameObjectSetname) == false) {
            throw new IllegalArgumentException("GameLayer.getGameObjectSet: " + "Set does not exist: " + gameObjectSetname);
        }
        return gameObjectCollections.get(gameObjectSetname);
    }

    /**
	 * Return if this game layer contains the specified game object collection
	 * 
	 * @param gameObjectSetname
	 *            referral name of the game object set to be checked
	 * @return boolean true if the layer contains the specified game object
	 *         collection, otherwise false
	 */
    public final boolean hasGameObjectCollection(String gameObjectSetname) {
        if (gameObjectCollections.containsKey(gameObjectSetname) == false) {
            return false;
        } else {
            return true;
        }
    }

    /**
	 * Retrieve the specified game object
	 * 
	 * @param gameObjectName
	 *            name of the game object to be retreived
	 */
    public final GameObject getGameObject(String gameObjectName) {
        return gameObjects.get(gameObjectName);
    }

    /**
	 * Queue the specified game object to be added.
	 * 
	 * @param gameObject
	 *            game object to be queued for addition
	 */
    public void queueGameObjectToAdd(GameObject gameObject) {
        gameObjectsToAdd.add(gameObject);
        gameObjectsToAddSetIdentifier.add(new String(""));
    }

    /**
	 * Queue the specified game object to be added to the specified game object
	 * set.
	 * 
	 * @param gameObject
	 *            game object to be queued for addition
	 * @param gameObjectSetname
	 *            name of the game object set to which the game object should be
	 *            added.
	 */
    public void queueGameObjectToAdd(GameObject gameObject, String gameObjectSetname) {
        gameObjectsToAdd.add(gameObject);
        gameObjectsToAddSetIdentifier.add(gameObjectSetname);
    }

    /**
	 * Add all currently queued game objects.
	 */
    public void addQueuedGameObjects() {
        for (int idx = 0; idx < gameObjectsToAdd.size(); idx++) {
            addGameObject(gameObjectsToAdd.get(idx), gameObjectsToAddSetIdentifier.get(idx));
        }
        gameObjectsToAdd.clear();
        gameObjectsToAddSetIdentifier.clear();
    }

    /**
	 * Add the specified game object
	 * 
	 * @param gameObject
	 *            game object to be added
	 */
    public void addGameObject(GameObject gameObject) {
        addGameObject(gameObject, "");
    }

    /**
	 * Add the specified game object, optionally to the specified game object
	 * set.
	 * 
	 * @param gameObject
	 *            game object to be added
	 * @param gameObjectSetname
	 *            name of the game object set to which the game object will be
	 *            added.
	 * 
	 * @exception NullPointerException
	 *                given a null game object or set name
	 * @exception IllegalArgumentException
	 *                if an invalid game set name is specified or attempting to
	 *                add a game object with the same name as an existing game
	 *                object.
	 */
    public void addGameObject(GameObject gameObject, String gameObjectSetname) {
        if (gameObject == null || gameObjectSetname == null) {
            throw new NullPointerException("GameLayer.addGameObject: " + "NULL pointer specified.");
        }
        if (gameObjects.containsKey(gameObject.getName())) {
            if (gameObject.gameObjectName.length() > 10 && gameObject.getName().substring(0, 10).compareTo("GameObject") == 0) {
                gameObject.setName("GameObject" + GameObject.UNIQUE_GAMEOBJECT_NEXT_ID++);
            } else {
                throw new IllegalArgumentException("GameLayer.addGameObject: " + "Adding game object with same name as " + "stored game object: " + gameObject.getName());
            }
        }
        if (gameObjectSetname.length() > 0) {
            if (gameObjectCollections.containsKey(gameObjectSetname) == false) {
                throw new IllegalArgumentException("GameLayer.addGameObject: " + "Adding game object with invalid set name: " + gameObjectSetname);
            }
            GameObjectCollection gameObjectCollection = gameObjectCollections.get(gameObjectSetname);
            gameObjectCollection.add(gameObject);
            gameObject.gameObjectCollection = gameObjectCollection;
        }
        gameObjects.put(gameObject.getName(), gameObject);
        int bottomIdx = -1;
        int middleIdx;
        int topIdx = gameObjectsDrawOrderSorted.size();
        int targetDrawOrder = gameObject.getDrawOrder();
        while (topIdx - bottomIdx > 1) {
            middleIdx = (topIdx + bottomIdx) / 2;
            if (gameObjectsDrawOrderSorted.gameObjects[middleIdx].getDrawOrder() < targetDrawOrder) {
                bottomIdx = middleIdx;
            } else {
                topIdx = middleIdx;
            }
        }
        gameObjectsDrawOrderSorted.add(topIdx, gameObject);
    }

    /**
	 * Queue the specified game object to be removed.
	 * <P>
	 * Note: It is important that if the game object for removal is also part of
	 * a game object set then the queueGameObjectToRemove( GameObject, String )
	 * method is used to ensure that the game object is also removed from the
	 * associated game set. This method could be extended to check to see if an
	 * object to be removed as also part of game object set to the arrange for
	 * it to be removed from the game object set as well. However, for reasons
	 * for performance (and the fact that it should be a major issue for the
	 * game to arrange for the correct method to be called) this has not been
	 * done.
	 * 
	 * @param gameObject
	 *            game object to be queued for removal
	 */
    public void queueGameObjectToRemove(GameObject gameObject) {
        gameObjectsToRemove.add(gameObject);
    }

    /**
	 * Removed all currently queued game objects.
	 */
    public void removeQueuedGameObjects() {
        for (int idx = 0; idx < gameObjectsToRemove.size(); idx++) {
            removeGameObject(gameObjectsToRemove.get(idx));
        }
        gameObjectsToRemove.clear();
    }

    /**
	 * Remove the specified game object, optionally from the specified game
	 * object set.
	 * 
	 * @param gameObject
	 *            game object to be removed
	 * @param gameObjectSetname
	 *            name of the game object set to which the game object will be
	 *            removed.
	 * 
	 * @exception NullPointerException
	 *                given a null game object or set name
	 * @exception IllegalArgumentException
	 *                if an invalid set is specified
	 */
    public void removeGameObject(GameObject gameObject) {
        if (gameObject == null) {
            throw new NullPointerException("GameLayer.removeGameObject: " + "NULL pointer specified.");
        }
        if (gameObject.gameObjectCollection != null) {
            gameObject.gameObjectCollection.remove(gameObject);
        }
        gameObjects.remove(gameObject.getName());
        gameObjectsDrawOrderSorted.remove(gameObject);
    }

    /**
	 * Remove all game objects from this game layer
	 */
    public void removeAllGameObjects() {
        gameObjects = new HashMap<String, GameObject>(GAMEOBJECT_STORESIZE);
        for (GameObjectCollection gameObjectCollection : gameObjectCollections.values()) {
            gameObjectCollection.clear();
        }
        gameObjectsDrawOrderSorted = new GameObjectCollection("GameObjectsSortedOnDrawOrder", GAMEOBJECTGRAPHICAL_STORESIZE);
        gameObjectsToAdd = new ArrayList<GameObject>(GAMEOBJECT_ADD_QUEUE_STORESZIE);
        gameObjectsToAddSetIdentifier = new ArrayList<String>(GAMEOBJECT_ADD_QUEUE_STORESZIE);
        gameObjectsToRemove = new ArrayList<GameObject>(GAMEOBJECT_REMOVE_QUEUE_STORESIZE);
    }

    /**
	 * Sort all game objects based upon their defined draw order
	 * <P>
	 * Note: This method uses a modified form of bubble sort to order the game
	 * objects into the correct sequence. This form of algorithm will work
	 * faster than other forms of sort algorithm, e.g. quick sort, merge sort,
	 * etc. if the objects are nearly in sorted order and only a few elements
	 * need to be 'corrected' - which will be the case for adding a few game
	 * objects to an already sorted set of game objects.
	 */
    public final void sortGameObjectsOnDrawOrder() {
        GameObject tempGameObject;
        int size = gameObjectsDrawOrderSorted.size();
        for (int i = 0; i < size - 1; i++) {
            if (gameObjectsDrawOrderSorted.gameObjects[i].getDrawOrder() > gameObjectsDrawOrderSorted.gameObjects[i + 1].getDrawOrder()) {
                tempGameObject = gameObjectsDrawOrderSorted.gameObjects[i];
                gameObjectsDrawOrderSorted.gameObjects[i] = gameObjectsDrawOrderSorted.gameObjects[i + 1];
                gameObjectsDrawOrderSorted.gameObjects[i + 1] = tempGameObject;
                int j = i;
                while (j > 0 && gameObjectsDrawOrderSorted.gameObjects[j].getDrawOrder() < gameObjectsDrawOrderSorted.gameObjects[j - 1].getDrawOrder()) {
                    tempGameObject = gameObjectsDrawOrderSorted.gameObjects[j];
                    gameObjectsDrawOrderSorted.gameObjects[j] = gameObjectsDrawOrderSorted.gameObjects[j - 1];
                    gameObjectsDrawOrderSorted.gameObjects[j - 1] = tempGameObject;
                    j--;
                }
                j = i + 1;
                while (j < size - 1 && gameObjectsDrawOrderSorted.gameObjects[j].getDrawOrder() > gameObjectsDrawOrderSorted.gameObjects[j + 1].getDrawOrder()) {
                    tempGameObject = gameObjectsDrawOrderSorted.gameObjects[j];
                    gameObjectsDrawOrderSorted.gameObjects[j] = gameObjectsDrawOrderSorted.gameObjects[j + 1];
                    gameObjectsDrawOrderSorted.gameObjects[j + 1] = tempGameObject;
                    j++;
                }
            }
        }
    }

    /**
	 * Return the on-screen x location of the specified game object
	 * 
	 * @param gameObject
	 *            GameObject to lookup
	 * @return int on-screen x location of this game object
	 */
    public final int getGameObjectScreenX(GameObject gameObject) {
        return (int) (viewPortScreenX + (gameObject.x - viewPortLayerX) * drawScaleFactor);
    }

    /**
	 * Return the on-screen y location of the specified game object
	 * 
	 * @param gameObject
	 *            GameObject to lookup
	 * @return int on-screen y location of this game object
	 */
    public final int getGameObjectScreenY(GameObject gameObject) {
        return (int) (viewPortScreenY + (gameObject.y - viewPortLayerY) * drawScaleFactor);
    }

    /**
	 * Return the on-screen width of the specified game object
	 * 
	 * @param gameObject
	 *            GameObject to lookup
	 * @return int on-screen width of this game object
	 */
    public final int getGameObjectScreenWidth(GameObject gameObject) {
        return (int) (gameObject.getWidth() * drawScaleFactor);
    }

    /**
	 * Return the on-screen height of the specified game object
	 * 
	 * @param gameObject
	 *            GameObject to lookup
	 * @return int on-screen height of this game object
	 */
    public final int getGameObjectScreenHeight(GameObject gameObject) {
        return (int) (gameObject.getHeight() * drawScaleFactor);
    }

    /**
	 * Return the on-screen bounding rectangle of the specified game object
	 * 
	 * @param gameObject
	 *            GameObject to lookup
	 * @return Rectangle on-screen bounding rectangle of the game object
	 */
    public final Rectangle getGameObjectScreenRectangle(GameObject gameObject) {
        Rectangle objectBound = gameObject.getBoundingRectangle();
        objectBound.x = (int) (viewPortScreenX + (objectBound.x - viewPortLayerX) * drawScaleFactor);
        objectBound.y = (int) (viewPortScreenY + (objectBound.y - viewPortLayerY) * drawScaleFactor);
        objectBound.width *= drawScaleFactor;
        objectBound.height *= drawScaleFactor;
        return objectBound;
    }

    /**
	 * Default game layer update behaviour (if the layer is active then add or
	 * remove queued game objects, if any).
	 */
    public void update() {
        if (gameLayerActivity == GameLayerActivity.ACTIVE) {
            removeQueuedGameObjects();
            addQueuedGameObjects();
        }
    }

    /**
	 * As each game layer can observe other object (e.g. game objects, assets,
	 * etc.). Provide an appropriate method to enable update messages to be
	 * processed.
	 */
    public void update(Observable observableObject, Object message) {
    }

    /**
	 * Draw all graphical game objects contained within the game layer to the
	 * specified graphics object.
	 * <P>
	 * If the game layer is currently INVISIBLE then no objects will be drawn.
	 */
    public void draw(Graphics2D graphics2D) {
        if (gameLayerVisibility != GameLayerVisibility.VISIBLE) {
            return;
        }
        AffineTransform originalTransform = graphics2D.getTransform();
        java.awt.Shape originalClipRegion = graphics2D.getClip();
        if (drawScaleFactor != 1.0) {
            graphics2D.transform(AffineTransform.getScaleInstance(drawScaleFactor, drawScaleFactor));
        }
        double viewPortHalfWidth = (viewPortWidth / 2) / drawScaleFactor;
        double viewPortHalfHeight = (viewPortHeight / 2) / drawScaleFactor;
        if (viewPortWidth < gameEngine.screenWidth || viewPortHeight < gameEngine.screenHeight) {
            originalClipRegion = graphics2D.getClip();
            graphics2D.setClip((int) (viewPortScreenX / drawScaleFactor - viewPortHalfWidth), (int) (viewPortScreenY / drawScaleFactor - viewPortHalfHeight), (int) (2.0 * viewPortHalfWidth), (int) (2.0 * viewPortHalfHeight));
        }
        validateViewPort();
        int gameObjectsDrawOrderSortedSize = gameObjectsDrawOrderSorted.size();
        for (int drawIdx = 0; drawIdx < gameObjectsDrawOrderSortedSize; drawIdx++) {
            GameObject gameObject = gameObjectsDrawOrderSorted.gameObjects[drawIdx];
            double gameObjectHalfBound = gameObject.boundingDimension / 2;
            if ((gameObject.x - gameObjectHalfBound > viewPortLayerX + viewPortHalfWidth || gameObject.x + gameObjectHalfBound < viewPortLayerX - viewPortHalfWidth || gameObject.y - gameObjectHalfBound > viewPortLayerY + viewPortHalfHeight || gameObject.y + gameObjectHalfBound < viewPortLayerY - viewPortHalfHeight) == false) {
                gameObject.draw(graphics2D, (int) (viewPortScreenX / drawScaleFactor + (gameObject.x - viewPortLayerX)), (int) (viewPortScreenY / drawScaleFactor + (gameObject.y - viewPortLayerY)));
            }
        }
        if (drawGameObjectDebugInformation) {
            for (int drawIdx = 0; drawIdx < gameObjectsDrawOrderSortedSize; drawIdx++) {
                GameObject gameObject = gameObjectsDrawOrderSorted.gameObjects[drawIdx];
                double gameObjectHalfBound = gameObject.boundingDimension / 2;
                if ((gameObject.x - gameObjectHalfBound > viewPortLayerX + viewPortHalfWidth || gameObject.x + gameObjectHalfBound < viewPortLayerX - viewPortHalfWidth || gameObject.y - gameObjectHalfBound > viewPortLayerY + viewPortHalfHeight || gameObject.y + gameObjectHalfBound < viewPortLayerY - viewPortHalfHeight) == false) {
                    drawGameObjectDebugInformation(graphics2D, gameObject, (int) (viewPortScreenX / drawScaleFactor + (gameObject.x - viewPortLayerX)), (int) (viewPortScreenY / drawScaleFactor + (gameObject.y - viewPortLayerY)));
                }
            }
        }
        graphics2D.setTransform(originalTransform);
        graphics2D.setClip(originalClipRegion);
    }

    /**
	 * Return the current draw order of this game layer
	 * 
	 * @return int draw order of this layer
	 */
    public final int getDrawOrder() {
        return drawOrder;
    }

    /**
	 * Set the draw order of this game layer to that specified
	 * 
	 * @param drawOrder
	 *            integer draw order to use
	 */
    public final void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
        gameEngine.sortGameLayersOnDrawOrder();
    }

    /**
	 * Return the current draw scale factor of this layer
	 * 
	 * @return double draw scale factor of this layer
	 */
    public final double getDrawScaleFactor() {
        return drawScaleFactor;
    }

    /**
	 * Set the draw scale factor of this game layer to that specified
	 * 
	 * @param drawScaleFactor
	 *            double draw scale factor to use
	 */
    public final void setDrawScaleFactor(double drawScaleFactor) {
        this.drawScaleFactor = drawScaleFactor;
    }

    /**
	 * This can be used to draw the bounding region and all rectangular bounds
	 * within a graphical game object. This is intended to be a debug method.
	 * <P>
	 * 
	 * @param graphics2D
	 *            Graphics object the bounds are to be drawn to
	 * @param gameObject
	 *            game object for which bounds will be drawn
	 * @param drawX
	 *            x screen location of gameObject(Note: screen x)
	 * @param drawY
	 *            y screen location of gameObject(Note: screen y)
	 */
    protected void drawGameObjectDebugInformation(Graphics2D graphics2D, GameObject gameObject, int drawX, int drawY) {
        Shape[] geometry = gameObject.getGeometry();
        if (geometry.length > 0) {
            java.awt.Color originalColor = graphics2D.getColor();
            AffineTransform originalTransform = graphics2D.getTransform();
            graphics2D.setColor(java.awt.Color.white);
            graphics2D.drawRect(drawX - (int) gameObject.boundingDimension / 2, drawY - (int) gameObject.boundingDimension / 2, (int) gameObject.boundingDimension, (int) gameObject.boundingDimension);
            graphics2D.setColor(java.awt.Color.green);
            java.awt.geom.AffineTransform rotationAffineTransform = new java.awt.geom.AffineTransform();
            rotationAffineTransform.rotate(gameObject.rotation, drawX, drawY);
            graphics2D.transform(rotationAffineTransform);
            for (int shapeIdx = 0; shapeIdx < geometry.length; shapeIdx++) {
                if (geometry[shapeIdx] instanceof Box) {
                    Box box = (Box) geometry[shapeIdx];
                    graphics2D.drawRect(drawX + (int) (box.offsetX - box.width / 2), drawY + (int) (box.offsetY - box.height / 2), (int) box.width, (int) box.height);
                } else {
                    Circle circle = (Circle) geometry[shapeIdx];
                    graphics2D.drawOval(drawX + (int) (circle.offsetX - circle.radius), drawY + (int) (circle.offsetY - circle.radius), (int) circle.radius * 2, (int) circle.radius * 2);
                    graphics2D.drawLine(drawX + (int) circle.offsetX, drawY + (int) circle.offsetY, drawX + (int) (circle.offsetX + circle.radius), drawY + (int) circle.offsetY);
                }
            }
            graphics2D.setColor(originalColor);
            graphics2D.setTransform(originalTransform);
        }
    }
}
