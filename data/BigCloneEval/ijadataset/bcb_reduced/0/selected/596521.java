package mutt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Statistics {

    /**
	 * Method that finds possible function calls in a script string.
	 * 
	 * @param eval
	 *            script string
	 * @return Collection of function calls
	 * @author Margus Martsepp
	 */
    public static Collection<String> getFunctionCalls(String eval) throws NullPointerException {
        HashSet<String> uniqueResult = new HashSet<String>();
        Pattern r = Pattern.compile("(?:\\w+\\.)?\\w+(?=\\()");
        Matcher m = r.matcher(eval);
        while (m.find()) uniqueResult.add(eval.substring(m.start(), m.end()));
        ArrayList<String> result = new ArrayList<String>();
        for (String element : uniqueResult) result.add(element);
        Collections.sort(result);
        return result;
    }
}
