package gbl.tsp.genetic.entity;

import gbl.common.entity.TSProblemModel;
import gbl.tsp.genetic.TravellerWorld;
import gbl.tsp.genetic.tools.MersenneTwister;

public class TravellerChromosome extends Chromosome {

    protected static MersenneTwister m_mt = null;

    private static int hometown = TSProblemModel.getInstance().getSourceNodeIndex();

    private TravellerWorld m_world = null;

    private TSProblemModel t_model = TSProblemModel.getInstance();

    private Codon[] m_cityList = null;

    private boolean m_fitnessValid = false;

    private String m_originator = new String("never initialized");

    public TravellerChromosome(TravellerWorld world, String originatorName) {
        super();
        m_world = world;
        m_originator = new String(originatorName);
        if (m_mt == null) {
            m_mt = MersenneTwister.getTwister();
        }
        m_fitness = Double.MAX_VALUE;
        m_fitnessValid = false;
        m_cityList = new Codon[m_world.getNumberOfCities()];
        for (int i = 0; i < m_cityList.length; i++) {
            m_cityList[i] = new Codon(t_model.nodeIndex[i]);
        }
        for (int currentSlot = m_cityList.length - 1; currentSlot > 0; currentSlot--) {
            int swapSlot = m_mt.nextInt(currentSlot + 1);
            int temp = m_cityList[swapSlot].get();
            m_cityList[swapSlot].set(m_cityList[currentSlot].get());
            m_cityList[currentSlot].set(temp);
        }
    }

    public TravellerChromosome(TravellerChromosome c) {
        super(c);
        m_world = c.m_world;
        m_fitness = c.m_fitness;
        m_fitnessValid = c.m_fitnessValid;
        m_originator = new String(c.m_originator);
        m_cityList = new Codon[m_world.getNumberOfCities()];
        for (int n = 0; n < m_world.getNumberOfCities(); ++n) {
            int got = -1;
            try {
                got = c.m_cityList[n].get();
            } catch (Exception TCcopyloopGet) {
            }
            try {
                m_cityList[n] = new Codon(got);
            } catch (Exception TCcopyloopSet) {
            }
        }
    }

    public TravellerChromosome(TravellerWorld world, String originatorName, int[] nodeList) {
        super();
        m_world = world;
        m_originator = new String(originatorName);
        m_fitness = Double.MAX_VALUE;
        m_fitnessValid = false;
        m_cityList = new Codon[m_world.getNumberOfCities()];
        for (int n = 0; n < m_world.getNumberOfCities(); ++n) {
            int got = nodeList[n];
            try {
                m_cityList[n] = new Codon(got);
            } catch (Exception TCcopyloopSet) {
            }
        }
    }

    public String toString() {
        StringBuffer b = new StringBuffer("");
        b.append("[");
        if (this.m_cityList == null) {
            b.append("(null city list in genome)");
        } else {
            if (this.m_cityList.length > 0) {
                for (int i = 0; i < this.m_cityList.length; i++) {
                    b.append(((i == 0) ? "" : ",") + (this.m_cityList[i]).toString());
                }
            } else {
                b.append("(empty, non-null city list in genome)");
            }
        }
        b.append("]");
        return b.toString();
    }

    public String toString(int offset) {
        StringBuffer b = new StringBuffer("");
        b.append("[");
        if (this.m_cityList == null) {
            b.append("(null city list in genome)");
        } else {
            int gl = this.m_cityList.length;
            if (gl > 0) {
                for (int i = 0; i < gl; i++) {
                    int realIndex = i + offset;
                    while (realIndex < 0) {
                        realIndex += gl;
                    }
                    realIndex = realIndex % gl;
                    b.append(((i == 0) ? "" : ",") + (this.m_cityList[realIndex]).toString());
                }
            } else {
                b.append("(empty, non-null city list in genome)");
            }
        }
        b.append("]");
        return b.toString();
    }

    public String toString(int offset, boolean reversed) {
        StringBuffer b = new StringBuffer("");
        b.append("[");
        if (this.m_cityList == null) {
            b.append("(null city list in genome)");
        } else {
            int gl = this.m_cityList.length;
            if (gl > 0) {
                for (int i = 0; i < gl; i++) {
                    int realIndex = offset + ((reversed) ? (-i) : i);
                    while (realIndex < 0) {
                        realIndex += gl;
                    }
                    realIndex = realIndex % gl;
                    b.append(((i == 0) ? "" : ",") + (this.m_cityList[realIndex]).toString());
                }
            } else {
                b.append("(empty, non-null city list in genome)");
            }
        }
        b.append("]");
        return b.toString();
    }

    public void invalidateCities() {
        for (int i = 0; i < this.m_cityList.length; i++) {
            m_cityList[i].set(-1);
        }
        m_fitnessValid = false;
    }

    public boolean looksLikeMe(TravellerChromosome her) {
        this.canonicalize();
        her.canonicalize();
        for (int i = 0; i < this.m_cityList.length; i++) {
            if (her.getCity(i) != this.getCity(i)) return false;
        }
        return true;
    }

    public int[] getGenomeAsArray() {
        int numCities = m_world.getNumberOfCities();
        int value[] = new int[numCities];
        for (int i = 0; i < numCities; i++) {
            value[i] = this.getCity(i);
        }
        return value;
    }

    public Codon getCodon(int n) {
        int j = n;
        while (j < 0) {
            j += this.getNumCities();
        }
        j = j % this.getNumCities();
        return m_cityList[j];
    }

