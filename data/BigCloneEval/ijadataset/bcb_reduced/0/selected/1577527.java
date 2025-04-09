package net.sf.myfacessandbox.components.ajax.autocomplete;

import java.util.ArrayList;
import javax.faces.context.FacesContext;
import org.apache.myfaces.component.html.ext.HtmlInputText;

/**
 * The original file is from the java blueprints project, the license is
 * included to be compliant (see blueprintslicense.txt)
 */
public class AjaxTextField extends HtmlInputText {

    private String completionMethod;

    private String onchoose;

    private String ondisplay;

    private String pattern;

    private Boolean multiselect;

    public AjaxTextField() {
        super();
        setRendererType("ajaxautocomplete");
    }

    /**
	 * return the component family
	 */
    public String getFamily() {
        return "ajaxinput";
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        values[1] = completionMethod;
        values[2] = onchoose;
        values[3] = ondisplay;
        values[4] = pattern;
        values[5] = multiselect;
        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.completionMethod = (String) values[1];
        this.onchoose = (String) values[2];
        this.ondisplay = (String) values[3];
        this.pattern = (String) values[4];
        this.multiselect = (Boolean) values[5];
    }

    public void setOnchoose(String onchoose) {
        this.onchoose = onchoose;
    }

    public String getOnchoose() {
        return onchoose;
    }

    public void setOndisplay(String ondisplay) {
        this.ondisplay = ondisplay;
    }

    public String getOndisplay() {
        return ondisplay;
    }

    public void setCompletionMethod(String completionMethod) {
        this.completionMethod = completionMethod;
    }

    public String getCompletionMethod() {
        return completionMethod;
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

    /**
	 * Return a short completion list from the given data using the given prefix
	 * 
	 * @param sortedData
	 *            A sorted array of Strings we want to pick completion results
	 *            from
	 * @param prefix
	 *            A prefix of some of the strings in the sortedData that the
	 *            user has typed so far
	 * @param caseInsensitive
	 *            Whether to ignore case in comparisons. <b>Note that the
	 *            sortedData should be case-sorted if you're doing case
	 *            sensitive completion, and vice versa.</b>
	 * @return A set of matches for the given prefix, limited to at most
	 *         {@link getMaxCount} matches.
	 */
    public static String[] complete(String[] sortedData, String prefix, boolean caseInsensitive) {
        ArrayList matches = new ArrayList();
        int index;
        if (prefix.length() > 0) {
            index = findClosestIndex(sortedData, prefix, caseInsensitive);
        } else {
            index = 0;
        }
        if (index == -1) {
            index = 0;
        }
        for (int i = 0; i < RenderScriptPhaseListener.MAX_RESULTS_RETURNED; i++) {
            if (index >= sortedData.length) {
                break;
            }
            matches.add(sortedData[index]);
            index++;
        }
        return (String[]) matches.toArray(new String[matches.size()]);
    }

    /**
	 * Return the maximum number of results returned from this text field
	 * 
	 * @return A numberf indicating the maximum number of completion matches
	 *         that should be returned/displayed
	 */
    public int getMaxCount() {
        return RenderScriptPhaseListener.MAX_RESULTS_RETURNED;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getMultiselect() {
        return multiselect;
    }

    public void setMultiselect(Boolean multiselect) {
        this.multiselect = multiselect;
    }
}
