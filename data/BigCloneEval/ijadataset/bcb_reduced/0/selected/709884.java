package org.adapit.wctoolkit.fomda.features.view.transformation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.adapit.wctoolkit.fomda.features.util.Exportable;
import org.adapit.wctoolkit.fomda.features.view.Drawable;
import org.adapit.wctoolkit.infrastructure.DefaultApplicationFrame;
import org.adapit.wctoolkit.models.diagram.AbstractGraphicComponent;
import org.adapit.wctoolkit.models.util.AllElements;
import org.adapit.wctoolkit.uml.ext.fomda.metamodel.transformation.SpecializationPoint;
import org.adapit.wctoolkit.uml.ext.fomda.metamodel.transformation.TransformationDescriptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class TransformationImplementationViewComponent extends TransformerViewComponent implements Drawable, Exportable, ActionListener {

    protected TransformationDescriptor observer;

    protected JPopupMenu popup;

    protected Rectangle2D rectangle;

    protected SpecializationPoint specializationPoint;

    protected TransformerViewComponent transformerViewComponent;

    protected SpecializationPointViewComponent specializationPointViewComponent;

    private Line2D.Float line_horiz = new Line2D.Float();

    private Line2D.Float line_vert1 = new Line2D.Float();

    private Line2D.Float line_vert2 = new Line2D.Float();

    private Polygon poly = null;

    public TransformationImplementationViewComponent(TransformationDescriptor observer, SpecializationPoint parameter) throws Exception {
        super(observer);
        center = new Point(35, 35);
        this.observer = observer;
        poly = new Polygon();
        this.specializationPoint = parameter;
        element = parameter;
    }

    public void draw(Graphics g, JFrame frame) {
        Graphics2D g2 = (Graphics2D) g;
        float dash[] = { 5.0f };
        Stroke dashedStroke = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
        Stroke defStroke = g2.getStroke();
        g2.setStroke(dashedStroke);
        if (poly == null) processMovement();
        g2.draw(line_vert1);
        g2.draw(line_vert2);
        g2.draw(line_horiz);
        g2.setStroke(defStroke);
        boolean vi = specializationPoint.isValidImplementation(observer);
        if (!vi) {
            int errory = ((int) transformerViewComponent.getCenter().y - 40);
            int errorx = (int) line_vert1.x1;
            List<String> errors = specializationPoint.getInvalidImplementationMessages(observer);
            try {
                DrawErrorMessage.draw(g2, errorx, errory, errors, messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        g2.setColor(Color.WHITE);
        g2.fill(poly);
        g2.setColor(Color.BLACK);
        g2.draw(poly);
    }

    private void processMovement() {
        int spx = (int) specializationPointViewComponent.getCenter().getX();
        int spy = (int) specializationPointViewComponent.getCenter().getY();
        int tfx = (int) transformerViewComponent.getCenter().getX();
        int tfy = (int) transformerViewComponent.getCenter().getY();
        int spmaxy = specializationPointViewComponent.getMaxY() + 15;
        spy = spmaxy;
        int ymid = (tfy + spy) / 2;
        line_vert1 = new Line2D.Float((float) tfx, ymid, (float) tfx, (float) tfy);
        line_vert2 = new Line2D.Float((float) spx, ymid, (float) spx, (float) spy);
        line_horiz = new Line2D.Float((float) spx, ymid, (float) tfx, ymid);
        int xpoints[] = { spx, spx + 15, spx - 15 };
        int ypoints[] = { spy, spy + 15, spy + 15 };
        poly = new Polygon(xpoints, ypoints, 3);
    }

    public boolean isDraged() {
        return true;
    }

    public void mouseClicked(int mx, int my) {
        if (poly.contains(mx, my)) {
        }
    }

    public void move(int mx, int my) {
    }

    public boolean isInArea(int x, int y) {
        if ((line_vert1.ptLineDistSq(x, y) < 5)) return true;
        return false;
    }

    public void paintSelect(Graphics g, JFrame frame) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(253, 10, 10));
        g2.draw(line_vert1);
        g2.draw(line_vert2);
        g2.draw(line_horiz);
        g2.setColor(Color.WHITE);
        g2.fill(poly);
        g2.setColor(Color.BLACK);
        g2.draw(poly);
    }

    public String toXMLBeanFactory() {
        String str = "";
        return str;
    }

    /**
	 * @return Returns the observer.
	 */
    public TransformationDescriptor getObserver() {
        return observer;
    }

    /**
	 * @param observer
	 *            The observer to set.
	 */
    public void setObserver(TransformationDescriptor observer) {
        this.observer = observer;
    }

    public void makeDifferenceFromCenter(int dx, int dy) {
    }

    public void actionPerformed(ActionEvent e) {
    }

    private static org.adapit.wctoolkit.fomda.features.i18n.I18N_FomdaProfile messages = org.adapit.wctoolkit.fomda.features.i18n.I18N_FomdaProfile.getInstance();

    /**
	 * @return Returns the popup.
	 */
    public JPopupMenu getPopup() {
        {
            popup = new JPopupMenu();
            JMenuItem jmi = new JMenuItem(messages.getMessage("Remove"));
            jmi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        int resp = JOptionPane.showConfirmDialog(DefaultApplicationFrame.getInstance(), "Remover a rela��o entre '" + transformationDescriptor.getName() + "'  e  '" + specializationPoint.getName() + "'?");
                        if (resp != JOptionPane.YES_OPTION) return;
                        specializationPoint.removeElement(transformationDescriptor);
                        diagram.getTransformerViewComponents().remove(TransformationImplementationViewComponent.this);
                        diagram.getPaintPanel().updateUI();
                        DefaultApplicationFrame.getInstance().refreshRootNode();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            popup.add(jmi);
        }
        return popup;
    }

    /**
	 * @param popup
	 *            The popup to set.
	 */
    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }

    public Rectangle2D getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle2D rectangle) {
        this.rectangle = rectangle;
    }

    public String exportXMLForm(int tab) {
        String str = "";
        str += '\n';
        for (int i = 0; i < tab; i++) {
            str += '\t';
        }
        str += "<TransformationImplementationViewComponent " + getExportAttributes() + " >";
        str += exportOwnedElements(tab);
        str += '\n';
        for (int i = 0; i < tab; i++) {
            str += '\t';
        }
        str += "</TransformationImplementationViewComponent>";
        return str;
    }

    @Override
    public String exportOwnedElements(int tab) {
        String str = super.exportOwnedElements(tab);
        if (observer != null) {
            str += '\n';
            for (int i = 0; i < tab + 1; i++) {
                str += '\t';
            }
            str += "<Observer idref=\"" + observer.getId() + "\"/>";
        }
        if (specializationPoint != null) {
            str += '\n';
            for (int i = 0; i < tab + 1; i++) {
                str += '\t';
            }
            str += "<SpecializationPointViewComponent idref=\"" + specializationPoint.getId() + "\"/>";
        }
        if (transformerViewComponent != null) {
            str += '\n';
            for (int i = 0; i < tab + 1; i++) {
                str += '\t';
            }
            str += "<TransformerViewComponent idref=\"" + transformerViewComponent.getId() + "\"/>";
        }
        return str;
    }

    private String idObserver, idSpecPoint, idSpecPointVC, idTransfView;

    public void importXMLForm(Node element) {
        super.importXMLForm(element);
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("Observer")) {
                String idref = n.getAttributes().getNamedItem("idref").getNodeValue();
                idObserver = idref;
            } else if (n.getNodeName().equals("SpecializationPointViewComponent")) {
                String idref = n.getAttributes().getNamedItem("idref").getNodeValue();
                idSpecPointVC = idref;
            } else if (n.getNodeName().equals("SpecializationPoint")) {
                String idref = n.getAttributes().getNamedItem("idref").getNodeValue();
                idSpecPoint = idref;
            } else if (n.getNodeName().equals("TransformerViewComponent")) {
                String idref = n.getAttributes().getNamedItem("idref").getNodeValue();
                idTransfView = idref;
            }
        }
    }

    @Override
    public void afterImport() {
        super.afterImport();
        if (idObserver != null) {
            try {
                observer = (TransformationDescriptor) AllElements.getElementById(idObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (idSpecPoint != null) {
            try {
                specializationPoint = (SpecializationPoint) AllElements.getElementById(idSpecPoint);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (idSpecPointVC != null) {
            try {
                specializationPointViewComponent = (SpecializationPointViewComponent) AllElements.getElementById(idSpecPointVC);
                specializationPointViewComponent.addMovementListener(defMovListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (idTransfView != null) {
            try {
                transformerViewComponent = (TransformerViewComponent) AllElements.getElementById(idTransfView);
                transformerViewComponent.addMovementListener(defMovListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SpecializationPoint getSpecializationPoint() {
        return specializationPoint;
    }

    public static void setMessages(org.adapit.wctoolkit.fomda.features.i18n.I18N_FomdaProfile messages) {
        TransformationImplementationViewComponent.messages = messages;
    }

    public org.adapit.wctoolkit.fomda.features.i18n.I18N_FomdaProfile getMessages() {
        return messages;
    }

    public SpecializationPointViewComponent getSpecializationPointViewComponent() {
        return specializationPointViewComponent;
    }

    public void setSpecializationPointViewComponent(SpecializationPointViewComponent specializationPointViewComponent) {
        this.specializationPointViewComponent = specializationPointViewComponent;
        this.specializationPointViewComponent.addMovementListener(defMovListener);
    }

    public TransformerViewComponent getTransformerViewComponent() {
        return transformerViewComponent;
    }

    public void setTransformerViewComponent(TransformerViewComponent transformerViewComponent) {
        this.transformerViewComponent = transformerViewComponent;
        this.transformerViewComponent.addMovementListener(defMovListener);
    }

    private MovementListener defMovListener = new MovementListener() {

        @Override
        public void componentMoved(AbstractGraphicComponent comp) {
            processMovement();
        }
    };
}
