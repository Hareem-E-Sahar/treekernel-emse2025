package DAOs;

import beans.EmpTraining;
import beans.EmpTrainingPK;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import DAOs.EmpTrainingJpaController;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author Ioana C
 */
public class EmpTrainingConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent component, String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        EmpTrainingPK id = getId(string);
        EmpTrainingJpaController controller = (EmpTrainingJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "empTrainingJpa");
        return controller.findEmpTraining(id);
    }

    EmpTrainingPK getId(String string) {
        EmpTrainingPK id = new EmpTrainingPK();
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
        id.setTrainingID(Short.parseShort(params[0]));
        id.setPersonID(Integer.parseInt(params[1]));
        return id;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof EmpTraining) {
            EmpTraining o = (EmpTraining) object;
            EmpTrainingPK id = o.getEmpTrainingPK();
            if (id == null) {
                return "";
            }
            String delim = "#";
            String escape = "~";
            Object trainingIDObj = id.getTrainingID();
            String trainingID = trainingIDObj == null ? "" : String.valueOf(trainingIDObj);
            trainingID = trainingID.replace(escape, escape + escape);
            trainingID = trainingID.replace(delim, escape + delim);
            String personID = String.valueOf(id.getPersonID());
            personID = personID.replace(escape, escape + escape);
            personID = personID.replace(delim, escape + delim);
            return trainingID + delim + personID;
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: beans.EmpTraining");
        }
    }
}
