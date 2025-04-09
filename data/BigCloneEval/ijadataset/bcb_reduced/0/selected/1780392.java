package com.exult.android;

import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.io.RandomAccessFile;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.IOException;
import android.graphics.Point;
import android.graphics.Canvas;
import java.util.Calendar;
import com.exult.android.NewFileGump.SaveGameParty;

public class GameWindow extends GameSingletons {

    private static GameWindow instance;

    private EffectsManager effects;

    private Vector<GameMap> maps;

    private GameMap map;

    private GameRender render;

    private TimeQueue tqueue;

    private UsecodeMachine usecode;

    private Rectangle paintBox;

    private Rectangle clipBox;

    private Rectangle tempDirty;

    private Rectangle tempFind;

    private Point tempPoint = new Point();

    private Tile tempTile = new Tile(), tempTile2 = new Tile();

    private Tile SRTempTile = new Tile();

    private Tile cacheTile = new Tile();

    private ImageBuf win;

    private Palette pal;

    private PlasmaThread plasmaThread;

    private MainActor mainActor;

    private Actor cameraActor;

    private Vector<Actor> npcs;

    private Vector<Actor.DeadBody> bodies;

    private int numNpcs1;

    private BargeObject movingBarge;

    private int scrolltx, scrollty;

    private Rectangle scrollBounds;

    private boolean painted;

    private Rectangle dirty;

    int stepTileDelta = 8;

    private boolean combat;

    private int skipAboveActor;

    private int inDungeon;

    private boolean iceDungeon;

    private boolean ambientLight;

    private int specialLight;

    private int timeStopped;

    int theftWarnings;

    short theftCx, theftCy;

    public int skipLift;

    public static GameObject targetObj;

    public static GameObject onObj;

    public boolean paintEggs = true;

    public int blits;

    public boolean skipFirstScene;

    public boolean armageddon;

    public boolean wizardEye;

    public String busyMessage;

    public void setBusyMessage(String s) {
        busyMessage = s;
        setAllDirty();
        if (s != null) tqueue.pause(); else tqueue.resume();
    }

    public static GameWindow instanceOf() {
        return instance;
    }

    public GameWindow(int width, int height) {
        instance = this;
        maps = new Vector<GameMap>(1);
        map = new GameMap(0);
        render = new GameRender();
        tqueue = new TimeQueue();
        effects = new EffectsManager();
        usecode = new UsecodeMachine();
        maps.add(map);
        win = new ImageBuf(width, height);
        pal = new Palette(win);
        dirty = new Rectangle();
        scrollBounds = new Rectangle();
        paintBox = new Rectangle();
        clipBox = new Rectangle();
        tempDirty = new Rectangle();
        tempFind = new Rectangle();
        GameSingletons.init(this);
        skipLift = 16;
        skipAboveActor = 31;
        theftCx = theftCy = -1;
    }

    public GameMap getMap(int num) {
        GameMap newMap;
        if (num >= maps.size()) maps.setSize(num + 1);
        newMap = maps.elementAt(num);
        if (newMap == null) {
            newMap = new GameMap(num);
            maps.setElementAt(newMap, num);
            newMap.init();
        }
        return newMap;
    }

    public final GameMap getMap() {
        return map;
    }

    public void setMap(int num) {
        map = getMap(num);
        GameSingletons.gmap = map;
    }

    public final Palette getPal() {
        return pal;
    }

    public final EffectsManager getEffects() {
        return effects;
    }

    public final TimeQueue getTqueue() {
        return tqueue;
    }

    public final UsecodeMachine getUsecode() {
        return usecode;
    }

    public final boolean isMoving() {
        return movingBarge != null ? movingBarge.isMoving() : mainActor.isMoving();
    }

    public final boolean inCombat() {
        return combat;
    }

    public int getParty(Actor a_list[], boolean avatar_too) {
        int n = 0;
        if (avatar_too && mainActor != null) a_list[n++] = mainActor;
        int cnt = partyman.getCount();
        for (int i = 0; i < cnt; i++) {
            int party_member = partyman.getMember(i);
            Actor person = getNpc(party_member);
            if (person != null) a_list[n++] = person;
        }
        return n;
    }

    public final int isTimeStopped() {
        if (timeStopped == 0) return 0;
        if (timeStopped == -1) return 1500;
        int delay = timeStopped - TimeQueue.ticks;
        if (delay > 0) return delay;
        timeStopped = 0;
        return 0;
    }

    public final void setTimeStopped(int delay) {
        if (delay == -1) timeStopped = -1; else if (delay == 0) timeStopped = 0; else {
            int new_expire = TimeQueue.ticks + delay;
            if (new_expire > timeStopped) timeStopped = new_expire;
        }
    }

    public void toggleCombat() {
        combat = !combat;
        ExultActivity.setInCombat();
        int newsched = combat ? Schedule.combat : Schedule.follow_avatar;
        int cnt = partyman.getCount();
        for (int i = 0; i < cnt; i++) {
            int party_member = partyman.getMember(i);
            Actor person = getNpc(party_member);
            if (person == null) continue;
            int sched = person.getScheduleType();
            if (sched != newsched && sched != Schedule.wait && sched != Schedule.loiter) person.setScheduleType(newsched);
        }
        if (mainActor.getScheduleType() != newsched) mainActor.setScheduleType(newsched);
        if (combat) {
            mainActor.readyBestWeapon();
            setMovingBarge(null);
            Actor all[] = new Actor[9];
            cnt = getParty(all, true);
            for (int i = 0; i < cnt; i++) {
                Actor act = all[i];
                if (act.getAttackMode() == Actor.flee && !act.didUserSetAttack()) act.setAttackMode(Actor.nearest, false);
                GameObject targ = act.getTarget();
                if (targ != null && targ.getFlag(GameObject.in_party)) act.setTarget(null);
            }
        } else CombatSchedule.resume();
    }

    public final MainActor getMainActor() {
        return mainActor;
    }

    public final Actor getCameraActor() {
        return cameraActor;
    }

    public final boolean isMainActorInside() {
        return skipAboveActor < 31;
    }

    public final int getSkipAboveActor() {
        return skipAboveActor;
    }

    public final boolean setAboveMainActor(int lift) {
        if (skipAboveActor == lift) return false;
        skipAboveActor = lift;
        return true;
    }

    public boolean setInDungeon(int lift) {
        if (inDungeon == lift) return false;
        inDungeon = lift;
        return true;
    }

    public void setIceDungeon(boolean ice) {
        iceDungeon = ice;
    }

    public boolean isInIceDungeon() {
        return iceDungeon;
    }

    public int isInDungeon() {
        return inDungeon;
    }

    public boolean isSpecialLight() {
        return ambientLight || specialLight != 0;
    }

    void addSpecialLight(int units) {
        if (specialLight == 0) {
            specialLight = clock.getTotalMinutes();
            clock.setPalette();
        }
        specialLight += units / 20;
    }

    public void toggleAmbientLight(boolean state) {
        ambientLight = state;
    }

    public final void setMovingBarge(BargeObject b) {
        if (b != null && b != movingBarge) {
            b.gather();
            if (!b.contains(mainActor)) b.setToGather();
        } else if (b == null && movingBarge != null) movingBarge.done();
        movingBarge = b;
    }

    public final BargeObject getMovingBarge() {
        return movingBarge;
    }

    public final int getNumNpcs() {
        return npcs.size();
    }

    public final Actor getNpc(int n) {
        return n >= 0 && n < npcs.size() ? npcs.elementAt(n) : null;
    }

    public void setBody(int npcNum, Actor.DeadBody body) {
        if (bodies == null) bodies = new Vector<Actor.DeadBody>(npcNum + 1);
        if (npcNum >= bodies.size()) bodies.setSize(npcNum + 1);
        bodies.setElementAt(body, npcNum);
    }

    public Actor.DeadBody getBody(int npcNum) {
        return bodies != null && bodies.size() > npcNum ? bodies.elementAt(npcNum) : null;
    }

    public final int getRenderSkipLift() {
        return skipAboveActor < skipLift ? skipAboveActor : skipLift;
    }

    public final boolean mainActorDontMove() {
        return mainActor != null && (mainActor.getFlag(GameObject.dont_move) || mainActor.getFlag(GameObject.dont_render));
    }

    public final boolean mainActorCanAct() {
        return !wizardEye && mainActor != null && mainActor.canAct();
    }

