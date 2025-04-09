package uk.ac.bath.machine.neural3;

import uk.ac.bath.base.BuilderIF;
import uk.ac.bath.base.Gene;
import java.util.ArrayList;
import javax.swing.JFrame;
import uk.ac.bath.base.MachineIF;
import uk.ac.bath.util.MyRandom;

/**
 *
 * @author pjl
 */
public class VinillaNeural3Builder implements BuilderIF {

    private int nUserIn;

    private int nHidden;

    private int nOut;

    private int bitsPerGene;

    private boolean recursive;

    int bitsPerConnection = 16;

    private int nIn;

    public VinillaNeural3Builder(int nUserIn, int nHidden, int nOut, boolean recursive) {
        this.nUserIn = nUserIn;
        this.nHidden = nHidden;
        this.nOut = nOut;
        this.recursive = recursive;
        nIn = nUserIn;
        if (recursive) nIn += nHidden;
        bitsPerGene = (nIn + nOut) * bitsPerConnection;
    }

    public MachineIF build(Object[] list) {
        assert (list.length == nHidden);
        Network net = new Network(nUserIn, nHidden, nOut, recursive);
        Temp hid = new Temp(nHidden);
        Temp out = new Temp(nOut);
        int iHid = 0;
        for (Object a : list) {
            MyFloatList gene = (MyFloatList) a;
            for (int i = 0; i < nIn; i++) {
                float w = gene.floatAt(i);
                hid.add(iHid, i, w);
            }
            for (int i = 0; i < nOut; i++) {
                float w = gene.floatAt(nIn + i);
                out.add(i, iHid, w);
            }
            iHid++;
        }
        for (int i = 0; i < nHidden; i++) {
            net.addHidden(hid.ptrA.get(i), hid.wA.get(i), i);
        }
        for (int i = 0; i < nOut; i++) {
            net.addOut(out.ptrA.get(i), out.wA.get(i), i);
        }
        return net;
    }

    public void crossOver(Gene a, Gene b, Gene a1, Gene b1) {
        int point = MyRandom.nextInt(bitsPerGene + 1);
        for (int j = 0; j < bitsPerGene; j++) {
            if (j < point) {
                a1.bits[j] = a.bits[j];
                b1.bits[j] = b.bits[j];
            } else {
                a1.bits[j] = b.bits[j];
                b1.bits[j] = a.bits[j];
            }
        }
    }

    public Object createRandomGene() {
        return new SimpFloatList(nHidden, -10.0f, 10.0f, 1.0f);
    }

    public Object[] createGeneArray() {
        return new Object[nHidden];
    }

    public MachineIF createRandomMachine() {
        Object[] genes = createGeneArray();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = createRandomGene();
        }
        return build(genes);
    }

    public String reportSetup() {
        return " Builder:Neural3 userIn,hidden,nOut,recusive" + nUserIn + " " + nHidden + " " + nOut + " " + recursive;
    }

    public void crossOver(Object a, Object b, Object a1, Object b1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mutate(Object gene) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static class Temp {

        ArrayList<ArrayList<Float>> wA;

        ArrayList<ArrayList<Integer>> ptrA;

        int n;

        Temp(int n) {
            this.n = n;
            wA = new ArrayList<ArrayList<Float>>();
            ptrA = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < n; i++) {
                wA.add(new ArrayList<Float>());
                ptrA.add(new ArrayList<Integer>());
            }
        }

        void add(int i, Integer ptr, Float w) {
            assert (i < n);
            wA.get(i).add(w);
            ptrA.get(i).add(ptr);
        }
    }

    public static void main(String args[]) {
        int nUserIn = 5;
        int nHidden = 4;
        int nOut = 3;
        boolean recursive = true;
        VinillaNeural3Builder b = new VinillaNeural3Builder(nUserIn, nHidden, nOut, recursive);
        Network net = (Network) b.createRandomMachine();
        JFrame frame = new JFrame();
        NetworkPanel panel = new NetworkPanel();
        panel.setNet(net);
        frame.setSize(400, 400);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
