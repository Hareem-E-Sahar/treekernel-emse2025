package net.sourceforge.kas.cTree.cSwap;

import java.util.ArrayList;
import java.util.HashMap;
import net.sourceforge.kas.cTree.CElement;
import net.sourceforge.kas.cTree.adapter.C_Changer;
import net.sourceforge.kas.cTree.adapter.C_Event;
import net.sourceforge.kas.cTree.adapter.DOMElementMap;
import net.sourceforge.kas.cTree.cDefence.CD_Event;

public class SwapHandler {

    private static volatile SwapHandler uniqueInstance;

    public HashMap<String, CS_Base> getSwappers;

    @SuppressWarnings("unchecked")
    private SwapHandler() {
        this.getSwappers = new HashMap<String, CS_Base>();
        final ArrayList<String> strings = new ArrayList<String>();
        strings.add("SwapPunkt");
        strings.add("SwapStrich");
        strings.add("SwapRoot");
        strings.add("SwapSquare");
        Class c;
        for (final String s : strings) {
            try {
                c = Class.forName("net.sourceforge.kas.cTree.cSwap." + s);
                final CS_Base a = (CS_Base) c.getConstructor().newInstance();
                a.register(this.getSwappers);
            } catch (final Exception e) {
                System.err.println("Error in SwapHandler");
            }
        }
    }

    public static SwapHandler getInstance() {
        if (SwapHandler.uniqueInstance == null) {
            synchronized (DOMElementMap.class) {
                if (SwapHandler.uniqueInstance == null) {
                    SwapHandler.uniqueInstance = new SwapHandler();
                }
            }
        }
        return SwapHandler.uniqueInstance;
    }

    public HashMap<String, C_Changer> getOptions(final ArrayList<CElement> els) {
        final HashMap<String, C_Changer> options = new HashMap<String, C_Changer>();
        final C_Event event = new C_Event(els);
        for (final CS_Base cs : this.getSwappers.values()) {
            final C_Changer c = cs.getChanger(event);
            if (c.canDo()) {
                options.put(cs.getText(), cs);
            }
        }
        return options;
    }

    public void swap(final ArrayList<CElement> els, final String actionCommand) {
        final CD_Event event = new CD_Event(false);
        if (this.getSwappers.containsKey(actionCommand)) {
            this.getSwappers.get(actionCommand).doSwap(event);
        }
    }
}
