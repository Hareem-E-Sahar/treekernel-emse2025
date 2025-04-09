package org.neuroph.netbeans.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.ServiceLoader;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.transfer.Trapezoid;
import org.neuroph.netbeans.ideservices.CreateNeuralNetworkFileServiceInterface;
import org.neuroph.netbeans.ideservices.CreateTrainigSetFileServiceInterface;
import org.neuroph.netbeans.main.easyneurons.MessageBoxTopComponent;
import org.neuroph.netbeans.main.easyneurons.NeuralNetworkTopComponent;
import org.neuroph.netbeans.main.easyneurons.NeuralNetworkTraining;
import org.neuroph.netbeans.main.easyneurons.TrainingSetTopComponent;
import org.neuroph.netbeans.main.easyneurons.dialog.NeuronPropertiesFrameTopComponent;
import org.neuroph.netbeans.main.easyneurons.dialog.SupervisedTrainingMonitorFrameTopComponent;
import org.neuroph.netbeans.main.easyneurons.errorgraph.GraphFrameTopComponent;
import org.neuroph.netbeans.main.easyneurons.samples.BasicNeuronSampleTopComponent;
import org.neuroph.netbeans.main.easyneurons.samples.KohonenSampleTopComponent;
import org.neuroph.netbeans.main.easyneurons.samples.NFRSampleTopComponent;
import org.neuroph.netbeans.main.easyneurons.samples.perceptron.PerceptronSampleTrainingSet;
import org.neuroph.netbeans.visual.GraphViewTopComponent;
import org.neuroph.nnet.Kohonen;
import org.neuroph.nnet.NeuroFuzzyPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.BinaryDeltaRule;

/**
 *
 * @author user
 */