    public final void scheduleNpcs(int hour, int backwards, boolean repaint) {
        Iterator<Actor> iter = npcs.iterator();
        iter.next();
        while (iter.hasNext()) {
            Actor npc = iter.next();
            if (npc != null && npc.getScheduleType() != Schedule.wait && (npc.getScheduleType() != Schedule.combat || npc.getTarget() == null)) npc.updateSchedule(hour / 3, backwards, hour % 3 == 0 ? -1 : 0);
        }
        if (repaint) paint();
    }

    public final void scheduleNpcs(int hour) {
        scheduleNpcs(hour, 7, true);
    }

    public void mendNpcs() {
        for (Actor npc : npcs) {
            if (npc != null) npc.mendHourly();
        }
    }

    private int getGuardShape(int tx, int ty) {
        if (!game.isSI()) return (0x3b2);
        if (tx >= 2054 && ty >= 1698 && tx < 2590 && ty < 2387) return 0x103;
        if (tx >= 895 && ty >= 1604 && tx < 1173 && ty < 1960) return 0x17d;
        if (tx >= 670 && ty >= 2430 && tx < 1135 && ty < 2800) return 0xe4;
        return -1;
    }

    private Actor findWitness(Actor closest_npc[]) {
        Vector<GameObject> npcs = new Vector<GameObject>();
        mainActor.findNearbyActors(npcs, EConst.c_any_shapenum, 12, 0x28);
        closest_npc[0] = null;
        int closest_dist = 5000;
        Actor witness = null;
        int closest_witness_dist = 5000;
        for (GameObject each : npcs) {
            Actor npc = (Actor) each;
            if (npc instanceof MonsterActor || npc.getFlag(GameObject.in_party) || (npc.getFrameNum() & 15) == Actor.sleep_frame || npc.getNpcNum() >= numNpcs1) continue;
            int dist = npc.distance(mainActor);
            if (dist >= closest_witness_dist || !PathFinder.FastClient.isGrabable(npc, mainActor)) continue;
            int dir = npc.getDirection(mainActor);
            int facing = npc.getDirFacing();
            int dirdiff = (dir - facing + 8) % 8;
            if (dirdiff < 3 || dirdiff > 5) {
                witness = npc;
                closest_witness_dist = dist;
            } else if (dist < closest_dist) {
                closest_npc[0] = npc;
                closest_dist = dist;
            }
        }
        return witness;
    }

    public void theft() {
        int cx = mainActor.getCx(), cy = mainActor.getCy();
        if (cx != theftCx || cy != theftCy) {
            theftCx = (short) cx;
            theftCy = (short) cy;
            theftWarnings = 0;
        }
        Actor closest_npc[] = new Actor[1];
        Actor witness = findWitness(closest_npc);
        if (witness == null) {
            if (closest_npc[0] != null && EUtil.rand() % 2 != 0) closest_npc[0].say(ItemNames.heard_something);
            return;
        }
        int dir = witness.getDirection(mainActor);
        witness.changeFrame(witness.getDirFramenum(dir, Actor.standing));
        theftWarnings++;
        if (theftWarnings < 2 + EUtil.rand() % 3) {
            witness.say(ItemNames.first_theft, ItemNames.last_theft);
            return;
        }
        gumpman.closeAllGumps(false);
        callGuards(witness);
    }

    public void callGuards(Actor witness) {
        if (armageddon || inDungeon > 0) return;
        if (witness != null || (witness = findWitness(new Actor[1])) != null) witness.say(ItemNames.first_call_guards, ItemNames.last_call_guards);
        int gshape = getGuardShape(mainActor.getTileX(), mainActor.getTileY());
        if (gshape < 0) {
            attackAvatar(0);
            return;
        }
        Tile actloc = new Tile(), dest = new Tile();
        mainActor.getTile(actloc);
        dest.set(actloc.tx + 128, actloc.ty + 128, actloc.tz);
        MonsterActor guard = MonsterActor.create(gshape, dest);
        if (!MapChunk.findSpot(actloc, 5, guard.getShapeNum(), guard.getFrameNum(), 1)) {
            int dir = EUtil.getDirection(dest.ty - actloc.ty, actloc.tx - dest.tx);
            byte frames[] = new byte[2];
            frames[0] = (byte) guard.getDirFramenum(dir, Actor.standing);
            frames[1] = (byte) guard.getDirFramenum(dir, 3);
            ActorAction action = new ActorAction.Sequence(new ActorAction.Frames(frames, 2), new ActorAction.Usecode(0x625, guard, UsecodeMachine.double_click));
            Schedule.setActionSequence(guard, dest, action, true, 0);
        }
    }

    void attackAvatar(int create_guards) {
        if (armageddon) return;
        int tx = mainActor.getTileX(), ty = mainActor.getTileY();
        Tile loc = new Tile(tx + 128, ty + 128, mainActor.getLift());
        int gshape = getGuardShape(tx, ty);
        if (gshape >= 0) {
            while (create_guards-- > 0) {
                MonsterActor guard = MonsterActor.create(gshape, loc);
                guard.setTarget(mainActor, true);
                guard.approachAnother(mainActor, false);
            }
        }
        Vector<GameObject> npcs = new Vector<GameObject>();
        mainActor.findNearbyActors(npcs, EConst.c_any_shapenum, 20, 0x28);
        for (GameObject each : npcs) {
            Actor npc = (Actor) each;
            if ((gshape < 0 || npc.getShapeNum() == gshape || !(npc instanceof MonsterActor)) && !npc.getFlag(GameObject.in_party)) npc.setTarget(mainActor, true);
        }
    }

    public final void getShapeLocation(Point loc, int tx, int ty, int tz) {
        int lft = 4 * tz;
        tx += 1 - scrolltx;
        ty += 1 - scrollty;
        if (tx < -EConst.c_num_tiles / 2) tx += EConst.c_num_tiles;
        if (ty < -EConst.c_num_tiles / 2) ty += EConst.c_num_tiles;
        loc.x = tx * EConst.c_tilesize - 1 - lft;
        loc.y = ty * EConst.c_tilesize - 1 - lft;
    }

    public final void getShapeLocation(Point loc, GameObject obj) {
        getShapeLocation(loc, obj.getTileX(), obj.getTileY(), obj.getLift());
    }

    public final Rectangle getShapeRect(Rectangle r, GameObject obj) {
        if (obj.getChunk() == null) {
            Gump gump = gumpman.findGump(obj);
            if (gump != null) gump.getShapeRect(r, obj); else r.set(0, 0, 0, 0);
            return r;
        }
        ShapeFrame s = obj.getShape();
        if (s == null) {
            r.set(0, 0, 0, 0);
            return r;
        }
        obj.getTile(SRTempTile);
        int lftpix = 4 * SRTempTile.tz;
        SRTempTile.tx += 1 - scrolltx;
        SRTempTile.ty += 1 - scrollty;
        if (SRTempTile.tx < -EConst.c_num_tiles / 2) SRTempTile.tx += EConst.c_num_tiles;
        if (SRTempTile.ty < -EConst.c_num_tiles / 2) SRTempTile.ty += EConst.c_num_tiles;
        return getShapeRect(r, s, SRTempTile.tx * EConst.c_tilesize - 1 - lftpix, SRTempTile.ty * EConst.c_tilesize - 1 - lftpix);
    }

    public final Rectangle getShapeRect(Rectangle r, ShapeFrame s, int x, int y) {
        r.set(x - s.getXLeft(), y - s.getYAbove(), s.getWidth(), s.getHeight());
        return r;
    }

    public ShapeID getFlat(ShapeID id, int x, int y) {
        int tx = (scrolltx + x / EConst.c_tilesize) % EConst.c_num_tiles;
        int ty = (scrollty + y / EConst.c_tilesize) % EConst.c_num_tiles;
        int cx = tx / EConst.c_tiles_per_chunk, cy = ty / EConst.c_tiles_per_chunk;
        tx = tx % EConst.c_tiles_per_chunk;
        ty = ty % EConst.c_tiles_per_chunk;
        MapChunk chunk = map.getChunk(cx, cy);
        if (id == null) id = new ShapeID();
        ChunkTerrain ter = chunk.getTerrain();
        if (ter != null) chunk.getTerrain().getFlat(id, tx, ty); else id.setShape(-1);
        return id;
    }

    public void getWinTileRect(Rectangle r) {
        r.set(getScrolltx(), getScrollty(), (win.getWidth() + EConst.c_tilesize - 1) / EConst.c_tilesize, (win.getHeight() + EConst.c_tilesize - 1) / EConst.c_tilesize);
    }

    public final int getScrolltx() {
        return scrolltx;
    }

    public final int getScrollty() {
        return scrollty;
    }

