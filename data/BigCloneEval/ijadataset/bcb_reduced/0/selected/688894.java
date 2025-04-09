package org.freelords.game;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.freelords.armies.MarchingFormation;
import org.freelords.armies.fight.UnresolvedBattle;
import org.freelords.entity.EntityId;
import org.freelords.entity.FreelordsEntity;
import org.freelords.entity.FreelordsMapEntity;
import org.freelords.game.listener.CityListener;
import org.freelords.game.listener.EntityListener;
import org.freelords.game.listener.ItemListener;
import org.freelords.game.listener.StackListener;
import org.freelords.map.GameMap;
import org.freelords.map.TileSelection;
import org.freelords.player.AllPlayers;
import org.freelords.player.PlayerId;
import org.freelords.util.CollectionUtils;
import org.freelords.util.OwnedBy;
import org.freelords.util.ReflectiveCopy;
import org.freelords.util.SortedList;
import org.freelords.util.geom.Point;
import org.freelords.util.geom.Rect;

public class Game {

    /** List of UnresolvedBattles */
    private List<UnresolvedBattle> unresolvedBattles = new ArrayList<UnresolvedBattle>();

    /** List of EntityListener connecting a Game and an Entity */
    private List<EntityListener> entityListeners = new ArrayList<EntityListener>();

    /** List of StackListeners, specialized from EntityListener */
    private List<StackListener> stackListeners = new ArrayList<StackListener>();

    /** List of city listeners */
    private List<CityListener> cityListeners = new ArrayList<CityListener>();

    /** List of item listeners */
    private List<ItemListener> itemListeners = new ArrayList<ItemListener>();

    /** Obscure variable */
    private boolean serverGame;

    /** The current game map */
    private GameMap currentMap;

    /** List of all players */
    private AllPlayers players = new AllPlayers();

    /** Encapsulates all static data */
    private GameDatabase gameDatabase = new GameDatabase();

    /** Relation player, entity = ownership */
    private Map<PlayerId, OwnedBy> ownedBy = new HashMap<PlayerId, OwnedBy>();

    /** Relation EntityID, entity */
    private Map<EntityId, FreelordsEntity> entityMap = new HashMap<EntityId, FreelordsEntity>();

    /** Relation Entity, Position in map */
    private Map<FreelordsEntity, Point> entityLocated = new HashMap<FreelordsEntity, Point>();

    /** Turn counter */
    private int turn;

    /** Sorts MapEntities depending on their location on the map. */
    private Comparator<TileSelection> positionSorter = new Comparator<TileSelection>() {

        /** Compares the positions */
        public int compare(TileSelection o1, TileSelection o2) {
            return (o1.getPoint().getY() - o2.getPoint().getY()) * currentMap.getWidth() + (o1.getPoint().getX() - o2.getPoint().getX());
        }
    };

    /** List of entities at a certain tile position (opposite of entityLocated) */
    private SortedList<TileSelectionDefault> tileSelections = new SortedList<TileSelectionDefault>(positionSorter);

    /** Stores tileSelections (for speedup?) */
    private Map<Point, TileSelectionDefault> pointToSelection = new HashMap<Point, TileSelectionDefault>();

    /** Creates a new game instance. */
    public Game(boolean serverGame) {
        for (PlayerId pid : PlayerId.players()) {
            OwnedBy ob = new OwnedBy(this, pid);
            ownedBy.put(pid, ob);
        }
        ownedBy.put(null, new OwnedBy(this, null));
        this.serverGame = serverGame;
    }

    /** Should be called when the game starts. */
    public void init() {
    }

    /** Adds a new EntityListener to the list of objects to be informed about
	  * entity changes.
	  */
    public void addEntityListener(EntityListener ecl) {
        if (ecl == null) {
            throw new IllegalArgumentException("Listener can not be null");
        }
        entityListeners.add(ecl);
    }

