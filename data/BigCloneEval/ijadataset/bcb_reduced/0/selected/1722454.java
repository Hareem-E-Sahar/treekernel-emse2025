package org.moyoman.module.loosegroups.simplelgimpl;

import org.moyoman.debug.*;
import org.moyoman.framework.Scheduler;
import org.moyoman.log.*;
import org.moyoman.module.*;
import org.moyoman.module.board.*;
import org.moyoman.module.groups.*;
import org.moyoman.module.loosegroups.*;
import org.moyoman.util.*;
import org.moyoman.util.closestpoint.*;
import java.util.*;

/** This class is responsible for 
  * assigning stones to a loose group.  Each stone on the 
  * board is a member of exactly one loose group.  The total
  * number of loose groups is less than or equal to the number
  * of groups.  Any two stones that are in the same group are
  * also in the same loose group.
  */
public class LGImpl extends Module implements LooseGroups {

    /** The debug types that this module produces.*/
    private static DebugType[] dt;

    /** The module types that this module is dependent on.*/
    private ModuleType[] mt;

    /** The number of the next move.*/
    private short moveNumber;

    /** The color of the player to move.*/
    private Color moveColor;

    /** Each element is a SingleLooseGroup object.*/
    private ArrayList slggroups;

    /** The ClosestPoints class is used to help determine which groups are in the same loose group.*/
    private ClosestPoints cp;

    /** The Board object with the current state of the board.*/
    private Board board;

    /** The RatedMove objects created by generateMove().*/
    private ArrayList ratedMoves;

    /** One of the LooseGroups constants ADD, UPDATE, or COMBINE.*/
    private short moveStatus;

    /** If any loose groups were split into two or more loose groups on the last move, this is true.*/
    private boolean groupsSplit;

    /** This is true if at least one loose group was entirely captured on the last move.*/
    private boolean groupsCaptured;

    /** If status() returns ADD, then this is the new group.*/
    private SingleLGImpl newGroup;

    /** If status() returns UPDATE then this is the updated group.*/
    private SingleLGImpl updatedGroup;

    /** If groupsSplit is true, then this contains SingleLooseGroup objects after splitting.*/
    private ArrayList splitGroups;

    /** Contains SingleLooseGroup objects which were combined on the current move into one.*/
    private ArrayList combinedGroups;

    /** Contains SingleLooseGroups which had some stones captured from them by the last move.*/
    private ArrayList partiallyCapturedGroups;

    /** Contains SingleLooseGroups which were entirely captured by the last move.*/
    private ArrayList fullyCapturedGroups;

    /** The key is a Long returned by Zobrist, and the value is a Point array.*/
    private static HashMap edgePoints;

    static {
        dt = new DebugType[2];
        dt[0] = new DebugType(DebugType.AGGREGATE);
        edgePoints = new HashMap();
    }

    /** Create the LGImpl object.
	  * @param id The game id.
	  * @param name The ModuleName of this module.
	  * @throws InternalErrorException Thrown if the create fails for any reason.
	  */
    public LGImpl(GameId id, ModuleName name) throws InternalErrorException {
        super(id, name);
        moveNumber = 1;
        moveColor = Color.BLACK;
        slggroups = new ArrayList();
        cp = new ClosestPoints();
        ratedMoves = new ArrayList();
        moveStatus = ADD;
        groupsSplit = false;
        groupsCaptured = false;
        splitGroups = new ArrayList();
        combinedGroups = new ArrayList();
        partiallyCapturedGroups = new ArrayList();
        fullyCapturedGroups = new ArrayList();
        mt = new ModuleType[2];
        try {
            mt[0] = ModuleType.getModuleType("Board");
            mt[1] = ModuleType.getModuleType("Groups");
        } catch (Exception e) {
            SystemLog.error(e);
            mt = new ModuleType[0];
            throw new InternalErrorException(e);
        }
    }

    /** Return the status of the last move made.
	  * The status is one of three constants
	  * indicating that the last move either:
	  * <OL>
	  * <LI>Caused a new loose group to be created.
	  * <LI>Updated an existing loose group
	  * <LI>Combined two or more existing loose groups.
	  * </OL>
	  * <p>
	  * The status is independent of whether any stones were 
	  * captured on the last move.
	  * @return The status.
	  */
    public short status() {
        return moveStatus;
    }

    /** Determine if a loose group was split on the last move.
	  * @return true if a loose group was split, or false.
	  */
    public boolean wasLooseGroupSplit() {
        return groupsSplit;
    }

    /** Determine if a loose group was completely captured on the last move.
	  * @return true if a loose group was completely captured, or false.
	  */
    public boolean wereGroupsCaptured() {
        return groupsCaptured;
    }

    /** Get the loose groups that were split by the last move.
	  * @return An array of SingleLooseGroup objects that were one loose group prior to the last move.
	  */
    public SingleLooseGroup[] getSplitLooseGroups() {
        SingleLooseGroup[] split = new SingleLooseGroup[splitGroups.size()];
        splitGroups.toArray(split);
        return split;
    }

    /** This method returns all of the loose groups.
	  * @return An array of SingleLooseGroup objects.
	  */
    public SingleLooseGroup[] getSingleLooseGroups() {
        SingleLooseGroup[] slg = new SingleLooseGroup[slggroups.size()];
        slggroups.toArray(slg);
        return slg;
    }

    /** Get the loose group which contains the single group.
	  * @param sg The group in question.
	  * @return The SingleLooseGroup object which contains the single group.
	  * @throws NoSuchDataException Thrown if the loose group cannot be found.
	  */
    public SingleLooseGroup getSingleLooseGroup(SingleGroup sg) throws NoSuchDataException {
        return getSingleLooseGroup(sg.getAnyStone());
    }

