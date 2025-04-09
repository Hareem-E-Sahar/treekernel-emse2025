package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author kazamatzuri
 * 
 */
public class SemanticAnnotationProperty extends AbstractType {

    public SemanticAnnotationProperty() {
        this.sectionFinder = new AnnotationPropertySectionFinder();
        this.childrenTypes.add(new SemanticAnnotationPropertyDelimiter());
        this.childrenTypes.add(new SemanticAnnotationPropertyName());
        this.addSubtreeHandler(new SemanticAnnotationPropertySubTreeHandler());
    }

    public static class AnnotationPropertySectionFinder implements SectionFinder {

        private final String PATTERN = "[(\\w:)?\\w]*::";

        @Override
        public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
            ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
            Pattern p = Pattern.compile(PATTERN);
            Matcher m = p.matcher(text);
            while (m.find()) {
                result.add(new SectionFinderResult(m.start(), m.end()));
            }
            return result;
        }
    }

    private class SemanticAnnotationPropertySubTreeHandler extends OwlSubtreeHandler<SemanticAnnotationProperty> {

        @Override
        public Collection<Message> create(KnowWEArticle article, Section<SemanticAnnotationProperty> s) {
            Section<SemanticAnnotationPropertyName> name = Sections.findChildOfType(s, SemanticAnnotationPropertyName.class);
            IntermediateOwlObject io = new IntermediateOwlObject();
            UpperOntology uo = UpperOntology.getInstance();
            String prop = name.getText();
            URI property = null;
            if (prop.equals("subClassOf") || prop.equals("subPropertyOf")) {
                property = uo.getRDFS(prop);
            } else if (prop.equals("type")) {
                property = uo.getRDF(prop);
            } else if (prop.contains(":")) {
                String ns = SemanticCoreDelegator.getInstance().getNameSpaces().get(prop.split(":")[0]);
                if (ns == null || ns.length() == 0) {
                    io.setBadAttribute("no namespace given");
                    io.setValidPropFlag(false);
                } else if (ns.equals(prop.split(":")[0])) {
                    io.setBadAttribute(ns);
                    io.setValidPropFlag(false);
                } else {
                    property = uo.getHelper().createURI(ns, prop.split(":")[1]);
                }
            } else {
                property = uo.getHelper().createlocalURI(prop);
            }
            io.addLiteral(property);
            KnowWEUtils.storeObject(article, s, OwlHelper.IOO, io);
            return null;
        }
    }
}