    public final void setScrolls(int newscrolltx, int newscrollty) {
        scrolltx = newscrolltx;
        scrollty = newscrollty;
        scrollBounds.w = scrollBounds.h = 2;
        scrollBounds.x = scrolltx + (getWidth() / EConst.c_tilesize - scrollBounds.w) / 2;
        scrollBounds.y = scrollty + ((getHeight()) / EConst.c_tilesize - scrollBounds.h) / 2;
        BargeObject oldActiveBarge = movingBarge;
        map.readMapData();
        if (oldActiveBarge == null && movingBarge != null) {
            BargeObject b = movingBarge;
            movingBarge = null;
            setMovingBarge(b);
        }
        int cx = cameraActor.getCx(), cy = cameraActor.getCy();
        MapChunk nlist = map.getChunk(cx, cy);
        int tx = cameraActor.getTx(), ty = cameraActor.getTy();
        setAboveMainActor(nlist.isRoof(tx, ty, cameraActor.getLift()));
        setInDungeon(nlist.hasDungeon() ? nlist.isDungeon(tx, ty) : 0);
        setIceDungeon(nlist.isIceDungeon(tx, ty));
    }

    public final void centerView(Tile t) {
        int zoff = t.tz / 2;
        int tw = getWidth() / EConst.c_tilesize, th = (getHeight()) / EConst.c_tilesize;
        setScrolls(EConst.DECR_TILE(t.tx, tw / 2 + zoff), EConst.DECR_TILE(t.ty, th / 2 + zoff));
        setAllDirty();
    }

    public boolean scrollIfNeeded(Tile t) {
        boolean scrolled = false;
        int tx = t.tx - t.tz / 2, ty = t.ty - t.tz / 2;
        if (Tile.gte(EConst.DECR_TILE(scrollBounds.x), tx)) {
            shiftViewHoriz(true);
            scrolled = true;
        } else if (Tile.gte(tx, (scrollBounds.x + scrollBounds.w) % EConst.c_num_tiles)) {
            shiftViewHoriz(false);
            scrolled = true;
        }
        if (Tile.gte(EConst.DECR_TILE(scrollBounds.y), ty)) {
            shiftViewVertical(true);
            scrolled = true;
        } else if (Tile.gte(ty, (scrollBounds.y + scrollBounds.h) % EConst.c_num_tiles)) {
            shiftViewVertical(false);
            scrolled = true;
        }
        return (scrolled);
    }

    public boolean scrollIfNeeded(Actor a, Tile t) {
        if (a != cameraActor) return false; else return scrollIfNeeded(t);
    }

    public void centerView(int tx, int ty, int tz) {
        int tw = win.getWidth() / EConst.c_tilesize, th = (win.getHeight()) / EConst.c_tilesize;
        setScrolls(EConst.DECR_TILE(tx, tw / 2 + tz / 2), EConst.DECR_TILE(ty, th / 2 + tz / 2));
        setAllDirty();
    }

    public void shiftViewHoriz(boolean toLeft) {
        shiftViewHoriz(toLeft, false);
    }

    public void shiftViewHoriz(boolean toleft, boolean nopaint) {
        int w = getWidth(), h = getHeight();
        if (toleft) {
            scrolltx = EConst.DECR_TILE(scrolltx);
            scrollBounds.x = EConst.DECR_TILE(scrollBounds.x);
        } else {
            scrolltx = EConst.INCR_TILE(scrolltx);
            scrollBounds.x = EConst.INCR_TILE(scrollBounds.x);
        }
        map.readMapData();
        if (nopaint || gumpman.showingGumps()) {
            setAllDirty();
            return;
        }
        synchronized (win) {
            mouse.hide();
            if (toleft) {
                win.copy(0, 0, w - EConst.c_tilesize, h, EConst.c_tilesize, 0);
                paint(0, 0, EConst.c_tilesize, h);
                dirty.x += EConst.c_tilesize;
            } else {
                win.copy(EConst.c_tilesize, 0, w - EConst.c_tilesize, h, 0, 0);
                paint(w - EConst.c_tilesize, 0, EConst.c_tilesize, h);
                dirty.x -= EConst.c_tilesize;
            }
        }
        clipToWin(dirty);
    }

    public void shiftViewVertical(boolean up) {
        shiftViewVertical(up, false);
    }

    public void shiftViewVertical(boolean up, boolean nopaint) {
        int w = getWidth(), h = getHeight();
        if (up) {
            scrollty = EConst.DECR_TILE(scrollty);
            scrollBounds.y = EConst.DECR_TILE(scrollBounds.y);
        } else {
            scrollty = EConst.INCR_TILE(scrollty);
            scrollBounds.y = EConst.INCR_TILE(scrollBounds.y);
        }
        map.readMapData();
        if (nopaint || gumpman.showingGumps()) {
            setAllDirty();
            return;
        }
        synchronized (win) {
            mouse.hide();
            if (up) {
                win.copy(0, 0, w, h - EConst.c_tilesize, 0, EConst.c_tilesize);
                paint(0, 0, w, EConst.c_tilesize);
                dirty.y += EConst.c_tilesize;
            } else {
                win.copy(0, EConst.c_tilesize, w, h - EConst.c_tilesize, 0, 0);
                paint(0, h - EConst.c_tilesize, w, EConst.c_tilesize);
                dirty.y -= EConst.c_tilesize;
            }
        }
        clipToWin(dirty);
    }

    public void shiftWizardEye(int mx, int my) {
        int cx = getWidth() / 2, cy = getHeight() / 2;
        int dy = cy - my, dx = mx - cx;
        int dir = EUtil.getDirection(dy, dx);
        final int deltas[] = { 0, -1, 1, -1, 1, 0, 1, 1, 0, 1, -1, 1, -1, 0, -1, -1 };
        int dirx = deltas[2 * dir], diry = deltas[2 * dir + 1];
        for (int i = 0; i < 4; ++i) {
            if (dirx == 1) shiftViewHoriz(false, true); else if (dirx == -1) shiftViewHoriz(true, true);
            if (diry == 1) shiftViewVertical(false, true); else if (diry == -1) shiftViewVertical(true, true);
        }
    }

    public void setCameraActor(Actor a) {
        if (a == mainActor && cameraActor != null && (cameraActor.getCx() != mainActor.getCx() || cameraActor.getCy() != mainActor.getCy())) cameraActor = a;
        setCenter();
    }

    public void setCenter() {
        centerView(cameraActor.getTileX(), cameraActor.getTileY(), cameraActor.getLift());
        setAllDirty();
    }

    public void startActorAlongPath(int winx, int winy, int speed) {
        if (mainActor.getFlag(GameObject.asleep) || mainActor.getFlag(GameObject.paralyzed) || mainActor.getScheduleType() == Schedule.sleep || movingBarge != null) return;
        int lift = mainActor.getLift();
        int liftpixels = 4 * lift;
        Tile dest = tempTile;
        dest.set(getScrolltx() + (winx + liftpixels) / EConst.c_tilesize, getScrollty() + (winy + liftpixels) / EConst.c_tilesize, lift);
        if (!mainActor.walkPathToTile(dest, speed, 0, 0)) System.out.println("Couldn't find path for Avatar."); else mainActor.getFollowers();
    }

    private void startActorSteps(int fromx, int fromy, int winx, int winy, int speed) {
        mainActor.getTile(tempTile);
        Tile start = tempTile;
        int dir;
        Tile dest = tempTile2;
        dir = EUtil.getDirection(fromy - winy, winx - fromx);
        int tflags = mainActor.getTypeFlags();
        start.getNeighbor(dest, dir);
        if (!mainActor.areaAvailable(dest, start, tflags)) {
            start.getNeighbor(dest, (dir + 1) % 8);
            if (mainActor.areaAvailable(dest, start, tflags)) dir = (dir + 1) % 8; else {
                start.getNeighbor(dest, (dir + 7) % 8);
                if (mainActor.areaAvailable(dest, start, tflags)) dir = (dir + 7) % 8; else dir = -1;
            }
        }
        if (dir == -1) {
            stopActor();
            return;
        }
        int delta = stepTileDelta * EConst.c_tilesize;
        switch(dir) {
            case EConst.north:
                fromy -= delta;
                break;
            case EConst.northeast:
                fromy -= delta;
                fromx += delta;
                break;
            case EConst.east:
                fromx += delta;
                break;
            case EConst.southeast:
                fromy += delta;
                fromx += delta;
                break;
            case EConst.south:
                fromy += delta;
                break;
            case EConst.southwest:
                fromy += delta;
                fromx -= delta;
                break;
            case EConst.west:
                fromx -= delta;
                break;
            case EConst.northwest:
                fromy -= delta;
                fromx -= delta;
                break;
        }
        int lift = mainActor.getLift();
        int liftpixels = 4 * lift;
        int tx = scrolltx + (fromx + liftpixels) / EConst.c_tilesize, ty = scrollty + (fromy + liftpixels) / EConst.c_tilesize;
        tx = (tx + EConst.c_num_tiles) % EConst.c_num_tiles;
        ty = (ty + EConst.c_num_tiles) % EConst.c_num_tiles;
        tempTile.set(tx, ty, lift);
        mainActor.walkToTile(tempTile, speed, 0);
        if (mainActor.getAction() != null) mainActor.getAction().setGetParty(true);
    }

