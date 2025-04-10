import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.actions.ActionFactory;
import org.icepdf.core.pobjects.actions.GoToAction;
import org.icepdf.core.pobjects.actions.URIAction;
import org.icepdf.core.pobjects.annotations.*;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.core.util.Library;
import org.icepdf.core.views.swing.AbstractPageViewComponent;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * The <code>NewAnnotationPostPageLoad</code> class is an example of how to use
 * <code>DocumentSearchController</code> to find search terms in a
 * Document and convert the found words to annotations.
 * <p/>
 * A file specified at the command line is
 * opened in a JFrame which contains the viewer component and any number
 * of search terms can be specefied after the file name.
 * <p/>
 * Example:
 * SearchHighlight "c:\DevelopersGuide.pdf" "PDF" "ICEsoft" "ICEfaces" "ICEsoft technologies"
 * <p/>
 * The file that is opened in the Viewer RI will have the new annotations created
 * around the found search terms.  The example creates a URIActions for each
 * annotation but optionally can be compiled to build GotoActions to 'goto'
 * the last page of the document when executed.
 * <p/>
 * The annotation are created after the Document view is created so we
 * have to create new annotation slightly differently then if we where adding
 * them before teh view was created.
 *
 * @since 4.0
 */
public class NewAnnotationPostPageLoad {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("At leasts two command line arguments must " + "be specified. ");
            System.out.println("<filename> <term1> ... <termN>");
        }
        String filePath = args[0];
        String[] terms = new String[args.length - 1];
        for (int i = 1, max = args.length; i < max; i++) {
            terms[i - 1] = args[i];
        }
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();
        JFrame applicationFrame = new JFrame();
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationFrame.getContentPane().add(viewerComponentPanel);
        controller.getDocumentViewController().setAnnotationCallback(new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));
        controller.getDocumentViewController().setViewType(DocumentViewControllerImpl.ONE_COLUMN_VIEW);
        controller.openDocument(filePath);
        DocumentSearchController searchController = controller.getDocumentSearchController();
        for (String term : terms) {
            searchController.addSearchTerm(term, false, false);
        }
        Document document = controller.getDocument();
        int pageCount = 25;
        if (pageCount > document.getNumberOfPages()) {
            pageCount = document.getNumberOfPages();
        }
        applicationFrame.pack();
        applicationFrame.setVisible(true);
        AnnotationState annotationState = new AnnotationState(Annotation.VISIBLE_RECTANGLE, LinkAnnotation.HIGHLIGHT_INVERT, 1f, BorderStyle.BORDER_STYLE_SOLID, Color.GRAY);
        ArrayList<WordText> foundWords;
        java.util.List<AbstractPageViewComponent> pageComponents = controller.getDocumentViewController().getDocumentViewModel().getPageComponents();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            foundWords = searchController.searchPage(pageIndex);
            if (foundWords != null) {
                AbstractPageViewComponent pageViewComponent = pageComponents.get(pageIndex);
                for (WordText wordText : foundWords) {
                    LinkAnnotation linkAnnotation = (LinkAnnotation) AnnotationFactory.buildAnnotation(document.getPageTree().getLibrary(), AnnotationFactory.LINK_ANNOTATION, wordText.getBounds().getBounds(), annotationState);
                    org.icepdf.core.pobjects.actions.Action action = createURIAction(document.getPageTree().getLibrary(), "http://www.icepdf.org");
                    linkAnnotation.addAction(action);
                    pageViewComponent.addAnnotation(linkAnnotation);
                }
            }
            searchController.clearSearchHighlight(pageIndex);
        }
    }

    /**
     * Utility for creation a URI action
     *
     * @param library document library reference
     * @param uri     uri that actin will launch
     * @return new URIAction object instance.
     */
    private static org.icepdf.core.pobjects.actions.Action createURIAction(Library library, String uri) {
        URIAction action = (URIAction) ActionFactory.buildAction(library, ActionFactory.URI_ACTION);
        action.setURI(uri);
        return action;
    }

    /**
     * Utility for creation a GoTo action
     *
     * @param library   document library reference
     * @param pageIndex page index to go to.
     * @return new GoToAction object instance.
     */
    private static org.icepdf.core.pobjects.actions.Action createGoToAction(Library library, Document document, int pageIndex) {
        GoToAction action = (GoToAction) ActionFactory.buildAction(library, ActionFactory.GOTO_ACTION);
        Reference pageReference = document.getPageTree().getPageReference(pageIndex);
        Vector destVector = Destination.destinationSyntax(pageReference, Destination.TYPE_FIT);
        action.setDestination(new Destination(library, destVector));
        return action;
    }
}
