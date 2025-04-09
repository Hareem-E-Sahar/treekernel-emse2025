package mercurievv.fractalsync.controller.fractalizator.example;

import mercurievv.fractalsync.controller.fractalizator.AbstractFractalEngine;
import mercurievv.fractalsync.model.Harmonica;

/**
 * User: MercurieVV
 * Date: 2009.2.3
 * Time: 23:19:56
 */
public class ExampleFractalEngine extends AbstractFractalEngine {

    protected Harmonica generateNewHarmonica(Harmonica previousHarmonica, Harmonica harmonica) {
        Harmonica newHarmonica = new Harmonica(precalculatedSignal);
        double prevHarmonicaFrequency = previousHarmonica.getFrequencyRate();
        double harmonicaFrequency = harmonica.getFrequencyRate();
        double newHarmonicaFrequency = prevHarmonicaFrequency + (harmonicaFrequency - prevHarmonicaFrequency) / 2;
        newHarmonica.setFrequencyRate(newHarmonicaFrequency);
        double prevHarmonicaVolume = previousHarmonica.getAmplitude();
        double harmonicaVolume = harmonica.getAmplitude();
        double minVolume = Math.min(harmonicaVolume, prevHarmonicaVolume);
        double newHarmonicaVolume = minVolume / 2;
        newHarmonica.setAmplitude(newHarmonicaVolume);
        return newHarmonica;
    }
}