    public void teleportParty(Tile t, boolean skipEggs, int newMap) {
        Tile oldpos = tempTile;
        mainActor.getTile(oldpos);
        mainActor.setAction(null);
        movingBarge = null;
        PartyManager party_man = GameSingletons.partyman;
        int i, cnt = party_man.getCount();
        if (newMap != -1) setMap(newMap);
        mainActor.move(t.tx, t.ty, t.tz, newMap);
        newMap = mainActor.getMapNum();
        centerView(t);
        clock.reset();
        clock.setPalette();
        Tile t1 = new Tile();
        for (i = 0; i < cnt; i++) {
            int party_member = party_man.getMember(i);
            Actor person = getNpc(party_member);
            if (person != null && !person.isDead() && person.getScheduleType() != Schedule.wait) {
                person.setAction(null);
                t1.set(t);
                if (MapChunk.findSpot(t1, 8, person.getShapeNum(), person.getFrameNum(), 1)) person.move(t1.tx, t1.ty, t1.tz, newMap);
            }
        }
        mainActor.getFollowers();
        if (!skipEggs) MapChunk.tryAllEggs(mainActor, t.tx, t.ty, t.tz, oldpos.tx, oldpos.ty);
    }

    public void startActor(int fromx, int fromy, int tox, int toy, int speed) {
        if (mainActor.getFlag(GameObject.asleep) || mainActor.getFlag(GameObject.paralyzed) || mainActor.inUsecodeControl() || mainActor.getScheduleType() == Schedule.sleep) return;
        if (gumpman.gumpMode()) return;
        if (movingBarge != null) {
            int lift = mainActor.getLift();
            int liftpixels = 4 * lift;
            int tx = getScrolltx() + (tox + liftpixels) / EConst.c_tilesize, ty = getScrollty() + (toy + liftpixels) / EConst.c_tilesize;
            tx = (tx + EConst.c_num_tiles) % EConst.c_num_tiles;
            ty = (ty + EConst.c_num_tiles) % EConst.c_num_tiles;
            Tile atile = movingBarge.getCenter();
            int bx = movingBarge.getTileX(), by = movingBarge.getTileY(), bz = movingBarge.getLift();
            tempTile.set(tx + bx - atile.tx, ty + by - atile.ty, bz);
            movingBarge.travelToTile(tempTile, 1);
        } else {
            int sched = mainActor.getScheduleType();
            if (sched != Schedule.follow_avatar && sched != Schedule.combat && !mainActor.getFlag(GameObject.asleep)) mainActor.setScheduleType(Schedule.follow_avatar);
            startActorSteps(fromx, fromy, tox, toy, speed);
        }
    }

    public final void stopActor() {
        if (movingBarge != null) movingBarge.stop(); else if (mainActor != null) {
            mainActor.stop();
            if (!gumpman.gumpMode()) mainActor.getFollowers();
        }
    }

    public void emulateCache(MapChunk olist, MapChunk nlist) {
        if (olist == null) return;
        effects.removeWeatherEffects(120);
        int newx = nlist.getCx(), newy = nlist.getCy(), oldx = olist.getCx(), oldy = olist.getCy();
        GameMap omap = olist.getMap(), nmap = nlist.getMap();
        cacheTile.set(newx * EConst.c_tiles_per_chunk, newy * EConst.c_tiles_per_chunk, 0);
        UsecodeScript.purge(cacheTile, 4 * EConst.c_tiles_per_chunk);
        int nearby[] = new int[25];
        int x, y;
        int old_minx = EConst.c_num_chunks + oldx - 2, old_maxx = EConst.c_num_chunks + oldx + 2;
        int old_miny = EConst.c_num_chunks + oldy - 2, old_maxy = EConst.c_num_chunks + oldy + 2;
        if (nmap == omap) {
            int new_minx = EConst.c_num_chunks + newx - 2, new_maxx = EConst.c_num_chunks + newx + 2;
            int new_miny = EConst.c_num_chunks + newy - 2, new_maxy = EConst.c_num_chunks + newy + 2;
            for (y = new_miny; y <= new_maxy; y++) {
                if (y > old_maxy) break;
                int dy = y - old_miny;
                if (dy < 0) continue;
                assert (dy < 5);
                for (x = new_minx; x <= new_maxx; x++) {
                    if (x > old_maxx) break;
                    int dx = x - old_minx;
                    if (dx >= 0) {
                        assert (dx < 5);
                        nearby[dy * 5 + dx] = 1;
                    }
                }
            }
        }
        Vector<GameObject> removes = new Vector<GameObject>(30);
        for (y = 0; y < 5; y++) {
            for (x = 0; x < 5; x++) {
                if (nearby[y * 5 + x] != 0) continue;
                MapChunk list = omap.getChunk((old_minx + x) % EConst.c_num_chunks, (old_miny + y) % EConst.c_num_chunks);
                if (list == null) continue;
                ObjectList.ObjectIterator it = new ObjectList.ObjectIterator(list.getObjects());
                GameObject each;
                while ((each = it.next()) != null) {
                    if (each.isEgg()) ((EggObject) each).reset(); else if (each.getFlag(GameObject.is_temporary)) removes.add(each);
                }
            }
        }
        for (GameObject obj : removes) {
            obj.deleteContents();
            obj.removeThis();
        }
        if (omap == nmap) omap.cacheOut(newx, newy); else omap.cacheOut(-1, -1);
    }

    public boolean emulateIsMoveAllowed(int tx, int ty) {
        int ax = cameraActor.getCx() / EConst.c_chunks_per_schunk;
        int ay = cameraActor.getCy() / EConst.c_chunks_per_schunk;
        tx /= EConst.c_tiles_per_schunk;
        ty /= EConst.c_tiles_per_schunk;
        int difx = ax - tx;
        int dify = ay - ty;
        if (difx < 0) difx = -difx;
        if (dify < 0) dify = -dify;
        if ((difx == 0 || difx == 1 || difx == EConst.c_num_schunks || difx == EConst.c_num_schunks - 1) && (dify == 0 || dify == 1 || dify == EConst.c_num_schunks || dify == EConst.c_num_schunks - 1)) return true;
        return false;
    }

    public GameObject findObject(int x, int y) {
        int not_above = getRenderSkipLift();
        int start_cx = ((scrolltx + x / EConst.c_tilesize) / EConst.c_tiles_per_chunk) % EConst.c_num_chunks;
        int start_cy = ((scrollty + y / EConst.c_tilesize) / EConst.c_tiles_per_chunk) % EConst.c_num_chunks;
        int stop_cx = (2 + (scrolltx + (x + 4 * not_above) / EConst.c_tilesize) / EConst.c_tiles_per_chunk) % EConst.c_num_chunks;
        int stop_cy = (2 + (scrollty + (y + 4 * not_above) / EConst.c_tilesize) / EConst.c_tiles_per_chunk) % EConst.c_num_chunks;
        GameObject best = null;
        boolean trans = true;
        for (int cy = start_cy; cy != stop_cy; cy = EConst.INCR_CHUNK(cy)) for (int cx = start_cx; cx != stop_cx; cx = EConst.INCR_CHUNK(cx)) {
            MapChunk olist = map.getChunk(cx, cy);
            if (olist == null) continue;
            ObjectList.ObjectIterator iter = olist.getObjects().getIterator();
            GameObject obj;
            while ((obj = iter.next()) != null) {
                if (obj.getLift() >= not_above) continue;
                getShapeRect(tempFind, obj);
                if (!tempFind.hasPoint(x, y) || !obj.isFindable()) continue;
                ShapeFrame s = obj.getShape();
                getShapeLocation(tempPoint, obj);
                if (!s.hasPoint(x - tempPoint.x, y - tempPoint.y)) continue;
                if (best == null || best.lt(obj) == 1 || trans) {
                    boolean ftrans = obj.getInfo().isTransparent();
                    if (!ftrans || trans) {
                        best = obj;
                        trans = ftrans;
                    }
                }
            }
        }
        return (best);
    }

