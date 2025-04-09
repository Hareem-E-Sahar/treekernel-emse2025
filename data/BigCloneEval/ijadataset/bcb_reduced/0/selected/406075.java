package org.tex4java.tex.environment;

import java.lang.reflect.*;
import java.util.*;
import org.tex4java.Manager;
import org.tex4java.presenter.*;
import org.tex4java.tex.boxworld.*;
import org.tex4java.tex.parser.*;
import org.tex4java.tex.scanner.tokens.*;

/**
 * The enviroment: Keeps a stack of scopes, the assignment between tokens and
 * categoriecodes...
 * 
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * @author <a href="mailto:paladin@cs.tu-berlin.de">Peter Annuss </a>
 * @author <a href="mailto:thomas@dohmke.de">Thomas Dohmke </a>
 * @version $Revision: 1.1.1.1 $
 * @version Previous Revision: 1.53
 */
public class Environment {

    protected Manager manager;

    /**
   * The top scope.
   */
    public Scope topScope;

    /**
   * The current scope.
   */
    private Scope scope;

    /**
   * Assignment between the category codes and the tokens.
   */
    public Token[] cattokens;

    /**
   * A stack for the history of the parser mode.
   */
    public Stack mode;

    /**
   * The BoxWorld
   */
    public Box box;

    public Presenter presenter;

    public double raiseNextBox = 0;

    public double moveNextBox = 0;

    public HashMap token2parser;

    public String fontDir;

    public String libDir;

    /**
   * Static constructor to initiate all variables.
   */
    public Environment(Manager manager) {
        this.manager = manager;
    }

    public void init(String confDir) {
        mode = new Stack();
        mode.push(new VerticalMode());
        box = new Box();
        VBox vbox = new VBox(manager);
        box.appendChild(vbox);
        box = vbox;
        cattokens = new Token[16];
        cattokens[0] = new MacroToken();
        cattokens[1] = new BeginGroupToken();
        cattokens[2] = new EndGroupToken();
        cattokens[3] = new MathShiftToken();
        cattokens[4] = new AlignmentToken();
        cattokens[5] = new EndOfLineToken();
        cattokens[6] = new ParameterToken();
        cattokens[7] = new SuperscriptToken();
        cattokens[8] = new SubscriptToken();
        cattokens[9] = null;
        cattokens[10] = new SpaceToken();
        cattokens[11] = new LetterToken();
        cattokens[12] = new OtherToken();
        cattokens[13] = new ActiveToken();
        cattokens[14] = null;
        cattokens[15] = null;
        token2parser = new HashMap();
        scope = new Scope(manager, null);
        LoadClasses loadClasses = new LoadClasses(manager, scope, token2parser, confDir, "classes.xml");
        topScope = scope;
    }

    /**
   * Returns the current scope.
   * 
   * @return a <code>Scope</code> value
   */
    public Scope getScope() {
        return scope;
    }

    /**
   * Returns the top of scopes.
   * 
   * @return a <code>Scope</code> value
   */
    public Scope getTopScope() {
        return topScope;
    }

    /**
   * Returns the parser associated with the token. MacroToken aren't handled.
   * 
   * @param token A token from scanner, which isn't parsed yet.
   * @return a <code>Parser</code> value
   */
    public Parser getParser(Token token) {
        String tokenName = token.getClass().getName();
        Object parserClassName = token2parser.get(tokenName);
        if (parserClassName != null) {
            try {
                Class parserClass = Class.forName((String) parserClassName);
                Class parameterType[] = new Class[] { this.manager.getClass() };
                Object args[] = new Object[] { this.manager };
                Constructor constructor = parserClass.getConstructor(parameterType);
                return (Parser) constructor.newInstance(args);
            } catch (Exception e) {
                System.err.println("Error while creating Parser for token " + tokenName);
            }
        } else {
            System.err.println("No Parser assigned for token " + tokenName);
        }
        return null;
    }

    /**
   * Push a new scope.
   */
    public void pushScope() {
        scope = new Scope(manager, scope);
    }

    /**
   * Pop the last scope.
   */
    public void popScope() throws Exception {
        if (scope.parent == null) {
            throw new Exception("Tried to pop topScope.");
        } else {
            scope = scope.parent;
        }
    }

    /**
   * Push a new parser mode.
   */
    public void pushMode(Mode mode) {
        mode.parent = peekMode();
        this.mode.push(mode);
    }

    /**
   * Returns the current parser mode.
   * 
   * @return The current parser mode.
   */
    public Mode peekMode() {
        return (Mode) mode.peek();
    }

    /**
   * Pop the last parser mode.
   */
    public Mode popMode() {
        return (Mode) mode.pop();
    }

    public Object getRegisterValue(String controlSequence, TokenList tokens) throws Exception {
        return scope.getRegisterValue(controlSequence, tokens);
    }

    public Object getRegisterValue(String controlSequence) throws Exception {
        return scope.getRegisterValue(controlSequence, null);
    }

    public void setRegisterValue(String controlSequence, Object o) {
        scope.setRegisterValue(controlSequence, o);
    }

    /**
   * Print the current boxworld to string. For debugging only.
   */
    public String boxToString(Box box, String deep) {
        String string = deep + box.toString() + "\n";
        if (box.hasChilds()) {
            string += boxToString(box.firstChild, deep + "  ");
        }
        if (box.nextSibling != null) {
            string += boxToString(box.nextSibling, deep);
        }
        return string;
    }

    /**
   * Print the current boxworld to string. For debugging only.
   */
    public String boxToString(Box box) {
        return boxToString(box, "");
    }
}
