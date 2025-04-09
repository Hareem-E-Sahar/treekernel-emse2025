package DAOs;

import beans.SecondaryStudies;
import beans.SecondaryStudiesPK;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import DAOs.SecondaryStudiesJpaController;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author Ioana C
 */
public class SecondaryStudiesConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent component, String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        SecondaryStudiesPK id = getId(string);
        SecondaryStudiesJpaController controller = (SecondaryStudiesJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "secondaryStudiesJpa");
        return controller.findSecondaryStudies(id);
    }

    SecondaryStudiesPK getId(String string) {
        SecondaryStudiesPK id = new SecondaryStudiesPK();
        String[] params = new String[2];
        int p = 0;
        int grabStart = 0;
        String delim = "#";
        String escape = "~";
        Pattern pattern = Pattern.compile(escape + "*" + delim);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String found = matcher.group();
            if (found.length() % 2 == 1) {
                params[p] = string.substring(grabStart, matcher.start());
                p++;
                grabStart = matcher.end();
            }
        }
        if (p != params.length - 1) {
            throw new IllegalArgumentException("string " + string + " is not in expected format. expected 2 ids delimited by " + delim);
        }
        params[p] = string.substring(grabStart);
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].replace(escape + delim, delim);
            params[i] = params[i].replace(escape + escape, escape);
        }
        id.setPersonID(Integer.parseInt(params[0]));
        id.setStudyID(Short.parseShort(params[1]));
        return id;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof SecondaryStudies) {
            SecondaryStudies o = (SecondaryStudies) object;
            SecondaryStudiesPK id = o.getSecondaryStudiesPK();
            if (id == null) {
                return "";
            }
            String delim = "#";
            String escape = "~";
            String personID = String.valueOf(id.getPersonID());
            personID = personID.replace(escape, escape + escape);
            personID = personID.replace(delim, escape + delim);
            Object studyIDObj = id.getStudyID();
            String studyID = studyIDObj == null ? "" : String.valueOf(studyIDObj);
            studyID = studyID.replace(escape, escape + escape);
            studyID = studyID.replace(delim, escape + delim);
            return personID + delim + studyID;
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: beans.SecondaryStudies");
        }
    }
}