    /** Removes an EntityListener. */
    public void removeEntityListener(EntityListener ecl) {
        entityListeners.remove(ecl);
    }

    /** Adds a new Stacklistener to the list of objects to be informed about
	  * army changes.
	  */
    public void addStackListener(StackListener stackTracker) {
        stackListeners.add(stackTracker);
    }

    /** Removes a StackListener. */
    public void removeStackListener(StackListener stackTracer) {
        stackListeners.remove(stackTracer);
    }

    /** Adds a CityListener to the list of objects informed about city changes. */
    public void addCityListener(CityListener tracker) {
        cityListeners.add(tracker);
    }

    /** Removes an object from the list of listeners to city events. */
    public void removeCityListener(CityListener tracker) {
        cityListeners.remove(tracker);
    }

    /** Returns all city listeners*/
    public Collection<CityListener> getCityListeners() {
        return Collections.unmodifiableCollection(cityListeners);
    }

    /** Adds an ItemListener to the list of objects informed about equipment. */
    public void addItemListener(ItemListener tracker) {
        itemListeners.add(tracker);
    }

    /** Removes an object from the list of listeners to item events. */
    public void removeItemListener(ItemListener tracker) {
        itemListeners.remove(tracker);
    }

    /** Returns a list of item listeners */
    public Collection<ItemListener> getItemListeners() {
        return Collections.unmodifiableCollection(itemListeners);
    }

    /** Sets the list of unresolved battles */
    public void setUnresolvedBattles(List<UnresolvedBattle> unresolvedBattles) {
        this.unresolvedBattles = unresolvedBattles;
    }

    /** Returns a list of unresolved battles */
    public List<UnresolvedBattle> getUnresolvedBattles() {
        return unresolvedBattles;
    }

    /** Returns the obscure variable serverGame's content */
    public boolean isServerGame() {
        return serverGame;
    }

    /** Sets the map data. */
    public void setCurrentMap(GameMap newMap) {
        currentMap = newMap;
    }

    /** Returns the map data. */
    public GameMap getCurrentMap() {
        return currentMap;
    }

    /** Returns a pointer to the static game data. */
    public GameDatabase getPersistentGameData() {
        return gameDatabase;
    }

    /** Returns all players taking part in the game. */
    public AllPlayers getAllPlayers() {
        return players;
    }

    /** Sets turn counter */
    public void setTurn(int turn) {
        this.turn = turn;
    }

    /** Returns turn counter */
    public int getTurn() {
        return turn;
    }

    /** Changes the static game data. */
    public void setPersistentGameData(GameDatabase persistentGameData) {
        this.gameDatabase = persistentGameData;
    }

    /** Returns the objects that each player possesses.
	  * 
	  * Note that the returned Map contains entries for all possible players,
	  * not only for those that take part in the game.
	  */
    public Map<PlayerId, OwnedBy> getOwners() {
        return ownedBy;
    }

    /** Returns a list of objects to be found at position p. */
    public TileSelection getTileSelection(Point p) {
        return getTileSelection(p, false);
    }

    /** Returns a list of objects to be found at position p and/or creates an
	  * empty one.
	  *
	  * @param p the point whose objects are requested.
	  * @param create if set to true and no objects are found, create an empty
	  * list.
	  * @return the list of objects on the given point.
	  */
    private TileSelectionDefault getTileSelection(Point p, boolean create) {
        TileSelectionDefault ts = pointToSelection.get(p);
        if (ts == null) {
            ts = new TileSelectionDefault(p);
            ts.setSourceTile(currentMap.getTile(p.getX(), p.getY()));
            if (create) {
                pointToSelection.put(p, ts);
                tileSelections.add(ts);
            }
        }
        return ts;
    }

