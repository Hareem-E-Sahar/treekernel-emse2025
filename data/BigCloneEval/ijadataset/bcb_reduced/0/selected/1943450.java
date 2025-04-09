package game.neurons;

import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import game.models.simplify.Grammar;
import game.trainers.Trainer;
import game.data.TreeData;
import game.data.InputFactor;
import game.data.OutputAttribute;
import game.data.GlobalData;
import game.gui.*;
import game.utils.MyRandom;
import game.utils.RMSData;
import game.utils.UnitLoader;
import game.configuration.NetworkConfiguration;
import game.configuration.NetworkGAMEConfiguration;

/**
 * GAME network - the core of the application, extends the GMDH network
 * It is a network of interconnected processing elements (units or neurons).
 * The network is generated in the learning phase - it grows from minimal form.
 * It employs the deterministic crowding niching genetic algorithm to select units
 * and learning techniques proper for the modelled system.
 */
public class GAMEnetwork extends GMDHnetwork implements java.io.Serializable {

    public static final String version = "GAME version=1.0(28.2.2009)";

    static Logger logger = Logger.getLogger(GAMEnetwork.class);

    /**
     * The <code>layer</code> of the GAME network consists of units with the same maximum number of inputs allowed
     * <p/>
     * This units are connected to units from previous layers and to input features.
     */
    private Layer[] layer = new Layer[NetworkConfiguration.MAX_LAYERS];

    NetworkGAMEConfiguration c;

    int indx;

    private Neuron output;

    private double data_noise;

    private float[] varsig;

    public void setFeature(double[] feature) {
        this.feature = feature;
    }

    /**
     * This significance is derived from interconnections in the network
     * @param index
     * @return
     */
    public double getFeatureSignificance2(int index) {
        return feature[index];
    }

    /**
     * The field <code>feature</code> stores the information about features ranking.
     */
    private double[] feature;

    private int[] overvarsig;

    private transient CrowdingWindow cw;

    public NetworkGAMEConfiguration getConfiguration() {
        return c;
    }

    private transient RMSWindow rmsw;

    private transient String[] gD;

    private int genesDefined;

    /**
    * added for time measuring for multiprocessor support
    * by jakub spirk spirk.jakub@gmail.com
    * 05. 2008
    */
    private double learningTime = 0;

    public void computeSingnificance(float[] vs) {
        float all = 0;
        for (int i = 0; i < GlobalData.getInstance().getINumber(); i++) {
            varsig[i] += (float) ((float) vs[i]);
            all += varsig[i];
        }
        if (all == 0) {
            all = (float) 0.00001;
        }
        for (int i = 0; i < GlobalData.getInstance().getINumber(); i++) {
            overvarsig[i] = (int) ((10000 * varsig[i]) / all);
            overvarsigProcArray[actualLayer][i] = overvarsig[i];
        }
    }

    public void setLayer(GAMEnetwork.Layer[] layer) {
        this.layer = layer;
    }

    public void setILayer(InputLayer newiLayer) {
        iLayer = newiLayer;
    }

    public Layer[] getLayers() {
        return layer;
    }

    public ALayer getInputLayer() {
        return iLayer;
    }

    public Neuron[] getLayer(int index) {
        if (layer == null) return null;
        if (layer[index] == null) return null;
        return layer[index].n;
    }

    /**
     * class Layer represents a layer of network
     */
    public class Layer extends GMDHnetwork.Layer implements java.io.Serializable {

        /**
         * Group of chromosomes <code>g</code> are used for encoding of units to the genotype.
         */
        transient Genome[] g;

        /**
         * Variable <code>inputsPossible</code> is the number of input features plus
         * <p/>
         * all units that survived in previous layers.
         */
        int inputsPossible;

        /**
         * The same initialization as for layers of the GMDH network
         */
        public Layer() {
            super();
        }

        /**
         * This constructor is called when network is loaded from PMML document
         *
         * @param e is the <NeuralLayer> element from the PMML document
         * @author Vit Ulicny
         */
        public Layer(Element e) {
            String typeOfFunction;
            NodeList nodeListOfNeurons = e.getElementsByTagName("Neuron");
            int numberOfNeurons = nodeListOfNeurons.getLength();
            number = inumber = numberOfNeurons;
            Neuron[] neurons = new Neuron[numberOfNeurons];
            for (int i = 0; i < numberOfNeurons; i++) {
                typeOfFunction = ((org.w3c.dom.Element) nodeListOfNeurons.item(i)).getAttribute("type");
                Neuron neuron = new Neuron();
                if (typeOfFunction.equals("Sigmoid")) {
                    neuron = new SigmNeuron();
                } else if (typeOfFunction.equals("Linear")) {
                    neuron = new LinearNeuron();
                } else if (typeOfFunction.equals("MultiGaussian")) {
                    neuron = new MultiGaussianNeuron();
                } else if (typeOfFunction.equals("Rational")) {
                    neuron = new PolyFractNeuron();
                } else if (typeOfFunction.equals("Polynomial")) {
                    neuron = new PolySimpleNeuron();
                } else if (typeOfFunction.equals("Polynomial - NR")) {
                    neuron = new PolySimpleNRNeuron();
                } else if (typeOfFunction.equals("Sine")) {
                    neuron = new SinusNeuron();
                } else if (typeOfFunction.equals("Polynomial - combi")) {
                    neuron = new CombiNeuron();
                } else if (typeOfFunction.equals("Exponential")) {
                    neuron = new ExpNeuron();
                } else if (typeOfFunction.equals("Gaussian")) {
                    neuron = new GaussianNeuron();
                } else if (typeOfFunction.equals("Gauss")) {
                    neuron = new GaussNeuron();
                }
                neuron.setId(Integer.parseInt(((org.w3c.dom.Element) nodeListOfNeurons.item(i)).getAttribute("id")));
                neurons[i] = neuron;
            }
            this.n = neurons;
        }