    public final void showItems(int x, int y) {
        GameObject obj;
        Gump gump = gumpman.findGump(x, y);
        if (gump != null) {
            obj = gump.findObject(x, y);
        } else obj = findObject(x, y);
        if (obj != null) {
            System.out.printf("Found '%1$s'(%2$d:%3$d) at (%4$h, %5$h, %6$h)\n", obj.getName(), obj.getShapeNum(), obj.getFrameNum(), obj.getTileX(), obj.getTileY(), obj.getLift());
            if (obj instanceof Actor) System.out.printf("Npc #%1$d, sched=%2$d\n", ((Actor) obj).getNpcNum(), ((Actor) obj).getScheduleType());
            showObjName(obj);
        }
    }

    private String getObjectName(GameObject obj) {
        if (obj == mainActor) {
            if (game.isBG()) return ItemNames.misc[0x42]; else if (game.isSI()) return ItemNames.misc[0x4e];
        }
        return obj.getName();
    }

    void showObjName(GameObject obj) {
        String objName = getObjectName(obj);
        if (inCombat() && CombatSchedule.mode != CombatSchedule.original) {
            Actor npc = obj.asActor();
            if (npc != null) objName = String.format("%1$s (%1$d)", objName, npc.getProperty(Actor.health));
        }
        effects.addText(objName, obj);
    }

    public void doubleClicked(int x, int y) {
        if (mainActorDontMove()) return;
        GameObject obj = null;
        Gump gump = gumpman.findGump(x, y);
        boolean avatar_can_act = mainActorCanAct();
        if (gump != null) {
            obj = gumpman.doubleClicked(gump, x, y);
        } else {
            obj = findObject(x, y);
            if (obj != null && obj.asActor() == null && !cheat.inHackMover() && !PathFinder.FastClient.isGrabable(mainActor, obj)) {
                mouse.flashShape(Mouse.blocked);
                return;
            }
        }
        if (obj == null || !avatar_can_act) {
            return;
        }
        System.out.println("Double-clicked on shape " + obj.getShapeNum() + ":  " + obj.getName());
        if (combat && gump != null && !CombatSchedule.isPaused() && (!gumpman.gumpMode())) {
            Actor npc = obj.asActor();
            if ((npc == null || !npc.getFlag(GameObject.in_party)) && !obj.getInfo().isBodyShape()) {
                combat = false;
                mainActor.setTarget(obj);
                toggleCombat();
                return;
            }
        }
        effects.removeTextEffects();
        usecode.initConversation();
        obj.activate();
    }

    public ImageBuf getWin() {
        return win;
    }

    public final int getWidth() {
        return win.getWidth();
    }

    public final int getHeight() {
        return win.getHeight();
    }

    public final void setPainted() {
        painted = true;
    }

    public final boolean wasPainted() {
        return painted;
    }

    public boolean show(Canvas c, boolean force) {
        if (painted || force) {
            win.show(c);
            ++blits;
            painted = false;
            return true;
        }
        return false;
    }

    public void setAllDirty() {
        dirty.set(0, 0, win.getWidth(), win.getHeight());
    }

    public void clearDirty() {
        dirty.w = 0;
    }

    public boolean isDirty() {
        return dirty.w > 0 && plasmaThread == null;
    }

    public void addDirty(Rectangle r) {
        if (dirty.w > 0) dirty.add(r); else dirty.set(r);
    }

    public boolean addDirty(GameObject obj) {
        getShapeRect(tempDirty, obj);
        tempDirty.enlarge(1 + EConst.c_tilesize / 2);
        clipToWin(tempDirty);
        if (tempDirty.w > 0 && tempDirty.h > 0) {
            addDirty(tempDirty);
            return true;
        } else return false;
    }

    public void paint(int x, int y, int w, int h) {
        int gx = x, gy = y, gw = w, gh = h;
        if (gx < 0) {
            gw += x;
            gx = 0;
        }
        if ((gx + gw) > win.getWidth()) gw = win.getWidth() - gx;
        if (gy < 0) {
            gh += gy;
            gy = 0;
        }
        if ((gy + gh) > win.getHeight()) gh = win.getHeight() - gy;
        synchronized (win) {
            win.setClip(gx, gy, gw, gh);
            int light_sources = 0;
            if (mainActor != null) render.paintMap(gx, gy, gw, gh); else win.fill8((byte) 0, gw, gh, gx, gy);
            effects.paint();
            if (wizardEye) GameRender.paintWizardEye();
            gumpman.paint(false);
            if (drag != null) drag.paint();
            effects.paintText();
            gumpman.paint(true);
            Conversation conv = GameSingletons.conv;
            if (conv != null) conv.paint();
            paintBusy();
            if (gx == 0 && gy == 0 && gw == getWidth() && gh == getHeight() && mainActor != null) {
                int cnt = partyman.getCount();
                boolean carried_light = mainActor.hasLightSource();
                for (int i = 0; !carried_light && i < cnt; i++) {
                    Actor npc = npcs.elementAt(partyman.getMember(i));
                    if (npc != null) carried_light = npc.hasLightSource();
                }
                if (specialLight != 0 && clock.getTotalMinutes() > specialLight) {
                    specialLight = 0;
                    clock.setPalette();
                }
                clock.setLightSource((carried_light ? 1 : 0) + ((light_sources > 0) ? 1 : 0), inDungeon);
            }
            win.clearClip();
        }
    }

    public void paint(Rectangle r) {
        paint(r.x, r.y, r.w, r.h);
    }

    public void paintMapAtTile(int x, int y, int w, int h, int toptx, int topty, int skip_above) {
        synchronized (win) {
            int savescrolltx = scrolltx, savescrollty = scrollty;
            int saveskip = skipLift;
            scrolltx = toptx;
            scrollty = topty;
            skipLift = skip_above;
            map.readMapData();
            win.setClip(x, y, w, h);
            render.paintMap(0, 0, getWidth(), getHeight());
            win.clearClip();
            scrolltx = savescrolltx;
            scrollty = savescrollty;
            skipLift = saveskip;
        }
    }

    public void paintBusy() {
        if (busyMessage != null) {
            int text_height = fonts.getTextHeight(0);
            int text_width = fonts.getTextWidth(0, busyMessage);
            fonts.paintText(0, busyMessage, getWidth() / 2 - text_width / 2, getHeight() / 2 - text_height);
        }
    }

    public void clipToWin(Rectangle r) {
        clipBox.set(0, 0, win.getWidth(), win.getHeight());
        r.intersect(clipBox);
    }

    public void paintDirty() {
        if (!mainActorDontMove()) gumpman.updateGumps();
        effects.updateDirtyText();
        paintBox.set(dirty);
        clipToWin(paintBox);
        if (paintBox.w > 0 && paintBox.h > 0) paint(paintBox);
        clearDirty();
    }

    public void paint() {
        setAllDirty();
        paintDirty();
    }

    public void initActors() throws IOException {
        if (mainActor != null) {
            game.clearAvName();
            game.clearAvSex();
            game.clearAvSkin();
            return;
        }
        readNpcs();
        boolean changed = game.isNewGame();
        if (game.getAvSex() == 0 || game.getAvSex() == 1 || game.getAvName() != null || (game.getAvSkin() >= 0 && game.getAvSkin() <= 2)) changed = true;
        game.clearAvName();
        game.clearAvSex();
        game.clearAvSkin();
        if (changed) {
            scheduleNpcs(6, 7, false);
            writeNpcs();
        }
    }

    public void initFiles(boolean cycle) {
        if (cycle) startPlasma();
        ShapeID.loadStatic();
        tqueue.add(TimeQueue.ticks, clock, this);
    }

    public void setupGame() {
        System.out.println("setupGame: at start");
        if (EUtil.U7exists(EFile.IDENTITY) == null) initGamedat(true);
        getMap(0).init();
        try {
            initActors();
        } catch (IOException e) {
            System.out.println("FAILED to read NPCs: " + e.getMessage());
        }
        System.out.println("setupGame: finished initActors");
        usecode.read();
        if (game.isBG()) {
            if (skipFirstScene) usecode.setGlobalFlag(UsecodeMachine.did_first_scene, 1);
            if (usecode.getGlobalFlag(UsecodeMachine.did_first_scene)) mainActor.clearFlag(GameObject.bg_dont_render); else mainActor.setFlag(GameObject.bg_dont_render);
        }
        stopPlasma();
        mainActor.initReadied();
        int cnt = partyman.getCount();
        for (int i = 0; i < cnt; i++) {
            getNpc(partyman.getMember(i)).initReadied();
        }
        System.out.println("setupGame: about to activate eggs");
        MapChunk olist = mainActor.getChunk();
        int tx = mainActor.getTileX(), ty = mainActor.getTileY(), tz = mainActor.getLift();
        System.out.printf("setupGame: Avatar is at %1$d, %2$d, %3$d\n", tx, ty, tz);
        olist.activateEggs(mainActor, tx, ty, tz, -1, -1, true);
        setAllDirty();
        painted = true;
        gumpman.closeAllGumps(true);
        clock.reset();
        clock.setPalette();
        pal.fade(6, true, -1);
        System.out.println("setupGame: done");
    }

