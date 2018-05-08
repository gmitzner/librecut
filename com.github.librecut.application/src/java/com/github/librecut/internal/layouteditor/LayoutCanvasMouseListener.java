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
package com.github.librecut.internal.layouteditor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;

import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.common.design.model.Point;
import com.github.librecut.resource.model.IDesignEntity;

public class LayoutCanvasMouseListener implements MouseListener, MouseMoveListener {

	private final int LEFT_BUTTON = 1;

	private final double EPSILON = 2.5d;

	private final LayoutCanvas canvas;
	private final Supplier<LayoutModel> layoutModelSupplier;
	private final Supplier<RenderingParameters> viewingParametersSupplier;

	private int x0;
	private int y0;

	public LayoutCanvasMouseListener(LayoutCanvas canvas, Supplier<LayoutModel> layoutModelSupplier,
			Supplier<RenderingParameters> viewingParametersSupplier) {

		this.canvas = canvas;
		this.layoutModelSupplier = layoutModelSupplier;
		this.viewingParametersSupplier = viewingParametersSupplier;
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// nothing to do
	}

	@Override
	public void mouseDown(MouseEvent e) {

		if (e.button == LEFT_BUTTON) {
			Rectangle clientArea = canvas.getClientArea();
			x0 = e.x - clientArea.x;
			y0 = e.y - clientArea.y;
			LayoutModel layout = layoutModelSupplier.get();
			RenderingParameters viewingParameters = viewingParametersSupplier.get();
			Map<String, LayoutPattern> patternMap = layout.getPatternMap(viewingParameters, clientArea);
			String id = getSelectedPatternId(patternMap, x0, y0);
			canvas.setSelectedPatternId(id);

			canvas.addMouseMoveListener(this);
		}
	}

	private String getSelectedPatternId(Map<String, LayoutPattern> patternMap, int x, int y) {

		for (Entry<String, LayoutPattern> entry : patternMap.entrySet()) {
			LayoutPattern pattern = entry.getValue();
			for (LayoutPattern.Line line : pattern.getLines()) {
				if (line.contains(x, y, EPSILON)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	@Override
	public void mouseUp(MouseEvent e) {

		if (e.button == LEFT_BUTTON) {
			Rectangle clientArea = canvas.getClientArea();
			canvas.removeMouseMoveListener(this);
			processMousePosition(e.x - clientArea.x, e.y - clientArea.y, (e.stateMask & SWT.MODIFIER_MASK) == 0,
					(e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1, (e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD3);
		}
	}

	@Override
	public void mouseMove(MouseEvent e) {

		Rectangle clientArea = canvas.getClientArea();
		processMousePosition(e.x - clientArea.x, e.y - clientArea.y, (e.stateMask & SWT.MODIFIER_MASK) == 0,
				(e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1, (e.stateMask & SWT.MODIFIER_MASK) == SWT.MOD3);
	}

	private void processMousePosition(int x, int y, boolean move, boolean rotate, boolean resize) {

		IDesignEntity designEntity = canvas.getSelectedDesignEntity();
		if (designEntity != null) {
			if (move) {
				IPoint position = designEntity.getPosition();
				RenderingParameters viewingParameters = viewingParametersSupplier.get();
				double dpi = viewingParameters.getDpi();
				IPoint newPosition = new Point(position.getX() + (x - x0) / dpi, position.getY() + (y - y0) / dpi);
				designEntity.setPosition(newPosition);
			} else if (rotate) {
				IPoint position = designEntity.getPosition();
				RenderingParameters viewingParameters = viewingParametersSupplier.get();
				double dpi = viewingParameters.getDpi();
				double ax = x0 / dpi - position.getX();
				double ay = y0 / dpi - position.getY();
				double bx = x / dpi - position.getX();
				double by = y / dpi - position.getY();
				double divisor = Math.sqrt(ax * ax + ay * ay) * Math.sqrt(bx * bx + by * by);
				double alpha = 0.0;
				if (divisor > 0.0) {
					double cosAlpha = (ax * bx + ay * by) / divisor;
					if (cosAlpha < 1.0) {
						alpha = Math.signum(ax * by - ay * bx) * Math.acos(cosAlpha);
					}
				}
				double newRotationAngle = designEntity.getRotationAngle() + alpha;
				designEntity.setRotationAngle(newRotationAngle);
			} else if (resize) {
				IPoint position = designEntity.getPosition();
				RenderingParameters viewingParameters = viewingParametersSupplier.get();
				double dpi = viewingParameters.getDpi();
				double ax = x0 / dpi - position.getX();
				double ay = y0 / dpi - position.getY();
				double bx = x / dpi - position.getX();
				double by = y / dpi - position.getY();
				double factor = Math.sqrt(bx * bx + by * by) / Math.sqrt(ax * ax + ay * ay);
				double newScale = designEntity.getScale() * factor;
				designEntity.setScale(newScale);
			}
			layoutModelSupplier.get().notifyDesignEntitiesChanged();
			canvas.redraw();
		}

		x0 = x;
		y0 = y;
	}
}