public class ViewManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static ViewManager instance;

    private HashMap<NeuralNetwork, NeuralNetworkTopComponent> openedNetworks = new HashMap<NeuralNetwork, NeuralNetworkTopComponent>();

    private HashMap<TrainingSet, TrainingSetTopComponent> openedTrainingSets = new HashMap<TrainingSet, TrainingSetTopComponent>();

    private MessageBoxTopComponent messageBox;

    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    private ViewManager() {
    }

    /**
         *  Opens NetworkViewFrameTopComponent
         * @param nnet - inputs neural network that will be shown
         * @param trainingSets - input training sets that will be loaded in training sets combo box
         */
    public void openNeuralNetworkWindow(NeuralNetwork nnet) {
        NeuralNetworkTopComponent networkTopComponent = null;
        if (openedNetworks.containsKey(nnet)) {
            networkTopComponent = openedNetworks.get(nnet);
            networkTopComponent.requestActive();
        } else {
            networkTopComponent = new NeuralNetworkTopComponent(nnet);
            networkTopComponent.open();
            openedNetworks.put(nnet, networkTopComponent);
            networkTopComponent.requestActive();
        }
    }

    public void openVisualEditorWindow(NeuralNetwork nnet) {
        GraphViewTopComponent visualEditor = new GraphViewTopComponent(nnet);
        visualEditor.open();
        visualEditor.requestActive();
    }

    public void onNetworkClose(NeuralNetwork nnet) {
        openedNetworks.remove(nnet);
    }

    public void onTrainingSetClose(TrainingSet tset) {
        openedTrainingSets.remove(tset);
    }

    /**
         *  Opens TrainigSetEditFrameTopComponent - opened by double clicking on training set
         * @param trainingSet - input trainig set that will be edited
         */
    public void openTrainingSetWindow(TrainingSet trainingSet) {
        TrainingSetTopComponent trainingSetTopComponent = null;
        if (openedTrainingSets.containsKey(trainingSet)) {
            trainingSetTopComponent = openedTrainingSets.get(trainingSet);
            trainingSetTopComponent.requestActive();
        } else {
            trainingSetTopComponent = new TrainingSetTopComponent(trainingSet);
            trainingSetTopComponent.open();
            openedTrainingSets.put(trainingSet, trainingSetTopComponent);
            trainingSetTopComponent.requestActive();
        }
    }

    /**
         *  Opens NeuornPropertiseFrame
         * @param neuron - input neuron which properties will be shown
         */
    public void openNeuronPropertiesFrame(Neuron neuron) {
        NeuronPropertiesFrameTopComponent neuronPropertiesFrame = NeuronPropertiesFrameTopComponent.findInstance();
        neuronPropertiesFrame.setNeuronForNeuronPropertiesFrame(neuron);
        neuronPropertiesFrame.open();
        neuronPropertiesFrame.requestActive();
    }

    /**
         *  Opens TrainingEditFrameTopComponent - opened by TrainingSet Wizard
         * @param inputs - number of inputs
         * @param outputs - number of outputs
         * @param type - type of trainig set
         * @param label - label name of training set
         */
    public void showTrainingSetEditFrame(int inputs, int outputs, String type, String label) {
        TrainingSet trainingSet = new TrainingSet(label);
        TrainingSetTopComponent trainingSetTopComponent = new TrainingSetTopComponent();
        trainingSetTopComponent.setTrainingSetEditFrameVariables(trainingSet, type, inputs, outputs);
        trainingSetTopComponent.open();
        trainingSetTopComponent.requestActive();
    }

    public void showTrainingSetEditFrame(TrainingSet trainingSet, int inputs, int outputs, String type, String label) {
        TrainingSetTopComponent trainingSetTopComponent = new TrainingSetTopComponent();
        trainingSetTopComponent.setTrainingSetEditFrameVariables(trainingSet, type, inputs, outputs);
        trainingSetTopComponent.open();
        trainingSetTopComponent.requestActive();
    }

    public SupervisedTrainingMonitorFrameTopComponent openMonitorFrame(NeuralNetworkTraining trainingController) {
        SupervisedTrainingMonitorFrameTopComponent monitorFrame = SupervisedTrainingMonitorFrameTopComponent.findInstance();
        monitorFrame.setSupervisedTrainingMonitorFrameVariables(trainingController);
        monitorFrame.open();
        monitorFrame.requestActive();
        return monitorFrame;
    }

    /**
         *  Opens ErrorGraphTopComponent
         * @return
         */
    public GraphFrameTopComponent openErrorGraphFrame() {
        GraphFrameTopComponent graphFrame = new GraphFrameTopComponent();
        graphFrame.open();
        graphFrame.requestActive();
        return graphFrame;
    }

    /**
         *  Opens Kohonen sample
         */
    public void kohonenSample() {
        int sampleSize = 100;
        NeuralNetwork neuralNet = new Kohonen(new Integer(2), new Integer(sampleSize));
        neuralNet.setLabel("KohonenNet");
        TrainingSet trainingSet = new TrainingSet();
        trainingSet.setLabel("Sample training set");
        for (int i = 0; i < sampleSize; i++) {
            ArrayList<Double> trainVect = new ArrayList<Double>();
            trainVect.add(Math.random());
            trainVect.add(Math.random());
            TrainingElement te = new TrainingElement(trainVect);
            trainingSet.addElement(te);
        }
        NeuralNetworkTraining controller = new NeuralNetworkTraining(neuralNet, trainingSet);
        KohonenSampleTopComponent kohonenVisualizer = new KohonenSampleTopComponent();
        kohonenVisualizer.setNeuralNetworkTrainingController(controller);
        neuralNet.getLearningRule().addObserver(kohonenVisualizer);
        neuralNet.addObserver(kohonenVisualizer);
        for (CreateNeuralNetworkFileServiceInterface fileservices : ServiceLoader.load(CreateNeuralNetworkFileServiceInterface.class)) {
            fileservices.serialise(neuralNet);
        }
        for (CreateTrainigSetFileServiceInterface fileservices : ServiceLoader.load(CreateTrainigSetFileServiceInterface.class)) {
            fileservices.serialise(trainingSet);
        }
        kohonenVisualizer.setVisible(true);
        kohonenVisualizer.open();
        kohonenVisualizer.requestActive();
        showMessage("Created Kohonen Sample");
    }

    /**
         * Opens NFRSample
         */
    public void nfrSample() {
        double[][] pointsSets = { { 0, 0, 20, 22 }, { 20, 22, 40, 42 }, { 40, 42, 80, 82 }, { 80, 82, 100, 100 } };
        double[][] timeSets = { { 15, 15, 20, 25 }, { 20, 25, 35, 40 }, { 35, 40, 1000, 1000 } };
        NeuralNetwork nnet = new NeuroFuzzyPerceptron(pointsSets, timeSets);
        TrainingSet tSet = new TrainingSet();
        Layer setLayer = nnet.getLayerAt(1);
        int outClass = 0;
        for (int i = 0; i <= 3; i++) {
            Neuron icell = setLayer.getNeuronAt(i);
            Trapezoid tfi = (Trapezoid) icell.getTransferFunction();
            double r1i = tfi.getRightLow();
            double l2i = tfi.getLeftHigh();
            double r2i = tfi.getRightHigh();
            double right_intersection_i = r2i + (r1i - r2i) / 2;
            for (int j = 6; j >= 4; j--) {
                Neuron jcell = setLayer.getNeuronAt(j);
                Trapezoid tfj = (Trapezoid) jcell.getTransferFunction();
                double r1j = tfj.getRightLow();
                double l2j = tfj.getLeftHigh();
                double r2j = tfj.getRightHigh();
                double right_intersection_j = r2j + (r1j - r2j) / 2;
                String outputPattern;
                if (outClass <= 3) {
                    outputPattern = "1 0 0 0";
                } else if ((outClass >= 4) && (outClass <= 6)) {
                    outputPattern = "0 1 0 0";
                } else if ((outClass >= 7) && (outClass <= 9)) {
                    outputPattern = "0 0 1 0";
                } else {
                    outputPattern = "0 0 0 1";
                }
                String inputPattern = Double.toString(l2i) + " " + Double.toString(l2j);
                SupervisedTrainingElement tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(l2i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(l2i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(l2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(r2i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(l2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(r2j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                inputPattern = Double.toString(right_intersection_i) + " " + Double.toString(right_intersection_j);
                tEl = new SupervisedTrainingElement(inputPattern, outputPattern);
                tSet.addElement(tEl);
                outClass++;
            }
        }
        nnet.setLabel("NFR sample");
        tSet.setLabel("NFR tset");
        NeuralNetworkTraining controller = new NeuralNetworkTraining(nnet, tSet);
        for (CreateNeuralNetworkFileServiceInterface fileservices : ServiceLoader.load(CreateNeuralNetworkFileServiceInterface.class)) {
            fileservices.serialise(nnet);
        }
        for (CreateTrainigSetFileServiceInterface fileservices : ServiceLoader.load(CreateTrainigSetFileServiceInterface.class)) {
            fileservices.serialise(tSet);
        }
        NFRSampleTopComponent frame = new NFRSampleTopComponent();
        frame.setNeuralNetworkTrainingController(controller);
        frame.open();
        frame.requestActive();
    }

    /**
         *  Shows message in MessageBoxTopComponent
         * @param message - input message that will be displayed
         */
    public void showMessage(String message) {
        messageBox.getDefault().addMessage(message);
    }

    /**
         *  Opens BasicNeuronSample
         */
    public void showBasicNeuronSample() {
        BasicNeuronSampleTopComponent sample = new BasicNeuronSampleTopComponent();
        sample.open();
        sample.requestActive();
        showMessage("Started Basic Neuron Sample");
    }

    /**
         * Opens RecommenderSample
         */
    public void recommenderSample() {
        NeuralNetwork nnet = new org.neuroph.contrib.RecommenderNetwork();
        ((org.neuroph.contrib.RecommenderNetwork) nnet).createDemoNetwork();
        TrainingSet tSet = new TrainingSet();
        nnet.setLabel("Recommender sample");
        tSet.setLabel("E-commerce tset");
        NeuralNetworkTraining controller = new NeuralNetworkTraining(nnet, tSet);
        for (CreateNeuralNetworkFileServiceInterface fileservices : ServiceLoader.load(CreateNeuralNetworkFileServiceInterface.class)) {
            fileservices.serialise(nnet);
        }
        for (CreateTrainigSetFileServiceInterface fileservices : ServiceLoader.load(CreateTrainigSetFileServiceInterface.class)) {
            fileservices.serialise(tSet);
        }
    }

    /**
         *  Opens PerceptronSample
         */
    public void showPerceptronSample() {
        NeuralNetwork neuralNet = new Perceptron(new Integer(2), new Integer(1));
        neuralNet.setLearningRule(new BinaryDeltaRule());
        neuralNet.setLabel("PerceptronSampleNetwork");
        org.neuroph.netbeans.main.easyneurons.samples.perceptron.TrainingSetObserver trainingSet = new org.neuroph.netbeans.main.easyneurons.samples.perceptron.TrainingSetObserver() {

            @Override
            public void update(Observable o, Object arg) {
                TrainingSet t = new TrainingSet();
                super.update(o, arg);
                t = getTrainingSet();
                t.setLabel("Perceptron Sample Training Set");
            }
        };
        org.neuroph.netbeans.main.easyneurons.samples.perceptron.PerceptronSampleTrainingSet pst = new PerceptronSampleTrainingSet();
        pst.addObserver(trainingSet);
        TrainingSet ts = trainingSet.getTrainingSet();
        NeuralNetworkTraining nnTraining = new NeuralNetworkTraining(neuralNet, ts);
        org.neuroph.netbeans.main.easyneurons.samples.perceptron.PerceptronSampleFrameTopComponent perceptronVisualizer = new org.neuroph.netbeans.main.easyneurons.samples.perceptron.PerceptronSampleFrameTopComponent();
        perceptronVisualizer.setTrainingControllerAndTrainingSet(nnTraining, pst);
        neuralNet.getLearningRule().addObserver(perceptronVisualizer);
        neuralNet.addObserver(perceptronVisualizer);
        perceptronVisualizer.open();
        perceptronVisualizer.requestActive();
        showMessage("Started Perceptron Sample");
    }

    public void showMultiLayerPerceptronSample() {
        org.neuroph.netbeans.main.easyneurons.samples.mlperceptron.NeuralNetObserver markovNeuralNet = new org.neuroph.netbeans.main.easyneurons.samples.mlperceptron.NeuralNetObserver() {

            @Override
            public void update(Observable o, Object arg) {
                NeuralNetwork net = getNnet();
                super.update(o, arg);
                net = getNnet();
            }
        };
        org.neuroph.netbeans.main.easyneurons.samples.perceptron.TrainingSetObserver trainingSet = new org.neuroph.netbeans.main.easyneurons.samples.perceptron.TrainingSetObserver() {

            @Override
            public void update(Observable o, Object arg) {
                TrainingSet t = getTrainingSet();
                super.update(o, arg);
                t = getTrainingSet();
            }
        };
        org.neuroph.netbeans.main.easyneurons.samples.perceptron.PerceptronSampleTrainingSet pst = new PerceptronSampleTrainingSet();
        pst.addObserver(trainingSet);
        pst.addObserver(markovNeuralNet);
        NeuralNetwork nnet = markovNeuralNet.getNnet();
        nnet.setLearningRule(new BackPropagation());
        TrainingSet ts = trainingSet.getTrainingSet();
        org.neuroph.netbeans.main.easyneurons.samples.mlperceptron.MultiLayerPerceptronSampleTopComponent backpropagationVisualizer = new org.neuroph.netbeans.main.easyneurons.samples.mlperceptron.MultiLayerPerceptronSampleTopComponent();
        backpropagationVisualizer.setTrainingSetForMultiLayerPerceptronSample(pst);
        backpropagationVisualizer.setVisible(true);
        backpropagationVisualizer.open();
        backpropagationVisualizer.requestActive();
        showMessage("Started Multi Layer Perceptron with Backpropagation Sample");
    }
}
