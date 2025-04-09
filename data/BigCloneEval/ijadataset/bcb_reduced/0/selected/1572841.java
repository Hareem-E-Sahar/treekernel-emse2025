package versusSNP.genome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import versusSNP.blast.BlastList;
import versusSNP.blast.BlastSet;
import versusSNP.blast.util.SNPList;
import versusSNP.io.FastaFile;

public class Genome {

    public static int transversion = 0;

    public static int transition = 0;

    private ArrayList<ORF> orfList;

    private LinkedList<SNPList> sSNPList, nsSNPList, inSNPList, delSNPList;

    private String name;

    private int size;

    public Genome() {
        super();
        orfList = new ArrayList<ORF>();
        sSNPList = new LinkedList<SNPList>();
        nsSNPList = new LinkedList<SNPList>();
        inSNPList = new LinkedList<SNPList>();
        delSNPList = new LinkedList<SNPList>();
    }

    public Genome(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<ORF> getOrfList() {
        return orfList;
    }

    public SNPList getSSNPList(Genome subjectGenome) {
        for (Iterator<SNPList> iter = sSNPList.iterator(); iter.hasNext(); ) {
            SNPList snpList = iter.next();
            if (snpList.getSubjectGenome() == subjectGenome) return snpList;
        }
        return null;
    }

    public SNPList getNsSNPList(Genome subjectGenome) {
        for (Iterator<SNPList> iter = nsSNPList.iterator(); iter.hasNext(); ) {
            SNPList snpList = iter.next();
            if (snpList.getSubjectGenome() == subjectGenome) return snpList;
        }
        return null;
    }

    public SNPList getInSNPList(Genome subjectGenome) {
        for (Iterator<SNPList> iter = inSNPList.iterator(); iter.hasNext(); ) {
            SNPList snpList = iter.next();
            if (snpList.getSubjectGenome() == subjectGenome) return snpList;
        }
        return null;
    }

    public SNPList getDelSNPList(Genome subjectGenome) {
        for (Iterator<SNPList> iter = delSNPList.iterator(); iter.hasNext(); ) {
            SNPList snpList = iter.next();
            if (snpList.getSubjectGenome() == subjectGenome) return snpList;
        }
        return null;
    }

    public int getNumOfORFs() {
        return orfList.size();
    }

    public ORF findORF(int begin) {
        if (ORF.compare_key == ORF.BEGIN_KEY) return (ORF) binarySearch(orfList, new ORF(begin));
        for (Iterator<ORF> iter = orfList.iterator(); iter.hasNext(); ) {
            ORF orf = iter.next();
            if (orf.getBegin() == begin) return orf;
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ORF findORF(String name) {
        if (ORF.compare_key == ORF.NAME_KEY) return (ORF) binarySearch(orfList, new ORF(name));
        for (Iterator<ORF> iter = orfList.iterator(); iter.hasNext(); ) {
            ORF orf = iter.next();
            if (orf.getName().equals(name)) return orf;
        }
        return null;
    }

    public void addORF(ORF orf) {
        this.orfList.add(orf);
        if (orf.getEnd() > size) size = orf.getEnd();
    }

    public void addSSNPList(SNPList snpList) {
        this.sSNPList.add(snpList);
    }

    public void addNsSNPList(SNPList snpList) {
        this.nsSNPList.add(snpList);
    }

    public void addInSNPList(SNPList snpList) {
        this.inSNPList.add(snpList);
    }

    public void addDelSNPList(SNPList snpList) {
        this.delSNPList.add(snpList);
    }

    public void sortOrfList() {
        Collections.sort(orfList);
    }

    /**
	 * attach sequences to the ORFs in this genome matching ORFs' names
	 * @param sequences - a list of sequences to add to ORFs
	 */
    public void attachSequences(ArrayList<Sequence> sequences) {
        for (Iterator<Sequence> iter = sequences.iterator(); iter.hasNext(); ) {
            Sequence sequence = iter.next();
            ORF orf = findORF(sequence.getName());
            if (orf != null) orf.setSequence(sequence);
        }
    }

    public void attachBlastSets(BlastList blastList, Genome subjectGenome) {
        attachBlastSets(blastList, subjectGenome, false);
    }

    public void attachBlastSets(BlastList blastList, Genome subjectGenome, boolean no_gui) {
        SNPList sSNPList = new SNPList(this, subjectGenome);
        SNPList nsSNPList = new SNPList(this, subjectGenome);
        SNPList inSNPList = new SNPList(this, subjectGenome);
        SNPList delSNPList = new SNPList(this, subjectGenome);
        for (Iterator<BlastSet> iter = blastList.iterator(); iter.hasNext(); ) {
            BlastSet set = iter.next();
            ORF qOrf, sOrf;
            if ((qOrf = findORF(set.getQName())) != null) {
                qOrf.addBlastSet(set);
                if (no_gui) set.getAlignmentStore().transverseCount();
                if ((sOrf = subjectGenome.findORF(set.getSName())) != null) {
                    set.setQuerySubjectOrf(qOrf, sOrf);
                    qOrf.statSNP();
                    SNPList.add(set.getSNPList(), sSNPList, nsSNPList, inSNPList, delSNPList);
                }
            }
        }
        addSSNPList(sSNPList);
        addNsSNPList(nsSNPList);
        addInSNPList(inSNPList);
        addDelSNPList(delSNPList);
    }

    public void printSNPSummary(Genome subjectGenome) {
        System.out.print(getSSNPList(subjectGenome).size());
        System.out.print("\t");
        System.out.print(getNsSNPList(subjectGenome).size());
        System.out.print("\t");
        System.out.print(getInSNPList(subjectGenome).size());
        System.out.print("\t");
        System.out.print(getDelSNPList(subjectGenome).size());
        System.out.print("\t");
        System.out.print(transition);
        System.out.print("\t");
        System.out.println(transversion);
    }

    /**
	 * Searches the specified list for the specified object using the binary search algorithm
	 * @param list - the list to be searched 
	 * @param x - the key to be searched for
	 * @return the element matching the key
	 */
    private static Comparable binarySearch(ArrayList<? extends Comparable> list, Comparable x) {
        int low = 0;
        int high = list.size() - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (list.get(mid).compareTo(x) < 0) low = mid + 1; else if (list.get(mid).compareTo(x) > 0) high = mid - 1; else return list.get(mid);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean toFasta(String path) {
        return FastaFile.writeFile(path, this);
    }
}
