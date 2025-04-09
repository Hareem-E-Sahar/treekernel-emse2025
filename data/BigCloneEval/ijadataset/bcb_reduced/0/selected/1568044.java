package org.kalypso.nofdpidss.profiles.chart.layers;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.changes.ActiveObjectEdit;
import org.kalypso.model.wspm.core.profil.changes.PointMarkerEdit;
import org.kalypso.model.wspm.core.profil.changes.PointMarkerSetPoint;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfilObsHelper;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.ui.profil.operation.ProfilOperationJob;
import org.kalypso.model.wspm.ui.profil.operation.changes.VisibleMarkerEdit;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilChartLayer;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartView;
import org.kalypso.model.wspm.ui.view.chart.color.IProfilColorSet;
import org.kalypso.nofdpidss.profiles.marker.NofdpPointMarker;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import de.belger.swtchart.EditInfo;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.layer.IChartLayer;

public abstract class NofdpAbstractProfileLayer extends AbstractProfilChartLayer {

    public NofdpAbstractProfileLayer(final String layerId, final ProfilChartView chartView, final AxisRange domainRange, final AxisRange valueRange, final String label) {
        super(layerId, chartView, domainRange, valueRange, label);
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#edit(org.eclipse.swt.graphics.Point, java.lang.Object)
   */
    @SuppressWarnings("deprecation")
    @Override
    public void editProfil(final Point point, final Object data) {
        final IProfilPointMarker activeDevider = (IProfilPointMarker) data;
        final IRecord destinationPoint = ProfilUtil.findNearestPoint(getProfil(), screen2logical(point).getX());
        final Object value = destinationPoint.getValue(activeDevider.getId());
        if (value instanceof Boolean) {
            final Boolean b = (Boolean) value;
            if (Boolean.TRUE.equals(b)) return;
        }
        final IRecord oldPos = activeDevider.getPoint();
        if (oldPos != destinationPoint) {
            final ProfilOperation operation = new ProfilOperation(activeDevider.toString() + " moving", getProfil(), true);
            operation.addChange(new PointMarkerSetPoint(activeDevider, destinationPoint));
            operation.addChange(new ActiveObjectEdit(getProfil(), destinationPoint, null));
            new ProfilOperationJob(operation).schedule();
        }
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#getBounds()
   */
    public Rectangle2D getBounds() {
        try {
            final IComponent[] markerTypes = getProfil().getPointMarkerTypes();
            for (final IComponent markerType : markerTypes) {
                final IProfilPointMarker[] deviders = getProfil().getPointMarkerFor(markerType);
                if (deviders.length < 2) return IChartLayer.MINIMAL_RECT;
                final double left = (Double) deviders[0].getPoint().getValue(ProfilObsHelper.getPropertyFromId(deviders[0].getPoint(), IWspmConstants.POINT_PROPERTY_BREITE));
                final double right = (Double) deviders[deviders.length - 1].getPoint().getValue(ProfilObsHelper.getPropertyFromId(deviders[deviders.length - 1].getPoint(), IWspmConstants.POINT_PROPERTY_BREITE));
                final double top = (Double) deviders[0].getPoint().getValue(ProfilObsHelper.getPropertyFromId(deviders[0].getPoint(), IWspmConstants.POINT_PROPERTY_HOEHE));
                return new Rectangle2D.Double(left, top, right - left, 0);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    protected abstract ColorRegistry getColorRegistry();

    private String getDeviderInfo(final IProfilPointMarker devider) {
        try {
            return String.format("%s%n%10.4f [m]", new Object[] { devider.getId().getName(), devider.getPoint().getValue(ProfilObsHelper.getPropertyFromId(devider.getPoint(), IWspmConstants.POINT_PROPERTY_BREITE)) });
        } catch (final Exception e) {
            return "";
        }
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#getHoverInfo(org.eclipse.swt.graphics.Point)
   */
    @Override
    public EditInfo getHoverInfo(final Point mousepoint) {
        if (getViewData().getMarkerVisibility(getMarkerType())) {
            final IProfil profil = getProfil();
            final IProfilPointMarker[] deviders = profil.getPointMarkerFor(ProfilObsHelper.getPropertyFromId(profil, getMarkerType()));
            final AxisRange valueRange = getValueRange();
            final int bottom = valueRange.getScreenFrom() + valueRange.getGapSpace();
            final int top = valueRange.getScreenTo() + valueRange.getGapSpace() + getTopOffset();
            for (final IProfilPointMarker devider : deviders) {
                final IRecord deviderPos = devider.getPoint();
                final double breite = (Double) deviderPos.getValue(ProfilObsHelper.getPropertyFromId(deviderPos, IWspmConstants.POINT_PROPERTY_BREITE));
                final Point point = logical2screen(new Point2D.Double(breite, (Double) deviderPos.getValue(ProfilObsHelper.getPropertyFromId(deviderPos, IWspmConstants.POINT_PROPERTY_HOEHE))));
                final Rectangle devRect = new Rectangle(point.x - 5, top - 5, 10, bottom - top + 10);
                final Rectangle pointRect = new Rectangle(point.x - 5, point.y - 5, 10, 10);
                if (pointRect.contains(mousepoint.x, mousepoint.y)) return null;
                if (devRect.contains(mousepoint.x, mousepoint.y)) return new EditInfo(this, devRect, devider, getDeviderInfo(devider));
            }
        }
        return null;
    }

    public abstract String getMarkerType();

    protected abstract int getTopOffset();

    /**
   * @see com.bce.eind.core.profil.IProfilListener#onProfilChanged(com.bce.eind.core.profil.changes.ProfilChangeHint,
   *      com.bce.eind.core.profil.IProfilChange[])
   */
    @Override
    public void onProfilChanged(final ProfilChangeHint hint, final IProfilChange[] changes) {
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper)
   */
    public void paint(final GCWrapper gc) {
        gc.setLineWidth(3);
        gc.setLineStyle(SWT.LINE_SOLID);
        final int top = getValueRange().getScreenTo() + getValueRange().getGapSpace() + getTopOffset();
        final IComponent component = ProfilObsHelper.getPropertyFromId(getProfil(), getMarkerType());
        if (getViewData().getMarkerVisibility(component.getId())) {
            final IProfilPointMarker[] deviders = getProfil().getPointMarkerFor(component);
            final int bottom = getValueRange().getScreenFrom() + getValueRange().getGapSpace();
            gc.setForeground(getColorRegistry().get(component.getId()));
            for (final IProfilPointMarker d : deviders) {
                final IRecord point = d.getPoint();
                final double leftvalue = (Double) point.getValue(ProfilObsHelper.getPropertyFromId(point, IWspmConstants.POINT_PROPERTY_BREITE));
                final int left = (int) getDomainRange().logical2screen(leftvalue);
                gc.drawLine(left, top, left, bottom);
            }
        }
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#paintDrag(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Point, java.lang.Object)
   */
    @Override
    public void paintDrag(final GCWrapper gc, final Point editing, final Object hoverData) {
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setLineWidth(1);
        gc.setForeground(getColorRegistry().get(IProfilColorSet.COLOUR_AXIS_FOREGROUND));
        if (hoverData instanceof IProfilPointMarker) {
            final AxisRange valueRange = getValueRange();
            final int bottom = valueRange.getScreenFrom() + valueRange.getGapSpace();
            final int top = valueRange.getScreenTo() + valueRange.getGapSpace();
            try {
                final IRecord destinationPoint = ProfilUtil.findNearestPoint(getProfil(), screen2logical(editing).getX());
                final Point destP = logical2screen(new Point2D.Double((Double) destinationPoint.getValue(ProfilObsHelper.getPropertyFromId(destinationPoint, IWspmConstants.POINT_PROPERTY_BREITE)), (Double) destinationPoint.getValue(ProfilObsHelper.getPropertyFromId(destinationPoint, IWspmConstants.POINT_PROPERTY_HOEHE))));
                gc.drawRectangle(destP.x - 5, top - 5, 10, bottom - top + 10);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
   * @see de.belger.swtchart.layer.IChartLayer#paintLegend(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper)
   */
    @Override
    public void paintLegend(final GCWrapper gc) {
        final Rectangle clipping = gc.getClipping();
        final int left = clipping.x;
        final int top = clipping.y;
        final int right = clipping.x + clipping.width;
        final int bottom = clipping.y + clipping.width;
        final int midx = (left + right) / 2;
        gc.drawLine(midx, top, midx, bottom);
    }

    public void removeYourself() {
        throw new UnsupportedOperationException();
    }

    /**
   * @see org.kalypso.model.wspm.ui.profil.view.chart.layer.AbstractProfilChartLayer#setActivePoint(java.lang.Object)
   */
    @Override
    public void setActivePoint(final Object data) {
        if (data instanceof IProfilPointMarker) {
            final IProfilPointMarker devider = (IProfilPointMarker) data;
            final IRecord activePoint = devider.getPoint();
            final ProfilOperation operation = new ProfilOperation("", getProfil(), new ActiveObjectEdit(getProfil(), activePoint, null), true);
            final IStatus status = operation.execute(new NullProgressMonitor(), null);
            operation.dispose();
            if (!status.isOK()) KalypsoModelWspmUIPlugin.getDefault().getLog().log(status);
        }
    }

    public void createPointMarkers(final ProfilOperation operation, final IProfil profil, final ProfilChartView view) {
        final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders(profil.getType());
        final IComponent component = provider.getPointProperty(getMarkerType());
        if (!profil.hasPointProperty(component)) profil.addPointProperty(component);
        final TupleResult result = profil.getResult();
        final IRecord rStart = result.get(0);
        final IRecord rEnd = result.get(result.size() - 1);
        operation.addChange(new PointMarkerEdit(new NofdpPointMarker(rStart, component), true));
        operation.addChange(new PointMarkerEdit(new NofdpPointMarker(rEnd, component), true));
        operation.addChange(new VisibleMarkerEdit(view.getViewData(), component.getId(), true));
        operation.addChange(new ActiveObjectEdit(profil, rStart, component));
        operation.addChange(new ActiveObjectEdit(profil, rEnd, component));
    }
}