    /** Get the loose group which contains the stone.
	  * @param stone The stone in question.
	  * @return The SingleLooseGroup object which contains the stone.
	  * @throws NoSuchDataException Thrown if the loose group cannot be found.
	  */
    public SingleLooseGroup getSingleLooseGroup(Stone stone) throws NoSuchDataException {
        for (int i = 0; i < slggroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) slggroups.get(i);
            if (slgi.isInSingleLooseGroup(stone)) return slgi;
        }
        String s = "Could not find loose group that contains stone - " + stone;
        SystemLog.error(s);
        throw new NoSuchDataException(s);
    }

    /** Return loose groups that were partially captured on the last move.
	  * The groups are as they were before the capture, containing some
	  * SingleGroup objects that were captured, and some that remain
	  * on the board.  Use the Groups.getCapturedSingleGroups() method
	  * to determine which stones are still in this loose group.
	  * @return An array of SingleLooseGroup objects.
	  */
    public SingleLooseGroup[] getSingleLooseGroupsPartiallyCaptured() {
        SingleLooseGroup[] arr = new SingleLooseGroup[partiallyCapturedGroups.size()];
        partiallyCapturedGroups.toArray(arr);
        return arr;
    }

    /** Return loose groups that were completely captured on the last move.
	  * The set of SingleLooseGroup objects returned by this method
	  * and getSingleLooseGroupsPartiallyCaptured() are disjoint.
	  * @return An array of SingleLooseGroup objects.
	  */
    public SingleLooseGroup[] getSingleLooseGroupsFullyCaptured() {
        SingleLooseGroup[] arr = new SingleLooseGroup[fullyCapturedGroups.size()];
        fullyCapturedGroups.toArray(arr);
        return arr;
    }

    /** Get the new loose group that contains the last move made.
	  * @return a SingleLooseGroup object.
	  * @throws NoSuchDataException Thrown if the last move did not
	  * case a new loose group to be created.
	  */
    public SingleLooseGroup getNewSingleLooseGroup() throws NoSuchDataException {
        if (status() == ADD) return newGroup; else {
            String s = "Call to LGImpl.getNewSingleLooseGroup() when status was not ADD";
            SystemLog.error(s);
            throw new NoSuchDataException(s);
        }
    }

    /** Get the loose group that contains the last move made.
	  * @return A SingleLooseGroup object.
	  * @throws NoSuchDataException Thrown if the last move did not
	  * add a stone to an existing loose group.
	  */
    public SingleLooseGroup getUpdatedSingleLooseGroup() throws NoSuchDataException {
        if (status() == UPDATE) return updatedGroup; else {
            String s = "Call to LGImpl.getUpdatedSingleLooseGroup() when status was not UPDATE";
            SystemLog.error(s);
            throw new NoSuchDataException(s);
        }
    }

    /** Return the loose groups that were combined into a single loose group.
	  * @return An array of SingleLooseGroup objects.
	  * @throws NoSuchDataException Thrown if the last move did not
	  * cause two or more existing loose groups to be combined.
	  */
    public SingleLooseGroup[] getCombinedSingleLooseGroups() throws NoSuchDataException {
        if (status() == COMBINE) {
            SingleLooseGroup[] combined = new SingleLooseGroup[combinedGroups.size()];
            combinedGroups.toArray(combined);
            return combined;
        } else {
            String s = "Call to LGImpl.getCombinedSingleLooseGroups() when status was not COMBINE";
            SystemLog.error(s);
            throw new NoSuchDataException(s);
        }
    }

    /** Return the loose groups that were removed because of suicide.
	  * @return An array of SingleLooseGroup objects.
	  * @throws NoSuchDataException Thrown if the last move did not
	  * result in suicide.
	  */
    public SingleLooseGroup[] getSuicidedSingleLooseGroups() throws NoSuchDataException {
        if (status() != SUICIDE) {
            throw new NoSuchDataException("Cant call getSuicidedSingleLooseGroups() if not suicide.");
        }
        return new SingleLooseGroup[0];
    }

    /** Remove the group from the list of loose groups.
	  * @param slg The loose group to be removed.
	  */
    private void removeGroup(SingleLGImpl slg) {
        if (!slggroups.remove(slg)) error("Could not remove group " + slg.getAnyStone());
    }

    /** Generate moves for the loose group module.
	  * Currently, this module does not recommend any moves.
	  * The makeMove() method is creating the Debug objects,
	  * so this method does not need to.
	  * @param modules - The modules which this module needs in order
	  * to make its move.
	  */
    public void generateMove(Module[] modules) {
    }

    /** Get the debug information for this module.
	  * @param types The debug types that the caller can handle.
	  * @return An array of Debug objects.
	  */
    public Debug[] getDebugInformation(DebugType[] types) {
        ArrayList al = new ArrayList();
        for (int i = 0; i < types.length; i++) {
            if (types[i].get() == DebugType.AGGREGATE) {
                try {
                    Aggregate agg = new Aggregate("DEBUG_AGGREGATE_LOOSE_GROUPS");
                    for (int j = 0; j < slggroups.size(); j++) {
                        SingleLGImpl slgi = (SingleLGImpl) slggroups.get(j);
                        SingleGroup[] sg = slgi.getSingleGroups();
                        Stone[] starr = slgi.getStones();
                        int gr = agg.addGroup(starr);
                        for (int k = 0; k < sg.length; k++) {
                            Stone[] sgstones = sg[k].getStones();
                            agg.setSubgroup(gr, sgstones);
                        }
                    }
                    al.add(agg);
                } catch (Exception e) {
                    error(e);
                    Debug[] debug = new Debug[0];
                    return debug;
                }
            }
        }
        Debug[] di = new Debug[al.size()];
        al.toArray(di);
        return di;
    }

    /** Get the debug types that this module produces.
	  * @return An array of DebugType objects.
	  */
    public DebugType[] getDebugTypes() {
        return dt;
    }

    /** Get the rated moves that this module creates.
	  * Currently, this module does not rate moves,
	  * so the array size will always be zero.
	  * @return An array of RatedMove objects.
	  */
    public RatedMove[] getMoves() {
        RatedMove[] rm = new RatedMove[ratedMoves.size()];
        ratedMoves.toArray(rm);
        return rm;
    }

    /** Get the module types that this module requires.
	  * @return An array of ModuleType objects.
	  */
    public ModuleType[] getRequiredModuleList() {
        return mt;
    }

    /** Update the internal data structures with the last move.
	  * @param move The move that was made.
	  * @param modules Modules that this module uses.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public void makeMove(Move move, Module[] modules) throws InternalErrorException {
        ratedMoves.clear();
        groupsSplit = false;
        groupsCaptured = false;
        splitGroups.clear();
        combinedGroups.clear();
        partiallyCapturedGroups.clear();
        fullyCapturedGroups.clear();
        ModuleType bt = ModuleType.getModuleType("Board");
        board = (Board) Module.getModule(modules, bt);
        moveNumber = board.getMoveNumber();
        moveColor = board.getColorToMove();
        if (board.isSuicideAllowed()) {
            error("LGImpl cannot handle suicide moves");
            throw new InternalErrorException("LGImpl cannot handle suicide moves");
        }
        ModuleType gt = ModuleType.getModuleType("Groups");
        Groups groups = (Groups) Module.getModule(modules, gt);
        Stone st;
        if (move.isNumberedStone()) {
            st = move.getNumberedStone();
            st = st.castToStone();
        } else return;
        int oldcount = 0;
        for (int i = 0; i < slggroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) slggroups.get(i);
            if (slgi.getColor().equals(st.getColor())) oldcount++;
        }
        SingleGroup sg = groups.getSingleGroup(st);
        ClosestPointGroup currcpg = null;
        newGroup = null;
        updatedGroup = null;
        short status = groups.status();
        switch(status) {
            case Groups.ADD:
                cp.newGroup(st);
                SingleLGImpl slgi = new SingleLGImpl(sg);
                slggroups.add(slgi);
                newGroup = slgi;
                currcpg = cp.getGroup(st);
                break;
            case Groups.UPDATE:
                Stone[] stones = sg.getStones();
                Stone oldstone;
                if (stones[0].equals(st)) oldstone = stones[1]; else oldstone = stones[0];
                currcpg = cp.getGroup(oldstone);
                cp.addStone(currcpg, st);
                SingleLGImpl slg = (SingleLGImpl) getSingleLooseGroup(oldstone);
                slg.removeSingleGroup(oldstone);
                slg.addSingleGroup(sg);
                updatedGroup = slg;
                break;
            case Groups.COMBINE:
                cp.newGroup(st);
                currcpg = cp.getGroup(st);
                slgi = new SingleLGImpl(sg);
                slggroups.add(slgi);
                SingleGroup[] csg = groups.getCombinedSingleGroups();
                HashSet hs = new HashSet();
                hs.add(currcpg);
                for (int i = 0; i < csg.length; i++) {
                    stones = csg[i].getStones();
                    ClosestPointGroup ecpg = cp.getGroup(stones[0]);
                    hs.add(ecpg);
                }
                if (hs.size() > 1) {
                    SingleLGImpl[] combslgi = new SingleLGImpl[hs.size()];
                    Iterator it = hs.iterator();
                    for (int i = 0; i < hs.size(); i++) {
                        ClosestPointGroup ncpg = (ClosestPointGroup) it.next();
                        stones = ncpg.getStones();
                        combslgi[i] = (SingleLGImpl) getSingleLooseGroup(stones[0]);
                    }
                    combineGroups(combslgi);
                    ClosestPointGroup[] combgrps = new ClosestPointGroup[hs.size()];
                    hs.toArray(combgrps);
                    cp.combineGroups(combgrps);
                    currcpg = cp.getGroup(st);
                }
                slg = (SingleLGImpl) getSingleLooseGroup(st);
                for (int i = 0; i < csg.length; i++) {
                    stones = csg[i].getStones();
                    if (sg.isInSingleGroup(stones[0])) slg.removeSingleGroup(stones[0]);
                }
                slg.addSingleGroup(sg);
                break;
            default:
                error("Unknown type for groups.status() " + status);
                return;
        }
        if (groups.wereGroupsCaptured()) {
            SingleGroup[] capturedsg = groups.getCapturedSingleGroups();
            updatePartialAndFullCaptureSLG(capturedsg);
            HashSet chs = new HashSet();
            for (int i = 0; i < capturedsg.length; i++) {
                Stone[] capstones = capturedsg[i].getStones();
                Stone capstone = capstones[0];
                ClosestPointGroup capgroup = cp.getGroup(capstone);
                cp.removeStones(capgroup, capstones);
                SingleLGImpl clg = (SingleLGImpl) getSingleLooseGroup(capturedsg[i]);
                clg.removeSingleGroup(capturedsg[i]);
                chs.add(clg);
            }
            Iterator it = chs.iterator();
            while (it.hasNext()) {
                SingleLGImpl clg = (SingleLGImpl) it.next();
                if (clg.getTotalSingleGroups() == 0) {
                    slggroups.remove(clg);
                }
            }
        }
        checkOpponentsLooseGroups(st, currcpg, groups, board);
        combineAdjacentGroupsIfPossible(currcpg, board, groups);
        int newcount = 0;
        for (int i = 0; i < slggroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) slggroups.get(i);
            if (slgi.getColor().equals(st.getColor())) newcount++;
        }
        if (oldcount < newcount) moveStatus = ADD; else if (oldcount == newcount) moveStatus = UPDATE; else moveStatus = COMBINE;
    }

    /** Update the list of groups that were fully or partially captured.
	  * The loose groups that are stored are as they were before any
	  * stones were removed from them.
	  * @param sg An array of SingleGroup objects that were captured on the last move.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private void updatePartialAndFullCaptureSLG(SingleGroup[] sg) throws InternalErrorException {
        ArrayList tempGroups = new ArrayList();
        for (int i = 0; i < sg.length; i++) {
            SingleLGImpl slgi = (SingleLGImpl) getSingleLooseGroup(sg[i].getAnyStone());
            slgi = (SingleLGImpl) slgi.clone();
            SingleGroup[] sgarr = slgi.getSingleGroups();
            boolean fullCapture = true;
            Stone[] oldst = slgi.getStones();
            int oldTotal = oldst.length;
            int capTotal = 0;
            for (int j = 0; j < sg.length; j++) capTotal += sg[j].getStones().length;
            if (capTotal < oldTotal) fullCapture = false;
            if (fullCapture) {
                fullyCapturedGroups.add(slgi);
                groupsCaptured = true;
            } else partiallyCapturedGroups.add(slgi);
        }
    }

    /** Attempt to combine any friendly groups adjacent to the group containing the last stone.
	  * If any prisoners were captured on the last move, it is also necessary to examine any
	  * friendly groups which were adjacent to the prisoners.
	  * @param currcpg The ClosestPointGroup which contains the last move.
	  * @param board The Board module.
	  * @param groups The Groups module.
	  * @throws InternalErrorException Thrown is an error occurred for any reason.
	  */
    private void combineAdjacentGroupsIfPossible(ClosestPointGroup currcpg, Board board, Groups groups) throws InternalErrorException {
        Color col = currcpg.getAnyStone().getColor();
        HashSet hs = new HashSet();
        ClosestPointGroup[] adjgroups = cp.getAdjacentGroups(currcpg);
        for (int i = 0; i < adjgroups.length; i++) {
            if (adjgroups[i].getAnyStone().getColor().equals(col)) {
                hs.add(adjgroups[i]);
            }
        }
        SingleGroup[] sg = groups.getCapturedSingleGroups();
        for (int i = 0; i < sg.length; i++) {
            Stone[] st = sg[i].getStones();
            for (int j = 0; j < st.length; j++) {
                ClosestPointGroup[] cparr = cp.getGroups(st[j]);
                for (int k = 0; k < cparr.length; k++) {
                    if (cparr[k].getColor().equals(col)) hs.add(cparr[k]);
                }
            }
        }
        ClosestPointGroup[] arr = new ClosestPointGroup[hs.size() + 1];
        arr[0] = currcpg;
        Iterator it = hs.iterator();
        for (int i = 0; i < hs.size(); i++) {
            arr[i + 1] = (ClosestPointGroup) it.next();
        }
        combineAdjacentGroupsIfPossible(arr, board, groups);
    }

    /** Combine as many of the candidate groups as possible.
	  * @param adjgroups An array of ClosestPointGroup objects corresponding to the SingleLooseGroups.
	  * @param board The Board module.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private void combineAdjacentGroupsIfPossible(ClosestPointGroup[] adjgroups, Board board, Groups groups) throws InternalErrorException {
        HashSet hs = new HashSet();
        for (int i = 0; i < adjgroups.length - 1; i++) {
            for (int j = i + 1; j < adjgroups.length; j++) {
                if (isSameLooseGroup(adjgroups[i], adjgroups[j], board)) {
                    hs.add(adjgroups[i]);
                    hs.add(adjgroups[j]);
                }
            }
        }
        if (hs.size() < 2) return;
        Iterator it = hs.iterator();
        ClosestPointGroup first = (ClosestPointGroup) it.next();
        Stone st = first.getAnyStone();
        SingleLGImpl slg = (SingleLGImpl) getSingleLooseGroup(st);
        SingleLGImpl firstslg = (SingleLGImpl) slg.clone();
        combinedGroups.add(firstslg);
        while (it.hasNext()) {
            ClosestPointGroup curr = (ClosestPointGroup) it.next();
            Stone currst = curr.getAnyStone();
            ClosestPointGroup[] cpgarr = new ClosestPointGroup[2];
            cpgarr[0] = first;
            cpgarr[1] = curr;
            cp.combineGroups(cpgarr);
            SingleLGImpl currslg = (SingleLGImpl) getSingleLooseGroup(currst);
            SingleLGImpl clonecurr = (SingleLGImpl) currslg.clone();
            combinedGroups.add(clonecurr);
            SingleGroup[] sgrps = currslg.getSingleGroups();
            slg.addSingleGroups(sgrps);
            removeGroup(currslg);
            first = cp.getGroup(st);
        }
    }

    /** Combine the single loose groups into one loose group.
	  * @param grp An array of SingleLGImpl groups to be combined into one.
	  */
    private void combineGroups(SingleLGImpl[] grp) {
        for (int i = 1; i < grp.length; i++) {
            combineGroups(grp[0], grp[i]);
        }
    }

    /** Combine the two SingleLooseGroups into one.
	  * The results of the combined groups are in	
	  * the first loose group.
	  * @param g1 - The SingleLooseGroup to hold the results of the combination.
	  * @param g2 - The SingleLooseGroup to be removed.
	  */
    private void combineGroups(SingleLGImpl g1, SingleLGImpl g2) {
        SingleGroup[] sgarr = g2.getSingleGroups();
        g1.addSingleGroups(sgarr);
        removeGroup(g2);
    }

    /** Determine if the two groups should be part of the same loose group.
	  * This method is used when the current board is being operated on.
	  * @param cpg1 - One of the ClosestPointGroup objects.
	  * @param cpg2 - The other ClosestPointGroup object.
	  * @param board - The Board module.
	  * @return true if the two groups should be combined, or false.
	  */
    private boolean isSameLooseGroup(ClosestPointGroup cpg1, ClosestPointGroup cpg2, Board board) {
        return isSameLooseGroup(cp, cpg1, cpg2, board);
    }

    /** Determine if the two groups should be part of the same loose group.
	  * This method is really the intelligence of this module.  More sophisticated
	  * implementations of LooseGroups could leave the rest of the code unchanged,
	  * and just work on improving this method.
	  * <p>
	  * This method takes a ClosestPoints object as a parameter so that it can work
	  * on different board configurations than the one with the current move.
	  * @param cpts - The ClosestPoints object.
	  * @param cpg1 - One of the ClosestPointGroup objects.
	  * @param cpg2 - The other ClosestPointGroup object.
	  * @param board - The Board module.
	  * @param groups - The Groups module.
	  * @return true if the two groups should be combined, or false.
	  */
    private boolean isSameLooseGroup(ClosestPoints cpts, ClosestPointGroup cpg1, ClosestPointGroup cpg2, Board board) {
        Stone[] stones1 = cpg1.getStones();
        Stone[] stones2 = cpg2.getStones();
        Color col = stones1[0].getColor();
        Color oppcolor = col.flip();
        if (!stones1[0].getColor().equals(stones2[0].getColor())) return false;
        StoneDistance[] sdarr = getStoneDistances(stones1, stones2);
        ArrayList lgstones = new ArrayList();
        boolean flag = false;
        for (int i = 0; i < sdarr.length; i++) {
            Stone st1 = sdarr[i].getStone1();
            Stone st2 = sdarr[i].getStone2();
            int x1 = st1.getX();
            int y1 = st1.getY();
            int y2 = st2.getY();
            int x2 = st2.getX();
            int hor = sdarr[i].getHorizontal();
            int vert = sdarr[i].getVertical();
            if (sdarr[i].compare(0, 0) == 0) {
                SystemLog.warning("In LGImpl.isSameLooseGroup(), distance == 0");
                continue;
            } else if (sdarr[i].compare(1, 1) == 0) {
                Point pt1 = Point.get(x1, y2);
                Point pt2 = Point.get(x2, y1);
                Color c1 = board.getColor(pt1);
                Color c2 = board.getColor(pt2);
                int oppcount = 0;
                Point centerPt = null;
                if (col.flip().equals(c1)) {
                    oppcount++;
                    centerPt = pt2;
                }
                if (col.flip().equals(c2)) {
                    oppcount++;
                    centerPt = pt1;
                }
                if (oppcount == 0) return true;
                if (oppcount == 2) continue;
                if (checkPonnuki(centerPt, col, board)) return true;
            } else if (sdarr[i].compare(2, 0) == 0) {
                flag = true;
                int cx = (x1 + x2) / 2;
                int cy = (y1 + y2) / 2;
                Stone center = Stone.get(col, cx, cy);
                if (board.getColor(center).equals(oppcolor)) flag = false;
                if (x1 == cx) {
                    if (x1 > 0) {
                        Point pt = Point.get(x1, cy);
                        if (board.getColor(pt).equals(oppcolor)) flag = false;
                    }
                    if (x1 < 18) {
                        Point pt = Point.get(x1 + 1, cy);
                        if (board.getColor(pt).equals(oppcolor)) flag = false;
                    }
                } else {
                    if (y1 > 0) {
                        Point pt = Point.get(cx, y1 - 1);
                        if (board.getColor(pt).equals(oppcolor)) flag = false;
                    }
                    if (y1 < 18) {
                        Point pt = Point.get(cx, y1 + 1);
                        if (board.getColor(pt).equals(oppcolor)) flag = false;
                    }
                }
            } else if ((sdarr[i].compare(2, 1) == 0) || (sdarr[i].compare(2, 2) == 0)) {
                flag = true;
                int minx = Math.min(x1, x2);
                int maxx = Math.max(x1, x2);
                int miny = Math.min(y1, y2);
                int maxy = Math.max(y1, y2);
                for (int j = minx; j <= maxx; j++) {
                    for (int k = miny; k <= maxy; k++) {
                        Point pt = Point.get(j, k);
                        if (board.getColor(pt).equals(oppcolor)) {
                            flag = false;
                            break;
                        }
                    }
                }
            } else if ((sdarr[i].compare(3, 0) == 0) || (sdarr[i].compare(3, 1) == 0)) {
                lgstones.add(sdarr[i].getStone1());
                lgstones.add(sdarr[i].getStone2());
                if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                    flag = true;
                    break;
                }
            } else if (sdarr[i].compare(4, 0) == 0) {
                if ((hor == 4) || (hor == -4)) {
                    if (y1 < 18) {
                        Stone tent1 = Stone.get(col, x1, y1 + 1);
                        Stone tent2 = Stone.get(col, x2, y1 + 1);
                        if (process3JumpCase(tent1, tent2, i, sdarr, lgstones)) {
                            if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (y1 > 0) {
                        Stone tent1 = Stone.get(col, x1, y1 - 1);
                        Stone tent2 = Stone.get(col, x2, y1 - 1);
                        if (process3JumpCase(tent1, tent2, i, sdarr, lgstones)) {
                            if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                } else {
                    if (x1 < 18) {
                        Stone tent1 = Stone.get(col, x1 + 1, y1);
                        Stone tent2 = Stone.get(col, x2 + 1, y1);
                        if (process3JumpCase(tent1, tent2, i, sdarr, lgstones)) {
                            if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (x1 > 0) {
                        Stone tent1 = Stone.get(col, x1 - 1, y1);
                        Stone tent2 = Stone.get(col, x2 - 1, y1);
                        if (process3JumpCase(tent1, tent2, i, sdarr, lgstones)) {
                            if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }
            } else if (sdarr[i].compare(4, 1) == 0) {
                ArrayList tent = new ArrayList();
                if (hor == 4) {
                    if (x1 > 0) tent.add(Stone.get(col, x1 - 1, y2));
                    if (x2 < 18) tent.add(Stone.get(col, x2 + 1, y1));
                } else if (hor == -4) {
                    if (x1 < 18) tent.add(Stone.get(col, x1 + 1, y2));
                    if (x2 > 0) tent.add(Stone.get(col, x2 - 1, y1));
                } else if (vert == 4) {
                    if (y1 > 0) tent.add(Stone.get(col, x2, y1 - 1));
                    if (y2 < 18) tent.add(Stone.get(col, x1, y2 + 1));
                } else if (vert == -4) {
                    if (y1 < 18) tent.add(Stone.get(col, x2, y1 + 1));
                    if (y2 > 0) tent.add(Stone.get(col, x1, y2 - 1));
                }
                Stone origst1 = sdarr[i].getStone1();
                Stone origst2 = sdarr[i].getStone2();
                int index = i + 1;
                while (index < sdarr.length) {
                    int cval = sdarr[index].compare(5, 0);
                    if (cval > 0) break; else if (cval == 0) {
                        for (int j = 0; j < tent.size(); j++) {
                            Stone st = (Stone) tent.get(j);
                            Stone testst1 = sdarr[index].getStone1();
                            Stone testst2 = sdarr[index].getStone2();
                            if (st.equals(testst1)) {
                                if (testst2.equals(origst2)) {
                                    flag = true;
                                    lgstones.add(st);
                                    break;
                                }
                            }
                            if (st.equals(testst2)) {
                                if (testst1.equals(origst1)) {
                                    flag = true;
                                    lgstones.add(st);
                                    break;
                                }
                            }
                            if (flag) {
                                lgstones.add(origst1);
                                lgstones.add(origst2);
                                flag = false;
                                if (checkEmptyPoints(cpts, lgstones, col, cpg1, cpg2)) {
                                    flag = true;
                                    break;
                                } else {
                                    flag = false;
                                    lgstones.clear();
                                }
                            }
                        }
                    }
                    index++;
                }
            }
        }
        return flag;
    }

    /** Determine if the two groups form a three space extension from a two stone wall.
	  * In addition, put the three stones into the lgstones ArrayList variable.
	  * @param tent1 The tentative stone from the first group.
	  * @param tent1 The tentative stone from the second group.
	  * @param index The index to start searching the sorted sdarr array at.
	  * @param sdarr The sorted array of StoneDistance objects.
	  * @param lgstones The ArrayList to add the three stones to if they are present.
	  * @return true if the three space jump from a two stone wall is found, or false.
	  */
    private boolean process3JumpCase(Stone tent1, Stone tent2, int index, StoneDistance[] sdarr, ArrayList lgstones) {
        boolean flag = false;
        while (index < sdarr.length) {
            int cval = sdarr[index].compare(4, 1);
            if (cval > 0) break; else if (cval == 0) {
                if (tent1.equals(sdarr[index].getStone1())) {
                    flag = true;
                    lgstones.add(sdarr[index].getStone1());
                    lgstones.add(sdarr[index].getStone2());
                    lgstones.add(tent1);
                    break;
                } else if (tent2.equals(sdarr[index].getStone2())) {
                    flag = true;
                    lgstones.add(sdarr[index].getStone1());
                    lgstones.add(sdarr[index].getStone2());
                    lgstones.add(tent2);
                    break;
                }
            }
            index++;
        }
        return flag;
    }

    /** Determine if at least three sides of a ponnuki of the given color exist around the center point.
	  * @param center The point around which the ponnuki would exist. 
	  * @param color The color of the side for which the ponnuki is being checked. 
	  * @param board The Board module.
	  * @return true if there is a ponnuki, or false.
	  */
    private boolean checkPonnuki(Point center, Color color, Board board) {
        center = center.castToPoint();
        Color oppColor = color.flip();
        int cx = center.getX();
        int cy = center.getY();
        if (((cx == 0) || (cx == 18)) && ((cy == 0) || (cy == 18))) return true;
        if ((cx == 0) || (cx == 18)) {
            Point pt1 = Point.get(cx, cy + 1);
            Point pt2 = Point.get(cx, cy - 1);
            if (board.getColor(pt1).equals(oppColor)) return false; else if (board.getColor(pt2).equals(oppColor)) return false; else return true;
        }
        if ((cy == 0) || (cy == 18)) {
            Point pt1 = Point.get(cx + 1, cy);
            Point pt2 = Point.get(cx - 1, cy);
            if (board.getColor(pt1).equals(oppColor)) return false; else if (board.getColor(pt2).equals(oppColor)) return false; else return true;
        }
        Point pt[] = new Point[4];
        pt[0] = Point.get(cx, cy + 1);
        pt[1] = Point.get(cx + 1, cy);
        pt[2] = Point.get(cx, cy - 1);
        pt[3] = Point.get(cx - 1, cy);
        int pointCount = 0;
        for (int i = 0; i < 4; i++) {
            if (board.getColor(pt[i]).equals(color)) pointCount++;
        }
        if (pointCount == 4) return true; else if (pointCount < 3) return false; else {
            for (int i = 0; i < 4; i++) {
                if (board.getColor(pt[i]).equals(oppColor)) return false;
            }
            return true;
        }
    }

    /** Determine if there are any opponents stones preventing the two groups from combining.
	  * @param cpts - The ClosestPoints variable which contains all of the groups.
	  * @param al - The stones between which the empty points are being checked.
	  * @param col - The color of the groups being checked.
	  * @param cpg1 - The first group being checked.
	  * @param cpg2 - The other group being checked.
	  * @return true if the empty points are clear of opponents stones, or false.
	  */
    private boolean checkEmptyPoints(ClosestPoints cpts, ArrayList al, Color col, ClosestPointGroup cpg1, ClosestPointGroup cpg2) {
        int minx = 19;
        int miny = 19;
        int maxx = -1;
        int maxy = -1;
        for (int i = 0; i < al.size(); i++) {
            Stone st = (Stone) al.get(i);
            int x = st.getX();
            int y = st.getY();
            if (x < minx) minx = x;
            if (x > maxx) maxx = x;
            if (y < miny) miny = y;
            if (y > maxy) maxy = y;
        }
        ArrayList candAl = new ArrayList();
        ClosestPointGroup[] enc1 = cpts.getEnclosedGroups(cpg1);
        ClosestPointGroup[] enc2 = cpts.getEnclosedGroups(cpg2);
        ClosestPointGroup[] enclosed = new ClosestPointGroup[enc1.length + enc2.length];
        System.arraycopy(enc1, 0, enclosed, 0, enc1.length);
        System.arraycopy(enc2, 0, enclosed, enc1.length, enc2.length);
        for (int i = minx; i <= maxx; i++) {
            for (int j = miny; j <= maxy; j++) {
                candAl.clear();
                Point pt = Point.get(i, j);
                if (cpts.isEmptyPoint(pt)) {
                    ClosestPointGroup[] arr = cpts.getGroups(pt);
                    boolean flag = true;
                    for (int k = 0; k < arr.length; k++) {
                        if (arr[k].equals(cpg1) || arr[k].equals(cpg2)) flag = false;
                    }
                    if (flag) {
                        for (int k = 0; k < arr.length; k++) candAl.add(arr[k]);
                    }
                }
                Color c = col.flip();
                Stone st = Stone.get(c, i, j);
                ClosestPointGroup cpgrp = cpts.getGroupNoException(st);
                if (cpgrp != null) {
                    candAl.add(cpgrp);
                }
                for (int k = 0; k < candAl.size(); k++) {
                    boolean flag = false;
                    ClosestPointGroup currCpg = (ClosestPointGroup) candAl.get(k);
                    if (currCpg.getColor().equals(col)) continue;
                    for (int l = 0; l < enclosed.length; l++) {
                        if (currCpg.equals(enclosed[l])) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) return false;
                }
            }
        }
        return true;
    }

    /** Get the distances between any two pair of stones.
	  * The array is sorted in ascending distance between the
	  * two stones. If there are ties, then the order is undefined.
	  * @param s1 - An array of stones.
	  * @param s2 - A second array of stones.
	  * @return An array of StoneDistance objects.
	  */
    private StoneDistance[] getStoneDistances(Stone[] s1, Stone[] s2) {
        StoneDistance[] retarr = new StoneDistance[s1.length * s2.length];
        for (int i = 0; i < s1.length; i++) {
            for (int j = 0; j < s2.length; j++) {
                StoneDistance sd = new StoneDistance(s1[i], s2[j]);
                retarr[(s2.length * i) + j] = sd;
            }
        }
        Arrays.sort(retarr);
        return retarr;
    }

    /** Split opponents groups adjacent to the current loose group if necessary.
	  * The last move made has either added a new group, added a stone to an
	  * existing group, or caused two or more groups to be combined into one.
	  * In any case, this may cause an opponents loose group to be split into
	  * two or more loose groups.  Perform this operation if necessary.
	  * @param stone - The last move made.
	  * @param cpg - The ClosestPointGroup object which contains the stone.
	  */
    private void checkOpponentsLooseGroups(Stone stone, ClosestPointGroup cpg, Groups groups, Board board) throws InternalErrorException {
        Color oppcolor = stone.getColor().flip();
        ArrayList olg = new ArrayList();
        ClosestPointGroup[] cpgarr = cp.getAdjacentGroups(cpg);
        for (int i = 0; i < cpgarr.length; i++) {
            if (cpgarr[i].getColor().equals(oppcolor)) {
                olg.add(cpgarr[i]);
            }
        }
        if (olg.size() == 0) return;
        for (int i = olg.size() - 1; i >= 0; i--) {
            ClosestPointGroup candidate = (ClosestPointGroup) olg.get(i);
            SingleLGImpl candslg = (SingleLGImpl) getSingleLooseGroup(candidate.getAnyStone());
            SingleLGImpl[] arr = splitGroupIfNecessary(stone, candslg, groups, board);
            if (arr.length > 1) {
                groupsSplit = true;
                cp.removeGroup(candidate);
                removeGroup(candslg);
                for (int j = 0; j < arr.length; j++) {
                    splitGroups.add(arr[j]);
                    Stone[] slgstones = arr[j].getStones();
                    cp.newGroup(slgstones[0]);
                    ClosestPointGroup newcpg = cp.getGroup(slgstones[0]);
                    for (int k = 1; k < slgstones.length; k++) {
                        cp.addStone(newcpg, slgstones[k]);
                    }
                    slggroups.add(arr[j]);
                }
            }
        }
    }

    /** Determine if the given loose group should be split up based on the last move.
	  * @param stone The last move played.
	  * @param candidate The loose group in question.
	  * @param groups The Groups module.
	  * @param board The Board module.
	  * @return An array of SingleLGImpl objects, each of which should be its own loose group.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private SingleLGImpl[] splitGroupIfNecessary(Stone stone, SingleLGImpl candidate, Groups groups, Board board) throws InternalErrorException {
        if (candidate.getTotalSingleGroups() == 1) {
            SingleLGImpl[] slarr = new SingleLGImpl[1];
            slarr[0] = candidate;
            return slarr;
        }
        ClosestPoints newcp = (ClosestPoints) cp.clone();
        Stone candstone = candidate.getAnyStone();
        ClosestPointGroup remcpg = newcp.getGroup(candstone);
        newcp.removeGroup(remcpg);
        ArrayList al = new ArrayList();
        SingleGroup[] sg = candidate.getSingleGroups();
        for (int i = 0; i < sg.length; i++) {
            Stone[] starr = sg[i].getStones();
            newcp.newGroup(starr[0]);
            ClosestPointGroup currcpg = newcp.getGroup(starr[0]);
            al.add(currcpg);
            for (int j = 1; j < starr.length; j++) {
                newcp.addStone(currcpg, starr[j]);
            }
        }
        ClosestPointGroup[] cpgarr = combinePairs(newcp, al, board, groups);
        if (cpgarr.length == 1) {
            SingleLGImpl[] slarr = new SingleLGImpl[1];
            slarr[0] = candidate;
            return slarr;
        }
        SingleLGImpl[] slgi = new SingleLGImpl[cpgarr.length];
        for (int i = 0; i < cpgarr.length; i++) {
            Stone[] starr = cpgarr[i].getStones();
            HashSet hs = new HashSet();
            for (int j = 0; j < starr.length; j++) {
                SingleGroup currsg = groups.getSingleGroup(starr[j]);
                hs.add(currsg);
            }
            Iterator it = hs.iterator();
            SingleGroup currsg = (SingleGroup) it.next();
            slgi[i] = new SingleLGImpl(currsg);
            while (it.hasNext()) {
                currsg = (SingleGroup) it.next();
                slgi[i].addSingleGroup(currsg);
            }
        }
        return slgi;
    }

    /** Combine the ClosestPointGroups as much as possible based on criteria for loose groups.
	  * @param cpts The appropriate ClosestPoints object.
	  * @param al Each element is a ClosestPointGroup objects.
	  * @param board The Board module.
	  * @param groups The Groups module.
	  * @return An array of ClosestPointGroup objects.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private ClosestPointGroup[] combinePairs(ClosestPoints cpts, ArrayList al, Board board, Groups groups) throws InternalErrorException {
        for (int i = al.size() - 2; i >= 0; i--) {
            for (int j = al.size() - 1; j > i; j--) {
                ClosestPointGroup cpg1 = (ClosestPointGroup) al.get(i);
                ClosestPointGroup cpg2 = (ClosestPointGroup) al.get(j);
                if (isSameLooseGroup(cpts, cpg1, cpg2, board)) {
                    Stone[] st = cpg2.getStones();
                    al.remove(cpg2);
                    cpts.removeGroup(cpg2);
                    for (int k = 0; k < st.length; k++) {
                        cpts.addStone(cpg1, st[k]);
                    }
                }
            }
        }
        ClosestPointGroup[] cpgrp = new ClosestPointGroup[al.size()];
        al.toArray(cpgrp);
        return cpgrp;
    }

    /** Get the edge points for the given loose group.
	  * Conceptually, loose groups near the edge of the board may be thought
	  * of as having implicit points on the edge of the board.  For example,
	  * for a two space extension on the third line, each of the points on the
	  * edge below the stones could be thought of as a point belonging to the
	  * loose group.  This method finds those points, if the loose group has any.
	  * @param slg The loose group in question.
	  * @return An array of Point objects, each of which is an edge point.
	  */
    public Point[] getAssociatedEdgePoints(SingleLooseGroup slg) {
        try {
            Stone any = slg.getAnyStone();
            slg = getSingleLooseGroup(any);
            Long zval = Zobrist.getValue(slg.getStones());
            Point[] pts = (Point[]) edgePoints.get(zval);
            if (pts != null) return pts;
            ArrayList points = new ArrayList();
            HashSet hs = new HashSet();
            Color color = slg.getColor();
            Stone[] starr = slg.getStones();
            for (int i = 0; i < starr.length; i++) {
                int x = starr[i].getX();
                int y = starr[i].getY();
                if (x < 3) {
                    Stone st = Stone.get(color, 0, y);
                    Point nextpt = Point.get(1, y);
                    if (board.getColor(nextpt).equals(color)) points.add(st.castToPoint()); else hs.add(st);
                } else if (x > 15) {
                    Stone st = Stone.get(color, 18, y);
                    Point nextpt = Point.get(17, y);
                    if (board.getColor(nextpt).equals(color)) points.add(st.castToPoint()); else hs.add(st);
                }
                if (y < 3) {
                    Stone st = Stone.get(color, x, 0);
                    Point nextpt = Point.get(x, 1);
                    if (board.getColor(nextpt).equals(color)) points.add(st.castToPoint()); else hs.add(st);
                } else if (y > 15) {
                    Stone st = Stone.get(color, x, 18);
                    Point nextpt = Point.get(x, 17);
                    if (board.getColor(nextpt).equals(color)) points.add(st.castToPoint()); else hs.add(st);
                }
            }
            ClosestPoints newcp = (ClosestPoints) cp.clone();
            Stone currst = slg.getAnyStone();
            ClosestPointGroup currcpg = newcp.getGroup(currst);
            Iterator it = hs.iterator();
            while (it.hasNext()) {
                Stone st = (Stone) it.next();
                if (!board.getColor(st).equals(Color.EMPTY)) continue;
                newcp.newGroup(st);
                ClosestPointGroup tentcpg = newcp.getGroup(st);
                Color colorToMove = board.getColorToMove();
                short movenum = moveNumber;
                Board newboard = (Board) board.clone();
                if (!color.equals(colorToMove)) {
                    Move passmove = new Move(Move.PASS, movenum, colorToMove);
                    newboard.makeMove(passmove);
                    movenum++;
                }
                if (newboard.isLegal(st)) {
                    Move newmove = new Move(movenum, st);
                    newboard.makeMove(newmove);
                    if (isSameLooseGroup(newcp, currcpg, tentcpg, newboard)) points.add(st.castToPoint());
                }
            }
            Point[] pt = new Point[points.size()];
            points.toArray(pt);
            zval = Zobrist.getValue(slg.getStones());
            edgePoints.put(zval, pt);
            return pt;
        } catch (Exception e) {
            error(e);
            return new Point[0];
        }
    }

    /** Get a String representation of the object.
	  * @return A String which represents the object.
	  */
    public String toString() {
        String s = super.toString() + " slggroups.size() = " + Integer.toString(slggroups.size());
        return s;
    }

    /** Override the Object.clone() method.
	  * @return An LGImpl object which is a clone of this one.
	  */
    public Object clone() {
        HashMap mapping = new HashMap();
        LGImpl lg = (LGImpl) super.clone();
        lg.slggroups = new ArrayList();
        for (int i = 0; i < slggroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) slggroups.get(i);
            SingleLGImpl slgnew = (SingleLGImpl) slgi.clone();
            lg.slggroups.add(slgnew);
            mapping.put(slgi, slgnew);
        }
        lg.cp = (ClosestPoints) cp.clone();
        lg.ratedMoves = new ArrayList();
        for (int i = 0; i < ratedMoves.size(); i++) {
            RatedMove rm = (RatedMove) ratedMoves.get(i);
            lg.ratedMoves.add(rm.clone());
        }
        if (newGroup != null) lg.newGroup = (SingleLGImpl) mapping.get(newGroup);
        if (updatedGroup != null) lg.updatedGroup = (SingleLGImpl) mapping.get(updatedGroup);
        lg.splitGroups = new ArrayList();
        for (int i = 0; i < splitGroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) splitGroups.get(i);
            lg.splitGroups.add(slgi.clone());
        }
        lg.combinedGroups = new ArrayList();
        for (int i = 0; i < combinedGroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) combinedGroups.get(i);
            lg.combinedGroups.add(slgi.clone());
        }
        lg.partiallyCapturedGroups = new ArrayList();
        for (int i = 0; i < partiallyCapturedGroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) partiallyCapturedGroups.get(i);
            lg.partiallyCapturedGroups.add(slgi.clone());
        }
        lg.fullyCapturedGroups = new ArrayList();
        for (int i = 0; i < fullyCapturedGroups.size(); i++) {
            SingleLGImpl slgi = (SingleLGImpl) fullyCapturedGroups.get(i);
            lg.fullyCapturedGroups.add(slgi.clone());
        }
        return lg;
    }
}
