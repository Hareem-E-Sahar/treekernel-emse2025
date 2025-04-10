package com.newisys.behsim;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;
import com.newisys.ova.OVAEngine;
import com.newisys.random.PRNGFactory;
import com.newisys.random.PRNGFactoryFactory;

/**
 * Provides the main() method used to launch behavioral simulations.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class BehavioralLauncher {

    /**
     * Launches a behavioral simulation.
     * <P>
     * usage: BehavioralLauncher &lt;appclass&gt; [plus args]
     *
     * @param args an array of arguments to pass to the behavioral simulation
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Syntax: BehavioralLauncher <appclass> [plus args]");
            System.exit(1);
        }
        SimulationThread behClkThread = null;
        DVApplication app = null;
        try {
            Class appCls = null;
            try {
                appCls = Class.forName(args[0]);
            } catch (UnsupportedClassVersionError unsupportedClassVersionError) {
                System.err.println("Could not load class" + args[0]);
                throw unsupportedClassVersionError;
            }
            assert (DVApplication.class.isAssignableFrom(appCls));
            final BehavioralSimulation sim = new BehavioralSimulation(Arrays.asList(args));
            final PRNGFactory rngFactory = PRNGFactoryFactory.getDefaultFactory();
            final SimulationManager simManager = new SimulationManager("BehavioralSimulation", rngFactory, rngFactory.newInstance(0));
            final OVAEngine ovaEngine = null;
            final DVSimulation dvSim = new DVSimulation(sim, simManager, ovaEngine);
            final String clockName = "DefaultClock";
            sim.createRegister(clockName, 1);
            behClkThread = dvSim.fork(clockName, new BehavioralClockGenerator(dvSim, clockName, 100));
            final Constructor<?> appCtor = appCls.getConstructor(DVSimulation.class);
            app = (DVApplication) appCtor.newInstance(dvSim);
            app.start();
            sim.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (behClkThread != null) {
                behClkThread.terminate();
            }
            if (app != null) {
                app.finish();
            }
        }
    }
}