    public void readNpcs() throws IOException {
        npcs = new Vector<Actor>(1);
        cameraActor = mainActor = new MainActor("", 0);
        npcs.add(mainActor);
        InputStream nfile = EUtil.U7openStream(EFile.NPC_DAT);
        int numNpcs;
        numNpcs1 = EUtil.Read2(nfile);
        numNpcs = numNpcs1 + EUtil.Read2(nfile);
        mainActor.read(nfile, 0, false);
        npcs.setSize(numNpcs);
        if (bodies != null) bodies.setSize(numNpcs);
        int i;
        centerView(mainActor.getTileX(), mainActor.getTileY(), mainActor.getLift());
        for (i = 1; i < numNpcs; i++) {
            Actor actor = new NpcActor("", 0);
            npcs.set(i, actor);
            actor.read(nfile, i, i < numNpcs1);
            if (actor.isUnused()) {
                actor.removeThis();
                actor.setScheduleType(Schedule.wait);
            } else actor.restoreSchedule();
        }
        nfile.close();
        mainActor.setActorShape();
        String fname = EUtil.U7exists(EFile.MONSNPCS);
        if (fname != null) try {
            PushbackInputStream nfile2 = new PushbackInputStream(EUtil.U7openStream(fname));
            int cnt = EUtil.Read2(nfile2);
            while (cnt-- > 0) {
                nfile2.skip(2);
                int shnum = (int) EUtil.Read2(nfile2) & 0x3ff;
                nfile2.unread(4);
                if (ShapeFiles.SHAPES_VGA.getFile().getNumFrames(shnum) < 16) break;
                MonsterActor act = MonsterActor.create(shnum);
                act.read(nfile2, -1, false);
                act.restoreSchedule();
            }
            nfile2.close();
        } catch (IOException e) {
        }
        if (movingBarge != null) {
            BargeObject b = movingBarge;
            movingBarge = null;
            setMovingBarge(b);
        }
        readSchedules();
        centerView(mainActor.getTileX(), mainActor.getTileY(), mainActor.getLift());
    }

    private static int setToReadSchedules(InputStream sfile, Vector<Integer> offsets) {
        int num_script_names = -1;
        int num_npcs = EUtil.Read4(sfile);
        if (num_npcs == -1) {
            num_npcs = EUtil.Read4(sfile);
            num_script_names = 0;
        } else if (num_npcs == -2) {
            num_npcs = EUtil.Read4(sfile);
            num_script_names = EUtil.Read2(sfile);
        }
        offsets.setSize(num_npcs + 1);
        offsets.setElementAt(num_npcs, 0);
        for (int i = 0; i < num_npcs; i++) offsets.setElementAt(EUtil.Read2(sfile), i + 1);
        return num_script_names;
    }

    private void readASchedule(InputStream sfile, int index, Actor npc, int entsize, Vector<Integer> offsets, byte ent[]) throws IOException {
        int cnt = offsets.elementAt(index) - offsets.elementAt(index - 1);
        Schedule.ScheduleChange schedules[] = cnt > 0 ? new Schedule.ScheduleChange[cnt] : null;
        if (entsize == 4) {
            for (int j = 0; j < cnt; j++) {
                sfile.read(ent, 0, 4);
                schedules[j] = new Schedule.ScheduleChange();
                schedules[j].set4(ent);
            }
        } else {
            for (int j = 0; j < cnt; j++) {
                sfile.read(ent, 0, 8);
                schedules[j] = new Schedule.ScheduleChange();
                schedules[j].set8(ent);
            }
        }
        if (npc != null) npc.setSchedules(schedules);
    }

    private void readSchedules() throws IOException {
        InputStream sfile = EUtil.U7openStream2(EFile.GSCHEDULE, EFile.SCHEDULE_DAT);
        if (sfile == null) {
            ExultActivity.fileFatal(EFile.SCHEDULE_DAT);
            return;
        }
        int i, num_npcs, entsize;
        Vector<Integer> offsets = new Vector<Integer>();
        int num_script_names = setToReadSchedules(sfile, offsets);
        num_npcs = offsets.remove(0);
        entsize = num_script_names >= 0 ? 8 : 4;
        if (num_script_names > 0) {
            EUtil.Read2(sfile);
            for (i = 0; i < num_script_names; ++i) {
                int sz = EUtil.Read2(sfile);
                byte nm[] = new byte[sz];
                sfile.read(nm);
            }
        }
        byte ent[] = new byte[10];
        for (i = 0; i < num_npcs - 1; i++) {
            Actor npc = npcs.elementAt(i + 1);
            readASchedule(sfile, i + 1, npc, entsize, offsets, ent);
        }
        sfile.close();
    }

    boolean initGamedat(boolean create) {
        if (create) {
            System.out.println("Creating 'gamedat' files.");
            String fname = EFile.PATCH_INITGAME;
            try {
                if (EUtil.U7exists(fname) != null) restoreGamedat(fname); else {
                    game.setNewGame();
                    restoreGamedat(fname = EFile.INITGAME);
                }
            } catch (IOException e) {
                ExultActivity.fileFatal(fname);
            }
        } else if (EUtil.U7exists(EFile.IDENTITY) == null) {
            return false;
        } else {
            byte id[] = new byte[256];
            try {
                RandomAccessFile identity_file = EUtil.U7open(EFile.IDENTITY, false);
                int i, cnt = identity_file.read(id);
                identity_file.close();
                for (i = 0; i < cnt && id[i] != 0x1a && id[i] != 0x0d && id[i] != 0x0a; i++) ;
                System.out.println("Gamedat identity " + new String(id, 0, i));
            } catch (IOException e) {
            }
        }
        return true;
    }

    private void clearWorld() {
        CombatSchedule.resume();
        tqueue.clear();
        clearDirty();
        UsecodeScript.clear();
        int cnt = maps.size();
        for (int i = 0; i < cnt; ++i) maps.elementAt(i).clear();
        setMap(0);
        MonsterActor.deleteAll();
        mainActor = null;
        cameraActor = null;
        numNpcs1 = 0;
        theftCx = theftCy = -1;
        combat = false;
        npcs.setSize(0);
        if (bodies != null) bodies.setSize(0);
        movingBarge = null;
        specialLight = 0;
        ambientLight = false;
        effects.removeAllEffects();
    }

    static class RestoreThread extends Thread {

        private int num;

        public RestoreThread(int n) {
            num = n;
        }

        public void run() {
            try {
                gwin.startPlasma();
                gwin.restoreGamedat(num);
                gwin.read();
            } catch (IOException e) {
                ExultActivity.fatal(String.format("Failed restoring: %1$s", e.getMessage()));
            }
            gwin.setBusyMessage(null);
        }
    }

    public void startPlasma() {
        if (plasmaThread == null) {
            System.out.println("startPlasma: " + TimeQueue.ticks);
            plasmaThread = new PlasmaThread(pal);
            plasmaThread.start();
        }
        Thread.yield();
    }

    public void stopPlasma() {
        if (plasmaThread != null) {
            plasmaThread.finish = true;
            try {
                plasmaThread.join();
            } catch (InterruptedException e) {
            }
            plasmaThread = null;
            setAllDirty();
        }
    }

    public void read(int num) {
        setBusyMessage("Restoring Game");
        startPlasma();
        Thread t = new RestoreThread(num);
        t.start();
    }

    public void read() {
        audio.cancelStreams();
        startPlasma();
        clearWorld();
        readGwin();
        setupGame();
    }

    public void readGwin() {
        if (!clock.inQueue()) tqueue.add(TimeQueue.ticks, clock, this);
        InputStream gin = null;
        try {
            gin = EUtil.U7openStream(EFile.GWINDAT);
        } catch (IOException e) {
            return;
        }
        scrolltx = EUtil.Read2(gin);
        scrollty = EUtil.Read2(gin);
        clock.reset();
        clock.setDay(EUtil.Read2(gin));
        clock.setHour(EUtil.Read2(gin));
        clock.setMinute(EUtil.Read2(gin));
        specialLight = EUtil.Read4(gin);
        armageddon = false;
        try {
            gin.close();
        } catch (IOException e) {
        }
    }

