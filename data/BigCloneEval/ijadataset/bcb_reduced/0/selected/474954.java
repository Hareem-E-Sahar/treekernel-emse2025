package serviceImplementations.evaluation;

import java.util.Random;
import coordinator.participant.BasicParticipantService;
import coordinator.participant.ParticipantException;

/**
 * The second concrete service, which will never encounter
 * a failure.
 * 
 * @author Michael Sch�fer
 *
 */
public class ConcreteService2 extends BasicParticipantService {

    private static final boolean SYNCHRONOUS_REGISTRATION = true;

    private static final String ADDRESS_CONCRETESERVICE2 = "http://localhost:8080/axis/services/ConcreteService2";

    private static final String IDENTIFIER = "http://sourceforge.net/projects/frogs/ConcreteService2";

    private static final int PROCESSINGTIME_MAX = 20;

    private static final int PROCESSINGTIME_MIN = 3;

    /**
	 * Constructs a new concrete service object.
	 *
	 */
    public ConcreteService2() {
        super("ConcreteService2", ADDRESS_CONCRETESERVICE2, IDENTIFIER, SYNCHRONOUS_REGISTRATION);
    }

    /**
	 * Processes a service request. The number of seconds which the
	 * service requires to do so is selected by random. 
	 *
	 */
    public void doSomething() {
        if (!this.isRegisteredAtCoordinator()) {
            try {
                this.registerAtCoordinator();
            } catch (ParticipantException e) {
                System.out.println("ConcreteService2 exception: " + e.getLocalizedMessage());
            }
        }
        Random randomGenerator = new Random();
        int r = randomGenerator.nextInt(PROCESSINGTIME_MAX + 1);
        long sleepTimeMilli = 1000;
        if (r < PROCESSINGTIME_MIN) {
            sleepTimeMilli *= PROCESSINGTIME_MIN;
        } else {
            sleepTimeMilli *= r;
        }
        try {
            Thread.sleep(sleepTimeMilli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StatisticsManager.getInstance().addFinishedService(true);
    }
}
