package uk.ac.bath.machine.neural3;

import uk.ac.bath.base.BuilderIF;
import uk.ac.bath.base.Gene;
import java.util.ArrayList;
import uk.ac.bath.base.MachineIF;
import uk.ac.bath.util.MyRandom;

/**
 *
 * @author pjl
 */
public class Neural3Builder implements BuilderIF {

    private int nUserIn;

    private int nHidden;

    private int nOut;

    private int nConnect;

    private int bitsPerGene;

    private boolean recursive;

    public Neural3Builder(int nUserIn, int nHidden, int nOut, int nConnect, boolean recursive) {
        this.nUserIn = nUserIn;
        this.nHidden = nHidden;
        this.nOut = nOut;
        this.nConnect = nConnect;
        this.recursive = recursive;
        bitsPerGene = nHidden * 24;
    }

    public MachineIF build(Object list[]) {
        Network net = new Network(nUserIn, nHidden, nOut, recursive);
        Temp hid = new Temp(nHidden);
        Temp out = new Temp(nOut);
        int bitsPerConnection = 24;
        int iHid = 0;
        int nIn = net.getTotalIn();
        for (Object o : list) {
            Gene gene = (Gene) o;
            for (int i = 0; i < nConnect; i++) {
                int start = i * bitsPerConnection;
                float w = gene.floatAt(start, 16, 3);
                int ptr = gene.unsignedAt(start + 16, 8);
                if (ptr < 128) {
                    ptr = ptr % nOut;
                    out.add(ptr, iHid, w);
                } else {
                    ptr = (ptr - 128) % nIn;
                    hid.add(iHid, ptr, w);
                }
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

    public void crossOver(Object ao, Object bo, Object a1o, Object b1o) {
        Gene a = (Gene) ao;
        Gene b = (Gene) bo;
        Gene a1 = (Gene) a1o;
        Gene b1 = (Gene) b1o;
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

    public void mutate(Object o) {
        Gene gene = (Gene) o;
        int point = MyRandom.nextInt(bitsPerGene);
        gene.bits[point] = !gene.bits[point];
    }

    public Object createRandomGene() {
        return new Gene(bitsPerGene, true);
    }

    public Object[] createGeneArray() {
        return new Gene[nHidden];
    }

    public MachineIF createRandomMachine() {
        Gene[] genes = (Gene[]) createGeneArray();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = (Gene) createRandomGene();
        }
        return build(genes);
    }

    public String reportSetup() {
        return " Builder:Neural3 userIn,hidden,nOut,recusive" + nUserIn + " " + nHidden + " " + nOut + " " + recursive;
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
}