    public void write() throws IOException {
        int mapcnt = maps.size();
        try {
            for (int i = 0; i < mapcnt; ++i) maps.elementAt(i).writeIreg();
            writeNpcs();
            usecode.write();
            writeGwin();
            writeSaveInfo();
        } catch (IOException e) {
            ExultActivity.fatal("Error saving: " + e.getMessage());
        }
    }

    static class SaveThread extends Observable implements Runnable {

        private int num;

        private String savename;

        public SaveThread(int n, String s, Observer client) {
            num = n;
            savename = s;
            if (client != null) addObserver(client);
        }

        public void start() {
            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            try {
                gwin.write();
                gwin.saveGamedat(num, savename);
                setChanged();
                notifyObservers();
            } catch (IOException e) {
                ExultActivity.fatal(String.format("Failed saving: %1$s", e.getMessage()));
            }
            System.out.println("Finished save");
            gwin.setBusyMessage(null);
        }
    }

    public void write(int num, String savename, Observer client) {
        setBusyMessage("Saving Game");
        SaveThread t = new SaveThread(num, savename, client);
        t.start();
    }

    public void write(int num, String savename) {
        write(num, savename, null);
    }

    private void writeGwin() throws IOException {
        OutputStream gout = EUtil.U7create(EFile.GWINDAT);
        EUtil.Write2(gout, getScrolltx());
        EUtil.Write2(gout, getScrollty());
        EUtil.Write2(gout, clock.getDay());
        EUtil.Write2(gout, clock.getHour());
        EUtil.Write2(gout, clock.getMinute());
        EUtil.Write4(gout, specialLight);
        {
            EUtil.Write4(gout, -1);
            EUtil.Write4(gout, 0);
        }
        gout.write(armageddon ? 1 : 0);
        gout.write(ambientLight ? 1 : 0);
        gout.flush();
    }

    private void writeNpcs() throws IOException {
        int num_npcs = npcs.size();
        OutputStream out = EUtil.U7create(EFile.NPC_DAT);
        EUtil.Write2(out, numNpcs1);
        EUtil.Write2(out, npcs.size() - numNpcs1);
        for (int i = 0; i < num_npcs; i++) npcs.elementAt(i).write(out);
        out.close();
        writeSchedules();
        out = EUtil.U7create(EFile.MONSNPCS);
        int cnt = 0;
        HashSet<MonsterActor> monsters = MonsterActor.getAll();
        for (MonsterActor mact : monsters) {
            if (!mact.isDead()) cnt++;
        }
        EUtil.Write2(out, cnt);
        for (MonsterActor mact : monsters) {
            if (!mact.isDead()) mact.write(out);
        }
        out.close();
    }

    private void writeSchedules() throws IOException {
        Schedule.ScheduleChange schedules[];
        int cnt;
        short offset = 0;
        int i;
        int num;
        num = npcs.size();
        OutputStream sfile = EUtil.U7create(EFile.GSCHEDULE);
        EUtil.Write4(sfile, -2);
        EUtil.Write4(sfile, num);
        EUtil.Write2(sfile, 0);
        EUtil.Write2(sfile, 0);
        for (i = 1; i < num; i++) {
            schedules = npcs.elementAt(i).getSchedules();
            cnt = schedules == null ? 0 : schedules.length;
            offset += cnt;
            EUtil.Write2(sfile, offset);
        }
        byte ent[] = new byte[20];
        for (i = 1; i < num; i++) {
            schedules = npcs.elementAt(i).getSchedules();
            cnt = schedules == null ? 0 : schedules.length;
            for (int j = 0; j < cnt; j++) {
                schedules[j].write8(ent);
                sfile.write(ent, 0, 8);
            }
        }
        sfile.close();
    }

    void restoreGamedat(int num) throws IOException {
        String nm = String.format(EFile.SAVENAME, num, game.isBG() ? "bg" : "si");
        restoreGamedat(nm);
    }

    void restoreGamedat(String fname) throws IOException {
        System.out.println("restoreGamedat:  " + fname);
        startPlasma();
        EUtil.U7mkdir("<GAMEDAT>");
        if (!EUtil.isFlex(fname)) {
            restoreGamedatZip(fname);
            return;
        }
        RandomAccessFile in = EUtil.U7open(fname, true);
        if (in == null) ExultActivity.fatal("Can't open file: " + EUtil.getSystemPath(fname));
        removeBeforeRestore();
        try {
            restoreFlexFiles(in, EFile.GAMEDAT);
        } catch (IOException e) {
            ExultActivity.fatal("Error restoring from: " + EUtil.getSystemPath(fname));
            return;
        }
        in.close();
        stopPlasma();
    }

    void restoreFlexFiles(RandomAccessFile in, String basepath) throws IOException {
        in.seek(0x54);
        int numfiles = EUtil.Read4(in);
        System.out.println("RestoreFlexFiles: cnt = " + numfiles);
        in.seek(0x80);
        int finfo[] = new int[2 * numfiles];
        int i;
        for (i = 0; i < numfiles; i++) {
            finfo[2 * i] = EUtil.Read4(in);
            finfo[2 * i + 1] = EUtil.Read4(in);
        }
        byte nm13[] = new byte[13];
        for (i = 0; i < numfiles; i++) {
            int len = finfo[2 * i + 1] - 13, pos = finfo[2 * i];
            if (len <= 0) continue;
            in.seek(pos);
            in.read(nm13);
            int nlen;
            for (nlen = 0; nlen < nm13.length && nm13[nlen] != 0; ++nlen) ;
            if (nm13[nlen] == '.') nlen--;
            String fname = basepath + new String(nm13, 0, nlen);
            byte buf[] = new byte[len];
            in.read(buf);
            try {
                OutputStream out = EUtil.U7create(fname);
                out.write(buf);
                out.close();
            } catch (IOException e) {
                ExultActivity.fatal(String.format("Error writing '%1$s'.", EUtil.getSystemPath(fname)));
                return;
            }
        }
    }

    private boolean restoreGamedatZip(String fname) {
        System.out.println("restoreGamedatZip: " + fname);
        InputStream in;
        ZipInputStream zin;
        try {
            in = EUtil.U7openStream(fname);
            in.skip(saveNameSize);
            zin = new ZipInputStream(in);
            String nm = EUtil.getSystemPath(fname);
            System.out.println("restoreGamedatZip: opening " + nm);
        } catch (IOException e) {
            System.out.println("Zip exception: " + e.getMessage());
            ExultActivity.fileFatal(fname);
            return false;
        }
        removeBeforeRestore();
        ZipEntry ze = null;
        byte buf[] = null;
        System.out.println("About to read zip entries");
        try {
            while ((ze = zin.getNextEntry()) != null) {
                String fnm = EFile.GAMEDAT + ze.getName();
                int len = (int) ze.getSize();
                if (len == -1) len = 0x1000;
                if (buf == null || buf.length < len) buf = new byte[len];
                OutputStream out = EUtil.U7create(fnm);
                int rcnt;
                while ((rcnt = zin.read(buf, 0, len)) > 0) {
                    out.write(buf, 0, rcnt);
                }
                out.close();
                zin.closeEntry();
            }
            zin.close();
            System.out.println("restoreGamedatZip completed");
        } catch (IOException e) {
            String err = String.format("Error restoring '%1$s': %2$s", EUtil.getSystemPath(fname), e.getMessage());
            System.out.println(err);
            ExultActivity.fatal(err);
            return false;
        }
        return true;
    }

    private void removeBeforeRestore() {
        EUtil.U7remove(EFile.USEDAT);
        EUtil.U7remove(EFile.USEVARS);
        EUtil.U7remove(EFile.U7NBUF_DAT);
        EUtil.U7remove(EFile.NPC_DAT);
        EUtil.U7remove(EFile.MONSNPCS);
        EUtil.U7remove(EFile.FLAGINIT);
        EUtil.U7remove(EFile.GWINDAT);
        EUtil.U7remove(EFile.IDENTITY);
        EUtil.U7remove(EFile.GSCHEDULE);
        EUtil.U7remove("<STATIC>/flags.flg");
        EUtil.U7remove(EFile.GSCRNSHOT);
        EUtil.U7remove(EFile.GSAVEINFO);
        EUtil.U7remove(EFile.GNEWGAMEVER);
        EUtil.U7remove(EFile.GEXULTVER);
        EUtil.U7remove(EFile.KEYRINGDAT);
        EUtil.U7remove(EFile.NOTEBOOKXML);
    }

