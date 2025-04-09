package practica6.impl.logger;

import java.util.Vector;
import practica6.entity.Event;
import practica6.entity.Packet;
import practica6.interfaces.Logger;

/**
 * Aquest Logger escoltara nom�s els events de perdua de paquets
 * en el sistema per treure'n estad�stiques.
 * @author Enric Rodriguez 37615
 * @author Ricard Granado 38791
 */
public class PaquetsDescartats implements Logger {

    /**
	 * Guardarem els paquets descartats en un vector per si m�s endavant
	 * volem treure altres tipus d'estad�stiques.
	 */
    private Vector<Event> descartats;

    private int[] num_descartats = new int[5];

    private int entrades = 0;

    private long t_proc_mig;

    /**
	 * Constructor de la classe.
	 */
    public PaquetsDescartats() {
        descartats = new Vector<Event>();
    }

    /**
	 * Metode que retorna el nombre de paquests perduts al sistema.
	 * @return paquests perduts
	 */
    public int getDescartats() {
        return descartats.size();
    }

    /**
	 * Metode que retorna el nombre de paquests perduts al sistema d'un tipus
	 * determinat de paquets.
	 * @param tipus El tipus de paquets.
	 * @return El n�mero de paquets perduts d'aquell tipus.
	 */
    public int getDescartats(int tipus) {
        return num_descartats[tipus];
    }

    /**
	 * Calcula la percentatge de paquets descartats respecte al total.
	 */
    public float getperdcent() {
        float perdcent = ((float) getDescartats()) * 100.0f / ((float) entrades);
        return perdcent;
    }

    /**
	 * M�tode necessari perque implementem Logger. Anira guardant
	 * nom�s els events de paquets rebuts.
	 */
    public void receiveEvent(Event e) {
        if (e.getTipus() == Event.PAQUET_ENTRA_SISTEMA) {
            entrades++;
        }
        if (e.getTipus() == Event.PAQUET_DESCARTAT) {
            descartats.add(e);
            num_descartats[((Packet) e.getSource()).getTipus()]++;
        }
        if (e.getTipus() == Event.TEMPS_PROCESSAMENT_ENTRADA) {
            long t_proc = e.getTime() - ((Event) e.getSource()).getTime();
            if (t_proc_mig == 0) t_proc_mig = t_proc;
            t_proc_mig = (t_proc_mig + t_proc) / 2;
        }
    }

    public long getTempsProcessamentMig() {
        return t_proc_mig;
    }
}
