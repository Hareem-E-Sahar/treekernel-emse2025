// Copyright 2007 Konrad Twardowski
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.makagiga.chart;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.makagiga.commons.BooleanProperty;
import org.makagiga.commons.ColorProperty;
import org.makagiga.commons.EnumProperty;
import org.makagiga.commons.FloatProperty;
import org.makagiga.commons.FontProperty;
import org.makagiga.commons.IntegerProperty;
import org.makagiga.commons.MColor;
import org.makagiga.commons.StringProperty;
import org.makagiga.commons.TK;
import org.makagiga.commons.UI;
import org.makagiga.commons.WTFError;
import org.makagiga.commons.painters.Painter;

/**
 * @since 2.0
 */
public class ChartPainter<M extends ChartModel>
implements
	Painter,
	Serializable
{
	private static final long serialVersionUID = -4303513409081208559L;
	
	// public
	
	public enum TextType { RECT, ROUND_RECT }

	/**
	 * @since 3.2
	 */
	public static final int MINIMUM_OUTLINE_SIZE = 1;

	/**
	 * @since 3.2
	 */
	public static final int MAXIMUM_OUTLINE_SIZE = 20;

	// chart properties
	
	/**
	 * @since 2.4
	 */
	public final ColorProperty backgroundColor = new ColorProperty(Color.WHITE);
	
	public final IntegerProperty chartSize = new IntegerProperty(200);
	
	// fx properties
	public final BooleanProperty fxShine = new BooleanProperty(true);
	
	// image properties
	public final BooleanProperty imageScale = new BooleanProperty(true);
	
	// outline properties
	public final BooleanProperty outlineBorder = new BooleanProperty(true);
	public final ColorProperty outlineColor = new ColorProperty(Color.WHITE);
	public final IntegerProperty outlineSize = new IntegerProperty(3);
	public final BooleanProperty outlineVisible = new BooleanProperty(true);
	
	// shadow properties
	public final ColorProperty shadowColor = new ColorProperty(Color.GRAY);
	public final IntegerProperty shadowOffset = new IntegerProperty(5);
	public final BooleanProperty shadowVisible = new BooleanProperty(true);

	// text properties
	
	/**
	 * @since 3.0
	 */
	public final FloatProperty textAlpha = new FloatProperty(0.7f);

	public final ColorProperty textBackground = new ColorProperty(0xffffdc);
	public final IntegerProperty textDistance = new IntegerProperty(20);
	public final FontProperty textFont = new FontProperty(Font.DIALOG, Font.BOLD, UI.getDefaultFontSize());
	public final ColorProperty textForeground = new ColorProperty((Color)null);

	/**
	 * @since 3.2
	 */
	public final StringProperty textFormat = new StringProperty();

	public final BooleanProperty textLineAutoColor = new BooleanProperty();
	public final IntegerProperty textLineSize = new IntegerProperty(1);
	public final IntegerProperty textPadding = new IntegerProperty(3);
	public final EnumProperty<TextType> textType = new EnumProperty<>(TextType.ROUND_RECT);
	public final BooleanProperty textVisible = new BooleanProperty(true);
	
	// protected

	transient protected CacheInfo[] itemCache;
	protected Insets imageArea = UI.createInsets(0);
	protected int chartX;
	protected int chartY;
	protected int height;
	protected int midChartSize;
	protected int midX;
	protected int midY;
	protected int width;
	protected long totalNumber;

	// private

	private int lightMargin = 13;
	private int lightSize;
	private int outlineWidth;
	private int shadowSize;
	private M model;
	
	// package protected
	
	boolean interactive;
	ChartModel.Item activeItem;
	
	// public
	
	public ChartPainter(final M model) {
		this.model = TK.checkNull(model, "model");
	}
	
	public M getModel() { return model; }
	
	public void setModel(final M value) {
		model = TK.checkNull(value, "value");
	}
	
	public void paint(final Graphics2D g, final int width, final int height, final Color background, final Point mousePosition) {
		this.width = width;
		this.height = height;
		midX = width / 2;
		midY = height / 2;
		
		int cs = chartSize.get();
		midChartSize = cs / 2;
		chartX = midX - midChartSize;
		chartY = midY - midChartSize;
		
		totalNumber = model.getTotal();
		
		activeItem = null;
		
		Shape _oldClip = g.getClip();
		Composite _oldComposite = g.getComposite();
		Paint _oldPaint = g.getPaint();
		
		lightSize = cs - (lightMargin * 2) / 2;
		int so = shadowOffset.get();
		shadowSize = lightSize + lightMargin + 12;
		
		outlineWidth = outlineVisible.get() ? outlineSize.get() : 0;
		outlineWidth = Math.max(outlineWidth, MINIMUM_OUTLINE_SIZE);
		outlineWidth = Math.min(outlineWidth, MAXIMUM_OUTLINE_SIZE);
		imageArea.left = chartX - outlineWidth;
		imageArea.top = chartY - outlineWidth;
		imageArea.right = imageArea.left + cs + so + (outlineWidth * 2);
		imageArea.bottom = imageArea.top + cs + so + (outlineWidth * 2);

		drawBackground(g, background);

		setupRenderingHints(g);

		if (shadowVisible.get())
			drawShadow(g);

		resetCache();
		
		int itemCount = model.getRowCount();
		for (int index = 0; index < itemCount; index++) {
			CacheInfo cacheInfo = itemCache[index];
			ChartModel.Item i = model.getRowAt(index);
			Shape pie = drawPie(g, i, cacheInfo, cacheInfo.startAngle, mousePosition);
			if (i.getImage() != null)
				drawIcon(g, i, cacheInfo, cacheInfo.startAngle, pie);
		}
		
		drawTopGradient(g);

		g.setClip(_oldClip);
		g.setComposite(_oldComposite);
		g.setPaint(_oldPaint);

		if (outlineWidth > 0)
			drawOutline(g);

// TODO: 2.0: paint text lines "under" pie/shadow and text over pie
		if (textVisible.get())
			drawText(g);

		g.setClip(_oldClip);
		g.setComposite(_oldComposite);
		g.setPaint(_oldPaint);
	}
	
	public BufferedImage toBufferedImage(final Color background) {
		return toBufferedImage(width, height, background);
	}

	public BufferedImage toBufferedImage(final int width, final int height, final Color background) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try {
			paint(
				g,
				width,
				height,
				background,
				null
			);
		}
		finally {
			g.dispose();
		}
		
		return cutImage(image, imageArea, width, height);
	}
	
	// Painter

	public Insets getPainterInsets(final Component c) { return null; }
	
	public void paint(final Component c, final Graphics2D g) {
		paint(c, g, 0, 0, c.getWidth(), c.getHeight());
	}
	
	public void paint(final Component c, final Graphics2D g, final int x, final int y, final int width, final int height) {
		paint(
			g,
			width,
			height,
			c.isOpaque() ? backgroundColor.get() : null,
			interactive ? c.getMousePosition() : null
		);
	}

	// protected
	
	protected void drawBackground(final Graphics2D g, final Color background) {
		if (background != null) {
			g.setColor(background);
			g.fillRect(0, 0, width, height);
		}
	}
	
	protected void drawIcon(final Graphics2D g, final ChartModel.Item item, final CacheInfo cacheInfo, final int startAngle, final Shape pie) {
		Point middle = new Point(
			(int)(midX + cacheInfo.cos),
			(int)(midY + cacheInfo.sin) + 1
		);

		Shape oldClip = g.getClip();
		double lineLength = cacheInfo.labelStart.distance(middle) / 2;
		g.clip(pie);
		if (cacheInfo.arcAngle > 190) {
			Image i = item.getImage();
			if (imageScale.get()) {
				g.drawImage(
					i,
					chartX,
					chartY,
					chartSize.get(),
					chartSize.get(),
					null
				);
			}
			else {
				g.drawImage(
					i,
					midX - i.getWidth(null) / 2,
					midY - i.getHeight(null) / 2,
					null
				);
			}
		}
		else {
			Image i = item.getImage();
			if (imageScale.get()) {
				Point p1 = new Point(
					(int)(midX + cacheInfo.cos * midChartSize),
					(int)(midY + cacheInfo.sin * midChartSize)
				);

				double d = Math.PI * -(startAngle + cacheInfo.arcAngle) / 180;
				Point p2 = new Point(
					(int)(midX + Math.cos(d) * midChartSize),
					(int)(midY + Math.sin(d) * midChartSize)
				);
				int imageSize = (int)Math.min(p1.distance(p2), lineLength * 2);
				g.drawImage(
					i,
					(int)(midX + cacheInfo.textCos * lineLength) - imageSize / 2,
					(int)(midY + cacheInfo.textSin * lineLength) - imageSize / 2,
					imageSize,
					imageSize,
					null
				);
			}
			else {
				g.drawImage(
					i,
					(int)(midX + cacheInfo.textCos * (lineLength + 10)) - i.getWidth(null) / 2,
					(int)(midY + cacheInfo.textSin * (lineLength + 10)) - i.getHeight(null) / 2,
					null
				);
			}
		}
		g.setClip(oldClip);
	}
	
	protected void drawOutline(final Graphics2D g) {
		g.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(outlineColor.get());
		int itemCount = model.getRowCount();
		for (int index = 0; index < itemCount; index++) {
			CacheInfo cacheInfo = itemCache[index];
			g.drawLine(
				(int)(midX + cacheInfo.cos),
				(int)(midY + cacheInfo.sin) + 1,
				(int)(midX + cacheInfo.cos * midChartSize),
				(int)(midY + cacheInfo.sin * midChartSize)
			);
		}
		int cs = chartSize.get();

		if ((outlineWidth > 0) && outlineBorder.get())
			g.drawOval(chartX - 1, chartY - 1, cs + 1, cs + 1);
// FIXME: 2.0: visually "join" lines in the center of chart
	}
	
	protected Shape drawPie(final Graphics2D g, final ChartModel.Item item, final CacheInfo cacheInfo, final int startAngle, final Point mousePosition) {
		Shape pie = new Arc2D.Double(
			chartX,
			chartY,
			chartSize.get(), // width
			chartSize.get(), // height
			startAngle,
			cacheInfo.arcAngle,
			Arc2D.PIE
		);
		if ((mousePosition != null) && pie.contains(mousePosition)) {
			activeItem = item;
			g.setColor(MColor.getBrighter(item.getValue()));
		}
		else {
			g.setColor(item.getValue());
		}
		g.fill(pie);
		
		return pie;
	}

	protected void drawShadow(final Graphics2D g) {
		Composite oldComposite = g.getComposite();
		int so = shadowOffset.get();
		g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
		g.setPaint(new RadialGradientPaint(
			midX + so,
			midY + so,
			(float)shadowSize / 2,
			new float[] { 0.0f, 0.90f, 1.0f },
			new Color[] { Color.WHITE, shadowColor.get(), UI.INVISIBLE }
		));
		g.fillOval(
			chartX + so,
			chartY + so,
			shadowSize,
			shadowSize
		);
		g.setComposite(oldComposite);
	}

	protected void drawText(final Graphics2D g) {
		int itemCount = model.getRowCount();
		int l = (chartSize.get() + textDistance.get() * 2) / 2;
		int lastX = Integer.MIN_VALUE;
		int lastY = Integer.MIN_VALUE;
		for (int index = 0; index < itemCount; index++) {
			CacheInfo cacheInfo = itemCache[index];
			ChartModel.Item i = model.getRowAt(index);
			if (!TK.isEmpty(cacheInfo.text)) {
				Point labelStart2 = new Point(
					(int)(midX + cacheInfo.textCos * l),
					(int)(midY + cacheInfo.textSin * l)
				);
				if ((lastX != Integer.MIN_VALUE) && (lastY != Integer.MIN_VALUE)) {
					int labelHeight = textFont.get().getSize() + ((textPadding.get() + textLineSize.get()) * 2);
					if (
						((lastX < midX) && (labelStart2.x < midX)) &&
						(labelStart2.y < (lastY + labelHeight))
					) {
						labelStart2.y = lastY + labelHeight;
					}
					else if (
						((lastX > midX) && (labelStart2.x > midX)) &&
						((labelStart2.y + labelHeight) > lastY)
					) {
						labelStart2.y = lastY - labelHeight;
					}
				}
				drawText(g, cacheInfo.labelStart, labelStart2, i, cacheInfo.text);
				lastX = labelStart2.x;
				lastY = labelStart2.y;
			}
		}
	}
	
	/**
	 * @since 2.2
	 */
	protected void drawText(final Graphics2D g, final Point labelStart, final Point labelStart2, final ChartModel.Item item, final String text) {
		Color labelBackground = textBackground.get();
		Color labelForeground = textForeground.get();
		if (labelForeground == null)
			labelForeground = MColor.deriveColor(labelBackground, 0.3f);
		Color labelLineColor = null;
		if (textLineAutoColor.get())
			labelLineColor = item.getValue();
		if (labelLineColor == null)
			labelLineColor = labelForeground;
		g.setColor(labelLineColor);

		Composite oldComposite = g.getComposite();
		float a = textAlpha.get();
		a = Math.max(a, 0.0f);
		a = Math.min(a, 1.0f);
		g.setComposite(AlphaComposite.SrcOver.derive(a));
		
		boolean drawTextLine = (textLineSize.get() > 0);
		Point pos = new Point();
		pos.x = (labelStart2.x < midX) ? (labelStart2.x - textDistance.get()) : (labelStart2.x + textDistance.get());
		pos.y = labelStart2.y;
		if (drawTextLine) {
			g.setStroke(new BasicStroke(
				textLineSize.get(),
// FIXME: 2.0: large text line size and alpha
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND
			));
			if (textDistance.get() > 0) {
				g.drawPolyline(
					new int[] { labelStart.x, labelStart2.x, labelStart2.x, pos.x },
					new int[] { labelStart.y, labelStart2.y, labelStart2.y, pos.y },
					4
				);
			}
		}

		g.setFont(textFont.get());
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D textBounds = metrics.getStringBounds(text, g);
		
		int labelPadding = textPadding.get();
		int w = (int)textBounds.getWidth() + labelPadding * 2;
		int h = (int)textBounds.getHeight() + labelPadding * 2;
		
		// draw background
		int x = (pos.x < midX) ? (pos.x - w) : pos.x;
		int y = pos.y - h / 2;

		int margin = Math.max(1, textLineSize.get() / 2);
		switch (textType.get()) {
			case RECT:
				if (drawTextLine) {
					g.setStroke(new BasicStroke(
						textLineSize.get(),
						BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_MITER
					));
					g.drawRect(x, y, w, h);
				}
				g.setColor(labelBackground);
				g.fillRect(x + margin, y + margin, w - margin * 2 + 1, h - margin * 2 + 1);
				break;
			case ROUND_RECT:
				int arc = 7;
				if (drawTextLine) {
					g.setStroke(new BasicStroke(
						textLineSize.get(),
						BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_ROUND
					));
					g.drawRoundRect(x, y, w, h, arc, arc);
				}
				g.setColor(labelBackground);
				g.fillRoundRect(x + margin, y + margin, w - margin * 2 + 1, h - margin * 2 + 1, arc, arc);
				break;
			default:
				throw new WTFError(textType.get());
		}

		imageArea.left = Math.min(imageArea.left, x - textLineSize.get());
		imageArea.top = Math.min(imageArea.top, y - textLineSize.get());
		imageArea.right = Math.max(imageArea.right, x + w + textLineSize.get() + 1);
		imageArea.bottom = Math.max(imageArea.bottom, y + h + textLineSize.get() + 1);
		
		// draw text
		g.setColor(labelForeground);
		x += labelPadding;
		y += (h - (int)textBounds.getHeight()) / 2 + metrics.getAscent() - 1;
		g.drawString(text, x, y);
		
		g.setComposite(oldComposite);
	}
	
	protected void drawTopGradient(final Graphics2D g) {
		int cs = chartSize.get();
		if (fxShine.get()) {
			g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
			g.setPaint(new RadialGradientPaint(
				midX,
				midY,
				(float)lightSize / 2,
				new float[] { 0.0f, 0.7f, 1.0f },
				new Color[] { new Color(255, 255, 255, 255), new Color(255, 255, 255, 205), new Color(255, 255, 255, 210) }
			));
			g.fillOval(
				chartX + lightMargin / 2,
				chartY + lightMargin / 2,
				lightSize,
				lightSize
			);
		}

		g.setComposite(AlphaComposite.SrcOver.derive(0.4f));
		g.setClip(new Ellipse2D.Float(chartX, chartY - 1, cs, cs - (outlineWidth * 2)));
		g.setPaint(new LinearGradientPaint(
			chartX + midChartSize, chartY,
			chartX + midChartSize, chartY + cs,
			new float[] { 0.0f, 0.9f },
			new Color[] { Color.WHITE, UI.INVISIBLE }
		));
		g.fillRect(chartX, chartY, cs + (outlineWidth * 2), cs);
	}
	
	protected void setupRenderingHints(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	}
	
	// private
	
	private static BufferedImage cutImage(final BufferedImage image, final Insets i, final int w, final int h) {
		return image.getSubimage(
			Math.max(i.left, 0),
			Math.max(i.top, 0),
			Math.min(i.right - i.left, w),
			Math.min(i.bottom - i.top, h)
		);
	}
	
	private void resetCache() {
		int cs = chartSize.get();
		int itemCount = model.getRowCount();
		int startAngle = 0;
		itemCache = new CacheInfo[itemCount];
		for (int index = 0; index < itemCount; index++) {
			CacheInfo cacheInfo = new CacheInfo();
			itemCache[index] = cacheInfo;
			ChartModel.Item i = model.getRowAt(index);
			cacheInfo.text = model.formatText(textFormat.get(), i, totalNumber);
			cacheInfo.arcAngle = (int)((float)(i.number * 360) / totalNumber);
			// HACK: fix last item
			if (index == itemCount - 1)
				cacheInfo.arcAngle = 360 - startAngle;
			
			double d = Math.PI * -startAngle / 180;
			cacheInfo.cos = Math.cos(d);
			cacheInfo.sin = Math.sin(d);

			d = Math.PI * -(startAngle + ((startAngle + cacheInfo.arcAngle) - startAngle) / 2) / 180;
			cacheInfo.textCos = Math.cos(d);
			cacheInfo.textSin = Math.sin(d);
			int l = (cs + outlineWidth) / 2;
			cacheInfo.labelStart = new Point(
				(int)(midX + cacheInfo.textCos * l),
				(int)(midY + cacheInfo.textSin * l)
			);
			cacheInfo.startAngle = startAngle;
			startAngle += cacheInfo.arcAngle;
		}
	}
	
	// protected classes
	
	protected static final class CacheInfo {
		
		// protected

		protected double cos;
		protected double sin;
		protected double textCos;
		protected double textSin;
		protected int arcAngle;
		protected int startAngle;
		protected Point labelStart;
		
		/**
		 * @since 2.2
		 */
		protected String text;
		
	}
	
}
