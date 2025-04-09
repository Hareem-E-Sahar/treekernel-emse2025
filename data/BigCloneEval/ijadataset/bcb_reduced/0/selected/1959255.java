package org.kalypso.nofdpidss.profiles.chart.layers;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.changes.PointAdd;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyEdit;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfilObsHelper;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.model.wspm.ui.view.ProfilViewData;
import org.kalypso.model.wspm.ui.view.chart.AbstractPolyLineLayer;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartView;
import org.kalypso.nofdpidss.profiles.INofdpWspmConstants;
import org.kalypso.nofdpidss.profiles.chart.panels.GelaendePanel;
import org.kalypso.nofdpidss.profiles.i18n.Messages;
import org.kalypso.observation.result.IRecord;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.util.LogicalRange;

public class GelaendeLayer extends AbstractPolyLineLayer implements IProfilChartLayer {

    public GelaendeLayer(final ProfilChartView pcv) {
        super(INofdpWspmConstants.LAYER_GELAENDE, Messages.GelaendeLayer_0, pcv, pcv.getDomainRange(), pcv.getValueRangeLeft(), new String[] { IWspmConstants.POINT_PROPERTY_HOEHE }, true, true, true);
        setColors(setColor(pcv.getColorRegistry()));
    }

    @Override
    public IProfilView createLayerPanel(final IProfil pem, final ProfilViewData viewData) {
        return new GelaendePanel(pem, viewData);
    }

    @Override
    public IRecord[] getPoints() {
        return getProfil().getPoints();
    }

    /**
   * @see com.bce.eind.core.profil.IProfilListener#onProfilChanged(com.bce.eind.core.profil.changes.ProfilChangeHint,
   *      com.bce.eind.core.profil.IProfilChange[])
   */
    @Override
    public void onProfilChanged(final ProfilChangeHint hint, final IProfilChange[] changes) {
        if (!(hint.isPointValuesChanged() || hint.isPointsChanged())) return;
        final AxisRange domainRange = getDomainRange();
        final AxisRange valueRange = getValueRange();
        final double left = domainRange.getLogicalFrom();
        final double right = domainRange.getLogicalTo();
        final double top = valueRange.getLogicalTo();
        final double bottom = valueRange.getLogicalFrom();
        for (final IProfilChange change : changes) if (change instanceof PointPropertyEdit || change instanceof PointAdd) for (final IRecord point : (IRecord[]) change.getObjects()) try {
            final double breite = (Double) point.getValue(ProfilObsHelper.getPropertyFromId(point, IWspmConstants.POINT_PROPERTY_BREITE));
            final double hoehe = (Double) point.getValue(ProfilObsHelper.getPropertyFromId(point, IWspmConstants.POINT_PROPERTY_HOEHE));
            if (breite > right || breite < left || hoehe > top || hoehe < bottom) {
                valueRange.setLogicalRange(new LogicalRange(Math.min(hoehe, bottom), Math.max(hoehe, top)));
                domainRange.setLogicalRange(new LogicalRange(Math.min(breite, left), Math.max(breite, right)));
            }
        } catch (final Exception e) {
            return;
        }
    }

    @Override
    public void paintLegend(final GCWrapper gc) {
        final Rectangle clipping = gc.getClipping();
        final int left = clipping.x;
        final int top = clipping.y;
        final int right = clipping.x + clipping.width;
        final int bottom = clipping.y + clipping.width;
        final int midx = (left + right) / 2;
        final int midy = (top + bottom) / 2;
        drawStationline(gc, midx, midy, midx, bottom);
        gc.setLineWidth(1);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(m_colors[0]);
        gc.drawOval(midx - 2, midy - 2, 4, 4);
        gc.drawLine(left, top, midx, midy);
        gc.drawLine(midx, midy, right, midy);
    }

    /**
   * @see IProfilChartLayer#removeYourself()
   */
    public void removeYourself() {
        throw new UnsupportedOperationException();
    }

    private final Color[] setColor(final ColorRegistry cr) {
        if (!cr.getKeySet().contains(INofdpWspmConstants.LAYER_GELAENDE)) cr.put(INofdpWspmConstants.LAYER_GELAENDE, new RGB(255, 150, 0));
        return new Color[] { cr.get(INofdpWspmConstants.LAYER_GELAENDE) };
    }

    @Override
    public String toString() {
        return Messages.GelaendeLayer_1;
    }
}