    /** Adds a new entity to a given location.
	  *
	  * Since the Game class stores entities in the form of TileSelections,
	  * this tries to merge the new object with already existing objects or
	  * creates a new one. It also informs attached EntityListeners about the new
	  * object.
	  *
	  * @param fe the entity to be added
	  * @param point the point where the entity pops up
	  * @throws IllegalArgumentException if the entity occupies more than one
	  * space.
	  */
    public void add(FreelordsEntity fe, Point point) {
        FreelordsEntity old = entityMap.get(fe.getId());
        if (old != null) {
            if (entityLocated.containsKey(old)) {
                remove(old);
            }
            ReflectiveCopy.copy(fe, old);
            fe = old;
        }
        entityMap.put(fe.getId(), fe);
        fe.linkToGame(this);
        if (point != null) {
            FreelordsMapEntity fme = (FreelordsMapEntity) fe;
            if (fme.getWidth() > 1 || fme.getHeight() > 1) {
                throw new IllegalArgumentException("Can not handle non 1x1 objects yet.");
            }
            entityLocated.put(fe, point);
            TileSelectionDefault ts = getTileSelection(point, true);
            boolean created = ts.isEmpty();
            ts.add(fme);
            if (created) {
                for (StackListener sl : stackListeners) {
                    sl.stackCreated(ts);
                }
            }
        }
        for (EntityListener ecl : entityListeners) {
            ecl.entityAdded(this, fe);
        }
    }

    /** Removes and re-adds a given entity to the game.
	  * 
	  * Used, e.g., if the ownership of a city changes.
	  */
    public void update(FreelordsEntity fe) {
        Point position = getOptionalLocation(fe);
        remove(fe);
        add(fe, position);
    }

    /** Removes a single entity (on the map!) from the game. */
    public void remove(FreelordsEntity entity) {
        if (entityLocated.get(entity) == null) {
            entityMap.remove(entity.getId());
            for (EntityListener ecl : entityListeners) {
                ecl.entityRemoved(this, entity);
            }
        } else {
            removeSingleLocation(Collections.singleton(entity));
        }
    }

    /** Removes a set of entities (on the map!) from the game one by one. */
    public void remove(Collection<? extends FreelordsEntity> entities) {
        entities = new HashSet<FreelordsEntity>(entities);
        for (FreelordsEntity id : entities) {
            remove(id);
        }
    }

    /** Removes a set of entities from the game, where the entities are supposed
	  * to share the same tile.
	  *
	  * Also cleans up empty TileSelections after removal.
	  */
    private void removeSingleLocation(Collection<? extends FreelordsEntity> entities) {
        List<FreelordsEntity> removed = new ArrayList<FreelordsEntity>();
        Point sharedLocation = getSharedLocation(entities);
        for (FreelordsEntity id : entities) {
            removed.add(entityMap.remove(id.getId()));
            entityLocated.remove(id);
        }
        TileSelectionDefault ts = getTileSelection(sharedLocation, false);
        ts.remove(entities);
        if (ts.isEmpty()) {
            tileSelections.remove(ts);
            pointToSelection.remove(ts.getPoint());
            for (StackListener sl : stackListeners) {
                sl.stackRemoved(ts);
            }
        }
        for (FreelordsEntity rem : removed) {
            for (EntityListener ecl : entityListeners) {
                ecl.entityRemoved(this, rem);
            }
        }
    }

    /** Returns the (common) location of a set of entities.
	  * 
	  * @throws IllegalArgumentException for bad arguments or if the entities do
	  * not share the same map tile.
	  */
    private Point getSharedLocation(Collection<? extends FreelordsEntity> entityIds) {
        if (entityIds == null) {
            throw new IllegalArgumentException("Must provide an entityId");
        }
        Point oldPoint = null;
        for (FreelordsEntity id : entityIds) {
            Point newPoint = entityLocated.get(id);
            if (newPoint == null) {
                throw new IllegalArgumentException("Entity " + id + " is not even on the map.");
            }
            if (oldPoint == null) {
                oldPoint = newPoint;
            } else if (!oldPoint.equals(newPoint)) {
                throw new IllegalArgumentException("The entities " + entityIds + " belong on at least two different tiles " + oldPoint + "," + newPoint);
            }
        }
        return oldPoint;
    }

