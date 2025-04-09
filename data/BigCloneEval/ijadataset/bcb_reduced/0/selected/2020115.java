package serviceImplementations.evaluation;

import java.util.Random;
import coordinator.participant.ParticipantException;

/**
 * The shared participant service, which is is standard service.
 * 
 * @author Michael Sch�fer
 *
 */
public class SharedParticipantService extends EvaluationParticipantService {

    private static final boolean SYNCHRONOUS_REGISTRATION = false;

    private static final String ADDRESS_CONCRETESERVICE2 = "http://localhost:8080/axis/services/SharedParticipantService";

    private static final String IDENTIFIER = "http://sourceforge.net/projects/frogs/SharedParticipantService";

    private static final int PROCESSINGTIME_MAX = 20;

    private static final int PROCESSINGTIME_MIN = 3;

    /**
	 * Constructs a new concrete service object.
	 *
	 */
    public SharedParticipantService() {
        super("SharedParticipantService", ADDRESS_CONCRETESERVICE2, IDENTIFIER, SYNCHRONOUS_REGISTRATION);
        this.setParticipantManager(ParticipantManagerConcreteService1.getInstance());
    }

    /**
	 * Processes a service request. The number of seconds which the
	 * service requires to do so is selected by random. If a failure occurs
	 * then the transaction coordinator will be informed. 
	 *
	 */
    public void doSomethingElse() {
        if (!this.isRegisteredAtCoordinator()) {
            try {
                this.registerAtCoordinator();
            } catch (ParticipantException e) {
                System.out.println("SharedParticipantService exception: " + e.getLocalizedMessage());
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