    public Codon getCodon(int n, int offset, boolean reversed) {
        int targetSlot = offset + ((reversed) ? (0 - n) : n);
        while (targetSlot < 0) {
            targetSlot += m_world.getNumberOfCities();
        }
        targetSlot = targetSlot % m_world.getNumberOfCities();
        return this.getCodon(targetSlot);
    }

    public int getCityIndex(int n) {
        int j = n;
        while (j < 0) {
            j += this.getNumCities();
        }
        j = j % this.getNumCities();
        return j;
    }

    public int getCity(int n) {
        int j = n;
        while (j < 0) {
            j += this.getNumCities();
        }
        j = j % this.getNumCities();
        return m_cityList[j].get();
    }

    public int getCity(int n, int offset, boolean reversed) {
        int targetSlot = offset + ((reversed) ? (0 - n) : n);
        while (targetSlot < 0) {
            targetSlot += m_world.getNumberOfCities();
        }
        targetSlot = targetSlot % m_world.getNumberOfCities();
        return this.getCity(targetSlot);
    }

    public void setCity(int n, int city, int offset, boolean reversed) {
        int targetSlot = offset + ((reversed) ? (0 - n) : n);
        while (targetSlot < 0) {
            targetSlot += m_world.getNumberOfCities();
        }
        targetSlot = targetSlot % m_world.getNumberOfCities();
        this.setCity(targetSlot, city);
        m_fitnessValid = false;
    }

    public void setCity(int n, int city) {
        int j = n;
        while (j < 0) {
            j += this.getNumCities();
        }
        j = j % this.getNumCities();
        m_cityList[j].set(city);
        m_fitnessValid = false;
    }

    public int findCity(int city) {
        int i = 0;
        while (true) {
            if (city == m_cityList[i].get()) return i;
            ++i;
            if (i == m_world.getNumberOfCities()) return -1;
        }
    }

    public int findCity(int city, int offset, boolean reverse) {
        int i = 0;
        while (true) {
            if (city == this.getCity(i, offset, reverse)) return i;
            ++i;
            if (i == m_world.getNumberOfCities()) return -1;
        }
    }

    public boolean indexIsInSegment(int index, int segStart, int segLength, int offset, boolean reverse) {
        int ringLength = this.getNumCities();
        int virtualIndex = ((reverse) ? (offset - index) : (offset + index));
        int indexStart = segStart + offset;
        int indexEnd = indexStart + ((reverse) ? (1 - segLength) : (segLength - 1));
        virtualIndex = (((virtualIndex % ringLength) + ringLength) % ringLength);
        indexStart = (((indexStart % ringLength) + ringLength) % ringLength);
        indexEnd = (((indexEnd % ringLength) + ringLength) % ringLength);
        return ((((!reverse) && (indexEnd > indexStart) && (virtualIndex >= indexStart) && (virtualIndex <= indexEnd)) || (reverse && (indexEnd < indexStart) && (virtualIndex <= indexStart) && (virtualIndex >= indexEnd)) || ((!reverse) && (indexEnd < indexStart) && ((virtualIndex <= indexEnd) || (virtualIndex >= indexStart))) || ((reverse) && (indexStart < indexEnd) && ((virtualIndex <= indexStart) || (virtualIndex >= indexEnd)))));
    }

    public int getNumCities() {
        return m_world.getNumberOfCities();
    }

    public Chromosome cloneThis() {
        return new TravellerChromosome(this);
    }

    public String getOriginator() {
        return m_originator;
    }

    public void setOriginator(String originatorName) {
        m_originator = new String(originatorName);
    }

    public TravellerWorld getWorld() {
        return m_world;
    }

    public void checkValidity() {
        try {
            int genomeLength = m_world.getNumberOfCities();
            int tgraphNodes = m_world.getTotalGraphNodes();
            boolean isIn[] = new boolean[genomeLength];
            for (int i = 0; i < genomeLength; i++) {
                isIn[i] = false;
            }
            for (int i = 0; i < genomeLength; i++) {
                int codonName = m_cityList[i].get();
                if ((codonName < 0) || (codonName > (tgraphNodes - 1))) {
                    System.out.println("\r\n" + "TravellerChromosome.isValid(); invalid Codon in genome: " + m_cityList[i].toString());
                    complain();
                    return;
                } else {
                    isIn[i] = true;
                }
            }
            for (int i = 0; i < genomeLength; i++) {
                if (isIn[i] == false) {
                    System.out.println("\r\n" + "TravellerChromosome.isValid(); city " + i + " missing in genome.");
                    complain();
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("TravellerChromosome.isValid() threw!");
            System.out.println("And did it for a chromosome created by " + m_originator);
        }
    }

    private void complain() {
        try {
            System.out.println("\r\n" + "isValid() failed for genome created by " + this.getOriginator());
        } catch (Exception e) {
            System.out.println("TravellerChromosome.complain() threw!");
            System.out.println("And did it for a chromosome created by " + m_originator);
        }
    }

    public double testFitness() {
        if (m_fitnessValid) {
            return m_fitness;
        }
        m_fitness = t_model.getDistance(hometown, m_cityList[m_world.getNumberOfCities() - 1].get());
        for (int n = 1; n < m_world.getNumberOfCities(); ++n) m_fitness += t_model.getDistance(m_cityList[n - 1].get(), m_cityList[n].get());
        m_fitness += t_model.getDistance(hometown, m_cityList[0].get());
        m_fitnessValid = true;
        return m_fitness;
    }

    public void canonicalize() {
    }
}
