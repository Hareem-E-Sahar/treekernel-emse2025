package serviceImplementations.evaluation;

import java.util.Random;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import coordinator.HeaderProcessing;
import coordinator.participant.BasicParticipantService;
import coordinator.participant.ParticipantException;
import coordinator.wsaddressing.AttributedURI;

/**
 * The first concrete service, which has a probability of
 * encountering a failure.
 * 
 * @author Michael Sch�fer
 *
 */
public class ConcreteService1 extends BasicParticipantService {

    private static final boolean SYNCHRONOUS_REGISTRATION = false;

    private static final String ADDRESS_CONCRETESERVICE1 = "http://localhost:8080/axis/services/ConcreteService1";

    private static final String IDENTIFIER = "http://sourceforge.net/projects/frogs/ConcreteService1";

    private static final int FAILURE_PROBABILITY = 10;

    private static final int PROCESSINGTIME_MAX = 20;

    private static final int PROCESSINGTIME_MIN = 3;

    /**
	 * Constructs a new concrete service object.
	 *
	 */
    public ConcreteService1() {
        super("ConcreteService1", ADDRESS_CONCRETESERVICE1, IDENTIFIER, SYNCHRONOUS_REGISTRATION);
    }

    /**
	 * Processes a service request. The number of seconds which the
	 * service requires to do so is selected by random. If a failure occurs
	 * then the transaction coordinator will be informed. 
	 *
	 */
    public void doSomething() {
        String clientIdentifier = this.getClientIdentifier();
        if (!this.isRegisteredAtCoordinator()) {
            try {
                this.registerAtCoordinator();
            } catch (ParticipantException e) {
                System.out.println("ConcreteService1 exception: " + e.getLocalizedMessage());
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
        r = randomGenerator.nextInt(101);
        if (r <= FAILURE_PROBABILITY) {
            System.out.println("Concrete Service 1 - Request failed: " + clientIdentifier);
            try {
                MessageContext messageContext = MessageContext.getCurrentContext();
                Message request = messageContext.getRequestMessage();
                AttributedURI messageID = HeaderProcessing.getMessageID(request);
                this.sendFault("Internal service error. The request could not be processed correctly.", messageID);
            } catch (Exception e) {
                System.out.println("ConcreteService1 exception: " + e.getLocalizedMessage());
            }
            StatisticsManager.getInstance().addFinishedService(false);
        } else {
            StatisticsManager.getInstance().addFinishedService(true);
        }
    }
}
