package org.jato.tags;

import java.lang.reflect.*;
import java.util.*;
import org.jato.*;
import org.jdom.*;

/**
 * An object finder that locates objects by creating new ones. This tag uses the
 * parameter tags contained in this tag to determine the constructor form to invoke.
 * <p>
 * Default tag name: constructor <b>
 * Attributes: None
 *
 * @author Andy Krumel
 */
public class CtorObjFinderScriptTag extends DefaultObjFinderScriptTag {

    public CtorObjFinderScriptTag() {
        super("constructor");
        this.setIterateMode(ITERATE_NEVER);
    }

    /**
    * Gets the object by creating a new one using a constructor.
    *
    * @param type The type of object to instantiate.
    * @param jato The interpreter running the script.
    * @param thisClass The current object's class.
    * @param thisObj The current object.
    * @param xmlIn The current input XML element being processed by the script.
    * @param xmlOut The current output XML element being acted upon by the script.
    *
    * @throws JatoException Thrown in response to any exception generated while
    *    attempting to obtain the constructor, get the parameters, or invoke the
    *    the constructor.
    */
    public Object getObject(String type, Interpreter jato, Class thisClass, Object thisObj, Element xmlIn, Element xmlOut) throws JatoException {
        Constructor ctor = null;
        Class types[] = null;
        try {
            ScriptTag params[] = getChildren(ParamScriptTag.sTypes, jato, thisClass, thisObj, xmlIn, xmlOut);
            List parmList = new ArrayList();
            List typeList = new ArrayList();
            ParamScriptTag parm;
            for (int i = 0, len = params.length; i < len; i++) {
                if (params[i] instanceof ParamScriptTag) {
                    parm = (ParamScriptTag) params[i];
                    parm.getParameters(typeList, parmList, jato, thisClass, thisObj, xmlIn, xmlOut);
                } else {
                    params[i].process(jato, thisClass, thisObj, xmlIn, xmlOut);
                }
            }
            Class ctorClass = jato.loadClass(type);
            types = (Class[]) typeList.toArray(ParamScriptTag.sClassArray);
            Object parms[] = parmList.toArray(ParamScriptTag.sObjectArray);
            ctor = ctorClass.getConstructor(types);
            return ctor.newInstance(parms);
        } catch (Exception ex) {
            StringBuffer msg = new StringBuffer("Unable to create object of type '");
            msg.append(type).append('\'');
            if (ctor != null) {
                msg.append(" using constructor ").append(ctor.toString());
            } else {
                if ((types != null) && (types.length != 0)) {
                    msg.append(" - no constructor taking argument types: ");
                    for (int i = 0; i < types.length; i++) {
                        if (i != 0) {
                            msg.append(", ");
                        }
                        msg.append(String.valueOf(types[i]));
                    }
                }
            }
            throw new JatoException(msg.toString(), ex);
        }
    }

    /**
    * The bootstrap template definition.
    */
    public static ScriptTag getTemplate() throws JatoException {
        ObjectScriptTag init = new ObjectScriptTag(CtorObjFinderScriptTag.class);
        init.setPublishable(true);
        init.setKey("child-tag");
        CtorObjFinderScriptTag tag = new CtorObjFinderScriptTag();
        tag.setTemplateInfo("true", init);
        return tag;
    }
}
