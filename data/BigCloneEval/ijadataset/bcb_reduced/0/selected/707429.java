package org.fudaa.ebli.visuallibrary.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.apache.commons.collections.Predicate;
import org.fudaa.ctulu.CtuluCommand;
import org.fudaa.ebli.ressource.EbliResource;
import org.fudaa.ebli.visuallibrary.EbliNode;
import org.fudaa.ebli.visuallibrary.EbliScene;
import org.fudaa.ebli.visuallibrary.PredicateFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action qui permet de realiser l alignement des composants. ATTTENTION CHOIX DE CONCEPTION: il faut que les EbliNode
 * soit movables (isMovable()== true) pour pouvoir les aligner il faut donc lorss de la creation des nodes remplir les
 * infos dimensions et size comme suit: EbliNodeDefault node = new EbliNodeDefault(); node.setCreator(new
 * EbliWidgetCreatorGraphe(g)); node.setTitle("Graphe"); node.setPreferedSize(new Dimension(300, 300));
 * node.setPreferedLocation(new Point(4, 4)); scene.addNode(node);
 * 
 *@author Adrien Hadoux
 */
@SuppressWarnings("serial")
public abstract class EbliWidgetActionAlign extends EbliWidgetActionFilteredAbstract {

    public static class Bottom extends EbliWidgetActionAlign {

        public Bottom(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("en bas"), EbliResource.EBLI.getToolIcon("aobottom"), "BOTTOM");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int maxY = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).y + _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).height;
            for (final Widget widget : _widgetToMove) {
                maxY = Math.max(maxY, widget.convertLocalToScene(widget.getBounds()).y + widget.convertLocalToScene(widget.getBounds()).height);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldX = p.x;
                p.y = maxY;
                widget.getParentWidget().convertSceneToLocal(p);
                p.y = p.y - widget.getBounds().height - widget.getBounds().y;
                p.x = oldX;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    public static class Center extends EbliWidgetActionAlign {

        public Center(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("Centrer horizontalement"), EbliResource.EBLI.getToolIcon("aocenterv"), "CENTERV");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int minx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).y;
            for (final Widget widget : _widgetToMove) {
                minx = Math.min(minx, widget.convertLocalToScene(widget.getBounds()).y);
            }
            int maxx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).y + _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).height;
            for (final Widget widget : _widgetToMove) {
                maxx = Math.max(maxx, widget.convertLocalToScene(widget.getBounds()).y + widget.convertLocalToScene(widget.getBounds()).height);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldY = p.x;
                p.y = (maxx + minx) / 2;
                widget.getParentWidget().convertSceneToLocal(p);
                p.y = p.y - widget.getBounds().height / 2 - widget.getBounds().y;
                p.x = oldY;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    public static class Left extends EbliWidgetActionAlign {

        public Left(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("Alignement � gauche"), EbliResource.EBLI.getToolIcon("aoleft"), "LEFT");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int minx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).x;
            for (final Widget widget : _widgetToMove) {
                minx = Math.min(minx, widget.convertLocalToScene(widget.getBounds()).x);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldY = p.y;
                p.x = minx;
                widget.getParentWidget().convertSceneToLocal(p);
                p.x = p.x - widget.getBounds().x;
                p.y = oldY;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    public static class Middle extends EbliWidgetActionAlign {

        public Middle(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("Centrer verticalement"), EbliResource.EBLI.getToolIcon("aocenterh"), "CENTERH");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int minx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).x;
            for (final Widget widget : _widgetToMove) {
                minx = Math.min(minx, widget.convertLocalToScene(widget.getBounds()).x);
            }
            int maxx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).x + _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).width;
            for (final Widget widget : _widgetToMove) {
                maxx = Math.max(maxx, widget.convertLocalToScene(widget.getBounds()).x + widget.convertLocalToScene(widget.getBounds()).width);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldY = p.y;
                p.x = (maxx + minx) / 2;
                widget.getParentWidget().convertSceneToLocal(p);
                p.x = p.x - widget.getBounds().width / 2 - widget.getBounds().x;
                p.y = oldY;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    public static class Right extends EbliWidgetActionAlign {

        public Right(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("Alignement � droite"), EbliResource.EBLI.getToolIcon("aoright"), "RIGHT");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int maxx = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).x + _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).width;
            for (final Widget widget : _widgetToMove) {
                maxx = Math.max(maxx, widget.convertLocalToScene(widget.getBounds()).x + widget.convertLocalToScene(widget.getBounds()).width);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldY = p.y;
                p.x = maxx;
                widget.getParentWidget().convertSceneToLocal(p);
                p.x = p.x - widget.getBounds().width - widget.getBounds().x;
                p.y = oldY;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    public static class Top extends EbliWidgetActionAlign {

        public Top(final EbliScene _scene) {
            super(_scene, EbliResource.EBLI.getString("en haut"), EbliResource.EBLI.getToolIcon("aotop"), "TOP");
        }

        @Override
        protected List<Point> getNewPositionFor(final List<Widget> _widgetToMove) {
            int miny = _widgetToMove.get(0).convertLocalToScene(_widgetToMove.get(0).getBounds()).y;
            for (final Widget widget : _widgetToMove) {
                miny = Math.min(miny, widget.convertLocalToScene(widget.getBounds()).y);
            }
            final List<Point> newPoints = new ArrayList<Point>(_widgetToMove.size());
            for (final Widget widget : _widgetToMove) {
                final Point p = widget.getPreferredLocation();
                final int oldX = p.x;
                p.y = miny;
                widget.getParentWidget().convertSceneToLocal(p);
                p.y = p.y - widget.getBounds().y;
                p.x = oldX;
                newPoints.add(p);
            }
            return newPoints;
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public EbliWidgetActionAlign(final EbliScene _scene, final String name, final Icon ic, final String id) {
        super(_scene, name, ic, id);
    }

    @Override
    protected Predicate getAcceptPredicate() {
        return PredicateFactory.geMovablePredicate();
    }

    /**
   * @return le nombre d'objet minimal pour activer la selection
   */
    public int getMinObjectSelectedToEnableAction() {
        return 2;
    }

    @Override
    protected CtuluCommand act(Set<EbliNode> filteredNode) {
        final List<Widget> widgetToMove = new ArrayList<Widget>(filteredNode.size());
        final List<Point> oldSize = new ArrayList<Point>(filteredNode.size());
        for (final Iterator<EbliNode> it = filteredNode.iterator(); it.hasNext(); ) {
            final EbliNode currentNode = it.next();
            final Widget widget = scene_.findWidget(currentNode);
            widgetToMove.add(widget);
            oldSize.add(widget.getPreferredLocation());
        }
        final List<Point> newPos = getNewPositionFor(widgetToMove);
        if (newPos != null) {
            for (int i = 0; i < newPos.size(); i++) {
                widgetToMove.get(i).setPreferredLocation(newPos.get(i));
            }
        }
        return new CommandMove(widgetToMove, oldSize, newPos);
    }

    /**
   * @param _widgetToMove non vide et non null
   * @return les nouvelles positions correspondantes aux points pass�s en parametres
   */
    protected abstract List<Point> getNewPositionFor(List<Widget> _widgetToMove);
}
