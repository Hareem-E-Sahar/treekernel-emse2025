package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class SemanticAnnotation extends AbstractType {

    private static String ANNOTATIONBEGIN = "\\[";

    private static String ANNOTATIONEND = "\\]";

    public SemanticAnnotation() {
        super(new AnnotationSectionFinder());
        this.setRenderer(new StandardAnnotationRenderer());
        this.childrenTypes.add(new SemanticAnnotationStartSymbol("["));
        this.childrenTypes.add(new SemanticAnnotationEndSymbol("]"));
        this.childrenTypes.add(new SemanticAnnotationContent());
    }

    public static class AnnotationSectionFinder implements SectionFinder {

        private final String PATTERN = ANNOTATIONBEGIN + "[\\w\\W]*?" + ANNOTATIONEND;

        @Override
        public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
            ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
            Pattern p = Pattern.compile(PATTERN);
            Matcher m = p.matcher(text);
            while (m.find()) {
                String found = m.group();
                if (found.contains("::")) result.add(new SectionFinderResult(m.start(), m.end()));
            }
            return result;
        }
    }
}
