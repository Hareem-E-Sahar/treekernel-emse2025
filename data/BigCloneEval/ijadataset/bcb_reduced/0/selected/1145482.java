package net.emotivecloud.scheduler.goiri.scheduling.bdim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import net.emotivecloud.scheduler.goiri.SimulatorScheduler;
import net.emotivecloud.scheduler.goiri.SimulatorWrapper;
import net.emotivecloud.scheduler.goiri.VM;
import net.emotivecloud.scheduler.goiri.SimulatorScheduler.Node;
import net.emotivecloud.scheduler.goiri.scheduling.Provider;
import net.emotivecloud.scheduler.goiri.scheduling.State;

/**
 * Profit-Eco-Efficient Business-driven policy
 * @author fito
 *
 */
public class ProfEcoEfficient_old extends SimulatorScheduler {

    private HashMap<String, Float> PRICE_KWH;

    private float PRICE_KWH_MIN;

    private static float POWER_IDLE = 230.0F;

    private static float POWER_1CPU = 259.0F;

    private static float POWER_2CPU = 273.0F;

    private static float POWER_3CPU = 291.0F;

    private static float POWER_FULL = 304.0F;

    private static float PRICE_FIXH = (float) ((4000.0 / (4 * 365 * 24)) + (0.1 * 2000 / (10 * 365 * 24)));

    private static float GOLD = 1.0F;

    private static float SILVER = 0.5F;

    private static float BRONZE = 0.25F;

    private static short RENEW = 1;

    private static short NON_RENEW = 0;

    private static float PRICE_EC2_EU = 0.19F;

    private static float PRICE_EC2_US = 0.17F;

    private static float NODES_CAPACITY = 4.0F;

    private static float PENALIZATION_NONRENEWABLE = 400.0F;

    private static float PENALIZATION_SLA = 100.0F;

    protected boolean outsourcing;

    protected ListOrderedMap providers;

    private float PROFIT_WEIGHT;

    private float ENERGY_WEIGHT;

    private int profit_w;

    private int energy_w;

