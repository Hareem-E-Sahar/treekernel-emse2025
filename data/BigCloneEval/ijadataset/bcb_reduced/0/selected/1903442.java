package org.galab.saveableobject.controller;

import org.galab.util.*;

/** 
 * A connection between two continuous neurons (normally)
 * which is plastic, with its weight changing according to
 * a rule which uses the presynaptic and postsynaptic cell
 * potentials.
 */
public class PlasticConnection extends Connection {

    public PlasticConnection() {
        super();
        doGain();
        changedWeight = null;
    }

    public PlasticConnection(PlasticConnection other) {
        super(other);
        setRule(other.getRule());
        setN(other.getN());
        setGainLevel(other.getGainLevel());
        setHasGain(other.getHasGain());
        doGain();
        changedWeight = null;
    }

    private void doGain() {
        switch(getHasGain()) {
            case 0:
                gain = 1;
                break;
            case 1:
                if (gainLevel != null) {
                    gain = Util.mapExponential(gainLevel.doubleValue(), 0, 1, 0.01, 10);
                } else {
                    gain = 1;
                }
                break;
            case 2:
                if (gainLevel != null) {
                    gain = 1 / Util.mapExponential(gainLevel.doubleValue(), 0, 1, 0.01, 10);
                } else {
                    gain = 1;
                }
                break;
        }
    }

    /**
	 * Called every time step to pass activation from the pre-neuron to the
	 * post.
	 */
    public void process() {
        if (changedWeight == null) {
            changedWeight = new Double(getWeight());
        }
        passActivation();
        updateWeight();
    }

    public void passActivation() {
        double gained = gain * Util.mapLinear(input.getActivation(), mapFiringRateFromMin, mapFiringRateFromMax, mapFiringRateToMin, mapFiringRateToMax);
        double inp = changedWeight.doubleValue() * gained;
        output.augmentActivation(inp);
    }

    public void setGainLevel(double newGainLevel) {
        gainLevel = new Double(newGainLevel);
        doGain();
    }

    public void setGainLevel(Double newGainLevel) {
        gainLevel = newGainLevel;
    }

    public double getGainLevel() {
        if (gainLevel != null) {
            return gainLevel.doubleValue();
        } else {
            return 0;
        }
    }

    public double getGain() {
        return gain;
    }

    /**
	 * Change the weight according to the rule specified in the genotype
	 */
    private void updateWeight() {
        double w = changedWeight.doubleValue();
        double z0 = ((w / (weight_MAX - weight_MIN)) + 1);
        if (w > weight_MAX || w < weight_MIN) {
            if (w > weight_MAX) {
                w = weight_MAX;
            } else {
                w = weight_MIN;
            }
        }
        double ave = (weight_MAX + weight_MIN) / 2;
        double rng = (weight_MAX - weight_MIN) / 2;
        double delta = -(Math.abs(w - ave) / rng) + 1;
        double p;
        ControllerComponent component = output.getControllerComponent();
        if (component instanceof ContinuousNeuron) {
            p = get_p(((ContinuousNeuron) component).getCellPotential() + ((ContinuousNeuron) component).getBias());
        } else {
            p = get_p(output.getActivation());
        }
        switch(getRule()) {
            case 0:
                w += delta * getN() * p * input.getActivation() * output.getActivation();
                break;
            case 1:
                w += delta * getN() * p * (input.getActivation() - z0) * output.getActivation();
                break;
            case 2:
                w += delta * getN() * p * input.getActivation() * (output.getActivation() - z0);
                break;
            case 3:
                break;
        }
        changedWeight = new Double(w);
    }

    double get_p(double x) {
        if (x < -4) {
            return -1;
        } else if (x < -2) {
            return (x / 2) + 1;
        } else if (x < 2) {
            return 0;
        } else if (x < 4) {
            return (x / 2) - 1;
        } else {
            return 1;
        }
    }

    public int getRule() {
        if (rule != null) {
            return rule.intValue();
        } else {
            return 3;
        }
    }

    public void setRule(int newRule) {
        rule = new Integer(newRule);
    }

    public void setRule(Integer newRule) {
        rule = newRule;
    }

    public double getN() {
        if (n != null) {
            return n.doubleValue();
        } else {
            return 0;
        }
    }

    public void setN(double newN) {
        n = new Double(newN);
    }

    public void setN(Double newN) {
        n = newN;
    }

    public int getHasGain() {
        if (hasGain != null) {
            return hasGain.intValue();
        } else {
            return 0;
        }
    }

    public void setHasGain(int newHasGain) {
        hasGain = new Integer(newHasGain);
    }

    public void setHasGain(Integer newHasGain) {
        hasGain = newHasGain;
    }

    public void setWeight(double newWeight) {
        super.setWeight(newWeight);
        changedWeight = null;
    }

    public void setWeight(Double newWeight) {
        super.setWeight(newWeight);
        changedWeight = null;
    }

    public double getChangedWeight() {
        if (changedWeight != null) {
            return changedWeight.doubleValue();
        } else {
            return getWeight();
        }
    }

    public void setColorFromActivation() {
        setCurrentColor(Util.getColorFromValue(getChangedWeight(), weight_MIN, weight_MAX));
    }

    private Double changedWeight;

    public Integer rule;

    public static final double rule_MIN = 0;

    public static final double rule_MAX = 4;

    public int rule_FIXED = RANDOM_AND_EVOLVABLE;

    public Double n;

    public static final double n_MIN = -0.9;

    public static final double n_MAX = 0.9;

    public int n_FIXED = SET_AND_EVOLVABLE;

    public Double gainLevel;

    public static final double gainLevel_MIN = 0;

    public static final double gainLevel_MAX = 1;

    public int gainLevel_FIXED = SET_AND_EVOLVABLE;

    public Integer hasGain;

    public static final double hasGain_MIN = 0;

    public static final double hasGain_MAX = 3;

    public int hasGain_FIXED = SET_AND_FIXED;

    static final double mapFiringRateFromMin = 0;

    static final double mapFiringRateFromMax = 1;

    static final double mapFiringRateToMin = -1;

    static final double mapFiringRateToMax = 1;

    private double gain;
}
