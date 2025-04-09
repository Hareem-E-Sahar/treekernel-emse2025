package org.retro.gis.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.retro.gis.relate.SchemeObjectException;

/**
 * A null implementation of the BasicActionNode, mainly
 * used for adding a 'no operation' style action or debugging.
 *
 * The null action will contain another tree, whereby the AI can
 * scan the set of possible actions.
 * <p>
 *  
 * This is a high-priority class, the name of the class is a misnomer,
 * this class is very useful,ignore the Null aspect.
 *
 * @author Berlin Brown
 * @see BasicActionTree
 * @see ActionTreeNode
 * @see BasicActionImpl
 */
public class NullActionImpl extends BasicActionNode {

    private double scoreTotal = 0.0;

    private BasicActionTreeImpl actionTree = new BasicActionTreeImpl();

    public NullActionImpl(int nested, double _min, double _max) throws InvalidScoreException {
        this.setScoreRangeSet(nested, _min, _max);
    }

    public void performActionCheck() {
        setRunFlag(true);
    }

    /**
	 * There are several levels to worry about when dealing
	 * with the BotProcessThread.
	 * <p>
	 * use - setPostProcessAuto after the set-process-auto has been
	 * used from the RootActionTree to hit the branch nodes.
	 *  
	 * Set the RootActionTree:(tree)
	 *              ActionTreeNodes:(node)
	 *                   BranchNodes:(branch/node)
	 *                   BranchNodes:(branch/node)
	 *              ActionTreeNodes:
	 */
    public void setPostProcessAuto() throws Exception {
        if (this.getProcessThreadNode() == null) throw new Exception("Branch-Auto : Invalid BotProcssThread");
        actionTree.setProcessThreadAuto(this.getProcessThreadNode());
    }

    /**
	 * The decision matrix will typically be simple,
	 * run through all the nodes in the internal tree,
	 * and find maximize the current score if the node,
	 * can be run.
	 *
	 * <b>Recursive-Call</b>
	 *
	 * The parent has the role of deciding on running nodes
	 * to achieve a good score.
	 * 
	 * <h2> the recursion aspect </h2>
	 * <ul> 
	 *  <li>Check level
	 *  <li> Check the max exceeded
	 *  <li> Check min reached
	 *  <li> Check if the branch node can be run again.
	 * </ul>
	 *
	 * @see #BasicActionNode.getPoints() 
	 *
	 */
    protected void runDecisionMatrix(int level) {
        boolean canrun = false;
        ArrayList l = actionTree.getArrayList();
        int errCtr = 0;
        for (Iterator it = l.iterator(); it.hasNext(); ) {
            Object actionNode = it.next();
            Boolean b = null;
            Double d = null;
            if (actionNode instanceof BranchActionNode) {
                BranchActionNode runNode = (BranchActionNode) actionNode;
                try {
                    runNode.setLevel(level);
                    Object results[] = (Object[]) runNode.runAction();
                    if (results[0] instanceof Boolean) b = (Boolean) results[0];
                    if (results[1] instanceof Double) d = (Double) results[1];
                    if (b.booleanValue()) errCtr++;
                    scoreTotal += d.doubleValue();
                } catch (Exception _invalid) {
                    _invalid.printStackTrace();
                }
            }
        }
        if (errCtr == 0) {
            return;
        } else if (scoreTotal < getMinScoreThreshold()) {
            return;
        } else if (scoreTotal > getMaxScoreThreshold()) {
            return;
        } else if (level > getLevelThreshold()) {
            return;
        } else {
            runDecisionMatrix(level + 1);
        }
    }

    /**
	 * This implemenatation may include searching a database for a value, pass if 
	 * target value found.
	 */
    protected boolean performScoreHitPass() {
        return false;
    }

    protected void runActionInternal() {
        runDecisionMatrix(0);
    }

    /**
	 * Check the totals based on the nodes in the
	 * current tree
	 */
    protected final void sumTotal() {
        try {
            scoreTotal = 0;
            if (false) throw new InvalidScoreException("Invalid");
        } catch (InvalidScoreException e) {
            System.out.println("Warning: Invalid Score Settings for this Node" + e.getMessage());
        }
    }

