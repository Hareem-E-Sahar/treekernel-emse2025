package org.processmining.mining.partialorderminingTimeUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.partialordermining.PartialOrderMiningResult;

public class PartialOrderMiningTimeUnitPlugin implements MiningPlugin {

    private PartialOrderMiningUI ui = null;

    private HashMap boxesCR = null;

    private Progress progress = null;

    public void PartialOrderMiningTimeUnitPlugin() {
    }

    public String getHtmlDescription() {
        return null;
    }

    public String getName() {
        return "Partial Order Mining TimeUnit";
    }

    public JPanel getOptionsPanel(LogSummary summary) {
        if (ui == null) {
            ui = new PartialOrderMiningUI();
        }
        return ui;
    }

    public synchronized MiningResult mine(LogReader log) {
        progress = new Progress("Progress:");
        progress.setMaximum(100);
        boxesCR = new HashMap();
        for (int i = 0; i < log.getInstances().size(); i++) {
            boxesCR.put(log.getInstance(i), new ArrayList());
        }
        return minePoTimeUnit(log, progress);
    }

    public MiningResult minePoTimeUnit(LogReader log, Progress progress) {
        if (!(log instanceof BufferedLogReader)) {
            Message.add("This Log-reader is READ-ONLY, so the partial order will not be stored." + " Please select another Log-Reader under help.");
        }
        Iterator<ProcessInstance> it = log.instanceIterator();
        while (it.hasNext()) {
            ProcessInstance pi = it.next();
            Iterator<AuditTrailEntry> itate = pi.getAuditTrailEntryList().iterator();
            int counter = 0;
            long curDateMill = -1;
            ArrayList currentBox = null;
            while (itate.hasNext()) {
                AuditTrailEntry ate = itate.next();
                long dateMillAte = ate.getTimestamp().getTime() / ui.getSelectedTimeUnit().getConversionValue();
                if (curDateMill == dateMillAte) {
                    currentBox.add(new AtePositionPiTuple(ate, counter));
                } else if (dateMillAte > curDateMill) {
                    ArrayList newBox = new ArrayList();
                    currentBox = newBox;
                    newBox.add(new AtePositionPiTuple(ate, counter));
                    ((ArrayList) boxesCR.get(pi)).add(currentBox);
                    curDateMill = dateMillAte;
                } else {
                    new Exception("The ates in the log are not ordered on timestamp!");
                }
                counter++;
            }
        }
        progress.setNote("Finished determining partial orders");
        progress.setProgress(50);
        int overwriteExisting = -1;
        ArrayList a = new ArrayList();
        Iterator<ProcessInstance> keysIt = boxesCR.keySet().iterator();
        while (keysIt.hasNext()) {
            ProcessInstance pi = keysIt.next();
            a.add(pi.getName());
            if (pi.getAttributes().containsKey(ProcessInstance.ATT_PI_PO) && pi.getAttributes().get(ProcessInstance.ATT_PI_PO).equals("true") && (overwriteExisting < 1)) {
                if (overwriteExisting == 0) {
                    continue;
                }
                int i = JOptionPane.showConfirmDialog(MainUI.getInstance(), "This log already contains partially ordered process instances. \n" + "Should this information be overwritten?\n" + "Click YES to overwrite the partial order with a new partial order, \n" + "click NO  to skip all process instances that already contain a partial order.", "Warning", JOptionPane.YES_NO_OPTION);
                if (i == JOptionPane.NO_OPTION) {
                    overwriteExisting = 0;
                    continue;
                }
                if (i == JOptionPane.YES_OPTION) {
                    overwriteExisting = 1;
                }
            }
            pi.setAttribute(ProcessInstance.ATT_PI_PO, "true");
            progress.setNote(pi.getName());
            progress.inc();
            Iterator<ArrayList> boxesIt = ((ArrayList) boxesCR.get(pi)).iterator();
            while (boxesIt.hasNext()) {
                ArrayList box = boxesIt.next();
                Iterator<AtePositionPiTuple> boxIt = box.iterator();
                while (boxIt.hasNext()) {
                    AtePositionPiTuple atePos = boxIt.next();
                    atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_ID, "id" + atePos.getPositionPi());
                    atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_POST, "");
                    atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_PRE, "");
                    try {
                        pi.getAuditTrailEntryList().replace(atePos.getAte(), atePos.getPositionPi());
                    } catch (IOException ex3) {
                    } catch (IndexOutOfBoundsException ex3) {
                    }
                }
            }
            int maxIndex = pi.getAuditTrailEntryList().size();
            for (int k = 0; k < maxIndex; k++) {
                AuditTrailEntry ate = null;
                try {
                    ate = pi.getAuditTrailEntryList().get(k);
                    ate.setAttribute(ProcessInstance.ATT_ATE_ID, "id" + k);
                    ate.setAttribute(ProcessInstance.ATT_ATE_POST, "");
                    ate.setAttribute(ProcessInstance.ATT_ATE_PRE, "");
                    pi.getAuditTrailEntryList().replace(ate, k);
                } catch (IOException ex3) {
                } catch (IndexOutOfBoundsException ex3) {
                }
            }
            ArrayList boxes = (ArrayList) boxesCR.get(pi);
            for (int i = 0; i < boxes.size(); i++) {
                ArrayList<AtePositionPiTuple> currentBox = (ArrayList) boxes.get(i);
                if (i > 0) {
                    ArrayList<AtePositionPiTuple> preBox = (ArrayList) boxes.get(i - 1);
                    for (int j = 0; j < currentBox.size(); j++) {
                        for (int k = 0; k < preBox.size(); k++) {
                            AuditTrailEntry curBoxAte = currentBox.get(j).getAte();
                            AuditTrailEntry preBoxAte = preBox.get(k).getAte();
                            String pre = curBoxAte.getAttributes().get(ProcessInstance.ATT_ATE_PRE);
                            pre += (pre.length() == 0 ? "" : ",") + preBoxAte.getAttributes().get(ProcessInstance.ATT_ATE_ID);
                            curBoxAte.setAttribute(ProcessInstance.ATT_ATE_PRE, pre);
                            try {
                                pi.getAuditTrailEntryList().replace(curBoxAte, currentBox.get(j).getPositionPi());
                            } catch (IOException ex4) {
                            } catch (IndexOutOfBoundsException ex4) {
                            }
                        }
                    }
                }
                if (i + 1 < boxes.size()) {
                    ArrayList<AtePositionPiTuple> postBox = (ArrayList) boxes.get(i + 1);
                    for (int j = 0; j < currentBox.size(); j++) {
                        for (int k = 0; k < postBox.size(); k++) {
                            AuditTrailEntry curBoxAte = currentBox.get(j).getAte();
                            AuditTrailEntry postBoxAte = postBox.get(k).getAte();
                            String post = curBoxAte.getAttributes().get(ProcessInstance.ATT_ATE_POST);
                            post += (post.length() == 0 ? "" : ",") + postBoxAte.getAttributes().get(ProcessInstance.ATT_ATE_ID);
                            curBoxAte.setAttribute(ProcessInstance.ATT_ATE_POST, post);
                            try {
                                pi.getAuditTrailEntryList().replace(curBoxAte, currentBox.get(j).getPositionPi());
                            } catch (IOException ex4) {
                            } catch (IndexOutOfBoundsException ex4) {
                            }
                        }
                    }
                }
                if (ui.isParOptionChecked()) {
                    for (int l = 0; l < currentBox.size(); l++) {
                        for (int m = 0; m < currentBox.size(); m++) {
                            AtePositionPiTuple firstAtePos = currentBox.get(l);
                            AtePositionPiTuple secondAtePos = currentBox.get(m);
                            if (firstAtePos != secondAtePos) {
                                String pre = firstAtePos.getAte().getAttributes().get(ProcessInstance.ATT_ATE_PRE);
                                pre += (pre.length() == 0 ? "" : ",") + secondAtePos.getAte().getAttributes().get(ProcessInstance.ATT_ATE_ID);
                                firstAtePos.getAte().setAttribute(ProcessInstance.ATT_ATE_PRE, pre);
                                try {
                                    pi.getAuditTrailEntryList().replace(firstAtePos.getAte(), firstAtePos.getPositionPi());
                                } catch (IOException ex4) {
                                } catch (IndexOutOfBoundsException ex4) {
                                }
                                String post = firstAtePos.getAte().getAttributes().get(ProcessInstance.ATT_ATE_POST);
                                post += (post.length() == 0 ? "" : ",") + secondAtePos.getAte().getAttributes().get(ProcessInstance.ATT_ATE_ID);
                                firstAtePos.getAte().setAttribute(ProcessInstance.ATT_ATE_POST, post);
                                try {
                                    pi.getAuditTrailEntryList().replace(firstAtePos.getAte(), firstAtePos.getPositionPi());
                                } catch (IOException ex4) {
                                } catch (IndexOutOfBoundsException ex4) {
                                }
                            }
                        }
                    }
                }
            }
        }
        progress.setProgress(100);
        progress.close();
        return new PartialOrderMiningResult(log, a);
    }
}
