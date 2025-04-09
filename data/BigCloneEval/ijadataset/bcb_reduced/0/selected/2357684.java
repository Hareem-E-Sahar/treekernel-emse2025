package org.dcopolis.algorithm.pseudotree;

import org.dcopolis.algorithm.*;
import org.dcopolis.platform.*;
import org.dcopolis.problem.*;
import org.dcopolis.util.*;
import java.util.*;
import java.io.*;

@SuppressWarnings("unchecked")
public class DynamicDecentralizedVariableOrderingPlugin extends DynamicVariableOrderingPlugin {

    DynamicVariableOrdering ordering;

    Hashtable<Variable, Integer> m;

    Hashtable<Variable, HashSet<Variable>> r;

    Hashtable<Variable, Boolean> s;

    Hashtable<Variable, Variable> active;

    HashSet<Variable> adding;

    Hashtable<Variable, Integer> logicTime;

    public DynamicDecentralizedVariableOrderingPlugin() {
        ordering = null;
        listenForMessages(AlreadyActiveMessage.class, AgentPlugin.MessageAction.NO_ACTION);
        listenForMessages(NewVarDoneMessage.class, AgentPlugin.MessageAction.CONSUME_FOR_AGENT);
        listenForMessages(RemovePossibleChildMessage.class, AgentPlugin.MessageAction.CONSUME_FOR_AGENT);
        listenForMessages(NewVarReply.class, AgentPlugin.MessageAction.CONSUME_FOR_AGENT);
    }

    @Override
    protected void initialize() {
        m = new Hashtable<Variable, Integer>();
        r = new Hashtable<Variable, HashSet<Variable>>();
        s = new Hashtable<Variable, Boolean>();
        active = new Hashtable<Variable, Variable>();
        adding = new HashSet<Variable>();
        logicTime = new Hashtable<Variable, Integer>();
    }

    @Override
    public AgentPlugin.MessageAction handleNewMessage(AgentMessage message) {
        if (message instanceof DynamicDecentralizedVariableOrderingMessage || (message instanceof AlreadyActiveMessage && !adding.contains(message.getRecipient()))) {
            addRoutine(new MessageHandler(message));
            return AgentPlugin.MessageAction.CONSUME_FOR_AGENT;
        } else return AgentPlugin.MessageAction.NO_ACTION;
    }

    private class MessageHandler implements Runnable {

        AgentMessage message;

        public MessageHandler(AgentMessage message) {
            this.message = message;
        }

        public void run() {
            if (message instanceof NewVarMessage) handleNewVarMessage((NewVarMessage) message); else if (message instanceof SetParentMessage) handleSetParentMessage((SetParentMessage) message); else if (message instanceof AddRequest) {
                addVariable(message.getRecipient());
            } else if (message instanceof AlreadyActiveMessage) handleAlreadyActive((AlreadyActiveMessage) message); else if (message instanceof CancelSearchMessage) handleCancelSearch((CancelSearchMessage) message); else if (message instanceof AbortSearchMessage) handleAbortSearch((AbortSearchMessage) message); else if (message instanceof IAmYourAncestorMessage) handleAncestorMessage((IAmYourAncestorMessage) message);
        }
    }

    private void handleAncestorMessage(IAmYourAncestorMessage am) {
        log("handleAncestorMessage from " + am.getSender() + "\n\tordering is " + (ordering == null ? "" : "not ") + "null" + (ordering == null ? "" : "\n\tordering.getNodeForVariable(" + am.getRecipient() + "): " + ordering.getNodeForVariable(am.getRecipient()) + "\n\tordering.getNodeForVariable(" + am.getSender() + "): " + ordering.getNodeForVariable(am.getSender())));
        if (ordering == null || ordering.getNodeForVariable(am.getRecipient()) == null) return;
        addDistantAncestor(ordering.getNodeForVariable(am.getRecipient()), am.getSender());
        if (ordering.getNodeForVariable(am.getSender()) == null) {
            log("ASSERTION FAILED: DISTANT ANCESTOR NOT ADDED!");
            System.exit(1);
        }
    }

