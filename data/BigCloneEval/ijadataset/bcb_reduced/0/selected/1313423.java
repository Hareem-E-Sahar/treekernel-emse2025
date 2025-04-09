package iwork.smartpresenter;

import java.lang.reflect.*;
import org.w3c.dom.*;
import iwork.eheap2.*;

/**
 *
 * $Id: ScriptItem.java,v 1.1 2002/04/09 07:45:59 emrek Exp $
 *
 * $Log: ScriptItem.java,v $
 * Revision 1.1  2002/04/09 07:45:59  emrek
 * release 1.0, initial release to sourceforge
 *
 * Revision 1.2  2001/12/09 01:05:50  penka
 * *** empty log message ***
 *
 * Revision 1.1  2001/12/08 10:24:57  penka
 * *** empty log message ***
 *
 * Revision 1.2  2001/03/09 04:01:23  emrek
 * first checkin of scriptengine, related changes to other files (completed
 * parsing and loading of xml script files).
 *
 * Revision 1.1  2001/03/07 03:03:18  emrek
 * initial checkin
 *
 *
 */
public abstract class ScriptItem {

    EventHeap eheap;

    protected static final String XML_SCRIPTITEM = "scriptitem";

    public void setEventHeap(EventHeap eheap) {
        this.eheap = eheap;
    }

    public abstract void doItem() throws ScriptException;

    public abstract String toXML() throws ScriptException;

    public static ScriptItem ParseXML(Element xml) throws ScriptException {
        try {
            String scriptobj = xml.getAttribute(SmartPresenterConstants.ATTR_SCRIPTCLASS);
            Class c = Class.forName(scriptobj);
            Class[] paramTypes = { Class.forName("org.w3c.dom.Element") };
            Constructor constructor = c.getConstructor(paramTypes);
            Object[] params = { xml };
            ScriptItem ret = (ScriptItem) constructor.newInstance(params);
            return ret;
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }
}
