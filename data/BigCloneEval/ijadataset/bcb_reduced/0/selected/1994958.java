package abstrasy.pcfx;

import abstrasy.Interpreter;
import abstrasy.interpreter.InterpreterException;
import abstrasy.Node;
import abstrasy.PCoder;
import abstrasy.interpreter.StdErrors;
import java.util.regex.*;

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
public class PCFx_list_regex extends PCFx {

    public PCFx_list_regex() {
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
        startAt.isGoodArgsCnt(4);
        String xnode = startAt.getSubNode(3, Node.VTYPE_STRINGS).getString();
        Node ynode = Node.createEmptyList();
        String regex = startAt.getSubNode(1, Node.VTYPE_STRINGS).getString();
        startAt.requirePCode(2, PCoder.PC_IN);
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(xnode);
            while (matcher.find()) {
                ynode.addElement(new Node(xnode.substring(matcher.start(), matcher.end())));
            }
        } catch (Exception ex) {
            if (Interpreter.isDebugMode()) {
                ex.printStackTrace();
            }
            throw new InterpreterException(StdErrors.extend(StdErrors.Regex_error, regex));
        }
        return ynode;
    }
}
