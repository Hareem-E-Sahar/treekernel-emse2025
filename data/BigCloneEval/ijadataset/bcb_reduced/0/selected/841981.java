package freemarker.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yuanheng.cookcc.doc.RhsDoc;
import org.yuanheng.cookcc.parser.Production;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.MapModel;
import freemarker.template.*;

/**
 * @author Heng Yuan
 * @version $Id: TypeBI.java 740 2012-03-16 07:10:17Z superduperhengyuan@gmail.com $
 */
public class TypeBI extends BuiltIn {

    @SuppressWarnings("unchecked")
    public static void init() {
        BuiltIn.builtins.put("type", new TypeBI());
    }

    @Override
    TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
        TemplateModel model = target.getAsTemplateModel(env);
        if (!(model instanceof BeanModel)) throw invalidTypeException(model, target, env, "bean");
        BeanModel seq = (BeanModel) model;
        return new TypeBuilder(seq);
    }

    @SuppressWarnings("rawtypes")
    private class TypeBuilder implements TemplateMethodModelEx {

        private final Production m_production;

        private TypeBuilder(BeanModel str) {
            super();
            m_production = (Production) ((RhsDoc) str.getWrappedObject()).getProperty("Production");
        }

        @SuppressWarnings("unchecked")
        public TemplateModel exec(List args) throws TemplateModelException {
            String t = ((SimpleScalar) args.get(0)).getAsString();
            int sym;
            if ("$".equals(t)) sym = m_production.getSymbol(); else sym = m_production.getProduction()[Integer.parseInt(t) - 1];
            MessageFormat format = ((Map<Integer, MessageFormat>) ((MapModel) args.get(1)).getWrappedObject()).get(sym);
            if (format == null) return (SimpleScalar) args.get(2);
            String text = ((SimpleScalar) args.get(2)).getAsString();
            return new SimpleScalar(format.format(new Object[] { text }));
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
