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

package com.github.librecut.internal.mediascanner.media;

import java.awt.Polygon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.gui.IRenderingParameters;
import com.github.librecut.api.gui.spi.IMediaRenderer;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.internal.mediascanner.ImageDataFactory;

public class ImageBasedMediaRenderer implements IMediaRenderer {

	private ImageDataFactory imageDataFactory;

	@Override
	public boolean isMediaSupported(IMedia media) {
		return media instanceof ImageBasedMedia;
	}

	@Override
	public void renderMedia(IMedia media, GC gc, Rectangle drawingArea, IRenderingParameters renderingParameters) {

		if (imageDataFactory == null) {
			imageDataFactory = new ImageDataFactory((ImageBasedMedia) media);
		}

		Rectangle mediaArea = getMediaArea(drawingArea, media.getMediaSize(), renderingParameters.getFrontLeftCorner(),
				renderingParameters.getDpi());

		double leftBorder = media.getBorders().getLeftBorder();
		double rightBorder = media.getBorders().getRightBorder();
		double topBorder = media.getBorders().getTopBorder();
		double bottomBorder = media.getBorders().getBottomBorder();
		Rectangle borderArea = getBorderArea(mediaArea, leftBorder, rightBorder, topBorder, bottomBorder,
				renderingParameters.getDpi());

		drawBackgroundImage(gc, (ImageBasedMedia) media, mediaArea, renderingParameters.getLoadingDirection(),
				renderingParameters.getDpi());
		drawBorders(gc, borderArea);
		drawMedia(gc, mediaArea);
		drawLoadingDirection(gc, drawingArea, renderingParameters.getLoadingDirection());
	}

	private Rectangle getMediaArea(Rectangle drawingArea, IMediaSize mediaSize, IPoint frontLeftCorner, double dpi) {

		int x = (int) Math.round(-frontLeftCorner.getX() * dpi) + drawingArea.x;
		int y = (int) Math.round(-frontLeftCorner.getY() * dpi) + drawingArea.y;
		int w = (int) Math.round(mediaSize.getWidth() * dpi);
		int h = (int) Math.round(mediaSize.getHeight() * dpi);
		return new Rectangle(x, y, w, h);
	}

	private static Rectangle getBorderArea(Rectangle mediaArea, double leftBorder, double rightBorder, double topBorder,
			double bottomBorder, double dpi) {

		return new Rectangle(mediaArea.x + (int) Math.round(leftBorder * dpi),
				mediaArea.y + (int) Math.round(topBorder * dpi),
				mediaArea.width - (int) Math.round((leftBorder + rightBorder) * dpi),
				mediaArea.height - (int) Math.round((topBorder + bottomBorder) * dpi));
	}

	private void drawBackgroundImage(GC gc, ImageBasedMedia media, Rectangle mediaArea, LoadingDirection direction,
			double dpi) {

		byte[] imageData = imageDataFactory.create(mediaArea.width, mediaArea.height, direction, dpi);

		Image image = new Image(gc.getDevice(), new ImageData(mediaArea.width, mediaArea.height, 24,
				new PaletteData(0xFF, 0xFF00, 0xFF0000), 3, imageData));
		gc.drawImage(image, mediaArea.x, mediaArea.y);
		image.dispose();
	}

	private void drawLoadingDirection(GC gc, Rectangle drawingArea, LoadingDirection loadingDirection) {

		Polygon arrow = createArrow(drawingArea, loadingDirection);
		drawPolygon(gc, arrow);
	}

	private static Polygon createArrow(Rectangle drawingArea, LoadingDirection direction) {

		int x = drawingArea.x;
		int y = drawingArea.y;
		int w = drawingArea.width;
		int h = drawingArea.height;

		switch (direction) {
		case Top:
			return new Polygon(new int[] { x + w / 2, x + w / 2 + 12, x + w / 2 + 5, x + w / 2 + 5, x + w / 2 - 5,
					x + w / 2 - 5, x + w / 2 - 12 },
					new int[] { y + 2, y + 12, y + 12, y + 20, y + 20, y + 12, y + 12 }, 7);
		case Bottom:
			return new Polygon(
					new int[] { x + w / 2, x + w / 2 + 12, x + w / 2 + 5, x + w / 2 + 5, x + w / 2 - 5, x + w / 2 - 5,
							x + w / 2 - 12 },
					new int[] { y + h - 3, y + h - 13, y + h - 13, y + h - 21, y + h - 21, y + h - 13, y + h - 13 }, 7);
		case Left:
			return new Polygon(new int[] { x + 2, x + 12, x + 12, x + 20, x + 20, x + 12, x + 12 },
					new int[] { y + h / 2, y + h / 2 + 12, y + h / 2 + 5, y + h / 2 + 5, y + h / 2 - 5, y + h / 2 - 5,
							y + h / 2 - 12 },
					7);
		case Right:
			return new Polygon(
					new int[] { x + w - 3, x + w - 13, x + w - 13, x + w - 21, x + w - 21, x + w - 13, x + w - 13 },
					new int[] { y + h / 2, y + h / 2 + 12, y + h / 2 + 5, y + h / 2 + 5, y + h / 2 - 5, y + h / 2 - 5,
							y + h / 2 - 12 },
					7);
		default:
			return null;
		}
	}

	private static void drawPolygon(GC gc, Polygon polygon) {

		int[] points = new int[polygon.npoints * 2];
		for (int i = 0; i < polygon.npoints; ++i) {
			points[i * 2] = polygon.xpoints[i];
			points[i * 2 + 1] = polygon.ypoints[i];
		}
		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		gc.setBackground(color);
		gc.fillPolygon(points);
	}

	private void drawMedia(GC gc, Rectangle mediaArea) {

		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		gc.setForeground(color);
		gc.drawRectangle(mediaArea);
	}

	private void drawBorders(GC gc, Rectangle borderArea) {

		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		gc.setForeground(color);
		gc.drawRectangle(borderArea);
	}
}