    public ProfEcoEfficient_old() {
        super();
        providers = new ListOrderedMap();
        PRICE_KWH = new HashMap<String, Float>();
        PRICE_KWH.put("GRID_EUROPEAN", 0.111F);
        PRICE_KWH.put("GRID_SPAIN", 0.084F);
        PRICE_KWH.put("GRID_UK", 0.101F);
        PRICE_KWH.put("GRID_SWEDEN", 0.063F);
        PRICE_KWH.put("RENEW_SPAIN", 0.33F);
        PRICE_KWH_MIN = Collections.min(PRICE_KWH.values());
        try {
            PropertiesConfiguration conf = new PropertiesConfiguration("/etc/scheduler.properties");
            outsourcing = conf.getBoolean("outsourcing", false);
            profit_w = conf.getInt("profit.weight");
            energy_w = conf.getInt("energy.weight");
            PROFIT_WEIGHT = (float) profit_w / 100;
            ENERGY_WEIGHT = (float) energy_w / 100;
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println("Virtualization scheduler parameters:");
        System.out.println("     Outsourcing " + outsourcing);
        System.out.println("     PRICE_FIXH  " + PRICE_FIXH);
        System.out.println("     PRICE_KWH_MIN  " + PRICE_KWH_MIN);
        System.out.println("     PROFIT WEIGHT  " + PROFIT_WEIGHT);
        System.out.println("     ENERGY WEIGHT  " + ENERGY_WEIGHT);
    }

    public void submitVM(VM vm) {
        vm.timeSubmit = this.t;
        if (this.vms.containsKey(vm.id)) {
            System.err.println("VM \"" + vm.id + "\" was already submitted");
        } else {
            this.vms.put(vm.id, vm);
            this.vmsQueue.add(vm.id);
        }
        this.schedule();
    }

    public void finishVM(String vmId) {
        System.out.println(" <--- Finishining vm=" + vmId);
        if (!this.vms.containsKey(vmId)) {
            System.err.println("VM \"" + vmId + "\" was not scheduled");
        } else {
            int pos = this.vms.indexOf(vmId);
            String nodeId = vmNode.get(vmId);
            if (nodes.containsKey(nodeId)) {
                Node node = nodes.get(nodeId);
                node.remove(vmId);
                this.vms.remove(pos);
            }
        }
        super.schedule();
    }

    private float profitUtility(double revenue, float price, float loss, Node node) {
        float prof = 0.0F;
        System.out.println("REVENUE = " + revenue);
        if (node == null) {
            if (revenue == GOLD) {
                prof = GOLD;
            } else if (revenue == SILVER) {
                prof = SILVER;
            } else {
                prof = BRONZE * 0.65F;
            }
        } else if (node.isRenewable()) {
            if (revenue == GOLD) {
                prof = GOLD * 0.75F;
            } else if (revenue == SILVER) {
                prof = SILVER * 0.75F;
            } else {
                prof = BRONZE * 0.75F;
            }
        } else {
            if (revenue == GOLD) {
                prof = GOLD * 0.85F;
            } else if (revenue == SILVER) {
                prof = SILVER * 0.85F;
            } else {
                prof = BRONZE * 0.85F;
            }
        }
        return prof;
    }

    private float energyUtility(Node node, VM vm) {
        int renew_ene = 0;
        int nonrenew_ene = 0;
        float dornw = 0;
        float eneff = 0;
        float ecoeff = 0;
        LinkedList<Integer> renewableHosts = new LinkedList<Integer>();
        LinkedList<Integer> nonRenewableHosts = new LinkedList<Integer>();
        float[] hostsAvailable = new float[onNodes.size()];
        for (int i = 0; i < onNodes.size(); i++) {
            String nodeId = this.onNodes.get(i);
            Node host = (Node) nodes.get(nodeId);
            if (host.isRenewable()) {
                renewableHosts.add(i);
                renew_ene += 4 - host.getAvailableCPU() * host.getCapacityCPU() / 100;
            } else {
                nonRenewableHosts.add(i);
                nonrenew_ene += 4 - host.getAvailableCPU() * host.getCapacityCPU() / 100;
            }
        }
        if (nonrenew_ene == 0 && renew_ene != 0) {
            dornw = 1.0F;
            eneff = (40 - (renew_ene + nonrenew_ene)) / 40;
        } else if (nonrenew_ene == 0 && renew_ene == 0) {
            dornw = 0.0F;
            eneff = 1.0F;
        } else {
            dornw = 1 - (nonrenew_ene / renew_ene);
            eneff = (40 - (renew_ene + nonrenew_ene)) / 40;
        }
        ecoeff = (dornw + eneff) / 2;
        System.out.println("VM ID = " + vm.id + "; Actual RNW CAP = " + renew_ene + ";NON RNW CAP = " + nonrenew_ene + "; DoRNW = " + dornw);
        System.out.println("Actual EnEff = " + eneff);
        if (node == null) {
            System.out.println("OUT - VM ID = " + vm.id);
            if (dornw == 0) dornw = 0.0F; else dornw = dornw * 0.9F;
            ecoeff = (dornw + eneff) / 2;
            return ecoeff;
        } else {
            if (node.isRenewable()) {
                System.out.println("LOCAL RNW - VM ID = " + vm.id);
                if (nonrenew_ene == 0 && dornw != 0) dornw = dornw * 1.1F; else if (nonrenew_ene == 0 && dornw == 0) dornw = 1.0F; else dornw = 1 - (nonrenew_ene / renew_ene++);
                eneff = (40 - (renew_ene++ + nonrenew_ene)) / 40;
            } else {
                System.out.println("LOCAL NON RNW - VM ID = " + vm.id);
                if (nonrenew_ene == 0 && dornw != 0) dornw = dornw * 0.5F; else if (nonrenew_ene == 0 && dornw == 0) dornw = 0.0F; else dornw = 1 - (nonrenew_ene++ / nonrenew_ene);
                eneff = (40 - (renew_ene + nonrenew_ene++)) / 40;
            }
            ecoeff = (dornw + eneff) / 2;
        }
        System.out.println("Future DoRNW = " + dornw);
        System.out.println("Future EnEff = " + eneff);
        return ecoeff;
    }

    public void schedule() {
        boolean outsource = false;
        int pos_prov = 0;
        Iterator<String> itVM = vmsQueue.iterator();
        LinkedList<String> localQueue = new LinkedList<String>();
        Iterator<String> itLocalVM = localQueue.iterator();
        int newVMs = vmsQueue.size();
        int numLocalNodes = onNodes.size();
        int numNodes = numLocalNodes + providers.size();
        int sched_options = 2 + this.providers.size();
        float ben_util[] = new float[sched_options];
        float prices[] = new float[sched_options];
        float loss[] = new float[sched_options];
        float ene_util[] = new float[sched_options];
        float watts[] = new float[sched_options];
        float total_util[] = new float[sched_options];
        double revenue = 0.0F;
        if (itVM.hasNext()) {
            String vmId1 = itVM.next();
            itVM.remove();
            int posVM1 = this.vms.indexOf(vmId1);
            VM vm = (VM) this.vms.getValue(posVM1);
            revenue = vm.revenue;
            float[] hostsAvailable = new float[onNodes.size()];
            for (int i = 0; i < onNodes.size(); i++) {
                String nodeId = this.onNodes.get(i);
                Node host = (Node) nodes.get(nodeId);
                hostsAvailable[i] = host.getAvailableAfterScheduling(vm);
            }
            LinkedList<Integer> selectedHosts = new LinkedList<Integer>();
            for (int i = 0; i < hostsAvailable.length; i++) {
                if (hostsAvailable[i] >= 0.0) {
                    selectedHosts.add(i);
                }
            }
            LinkedList<Integer> renewableHosts = new LinkedList<Integer>();
            LinkedList<Integer> nonRenewableHosts = new LinkedList<Integer>();
            for (int i = 0; i < hostsAvailable.length; i++) {
                String nodeId = this.onNodes.get(i);
                Node host = (Node) nodes.get(nodeId);
                if (host.isRenewable()) {
                    renewableHosts.add(i);
                } else {
                    nonRenewableHosts.add(i);
                }
            }
            prices[0] = PRICE_FIXH + (PRICE_KWH.get("RENEW_SPAIN") * (POWER_FULL / 1000));
            loss[0] = (float) (revenue * 0.1);
            boolean found = false;
            for (int i = 0; i < renewableHosts.size() && !found; i++) {
                String nodeId = this.onNodes.get(renewableHosts.get(i));
                Node host = (Node) nodes.get(nodeId);
                System.out.println("HOST ID = " + host.id + "; avail CPU = " + host.getAvailableCPU());
                if (host.getAvailableCPU() != 0) {
                    ben_util[0] = (float) this.profitUtility(revenue, prices[0], loss[0], host);
                    ene_util[0] = this.energyUtility(host, vm);
                    found = true;
                }
            }
            prices[1] = PRICE_FIXH + (PRICE_KWH.get("GRID_SWEDEN") * (POWER_FULL / 1000));
            loss[1] = (float) (revenue * 0.1);
            found = false;
            for (int i = 0; i < nonRenewableHosts.size() && !found; i++) {
                String nodeId2 = this.onNodes.get(nonRenewableHosts.get(i));
                Node host2 = (Node) nodes.get(nodeId2);
                if (host2.getAvailableCPU() != 0) {
                    ben_util[1] = (float) this.profitUtility(revenue, prices[1], loss[1], host2);
                    ene_util[1] = this.energyUtility(host2, vm);
                    found = true;
                }
            }
            for (int p = 2; p < providers.size() + 2; p++) {
                Provider prov_extern = (Provider) this.providers.getValue(p - 2);
                prices[p] = prov_extern.price * NODES_CAPACITY;
                loss[p] = 0.0F;
                ben_util[p] = (float) this.profitUtility(revenue, prices[p], loss[p], null);
                ene_util[p] = this.energyUtility(null, vm);
            }
            float maximum = Float.MIN_VALUE;
            int pos_maximum = 0;
            for (int p = 0; p < ben_util.length && p < ene_util.length; p++) {
                total_util[p] = (ben_util[p] * PROFIT_WEIGHT) + (ene_util[p] * ENERGY_WEIGHT);
                System.out.println("************ PROF UTIL = " + ben_util[p] + "; ENERGY UTIL = " + ene_util[p]);
                System.out.println("************ TOTAL UTIL = " + total_util[p]);
                if (total_util[p] > maximum) {
                    maximum = total_util[p];
                    pos_maximum = p;
                }
            }
            System.out.println("VM = " + vm.id + "; pos MAX = " + pos_maximum);
            if (pos_maximum <= 1) {
                localQueue.add(vmId1);
                this.backfilling(localQueue);
            } else {
                outsource = true;
                pos_prov = pos_maximum - 1;
                this.assignProvider(posVM1, numNodes - numLocalNodes - pos_prov);
            }
            if (outsource) this.performPowerOnOff();
        }
    }

    public void backfilling(LinkedList<String> VMs) {
        Iterator<String> itVM = VMs.iterator();
        while (itVM.hasNext()) {
            String vmId = itVM.next();
            int posVM = this.vms.indexOf(vmId);
            VM vm = (VM) this.vms.getValue(posVM);
            float[] hostsAvailable = new float[onNodes.size()];
            for (int i = 0; i < onNodes.size(); i++) {
                String nodeId = this.onNodes.get(i);
                Node host = (Node) nodes.get(nodeId);
                hostsAvailable[i] = host.getAvailableAfterScheduling(vm);
            }
            LinkedList<Integer> selectedHosts = new LinkedList<Integer>();
            for (int i = 0; i < hostsAvailable.length; i++) {
                if (hostsAvailable[i] >= 0.0) {
                    selectedHosts.add(i);
                }
            }
            LinkedList<Integer> renewableHosts = new LinkedList<Integer>();
            for (int i = 0; i < hostsAvailable.length; i++) {
                String nodeId = this.onNodes.get(i);
                Node host = (Node) nodes.get(nodeId);
                if (host.isRenewable()) {
                    renewableHosts.add(i);
                }
            }
            float[] renewableAvailable = new float[renewableHosts.size()];
            for (int i = 0; i < renewableHosts.size(); i++) {
                String nodeId = this.onNodes.get(renewableHosts.get(i));
                Node host = (Node) nodes.get(nodeId);
                renewableAvailable[i] = host.getAvailableAfterScheduling(vm);
            }
            boolean found = false;
            if (renewableHosts.size() > 0) {
                int selected = renewableHosts.get(0);
                for (Integer i : renewableHosts) {
                    if (renewableAvailable[i] < 0) selected = i + 1; else if (renewableAvailable[i] < renewableAvailable[selected]) {
                        found = true;
                        selected = i;
                    }
                }
                if (found) {
                    String nodeId = onNodes.get(selected);
                    this.assignNode(posVM, selected, State.CREATE);
                    this.addCreateEvent(vmId, nodeId);
                } else if (selectedHosts.size() > 0 && !found) {
                    int selected2 = selectedHosts.get(0);
                    for (Integer i : selectedHosts) {
                        if (hostsAvailable[i] < hostsAvailable[selected2]) {
                            selected2 = i;
                        }
                    }
                    String nodeId = onNodes.get(selected2);
                    this.assignNode(posVM, selected2, State.CREATE);
                    this.addCreateEvent(vmId, nodeId);
                } else {
                    this.assignProvider(posVM, 0);
                }
            }
            itVM.remove();
        }
    }

    protected void performPowerOnOff() {
        boolean finished = false;
        while (!finished) {
            float activeNodes = 0;
            for (int i = 0; i < onNodes.size(); i++) {
                String nodeId = onNodes.get(i);
                Node n = nodes.get(nodeId);
                if (n.isWorking()) {
                    activeNodes++;
                }
            }
            float factor = (activeNodes) / (onNodes.size() + startingNodes.size());
            if (powerOnOffType.equalsIgnoreCase("threshold")) {
                if (factor > powerOnOffLambdaMax || (onNodes.size() + startingNodes.size()) < powerOnfOffNodeMin) {
                    String nodeId = powerOnNode();
                    if (nodeId == null) {
                        finished = true;
                    }
                } else if (factor < powerOnOffLambdaMin && onNodes.size() > powerOnfOffNodeMin) {
                    String nodeId = powerOffNode();
                    if (nodeId == null) {
                        finished = true;
                    }
                } else {
                    finished = true;
                }
            } else {
                if ((onNodes.size() + startingNodes.size() - activeNodes) < powerOnfOffNodeMin) {
                    String nodeId = powerOnNode();
                    if (nodeId == null) {
                        finished = true;
                    }
                } else if ((onNodes.size() + startingNodes.size() - activeNodes) > powerOnfOffNodeMin) {
                    String nodeId = powerOffNode();
                    if (nodeId == null) {
                        finished = true;
                    }
                } else {
                    finished = true;
                }
            }
        }
    }

    /**
	 * Power on a node
	 */
    protected String powerOnNode() {
        String nodeId = null;
        float score = -Float.MAX_VALUE;
        boolean found = false;
        for (int i = 0; i < this.offNodes.size() && !found; i++) {
            String aux = this.offNodes.get(i);
            float auxScore = this.getNodeScoreRenewable(aux);
            if (auxScore > score) {
                nodeId = aux;
                score = auxScore;
            }
            if (!powerOnOffSmart && nodeId != null) {
                found = true;
            }
        }
        if (nodeId != null) {
            System.out.println("Add node: " + nodeId + " score=" + score);
            this.offNodes.remove(nodeId);
            this.startingNodes.add(nodeId);
            this.addNodeEvent(nodeId, true);
        }
        return nodeId;
    }

    /**
	 * Power off a node
	 */
    protected String powerOffNode() {
        String nodeId = null;
        float score = Float.MAX_VALUE;
        boolean found = false;
        for (int i = 0; i < this.onNodes.size() && !found; i++) {
            String aux = this.onNodes.get(i);
            Node auxNode = this.nodes.get(aux);
            if (!auxNode.isWorking()) {
                float auxScore = this.getNodeScoreRenewable(aux);
                if (auxScore < score) {
                    nodeId = aux;
                    score = auxScore;
                }
            }
            if (!powerOnOffSmart && nodeId != null) {
                found = true;
            }
        }
        if (nodeId != null) {
            System.out.println("Remove node: " + nodeId + " score=" + score);
            Node node = this.nodes.get(nodeId);
            for (String v : node.vmsRun) {
                System.out.println("   ->  " + v);
            }
            this.onNodes.remove(nodeId);
            this.finishingNodes.add(nodeId);
            this.addNodeEvent(nodeId, false);
        }
        return nodeId;
    }

    public String getVMLocalLocation(String vmId) {
        String node = vmNode.get(vmId);
        if (node == null) {
            node = "";
        }
        if (!onNodes.contains(node)) {
            node = "";
        }
        return node;
    }

    protected float getNodeScoreRenewable(String nodeId) {
        float ret = 0;
        Node auxNode = this.nodes.get(nodeId);
        ret = auxNode.getScoreRenewable();
        return ret;
    }

    public void addNode(String id, int cpu, int freq, int mem, float boot, float migration, boolean on, float powerIdle, float powerFull) {
        int host_num = Integer.parseInt(id.substring(4));
        if (host_num >= 0 && host_num <= 3) {
            this.addNode(new Node(id, cpu, freq, mem, boot, migration, powerIdle, powerFull, true, "spain"), on);
        } else {
            this.addNode(new Node(id, cpu, freq, mem, boot, migration, powerIdle, powerFull, false, "uk"), on);
        }
    }

    protected void addNode(Node node, boolean on) {
        super.addNode(node, on);
    }

    public void sendTime(int t) {
        super.sendTime(t);
        for (int i = 0; i < this.providers.size(); i++) {
            Provider p = (Provider) this.providers.getValue(i);
            p.sendTime(t);
        }
    }

    public void addProvider(String id, float boot, float price) {
        if (outsourcing) {
            Provider p = new Provider(this, id, boot, price);
            providers.put(id, p);
            System.out.println("New external provider: ID = " + id + "; Price = " + price);
        }
    }

    protected void assignProvider(int posVM, int posProvider) {
        VM vm = (VM) this.vms.getValue(posVM);
        Provider prov = (Provider) providers.getValue(posProvider);
        prov.submitVM(vm);
        vmNode.put(vm.id, prov.id);
        this.addOutsourceEvent(vm.id);
    }

    private void addCreateEvent(String vmId, String node) {
        Iterator<String> it = scheduleQueue.iterator();
        while (it.hasNext()) {
            String event = it.next();
            if (event.startsWith("create") && event.split(" ")[1].equals(vmId)) {
                it.remove();
            }
        }
        scheduleQueue.addLast("create " + vmId + " " + node);
    }

    protected void addOutsourceEvent(String vmId) {
        Iterator<String> it = scheduleQueue.iterator();
        while (it.hasNext()) {
            String event = it.next();
            if (event.startsWith("outsource") && event.split(" ")[1].equals(vmId)) {
                it.remove();
            }
        }
        scheduleQueue.addLast("outsource " + vmId);
    }

    /**
	 * Assign a VM to a given node.
	 * @param posVM Position of the VM.
	 * @param posNode Position of the Host.
	 * @param state State of the VM.
	 */
    protected Node assignNode(int posVM, int posNode, short state) {
        Node node = super.assignNode(posVM, posNode, state);
        return node;
    }
}
