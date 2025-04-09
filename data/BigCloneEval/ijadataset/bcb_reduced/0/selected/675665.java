package org.openjf.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTemplate {

    private TemplateField fields[];

    private String template;

    public SimpleTemplate(String template) {
        this.template = template;
        parse();
    }

    public class TextField implements TemplateField {

        private String text;

        public TextField(String text) {
            this.text = text;
        }

        public String value(Map evaluators) {
            return text;
        }
    }

    public class DollarField implements TemplateField {

        private String content;

        private String namespace;

        private String expression;

        public DollarField(String content) {
            this.content = content.trim();
            int dot = this.content.indexOf('.');
            if (dot >= 0) {
                namespace = this.content.substring(0, dot);
                expression = this.content.substring(dot + 1);
            } else {
                namespace = this.content;
                expression = null;
            }
        }

        public String value(Map model) {
            TemplateModel evaluator = (TemplateModel) model.get(namespace);
            if (evaluator == null) {
                return "${" + content + "}";
            } else {
                String result = evaluator.value(expression);
                if (result != null) {
                    return result;
                } else {
                    return "${" + content + "}";
                }
            }
        }
    }

    protected void parse() {
        List fieldList = new ArrayList();
        int lastEnd = 0;
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String text = template.substring(lastEnd, matcher.start());
                fieldList.add(new TextField(text));
            }
            fieldList.add(new DollarField(matcher.group(1)));
            lastEnd = matcher.end();
        }
        if (lastEnd < template.length()) {
            String text = template.substring(lastEnd);
            fieldList.add(new TextField(text));
        }
        fields = new TemplateField[fieldList.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = (TemplateField) fieldList.get(i);
        }
    }

    public void format(StringBuffer sb, Map model) {
        if (model == null) {
            model = new HashMap(0);
        }
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i].value(model));
        }
    }

    public String format(Map model) {
        StringBuffer sb = new StringBuffer();
        format(sb, model);
        return sb.toString();
    }
}
