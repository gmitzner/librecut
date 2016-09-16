/**
 * Copyright (C) 2016 Gerhard Mitzner.
 * 
 * This file is part of LibreCut.
 * 
 * LibreCut is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * LibreCut is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with LibreCut. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.librecut.internal.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;

import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;

public class ShapeExtractingGraphics2D extends AbstractGraphics2D {

	private final IShapeCollector shapeCollector;
	private final Graphics2D fontMetricGraphics;

	public ShapeExtractingGraphics2D(IShapeCollector shapeCollector) {
		super(true);

		this.shapeCollector = shapeCollector;
		this.gc = new GraphicContext();
		this.fontMetricGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	}

	public ShapeExtractingGraphics2D(ShapeExtractingGraphics2D graphics) {
		super(graphics);

		this.shapeCollector = graphics.shapeCollector;
		this.fontMetricGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	}

	@Override
	public Graphics create() {
		return new ShapeExtractingGraphics2D(this);
	}

	@Override
	public void dispose() {
		fontMetricGraphics.dispose();
	}

	@Override
	public void draw(Shape shape) {

		Shape transformedShape = gc.getTransform().createTransformedShape(shape);
		shapeCollector.addShape(transformedShape);
	}

	@Override
	public void drawRenderableImage(RenderableImage image, AffineTransform transformation) {
		shapeCollector.handleException(new UnsupportedOperationException("drawRenderableImage"));
	}

	@Override
	public void drawRenderedImage(RenderedImage image, AffineTransform transformation) {
		shapeCollector.handleException(new UnsupportedOperationException("drawRenderedImage"));
	}

	@Override
	public void drawString(String text, float x, float y) {

		TextLayout layout = new TextLayout(text, getFont(), getFontRenderContext());
		layout.draw(this, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {

		TextLayout layout = new TextLayout(iterator, getFontRenderContext());
		layout.draw(this, x, y);
	}

	@Override
	public void fill(Shape shape) {

		Shape transformedShape = gc.getTransform().createTransformedShape(shape);
		shapeCollector.addShape(transformedShape);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		shapeCollector.handleException(new UnsupportedOperationException("copyArea"));
	}

	@Override
	public boolean drawImage(Image image, int x, int y, ImageObserver observer) {

		shapeCollector.handleException(new UnsupportedOperationException("drawImage"));
		return false;
	}

	@Override
	public boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver observer) {

		shapeCollector.handleException(new UnsupportedOperationException("drawImage"));
		return false;
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		return fontMetricGraphics.getFontMetrics(font);
	}

	@Override
	public void setXORMode(Color color) {
		// nothing to do
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return null;
	}
}
