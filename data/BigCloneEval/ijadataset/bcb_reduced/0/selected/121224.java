package net.sf.myfacessandbox.components.ajax.tests;

import java.net.URLEncoder;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import net.sf.myfacessandbox.components.ajax.autocomplete.AutocompleteDataBuilder;
import net.sf.myfacessandbox.components.ajax.backend.IAjaxEventHandler;

public class GetCitiesEvendHandler implements IAjaxEventHandler {

    protected Object getBean(FacesContext context, String name) {
        return context.getApplication().getVariableResolver().resolveVariable(context, name);
    }

    public String handleEvent(FacesContext context, String eventName, String actionType, String id) {
        if (!eventName.equals("getcities")) return "";
        ApplicationBean application = (ApplicationBean) getBean(context, "ApplicationBean");
        if (application == null) {
            return "";
        }
        String[] cities = application.getCities();
        String[] zips = application.getZips();
        int index;
        if (id.length() > 0) {
            index = findClosestIndex(cities, id, true);
        } else {
            index = 0;
        }
        if (index == -1) {
            index = 0;
        }
        AutocompleteDataBuilder dataBuilder = new AutocompleteDataBuilder();
        ;
        dataBuilder.appendItem(new SelectItem("mail1", URLEncoder.encode("werpu@gmx.at")));
        for (int i = 0; i < 10; i++) {
            if (index >= cities.length) {
                break;
            }
            dataBuilder.appendItem(new SelectItem(zips[index], cities[index]));
            index++;
        }
        return dataBuilder.toString();
    }

    /**
	 * Perform a case insensitive binary search for the key in the data string.
	 * Not an exact binary search since we return the index of the closest match
	 * rather than returning an actual match.
	 */
    private static int findClosestIndex(String[] data, String key, boolean ignoreCase) {
        int low = 0;
        int high = data.length - 1;
        int middle = -1;
        while (high > low) {
            middle = (low + high) / 2;
            int result;
            if (ignoreCase) {
                result = key.compareToIgnoreCase(data[middle]);
            } else {
                result = key.compareTo(data[middle]);
            }
            if (result == 0) {
                return middle;
            } else if (result < 0) {
                high = middle;
            } else if (low != middle) {
                low = middle;
            } else {
                break;
            }
        }
        return middle;
    }
}