    private static final String bgsavefiles[] = { EFile.GSCRNSHOT, EFile.GSAVEINFO, EFile.IDENTITY, EFile.GEXULTVER, EFile.GNEWGAMEVER, EFile.NPC_DAT, EFile.MONSNPCS, EFile.USEVARS, EFile.USEDAT, EFile.FLAGINIT, EFile.GWINDAT, EFile.GSCHEDULE };

    private static final int bgnumsavefiles = bgsavefiles.length;

    private static final String sisavefiles[] = { EFile.GSCRNSHOT, EFile.GSAVEINFO, EFile.IDENTITY, EFile.GEXULTVER, EFile.GNEWGAMEVER, EFile.NPC_DAT, EFile.MONSNPCS, EFile.USEVARS, EFile.USEDAT, EFile.FLAGINIT, EFile.GWINDAT, EFile.GSCHEDULE, EFile.KEYRINGDAT, EFile.NOTEBOOKXML };

    private static final int sinumsavefiles = sisavefiles.length;

    private static final int saveNameSize = 0x50;

    public void saveGamedat(int num, String savename) throws IOException {
        String nm = String.format(EFile.SAVENAME, num, game.isBG() ? "bg" : "si");
        saveGamedat(nm, savename);
    }

    private boolean saveOneZip(ZipOutputStream zout, String fname, byte buf[]) throws IOException {
        int sz;
        InputStream in;
        try {
            in = EUtil.U7openStream(fname);
            sz = in.available();
            if (buf == null || buf.length < sz) buf = new byte[sz];
            in.read(buf, 0, sz);
        } catch (IOException e) {
            return true;
        }
        ZipEntry entry = new ZipEntry(EUtil.baseName(fname));
        zout.putNextEntry(entry);
        zout.write(buf, 0, sz);
        in.close();
        return true;
    }

    private void saveGamedat(String fname, String savename) throws IOException {
        int numsavefiles = game.isBG() ? bgnumsavefiles : sinumsavefiles;
        String savefiles[] = game.isBG() ? bgsavefiles : sisavefiles;
        OutputStream out = EUtil.U7create(fname);
        byte namebytes[] = savename.getBytes();
        int namelen = Math.min(namebytes.length, saveNameSize);
        byte namebuf[] = new byte[saveNameSize];
        System.arraycopy(namebytes, 0, namebuf, 0, namelen);
        out.write(namebuf);
        ZipOutputStream zout = new ZipOutputStream(out);
        System.out.println("Saving to " + fname);
        byte buf[] = null;
        for (int i = 0; i < numsavefiles; ++i) {
            if (!saveOneZip(zout, savefiles[i], buf)) return;
        }
        int mapcnt = maps.size();
        for (int j = 0; j < mapcnt; ++j) {
            GameMap map = maps.elementAt(j);
            if (map != null) {
                for (int schunk = 0; schunk < 12 * 12; schunk++) {
                    String iname = map.getSchunkFileName(EFile.U7IREG, schunk);
                    if (EUtil.U7exists(iname) != null) {
                        if (!saveOneZip(zout, iname, buf)) return;
                    }
                }
            }
        }
        zout.close();
    }

    public boolean getSaveInfo(int num, NewFileGump.SaveInfo info) {
        String fname = String.format(EFile.SAVENAME, num, game.isBG() ? "bg" : "si");
        if (!EUtil.isFlex(fname)) {
            return getSaveInfoZip(fname, info);
        }
        return false;
    }

    public boolean getSaveInfoZip(String fname, NewFileGump.SaveInfo info) {
        InputStream in;
        ZipInputStream zin;
        ZipEntry ze = null;
        try {
            in = EUtil.U7openStream(fname);
            byte namebuf[] = new byte[saveNameSize];
            in.read(namebuf);
            int i;
            for (i = 0; i < saveNameSize; ++i) if (namebuf[i] == 0) break;
            info.savename = new String(namebuf, 0, i);
            zin = new ZipInputStream(in);
            System.out.println("getSaveInfoZip: name is " + info.savename);
            String screenshotName = EUtil.baseName(EFile.GSCRNSHOT);
            String saveinfoName = EUtil.baseName(EFile.GSAVEINFO);
            int found = 0;
            while (found < 2 && (ze = zin.getNextEntry()) != null) {
                String fnm = ze.getName();
                if (fnm.equals(screenshotName)) {
                    ++found;
                    int ind = 0, rcnt, sz = EUtil.Read4(zin);
                    byte buf[] = new byte[sz];
                    EUtil.Write4(buf, 0, sz);
                    ind += 4;
                    while (ind < sz && (rcnt = zin.read(buf, ind, sz - ind)) > 0) ind += rcnt;
                    info.screenshot = new VgaFile.ShapeFile(buf);
                } else if (fnm.equals(saveinfoName)) {
                    ++found;
                    info.readSaveInfo(zin);
                }
                zin.closeEntry();
            }
            zin.close();
        } catch (IOException e) {
            System.out.println("Zip exception: " + e.getMessage());
            ExultActivity.fileFatal(fname);
            return false;
        }
        return true;
    }

    private void writeSaveInfo() throws IOException {
        int save_count = 1;
        try {
            InputStream in = EUtil.U7openStream(EFile.GSAVEINFO);
            in.skip(10);
            save_count += EUtil.Read2(in);
            in.close();
        } catch (IOException e) {
        }
        int party_size = partyman.getCount() + 1;
        Calendar timeinfo = Calendar.getInstance();
        OutputStream out = EUtil.U7create(EFile.GSAVEINFO);
        out.write(timeinfo.get(Calendar.MINUTE));
        out.write(timeinfo.get(Calendar.HOUR));
        out.write(timeinfo.get(Calendar.DAY_OF_MONTH));
        out.write(timeinfo.get(Calendar.MONTH) + 1);
        EUtil.Write2(out, timeinfo.get(Calendar.YEAR));
        out.write(clock.getMinute());
        out.write(clock.getHour());
        EUtil.Write2(out, clock.getDay());
        EUtil.Write2(out, save_count);
        out.write(party_size);
        out.write(0);
        out.write(timeinfo.get(Calendar.SECOND));
        for (int j = NewFileGump.SaveGameDetails.skip; j > 0; --j) out.write(0);
        for (int i = 0; i < party_size; i++) {
            Actor npc;
            if (i == 0) npc = mainActor; else npc = getNpc(partyman.getMember(i - 1));
            byte namestr[] = npc.getNpcName().getBytes();
            int namelen = Math.min(namestr.length, 18);
            out.write(namestr, 0, namelen);
            for (; namelen < 18; ++namelen) out.write(0);
            EUtil.Write2(out, npc.getShapeNum());
            EUtil.Write4(out, npc.getProperty(Actor.exp));
            EUtil.Write4(out, npc.getFlags());
            EUtil.Write4(out, npc.getFlags2());
            out.write(npc.getProperty(Actor.food_level));
            out.write(npc.getProperty(Actor.strength));
            out.write(npc.getProperty(Actor.combat));
            out.write(npc.getProperty(Actor.dexterity));
            out.write(npc.getProperty(Actor.intelligence));
            out.write(npc.getProperty(Actor.magic));
            out.write(npc.getProperty(Actor.mana));
            out.write(npc.getProperty(Actor.training));
            EUtil.Write2(out, npc.getProperty(Actor.health));
            EUtil.Write2(out, 0);
            for (int j = SaveGameParty.skip; j > 0; --j) out.write(0);
        }
        out.close();
        VgaFile.ShapeFile map = createMiniScreenshot(true);
        out = EUtil.U7create(EFile.GSCRNSHOT);
        map.save(out);
        out.close();
        if (EUtil.U7exists(EFile.GNEWGAMEVER) == null) {
            out = EUtil.U7create(EFile.GNEWGAMEVER);
            String unk = new String("Unknown\n");
            out.write(unk.getBytes());
            out.close();
        }
    }

    public VgaFile.ShapeFile createMiniScreenshot(boolean fast) {
        VgaFile.ShapeFile sh = null;
        ShapeFrame fr = null;
        byte img[] = null;
        synchronized (win) {
            setAllDirty();
            render.paintMap(0, 0, getWidth(), getHeight());
            img = win.miniScreenshot(fast);
            if (img != null) {
                fr = new ShapeFrame();
                fr.createRle(img, 0, 0, 96, 60);
                sh = new VgaFile.ShapeFile(fr);
            }
            setAllDirty();
            paint();
        }
        return sh;
    }
}
