package abstrasy.pcfx;

import abstrasy.Heap;
import abstrasy.Node;
import abstrasy.PCoder;
import abstrasy.interpreter.InterpreterException;
import abstrasy.interpreter.StdErrors;

/**
 * Abstrasy Interpreter
 *
 * Copyright : Copyright (c) 2006-2012, Luc Bruninx.
 *
 * Concédée sous licence EUPL, version 1.1 uniquement (la «Licence»).
 *
 * Vous ne pouvez utiliser la présente oeuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 *   http://www.osor.eu/eupl
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous
 * la Licence est distribué "en l’état", SANS GARANTIES OU CONDITIONS QUELLES
 * QU’ELLES SOIENT, expresses ou implicites.
 *
 * Consultez la Licence pour les autorisations et les restrictions
 * linguistiques spécifiques relevant de la Licence.
 *
 *
 * @author Luc Bruninx
 * @version 1.0
 */
public class PCFx_sort_static extends PCFx {

    /**
     * Réalise le tri des éléments d'une liste.
     * Cet opérateur respecte la protection des listes finales.
     * 
     * -> seules les listes non finales peuvent être triées.
     * 
     */
    public PCFx_sort_static() {
    }

    private static final boolean needPermute__(Node expr, Node n_left, Node n_right) throws Exception {
        Node argv = Node.createEmptyList();
        argv.addElement(n_left);
        argv.addElement(n_right);
        Heap.push();
        Heap.setv(PCoder.ARGV, argv);
        Node rnode = expr.exec(true);
        Heap.pull();
        return Node.isTrueEquivalent(rnode);
    }

    /**
     * Tri Fusion:
     * ----------
     *    Avantage : Algo qui effectue moins de comparaisons que le tri rapide. Or, c'est au
     *    --------   niveau des comparaisons que (sort! liste cond {...}) est le plus lent.
     *

     */
    private static final void fusion(Node[] array, int deb1, int fin1, int fin2, Node compare) throws Exception {
        int deb2 = fin1 + 1;
        Node table1[] = new Node[fin1 - deb1 + 1];
        System.arraycopy(array, deb1, table1, 0, table1.length);
        int compt1 = deb1;
        int compt2 = deb2;
        for (int i = deb1; i <= fin2; i++) {
            if (compt1 == deb2) {
                break;
            } else if (compt2 == (fin2 + 1)) {
                array[i] = table1[compt1 - deb1];
                compt1++;
            } else if (needPermute__(compare, table1[compt1 - deb1], array[compt2])) {
                array[i] = table1[compt1 - deb1];
                compt1++;
            } else {
                array[i] = array[compt2];
                compt2++;
            }
        }
    }

    private static final void triFusion__(Node[] array, int deb, int fin, Node compare) throws Exception {
        if (deb != fin) {
            int milieu = (fin + deb) / 2;
            triFusion__(array, deb, milieu, compare);
            triFusion__(array, milieu + 1, fin, compare);
            fusion(array, deb, milieu, fin, compare);
        }
    }

    private static final void triFusion_(Node[] array, Node compare) throws Exception {
        int longueur = array.length;
        if (longueur > 0) {
            triFusion__(array, 0, longueur - 1, compare);
        }
    }

    /**
     * eval
     *
     * @param startAt Node
     * @return Node
     * @throws Exception
     * @todo Implémenter cette méthode abstrasy.PCFx
     */
    public Node eval(Node startAt) throws Exception {
        boolean test = false;
        int i = 1;
        startAt.requireNodeType(i, Node.TYPE_SYMBOL);
        Node lNode = startAt.getSubNode(i++, Node.TYPE_LIST);
        if (lNode.isFinalNode()) {
            throw new InterpreterException(StdErrors.Illegal_access_to_final_value);
        }
        startAt.requirePCode(i++, PCoder.PC_COND);
        Node enode = startAt.getSubNode(i++, Node.TYPE_LAZY);
        startAt.isGoodArgsLength(true, 4);
        Node array[] = lNode.getArray();
        triFusion_(array, enode);
        lNode.setArray(array);
        return null;
    }
}