        /**
         * construct a GAME network layer
         * @param num
         * @param pl
         * @param inputs_per_neuron
         */
        public Layer(int num, ALayer pl, int inputs_per_neuron) {
            prevLayer = pl;
            index = num;
            ANeuron[] copyN;
            ANeuron[] inputsChoosen;
            g = new Genome[maxNeuronsInLayer];
            prevLayer = pl;
            ALayer aLayer = prevLayer;
            ANeuron choosen;
            Dimension pos;
            new Dimension();
            layerProgress = 0;
            boolean tryAgain;
            inputsToNeuron = inputs_per_neuron;
            logger.info("New layer " + num + ", max inputs:" + inputs_per_neuron);
            inumber = c.getLayerInitialNeuronsNumber(num);
            logger.info("Number of neurons in the population:" + inumber);
            number = inumber;
            if (number > inumber) {
                number = inumber;
            }
            int actInput, golayer, which, s;
            inputsPossible = 0;
            while (aLayer != null) {
                inputsPossible += aLayer.getNumber();
                aLayer = aLayer.getPreviousLayer();
            }
            if (c.isJustTwo()) {
                inputs_per_neuron = inputsToNeuron = inputsPossible;
            }
            inputsChoosen = new ANeuron[inputs_per_neuron];
            mode = PASSIVE;
            prevLayer.computeOutputs();
            for (int i = 0; i < inumber; i++) {
                copyN = prevLayer.getRandomNeurons(1);
                aLayer = prevLayer;
                ALayer al;
                inputsChoosen[0] = copyN[0];
                int inputsPN;
                if (c.isJustTwo()) {
                    inputsPN = myRandom.nextInt(inputs_per_neuron);
                    inputsPN++;
                } else {
                    inputsPN = inputs_per_neuron;
                }
                if (c.isEmployPrevious()) {
                    actInput = 1;
                } else {
                    actInput = 0;
                }
                int full;
                while (actInput < inputsPN) {
                    do {
                        aLayer = prevLayer;
                        switch(c.getParents()) {
                            case NetworkConfiguration.YOUNG:
                                full = aLayer.getNumber();
                                while (actInput >= full) {
                                    aLayer = aLayer.getPreviousLayer();
                                    if (aLayer != null) {
                                        full += aLayer.getNumber();
                                    } else {
                                        break;
                                    }
                                }
                                break;
                            case NetworkConfiguration.YOUNGER:
                                for (int j = s = 0; j < num + 1; j++) {
                                    s += (j + 1);
                                }
                                s = myRandom.nextInt(s + 1) - num - 1;
                                for (int j = num; s > 0; s -= j--) {
                                    if (aLayer != null) {
                                        aLayer = aLayer.getPreviousLayer();
                                    }
                                }
                                break;
                            case NetworkConfiguration.MIDDLE:
                                s = myRandom.nextInt(num + 1);
                                for (int j = 0; j < s; j++) {
                                    if (aLayer != null) {
                                        aLayer = aLayer.getPreviousLayer();
                                    }
                                }
                                break;
                            case NetworkConfiguration.OLDER:
                                for (int j = s = 0; j < num + 1; j++) {
                                    s += (j + 1);
                                }
                                s = myRandom.nextInt(s + 1) - num - 1;
                                for (int j = num; s > 0; s -= (num - (--j))) {
                                    if (aLayer != null) {
                                        aLayer = aLayer.getPreviousLayer();
                                    }
                                }
                                break;
                            case NetworkConfiguration.OLD:
                                full = 0;
                                do {
                                    al = getPreviousLayer();
                                    while (al.getPreviousLayer() != null) {
                                        al = al.getPreviousLayer();
                                    }
                                    aLayer = al;
                                    full += aLayer.getNumber();
                                } while (actInput >= full);
                                break;
                        }
                        choosen = aLayer.getNeuron(myRandom.nextInt(aLayer.getNumber()));
                        tryAgain = false;
                        for (int j = 0; j < actInput; j++) {
                            if (inputsChoosen[j].equals(choosen)) {
                                tryAgain = true;
                            }
                        }
                    } while (tryAgain);
                    inputsChoosen[actInput] = choosen;
                    actInput++;
                }
                g[i] = new Genome(iNum, inputsPossible, inputsPN);
                inputsToNeuron = inputsPN;
                do {
                    which = myRandom.nextInt(u.getNeuronsNumber());
                } while (!c.neuronTypeAllowed(which));
                int train;
                do {
                    train = myRandom.nextInt(u.getTrainersNumber());
                } while (!c.neuronTrainerAllowed(train));
                do {
                    n[i] = newNeuron(which, train, inputsChoosen);
                } while (n[i] == null);
                logger.trace("New neuron " + i + ", type id:" + which + ", trainer id:" + train);
                for (int j = 0; j < inputsPN; j++) {
                    pos = getNeuronParentPosition(n[i], j);
                    if (pos == null) {
                        int h = 0;
                    }
                    int ipos = (int) pos.getWidth();
                    int layerDist = (int) pos.getHeight();
                    ALayer pLay = this;
                    for (int k = 0; k < layerDist; k++) {
                        if (pLay != null) {
                            pLay = pLay.getPreviousLayer();
                        }
                    }
                    if (pLay != null) {
                        pLay = pLay.getPreviousLayer();
                    }
                    while (pLay != null) {
                        ipos += pLay.getNumber();
                        pLay = pLay.getPreviousLayer();
                    }
                    g[i].setInput(ipos, 1);
                }
                if (n[i].getClass().getName().compareTo("game.neurons.CombiNeuron") == 0) {
                    g[i] = new CombiGenome((CombiNeuron) n[i], g[i]);
                    ((CombiNeuron) n[i]).readGenome(((CombiGenome) g[i]));
                }
                logger.trace("Neuron input connections:" + g[i].toString());
            }
            mode = ACTIVE;
            inputsToNeuron = inputs_per_neuron;
        }