    /**
	 * Based on the incoming scheme message key, determine a suitable node to create.
	 *
	 * This method implements a loose Decision-Tree
	 *
	 * Scheme Used: [ car ] [ cdr ] [ member ]
	 * 
	 * The commands work with the smartbranch create in
	 * NullActionImpl.
	 * 
	 * <p>
	 * <ul>
	 * 	<li>irc-send
	 *  <li>client-send
	 *  <li>think-send
	 * </ul>
	 * 
	 * <p>
	 * These commands are used for smart action create
	 * <p>
	 * 
	 * <ul> 
	 *  <li>ircsendmsg
	 *	<li>clientsendmsg
	 *	<li>thinksendmsg
	 * </ul>
     *

	 *
	 * @param key                       The key is typically scheme based command
	 * @exception SchemeObejctException This tree must contain a valid scheme object or
	 *                                  an exception is thrown.
	 */
    public void smartCreateBranch(String key, Object objAttach) throws SchemeObjectException {
        this.checkSchemeObject();
        String tcmd = key;
        Pattern px = null;
        Matcher mx = null;
        px = Pattern.compile("\\s", Pattern.CASE_INSENSITIVE);
        mx = px.matcher(tcmd);
        String results[] = px.split(tcmd);
        boolean _found = false;
        int str = -1;
        int end = -1;
        while (mx.find()) {
            str = mx.start();
            end = mx.end();
            _found = true;
            break;
        }
        String cmd = results[0].trim();
        String args = key.substring(end, key.length()).trim();
        String cmds[] = this.getCommandList();
        try {
            if (cmd.equalsIgnoreCase(cmds[0])) {
                PrintBranchAction p = new PrintBranchAction(0, 0, 0);
                p.setMessage(args);
                p.setKey(key);
                actionTree.addNode(p);
            } else if (cmd.equalsIgnoreCase(cmds[1])) {
                AttachTreeAction p = new AttachTreeAction(0, 0, 0);
                p.attachObject(objAttach);
                p.setKey(key);
                actionTree.addNode(p);
            } else if (cmd.equalsIgnoreCase(cmds[2])) {
                SendMessageBranch p = new SendMessageBranch(0, 0, 0);
                p.setBotProcessThread(this.getProcessThreadNode());
                p.setMessage(args);
                p.setKey(key);
                actionTree.addNode(p);
            } else if (cmd.equalsIgnoreCase(cmds[3])) {
                InternalMessageBranch p = new InternalMessageBranch(0, 0, 0);
                p.setBotProcessThread(this.getProcessThreadNode());
                p.setMessage(args);
                p.setInternalType("irc-send");
                p.setKey(key);
                actionTree.addNode(p);
            } else if (cmd.equalsIgnoreCase(cmds[4])) {
                InternalMessageBranch p = new InternalMessageBranch(0, 0, 0);
                p.setBotProcessThread(this.getProcessThreadNode());
                p.setMessage(args);
                p.setInternalType("client-send");
                p.setKey(key);
                actionTree.addNode(p);
            } else if (cmd.equalsIgnoreCase(cmds[5])) {
                InternalMessageBranch p = new InternalMessageBranch(0, 0, 0);
                p.setBotProcessThread(this.getProcessThreadNode());
                p.setMessage(args);
                p.setInternalType("think-send");
                p.setKey(key);
                actionTree.addNode(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Add Default Branch element, you should instantiate a treenode
	 * outside of this method.
	 */
    public void addBranchNode(BranchActionNode node) throws KeyNotSetException {
        actionTree.addNode(node);
    }

    /**
	 * Add Default Branch element, you should instantiate a treenode
	 * outside of this method.
	 */
    public void addBranchNode(String key, double pts, double _pass, double _fail) throws InvalidScoreException, KeyNotSetException {
        NullBranchActionNode node = new NullBranchActionNode(pts, _pass, _fail);
        node.setKey(key);
        actionTree.addNode(node);
    }
}
