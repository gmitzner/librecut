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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.common.design.model.Point;

public class Design implements IDesign {

	private static final double SVG_DPI = 90.0d;
	private static final double FLATNESS = 0.5d;

	private final byte[] svgContents;
	private final Shape[] shapes;
	private final BitSet polylineEnabledFlags;

	public Design(byte[] svgContents, Shape[] shapes) {

		this.svgContents = svgContents;
		this.shapes = shapes;

		final int[] counters = new int[1];
		IPointListHandler handler = new IPointListHandler() {

			@Override
			public void handlePointList(List<IPoint> pointList, int shapeIndex) {
				++counters[0];
			}
		};
		renderShapes(shapes, new AffineTransform(), handler);
		this.polylineEnabledFlags = new BitSet(counters[0]);
		this.polylineEnabledFlags.set(0, counters[0], true);
	}

	public Design(byte[] svgContents, Shape[] shapes, BitSet polylineEnabledFlags) {

		this.svgContents = svgContents;
		this.shapes = shapes;
		this.polylineEnabledFlags = polylineEnabledFlags;
	}

	public byte[] getSvgContents() {
		return svgContents;
	}

	public BitSet getPolylineEnabledFlags() {
		return polylineEnabledFlags;
	}

	@Override
	public IPattern createPattern(double rotationAngle, double scaling, boolean mirrorPattern, double dpiX,
			double dpiY) {

		List<Shape> enabledShapeList = getEnabledShapeList();

		AffineTransform transformation = getTransformation(enabledShapeList, rotationAngle, scaling, mirrorPattern,
				dpiX, dpiY);

		final List<Polyline> polylineList = new ArrayList<Polyline>(shapes.length);
		IPointListHandler handler = new IPointListHandler() {

			@Override
			public void handlePointList(List<IPoint> pointList, int shapeIndex) {

				polylineList.add(new Polyline(pointList, polylineList.size(), Design.this));
			}
		};
		renderShapes(shapes, transformation, handler);

		return new Pattern(polylineList, polylineEnabledFlags, dpiX, dpiY);
	}

	private List<Shape> getEnabledShapeList() {

		final BitSet shapeEnabledFlags = new BitSet(polylineEnabledFlags.size());
		IPointListHandler handler = new IPointListHandler() {

			@Override
			public void handlePointList(List<IPoint> pointList, int shapeIndex) {
				shapeEnabledFlags.set(shapeIndex);
			}
		};
		renderShapes(shapes, new AffineTransform(), handler);

		List<Shape> shapeList = new ArrayList<Shape>(shapes.length);
		for (int i = 0; i < shapes.length; ++i) {
			if (shapeEnabledFlags.get(i)) {
				shapeList.add(shapes[i]);
			}
		}
		return shapeList;
	}

	private static AffineTransform getTransformation(List<Shape> shapeList, double rotationAngle, double scaling,
			boolean mirrorPattern, double dpiX, double dpiY) {

		AffineTransform transformation = AffineTransform.getScaleInstance(scaling * dpiX / SVG_DPI,
				scaling * dpiY / SVG_DPI);
		transformation.rotate(rotationAngle);
		if (mirrorPattern) {
			transformation.scale(-1.0d, 1.0d);
		}
		Rectangle2D border = getBorder(shapeList);
		transformation.translate(-border.getCenterX(), -border.getCenterY());
		return transformation;
	}

	private static Rectangle2D getBorder(Collection<Shape> shapes) {

		Rectangle2D border = null;
		for (Shape shape : shapes) {
			if (border == null) {
				border = shape.getBounds2D();
			} else {
				border.add(shape.getBounds2D());
			}
		}
		return border;
	}

	private static void renderShapes(Shape[] shapes, AffineTransform transformation, IPointListHandler handler) {

		double[] coords = new double[2];
		for (int shapeIndex = 0; shapeIndex < shapes.length; ++shapeIndex) {
			double x0 = 0.0d;
			double y0 = 0.0d;
			List<IPoint> pointList = null;
			PathIterator iterator = shapes[shapeIndex].getPathIterator(transformation, FLATNESS);
			while (!iterator.isDone()) {
				int operation = iterator.currentSegment(coords);
				if (operation == PathIterator.SEG_CLOSE) {
					pointList.add(new Point(x0, y0));
				} else if (operation == PathIterator.SEG_MOVETO) {
					handlePointList(handler, pointList, shapeIndex);

					x0 = coords[0];
					y0 = coords[1];
					pointList = new ArrayList<IPoint>();
					pointList.add(new Point(x0, y0));
				} else {
					pointList.add(new Point(coords[0], coords[1]));
				}
				iterator.next();
			}
			handlePointList(handler, pointList, shapeIndex);
		}
	}

	private static void handlePointList(IPointListHandler handler, List<IPoint> pointList, int shapeIndex) {

		if ((pointList != null) && (pointList.size() >= 2)) {
			handler.handlePointList(pointList, shapeIndex);
		}
	}

	private interface IPointListHandler {

		void handlePointList(List<IPoint> pointList, int shapeIndex);
	}
}
