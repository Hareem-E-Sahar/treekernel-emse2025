package DSENS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author teeteto
 */
public class WSNNetworkManager extends NetworkManager {

    private ArrayList<WirelessSensorNode> wsn;

    private WirelessSensorNode[][] wsnDeploy;

    private int dist_BS[];

    private int scan[];

    private int areaX;

    private int areaY;

    public String[] infoBattery;

    private double PEGASIS[];

    public WSNNetworkManager(ArrayList<Node> n, int dimAreaX, int dimAreaY) {
        super(n);
        java.util.Random rnd = new Random(GlobalInfo.SEED);
        this.wsn = new ArrayList<WirelessSensorNode>();
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            this.wsn.add(new WirelessSensorNode((int) (rnd.nextDouble() * dimAreaX), (int) (rnd.nextDouble() * dimAreaY), GlobalInfo.WirelessRange, 100.0));
        }
        int tmpX, tmpY;
        this.areaX = dimAreaX;
        this.areaY = dimAreaY;
        this.wsnDeploy = new WirelessSensorNode[dimAreaX][dimAreaY];
        this.dist_BS = new int[wsn.size()];
        this.PEGASIS = new double[wsn.size()];
        for (int i = 0; i < this.wsn.size(); i++) {
            tmpX = this.wsn.get(i).getX();
            tmpY = this.wsn.get(i).getY();
            wsnDeploy[tmpX][tmpY] = this.wsn.get(i);
            dist_BS[i] = GlobalInfo.WirelessRange;
            PEGASIS[i] = 0;
        }
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            n.add(this.wsn.get(i));
        }
    }

    void printWSN() {
        ArrayList<Sensor> s = new ArrayList<Sensor>(this.wsn.size());
        int n[];
        int n_dim;
        BufferedImage bimage = new BufferedImage(this.areaX, this.areaY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bimage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle(0, 0, this.areaX, this.areaY));
        for (int i = 0; i < this.wsn.size(); i++) {
            s.add(new Sensor(this.wsn.get(i).ID, this.wsn.get(i).getX(), this.wsn.get(i).getY(), this.wsn.get(i).getRange()));
            s.get(i).drawSensor(g2d);
            if (this.wsn.get(i).distBS == 2) g2d.draw(new Rectangle(this.wsn.get(i).getX() - 3, this.wsn.get(i).getY() - 3, 6, 6));
        }
        for (int i = 0; i < s.size(); i++) {
            if (!this.wsn.get(s.get(i).ID).neighborood.isEmpty()) {
                n_dim = this.wsn.get(s.get(i).ID).neighborood.size();
                n = new int[n_dim];
                for (int k = 0; k < n_dim; k++) {
                    n[k] = this.wsn.get(s.get(i).ID).neighborood.get(k).getId();
                }
                s.get(i).drawConnection(g2d, s, n);
            }
        }
        if (this.infoBattery != null) {
            g2d.setColor(Color.BLUE);
            Font f = new Font("SansSerif", Font.BOLD, 12);
            g2d.setFont(f);
        }
        try {
            File outputfile = new File("wsn" + GlobalInfo.CODE + "-" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + ".png");
            ImageIO.write(bimage, "png", outputfile);
        } catch (IOException e) {
        }
    }

    public void initNetworks() {
        ArrayList<Neighbour> nb;
        int distance;
        int tmpX, tmpY, r;
        int lowX, highX, lowY, highY;
        for (int i = 0; i < wsn.size(); i++) {
            tmpX = this.wsn.get(i).getX();
            tmpY = this.wsn.get(i).getY();
            r = this.wsn.get(i).getRange();
            lowX = tmpX - r;
            if (lowX < 0) lowX = 0;
            lowY = tmpY - r;
            if (lowY < 0) lowY = 0;
            highX = tmpX + r;
            if (highX > this.areaX) highX = this.areaY;
            highY = tmpY + r;
            if (highY > this.areaX) highY = this.areaY;
            nb = new ArrayList<Neighbour>(1);
            for (int j = lowX; j < highX; j++) {
                for (int k = lowY; k < highY; k++) {
                    distance = r + 1;
                    if (this.wsnDeploy[j][k] != null) distance = (int) (Math.sqrt(Math.pow(tmpX - this.wsnDeploy[j][k].getX(), 2) + Math.pow(tmpY - this.wsnDeploy[j][k].getY(), 2)));
                    if ((j != tmpX && k != tmpY) && (distance <= r)) nb.add(new Neighbour(distance, this.wsnDeploy[j][k].ID));
                }
            }
            this.wsn.get(i).setNeighborood(nb);
        }
        for (int i = 0; i < GlobalInfo.NoNodes; i++) this.network.set(i, this.wsn.get(i));
    }

    public void buildMET(int bs) {
        ArrayList<Neighbour> nb;
        if (bs > this.wsn.size()) return;
        dist_BS[bs] = 0;
        boolean complete = false;
        int index = 0, index_tmp;
        int dist_tmp[] = new int[GlobalInfo.NoNodes];
        int link2BS[] = new int[GlobalInfo.NoNodes];
        scan = new int[GlobalInfo.NoNodes];
        int done[] = new int[GlobalInfo.NoNodes];
        int done2[] = new int[GlobalInfo.NoNodes];
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            dist_tmp[i] = GlobalInfo.WirelessRange;
            done[i] = 0;
            done2[i] = 0;
            link2BS[i] = GlobalInfo.NoNodes;
        }
        scan[0] = bs;
        link2BS[bs] = bs;
        done[bs] = 1;
        index_tmp = 1;
        while (!complete) {
            wsn.get(scan[index]).orderNeighbour_LinkSize();
            for (int j = 0; j < wsn.get(scan[index]).neighborood.size(); j++) {
                if (index == 0) {
                    link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = bs;
                    dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = 1;
                    dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                    wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                    done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                    if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                        scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                        index_tmp++;
                    }
                } else if (index > 0) {
                    if (dist_BS[wsn.get(scan[index]).ID] < dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()]) {
                        if (dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] == (int) GlobalInfo.WirelessRange) {
                            link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).ID;
                            dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = dist_BS[wsn.get(scan[index]).ID] + 1;
                            dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                            wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                            done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                            if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                                scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                                index_tmp++;
                            }
                        } else if (dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] > wsn.get(scan[index]).neighborood.get(j).getLinkSize()) {
                            link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).ID;
                            dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = dist_BS[wsn.get(scan[index]).ID] + 1;
                            dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                            wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                            done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                            if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                                scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                                index_tmp++;
                            }
                        }
                    }
                }
            }
            index++;
            if (index == GlobalInfo.NoNodes) complete = true;
        }
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            nb = new ArrayList<Neighbour>(1);
            if (done[i] != 0) nb.add(new Neighbour(dist_tmp[i], link2BS[i]));
            wsn.get(i).setNeighborood(nb);
            this.network.set(i, this.wsn.get(i));
            if (done[i] != 0) done2[link2BS[i]]++;
        }
        double costo_rete = 0, costo_medio = 0, costo_max = 0;
        double fissa = 0.1;
        double variabile = 0.1;
        double datafusion = 0.01;
        double epsonK = 0.0002;
        double valori[] = new double[GlobalInfo.NoNodes];
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            valori[i] = (double) (fissa * done2[i] + datafusion + variabile + epsonK * dist_tmp[i] * dist_tmp[i]);
            System.out.println(valori[i] + " " + done2[i]);
            costo_rete += valori[i];
            if (i != 0 && valori[i] > costo_max) costo_max = valori[i];
        }
        costo_medio = costo_rete / GlobalInfo.NoNodes;
        this.infoBattery = new String[3];
        this.infoBattery[0] = "MAX " + costo_max;
        this.infoBattery[1] = "AVG " + costo_medio;
        this.infoBattery[2] = "Net " + costo_rete;
        System.out.println("------------------------");
        System.out.println("MAX " + costo_max);
        System.out.println("MEDIO " + costo_medio);
        System.out.println("RETE " + costo_rete);
    }

    public void buildMET2(int bs) {
        ArrayList<Neighbour> nb;
        if (bs > this.wsn.size()) return;
        dist_BS[bs] = 0;
        boolean complete = false;
        int index = 0, index_tmp;
        int dist_tmp[] = new int[GlobalInfo.NoNodes];
        int link2BS[] = new int[GlobalInfo.NoNodes];
        int dist_wait[] = new int[GlobalInfo.NoNodes];
        int link_wait[] = new int[GlobalInfo.NoNodes];
        scan = new int[GlobalInfo.NoNodes];
        int done[] = new int[GlobalInfo.NoNodes];
        int done2[] = new int[GlobalInfo.NoNodes];
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            dist_tmp[i] = GlobalInfo.WirelessRange;
            done[i] = 0;
            done2[i] = 0;
            link2BS[i] = GlobalInfo.NoNodes;
            dist_wait[i] = GlobalInfo.WirelessRange;
            link_wait[i] = GlobalInfo.NoNodes;
        }
        scan[0] = bs;
        link2BS[bs] = bs;
        done[bs] = 1;
        done2[bs] = 1;
        index_tmp = 1;
        int dim = 0;
        while (!complete) {
            wsn.get(scan[index]).orderNeighbour_LinkSize();
            for (int j = 0; j < wsn.get(scan[index]).neighborood.size(); j++) {
                dim = wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).neighborood.size() - 1;
                if (dim % 2 == 0) dim = dim / 2; else dim = (dim + 1) / 2;
                if (index == 0) {
                    if (wsn.get(scan[index]).neighborood.get(j).getLinkSize() <= wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).neighborood.get(dim).getLinkSize()) {
                        link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = bs;
                        dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = 1;
                        dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                        wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                        done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                        if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                            scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                            index_tmp++;
                        }
                    } else {
                        if (dist_wait[wsn.get(scan[index]).neighborood.get(j).getId()] >= wsn.get(scan[index]).neighborood.get(j).getLinkSize()) {
                            dist_wait[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                            link_wait[wsn.get(scan[index]).neighborood.get(j).getId()] = scan[index];
                        }
                    }
                } else if (index > 0) {
                    if (wsn.get(scan[index]).neighborood.get(j).getLinkSize() <= wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).neighborood.get(dim).getLinkSize()) {
                        if (dist_BS[wsn.get(scan[index]).ID] < dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()]) {
                            if (dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] == (int) GlobalInfo.WirelessRange) {
                                link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).ID;
                                dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = dist_BS[wsn.get(scan[index]).ID] + 1;
                                dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                                wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                                done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                                if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                                    scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                                    index_tmp++;
                                }
                            } else if (dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] > wsn.get(scan[index]).neighborood.get(j).getLinkSize()) {
                                link2BS[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).ID;
                                dist_BS[wsn.get(scan[index]).neighborood.get(j).getId()] = dist_BS[wsn.get(scan[index]).ID] + 1;
                                dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                                wsn.get(wsn.get(scan[index]).neighborood.get(j).getId()).setRange(dist_tmp[wsn.get(scan[index]).neighborood.get(j).getId()]);
                                done[wsn.get(scan[index]).neighborood.get(j).getId()]++;
                                if (done[wsn.get(scan[index]).neighborood.get(j).getId()] == 1) {
                                    scan[index_tmp] = wsn.get(scan[index]).neighborood.get(j).getId();
                                    index_tmp++;
                                }
                            }
                        }
                    } else {
                        if (dist_wait[wsn.get(scan[index]).neighborood.get(j).getId()] >= wsn.get(scan[index]).neighborood.get(j).getLinkSize()) {
                            dist_wait[wsn.get(scan[index]).neighborood.get(j).getId()] = wsn.get(scan[index]).neighborood.get(j).getLinkSize();
                            link_wait[wsn.get(scan[index]).neighborood.get(j).getId()] = scan[index];
                        }
                    }
                }
            }
            index++;
            if (index == GlobalInfo.NoNodes || index == index_tmp) complete = true;
        }
        System.out.print("\n-----\n  ");
        for (int i = 0; i < GlobalInfo.NoNodes; i++) {
            System.out.print(done[i] + " (" + scan[i] + ")  ");
            if (i != 0 && i % 25 == 0) System.out.println();
        }
        System.out.print("\n-----\n  ");
        int count = 0;
        int[] todo = new int[GlobalInfo.NoNodes];
        count = 0;
        for (int i = 0; i < GlobalInfo.NoNodes; i++) if (done[i] == 0) count++;
        {
            while (count > 0) {
                for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                    if (done[i] == 0) {
                        if (link_wait[i] != GlobalInfo.NoNodes && dist_BS[i] == GlobalInfo.WirelessRange) {
                            link2BS[i] = link_wait[i];
                            dist_BS[i] = dist_BS[link_wait[i]] + 1;
                            dist_tmp[i] = dist_wait[i];
                            wsn.get(i).setRange(dist_tmp[i]);
                            done[i]++;
                            count++;
                            int nindex = 0;
                            for (int j = 0; j < wsn.get(i).neighborood.size(); j++) {
                                dim = wsn.get(wsn.get(i).neighborood.get(j).getId()).neighborood.size() - 1;
                                if (dim % 2 == 0) dim = dim / 2; else dim = (dim + 1) / 2;
                                if (wsn.get(i).neighborood.get(j).getLinkSize() <= wsn.get(wsn.get(i).neighborood.get(j).getId()).neighborood.get(dim).getLinkSize()) {
                                    if (dist_BS[i] < dist_BS[wsn.get(i).neighborood.get(j).getId()]) {
                                        nindex = wsn.get(i).neighborood.get(j).getId();
                                        link2BS[nindex] = i;
                                        dist_BS[nindex] = dist_BS[i] + 1;
                                        dist_tmp[nindex] = wsn.get(i).neighborood.get(j).getLinkSize();
                                        wsn.get(wsn.get(i).neighborood.get(j).getId()).setRange(dist_tmp[nindex]);
                                    }
                                } else {
                                    if (dist_wait[wsn.get(i).neighborood.get(j).getId()] >= wsn.get(i).neighborood.get(j).getLinkSize()) {
                                        dist_wait[wsn.get(i).neighborood.get(j).getId()] = wsn.get(i).neighborood.get(j).getLinkSize();
                                        link_wait[wsn.get(i).neighborood.get(j).getId()] = i;
                                    }
                                }
                            }
                        }
                    }
                }
                count--;
            }
            count = 0;
            for (int i = 0; i < GlobalInfo.NoNodes; i++) if (done[i] == 0) count++;
            while (count > 0) {
                for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                    if (done[i] == 0) {
                        if (dist_BS[i] != GlobalInfo.WirelessRange) {
                            done[i]++;
                            int nindex = 0;
                            for (int j = 0; j < wsn.get(i).neighborood.size(); j++) {
                                dim = wsn.get(wsn.get(i).neighborood.get(j).getId()).neighborood.size() - 1;
                                if (dist_BS[i] < dist_BS[wsn.get(i).neighborood.get(j).getId()]) {
                                    nindex = wsn.get(i).neighborood.get(j).getId();
                                    link2BS[nindex] = i;
                                    dist_BS[nindex] = dist_BS[i] + 1;
                                    dist_tmp[nindex] = wsn.get(i).neighborood.get(j).getLinkSize();
                                    wsn.get(wsn.get(i).neighborood.get(j).getId()).setRange(dist_tmp[nindex]);
                                }
                            }
                        }
                    }
                }
                count--;
            }
            System.out.println();
            int count2 = 0;
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                System.out.print(done[i] + "  ");
                if (i != 0 && i % 25 == 0) System.out.println();
                if (done[i] == 0) count2++;
            }
            System.out.println("\n----++++----");
            System.out.println(count2);
            System.out.println("++++----++++");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                nb = new ArrayList<Neighbour>(1);
                if (done[i] != 0) nb.add(new Neighbour(dist_tmp[i], link2BS[i]));
                wsn.get(i).setNeighborood(nb);
                this.network.set(i, this.wsn.get(i));
                if (done[i] != 0) done2[link2BS[i]]++;
            }
            double costo_rete = 0, costo_medio = 0, costo_max = 0;
            double fissa = 0.1;
            double variabile = 0.1;
            double datafusion = 0.01;
            double epsonK = 0.0002;
            double valori[] = new double[GlobalInfo.NoNodes];
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                valori[i] = (double) (fissa * done2[i] + datafusion + variabile + epsonK * dist_tmp[i] * dist_tmp[i]);
                costo_rete += valori[i];
                if (i != 0 && valori[i] > costo_max) costo_max = valori[i];
            }
            costo_medio = costo_rete / GlobalInfo.NoNodes;
            this.infoBattery = new String[3];
            this.infoBattery[0] = "MAX " + costo_max;
            this.infoBattery[1] = "AVG " + costo_medio;
            this.infoBattery[2] = "Net " + costo_rete;
            System.out.println("------------------------");
            System.out.println("MAX " + costo_max);
            System.out.println("MEDIO " + costo_medio);
            System.out.println("RETE " + costo_rete);
        }
    }

    public void doPEGASIS() {
        int distance = 0;
        int ind = 0;
        for (int i = 0; i < wsn.size(); i++) {
            for (int j = 0; j < wsn.get(i).neighborood.size(); j++) {
                ind = (int) (Math.random() * wsn.get(i).neighborood.size() - 1);
                distance = wsn.get(i).neighborood.get(ind).getLinkSize();
            }
            this.PEGASIS[i] = distance;
            distance = 0;
        }
    }

    public void getInfo() {
        try {
            FileWriter out = new FileWriter("info" + GlobalInfo.CODE + "-" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + ".txt");
            out.write("Summary\n");
            double anxn = 0;
            int anxn_tmp = 0;
            int nwn = 0;
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (this.wsn.get(i).neighborood.size() != 0) anxn_tmp += this.wsn.get(i).neighborood.size(); else nwn++;
            }
            out.write("----------Number of Neighbours----------\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (i % 10 == 0) out.write("\n");
                out.write(this.wsn.get(i).neighborood.size() + "\t");
            }
            out.write("\n----------------------------------------------\n");
            out.write("\n-----------DISTANCE from Base Station-----------------\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (i % 10 == 0) out.write("\n");
                out.write(this.wsn.get(i).distBS + "\t");
            }
            out.write("\n----------------------------------------------\n");
            anxn = (double) (anxn_tmp / GlobalInfo.NoNodes);
            out.write(anxn + "\n" + nwn + "\n");
            out.write("\n-----------Power Consuming-----------------\n");
            int incoming[] = new int[GlobalInfo.NoNodes];
            for (int i = 0; i < GlobalInfo.NoNodes; i++) incoming[this.wsn.get(i).neighborood.get(0).getId()]++;
            double costo_rete = 0, costo_medio = 0, costo_max = 0;
            double fissa = 0.1;
            double variabile = 0.1;
            double datafusion = 0.01;
            double epsonK = 0.0002;
            double valori[] = new double[GlobalInfo.NoNodes];
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                valori[i] = (double) (fissa * incoming[i] + datafusion + variabile + epsonK * this.wsn.get(i).neighborood.get(0).getLinkSize() * this.wsn.get(i).neighborood.get(0).getLinkSize());
                costo_rete += valori[i];
                if (i != 0 && valori[i] > costo_max) costo_max = valori[i];
            }
            costo_medio = costo_rete / GlobalInfo.NoNodes;
            this.infoBattery = new String[3];
            this.infoBattery[0] = "MAX " + costo_max;
            this.infoBattery[1] = "AVG " + costo_medio;
            this.infoBattery[2] = "Net " + costo_rete;
            out.write("\nMAX " + costo_max);
            out.write("\nMEDIO " + costo_medio);
            out.write("\nRETE " + costo_rete);
            out.write("\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (i % 10 == 0) out.write("\n");
                out.write(valori[i] + "\t");
            }
            out.write("\n----------------------------------------------\n");
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void powerConsumption(int index) {
        try {
            FileWriter out = new FileWriter("power" + GlobalInfo.CODE + "-" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + "_" + index + ".txt");
            FileWriter out1;
            if (index == 1) out1 = new FileWriter("powerConsPEG" + GlobalInfo.CODE + "-" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + "_" + index + ".txt"); else out1 = new FileWriter("powerCons" + GlobalInfo.CODE + "-" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + "_" + index + ".txt");
            if (index == 1) this.doPEGASIS();
            out.write("\n-----------Power Consuming-----------------\n");
            int incoming[] = new int[GlobalInfo.NoNodes];
            for (int i = 0; i < GlobalInfo.NoNodes; i++) incoming[i] = 0;
            for (int i = 0; i < GlobalInfo.NoNodes; i++) incoming[this.wsn.get(i).neighborood.get(0).getId()]++;
            double costo_rete = 0, costo_medio = 0, costo_max = 0, costo_min = 100000;
            double fissa = 0.1;
            double epsonK = 0.0001;
            double valori[] = new double[GlobalInfo.NoNodes];
            double partenza[] = new double[GlobalInfo.NoNodes];
            int slot[] = new int[GlobalInfo.round];
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                partenza[i] = 500;
                if (index != 1) valori[i] = (double) (fissa * incoming[i] + epsonK * this.wsn.get(i).neighborood.get(0).getLinkSize() * this.wsn.get(i).neighborood.get(0).getLinkSize());
                if (index == 1) valori[i] = (double) (fissa + epsonK * this.PEGASIS[i] * this.PEGASIS[i]);
                costo_rete += valori[i];
                if (i != 0 && valori[i] > costo_max) costo_max = valori[i];
                if (i != 0 && valori[i] < costo_min) costo_min = valori[i];
            }
            for (int i = 0; i < GlobalInfo.round; i++) slot[i] = GlobalInfo.NoNodes;
            int dead = 0;
            int soglia = (int) (GlobalInfo.NoNodes * 25 / 100);
            for (int j = 0; j < 2000; j++) {
                for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                    if (partenza[i] != 100000) partenza[i] -= valori[i];
                    if (partenza[i] <= 0) {
                        partenza[i] = 100000;
                        dead++;
                        if (dead == 1) System.out.println("FND=" + j);
                        if (dead == soglia) System.out.println("25ND=" + j);
                    }
                }
                slot[j] = GlobalInfo.NoNodes - dead;
            }
            for (int i = 0; i < 2000; i++) {
                out1.write(slot[i] + "\n");
            }
            costo_medio = costo_rete / GlobalInfo.NoNodes;
            this.infoBattery = new String[4];
            this.infoBattery[0] = "MAX " + costo_max;
            this.infoBattery[1] = "AVG " + costo_medio;
            this.infoBattery[2] = "Net " + costo_rete;
            this.infoBattery[2] = "min " + costo_max;
            out.write("\nMAX " + costo_max);
            out.write("\nMAX " + costo_min);
            out.write("\nMEDIO " + costo_medio);
            out.write("\nRETE " + costo_rete);
            out.write("\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                out.write(valori[i] + "\n");
            }
            out.write("\n----------------------------------------------\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                out.write(valori[i] + "\n");
            }
            out.close();
            out1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void printMET() {
        try {
            FileWriter out = new FileWriter("info" + GlobalInfo.SEED + "-" + GlobalInfo.WirelessRange + ".txt");
            out.write("Summary\n");
            double anxn = 0;
            int anxn_tmp = 0;
            int nwn = 0;
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (this.wsn.get(i).neighborood.size() != 0) anxn_tmp += this.wsn.get(i).neighborood.size(); else nwn++;
            }
            out.write("----------Number of Neighbours----------\n");
            for (int i = 0; i < GlobalInfo.NoNodes; i++) {
                if (i % 10 == 0) out.write("\n");
                out.write(this.wsn.get(i).neighborood.size() + "\t");
            }
            out.write("\n----------------------------------------------\n");
            anxn = (double) (anxn_tmp / GlobalInfo.NoNodes);
            out.write(anxn + "\n" + nwn + "\n");
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
