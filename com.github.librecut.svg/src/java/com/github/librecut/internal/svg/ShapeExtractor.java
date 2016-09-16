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
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

public final class ShapeExtractor {

	private static final int MAX_SHAPE_BUFFER_SIZE = 7;

	private ShapeExtractor() {
		super();
	}

	public static IStatus extractShapeList(GraphicsNode rootNode, List<Shape> shapeList) {

		final MultiStatus multiStatus = new MultiStatus(Constants.PLUGIN_ID, 0,
				"Shape extraction failed due to multiple errors", null);
		final List<Shape> shapeCandidateList = new ArrayList<Shape>();
		ShapeExtractingGraphics2D graphics = new ShapeExtractingGraphics2D(new IShapeCollector() {

			@Override
			public void handleException(Exception exception) {

				multiStatus.add(new Status(IStatus.ERROR, Constants.PLUGIN_ID, exception.getMessage(), exception));
			}

			@Override
			public void addShape(Shape shape) {
				shapeCandidateList.add(shape);
			}
		});
		rootNode.primitivePaint(graphics);

		if (!multiStatus.isOK()) {
			IStatus[] stati = multiStatus.getChildren();
			if (stati.length == 1) {
				return stati[0];
			}
			return multiStatus;
		}

		shapeList.addAll(removeDuplicates(shapeCandidateList));
		return Status.OK_STATUS;
	}

	private static List<Shape> removeDuplicates(List<Shape> shapeList) {

		List<Shape> resultList = new ArrayList<Shape>(shapeList.size());
		for (Shape shape : shapeList) {
			if (!isContained(shape, resultList)) {
				resultList.add(shape);
			}
		}
		return resultList;
	}

	private static boolean isContained(Shape shape, List<Shape> shapeList) {

		for (Shape otherShape : shapeList) {
			if (isEqual(shape, otherShape)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEqual(Shape shape1, Shape shape2) {

		if (!shape1.getClass().equals(shape2.getClass())) {
			return false;
		}

		double[] buffer1 = new double[MAX_SHAPE_BUFFER_SIZE];
		double[] buffer2 = new double[MAX_SHAPE_BUFFER_SIZE];
		PathIterator iterator1 = shape1.getPathIterator(null);
		PathIterator iterator2 = shape2.getPathIterator(null);
		while (!iterator1.isDone()) {
			if (iterator2.isDone()) {
				return false;
			}

			resetBuffer(buffer1);
			resetBuffer(buffer2);
			if (iterator1.currentSegment(buffer1) != iterator2.currentSegment(buffer2)) {
				return false;
			}
			if (!Arrays.equals(buffer1, buffer2)) {
				return false;
			}

			iterator1.next();
			iterator2.next();
		}
		return iterator2.isDone();
	}

	private static void resetBuffer(double[] buffer) {

		for (int i = 0; i < buffer.length; ++i) {
			buffer[i] = 0.0d;
		}
	}
}
