import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.jgraph.*;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

/**
 * Encapsulador del objeto de grafo y su grafica, 
 * 
 * @author Equipo 6 Cynthia Trevi�o, Ricardo Magallanes, Daniel Ramirez
 *
 */
public class Grafo {

    private ListenableGraph<Persona, DefaultEdge> g;

    public JGraphModelAdapter<Persona, DefaultEdge> adapter = null;

    private JGraph display;

    public Grafo(Dimension graphSize) {
        this.g = new ListenableUndirectedWeightedGraph<Persona, DefaultEdge>(DefaultEdge.class);
        this.adapter = new JGraphModelAdapter<Persona, DefaultEdge>(this.g);
        this.display = new JGraph(this.adapter);
        this.display.setSize(graphSize);
    }

    public JGraph getDisplay() {
        return this.display;
    }

    public ListenableGraph<Persona, DefaultEdge> getGraph() {
        return this.g;
    }

    public void positionVertexAt(Persona vertex, int x, int y) {
        DefaultGraphCell cell = this.adapter.getVertexCell(vertex);
        Map attr = cell.getAttributes();
        Rectangle2D b = GraphConstants.getBounds(attr);
        GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int) b.getHeight()));
        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        this.adapter.edit(cellAttr, null, null, null);
    }

    public static void guardaInformacion(String file) throws IOException {
        PrintWriter salida = new PrintWriter(new FileWriter(file));
        for (int i = 0; i < Persona.personas.size(); i++) {
            salida.print(Persona.personas.get(i).getNombre() + "/");
            salida.flush();
        }
        salida.println();
        for (int i = 0; i < Persona.personas.size(); i++) {
            if (Persona.personas.get(i).wlToString().equals("")) {
                salida.print("@/");
            } else {
                salida.print(Persona.personas.get(i).wlToString() + "/");
            }
            salida.flush();
        }
        salida.println();
        for (int i = 0; i < Persona.personas.size(); i++) {
            if (Persona.personas.get(i).blToString().equals("")) {
                salida.print("@/");
            } else {
                salida.print(Persona.personas.get(i).blToString() + "/");
            }
            salida.flush();
        }
        salida.println();
        for (int i = 0; i < Persona.personas.size(); i++) {
            salida.print(Persona.personas.get(i).atrToString() + "/");
            salida.flush();
        }
        salida.close();
    }

    public static Persona encuentraElemento(String nombre) {
        for (int i = 0; i < Persona.personas.size(); i++) {
            if (Persona.personas.get(i).getNombre().equals(nombre)) {
                return Persona.personas.get(i);
            }
        }
        return null;
    }

    public static void cargaGrafo(String file) {
        try {
            BufferedReader ent = new BufferedReader(new FileReader(file));
            String s = ent.readLine();
            StringTokenizer st = new StringTokenizer(s, "/");
            String nodos[] = new String[st.countTokens()];
            int k = 0;
            while (st.hasMoreElements()) {
                String sub = (st.nextToken());
                new Persona(sub);
                nodos[k] = sub;
                k++;
            }
            s = ent.readLine();
            StringTokenizer st2 = new StringTokenizer(s, "/");
            k = 0;
            while (st2.hasMoreTokens()) {
                String conexiones = st2.nextToken();
                StringTokenizer personas = new StringTokenizer(conexiones, ",");
                while (personas.hasMoreTokens()) {
                    String persona = personas.nextToken();
                    Persona p = Persona.getPersona(nodos[k]);
                    Persona p2 = Persona.getPersona(persona);
                    if (p != null && p2 != null) {
                        p.agregaAmigo(p2);
                    }
                }
                k++;
            }
            s = ent.readLine();
            StringTokenizer st3 = new StringTokenizer(s, "/");
            int i = 0;
            while (st3.hasMoreElements()) {
                String c2 = st3.nextToken();
                if (!(c2.equals("@"))) {
                    Persona x = encuentraElemento(c2);
                    Persona.personas.get(i).agregaEnemigo(x);
                }
                i++;
            }
            s = ent.readLine();
            int cont = 0;
            StringTokenizer st4 = new StringTokenizer(s, "/");
            while (st4.hasMoreElements()) {
                StringTokenizer sub4 = new StringTokenizer(st4.nextToken(), ",");
                String a = "";
                while (sub4.hasMoreElements()) {
                    String z = sub4.nextToken();
                    if (!(z.equals("0.0"))) {
                        a += z + "|";
                    }
                }
                StringTokenizer ultimo = new StringTokenizer(a, "|");
                for (int ult = 1; ult <= 3; ult++) {
                    Persona.personas.get(cont).setAttribute(ultimo.nextToken(), Float.parseFloat(ultimo.nextToken()));
                }
                cont++;
            }
            ent.close();
        } catch (Exception e) {
        }
    }
}