    /** Returns the location of an entity.
	  * 
	  * @throws IllegalArgumentException if the entity has not been introduced
	  * into the game yet.
	  */
    public Point getLocation(FreelordsEntity entityId) {
        Point p = entityLocated.get(entityId);
        if (p == null) {
            throw new IllegalArgumentException("The entity " + entityId + " is not even on the map.");
        }
        return p;
    }

    public TileSelection getSelectionFor(Collection<? extends FreelordsEntity> entityIds) {
        if (entityIds.isEmpty()) {
            throw new IllegalArgumentException("Must have an entity to find the location of.");
        }
        Iterator<? extends FreelordsEntity> it = entityIds.iterator();
        Point p = getLocation(it.next());
        TileSelection ts = getTileSelection(p, false);
        while (it.hasNext()) {
            if (!ts.contains(it.next())) {
                throw new IllegalArgumentException("Not all of " + entityIds + " are on " + ts);
            }
        }
        return ts;
    }

    /** Returns the location of an entity (without throwing exceptions). */
    public Point getOptionalLocation(FreelordsEntity entityId) {
        return entityLocated.get(entityId);
    }

    /** Moves a set of entities to a given point.
	  *
	  * Though not thoroughly checked, the entities should of course have a
	  * common location. The entities are removed from this location and added
	  * to the TileSelection at the destination.
	  *
	  * @param source the list of entities to be moved.
	  * @param destination the destination point of the movement.
	  */
    public TileSelection move(Collection<? extends FreelordsMapEntity> source, Point destination) {
        Point sourcePoint = getSharedLocation(source);
        TileSelectionDefault ts = getTileSelection(sourcePoint, false);
        ts.remove(source);
        if (ts.isEmpty()) {
            tileSelections.remove(ts);
            pointToSelection.remove(ts.getPoint());
        }
        TileSelectionDefault dest = getTileSelection(destination, true);
        for (FreelordsMapEntity fme : source) {
            dest.add(fme);
            entityLocated.put(fme, destination);
        }
        for (StackListener sl : stackListeners) {
            sl.stackItemsMoved(ts, dest);
        }
        TileSelection moved = dest.getSubSelection(source);
        List<MarchingFormation> mfs = MarchingFormation.findFormations(moved.getUnitStack());
        for (MarchingFormation mf : mfs) {
            if (mf.getRoute() != null && mf.getRoute().isWaypoint(destination)) {
                mf.getRoute().removeWaypoint(false);
            }
        }
        return dest.getSubSelection(source);
    }

    /** Adds a set of entities to a new TileSelection.
	  *
	  * The new TileSelection has the same location as the entities, and
	  * contains only them. Useful to get a subselection of entities in a
	  * TileSelection.
	  */
    public TileSelection getSubSelection(Collection<? extends FreelordsMapEntity> movingArmies) {
        Point sharedPoint = getSharedLocation(movingArmies);
        TileSelectionDefault dts = new TileSelectionDefault(sharedPoint);
        dts.setSourceTile(currentMap.getTile(sharedPoint.getX(), sharedPoint.getY()));
        for (FreelordsMapEntity entity : movingArmies) {
            dts.add(entity);
        }
        return dts;
    }

    /** Returns all elements that lie (at least partially) within a
	  * certain region of the map.
	  */
    public List<? extends TileSelection> getPopulatedTiles(int x, int y, int width, int height) {
        Rect bounds = new Rect(x, y, width, height);
        TileSelectionDefault topMost = new TileSelectionDefault(new Point(0, y));
        TileSelectionDefault bottomMost = new TileSelectionDefault(new Point(0, y + height));
        List<TileSelectionDefault> subList = tileSelections.getSubList(topMost, bottomMost);
        List<TileSelectionDefault> inBounds = new ArrayList<TileSelectionDefault>();
        for (TileSelectionDefault subListed : subList) {
            if (bounds.contains(subListed.getPoint().getX(), subListed.getPoint().getY())) {
                inBounds.add(subListed);
            }
        }
        return inBounds;
    }