    private void handleAbortSearch(AbortSearchMessage asm) {
        Variable newVar = asm.getNewVar();
        log("handleAbortSearch(" + asm.getRecipient() + ", " + newVar + ")");
        if (active.get(asm.getRecipient()) == null || !active.get(asm.getRecipient()).equals(newVar)) return;
        VariableOrdering parent = ordering.getNodeForVariable(asm.getRecipient()).getParent();
        if (parent != null) {
            try {
                sendMessage(new AbortSearchMessage(parent.getVariable(), newVar));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        active.remove(asm.getRecipient());
        s.remove(newVar);
        m.remove(newVar);
        r.remove(newVar);
        updateProperty("active for " + asm.getRecipient(), false);
        updateProperty(asm.getRecipient().toString() + " is adding", null);
    }

    private void handleCancelSearch(CancelSearchMessage csm) {
        log("handleCancelSearch(" + csm.getRecipient() + ", " + csm.getNewVar() + ")");
        if (active.get(csm.getRecipient()) == null) return;
        if (!active.get(csm.getRecipient()).equals(csm.getNewVar())) return;
        Variable newVar = csm.getNewVar();
        if (!s.get(newVar).booleanValue()) {
            for (Variable c : r.get(newVar)) {
                try {
                    sendMessage(new CancelSearchMessage(c, newVar));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        active.remove(csm.getRecipient());
        s.remove(newVar);
        m.remove(newVar);
        r.remove(newVar);
        updateProperty("active for " + csm.getRecipient(), false);
        updateProperty(csm.getRecipient().toString() + " is adding", null);
    }

    private void handleAlreadyActive(AlreadyActiveMessage aam) {
        Variable newVar = active.get(aam.getRecipient());
        if (newVar == null) return;
        log("Was just notified by " + aam.getSender() + " that it is already active!");
        if (s.get(newVar).booleanValue()) {
            try {
                sendMessage(new AlreadyActiveMessage(aam.getRecipient(), newVar, aam.isHigherPriority()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            for (Variable c : r.get(newVar)) {
                try {
                    sendMessage(new AlreadyActiveMessage(aam.getRecipient(), c, aam.isHigherPriority()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        active.remove(aam.getRecipient());
        s.remove(newVar);
        m.remove(newVar);
        r.remove(newVar);
    }

    private boolean makeActive(Variable sender, Variable recipient, Variable newVar) {
        if (active.get(recipient) == null) {
            updateProperty("active for " + recipient, true);
            updateProperty(recipient.toString() + " is adding", newVar);
            active.put(recipient, newVar);
            return true;
        } else if (!active.get(recipient).equals(newVar)) {
            try {
                log("Warning: " + recipient + " is already active for variable " + active.get(recipient) + ", not " + newVar + "!  Notifying " + sender + "...");
                sendMessage(new AlreadyActiveMessage(recipient, sender, active.get(recipient).getName().compareTo(newVar.getName()) > 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void handleSetParentMessage(SetParentMessage spm) {
        log(spm.toString());
        Variable sender = spm.getSender();
        Variable recipient = spm.getRecipient();
        DynamicVariableOrdering recipientNode = ordering.getNodeForVariable(recipient);
        if (recipientNode.getParent() != null) {
            log("Warning: assertion failed: " + recipient + " received a SetParent message from " + sender + ", but " + recipient + " is not the root of its pseudotree!");
            return;
        }
        DynamicVariableOrdering senderNode = ordering.getNodeForVariable(sender);
        if (senderNode == null) senderNode = newInstance(sender);
        log(sender.toString() + " is now my parent!");
        setParent(recipientNode, senderNode);
        handleCancelSearch(new CancelSearchMessage(recipient, sender));
    }

    private void handleNewVarMessage(NewVarMessage nvm) {
        log("handleNewVarMessage: " + nvm.toString());
        log("nvm.getTime() = " + nvm.getTime());
        Variable sender = nvm.getSender();
        Variable newVar = nvm.getNewVariable();
        Variable recipient = nvm.getRecipient();
        int time = nvm.getTime();
        DynamicVariableOrdering recipientNode = null;
        if (ordering != null) recipientNode = ordering.getNodeForVariable(recipient);
        if (recipientNode != null && !makeActive(sender, recipient, newVar)) return;
        if (sender.equals(newVar)) {
            try {
                log("Time is: " + time);
                log("Sending " + (new NewVarReply(recipientNode != null, recipient, newVar, time)));
                sendMessage(new NewVarReply(recipientNode != null, recipient, newVar, time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (recipientNode == null) return;
        int q = nvm.getNumNeighbors();
        if (m.get(newVar) == null) {
            m.put(newVar, (sender.equals(newVar) ? 1 : 0));
            r.put(newVar, new HashSet<Variable>());
            s.put(newVar, sender.equals(newVar));
        }
        if (recipientNode.getChildren().contains(sender)) {
            m.put(newVar, m.get(newVar).intValue() + 1);
            r.get(newVar).add(sender);
        }
        if (recipientNode.getParent() == null && m.get(newVar).intValue() < q) {
            log("7: proposing that " + newVar + " become our (" + recipient + "'s) parent");
            try {
                sendMessage(new NewVarDoneMessage(null, recipient, 1, newVar, time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (m.get(newVar).intValue() == q) {
            if (r.get(newVar).size() == 1) {
                log("12");
                addDistantDescendant(recipientNode, newVar);
                int qprime = q;
                if (s.get(newVar)) qprime--;
                Variable vk = null;
                for (Variable v : r.get(newVar)) vk = v;
                try {
                    sendMessage(new NewVarMessage(newVar, qprime, recipient, vk, time));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                log("19");
                if (!r.get(newVar).isEmpty()) {
                    log("20");
                    DynamicVariableOrdering newParent = recipientNode.getParent();
                    if (newParent != null) setParent(recipientNode, addChild(newVar, newParent)); else {
                        try {
                            sendMessage(new RemovePossibleChildMessage(recipient, newVar));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setParent(recipientNode, newInstance(newVar));
                    }
                    try {
                        sendMessage(new NewVarDoneMessage((newParent == null ? null : newParent.getVariable()), recipient, q, newVar, time));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    log("24");
                    addChild(newVar, recipientNode);
                    try {
                        sendMessage(new NewVarDoneMessage(recipient, null, q, newVar, time));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handleCancelSearch(new CancelSearchMessage(recipient, newVar));
            }
        } else if (recipientNode.getParent() != null) {
            log("28");
            addDistantAncestor(recipientNode, newVar);
            try {
                sendMessage(new NewVarMessage(newVar, q, recipient, recipientNode.getParent().getVariable(), time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class IAmYourAncestorMessage extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 6892039157224963212L;

        Variable sender;

        public IAmYourAncestorMessage(Variable recipient, Variable sender) {
            super(recipient);
            this.sender = sender;
        }

        public Variable getSender() {
            return sender;
        }
    }

    private static class DynamicDecentralizedVariableOrderingMessage extends AgentMessage {

        private static final long serialVersionUID = -7215632982356283982L;

        public DynamicDecentralizedVariableOrderingMessage(Variable recipient) {
            super(recipient);
        }
    }

    private static class NewVarDoneMessage extends AgentMessage {

        private static final long serialVersionUID = 3238505865097109304L;

        Variable parent;

        Variable child;

        int q;

        int time;

        public NewVarDoneMessage(Variable parent, Variable child, int q, Variable recipient, int time) {
            super(recipient);
            this.parent = parent;
            this.child = child;
            this.q = q;
            this.time = time;
        }

        public int getTime() {
            return time;
        }

        public Variable getParent() {
            return parent;
        }

        public Variable getChild() {
            return child;
        }

        public int getNumNeighbors() {
            return q;
        }

        @Override
        public String toString() {
            return "NewVarDoneMessage(parent = \"" + parent + "\", child = \"" + child + "\", q = " + q + ", recipient = \"" + getRecipient() + "\", time = " + time + ")";
        }
    }

    private static class RemovePossibleChildMessage extends AgentMessage {

        private static final long serialVersionUID = 8737189704726289257L;

        Variable possibleChild;

        public RemovePossibleChildMessage(Variable possibleChild, Variable recipient) {
            super(recipient);
            this.possibleChild = possibleChild;
        }

        public Variable getPossibleChild() {
            return possibleChild;
        }
    }

    private static class NewVarReply extends AgentMessage {

        private static final long serialVersionUID = -6189095078157392582L;

        Variable sender;

        boolean pseudotree;

        int time;

        public NewVarReply(boolean hasPseudotree, Variable sender, Variable recipient, int time) {
            super(recipient);
            this.sender = sender;
            this.pseudotree = hasPseudotree;
            this.time = time;
        }

        public int getTime() {
            return time;
        }

        public Variable getSender() {
            return sender;
        }

        public boolean hasPseudotree() {
            return pseudotree;
        }

        @Override
        public String toString() {
            return "NewVarReply(" + pseudotree + ", " + sender + ", " + getRecipient() + ", " + time + ")";
        }
    }

    private static class AlreadyActiveMessage extends AgentMessage {

        private static final long serialVersionUID = -1863953432012405985L;

        Variable sender;

        boolean higherPriority;

        public AlreadyActiveMessage(Variable sender, Variable recipient, boolean higherPriority) {
            super(recipient);
            this.sender = sender;
            this.higherPriority = higherPriority;
        }

        public Variable getSender() {
            return sender;
        }

        public boolean isHigherPriority() {
            return higherPriority;
        }
    }

    private static class CancelSearchMessage extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 932376051059391068L;

        Variable newVar;

        public CancelSearchMessage(Variable recipient, Variable newVar) {
            super(recipient);
            this.newVar = newVar;
        }

        public Variable getNewVar() {
            return newVar;
        }
    }

    private static class AbortSearchMessage extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 4217970979903960537L;

        Variable newVar;

        public AbortSearchMessage(Variable recipient, Variable newVar) {
            super(recipient);
            this.newVar = newVar;
        }

        public Variable getNewVar() {
            return newVar;
        }
    }

    private static class AddRequest extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 8634747787995959419L;

        public AddRequest(Variable recipient) {
            super(recipient);
        }
    }

    private static class SetParentMessage extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 7656519120356630625L;

        Variable sender;

        public SetParentMessage(Variable sender, Variable recipient) {
            super(recipient);
            this.sender = sender;
        }

        public Variable getSender() {
            return sender;
        }

        @Override
        public String toString() {
            return "SetParentMessage(" + sender + " is the parent of " + getRecipient() + ")";
        }
    }

    private static class NewVarMessage extends DynamicDecentralizedVariableOrderingMessage {

        private static final long serialVersionUID = 6483120701717880838L;

        Variable newVar;

        Variable sender;

        int q;

        int time;

        public NewVarMessage(Variable newVar, int q, Variable sender, Variable recipient, int time) {
            super(recipient);
            this.newVar = newVar;
            this.sender = sender;
            this.q = q;
            this.time = time;
        }

        public int getTime() {
            return time;
        }

        public Variable getSender() {
            return sender;
        }

        public Variable getNewVariable() {
            return newVar;
        }

        public int getNumNeighbors() {
            return q;
        }

        @Override
        public String toString() {
            return "NewVarMessage(newVar = \"" + newVar + "\", q = " + q + ", sender = \"" + sender + "\", recipient = \"" + getRecipient() + "\", time = " + time + ")";
        }
    }

    private static class VariableComparator implements Comparator<Variable> {

        public VariableComparator() {
        }

        public int compare(Variable v1, Variable v2) {
            return v2.getName().compareTo(v1.getName());
        }
    }

    @SuppressWarnings("empty-statement")
    private boolean checkForAlreadyActiveMessages(HashSet<Variable> previouslySent, Variable newVar, long nextBackoff) {
        AlreadyActiveMessage aam = getNextPendingMessage(AlreadyActiveMessage.class);
        if (aam == null) return true;
        log(aam.getSender().toString() + " is already active for a " + (aam.isHigherPriority() ? "higher" : "lower") + " priority variable!");
        for (Variable ps : previouslySent) {
            if (ps.equals(aam.getSender())) continue;
            try {
                log("Sending CancelSearch message to " + ps + "...");
                sendMessage(new CancelSearchMessage(ps, newVar));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long sleepTime = (long) (0.25 * ((double) (new Random()).nextInt(Math.abs(newVar.hashCode()) % 1000) / 1000.0) * (double) nextBackoff);
        if (aam.isHigherPriority()) sleepTime += nextBackoff;
        log("Backing off for " + sleepTime + " ms...");
        try {
            getAgent().sleep(new org.sefirs.Milliseconds(sleepTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (getNextPendingMessage(RemovePossibleChildMessage.class) != null) ;
        while (getNextPendingMessage(AlreadyActiveMessage.class) != null) ;
        while (getNextPendingMessage(NewVarReply.class) != null) ;
        while (getNextPendingMessage(NewVarDoneMessage.class) != null) ;
        adding.remove(newVar);
        addVariable(newVar, nextBackoff * 2);
        return false;
    }

    public void addVariable(Variable newVar) {
        addVariable(newVar, 1);
    }

    void addVariable(Variable newVar, long nextBackoff) {
        if (adding.contains(newVar)) {
            log("We are already in the process of adding " + newVar + "!");
            return;
        }
        updateProperty("Next add variable backoff for " + newVar, "" + nextBackoff + "ms");
        adding.add(newVar);
        if (getAgent().getVariables().contains(newVar)) {
            for (Variable v : getAgent().getVariables()) {
                if (v.equals(newVar)) {
                    newVar = v;
                    break;
                }
            }
        }
        Integer time = logicTime.get(newVar);
        if (time == null) time = 0; else time = time.intValue() + 1;
        logicTime.put(newVar, time);
        DynamicVariableOrdering newNode = null;
        if (ordering != null) newNode = ordering.getNodeForVariable(newVar);
        if (newNode != null) {
            log("We are already a part of the pseudotree!");
            adding.remove(newVar);
            return;
        }
        try {
            int k = newVar.getNeighbors().size();
            int total = k;
            HashSet<Variable> possibleChildren = new HashSet<Variable>();
            int numPossibleChildren = 0;
            LinkedHashSet<Variable> varsWithoutPseudotrees = new LinkedHashSet<Variable>();
            TreeSet<Variable> sortedVars = new TreeSet<Variable>(new VariableComparator());
            sortedVars.addAll(newVar.getNeighbors());
            log("My neighbors are: " + sortedVars);
            HashSet<Variable> previouslySent = new HashSet<Variable>();
            for (Variable v : sortedVars) {
                log("Sending " + new NewVarMessage(newVar, k, newVar, v, time) + " to " + v);
                sendMessage(new NewVarMessage(newVar, k, newVar, v, time));
                previouslySent.add(v);
                for (; ; ) {
                    if (!checkForAlreadyActiveMessages(previouslySent, newVar, nextBackoff)) {
                        adding.remove(newVar);
                        return;
                    }
                    NewVarReply nvr = getNextPendingMessage(NewVarReply.class);
                    if (nvr == null || nvr.getTime() < time.intValue()) {
                        if (nvr != null) log("Discarding expired message: " + nvr);
                        try {
                            getAgent().sleep(new org.sefirs.Milliseconds(100));
                        } catch (Exception e) {
                        }
                        continue;
                    }
                    if (!nvr.getSender().equals(v)) {
                        log("Error: Assertion Failed: expected a NewVarReply from " + v + " but instead received one from " + nvr.getSender() + ": " + nvr);
                        System.exit(1);
                    }
                    if (!nvr.hasPseudotree()) {
                        log(nvr.getSender().toString() + " does not have a pseudotree!");
                        varsWithoutPseudotrees.add(nvr.getSender());
                        if (v.getName().compareTo(newVar.getName()) > 0) {
                            adding.remove(newVar);
                            log("We do not have the highest priorty name...");
                            for (Variable alreadySent : sortedVars) {
                                if (alreadySent.getName().compareTo(v.getName()) <= 0) break;
                                try {
                                    sendMessage(new AbortSearchMessage(alreadySent, newVar));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                log("Sending AddRequest to " + v);
                                sendMessage(new AddRequest(v));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            while (ordering == null || ordering.getNodeForVariable(newVar) == null) {
                                try {
                                    getAgent().sleep(new org.sefirs.Milliseconds(100));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            adding.remove(newVar);
                            return;
                        } else if (varsWithoutPseudotrees.size() == total) {
                            log("We have the highest priority name!");
                            if (ordering == null) ordering = newInstance(newVar); else setParent(ordering.getRoot(), newInstance(newVar));
                            ordering = ordering.getNodeForVariable(newVar);
                            adding.remove(newVar);
                            for (Variable v2 : sortedVars) {
                                if (ordering.getNodeForVariable(v2) == null) {
                                    try {
                                        log("Sending AddRequest to " + v2);
                                        sendMessage(new AddRequest(v2));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (ordering.getNodeForVariable(v2) == null) log("Waiting for " + v2 + "...");
                                while (ordering.getNodeForVariable(v2) == null) {
                                    try {
                                        getAgent().sleep(new org.sefirs.Milliseconds(1000));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                log(v2.toString() + " has added itself to our pseudotree!");
                            }
                            return;
                        }
                        k--;
                    }
                    break;
                }
            }
            if (newNode == null) newNode = newInstance(newVar);
            if (ordering == null) ordering = newNode;
            while (k > 0) {
                if (!checkForAlreadyActiveMessages(previouslySent, newVar, nextBackoff)) {
                    adding.remove(newVar);
                    return;
                }
                RemovePossibleChildMessage rpcm = getNextPendingMessage(RemovePossibleChildMessage.class);
                if (rpcm != null) {
                    possibleChildren.remove(rpcm.getPossibleChild());
                    numPossibleChildren--;
                }
                String qb4 = printMessageQueue();
                NewVarDoneMessage nvdm = getNextPendingMessage(NewVarDoneMessage.class);
                if (nvdm == null || nvdm.getTime() < time.intValue()) {
                    if (nvdm != null) log("Discarding expired message: " + nvdm);
                    try {
                        getAgent().sleep(new org.sefirs.Milliseconds(100));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                log("Received NVDM: " + nvdm);
                log("BEFORE: " + qb4 + "\nAFTER: " + printMessageQueue());
                if (nvdm.getParent() == null) {
                    log(nvdm.getChild().toString() + " proposes that we become the root.");
                    possibleChildren.add(nvdm.getChild());
                    numPossibleChildren++;
                } else {
                    possibleChildren.clear();
                    numPossibleChildren = 0;
                    k -= nvdm.getNumNeighbors();
                    DynamicVariableOrdering parent = ordering.getNodeForVariable(nvdm.getParent());
                    if (parent == null) parent = newInstance(nvdm.getParent());
                    setParent(newNode, parent);
                    if (nvdm.getChild() != null) {
                        DynamicVariableOrdering child = ordering.getNodeForVariable(nvdm.getChild());
                        if (child == null) child = newInstance(nvdm.getChild());
                        setParent(child, newNode);
                    }
                }
                log("k / total = " + k + " / " + total);
                log("possibleChildren = " + possibleChildren + " (#: " + numPossibleChildren + ")");
                if (numPossibleChildren >= k && !possibleChildren.isEmpty()) {
                    log("MAKING OURSELVES THE ROOT!");
                    for (Variable c : possibleChildren) {
                        DynamicVariableOrdering child = ordering.getNodeForVariable(c);
                        if (child == null) child = newInstance(c);
                        setParent(child, newNode);
                        log("Sending SetParentMessage to " + c);
                        sendMessage(new SetParentMessage(newVar, c));
                    }
                    for (Variable c : newVar.getNeighbors()) {
                        if (possibleChildren.contains(c)) continue;
                        log("Sending IAmYourAncestorMessage to " + c);
                        sendMessage(new IAmYourAncestorMessage(c, newVar));
                    }
                    k = 0;
                }
                ordering = newNode;
            }
            for (Variable v : varsWithoutPseudotrees) {
                if (newVar.getName().compareTo(v.getName()) > 0) {
                    try {
                        log("Sending AddRequest to " + v);
                        sendMessage(new AddRequest(v));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            adding.remove(newVar);
            throw new RuntimeException(ioe.getMessage());
        }
        adding.remove(newVar);
        log("Done adding new variable!  My ordering now is: " + ordering.getRoot());
    }

    @Override
    public void log(String message) {
        if (logicTime.size() == 1) {
            Variable v = null;
            for (Variable v2 : logicTime.keySet()) v = v2;
            super.log(logicTime.get(v).toString() + ": " + message);
        } else {
            String s = "";
            for (Variable v : logicTime.keySet()) s += "(" + v + ", " + logicTime.get(v) + ")";
            super.log(s + ": " + message);
        }
    }

    public synchronized DynamicVariableOrdering getCurrentOrdering() {
        return ordering;
    }

    private static class BlankAgent extends Agent {

        public BlankAgent(String name, String hostName, DisCOP problem, HashSet<Variable> variables, Algorithm algorithm, Platform<AgentIdentifier<HostIdentifier<PlatformIdentifier>>, HostIdentifier<PlatformIdentifier>, PlatformIdentifier> platform) {
            super(name, hostName, problem, variables, algorithm, platform);
            addPlugin(new DynamicDecentralizedVariableOrderingPlugin());
        }

        public void run() {
            while (isAlive()) {
                try {
                    sleep(new org.sefirs.Milliseconds(100));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    private static class BlankAlgorithm extends Algorithm<BlankAgent> {

        HashSet<BlankAgent> localAgents;

        public BlankAlgorithm() {
            localAgents = new HashSet<BlankAgent>();
        }

        public BlankAgent newAgent(String agentName, String hostName, DisCOP problem, HashSet<Variable> variables, Platform<AgentIdentifier<HostIdentifier<PlatformIdentifier>>, HostIdentifier<PlatformIdentifier>, PlatformIdentifier> platform) {
            BlankAgent ba = new BlankAgent(agentName, hostName, problem, variables, this, platform);
            localAgents.add(ba);
            return ba;
        }

        public Context getContext() {
            return new Context();
        }

        public HashSet<BlankAgent> getLocalAgents() {
            return localAgents;
        }

        public boolean isComplete() {
            return false;
        }

        public BlankAgent getLocalAgent(String agentName) {
            return null;
        }
    }

    private static class VariableAdder implements Runnable {

        BlankAgent ba;

        Variable v;

        boolean done;

        public VariableAdder(BlankAgent ba, Variable v) {
            this.ba = ba;
            this.v = v;
            done = false;
        }

        public boolean isDone() {
            return done;
        }

        public void run() {
            DynamicDecentralizedVariableOrderingPlugin ddvop = ba.getPluginByType(DynamicDecentralizedVariableOrderingPlugin.class);
            System.out.println("BEFORE ADDING " + v);
            ddvop.addVariable(v);
            System.out.println("AFTER ADDING " + v);
            done = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private static void ensureConsistency(Set<? extends Agent> agents) {
        boolean good = true;
        for (Agent a : agents) {
            for (Agent a2 : agents) {
                if (a == a2) continue;
                VariableOrdering o1 = a.getPluginByType(DynamicDecentralizedVariableOrderingPlugin.class).getCurrentOrdering();
                if (o1 == null) {
                    continue;
                }
                VariableOrdering o2 = a2.getPluginByType(DynamicDecentralizedVariableOrderingPlugin.class).getCurrentOrdering();
                if (o2 == null) {
                    continue;
                }
                if (!good) continue;
                if (!o1.isConsistent(o2)) {
                    System.out.println("Error: " + a + "'s variable ordering (" + o1 + ") is not consistent with " + a2 + "'s variable ordering (" + o2 + ")!");
                    good = false;
                }
            }
        }
        if (good) {
            System.out.println("All of the orderings are thus far consistent!");
        }
    }

    public static void main(String[] args) throws Exception {
        compareToDFSProbabilistically();
    }

    public static Tree buildHierarchy(Graph<GraphVertex> graph) {
        return buildHierarchy(graph, null, null);
    }

    private static Tree buildHierarchy(Graph<GraphVertex> graph, Statistics statistics) {
        return buildHierarchy(graph, null, statistics);
    }

    private static class Statistics {

        public long numMessages;

        public Statistics() {
            numMessages = 0;
        }
    }

    public static Tree buildHierarchy(Graph<GraphVertex> graph, Tree existingHierarchy) {
        return buildHierarchy(graph, existingHierarchy, null);
    }

    private static Tree buildHierarchy(Graph<GraphVertex> graph, Tree existingHierarchy, Statistics statistics) {
        Hashtable<Vertex, Tree> nodes = new Hashtable<Vertex, Tree>();
        Tree root = existingHierarchy;
        long numMessages = 0;
        if (root != null) {
            root = root.clone();
            int i = 0;
            for (Vertex v : graph.getVertices()) {
                if (i++ >= root.size()) break;
                nodes.put(v, root.getNodeByLabel(Integer.toString(i)));
            }
        }
        do {
            for (Vertex v : graph.getVertices()) {
                if (nodes.get(v) != null) continue;
                numMessages += 2 * v.getNeighbors().size();
                if (root == null) {
                    root = new Tree();
                    root.setLabel("1");
                    nodes.put(v, root);
                    continue;
                }
                HashSet<Vertex> inHierarchy = new HashSet<Vertex>();
                for (Vertex n : (Set<Vertex>) v.getNeighbors()) {
                    if (nodes.get(n) != null) inHierarchy.add(n);
                }
                if (inHierarchy.isEmpty()) continue;
                Hashtable<Tree, Integer> counts = new Hashtable<Tree, Integer>();
                Hashtable<Tree, HashSet<Tree>> receipts = new Hashtable<Tree, HashSet<Tree>>();
                Tree al = null;
                int minDepth = Integer.MAX_VALUE;
                int maxDepth = 0;
                for (Vertex n : inHierarchy) {
                    Tree t = nodes.get(n);
                    if (t.getDepth() < minDepth) minDepth = t.getDepth();
                    if (t.getDepth() > maxDepth) maxDepth = t.getDepth();
                    do {
                        if (counts.get(t) == null) counts.put(t, 1); else counts.put(t, counts.get(t) + 1);
                        if (counts.get(t) >= inHierarchy.size()) al = t;
                        Tree parent = t.getParent();
                        if (parent != null) {
                            HashSet<Tree> r = receipts.get(parent);
                            if (r == null) {
                                r = new HashSet<Tree>();
                                receipts.put(parent, r);
                            }
                            r.add(t);
                        }
                        t = parent;
                    } while (t != null && al == null);
                    if (al != null) break;
                }
                numMessages += maxDepth - minDepth + al.getDepth() - minDepth;
                if (receipts.get(al) == null || receipts.get(al).isEmpty()) {
                    nodes.put(v, new Tree(al));
                    nodes.get(v).setLabel(Integer.toString(nodes.size()));
                } else {
                    Tree node = new Tree(al.getParent());
                    if (node.isRoot()) root = node;
                    nodes.put(v, node);
                    node.setLabel(Integer.toString(nodes.size()));
                    al.setParent(node);
                }
            }
        } while (root.size() < graph.size());
        if (statistics != null) statistics.numMessages = numMessages;
        return root;
    }

    public static void compareToDFSProbabilistically() {
        for (double p = 0.1; p <= 1.0; p += 0.1) {
            for (int i = 1; i < 100; i++) {
                System.err.println("Working on " + i + " agents and p = " + p);
                compareToDFSProbabilistically(i, p);
            }
        }
    }

    public static void compareToDFSProbabilistically(int numAgents, double graphDensity) {
        long numInstances = 0;
        double dfsTotal = 0;
        double myTotal = 0;
        long numTimesDFSwasWorse = 0;
        double p = graphDensity;
        Random random = new Random();
        long dfsInducedWidthTotal = 0;
        long myInducedWidthTotal = 0;
        double dfsAverageDepth = 0;
        double myAverageDepth = 0;
        long myNumMessages = 0;
        long myNumMessagesDelta = 0;
        for (int i = 0; i < Math.min(Math.pow(2, numAgents), 100); i++) {
            boolean adj[][] = Graph.randomConnectedGraph(numAgents, p, random);
            Graph<GraphVertex> interactionGraph = Graph.newInstance(adj);
            Statistics stats = new Statistics();
            Tree ourFirstTree = buildHierarchy(interactionGraph, stats);
            Tree dfsFirstTree = interactionGraph.getSpanningTree();
            myNumMessages += stats.numMessages;
            dfsInducedWidthTotal += dfsFirstTree.calculateInducedWidth();
            myInducedWidthTotal += ourFirstTree.calculateInducedWidth();
            dfsAverageDepth += dfsFirstTree.getAverageDepth();
            myAverageDepth += ourFirstTree.getAverageDepth();
            boolean newAdj[][] = new boolean[numAgents + 1][numAgents + 1];
            for (int row = 0; row < numAgents; row++) for (int col = 0; col < numAgents; col++) newAdj[row][col] = adj[row][col];
            boolean addedLink = false;
            for (int j = 0; j < numAgents; j++) {
                if (j == numAgents - 1 && !addedLink) newAdj[numAgents][j] = true; else {
                    newAdj[numAgents][j] = (random.nextDouble() <= p);
                    if (newAdj[numAgents][j]) addedLink = true;
                }
                newAdj[j][numAgents] = newAdj[numAgents][j];
            }
            Graph<GraphVertex> nextGraph = Graph.newInstance(newAdj);
            numInstances++;
            stats = new Statistics();
            Tree ourNextTree = buildHierarchy(nextGraph, ourFirstTree, stats);
            Tree dfsNextTree = nextGraph.getSpanningTree();
            myNumMessagesDelta += stats.numMessages;
            int dfsDistance = dfsFirstTree.calculateEditDistance(dfsNextTree);
            int ourDistance = ourFirstTree.calculateEditDistance(ourNextTree);
            if (dfsDistance > ourDistance) numTimesDFSwasWorse++;
            myTotal += ourDistance;
            dfsTotal += dfsDistance;
        }
        System.out.println(Integer.toString(numAgents) + "\t" + p + "\t" + Double.toString((double) dfsTotal / (double) numInstances) + "\t" + Double.toString((double) myTotal / (double) numInstances) + "\t" + Double.toString((double) dfsInducedWidthTotal / (double) numInstances) + "\t" + Double.toString((double) myInducedWidthTotal / (double) numInstances) + "\t" + Double.toString((double) dfsAverageDepth / (double) numInstances) + "\t" + Double.toString((double) myAverageDepth / (double) numInstances) + "\t" + Double.toString((double) myNumMessages / (double) numInstances) + "\t" + Double.toString((double) myNumMessagesDelta / (double) numInstances));
    }

    public static void compareToDFS() {
        for (int i = 1; i < 8; i++) {
            if (i > 1) System.out.print(", ");
            compareToDFS(i);
        }
        System.out.println("");
    }

    public static void compareToDFS(int numAgents) {
        long numInstances = 0;
        double dfsTotal = 0;
        double myTotal = 0;
        long numTimesDFSwasWorse = 0;
        for (Graph<GraphVertex> interactionGraph : new ConnectedUndirectedGraphGenerator(numAgents)) {
            Tree ourFirstTree = buildHierarchy(interactionGraph);
            Tree dfsFirstTree = interactionGraph.getSpanningTree();
            for (Graph<GraphVertex> nextGraph : new AdditionalVertexGenerator(interactionGraph)) {
                numInstances++;
                Tree ourNextTree = buildHierarchy(nextGraph, ourFirstTree);
                Tree dfsNextTree = nextGraph.getSpanningTree();
                int dfsDistance = dfsFirstTree.calculateEditDistance(dfsNextTree);
                int ourDistance = ourFirstTree.calculateEditDistance(ourNextTree);
                if (ourDistance > 2 || ourDistance <= 0) {
                    System.out.println("interactionGraph:\n" + interactionGraph);
                    System.out.println("nextGraph:\n" + nextGraph);
                    System.out.println("ourFirstTree: " + ourFirstTree);
                    System.out.println("ourNextTree: " + ourNextTree);
                    System.out.println("editDistance: " + ourFirstTree.calculateEditDistance(ourNextTree, true));
                    System.exit(1);
                }
                if (dfsDistance > ourDistance) numTimesDFSwasWorse++;
                myTotal += ourDistance;
                dfsTotal += dfsDistance;
            }
        }
        System.out.print(Double.toString(dfsTotal / (double) numInstances) + "/" + Double.toString(myTotal / (double) numInstances) + "/" + ((double) numTimesDFSwasWorse / (double) numInstances));
    }

    public static void testUsingTCPPlatform() throws Exception {
        Platform platform = org.dcopolis.platform.tcp.TCPPlatform.newInstance();
        String resource = System.getProperty("PROBLEM_FILE", "").trim();
        DisCOP problem = null;
        if (!resource.equals("")) {
            try {
                DCOPInterpreter di = new DCOPInterpreter(new File(resource));
                problem = di.getDCOP();
            } catch (Exception e) {
                throw new RuntimeException("Error parsing DisCOP " + resource + ": " + e);
            }
        } else problem = new org.dcopolis.problem.coloring.RandomGraphColoringProblem();
        BlankAlgorithm algo = new BlankAlgorithm();
        org.dcopolis.DCOPolis dcopolis = new org.dcopolis.DCOPolis(problem, algo, platform);
        for (BlankAgent ba : algo.getLocalAgents()) for (Variable v : ba.getVariables()) System.out.println(v.toString() + "'s neighbors: " + v.getNeighbors());
        dcopolis.startSolving();
        for (BlankAgent ba : algo.getLocalAgents()) {
            for (Variable v : ba.getVariables()) {
                VariableAdder va = new VariableAdder(ba, v);
                synchronized (va) {
                    ba.addRoutine(va);
                    try {
                        va.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ensureConsistency(algo.getLocalAgents());
                printOrderings(algo);
            }
        }
        System.out.println("The final variable orderings are:");
        printOrderings(algo);
    }

    private static void printOrderings(BlankAlgorithm algo) {
        for (BlankAgent ba : algo.getLocalAgents()) {
            VariableOrdering vo = ba.getPluginByType(DynamicDecentralizedVariableOrderingPlugin.class).getCurrentOrdering();
            System.out.println(ba.toString() + ": " + (vo == null ? "null" : vo.getRoot()));
        }
        Hashtable<Variable, Tree> trees = new Hashtable<Variable, Tree>();
        HashSet<Tree> roots = new HashSet<Tree>();
        for (BlankAgent ba : algo.getLocalAgents()) {
            VariableOrdering vo2 = ba.getPluginByType(DynamicDecentralizedVariableOrderingPlugin.class).getCurrentOrdering();
            if (vo2 == null) continue;
            for (VariableOrdering vo : vo2.getRoot()) getTree(trees, roots, (DynamicVariableOrdering) vo);
        }
        for (Tree root : roots) System.out.println(root.toString());
    }

    private static Tree getTree(Hashtable<Variable, Tree> trees, HashSet<Tree> roots, DynamicVariableOrdering vo) {
        Variable v = vo.getVariable();
        Tree t = trees.get(v);
        if (t == null) {
            if (vo.getParent() == null) {
                t = new Tree();
                roots.add(t);
            } else {
                t = new Tree(getTree(trees, roots, vo.getParent()));
                roots.remove(t);
            }
            t.setLabel(v.toString());
            trees.put(v, t);
        } else if (vo.getParent() != null && t.getParent() == null) {
            t.setParent(getTree(trees, roots, vo.getParent()));
            roots.remove(t);
        }
        return t;
    }
}
