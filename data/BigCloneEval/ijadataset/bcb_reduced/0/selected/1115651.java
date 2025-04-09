package planificador;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import ar.fi.uba.celdas.interfaces.bc.IBaseConocimientos;
import ar.fi.uba.celdas.interfaces.bc.Regla;
import ar.fi.uba.celdas.interfaces.bc.Sensor;
import ar.fi.uba.celdas.interfaces.planificacion.IPlanificacion;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class Planificador implements IPlanificacion {

    public List<Regla> planificar(IBaseConocimientos bc) {
        List<Regla> reglas = bc.getReglas();
        Collections.sort(reglas);
        Collection<Sensor> estado = bc.getEstadoActual();
        DefaultMutableTreeNode nodo = null;
        Iterator i = reglas.iterator();
        while (i.hasNext() && nodo == null) {
            Regla regla = (Regla) i.next();
            DefaultMutableTreeNode root = armarArbol(regla.getPredicciones(), reglas);
            nodo = buscarNodo(root, estado);
        }
        if (nodo == null) {
            List<Regla> plan = new ArrayList<Regla>();
            int count = reglas.size();
            Random r = new Random();
            int number = r.nextInt(count + 1);
            Regla regla = reglas.get(number);
            plan.add(regla);
            return plan;
        } else {
            return armarCamino(nodo, reglas);
        }
    }

    /**
	 *
	 */
    DefaultMutableTreeNode armarArbol(Collection<Sensor> objetivo, List<Regla> reglas) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(objetivo);
        Iterator i = reglas.iterator();
        while (i.hasNext()) {
            Regla regla = (Regla) i.next();
            if (regla.getPredicciones().equals(objetivo) && buscarNodo(root, regla.getCondiciones()) == null) {
                root.add(new DefaultMutableTreeNode(regla.getCondiciones()));
            }
        }
        DefaultMutableTreeNode nodo = root;
        while (nodo.getNextNode() != null) {
            nodo = nodo.getNextNode();
            agregarHijos(nodo, reglas);
        }
        return root;
    }

    /**
	 *
	 */
    void agregarHijos(DefaultMutableTreeNode nodo, List<Regla> reglas) {
        Iterator i = reglas.iterator();
        while (i.hasNext()) {
            Regla regla = (Regla) i.next();
            if (regla.getPredicciones().equals(nodo.getUserObject()) && !tieneSituacion(nodo, regla.getCondiciones())) {
                nodo.add(new DefaultMutableTreeNode(regla.getCondiciones()));
            }
        }
    }

    /**
	 * se fija si existe en el camino a la raiz un nodo con iguales datos
	 */
    boolean tieneSituacion(DefaultMutableTreeNode hoja, Collection<Sensor> situacion) {
        boolean existe = false;
        DefaultMutableTreeNode nodo = hoja;
        while (nodo.getParent() != null && !existe) {
            nodo = (DefaultMutableTreeNode) nodo.getParent();
            if (nodo.getUserObject().equals(situacion)) existe = true;
        }
        return existe;
    }

    /**
	 * recorre el arbol en busca del nodo que contenga el objeto especificado
	 */
    DefaultMutableTreeNode buscarNodo(DefaultMutableTreeNode root, Object object) {
        DefaultMutableTreeNode node = null;
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject().equals(object)) {
                return node;
            }
        }
        return null;
    }

    /**
	 * busca regla con determinada prediccion y condicion
	 */
    Regla getRegla(Object nodo1, Object nodo2, List<Regla> reglas) {
        Iterator i = reglas.iterator();
        while (i.hasNext()) {
            Regla regla = (Regla) i.next();
            if (regla.getPredicciones().equals(nodo1) && regla.getCondiciones().equals(nodo2)) {
                return regla;
            }
        }
        return null;
    }

    /**
	 * arma el plan de reglas
	 */
    List<Regla> armarCamino(DefaultMutableTreeNode nodo, List<Regla> reglas) {
        List<Regla> plan = new ArrayList<Regla>();
        TreeNode[] path = nodo.getPath();
        for (int i = path.length - 2; i >= 0; i--) {
            DefaultMutableTreeNode pathi = (DefaultMutableTreeNode) path[i];
            DefaultMutableTreeNode pathimas = (DefaultMutableTreeNode) path[i + 1];
            Regla regla = getRegla(pathi.getUserObject(), pathimas.getUserObject(), reglas);
            if (regla != null) plan.add(regla);
        }
        return plan;
    }
}
