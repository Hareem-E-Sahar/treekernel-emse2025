package grammarscope.dependency;

import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.FourPassImageShaper;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbsoluteCrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport.Lens;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyImageLensSupport;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;
import edu.uci.ics.jung.visualization.util.Animator;
import grammarscope.common.Utils;
import grammarscope.dependency.Decorators.Decorator;
import grammarscope.dependency.Decorators.HasLabel;
import grammarscope.dependency.ModelRenderer.Analysis;
import grammarscope.dependency.Settings.Setting;
import grammarscope.dependency.SubGraph.PartitionMode;
import grammarscope.parser.PrintHelpers;
import grammarscope.parser.Sentence;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jung.BasicEdgeLabelRenderer;
import jung.BasicEdgeRenderer;
import jung.BasicRenderer;
import jung.BasicVertexLabelRenderer;
import jung.BiAggregateLayout;
import jung.Bracket;
import jung.BracketEdgeIndexFunction;
import jung.DefaultEdgeLabelRenderer;
import jung.EdgeOffsetFunctions;
import jung.EdgeOffsetFunctionsImpl;
import jung.Hyperbolic;
import jung.LayoutUtils;
import jung.VertexWeightFunction;
import jung.VertexWeightFunction.HasVertexWeightFunction;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

/**
 * View
 * 
 * @author Bernard Bou
 */
public class View extends JPanel implements Printable {

    private static final long serialVersionUID = 1L;

    /**
	 * Analysis
	 */
    private Analysis theAnalysis;

    /**
	 * Data model
	 */
    private Model theModel;

    /**
	 * Filter
	 */
    private RelationFilter theFilter;

    /**
	 * Settings
	 */
    private final Settings theSettings;

    /**
	 * Visualization viewer
	 */
    private VisualizationViewer<Node, Edge> theVisualizationViewer;

    /**
	 * Scroll pane, if any
	 */
    private GraphZoomScrollPane theGraphZoomScrollPane;

    /**
	 * Background image
	 */
    public Image theBackImage = null;

    /**
	 * Node default image
	 */
    public Image theNodeImage = null;

    /**
	 * Node to icon map cache
	 */
    private final HashMap<Node, Icon> theNodeIconCache = new HashMap<Node, Icon>();

    /**
	 * Edge index function
	 */
    private final EdgeIndexFunction<Node, Edge> theBracketEdgeIndexFunction = new BracketEdgeIndexFunction<Node, Edge>();

    /**
	 * Edge offset functions
	 */
    private final EdgeOffsetFunctions<Node, Edge> theBracketEdgeOffsetFunctions = new EdgeOffsetFunctionsImpl<Node, Edge>();

    /**
	 * Lens support
	 */
    private AbstractLensSupport<Node, Edge> theLensSupport;

    /**
	 * Lens type
	 */
    public static enum LensType implements Decorator, HasLabel {

        NONE("None"), HYPERBOLICLAYOUT("Hyperbolic layout"), HYPERBOLICVIEW("Hyperbolic layout"), HYPERBOLICIMAGE("Hyperbolic image"), MAGNIFYIMAGE("Magnify image");

        private final String theLabel;

        private LensType(final String thisLabel) {
            this.theLabel = thisLabel;
        }

        @Override
        public String toLabel() {
            return this.theLabel;
        }

        @Override
        public Object toValue() {
            return this;
        }
    }

    /**
	 * Node modes
	 */
    public static enum NodeMode implements Decorator, HasLabel {

        BITMAP("Bitmap"), LABELED("Labeled");

        private final String theLabel;

        private NodeMode(final String thisLabel) {
            this.theLabel = thisLabel;
        }

        @Override
        public String toLabel() {
            return this.theLabel;
        }

        @Override
        public Object toValue() {
            return this;
        }
    }

    public enum LayerType implements Decorator, HasLabel {

        LAYOUT("Layout", Layer.LAYOUT), VIEW("View", Layer.VIEW), BOTH("Both", null);

        private final Layer theLayer;

        private final String theLabel;

        private LayerType(final String thisLabel, final Layer thisLayer) {
            this.theLayer = thisLayer;
            this.theLabel = thisLabel;
        }

        @Override
        public String toLabel() {
            return this.theLabel;
        }

        @Override
        public Object toValue() {
            return this;
        }

        public Layer toLayer() {
            return this.theLayer;
        }
    }

    /**
	 * Partition modes
	 */
    private PartitionMode thePartitionMode = PartitionMode.CATEGORY;

    /**
	 * Render with enhanced icons for vertices
	 */
    public static boolean enhancedIconPaintingForVertex = true;

    /**
	 * Outline vertex shapes
	 */
    public static boolean outlineVertexIcons = false;

    /**
	 * Tooltip with score tags
	 */
    public static boolean tooltipWithScores = false;

    /**
	 * Tooltip with score tags
	 */
    public static boolean frameView = false;

    /**
	 * Selected color
	 */
    private static final Color theSelectedColor = Color.RED;

    /**
	 * Frame colors
	 */
    private static final Color theFrameViewColor = Color.GRAY.brighter();

    private static final Color theFrameViewColor1 = Color.PINK;

    private static final Color theFrameViewColor2 = Color.YELLOW.brighter();