    /** Returns an (unmodifiable) list of entities present on the map. */
    public Collection<FreelordsEntity> getAllEntities() {
        return Collections.unmodifiableCollection(entityMap.values());
    }

    /** Returns an (unmodifiable) list of entities of a certain type on the map. */
    public <C extends FreelordsEntity> Collection<C> getAllEntities(Class<C> clazz) {
        List<C> filter = new ArrayList<C>();
        CollectionUtils.filterInto(entityMap.values(), filter, clazz);
        return Collections.unmodifiableCollection(filter);
    }

    /** Returns an (unmodifiable) list of all TileSelections on the map. */
    public List<? extends TileSelection> getAllPopulatedTiles() {
        return Collections.unmodifiableList(tileSelections);
    }

    /** Returns a single entity with a specified id whose class can also be specified. */
    public <C extends FreelordsEntity> C getEntity(Class<C> clazz, EntityId id) {
        return getEntities(clazz, Collections.singleton(id)).get(0);
    }

    /** Returns, and if necessary creates, an entity.
	  * 
	  * @param clazz the exact class of the object to return
	  * @param id the id of the entity
	  * @return the entity or a template if the entity does not exist yet.
	  * @throws RuntimeException if the entity does not exist and could not
	  * be created (in detail: has no constructor that only takes the id as argument).
	  *
	  * This is essentially the same as {@link Game#getEntity}, but if the entity is not
	  * found, it will silently try to create a template for it. What is it for? Imagine,
	  * that the server tells the client about a new item that is assigned to Hero H. In
	  * some circumstances (game startup), the client does not know about H yet, because
	  * it is transferred after the item. However, the item knows that it belongs to H, and
	  * as soon as it is transferred, it will ask to have H's identity resolved. Using this
	  * function we can (try to) create a "pseudo-H" on the fly to have something to show to
	  * the item, which will be satisfied with this information. Afterwards, when the real
	  * instance of H is transferred to the client, it will silently replace the pseudo-H by
	  * the real H. In order to do this, we basically use some internals of the Java JRE.
	  * See {@link org.freelords.entity.FreelordsEntity} for details.
	  */
    public <C extends FreelordsEntity> C getCreateEntity(Class<C> clazz, EntityId id) {
        FreelordsEntity entity = entityMap.get(id);
        if (entity != null) {
            return clazz.cast(entity);
        } else {
            try {
                Constructor<C> construct = clazz.getConstructor(EntityId.class);
                C ent = construct.newInstance(id);
                entityMap.put(id, ent);
                return ent;
            } catch (Exception e) {
                throw new RuntimeException("Error when creating " + clazz + " with id " + id, e);
            }
        }
    }

    /** Returns a list of entities with specified id and a certain class type.
	  * 
	  * @param clazz we want to have the entities converted to this type
	  * @param entityIds the list of entities we want to have returned
	  * @return a list of entities properly cast to the correct type
	  * @throws IllegalArgumentException if a given id does not match any
	  * element or if an element cannot be converted.
	  */
    private <C extends FreelordsEntity> List<C> getEntities(Class<C> clazz, Collection<EntityId> entityIds) {
        List<C> ownedObjects = new ArrayList<C>(entityIds.size());
        for (EntityId armyId : entityIds) {
            FreelordsEntity fe = entityMap.get(armyId);
            if (fe == null) {
                throw new IllegalArgumentException("Unknown entity id " + armyId);
            }
            if (!clazz.isAssignableFrom(fe.getClass())) {
                throw new IllegalArgumentException("ID " + armyId + " Expecting " + clazz.getName() + " got " + fe.getClass().getName());
            }
            ownedObjects.add(clazz.cast(entityMap.get(armyId)));
        }
        return ownedObjects;
    }
}
