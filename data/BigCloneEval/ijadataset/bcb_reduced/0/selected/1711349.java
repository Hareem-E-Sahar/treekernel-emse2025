package freemarker.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Heng Yuan
 * @version $Id: ActionCodeBI.java 740 2012-03-16 07:10:17Z superduperhengyuan@gmail.com $
 */
public class ActionCodeBI extends BuiltIn {

    @SuppressWarnings("unchecked")
    public static void init() {
        BuiltIn.builtins.put("actioncode", new ActionCodeBI());
    }

    @Override
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        TemplateModel model = target.getAsTemplateModel(env);
        if (!(model instanceof StringModel)) throw invalidTypeException(model, target, env, "string");
        StringModel seq = (StringModel) model;
        return new ActionCodeBuilder(seq);
    }

    private class ActionCodeBuilder implements TemplateMethodModelEx {

        private final StringModel m_str;

        private ActionCodeBuilder(StringModel str) {
            super();
            m_str = str;
        }

        @SuppressWarnings("rawtypes")
        public TemplateModel exec(List args) throws TemplateModelException {
            return new StringArraySequence(parseActionCode(m_str.getAsString()));
        }
    }

    /**
	 * A very simple utility function that splits user action code based on
	 * $$ and $[0-9]+ deliminators.  This is useful for substitution purposes
	 * in the generated code.
	 * <p/>
	 * The reason why the parser didn't do this step automatically is to allow
	 * customizable deliminator patterns.
	 * <p/>
	 * An example:
	 * <p/>
	 * parseAction ("abc$$def$1hij")
	 * <p/>
	 * would return {"abc", "$", "def", "1", "hij"}.
	 * <p/>
	 * This piece code was written by gloomyturkey on mitbbs.
	 *
	 * @param	input
	 * 			the user action code.
	 * @return	the splitted strings
	 */
    public static String[] parseActionCode(String input) {
        ArrayList<String> spliter = new ArrayList<String>();
        ArrayList<String> content = new ArrayList<String>();
        Pattern pattern = Pattern.compile("([$][0-9]+|[$][$])");
        Matcher matcher = pattern.matcher(input);
        int index = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start >= index) {
                content.add(input.substring(index, start));
            }
            spliter.add(input.substring(start + 1, end));
            index = end;
        }
        if (index < input.length()) content.add(input.substring(index));
        String[] ret = new String[spliter.size() + content.size()];
        for (int i = 0; i < ret.length; ++i) {
            if ((i % 2) == 0) ret[i] = content.get(i / 2); else ret[i] = spliter.get(i / 2);
        }
        return ret;
    }
}