    /**
	 * Construct
	 * 
	 * @param theseSettings
	 *            settings
	 * @param thisFilter
	 *            filter
	 */
    public View(final Settings theseSettings, final RelationFilter thisFilter) {
        this.theModel = null;
        this.theSettings = theseSettings;
        this.theFilter = thisFilter;
        View.setSettingsExternal(theseSettings);
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());
        initialize();
    }

    /**
	 * Initialize view
	 */
    private void initialize() {
        final JComponent thisComponent = make();
        if (getComponentCount() > 0) {
            this.remove(0);
        }
        this.add(thisComponent, BorderLayout.CENTER);
        validate();
    }

    /**
	 * Make view component
	 * 
	 * @return view component
	 */
    protected JComponent make() {
        this.theVisualizationViewer = makeViewer();
        scaleDownLayout();
        this.theVisualizationViewer.setRenderer(new BasicRenderer<Node, Edge>());
        final RenderContext<Node, Edge> thisRenderContext = this.theVisualizationViewer.getRenderContext();
        thisRenderContext.setLabelOffset(this.theSettings.theEdgeLabelOffset);
        this.theVisualizationViewer.getRenderer().setVertexRenderer(new BasicVertexRenderer<Node, Edge>() {

            @Override
            protected void paintIconForVertex(final RenderContext<Node, Edge> thatRenderContext, final Node thisNode, final Layout<Node, Edge> thisLayout) {
                if (!View.enhancedIconPaintingForVertex) {
                    super.paintIconForVertex(thatRenderContext, thisNode, thisLayout);
                    return;
                }
                Point2D p = thisLayout.transform(thisNode);
                p = thatRenderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
                final float x = (float) p.getX();
                final float y = (float) p.getY();
                final GraphicsDecorator thisGraphics = thatRenderContext.getGraphicsContext();
                final Transformer<Node, Icon> thisTransformer = thatRenderContext.getVertexIconTransformer();
                final Icon thisIcon = thisTransformer.transform(thisNode);
                if (thisIcon == null || View.outlineVertexIcons) {
                    final Shape thisShape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(thatRenderContext.getVertexShapeTransformer().transform(thisNode));
                    paintShapeForVertex(thatRenderContext, thisNode, thisShape);
                }
                if (thisIcon != null) {
                    final int xLoc = (int) (x - thisIcon.getIconWidth() / 2);
                    final int yLoc = (int) (y - thisIcon.getIconHeight() / 2);
                    thisIcon.paintIcon(thatRenderContext.getScreenDevice(), thisGraphics.getDelegate(), xLoc, yLoc);
                }
            }
        });
        final BasicVertexLabelRenderer<Node, Edge> thisVertexLabelRenderer = new BasicVertexLabelRenderer<Node, Edge>(this.theSettings.theNodeLabelXOffset, this.theSettings.theNodeLabelYOffset);
        this.theVisualizationViewer.getRenderer().setVertexLabelRenderer(thisVertexLabelRenderer);
        thisRenderContext.setVertexLabelTransformer(new Transformer<Node, String>() {

            @SuppressWarnings({ "synthetic-access", "boxing" })
            @Override
            public String transform(final Node thisNode) {
                if (View.this.theSettings.theNodeMode != NodeMode.LABELED) return null;
                String thisLabel = thisNode.getLabel();
                if (View.this.theSettings.labelNodesWithRank) {
                    thisLabel = String.format("%s-%d", thisLabel, thisNode.getIndex());
                }
                return thisLabel;
            }
        });
        this.theVisualizationViewer.getRenderer().getVertexLabelRenderer().setPosition(this.theSettings.theNodeLabelPosition);
        thisRenderContext.setVertexFillPaintTransformer(new Transformer<Node, Paint>() {

            @Override
            public Paint transform(final Node input) {
                return Node.theBackColor;
            }
        });
        this.theVisualizationViewer.setForeground(Node.theForeColor);
        thisRenderContext.setVertexIconTransformer(new Transformer<Node, Icon>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Icon transform(final Node thisNode) {
                if (View.this.theSettings.theNodeMode == NodeMode.BITMAP) {
                    Icon thisIcon = View.this.theNodeIconCache.get(thisNode);
                    if (thisIcon != null) return thisIcon;
                    final BufferedImage thisImage = View.this.makeBufferedImage(thisNode);
                    thisIcon = new LayeredIcon(thisImage);
                    View.this.theNodeIconCache.put(thisNode, thisIcon);
                    return thisIcon;
                }
                final Image thisImage = thisNode.getImage();
                if (thisImage != null) return new LayeredIcon(thisImage);
                if (View.this.theNodeImage != null) return new LayeredIcon(View.this.theNodeImage);
                return null;
            }
        });
        final VertexIconShapeTransformer<Node> thisVertexIconShapeTransformer = new VertexIconShapeTransformer<Node>(new EllipseVertexShapeTransformer<Node>()) {

            private final boolean rectangular = false;

            @Override
            public Shape transform(final Node thisNode) {
                final Icon thisIcon = this.iconMap.get(thisNode);
                if (thisIcon != null && thisIcon instanceof ImageIcon) {
                    final Image thisImage = ((ImageIcon) thisIcon).getImage();
                    Shape thisShape = this.shapeMap.get(thisImage);
                    if (thisShape == null) {
                        thisShape = this.rectangular ? new Rectangle2D.Float(0, 0, thisImage.getWidth(null), thisImage.getHeight(null)) : FourPassImageShaper.getShape(thisImage, 30);
                        if (thisShape.getBounds().getWidth() > 0 && thisShape.getBounds().getHeight() > 0) {
                            final int width = thisImage.getWidth(null);
                            final int height = thisImage.getHeight(null);
                            final AffineTransform thisTransform = AffineTransform.getTranslateInstance(-width / 2, -height / 2);
                            thisShape = thisTransform.createTransformedShape(thisShape);
                            this.shapeMap.put(thisImage, thisShape);
                        }
                    }
                    return thisShape;
                }
                return this.delegate.transform(thisNode);
            }
        };
        thisVertexIconShapeTransformer.setIconMap(this.theNodeIconCache);
        thisRenderContext.setVertexShapeTransformer(thisVertexIconShapeTransformer);
        thisRenderContext.setVertexDrawPaintTransformer(new Transformer<Node, Paint>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Paint transform(final Node thisNode) {
                if (View.this.theVisualizationViewer.getPickedVertexState().isPicked(thisNode)) return View.theSelectedColor;
                final Color thisColor = thisNode.getForeColor();
                if (thisColor != null) return thisColor;
                return View.this.theSettings.theNodeForeColor != null ? View.this.theSettings.theNodeForeColor : Color.BLACK;
            }
        });
        thisRenderContext.setVertexFillPaintTransformer(new Transformer<Node, Paint>() {

            @Override
            public Paint transform(final Node thisNode) {
                return thisNode.getBackColor();
            }
        });
        thisRenderContext.setVertexFontTransformer(new Transformer<Node, Font>() {

            private final Font theDefaultFont = new Font("SansSerif", Font.PLAIN, 14);

            @SuppressWarnings("synthetic-access")
            @Override
            public Font transform(final Node thisNode) {
                if (View.this.theSettings.theNodeFontFace != null && View.this.theSettings.theNodeFontSize != 0) return new Font(View.this.theSettings.theNodeFontFace, Font.PLAIN, View.this.theSettings.theNodeFontSize);
                return this.theDefaultFont;
            }
        });
        this.theVisualizationViewer.getRenderer().setEdgeRenderer(new BasicEdgeRenderer<Node, Edge>() {
        });
        this.theVisualizationViewer.getRenderer().setEdgeLabelRenderer(new BasicEdgeLabelRenderer<Node, Edge>() {
        });
        thisRenderContext.setEdgeLabelTransformer(new Transformer<Edge, String>() {

            @Override
            public String transform(final Edge thisEdge) {
                return thisEdge.getLabel();
            }
        });
        final DefaultEdgeLabelRenderer thisEdgeLabelRenderer = new DefaultEdgeLabelRenderer(this.theSettings.theEdgeLabelForeColor);
        thisEdgeLabelRenderer.setBackground(this.theSettings.theEdgeLabelBackColor);
        thisEdgeLabelRenderer.setForeground(this.theSettings.theEdgeLabelForeColor);
        thisEdgeLabelRenderer.setRotateEdgeLabels(true);
        thisRenderContext.setEdgeLabelRenderer(thisEdgeLabelRenderer);
        thisRenderContext.setEdgeLabelClosenessTransformer(new Transformer<Context<Graph<Node, Edge>, Edge>, Number>() {

            @SuppressWarnings({ "boxing", "synthetic-access" })
            @Override
            public Number transform(final Context<Graph<Node, Edge>, Edge> input) {
                return View.this.theSettings.theEdgeLabelCloseness;
            }
        });
        thisRenderContext.setEdgeFontTransformer(new Transformer<Edge, Font>() {

            private final Font theDefaultFont = new Font("SansSerif", Font.PLAIN, 14);

            @SuppressWarnings("synthetic-access")
            @Override
            public Font transform(final Edge thisEdge) {
                if (View.this.theSettings.theEdgeLabelFontFace != null && View.this.theSettings.theEdgeLabelFontSize != 0) return new Font(View.this.theSettings.theEdgeLabelFontFace, Font.PLAIN, View.this.theSettings.theEdgeLabelFontSize);
                return this.theDefaultFont;
            }
        });
        final AbstractEdgeShapeTransformer<Node, Edge> thisEdgeShapeTransformer = makeEdgeShapeInstance(this.theSettings.theEdgeShapeTransformer);
        thisRenderContext.setEdgeShapeTransformer(thisEdgeShapeTransformer);
        thisRenderContext.setEdgeStrokeTransformer(new Transformer<Edge, Stroke>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Stroke transform(final Edge thisEdge) {
                if (thisEdge.getIsHidden()) return View.this.theSettings.theHiddenEdgeStroke;
                return View.this.theSettings.theEdgeStroke;
            }
        });
        final Transformer<Edge, Paint> thisEdge2PaintTransformer = new Transformer<Edge, Paint>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public Paint transform(final Edge thisEdge) {
                if (View.this.theVisualizationViewer.getPickedEdgeState().isPicked(thisEdge)) return View.theSelectedColor;
                final Color thisColor = thisEdge.getColor();
                if (thisColor != null) return thisColor;
                return View.this.theSettings.theEdgeColor != null ? View.this.theSettings.theEdgeColor : Color.BLACK;
            }
        };
        thisRenderContext.setEdgeDrawPaintTransformer(thisEdge2PaintTransformer);
        thisRenderContext.setArrowDrawPaintTransformer(thisEdge2PaintTransformer);
        thisRenderContext.setEdgeFillPaintTransformer(new Transformer<Edge, Paint>() {

            @Override
            public Paint transform(final Edge thisEdge) {
                return null;
            }
        });
        thisRenderContext.setEdgeIncludePredicate(makeEdgeIncludePredicate());
        this.theVisualizationViewer.setVertexToolTipTransformer(new Transformer<Node, String>() {

            @SuppressWarnings("boxing")
            @Override
            public String transform(final Node thisNode) {
                return String.format("<html><strong>%s</strong> %d<br>%s</html>", thisNode.getLabel(), thisNode.getIndex(), thisNode.getAncestors().replaceAll("\n", "<br>"));
            }
        });
        this.theVisualizationViewer.setEdgeToolTipTransformer(new Transformer<Edge, String>() {

            @Override
            public String transform(final Edge thisEdge) {
                return String.format("<html><strong>%s</strong><br>%s<br>%s</html>", thisEdge.getLabel(), thisEdge.getContent(), thisEdge.getAncestors().replaceAll("\n", "<br>"));
            }
        });
        this.theVisualizationViewer.setToolTipText(null);
        this.theVisualizationViewer.addPreRenderPaintable(new VisualizationServer.Paintable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void paint(final Graphics thisGraphics) {
                final Rectangle theseBounds = View.this.theVisualizationViewer.getBounds();
                final Color thisBackColor = View.this.theSettings.theBackColor;
                if (thisBackColor != null) {
                    thisGraphics.setColor(View.this.theSettings.theBackColor);
                    thisGraphics.fillRect(0, 0, theseBounds.width, theseBounds.height);
                    final Graphics2D thisGraphics2D = (Graphics2D) thisGraphics;
                    final MultiLayerTransformer thisTransformer = View.this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer();
                    if (View.this.theSettings.frameView) {
                        final Dimension thisSize = View.this.theVisualizationViewer.getGraphLayout().getSize();
                        final RoundRectangle2D thisRectangle = new RoundRectangle2D.Double();
                        thisRectangle.setRoundRect(0, 0, thisSize.getWidth(), thisSize.getHeight(), 25, 25);
                        final Shape thisShape = thisTransformer.transform(thisRectangle);
                        thisGraphics2D.setColor(View.theFrameViewColor);
                        thisGraphics2D.draw(thisShape);
                        if (View.this.theSettings.split) {
                            final Pair<Rectangle> theseSpaces = LayoutUtils.split(View.this.getGraphLayout().getSize(), View.this.theSettings.splitVertically, View.this.theSettings.theSplitRatio);
                            final Rectangle thisSpace1 = theseSpaces.getFirst();
                            final Rectangle thisSpace2 = theseSpaces.getSecond();
                            final RoundRectangle2D thisRectangle1 = new RoundRectangle2D.Double();
                            thisRectangle1.setRoundRect(thisSpace1.x, thisSpace1.y, thisSpace1.getWidth(), thisSpace1.getHeight(), 25, 25);
                            final RoundRectangle2D thisRectangle2 = new RoundRectangle2D.Double();
                            thisRectangle2.setRoundRect(thisSpace2.x, thisSpace2.y, thisSpace2.getWidth(), thisSpace2.getHeight(), 25, 25);
                            final Shape thisShape1 = thisTransformer.transform(thisRectangle1);
                            final Shape thisShape2 = thisTransformer.transform(thisRectangle2);
                            thisGraphics.setColor(View.theFrameViewColor1);
                            thisGraphics2D.draw(thisShape1);
                            thisGraphics.setColor(View.theFrameViewColor2);
                            thisGraphics2D.draw(thisShape2);
                        }
                    }
                }
                if (View.this.theBackImage != null) {
                    thisGraphics.setPaintMode();
                    final int xinc = View.this.theBackImage.getWidth(null);
                    final int yinc = View.this.theBackImage.getHeight(null);
                    if (xinc <= 0 || yinc <= 0) return;
                    final int xmax = theseBounds.width;
                    final int ymax = theseBounds.height;
                    for (int y = 0; y < ymax; y += yinc) {
                        for (int x = 0; x < xmax; x += xinc) {
                            thisGraphics.drawImage(View.this.theBackImage, x, y, null);
                        }
                    }
                }
            }

            @Override
            public boolean useTransform() {
                return false;
            }
        });
        this.theVisualizationViewer.setPickSupport(new ShapePickSupport<Node, Edge>(this.theVisualizationViewer));
        this.theVisualizationViewer.getPickedVertexState().addItemListener(new PickWithIconListener(thisRenderContext.getVertexIconTransformer()));
        final AbstractModalGraphMouse thisGraphMouse = new DefaultModalGraphMouse<Node, Edge>() {

            @Override
            protected void loadPlugins() {
                super.loadPlugins();
                final ScalingGraphMousePlugin sp = (ScalingGraphMousePlugin) this.scalingPlugin;
                final CrossoverScalingControl thisScaler = new AbsoluteCrossoverScalingControl();
                sp.setScaler(thisScaler);
                final CrossoverScalingControl thatScaler = (CrossoverScalingControl) sp.getScaler();
                thatScaler.setCrossover(1.0d);
            }
        };
        thisGraphMouse.setMode(Mode.PICKING);
        this.theVisualizationViewer.setGraphMouse(thisGraphMouse);
        this.theVisualizationViewer.addKeyListener(thisGraphMouse.getModeKeyListener());
        this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
            }
        });
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(final ComponentEvent evt) {
                final Dimension thisNewDimension = View.this.getSize();
                resized(thisNewDimension);
            }
        });
        if (this.theSettings.scroll) {
            this.theGraphZoomScrollPane = new GraphZoomScrollPane(this.theVisualizationViewer);
            return this.theGraphZoomScrollPane;
        }
        return this.theVisualizationViewer;
    }

    /**
	 * Set data model
	 * 
	 * @param thisModel
	 *            data model
	 * @param thisAnalysis
	 *            analysis
	 */
    public void set(final Model thisModel, final Analysis thisAnalysis) {
        this.theModel = thisModel;
        this.theAnalysis = thisAnalysis;
        applyModel();
        getVisualizationViewer().setToolTipText(this.theSettings.tooltip ? makeToolTipText() : null);
    }

    /**
	 * Get data model
	 * 
	 * @return data model
	 */
    public Model getModel() {
        return this.theModel;
    }

    /**
	 * Get sentence
	 * 
	 * @return sentence
	 */
    public Sentence getSentence() {
        return this.theAnalysis.sentence;
    }

    /**
	 * Get tree
	 * 
	 * @return tree
	 */
    public Tree getTree() {
        return this.theAnalysis.tree;
    }

    /**
	 * Make image from Url
	 * 
	 * @param thisUrlString
	 *            url string
	 * @return image
	 */
    private static Image getImage(final String thisUrlString) {
        if (thisUrlString != null) if (thisUrlString.startsWith("http:") || thisUrlString.startsWith("ftp:") || thisUrlString.startsWith("file:")) {
            try {
                final URL thisUrl = new URL(thisUrlString);
                return Toolkit.getDefaultToolkit().getImage(thisUrl);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else return new ImageIcon(Settings.class.getResource(thisUrlString)).getImage();
        return null;
    }

    /**
	 * Get image base Url
	 * 
	 * @param thatImageBase
	 *            image base Url string
	 * @param thatBase
	 *            document base Url string
	 * @return image base Url
	 */
    public static URL getImagesBase(final String thatImageBase, final String thatBase) {
        String thisImageBase = thatImageBase;
        URL thisBase = null;
        if (thisImageBase != null && !thisImageBase.isEmpty()) {
            try {
                return new URL(thisImageBase);
            } catch (final MalformedURLException e) {
            }
            if (!thisImageBase.endsWith("/")) {
                thisImageBase += "/";
            }
            thisBase = View.getBase(thatBase);
            try {
                return new URL(thisBase, thisImageBase);
            } catch (final MalformedURLException thisException) {
            }
        }
        if (thisBase == null) {
            thisBase = View.getBase(thatBase);
        }
        try {
            return new URL(thisBase, "images/");
        } catch (final MalformedURLException thisException) {
        }
        return null;
    }

    /**
	 * Get document base Url
	 * 
	 * @param thatBase
	 *            document base Url string
	 * @return document base Url
	 */
    private static URL getBase(final String thatBase) {
        final String thisBase = thatBase;
        String thisURLString = thisBase != null ? thisBase : System.getProperty("user.dir");
        if (!thisURLString.endsWith("/")) {
            thisURLString += "/";
        }
        try {
            return new URL(thisURLString);
        } catch (final MalformedURLException e) {
            try {
                final File thisFolder = new File(thisURLString);
                return thisFolder.toURI().toURL();
            } catch (final MalformedURLException thisException) {
            }
        }
        return null;
    }

    /**
	 * Get settings
	 * 
	 * @return settings
	 */
    public Settings getSettings() {
        return this.theSettings;
    }

    /**
	 * Apply model
	 */
    public void applyModel() {
        final VisualizationModel<Node, Edge> thisViewModel = makeViewModel(this.theModel, this.theSettings.theLayoutDimension);
        this.theVisualizationViewer.setModel(thisViewModel);
        this.theBracketEdgeIndexFunction.reset();
        this.theBracketEdgeOffsetFunctions.getSourceOffsetFunction().reset();
        this.theBracketEdgeOffsetFunctions.getDestOffsetFunction().reset();
        this.theVisualizationViewer.repaint();
    }

    public void applyLayout(final Class<? extends Layout<Node, Edge>> thisLayoutClass) {
        this.theSettings.split = false;
        final Layout<Node, Edge> thatLayout = this.theVisualizationViewer.getModel().getGraphLayout();
        final Layout<Node, Edge> thisLayout = makeLayoutInstance(thisLayoutClass, this.theModel.theGraph);
        thisLayout.setSize(this.theSettings.theLayoutDimension);
        this.theVisualizationViewer.getModel().setGraphLayout(thisLayout);
        animateToNewLayout(thatLayout, thisLayout);
    }

    /**
	 * Apply layout as per settings values
	 * 
	 * @param thisLayout1
	 *            whether to apply sublayout1
	 * @param thisLayout2
	 *            whether to apply sublayout1
	 */
    private void applyLayout(final boolean thisLayout1, final boolean thisLayout2) {
        if (!this.theSettings.split) {
            applyLayout(this.theSettings.theLayout);
        } else {
            applySubLayouts(thisLayout1 ? this.theSettings.theLayout1 : null, thisLayout2 ? this.theSettings.theLayout2 : null);
        }
    }

    /**
	 * Get graph layout
	 * 
	 * @return layout
	 */
    public Layout<Node, Edge> getGraphLayout() {
        return this.theVisualizationViewer.getGraphLayout();
    }

    /**
	 * Make layout from model
	 * 
	 * @param thisModel
	 *            model
	 * @param thisLayoutDimension
	 *            layout dimension
	 * @return layout
	 */
    protected Layout<Node, Edge> makeLayout(final Model thisModel, final Dimension thisLayoutDimension) {
        if (thisModel == null) return new StaticLayout<Node, Edge>(new SparseGraph<Node, Edge>());
        final Layout<Node, Edge> thisGraphLayout = makeLayoutInstance(this.theSettings.theLayout, thisModel.theGraph);
        if (!this.theSettings.split) return thisGraphLayout;
        return makeAggregateLayout(SubGraph.makeSubgraphs(thisModel.theGraph, this.thePartitionMode), LayoutUtils.split(thisLayoutDimension, this.theSettings.splitVertically, this.theSettings.theSplitRatio), thisGraphLayout);
    }

    /**
	 * Dynamically create layout instance
	 * 
	 * @param thisLayoutClass
	 *            layout class
	 * @param thisGraph
	 *            graph
	 * @return layout instance
	 */
    @SuppressWarnings("unchecked")
    protected AbstractLayout<Node, Edge> makeLayoutInstance(final Class<? extends Layout<Node, Edge>> thisLayoutClass, final Object thisGraph) {
        if (thisLayoutClass == null) return null;
        try {
            final Constructor<? extends Layout<Node, Edge>> thisConstructor = thisLayoutClass.getConstructor(new Class[] { Graph.class });
            final Object[] theseArgs = { thisGraph };
            final Object thisLayoutObject = thisConstructor.newInstance(theseArgs);
            final AbstractLayout<Node, Edge> thisLayout = (AbstractLayout<Node, Edge>) thisLayoutObject;
            if (thisLayoutObject instanceof HasVertexWeightFunction) {
                final HasVertexWeightFunction<Node> thisVertexWeightFunctionClient = (HasVertexWeightFunction<Node>) thisLayoutObject;
                thisVertexWeightFunctionClient.setWeightFunction(makeVertexWeightFunction());
            }
            return thisLayout;
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Animate from that layout to this
	 * 
	 * @param thatLayout
	 *            old layout
	 * @param thisLayout
	 *            new layout
	 */
    public void animateToNewLayout(final Layout<Node, Edge> thatLayout, final Layout<Node, Edge> thisLayout) {
        if (thatLayout != null) {
            final LayoutTransition<Node, Edge> thisTransition = new LayoutTransition<Node, Edge>(this.theVisualizationViewer, thatLayout, thisLayout);
            final Animator thisAnimator = new Animator(thisTransition);
            thisAnimator.start();
        }
    }

    /**
	 * Lock or unlock layout
	 * 
	 * @param thisFlag
	 *            whether to lock or unlock
	 */
    public void lockLayout(final boolean thisFlag) {
        final Layout<Node, Edge> thisLayout = this.theVisualizationViewer.getGraphLayout();
        final Relaxer thisRelaxer = this.theVisualizationViewer.getModel().getRelaxer();
        if (thisRelaxer != null) {
            if (thisFlag) {
                thisRelaxer.pause();
            } else {
                thisRelaxer.resume();
            }
        }
        for (final Node thisNode : this.theModel.theGraph.getVertices()) {
            thisLayout.lock(thisNode, thisFlag);
        }
    }

    public void applySubLayouts(final Class<? extends Layout<Node, Edge>> thisLayoutClass1, final Class<? extends Layout<Node, Edge>> thisLayoutClass2) {
        this.theSettings.split = true;
        final Pair<Graph<Node, Edge>> theseSubGraphs = SubGraph.makeSubgraphs(this.theModel.theGraph, this.thePartitionMode);
        applySubLayouts(theseSubGraphs, thisLayoutClass1, thisLayoutClass2);
    }

    /**
	 * Apply sublayouts to subgraphs
	 * 
	 * @param theseSubGraphs
	 *            pair of subgraphs
	 * @param thisLayoutClass1
	 *            sublayout1 class
	 * @param thisLayoutClass2
	 *            sublayout2 class
	 */
    private void applySubLayouts(final Pair<Graph<Node, Edge>> theseSubGraphs, final Class<? extends Layout<Node, Edge>> thisLayoutClass1, final Class<? extends Layout<Node, Edge>> thisLayoutClass2) {
        final Pair<Rectangle> theseSpaces = LayoutUtils.split(this.theVisualizationViewer.getModel().getGraphLayout().getSize(), this.theSettings.splitVertically, this.theSettings.theSplitRatio);
        final Layout<Node, Edge> thatLayout = this.theVisualizationViewer.getModel().getGraphLayout();
        final Layout<Node, Edge> thisLayout = makeAggregateLayout(theseSubGraphs, theseSpaces, thatLayout, thisLayoutClass1, thisLayoutClass2);
        this.theVisualizationViewer.getModel().setGraphLayout(thisLayout, thatLayout.getSize());
        animateToNewLayout(thatLayout, thisLayout);
    }

    /**
	 * Make aggregate layout
	 * 
	 * @param theseSubGraphs
	 *            subgraphs
	 * @param theseSpaces
	 *            subspaces
	 * @param thisDelegateLayout
	 *            delegate layout
	 * @return aggregate layout
	 */
    protected AggregateLayout<Node, Edge> makeAggregateLayout(final Pair<Graph<Node, Edge>> theseSubGraphs, final Pair<Rectangle> theseSpaces, final Layout<Node, Edge> thisDelegateLayout) {
        return makeAggregateLayout(theseSubGraphs, theseSpaces, thisDelegateLayout, this.theSettings.theLayout1, this.theSettings.theLayout2);
    }

    /**
	 * Make aggregate layout
	 * 
	 * @param theseSubGraphs
	 *            subgraphs
	 * @param theseSpaces
	 *            subspaces
	 * @param thatLayout
	 *            current layout
	 * @param thatLayoutClass1
	 *            layout class for subgraph 1 (may be null: preserve existing)
	 * @param thatLayoutClass2
	 *            layout class for subgraph 2 (may be null: preserve existing)
	 * @return aggregate layout
	 */
    protected AggregateLayout<Node, Edge> makeAggregateLayout(final Pair<Graph<Node, Edge>> theseSubGraphs, final Pair<Rectangle> theseSpaces, final Layout<Node, Edge> thatLayout, final Class<? extends Layout<Node, Edge>> thatLayoutClass1, final Class<? extends Layout<Node, Edge>> thatLayoutClass2) {
        Class<? extends Layout<Node, Edge>> thisLayoutClass1 = thatLayoutClass1;
        Class<? extends Layout<Node, Edge>> thisLayoutClass2 = thatLayoutClass2;
        Layout<Node, Edge> thisLayout = thatLayout;
        if (thisLayout instanceof LayoutDecorator) {
            thisLayout = ((LayoutDecorator<Node, Edge>) thisLayout).getDelegate();
        }
        BiAggregateLayout<Node, Edge> thisAggregateLayout = null;
        if (thisLayout instanceof BiAggregateLayout) {
            thisAggregateLayout = (BiAggregateLayout<Node, Edge>) thisLayout;
        } else {
            if (thisLayoutClass1 == null) {
                thisLayoutClass1 = this.theSettings.theLayout1;
            }
            if (thisLayoutClass2 == null) {
                thisLayoutClass2 = this.theSettings.theLayout2;
            }
            thisAggregateLayout = new BiAggregateLayout<Node, Edge>(thisLayout);
        }
        thisAggregateLayout.put(makeLayoutInstance(thisLayoutClass1, theseSubGraphs.getFirst()), makeLayoutInstance(thisLayoutClass2, theseSubGraphs.getSecond()), theseSpaces);
        return thisAggregateLayout;
    }

    public void applyPartitionMode(final PartitionMode thisMode) {
        this.thePartitionMode = thisMode;
        final Layout<Node, Edge> thatLayout = this.theVisualizationViewer.getModel().getGraphLayout();
        Layout<Node, Edge> thisLayout = thatLayout;
        if (thisLayout instanceof LayoutDecorator) {
            thisLayout = ((LayoutDecorator<Node, Edge>) thisLayout).getDelegate();
        }
        if (thisLayout instanceof BiAggregateLayout) {
            final BiAggregateLayout<Node, Edge> thisAggregateLayout = (BiAggregateLayout<Node, Edge>) thisLayout;
            final Pair<Graph<Node, Edge>> theseSubGraphs = SubGraph.makeSubgraphs(this.theModel.theGraph, this.thePartitionMode);
            thisAggregateLayout.reassignSubgraphs(theseSubGraphs);
            this.theVisualizationViewer.repaint();
        }
    }

    /**
	 * Make view model
	 * 
	 * @param thisModel
	 *            model
	 * @param thisLayoutDimension
	 *            layout dimension
	 * @return view model
	 */
    protected VisualizationModel<Node, Edge> makeViewModel(final Model thisModel, final Dimension thisLayoutDimension) {
        final Layout<Node, Edge> thisLayout = makeLayout(thisModel, thisLayoutDimension);
        final VisualizationModel<Node, Edge> thisVisualizationModel = new DefaultVisualizationModel<Node, Edge>(thisLayout, thisLayoutDimension);
        final Relaxer thisRelaxer = thisVisualizationModel.getRelaxer();
        if (thisRelaxer != null) {
            thisRelaxer.setSleepTime(100);
        }
        return thisVisualizationModel;
    }

    /**
	 * Make viewer
	 * 
	 * @return view component
	 */
    protected VisualizationViewer<Node, Edge> makeViewer() {
        this.setSize(this.theSettings.theViewDimension);
        final VisualizationModel<Node, Edge> thisVisualizationModel = makeViewModel(null, this.theSettings.theLayoutDimension);
        final VisualizationViewer<Node, Edge> thisViewer = new VisualizationViewer<Node, Edge>(thisVisualizationModel, this.theSettings.theViewDimension);
        final Map<Key, Object> theseRenderingHints = new HashMap<Key, Object>();
        theseRenderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        theseRenderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        thisViewer.setRenderingHints(theseRenderingHints);
        return thisViewer;
    }

    /**
	 * Get visualization viewer
	 * 
	 * @return viewer
	 */
    public VisualizationViewer<Node, Edge> getVisualizationViewer() {
        return this.theVisualizationViewer;
    }

    /**
	 * Resize layout
	 * 
	 * @param thisSize
	 *            size
	 */
    protected synchronized void resizeLayout(final Dimension thisSize) {
        final Layout<Node, Edge> thatLayout = getGraphLayout();
        if (thatLayout == null) return;
        final Layout<Node, Edge> thisLayout = LayoutUtils.scale(thatLayout, thisSize);
        this.theVisualizationViewer.getModel().setGraphLayout(thisLayout);
        animateToNewLayout(thatLayout, thisLayout);
    }

    /**
	 * Resize layout
	 * 
	 * @param scaleX
	 *            X scale factor
	 * @param scaleY
	 *            Y scale factor
	 */
    protected synchronized void resizeLayout(final double scaleX, final double scaleY) {
        final Layout<Node, Edge> thatLayout = getGraphLayout();
        if (thatLayout == null) return;
        final Layout<Node, Edge> thisLayout = LayoutUtils.scale(thatLayout, scaleX, scaleY);
        this.theVisualizationViewer.getModel().setGraphLayout(thisLayout);
        animateToNewLayout(thatLayout, thisLayout);
    }

    @SuppressWarnings("unused")
    private static int compareSize(final Dimension thisDimension1, final Dimension thisDimension2) {
        if (thisDimension1.equals(thisDimension2)) return 0;
        if (thisDimension1.width > thisDimension2.width && thisDimension1.height > thisDimension2.height) return 1;
        if (thisDimension1.width >= thisDimension2.width && thisDimension1.height < thisDimension2.height) return -3;
        if (thisDimension1.width < thisDimension2.width && thisDimension1.height >= thisDimension2.height) return -2;
        return -1;
    }

    /**
	 * Resize layout
	 * 
	 * @param thisViewSize
	 *            size
	 */
    protected synchronized void resized(final Dimension thisViewSize) {
        setPreferredSize(thisViewSize);
        if (this.theVisualizationViewer != null) {
            this.theVisualizationViewer.setPreferredSize(thisViewSize);
        }
        if (!this.theSettings.scaleToView) return;
        final Layout<Node, Edge> thatLayout = getGraphLayout();
        if (thatLayout == null) return;
        resizeToView();
    }

    /**
	 * Resize layout to view
	 */
    public void resizeToView() {
        final Dimension thisViewSize = this.theVisualizationViewer.getSize();
        this.theSettings.theLayoutDimension = thisViewSize;
        resizeLayout(thisViewSize);
        scaleDownLayout();
    }

    /**
	 * Zoom in layout
	 */
    public void zoomIn() {
        resizeLayout(1.2d, 1.2d);
    }

    /**
	 * Zoom out layout
	 */
    public void zoomOut() {
        resizeLayout(0.8d, 0.8d);
    }

    public void applyResetTransforms(final Layer thisLayer, final boolean scaleDown) {
        final MultiLayerTransformer thisMultiLayerTransformer = this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer();
        if (thisLayer == null) {
            thisMultiLayerTransformer.setToIdentity();
        } else {
            final MutableTransformer thisTransformer = thisMultiLayerTransformer.getTransformer(thisLayer);
            thisTransformer.setToIdentity();
        }
        if (scaleDown && (thisLayer == null || thisLayer == Layer.LAYOUT)) {
            scaleDownLayout();
        }
    }

    /**
	 * Scale down layout to avoid laying out of nodes on borders
	 */
    public void scaleDownLayout() {
        if (!this.theSettings.scaleDown) return;
        final Dimension thisLayoutSize = this.theSettings.theLayoutDimension;
        final Rectangle thisRectangle = new Rectangle(thisLayoutSize);
        thisRectangle.grow(-25, -25);
        final Point2D thisCenter = new Point2D.Double(thisRectangle.getCenterX(), thisRectangle.getCenterY());
        final double sx = thisRectangle.getWidth() / thisLayoutSize.getWidth();
        final double sy = thisRectangle.getHeight() / thisLayoutSize.getHeight();
        final MultiLayerTransformer thisMultiLayerTransformer = this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer();
        final MutableTransformer thisLayoutTransformer = thisMultiLayerTransformer.getTransformer(Layer.LAYOUT);
        thisLayoutTransformer.setScale(sx, sy, thisCenter);
    }

    /**
	 * Pick node
	 * 
	 * @param thisNode
	 *            node
	 */
    public void pick(final Node thisNode) {
        final PickedState<Node> thisPickedVertexState = this.theVisualizationViewer.getPickedVertexState();
        thisPickedVertexState.clear();
        thisPickedVertexState.pick(thisNode, true);
    }

    /**
	 * Unpick currently picked node
	 */
    public void unpickNode() {
        final PickedState<Node> thisPickedVertexState = this.theVisualizationViewer.getPickedVertexState();
        thisPickedVertexState.clear();
    }

    /**
	 * Pick edge
	 * 
	 * @param thisEdge
	 *            edge
	 */
    public void pick(final Edge thisEdge) {
        final PickedState<Edge> thisPickedEdgeState = this.theVisualizationViewer.getPickedEdgeState();
        thisPickedEdgeState.clear();
        thisPickedEdgeState.pick(thisEdge, true);
    }

    /**
	 * Unpick currently picked edge
	 */
    public void unpickEdge() {
        final PickedState<Edge> thisPickedEdgeState = this.theVisualizationViewer.getPickedEdgeState();
        thisPickedEdgeState.clear();
    }

    /**
	 * Get selected nodes
	 * 
	 * @return set of selected nodes
	 */
    public Set<Node> getSelectedNodes() {
        final PickedState<Node> thisPickState = this.theVisualizationViewer.getPickedVertexState();
        if (thisPickState != null) return thisPickState.getPicked();
        return null;
    }

    /**
	 * Get selected edges
	 * 
	 * @return set of selected edges
	 */
    public Set<Edge> getSelectedEdges() {
        final PickedState<Edge> thisPickState = this.theVisualizationViewer.getPickedEdgeState();
        if (thisPickState != null) return thisPickState.getPicked();
        return null;
    }

    /**
	 * Selected node marker
	 */
    static class PickWithIconListener implements ItemListener {

        private final Transformer<Node, Icon> theImager;

        private final Icon theCheck;

        public PickWithIconListener(final Transformer<Node, Icon> thisImager) {
            this.theImager = thisImager;
            this.theCheck = new Checkmark(Color.YELLOW);
        }

        @Override
        public void itemStateChanged(final ItemEvent e) {
            final Node thisNode = (Node) e.getItem();
            if (thisNode == null) return;
            final Icon thisIcon = this.theImager.transform(thisNode);
            if (thisIcon != null && thisIcon instanceof LayeredIcon) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    ((LayeredIcon) thisIcon).add(this.theCheck);
                } else {
                    ((LayeredIcon) thisIcon).remove(this.theCheck);
                }
            }
        }
    }

    /**
	 * Make buffered image to represent this node
	 * 
	 * @param thisNode
	 *            node
	 * @return node image (with label)
	 */
    protected BufferedImage makeBufferedImage(final Node thisNode) {
        Image thisImage = thisNode.getImage();
        if (thisImage == null) {
            thisImage = this.theNodeImage;
        }
        final int mh = 2;
        final int mv = 2;
        int wi = 0;
        int hi = 0;
        if (thisImage != null) {
            wi = Math.max(thisImage.getWidth(null), 0);
            hi = Math.max(thisImage.getHeight(null), 0);
        }
        int wl = 0;
        int hl = 0;
        int ol = 0;
        Font thisFont = null;
        final String thisLabel = thisNode.getLabel();
        if (thisLabel != null) {
            thisFont = this.theVisualizationViewer.getRenderContext().getVertexFontTransformer().transform(thisNode);
            final Graphics thisGraphics = getGraphics();
            thisGraphics.setFont(thisFont);
            final FontMetrics fm = thisGraphics.getFontMetrics();
            ol = fm.getAscent() + mv;
            hl = fm.getDescent() + ol + mv;
            wl = fm.stringWidth(thisLabel) + 2 * mh;
        }
        final int w = Math.max(wi, wl);
        final int h = hi + hl;
        final BufferedImage thisBufferedImage = new BufferedImage(w + 1, h + 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D thisGraphics = thisBufferedImage.createGraphics();
        thisGraphics.setFont(thisFont);
        if (thisImage != null) {
            int p = 0;
            if (wi < wl) {
                p = (wl - wi) / 2;
            }
            thisGraphics.drawImage(thisImage, p, 0, null);
        }
        if (thisLabel != null) {
            int p = 0;
            if (wl < wi) {
                p = (wi - wl) / 2;
            }
            final Paint thisForegroundPaint = this.theVisualizationViewer.getRenderContext().getVertexDrawPaintTransformer().transform(thisNode);
            final Paint thisBackgroundPaint = this.theVisualizationViewer.getRenderContext().getVertexFillPaintTransformer().transform(thisNode);
            final Shape thisShape = new RoundRectangle2D.Float(p, hi, wl, hl, 10.f, 10.f);
            thisGraphics.setPaint(thisBackgroundPaint);
            thisGraphics.fill(thisShape);
            thisGraphics.setPaint(thisForegroundPaint);
            thisGraphics.draw(thisShape);
            thisGraphics.drawString(thisLabel, mh + p, hi + ol);
        }
        return thisBufferedImage;
    }

    /**
	 * Make vertex weight function
	 * 
	 * @return vertex weight function
	 */
    protected VertexWeightFunction<Node> makeVertexWeightFunction() {
        return new VertexWeightFunction<Node>() {

            @Override
            public double getWeight(final Node thisNode) {
                final RenderContext<Node, Edge> thisContext = View.this.getVisualizationViewer().getRenderContext();
                final Font thisFont = thisContext.getVertexFontTransformer().transform(thisNode);
                final String thisText = thisContext.getVertexLabelTransformer().transform(thisNode);
                return View.getTextWidth(thisText, thisFont);
            }
        };
    }

    /**
	 * Get text dimensions
	 * 
	 * @param thisText
	 *            label text
	 * @param thisFont
	 *            font
	 * @return text width
	 */
    protected static double getTextWidth(final String thisText, final Font thisFont) {
        if (thisText == null || thisText.isEmpty()) return 0.d;
        final FontRenderContext thisContext = new FontRenderContext(null, true, false);
        final TextLayout thisLayout = new TextLayout(thisText, thisFont, thisContext);
        final Rectangle2D theseBounds = thisLayout.getBounds();
        return theseBounds.getWidth();
    }

    /**
	 * Make edge include predicate
	 * 
	 * @return edge include predicate
	 */
    protected Predicate<Context<Graph<Node, Edge>, Edge>> makeEdgeIncludePredicate() {
        return new Predicate<Context<Graph<Node, Edge>, Edge>>() {

            @SuppressWarnings({ "synthetic-access" })
            @Override
            public boolean evaluate(final Context<Graph<Node, Edge>, Edge> thisContext) {
                if (!View.this.theSettings.filterEdges) return true;
                final Edge thisEdge = thisContext.element;
                if (View.this.theFilter != null) {
                    final String thisKey = thisEdge.getRelation().getShortName();
                    if (View.this.theFilter.theMap != null && View.this.theFilter.theMap.containsKey(thisKey)) {
                        final Boolean thisBoolean = View.this.theFilter.theMap.get(thisKey);
                        return thisBoolean == null ? false : thisBoolean.booleanValue();
                    }
                }
                return !thisEdge.getIsHidden();
            }
        };
    }

    /**
	 * Make edge shape dynamically
	 * 
	 * @param thisEdgeShapeClass
	 *            edge shape class
	 * @return edge shape
	 */
    @SuppressWarnings("unchecked")
    protected AbstractEdgeShapeTransformer<Node, Edge> makeEdgeShapeInstance(final Class<? extends AbstractEdgeShapeTransformer<Node, Edge>> thisEdgeShapeClass) {
        try {
            final Constructor<? extends AbstractEdgeShapeTransformer<Node, Edge>> thisConstructor = thisEdgeShapeClass.getConstructor(new Class[] {});
            final Object[] theseArgs = {};
            final Object thisEdgeShapeObject = thisConstructor.newInstance(theseArgs);
            final AbstractEdgeShapeTransformer<Node, Edge> thisEdgeShapeTransformer = (AbstractEdgeShapeTransformer<Node, Edge>) thisEdgeShapeObject;
            thisEdgeShapeTransformer.setControlOffsetIncrement(this.theSettings.theEdgeControlOffsetIncrement);
            if (thisEdgeShapeTransformer instanceof Bracket) {
                final Bracket<Node, Edge> thisBracket = (Bracket<Node, Edge>) thisEdgeShapeTransformer;
                thisBracket.setEdgeIndexFunction(this.theBracketEdgeIndexFunction);
                thisBracket.setEdgeOffsetFunctions(this.theBracketEdgeOffsetFunctions);
                Bracket.setXOffsetIncrement(this.theSettings.theEdgeXOffsetIncrement);
                Bracket.setYOffsetIncrement(this.theSettings.theEdgeYOffsetIncrement);
            }
            return thisEdgeShapeTransformer;
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Make lens support
	 * 
	 * @param thisLensType
	 *            lens type
	 * @return lens support
	 */
    protected AbstractLensSupport<Node, Edge> makeLensSupport(final LensType thisLensType) {
        AbstractLensSupport<Node, Edge> thisLensSupport = null;
        LensTransformer thisLensTransformer = null;
        Lens thisLens = null;
        switch(thisLensType) {
            case HYPERBOLICLAYOUT:
                {
                    final MutableTransformer thisLayoutTransformerDelegate = this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
                    thisLensTransformer = new HyperbolicTransformer(this.theVisualizationViewer, thisLayoutTransformerDelegate);
                    thisLensSupport = new LayoutLensSupport<Node, Edge>(this.theVisualizationViewer, thisLensTransformer, new ModalLensGraphMouse());
                    thisLens = new Lens(thisLensTransformer);
                    thisLensSupport.setLens(thisLens);
                    break;
                }
            case HYPERBOLICVIEW:
                {
                    final MutableTransformer thisViewTransformerDelegate = this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
                    thisLensTransformer = new HyperbolicShapeTransformer(this.theVisualizationViewer, thisViewTransformerDelegate);
                    thisLensSupport = new ViewLensSupport<Node, Edge>(this.theVisualizationViewer, thisLensTransformer, new ModalLensGraphMouse());
                    thisLens = new Lens(thisLensTransformer);
                    thisLensSupport.setLens(thisLens);
                    break;
                }
            case HYPERBOLICIMAGE:
                {
                    thisLensSupport = Hyperbolic.make(this.theVisualizationViewer);
                    thisLens = new Lens(thisLensSupport.getLensTransformer());
                    thisLensSupport.setLens(thisLens);
                    break;
                }
            case MAGNIFYIMAGE:
                {
                    thisLensSupport = new MagnifyImageLensSupport<Node, Edge>(this.theVisualizationViewer);
                    thisLens = new Lens(thisLensSupport.getLensTransformer());
                    thisLensSupport.setLens(thisLens);
                    break;
                }
            default:
            case NONE:
                {
                    break;
                }
        }
        if (thisLens != null) {
            thisLens.setPaint(this.theSettings.theBackColor);
        }
        return thisLensSupport;
    }

    /**
	 * Status class used as list of things to do
	 */
    private class ToDos {

        public ToDos() {
        }

        public boolean newLayout = false;

        public boolean newLayout1 = false;

        public boolean newLayout2 = false;

        public boolean clearCache = false;

        public boolean repaint = false;
    }

    /**
	 * Refresh
	 * 
	 * @param toDos
	 */
    private void refresh(final ToDos toDos) {
        if (toDos.newLayout || toDos.newLayout1 || toDos.newLayout2) {
            applyLayout(toDos.newLayout1, toDos.newLayout2);
        }
        if (toDos.clearCache) {
            this.theNodeIconCache.clear();
        }
        if (toDos.repaint) {
            this.theVisualizationViewer.repaint();
        }
    }

    /**
	 * Change setting
	 * 
	 * @param thisSetting
	 *            setting key
	 * @param thisValue
	 *            setting value
	 * @param toDos
	 *            toDo things to perform adjusted
	 * @return true if handled
	 */
    private boolean changeSetting(final Setting thisSetting, final Object thisValue, final ToDos toDos) {
        switch(thisSetting) {
            case DEPENDENCYMODE:
                return false;
            case VIEWDIMENSION:
                toDos.repaint = true;
                break;
            case VIEWBACKCOLOR:
                toDos.repaint = true;
                break;
            case VIEWSCROLL:
                toDos.repaint = true;
                break;
            case VIEWSCALETOVIEW:
                toDos.repaint = true;
                break;
            case VIEWSCALEDOWN:
                toDos.repaint = true;
                break;
            case VIEWTOOLTIP:
                getVisualizationViewer().setToolTipText(this.theSettings.tooltip ? makeToolTipText() : null);
                break;
            case VIEWFRAME:
                toDos.repaint = true;
                break;
            case LAYOUT:
                if (!this.theSettings.split) {
                    toDos.newLayout = true;
                }
                break;
            case LAYOUT1:
                if (this.theSettings.split) {
                    toDos.newLayout1 = true;
                }
                break;
            case LAYOUT2:
                if (this.theSettings.split) {
                    toDos.newLayout2 = true;
                }
                break;
            case LAYOUTDIMENSION:
                toDos.newLayout = true;
                break;
            case LAYOUTSPLIT:
                toDos.newLayout = true;
                break;
            case LAYOUTSPLITVERTICALLY:
            case LAYOUTSPLITRATIO:
            case LAYOUTSPLITFUNCTION:
                if (this.theSettings.split) {
                    toDos.newLayout1 = true;
                    toDos.newLayout2 = true;
                }
                break;
            case NODEFORECOLOR:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.theForeColor = (Color) thisValue;
                this.theVisualizationViewer.setForeground(Node.theForeColor);
                break;
            case NODEBACKCOLOR:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.theBackColor = (Color) thisValue;
                break;
            case NODEFONTFACE:
                toDos.clearCache = true;
                toDos.repaint = true;
                break;
            case NODEFONTSIZE:
                toDos.clearCache = true;
                toDos.repaint = true;
                break;
            case NODEMODE:
                toDos.clearCache = true;
                toDos.repaint = true;
                break;
            case NODEIMAGE:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.theNodeImage = View.getImage((String) thisValue);
                break;
            case NODEIMAGENOUN:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.theNounImage = View.getImage((String) thisValue);
                break;
            case NODEIMAGEPRONOUN:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.thePronounImage = View.getImage((String) thisValue);
                break;
            case NODEIMAGEVERB:
                toDos.clearCache = true;
                toDos.repaint = true;
                Node.theVerbImage = View.getImage((String) thisValue);
                break;
            case NODELABELPOSITION:
                {
                    toDos.clearCache = true;
                    toDos.repaint = true;
                    this.theVisualizationViewer.getRenderer().getVertexLabelRenderer().setPosition((Position) thisValue);
                    break;
                }
            case NODELABELXOFFSET:
                {
                    toDos.clearCache = true;
                    toDos.repaint = true;
                    final BasicVertexLabelRenderer<Node, Edge> thisVertexLabelRenderer = (BasicVertexLabelRenderer<Node, Edge>) this.theVisualizationViewer.getRenderer().getVertexLabelRenderer();
                    thisVertexLabelRenderer.setXoffset(this.theSettings.theNodeLabelXOffset);
                    break;
                }
            case NODELABELYOFFSET:
                {
                    toDos.clearCache = true;
                    toDos.repaint = true;
                    final BasicVertexLabelRenderer<Node, Edge> thisVertexLabelRenderer = (BasicVertexLabelRenderer<Node, Edge>) this.theVisualizationViewer.getRenderer().getVertexLabelRenderer();
                    thisVertexLabelRenderer.setYoffset(this.theSettings.theNodeLabelYOffset);
                    break;
                }
            case NODELABELWITHRANK:
                toDos.clearCache = true;
                toDos.repaint = true;
                break;
            case EDGECOLOR:
                Edge.theColor = (Color) thisValue;
                toDos.repaint = true;
                break;
            case EDGESTROKE:
                toDos.repaint = true;
                break;
            case EDGEHIDDENSTROKE:
                toDos.repaint = true;
                break;
            case EDGELABELFORECOLOR:
                {
                    toDos.repaint = true;
                    final DefaultEdgeLabelRenderer thisEdgeLabelRenderer = (DefaultEdgeLabelRenderer) this.theVisualizationViewer.getRenderContext().getEdgeLabelRenderer();
                    thisEdgeLabelRenderer.setForeground(this.theSettings.theEdgeLabelForeColor);
                    break;
                }
            case EDGELABELBACKCOLOR:
                {
                    toDos.repaint = true;
                    final DefaultEdgeLabelRenderer thisEdgeLabelRenderer = (DefaultEdgeLabelRenderer) this.theVisualizationViewer.getRenderContext().getEdgeLabelRenderer();
                    thisEdgeLabelRenderer.setBackground(this.theSettings.theEdgeLabelBackColor);
                    break;
                }
            case EDGELABELFONTFACE:
                toDos.repaint = true;
                break;
            case EDGELABELFONTSIZE:
                toDos.repaint = true;
                break;
            case EDGELABELOFFSET:
                toDos.repaint = true;
                this.theVisualizationViewer.getRenderContext().setLabelOffset(this.theSettings.theEdgeLabelOffset);
                break;
            case EDGELABELCLOSENESS:
                toDos.repaint = true;
                break;
            case EDGESHAPETRANSFORMER:
                toDos.repaint = true;
                setEdgeShape(this.theSettings.theEdgeShapeTransformer);
                break;
            case EDGECONTROLOFFSETINCREMENT:
                toDos.repaint = true;
                final Transformer<Context<Graph<Node, Edge>, Edge>, Shape> t = this.theVisualizationViewer.getRenderContext().getEdgeShapeTransformer();
                if (t instanceof AbstractEdgeShapeTransformer) {
                    final AbstractEdgeShapeTransformer<Node, Edge> thisEdgeShapeTransformer = (AbstractEdgeShapeTransformer<Node, Edge>) t;
                    thisEdgeShapeTransformer.setControlOffsetIncrement(this.theSettings.theEdgeControlOffsetIncrement);
                }
                break;
            case EDGEXOFFSETINCREMENT:
                toDos.repaint = true;
                Bracket.setXOffsetIncrement(this.theSettings.theEdgeXOffsetIncrement);
                break;
            case EDGEYOFFSETINCREMENT:
                toDos.repaint = true;
                Bracket.setYOffsetIncrement(this.theSettings.theEdgeYOffsetIncrement);
                break;
            case EDGEFILTER:
                toDos.repaint = true;
                break;
        }
        return true;
    }

    /**
	 * Apply setting
	 * 
	 * @param thisSetting
	 *            setting key
	 * @param thisValue
	 *            setting value
	 */
    public void applySetting(final Setting thisSetting, final Object thisValue) {
        try {
            this.theSettings.setValue(thisSetting, thisValue);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final ToDos toDos = new ToDos();
        changeSetting(thisSetting, thisValue, toDos);
        refresh(toDos);
    }

    /**
	 * Apply settings
	 * 
	 * @param theseDiffs
	 *            settings diff
	 */
    public void applySettings(final Properties theseDiffs) {
        final ToDos toDos = new ToDos();
        for (final Entry<Object, Object> thisDiff : theseDiffs.entrySet()) {
            final Setting thisSetting = (Setting) thisDiff.getKey();
            final Object thisValue = thisDiff.getValue();
            applySetting(thisSetting, thisValue);
        }
        refresh(toDos);
    }

    /**
	 * Set Settings
	 * 
	 * @param theseSettings
	 *            settings
	 */
    private static void setSettingsExternal(final Settings theseSettings) {
        Node.theNodeImage = View.getImage(theseSettings.theNodeImage);
        Node.theVerbImage = View.getImage(theseSettings.theVerbImage);
        Node.theNounImage = View.getImage(theseSettings.theNounImage);
        Node.thePronounImage = View.getImage(theseSettings.thePronounImage);
        Node.theForeColor = theseSettings.theNodeForeColor;
        Node.theBackColor = theseSettings.theNodeBackColor;
        Edge.theColor = theseSettings.theEdgeColor;
    }

    /**
	 * Set edge shape
	 * 
	 * @param thisEdgeShapeClass
	 *            edge shape
	 */
    public void setEdgeShape(final Class<? extends AbstractEdgeShapeTransformer<Node, Edge>> thisEdgeShapeClass) {
        final AbstractEdgeShapeTransformer<Node, Edge> thisEdgeShapeTransformer = makeEdgeShapeInstance(thisEdgeShapeClass);
        this.theVisualizationViewer.getRenderContext().setEdgeShapeTransformer(thisEdgeShapeTransformer);
    }

    public void applyEdgeShape(final Class<? extends AbstractEdgeShapeTransformer<Node, Edge>> thisEdgeShapeClass) {
        setEdgeShape(thisEdgeShapeClass);
        this.theVisualizationViewer.repaint();
    }

    /**
	 * Apply filtering
	 * 
	 * @param thisFlag
	 *            whether filtering is on
	 */
    public void applyEdgeFilter(final boolean thisFlag) {
        this.theSettings.filterEdges = thisFlag;
        this.theVisualizationViewer.repaint();
    }

    /**
	 * Get grammatical relation filter
	 * 
	 * @return filter
	 */
    public RelationFilter getFilter() {
        return this.theFilter;
    }

    /**
	 * Apply grammatical relation filter
	 * 
	 * @param thisRelationFilter
	 *            relation filter
	 */
    public void applyFilter(final RelationFilter thisRelationFilter) {
        this.theFilter = thisRelationFilter;
        repaint();
    }

    /**
	 * Apply grammatical relation filter on this grammatical relation
	 * 
	 * @param thisGrammaticalRelation
	 *            relation key
	 * @param thisValue
	 *            value
	 */
    public void applyFilter(final GrammaticalRelation thisGrammaticalRelation, final Boolean thisValue) {
        this.theFilter.theMap.put(thisGrammaticalRelation.getShortName(), thisValue);
        repaint();
    }

    /**
	 * Apply node mode
	 * 
	 * @param thisMode
	 *            node mode
	 */
    public void applyNodeMode(final NodeMode thisMode) {
        this.theSettings.theNodeMode = thisMode;
        this.theNodeIconCache.clear();
        this.theVisualizationViewer.repaint();
    }

    /**
	 * Apply mouse mode
	 * 
	 * @param thisMode
	 *            mouse mode
	 */
    public void applyMouseMode(final ModalGraphMouse.Mode thisMode) {
        ((AbstractModalGraphMouse) getVisualizationViewer().getGraphMouse()).setMode(thisMode);
    }

    /**
	 * Apply lens
	 * 
	 * @param thisLensType
	 *            lens type
	 */
    public void applyLens(final LensType thisLensType) {
        if (this.theLensSupport != null) {
            this.theLensSupport.deactivate();
        }
        this.theLensSupport = makeLensSupport(thisLensType);
        if (this.theLensSupport != null) {
            this.theLensSupport.activate();
        }
    }

    /**
	 * Get empty buffered image of viewer's dimension
	 * 
	 * @return image of viewer
	 */
    private BufferedImage getBufferedImage() {
        final Dimension thisDimension = this.theVisualizationViewer.getSize();
        return new BufferedImage(thisDimension.width, thisDimension.height, BufferedImage.TYPE_INT_RGB);
    }

    /**
	 * Get image of viewer
	 * 
	 * @return image of viewer
	 */
    public BufferedImage getImage() {
        final BufferedImage thisImage = getBufferedImage();
        final Graphics2D thisGraphics = thisImage.createGraphics();
        this.theVisualizationViewer.print(thisGraphics);
        return thisImage;
    }

    @Override
    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
        if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
        final Graphics2D g2d = (Graphics2D) graphics;
        this.theVisualizationViewer.setDoubleBuffered(false);
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        this.theVisualizationViewer.paint(g2d);
        this.theVisualizationViewer.setDoubleBuffered(true);
        return Printable.PAGE_EXISTS;
    }

    /**
	 * Add menu to GraphZoomScrollPane if any
	 * 
	 * @param thisMenu
	 *            menu
	 */
    public void addMenuToCorner(final JMenu thisMenu) {
        if (this.theGraphZoomScrollPane == null) return;
        final JMenuBar thisMenuBar = new JMenuBar();
        thisMenuBar.add(thisMenu);
        this.theGraphZoomScrollPane.setCorner(thisMenuBar);
    }

    /**
	 * Make tooltip text
	 * 
	 * @return tooltip text
	 */
    private String makeToolTipText() {
        final String thisSentenceString = Utils.sentenceToTooltipString(this.theAnalysis.sentence, 50);
        final String thisStructureString = Utils.grammaticalStructureToTooltipString(this.theAnalysis.structure, PrintHelpers.VALUE | PrintHelpers.INDEX | PrintHelpers.COLLAPSE | PrintHelpers.TAGLEAVES);
        if (View.tooltipWithScores) {
            final String thisTreeString = Utils.treeToTooltipString(this.theAnalysis.tree, PrintHelpers.VALUE | PrintHelpers.INDEX | PrintHelpers.COLLAPSE | PrintHelpers.TAGLEAVES | PrintHelpers.SCORE);
            return String.format("<html><strong>Typed Dependency graph</strong> for:<br>%s<br><br>%s<br>%s</html>", thisSentenceString, thisStructureString, thisTreeString);
        }
        return String.format("<html><strong>Typed Dependency graph</strong> for:<br>%s<br><br>%s</html>", thisSentenceString, thisStructureString);
    }

    /**
	 * Stringify affine transform
	 * 
	 * @param af
	 * @return affine transform as string
	 */
    @SuppressWarnings("boxing")
    private static String toString(final AffineTransform af) {
        return String.format("scale=%5.1f,%5.1f translate=%5.1f,%5.1f", af.getScaleX(), af.getScaleY(), af.getTranslateX(), af.getTranslateY());
    }

    /**
	 * Stringify multilayer transformer
	 * 
	 * @param mlt
	 *            multilayer transformer
	 * @return multilayer transformer as string
	 */
    private static String toString(final MultiLayerTransformer mlt) {
        return String.format("layout:%s view:%s", View.toString(mlt.getTransformer(Layer.LAYOUT).getTransform()), View.toString(mlt.getTransformer(Layer.VIEW).getTransform()));
    }

    /**
	 * Dump multilayer transformer
	 */
    private void dumpTransforms() {
        final MultiLayerTransformer mlt = View.this.theVisualizationViewer.getRenderContext().getMultiLayerTransformer();
        System.out.println("multilayer transformer : " + View.toString(mlt));
    }

    public void test() {
        final Layout<Node, Edge> thatLayout = this.theVisualizationViewer.getModel().getGraphLayout();
        LayoutUtils.dump(thatLayout);
        dumpTransforms();
        repaint();
    }
}