        /**
         * Initializes an GAME unit.
         *
         * @param which         The type of the unit to be created.
         * @param train
         * @param inputsChoosen Field of units from previous layers
         *                      <p/>
         *                      that will be used as unit's inputs.
         * @return Initialized GAME unit that will be stored in the actual layer.
         */
        public Neuron newNeuron(int which, int train, ANeuron[] inputsChoosen) {
            Neuron nn = new Neuron();
            Trainer tt;
            new Trainer();
            try {
                nn = (Neuron) u.getNeuronClass(which).newInstance();
                nn.init(ja, inputsChoosen, inputsToNeuron, u.getNeuronConfig(which));
                tt = (Trainer) u.getTrainerClass(train).newInstance();
                tt.init(nn, u.getTrainerConfig(train), nn.getCoefsNumber());
                nn.setTrainer(tt);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return nn;
        }

        /**
         * @return Field of num ramdomly selected units from the actual layer.
         * @see GMDHnetwork.ALayer#getRandomNeurons(int)
         */
        public ANeuron[] getRandomNeurons(int num) {
            ANeuron[] ret = new ANeuron[num];
            MyRandom myR = new MyRandom(getNumber());
            for (int i = 0; i < num; i++) {
                ret[i] = n[myR.getRandom(getNumber())];
            }
            return ret;
        }

        /**
         * @return The unit <b>num</b> from the actual layer.
         * @see GMDHnetwork.ALayer#getNeuron(int)
         */
        public ANeuron getNeuron(int num) {
            if (num < getNumber()) {
                return n[num];
            } else {
                return null;
            }
        }

        public ALayer getPreviousLayer() {
            return prevLayer;
        }

        /**
         * @return The maximum of inputs for units from the actual layer
         * @see GMDHnetwork.ALayer#getInputsNumber()
         */
        public int getInputsNumber() {
            return inputsToNeuron;
        }

        /**
         * niching genetic algorithm
         * <p/>
         * Here units are evolved to get units with the best topology for the system modelled.
         */
        public void deterministic_crowding() {
            int[] parent = new int[inumber];
            Genome[] cn = new Genome[inumber];
            Neuron[] pn = new Neuron[inumber];
            ANeuron[] inputsChoosen;
            Genome[] h;
            int initDiversity = 0;
            MyRandom myRand;
            boolean progress = true;
            if (inumber == 0) {
                return;
            }
            int epoch = 0;
            for (int i = 0; i < inumber; i++) {
                g[i].refreshInpEmp(getInputsEmployed(g[i]));
            }
            cw.reset(c.getLayerEpochs(getLayerIndex()), inputsPossible);
            cw.initErrWin(inumber, (group < 500 ? group : 500));
            logger.info("Deterministic crowding starts");
            logger.info("Max number of generations: " + c.getLayerEpochs(getLayerIndex()));
            do {
                logger.debug("Generation " + epoch);
                if (myGraph != null) {
                    if (cw != null && GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                        cw.clearErrTable();
                    }
                }
                logger.debug("Computing error of neurons on validation data");
                computeError(n, inumber);
                if (myGraph != null) if (GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                    if (epoch == 0) {
                        cw.computeMedianAndVariance();
                    }
                    cw.setErrDataReady(true);
                    if (GMDHtree.autoCapture) {
                        GraphCanvas.getInstance().captureErrWin();
                    }
                    cw.setErrDataReady(false);
                }
                myRand = new MyRandom(inumber);
                for (int i = 0; i < inumber; i++) {
                    parent[i] = myRand.getRandom(inumber);
                }
                logger.debug("Selecting parents");
                for (int i = 0; i < inumber - 1; i += 2) {
                    logger.trace("Making love: indexes [" + parent[i] + ", " + parent[i + 1] + "]");
                    h = null;
                    if (!isCombiGenome(parent[i]) && !isCombiGenome(parent[i + 1])) h = g[parent[i]].cross(g[parent[i + 1]]);
                    if (isCombiGenome(parent[i]) && isCombiGenome(parent[i + 1])) h = ((CombiGenome) g[parent[i]]).cross(((CombiGenome) g[parent[i + 1]]));
                    if (isCombiGenome(parent[i]) && !isCombiGenome(parent[i + 1])) h = ((CombiGenome) g[parent[i]]).cross(g[parent[i + 1]]);
                    if (!isCombiGenome(parent[i]) && isCombiGenome(parent[i + 1])) h = ((CombiGenome) g[parent[i + 1]]).cross(g[parent[i]]);
                    h[0].mutate((int) (c.getMutationRate() * 1000));
                    h[1].mutate((int) (c.getMutationRate() * 1000));
                    cn[i] = h[0];
                    cn[i + 1] = h[1];
                }
                logger.debug("Producing offsprings");
                if ((inumber > 1) && (inumber % 2 == 1)) {
                    cn[inumber - 1] = cn[inumber - 2];
                }
                for (int i = 0; i < inumber; i++) {
                    cn[i].refreshInpEmp(getInputsEmployed(cn[i]));
                }
                for (int act = 0; act < inumber; act++) {
                    int ipos = inputsPossible;
                    ALayer pLay = prevLayer;
                    int neu = 0;
                    int nxt = 0;
                    inputsChoosen = new ANeuron[cn[act].getInputsNumber() + 1];
                    do {
                        neu += pLay.getNumber();
                        for (int k = 0; k < pLay.getNumber(); k++) {
                            if (cn[act].getInput(ipos - neu + k) == 1) {
                                inputsChoosen[nxt++] = pLay.getNeuron(k);
                            }
                        }
                        pLay = pLay.getPreviousLayer();
                    } while (pLay != null);
                    int itn = inputsToNeuron;
                    inputsToNeuron = nxt;
                    int which;
                    do {
                        which = myRandom.nextInt(u.getNeuronsNumber());
                    } while (!c.neuronTypeAllowed(which));
                    int train;
                    do {
                        train = myRandom.nextInt(u.getTrainersNumber());
                    } while (!c.neuronTrainerAllowed(train));
                    if (c.getRandomChildren() < 100) {
                        int pidx = (act / 2) * 2;
                        if ((inumber % 2 == 1) && (act == inumber - 1)) pidx = ((act - 1) / 2) * 2;
                        if (myRandom.nextInt(2) > 0) {
                            if (myRandom.nextInt(100) >= c.getRandomChildren()) which = u.getTypeNum(n[parent[pidx]]);
                            if (myRandom.nextInt(100) >= c.getRandomChildren()) train = u.getTrainNum(n[parent[pidx]].trainer);
                        } else {
                            if (myRandom.nextInt(100) >= c.getRandomChildren()) which = u.getTypeNum(n[parent[pidx + 1]]);
                            if (myRandom.nextInt(100) >= c.getRandomChildren()) train = u.getTrainNum(n[parent[pidx + 1]].trainer);
                        }
                    }
                    pn[act] = newNeuron(which, train, inputsChoosen);
                    inputsToNeuron = itn;
                    if (pn[act].getClass().getName().compareTo("game.neurons.CombiNeuron") == 0) {
                        if (cn[act].getClass().getName().compareTo("game.neurons.CombiGenome") == 0) {
                            ((CombiNeuron) pn[act]).readGenome(((CombiGenome) cn[act]));
                        } else {
                            cn[act] = new CombiGenome((CombiNeuron) pn[act], iNum, inputsPossible, itn);
                            ((CombiNeuron) pn[act]).readGenome(((CombiGenome) cn[act]));
                        }
                    }
                    logger.trace("New neuron " + act + ", type id:" + which + ", trainer id:" + train);
                    logger.trace("Neuron input connections:" + cn[act].toString());
                }
                int offspr = inumber;
                logger.debug("training offsprings, computing error");
                storeTrainingData(pn, offspr);
                teachUnits(pn, offspr);
                computeError(pn, offspr);
                if (c.isCrowdingEmployed()) {
                    for (int i = 0; i < inumber - 1; i += 2) {
                        double dist1 = 0;
                        double dist2 = 0;
                        if (c.isGenomeDistance()) {
                            dist1 += g[parent[i]].distance(cn[i]);
                            dist1 += g[parent[i + 1]].distance(cn[i + 1]);
                            dist2 += g[parent[i]].distance(cn[i + 1]);
                            dist2 += g[parent[i + 1]].distance(cn[i]);
                            if (dist1 > dist2) {
                                dist2 /= dist1;
                                dist1 = 1;
                            } else {
                                dist1 /= dist2;
                                dist2 = 1;
                            }
                        }
                        if (c.isCorrelationDistance()) {
                            double dist3 = 0;
                            double dist4 = 0;
                            dist3 += getCorrelationDistance(n[parent[i]], pn[i], 50);
                            dist3 += getCorrelationDistance(n[parent[i + 1]], pn[i + 1], 50);
                            dist4 += getCorrelationDistance(n[parent[i]], pn[i + 1], 50);
                            dist4 += getCorrelationDistance(n[parent[i + 1]], pn[i], 50);
                            if (dist3 > dist4) {
                                dist4 /= dist3;
                                dist3 = 1;
                            } else {
                                dist3 /= dist4;
                                dist4 = 1;
                            }
                            dist1 += dist3;
                            dist2 += dist4;
                        }
                        if (dist1 > dist2) {
                            if (n[parent[i]].getSquareError() > pn[i + 1].getSquareError()) {
                                n[parent[i]] = pn[i + 1];
                                g[parent[i]] = cn[i + 1];
                                logger.trace("Neuron " + parent[i] + " replaced by offspring " + (i + 1));
                            }
                            if (n[parent[i + 1]].getSquareError() > pn[i].getSquareError()) {
                                n[parent[i + 1]] = pn[i];
                                g[parent[i + 1]] = cn[i];
                                logger.trace("Neuron " + parent[i + 1] + " replaced by offspring " + i);
                            }
                        } else {
                            if (n[parent[i]].getSquareError() > pn[i].getSquareError()) {
                                n[parent[i]] = pn[i];
                                g[parent[i]] = cn[i];
                                logger.trace("Neuron " + parent[i] + " replaced by offspring " + i);
                            }
                            if (n[parent[i + 1]].getSquareError() > pn[i + 1].getSquareError()) {
                                n[parent[i + 1]] = pn[i + 1];
                                g[parent[i + 1]] = cn[i + 1];
                                logger.trace("Neuron " + parent[i + 1] + " replaced by offspring " + (i + 1));
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < inumber - 1; i += 2) {
                        if (n[parent[i]].getSquareError() > pn[i].getSquareError()) {
                            n[parent[i]] = pn[i];
                            g[parent[i]] = cn[i];
                            logger.trace("Neuron " + parent[i] + " replaced by offspring " + i);
                        }
                        if (n[parent[i + 1]].getSquareError() > pn[i + 1].getSquareError()) {
                            n[parent[i + 1]] = pn[i + 1];
                            g[parent[i + 1]] = cn[i + 1];
                            logger.trace("Neuron " + parent[i + 1] + " replaced by offspring " + (i + 1));
                        }
                    }
                }
                double err = 0, best = n[0].getSquareError();
                for (int i = 1; i < inumber; i++) {
                    err += n[i].getSquareError();
                    if (best > n[i].getSquareError()) {
                        best = n[i].getSquareError();
                    }
                }
                cw.setBestError(best);
                cw.setOverallError(err);
                cw.setEpoch(epoch + 1);
                for (int i = 0; i < inumber; i++) {
                    parent[i] = 0;
                    for (int j = 0; j < inumber; j++) {
                        if (g[i].equals(g[j])) {
                            parent[i]++;
                        }
                    }
                }
                for (int i = 0; i < inumber; i++) {
                    cn[i] = g[i];
                    pn[i] = n[i];
                }
                int lastIndex = inumber - 1;
                boolean anyChange;
                Genome swap;
                Neuron sw;
                int spar;
                do {
                    anyChange = false;
                    for (int i = 0; i < lastIndex; i++) {
                        if (parent[i] < parent[i + 1]) {
                            anyChange = true;
                            swap = cn[i];
                            spar = parent[i];
                            sw = pn[i];
                            parent[i] = parent[i + 1];
                            cn[i] = cn[i + 1];
                            pn[i] = pn[i + 1];
                            pn[i + 1] = sw;
                            cn[i + 1] = swap;
                            parent[i + 1] = spar;
                        }
                    }
                    lastIndex--;
                } while (anyChange);
                int diversity = 0;
                for (int i = 1; i < inumber; i++) {
                    if (c.isGenomeDistance()) {
                        diversity += g[0].distance(g[i]);
                    }
                    if (c.isCorrelationDistance()) {
                        diversity += getCorrelationDistance(n[0], n[i], 50);
                    }
                }
                if (epoch == 0) {
                    initDiversity = diversity;
                    cw.setDiversityDropAllowed(initDiversity * c.getMaximalDiversityDrop());
                }
                cw.setDiversity(diversity);
                int ii;
                for (int i = 0; i < inputsPossible; i++) {
                    ii = 0;
                    int inpu = 0;
                    do {
                        if ((cn[ii] != null) && (cn[ii].getInput(i) == 1)) {
                            inpu++;
                        }
                        ii++;
                    } while (ii < inumber);
                    cw.addUnitsConnected(inpu);
                }
                ii = 0;
                double erro;
                cw.resetGenes(inumber);
                do {
                    if (cn[ii] != null) {
                        erro = pn[ii].getSquareError();
                        for (int j = ii + 1; j < inumber; j++) {
                            if ((cn[j] != null) && cn[ii].equals(cn[j])) {
                                if (erro < pn[j].getSquareError()) {
                                    erro = pn[j].getSquareError();
                                }
                                cn[j] = null;
                            }
                        }
                        cw.addGenesGroup(cn[ii], parent[ii], erro);
                    }
                    ii++;
                } while (ii < inumber);
                if ((diversity / (double) initDiversity) < c.getMaximalDiversityDrop()) {
                    progress = false;
                    logger.info("Stop deterministic crowding - diversity treshold reached");
                }
                if (++epoch > c.getLayerEpochs(getLayerIndex())) {
                    progress = false;
                }
                if (myGraph != null) myGraph.redraw();
            } while (progress);
            cw.printVariablesScore();
            logger.info("Deterministic crowding ends");
        }

        /**
         * isCombiGenome
         *
         * @param i int
         * @return boolean
         */
        private boolean isCombiGenome(int i) {
            return (g[i].getClass().getName().compareTo("game.neurons.CombiGenome") == 0);
        }

        /**
         * getCorrelationDistance
         *
         * @param n   Neuron1
         * @param neu Neuron2
         * @param limit
         * @return String
         */
        private double getCorrelationDistance(Neuron n, Neuron neu, int limit) {
            int vTest = (int) (group * (c.getVectorsInTestingSet() / 100.0));
            int rVector;
            double error;
            double error1;
            double distance = 0;
            int count = 0;
            myRandom.resetRandom();
            for (int i = c.isTestOnBothTrainingAndTestingData() ? 0 : (group - vTest); i < group; i++) {
                rVector = 0;
                if (i < group - vTest) {
                    rVector = myRandom.getRandomLearningVector();
                }
                if (i == group - vTest) {
                    myRandom.resetRandom();
                }
                if (i >= group - vTest) {
                    rVector = myRandom.getRandomTestingVector();
                }
                iLayer.setActualVector(rVector);
                mode = PASSIVE;
                prevLayer.computeOutputs();
                error = n.getError(rVector);
                error1 = neu.getError(rVector);
                if (!Double.isNaN(error + error1)) {
                    distance += (error - error1) * (error - error1);
                    count++;
                    if (count > limit) i = group;
                }
            }
            if (count > 0) {
                distance /= count;
            }
            mode = ACTIVE;
            distance *= 1000;
            return distance;
        }

        /**
         * Forces units of the layer to store training data.
         *
         * @param n       The GAME units.
         * @param inumber
         */
        public void storeTrainingData(Neuron[] n, int inumber) {
            int vTest = (int) (group * (c.getVectorsInTestingSet() / 100.0));
            int rVector;
            MyRandom mr = new MyRandom(1);
            job = ENTERING_PASSIVE_MODE;
            mode = PASSIVE;
            myRandom.resetRandom();
            for (int i = 0; i < group - vTest; i++) {
                if (c.isBootstrap()) {
                    rVector = myRandom.getBootstrapRandomLearningVector();
                } else {
                    rVector = myRandom.getRandomLearningVector();
                }
                iLayer.setActualVector(rVector);
                layerProgress = (int) (100.0 * i / (double) (group - vTest - 1));
                if (i % 100 == 1) {
                    if (myGraph != null) myGraph.redraw();
                }
                boolean precomputed = false;
                for (int act = 0; act < inumber; act++) {
                    if ((c.getLearnPercent() == 9999) || (mr.nextInt(group - vTest) < c.getLearnPercent())) {
                        if (!precomputed) {
                            prevLayer.computeOutputs();
                            precomputed = true;
                        }
                        n[act].storeInputValue(rVector);
                    }
                }
            }
            if (myGraph != null) myGraph.redraw();
            mode = ACTIVE;
        }

        /**
         * choses between serial and parallel version of teahUnits method
         * based on variable multiProcessor and type of trainer
         * by jakub spirk spirk.jakub@gmail.com
         * 05.2008
         *
         * @param n
         * @param inumber
         */
        public void teachUnits(Neuron[] n, int inumber) {
            if (myGraph != null) GraphCanvas.getInstance().setPaintingRMS(true);
            MyTimer timer1 = new MyTimer();
            timer1.timerstart();
            ArrayList parallel = new ArrayList();
            ArrayList serial = new ArrayList();
            for (int i = 0; i < inumber; i++) {
                if ((n[i].trainer.isExecutableInParallelMode()) && (c.isMulticore())) {
                    parallel.add(n[i]);
                } else {
                    serial.add(n[i]);
                }
            }
            if (parallel.size() != 0) {
                teachUnitsParallel(parallel);
            }
            if (serial.size() != 0) {
                teachUnitsSerial(serial);
            }
            double learningTime1 = timer1.timerstop();
            learningTime = learningTime + learningTime1;
            if (myGraph != null) GraphCanvas.getInstance().setPaintingRMS(false);
        }

        /**
         * New version of teachUnits method using threads
         * by jakub spirk spirk.jakub@gmail.com
         * 05.2008
         *
         * @param n
         */
        public void teachUnitsParallel(ArrayList n) {
            job = NEW_NEURON;
            int running = n.size();
            int count = 0;
            TeachThread[] threads = new TeachThread[n.size()];
            while (n.size() - count >= running) {
                for (int i = count; i < count + running; i++) {
                    threads[i] = new TeachThread(i, (Neuron) n.get(i));
                    threads[i].start();
                }
                for (int i = count; i < count + running; i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                count = count + running;
            }
            if (count < n.size()) {
                for (int i = count; i < n.size(); i++) {
                    threads[i] = new TeachThread(i, (Neuron) n.get(i));
                    threads[i].start();
                }
                for (int i = count; i < n.size(); i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        public void setInputsToNeuron(int inputsToNeuron) {
            this.inputsToNeuron = inputsToNeuron;
        }

        public void setPrevLayer(GAMEnetwork.ALayer iLayer) {
            prevLayer = iLayer;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        /**
        * added for multiprocessor support
        * by jakub spirk spirk.jakub@gmail.com
        * 05. 2008
        */
        class TeachThread extends Thread {

            Neuron neuron;

            int act;

            public TeachThread(int i, Neuron n) {
                neuron = n;
                act = i;
            }

            public void run() {
                actualNeuron = act;
                neuron.learnYourself((Unit) neuron);
            }
        }

        public void teachUnitsSerial(ArrayList n) {
            Neuron neuron;
            job = NEW_NEURON;
            for (int act = 0; act < n.size(); act++) {
                actualNeuron = act;
                neuron = (Neuron) n.get(act);
                neuron.learnYourself((Unit) neuron);
            }
        }

        public void computeError(Neuron[] n, int inumber) {
            int vTest = (int) (group * (c.getVectorsInTestingSet() / 100.0));
            int rVector;
            double error;
            job = ERROR_COMPUTING;
            myRandom.resetRandom();
            int count = 0;
            for (int act = 0; act < inumber; act++) {
                n[act].setSquareError(0);
            }
            for (int i = c.isTestOnBothTrainingAndTestingData() ? 0 : (group - vTest); i < group; i++) {
                rVector = 0;
                if (i < group - vTest) {
                    rVector = myRandom.getRandomLearningVector();
                }
                if (i == group - vTest) {
                    myRandom.resetRandom();
                }
                if (i >= group - vTest) {
                    rVector = myRandom.getRandomTestingVector();
                }
                iLayer.setActualVector(rVector);
                mode = PASSIVE;
                prevLayer.computeOutputs();
                for (int act = 0; act < inumber; act++) {
                    error = n[act].getError(rVector);
                    if (myGraph != null) if (GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                        if (cw != null) {
                            cw.setErr(act, rVector, error);
                        }
                    }
                    n[act].setSquareError(n[act].getSquareError() + error * error);
                    layerProgress = (int) (100.0 * (i + (double) act / (double) (inumber - 1)) / (double) (group));
                }
                if (i % 1000 == 1) {
                }
                mode = ACTIVE;
                count++;
            }
            for (int act = 0; act < inumber; act++) {
                double error_data = n[act].getSquareError() / (double) count;
                n[act].setSquareError(error_data);
                double penalty;
                switch(c.getRegularization()) {
                    case NetworkGAMEConfiguration.RMS:
                        break;
                    case NetworkGAMEConfiguration.RMS_PENALTY:
                        n[act].penalizeComplexity();
                        break;
                    case NetworkGAMEConfiguration.RMS_PENALTY_NOISE:
                        n[act].penalizeComplexity();
                        penalty = n[act].getSquareError() - error_data;
                        n[act].setSquareError(error_data * (1 + penalty * data_noise * 100));
                        break;
                }
                if (myGraph != null) if (GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                    if (cw != null) {
                        cw.setSerr(act, n[act].getSquareError());
                    }
                    for (int i = 0; i < inumber; i++) {
                        if (cw != null) {
                            cw.setCorDist(i, act, getCorrelationDistance(n[i], n[act], 50));
                            cw.setGenDist(i, act, g[i].getNormalizedDistance(g[act]));
                        }
                    }
                }
            }
        }

        /**
         * teaching of one layer of the GMDH network
         */
        public double teachLayer(InputLayer iLayer) {
            Neuron swap;
            boolean anyChange;
            if (inumber == 0) {
                return Double.NaN;
            }
            rmsw = new RMSWindow((GAMEnetwork) ja);
            if (myGraph != null) GraphCanvas.getInstance().initializeRMSWindow(rmsw);
            storeTrainingData(n, inumber);
            teachUnits(n, inumber);
            computeError(n, inumber);
            logger.info("Computing errors of neurons");
            if (inumber > 1) {
                if (myGraph != null) {
                    GraphCanvas.getInstance().setDeterministicCrowdingRunning(true);
                    cw = new CrowdingWindow((GAMEnetwork) ja);
                    GraphCanvas.getInstance().initializeCrowdingWindow(cw);
                    deterministic_crowding();
                    GraphCanvas.getInstance().setDeterministicCrowdingRunning(false);
                }
            }
            job = NEURONS_SORTING;
            logger.info("Sorting out neurons according to the validation error and distance");
            Genome swg;
            double dstn1, dstn2, dstn3, dstn4, besterr = n[0].getSquareError();
            int bestindex = 0;
            for (int i = 0; i < inumber; i++) {
                if (n[i].getSquareError() < besterr) {
                    besterr = n[i].getSquareError();
                    bestindex = i;
                }
            }
            swg = g[0];
            g[0] = g[bestindex];
            g[bestindex] = swg;
            swap = n[0];
            n[0] = n[bestindex];
            logger.trace("Neuron swap " + 0 + " and " + bestindex);
            n[bestindex] = swap;
            for (int i = 0; i < inumber; i++) {
                n[i].setRanking(n[i].getSquareError() > 0 ? besterr / n[i].getSquareError() : 1);
            }
            int startIndex = 1;
            do {
                anyChange = false;
                for (int i = inumber - 1; i > startIndex; i--) {
                    if (c.getDistanceMatters() > 0) {
                        dstn1 = dstn2 = 0;
                        dstn3 = dstn4 = 0;
                        for (int k = 0; k < startIndex; k++) {
                            if (c.isGenomeDistance()) {
                                dstn1 += g[i].getNormalizedDistance(g[k]);
                                dstn2 += g[startIndex].getNormalizedDistance(g[k]);
                                if (dstn1 > dstn2) {
                                    dstn2 /= dstn1;
                                    dstn1 = 1;
                                } else {
                                    dstn1 /= dstn2;
                                    dstn2 = 1;
                                }
                            }
                            if (c.isCorrelationDistance()) {
                                dstn3 += getCorrelationDistance(n[i], n[k], 50);
                                dstn4 += getCorrelationDistance(n[i - 1], n[k], 50);
                                if (dstn3 > dstn4) {
                                    dstn4 /= dstn3;
                                    dstn3 = 1;
                                } else {
                                    dstn3 /= dstn4;
                                    dstn4 = 1;
                                }
                                dstn1 += dstn3;
                                dstn2 += dstn4;
                                if (c.isGenomeDistance()) {
                                    dstn1 /= 2;
                                    dstn2 /= 2;
                                }
                            }
                        }
                        dstn1 /= i;
                        dstn2 /= i;
                        dstn1 *= c.getDistanceMatters();
                        dstn2 *= c.getDistanceMatters();
                        dstn1 += 1;
                        dstn2 += 1;
                    } else {
                        dstn1 = dstn2 = 1;
                    }
                    if ((n[i].getSquareError() / dstn1) < (n[i - 1].getSquareError() / dstn2)) {
                        anyChange = true;
                        swg = g[i];
                        g[i] = g[i - 1];
                        g[i - 1] = swg;
                        swap = n[i];
                        n[i] = n[i - 1];
                        n[i - 1] = swap;
                        logger.trace("Neuron swap " + i + " and " + (i - 1));
                    }
                }
                startIndex++;
                layerProgress = 100 * (1 - (inumber - startIndex - 1) / (inumber - 1));
                if (myGraph != null) if (startIndex % 10 == 3) {
                    myGraph.redraw();
                }
            } while (anyChange);
            if (myGraph != null) if (GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                GraphCanvas.getInstance().setDeterministicCrowdingRunning(true);
                computeError(n, inumber);
                cw.setErrDataReady(true);
                myGraph.redraw();
                cw.setErrDataReady(false);
                if (GMDHtree.autoCapture) {
                    myGraph.captureErrWin();
                }
                GraphCanvas.getInstance().setDeterministicCrowdingRunning(false);
            }
            layerProgress = 100;
            if (myGraph != null) myGraph.redraw();
            job = NONE;
            if (c.isCrowdingEmployed() && c.isGenomeDistance()) {
                int ii = 0;
                do {
                    if (g[ii] != null) {
                        for (int j = ii + 1; j < inumber; j++) {
                            if ((g[j] != null) && g[ii].equals(g[j])) {
                                g[j] = null;
                                n[j] = null;
                                logger.trace("Neuron deleted " + j + "(worse performance than niche leader)");
                            }
                        }
                    }
                    ii++;
                } while (ii < inumber);
            }
            if (myGraph != null) if (GraphCanvas.getInstance().isVisualInspectionEnabled()) {
                for (int i = 0; i < inumber; i++) {
                    if (n[i] == null) {
                        cw.setSerr(i, 500);
                    }
                }
                GraphCanvas.getInstance().setDeterministicCrowdingRunning(true);
                cw.setErrDataReady(true);
                myGraph.redraw();
                cw.setErrDataReady(false);
                if (GMDHtree.autoCapture) {
                    myGraph.captureErrWin();
                }
                GraphCanvas.getInstance().setDeterministicCrowdingRunning(false);
            }
            double lastbest = Double.MAX_VALUE;
            if (this.getPreviousLayer().getType() == GAME_LAYER) {
                lastbest = ((GAMEnetwork.Layer) getPreviousLayer()).n[0].getSquareError();
            }
            if (c.isDeleteWorse()) {
                for (int i = 1; i < inumber; i++) {
                    if (n[i] != null) {
                        if (n[i].getSquareError() > lastbest) {
                            n[i] = null;
                            logger.trace("Neuron deleted " + i + "(worse performance than previous layer elite)");
                        }
                    }
                }
            }
            int ii = 0;
            do {
                if (g[ii] == null) {
                    int jj = ii;
                    while ((jj < inumber) && (g[jj] == null)) {
                        jj++;
                    }
                    if (jj < inumber) {
                        g[ii] = g[jj];
                        g[jj] = null;
                        n[ii] = n[jj];
                        n[jj] = null;
                        logger.trace("Neuron " + jj + " moved to " + ii);
                    } else {
                        ii = inumber;
                    }
                }
                ii++;
            } while (ii < inumber);
            ii = 0;
            do {
                if (ii > c.getLayerNeuronsNumber(getLayerIndex())) {
                    if (n[ii] != null) {
                        n[ii] = null;
                        logger.trace("Neuron deleted " + ii + "(max surviving treshold exceeded)");
                    }
                }
                ii++;
            } while (ii < inumber);
            number = 0;
            while ((number < inumber) && n[number] != null) {
                number++;
            }
            if (number == 0) {
                return Double.NaN;
            }
            for (int i = 0; i < number; i++) {
                addGeneString(getInputsEmployed(g[i]));
            }
            return n[0].getSquareError();
        }

        public int getLayerProgress() {
            return layerProgress;
        }

        public void computeOutputs() {
            prevLayer.computeOutputs();
            for (int i = 0; i < number; i++) {
                if (n[i] != null) {
                    n[i].getOutputValue();
                }
            }
        }

        public int getType() {
            return GAME_LAYER;
        }
    }

    /**
     * Inicializes the network
     *
     * @param iname the name of the network usually corresponds to the name of the
     *              <p/>
     *              output attribute
     * @param boss  the parent application
     * @param info  input output data and many more functions
     * @param gr    output graph
     * @param gc
     * @param cfg   the configuration of this network
     */
    public GAMEnetwork(String iname, GMDHtree boss, TreeData info, GraphCanvas gr, NetworkConfiguration gc, NetworkGAMEConfiguration cfg) {
        super(iname, boss, info, gr, gc);
        c = cfg;
    }

    /**
     * generates the GMDH network (learning process)
     */
    public void constructNetwork() {
        MyTimer timer = new MyTimer();
        timer.timerstart();
        myData = GlobalData.getInstance();
        logger.info("GAME model construction begins");
        logger.info("Name of the model:" + getName());
        logger.info("Number of inputs:" + myData.getINumber());
        double layerError, firstLayerErr;
        iLayer = new InputLayer();
        double sqerr;
        int i = 0;
        data_noise = 0;
        double avg = 0;
        int crt = c.getCriterion();
        logger.info("Index of the target variable:" + crt);
        logger.info("Name of the target variable:" + ((OutputAttribute) myData.getOAttr().elementAt(crt)).getName());
        for (int j = 0; j < group; j++) avg += oattr[j][crt];
        avg /= group;
        for (int j = 0; j < group; j++) data_noise += (oattr[j][crt] - avg) * (oattr[j][crt] - avg);
        data_noise /= group;
        System.out.println("Network: " + this.getName());
        varsig = new float[myData.getINumber()];
        feature = new double[myData.getINumber()];
        overvarsig = new int[myData.getINumber()];
        overvarsigArray = new int[20][myData.getINumber()];
        overvarsigProcArray = new int[20][myData.getINumber()];
        for (int ii = 0; ii < myData.getINumber(); ii++) {
            logger.info("Name of input variable (" + ii + "):" + ((InputFactor) myData.getIFactor().elementAt(ii)).getName());
            varsig[ii] = 0;
            feature[ii] = 0;
            overvarsig[ii] = 0;
            for (int jj = 0; jj < 20; jj++) {
                overvarsigArray[jj][ii] = 0;
                overvarsigProcArray[jj][ii] = 0;
            }
        }
        layerErr = new RMSData(0.001, 40, 0, NetworkGAMEConfiguration.MAX_LAYERS);
        layerErr.reset();
        myRandom.generateLearningAndTestingSet((int) (group * (c.getVectorsInTestingSet() / 100.0)));
        maxNeuronsInLayer = c.getLayerInitialNeuronsNumber(i);
        actualLayer = i;
        layerError = firstLayerErr = Double.MAX_VALUE;
        if (myGraph != null) GraphCanvas.getInstance().setLayerConstruction(true);
        gD = new String[NetworkGAMEConfiguration.MAX_UNITS_USED];
        genesDefined = 0;
        layer[i] = new Layer(i, iLayer, i + 1);
        while ((sqerr = layer[i].teachLayer(iLayer)) < layerError) {
            if (i == 0) {
                firstLayerErr = sqerr;
            }
            if (i > 1) {
                if (((layerError - sqerr) * 30.0) < (firstLayerErr - layerError)) {
                    if (!c.isBuildWhileDec()) {
                        actualLayer = ++i;
                        layerError = sqerr;
                        layerErr.add(sqerr);
                        break;
                    }
                }
            }
            output = layer[i].n[0];
            actualLayer = ++i;
            layerError = sqerr;
            layerErr.add(sqerr);
            if (i > NetworkGAMEConfiguration.MAX_LAYERS - 2) {
                break;
            }
            maxNeuronsInLayer = c.getLayerInitialNeuronsNumber(i);
            layer[i] = new Layer(i, layer[i - 1], i + 1);
        }
        logger.info("GAME model construction ends");
        layerErr.add(sqerr);
        lastLayer = i - 1;
        c.getLayerEpochs(i);
        gD = null;
        ivector = oattr = null;
        crit = null;
        if (myGraph != null) GraphCanvas.getInstance().setLayerConstruction(false);
    }

    private void printFeatureRankingToFile(int[][] rankingArray, int i, int layerEpochs, int[][] procenta) {
        File frFile = new File("featureRanking_" + layerEpochs + ".out");
        try {
            FileOutputStream fos = new FileOutputStream(frFile, true);
            PrintWriter fw = new PrintWriter(fos, true);
            System.out.println("");
            for (int frr = 0; frr <= i; frr++) {
                for (int frc = 0; frc < super.myData.getINumber(); frc++) {
                    fw.append(Double.toString(procenta[frr][frc] / 100.0).replace('.', ',') + "\t");
                }
                fw.append('\n');
            }
            for (int frr = 0; frr <= i; frr++) {
                for (int frc = 0; frc < super.myData.getINumber(); frc++) {
                    fw.append(Integer.toString(rankingArray[frr][frc]) + "\t");
                    System.out.print(rankingArray[frr][frc] + " ");
                }
                fw.append('\n');
                System.out.println("");
            }
            fw.append('\n');
            fw.close();
        } catch (FileNotFoundException fnfe) {
            System.out.print("soubor nenealezen v printFeatureRankingToFile()");
        } catch (IOException e) {
            System.out.println("IOE chyba v print feature ranking array");
            e.printStackTrace();
        }
    }

    /**
     * detete redundant units
     */
    public void prune() {
        clearUsedFlags();
        addUsed(output);
        int al = lastLayer + 1;
        while (layer[al] == null) al--;
        while (al >= 0) {
            for (int i = 0; i < layer[al].getINumber(); i++) {
                if (layer[al].n[i] != null) {
                    if (!layer[al].n[i].isUsed()) {
                        layer[al].n[i] = null;
                    }
                }
            }
            al--;
        }
    }

    private void clearUsedFlags() {
        int llay = lastLayer + 1;
        while (llay >= 0) {
            if (layer[llay] != null) {
                for (int i = 0; i < layer[llay].getINumber(); i++) {
                    if (layer[llay].n[i] != null) {
                        layer[llay].n[i].setUsed(false);
                    }
                }
            }
            llay--;
        }
        for (int i = 0; i < iLayer.getINumber(); i++) {
            iLayer.n[i].setUsed(false);
        }
    }

    public void computeSignificance() {
        for (int j = 0; j < super.myData.getINumber(); j++) {
            feature[j] = 0;
        }
        int llay = lastLayer + 1;
        while (llay >= 0) {
            if (layer[llay] != null) {
                for (int i = 0; i < layer[llay].getINumber(); i++) {
                    if (layer[llay].n[i] != null) {
                        enrichFeatures(layer[llay].n[i]);
                    }
                }
            }
            llay--;
        }
    }

    /**
     * add contributions of a unit to input features the unit is connected to
     *
     * @param unit a unit to be processed
     */
    private void enrichFeatures(Neuron unit) {
        this.clearUsedFlags();
        addUsed(unit);
        for (int i = 0; i < iLayer.getINumber(); i++) {
            if (iLayer.n[i].isUsed()) {
                feature[i] += unit.getRanking();
            }
        }
    }

    void addUsed(Neuron unit) {
        if (!unit.isUsed()) {
            unit.setUsed(true);
            for (int i = 0; i < unit.getInputs(); i++) {
                if (unit.f[i] != null) {
                    if (unit.f[i] instanceof Neuron) {
                        addUsed((Neuron) unit.f[i]);
                    } else {
                        unit.f[i].setUsed(true);
                    }
                }
            }
        }
    }

    void addGeneString(String s) {
        gD[genesDefined++] = s;
    }

    String getGeneString(int id) {
        return gD[id];
    }

    String getInputsEmployed(Genome g) {
        String s, pom;
        char[] p = new char[iNum];
        int ipos = g.inputsPossible;
        for (int j = 0; j < iNum; j++) {
            if (g.getInput(j) == 1) {
                p[j] = '1';
            } else {
                p[j] = '0';
            }
        }
        for (int k = iNum; k < ipos; k++) {
            if (g.getInput(k) == 1) {
                pom = getGeneString(k - iNum);
                for (int j = 0; j < iNum; j++) {
                    if ((p[j] == '0') && (pom.charAt(j) == '1')) {
                        p[j] = '1';
                    }
                }
            }
        }
        s = new String(p);
        return s;
    }

    /**
     * returns currently processed unit
     */
    public Neuron getProcessedNeuron() {
        return (layer[actualLayer].n[actualNeuron]);
    }

    /**
     * returns currently processed layer
     */
    public GMDHnetwork.Layer getProcessedLayer() {
        return (layer[actualLayer]);
    }

    /**
     * returns the response of the network
     *
     * @param vector input vector
     */
    public double getResponse(double[] vector) {
        double voutput;
        System.arraycopy(vector, 0, inputVector, 0, iNum);
        if (output == null) {
            return Double.NaN;
        }
        if (lastLayer < 0) {
            return Double.NaN;
        }
        while (layer[lastLayer] == null && (lastLayer > 0)) {
            lastLayer--;
        }
        if (layer[lastLayer] == null) {
            return Double.NaN;
        }
        if (normalization) {
            for (int i = 0; i < iNum; i++) {
                inputVector[i] = ((InputFactor) GlobalData.getInstance().iFactor.elementAt(i)).getStandardValue(vector[i]);
            }
        }
        iLayer.setActualVector(group);
        mode = PASSIVE;
        layer[lastLayer].computeOutputs();
        voutput = output.getOutputValue();
        mode = ACTIVE;
        if (normalization) {
        }
        return voutput;
    }

    /**
     * returns the response of the network normalized to <0,1>
     *
     * @param vector input vector should be given in normalized form to <0,1>
     */
    public double getStandardResponse(double[] vector) {
        double voutput = Double.NaN;
        if (output == null) {
            return voutput;
        }
        if (lastLayer < 0) {
            return voutput;
        }
        while (layer[lastLayer] == null && (lastLayer > 0)) {
            lastLayer--;
        }
        if (layer[lastLayer] == null) {
            return voutput;
        }
        if (!normalization) {
            for (int i = 0; i < iNum; i++) {
                vector[i] = ((InputFactor) GlobalData.getInstance().iFactor.elementAt(i)).decodeStandardValue(vector[i]);
            }
        }
        System.arraycopy(vector, 0, inputVector, 0, iNum);
        iLayer.setActualVector(group);
        mode = PASSIVE;
        layer[lastLayer].computeOutputs();
        voutput = output.getOutputValue();
        mode = ACTIVE;
        return ((OutputAttribute) GlobalData.getInstance().oAttr.elementAt(c.getCriterion())).getStandardValue(voutput);
    }

    /**
     * @return string with estimated percentage significance of inputs
     */
    public String getFeaturesSignificance() {
        computeSignificance();
        double allf = 0;
        for (int i = 0; i < iNum; i++) {
            allf += feature[i];
        }
        String s = "";
        for (int i = 0; i < iNum; i++) {
            s += Integer.toString((int) (100 * feature[i] / allf)) + "% ";
        }
        return s;
    }

    public String toSimplifiedEquation() {
        if (output == null) {
            return "null";
        }
        String s;
        s = output.toSimplifiedEquation();
        if (normalization) {
            OutputAttribute oatr = ((OutputAttribute) GlobalData.getInstance().oAttr.elementAt(c.getCriterion()));
            double range = oatr.getMax() - oatr.getMin();
            if (range != 1) s = "(" + s + ")*" + range;
            if (oatr.getMin() >= 0) s = s + " +" + oatr.getMin(); else if (oatr.getMin() < 0) s = s + " " + oatr.getMin();
        }
        Grammar eq = new Grammar(s);
        s = eq.simplify();
        return s;
    }

    /**
     * toEquation
     *
     * @return String The transfer function of the network
     */
    public String toEquation() {
        if (output == null) {
            return "null";
        }
        String s;
        s = output.toEquation();
        if (normalization) {
            OutputAttribute oatr = ((OutputAttribute) GlobalData.getInstance().oAttr.elementAt(c.getCriterion()));
            double range = oatr.getMax() - oatr.getMin();
            if (range != 1) s = "(" + s + ")*" + range;
            if (oatr.getMin() >= 0) s = s + " +" + oatr.getMin(); else if (oatr.getMin() < 0) s = s + " " + oatr.getMin();
        }
        return s;
    }

    /**
     * getSquareError
     *
     * @return double error of the best neuron
     */
    public double getSquareError() {
        if (output != null) return output.getSquareError();
        return Double.NaN;
    }

    public void setOutputNeuron(Neuron n) {
        output = n;
    }

    public Neuron getOutputNeuron() {
        return output;
    }

    public int getNumberOfLayers() {
        int i = 0;
        while (this.layer[i] != null) {
            i++;
        }
        return i;
    }

    public Layer getNewLayer() {
        Layer layer = new Layer();
        return layer;
    }

    /**
     * Return new layer created by using PMML element "NeuralLayer".
     *
     * @param e is the "NeuralNetwork" element.
     * @return new layer.
     * @author Vit Ulicny
     */
    public Layer getNewLayer(org.w3c.dom.Element e) {
        Layer layer = new Layer(e);
        for (int i = 0; i < layer.n.length; i++) {
            layer.n[i].myNet = this;
        }
        return layer;
    }

    /**
     * Return new input layer created by using PMML element "NeuralInputs".
     *
     * @param e              is the "NeuralInputs" element.
     * @param numberOfInputs is the number of netwrok's inputs.
     * @return new input layer.
     * @author Vit Ulicny
     */
    public InputLayer getNewiLayer(Element e, int numberOfInputs) {
        netInputs = numberOfInputs;
        return new InputLayer(e);
    }

    /**
     * Return neuron with ID.
     *
     * @param id is the ID of the neuron.
     * @return ANeuron
     * @author Vit Ulicny
     */
    public ANeuron getNeuron(int id) {
        int i = 0;
        while (i < this.netInputs) {
            if (id == this.iLayer.n[i].getId()) {
                return this.iLayer.n[i];
            }
            i++;
        }
        i = 0;
        int j = 0;
        int d = this.layer.length;
        while (i < d) {
            while (j < this.layer[i].n.length) {
                if (id == this.layer[i].n[j].getId()) {
                    return this.layer[i].n[j];
                }
                j++;
            }
            j = 0;
            i++;
        }
        return null;
    }

    /**
    * added for time measurments
    * by spirk.jakub@gmail.com
    */
    public class MyTimer {

        long startTime;

        long time;

        public void timerstart() {
            startTime = System.currentTimeMillis();
        }

        public long timerstop() {
            long endTime = System.currentTimeMillis();
            time = endTime - startTime;
            return time;
        }
    }

    public int getActualLayerNumber() {
        return actualLayer;
    }

    /**
     * Returns significance of particular input variable
     * @param index index of the input varible
     * @return significance derived from the niching genetic algorithm, -1 if overvarsig is null, -2 if index out of bounds
     */
    public float getFeatureSignificance(int index) {
        if (overvarsig == null) return -1;
        if (overvarsig.length <= index) return -2;
        return overvarsig[index];
    }

    public java.awt.Dimension getNeuronParentPosition(Neuron n, int inputNumber) {
        if (n.f[inputNumber] == null) return null;
        int layerDistance = 1;
        int neuronIndex = 0;
        GMDHnetwork.ALayer find;
        if (layer[0] == null) {
            find = iLayer;
            while ((find.getNeuron(neuronIndex) != n.f[inputNumber]) && (neuronIndex < find.getINumber())) neuronIndex++;
            if (neuronIndex < find.getINumber()) return new java.awt.Dimension(neuronIndex, layerDistance); else return null;
        }
        int last = 0;
        while (layer[last] != null) last++;
        find = layer[--last];
        boolean notFound = true;
        int neuronlayer = 0;
        while (notFound) {
            neuronIndex = 0;
            while ((find.getNeuron(neuronIndex) != n) && (neuronIndex < find.getINumber())) neuronIndex++;
            if (neuronIndex < find.getINumber()) {
                notFound = false;
            } else {
                find = find.getPreviousLayer();
                if (find == null) {
                    neuronlayer = -1;
                    break;
                }
                neuronlayer++;
            }
        }
        if (last - (neuronlayer + 1) < 0) find = iLayer; else find = layer[last - (neuronlayer + 1)];
        neuronIndex = 0;
        notFound = true;
        while (notFound) {
            neuronIndex = 0;
            while ((find.getNeuron(neuronIndex) != n.f[inputNumber]) && (neuronIndex < find.getINumber())) neuronIndex++;
            if (neuronIndex < find.getINumber()) {
                notFound = false;
            } else {
                find = find.getPreviousLayer();
                layerDistance++;
                if (find == null) break;
            }
        }
        if (notFound) return null;
        return new java.awt.Dimension(neuronIndex, layerDistance);
    }
}
