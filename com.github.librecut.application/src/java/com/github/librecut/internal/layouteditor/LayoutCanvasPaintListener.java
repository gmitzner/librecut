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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import com.github.librecut.api.gui.spi.IMediaRenderer;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.internal.resource.model.IDesignEntity;

public class LayoutCanvasPaintListener implements PaintListener {

	private final Supplier<LayoutModel> layoutModelSupplier;
	private final Supplier<String> selectionIdSupplier;
	private final Supplier<RenderingParameters> renderingParametersSupplier;
	private final IMediaRenderer mediaRenderer;

	public LayoutCanvasPaintListener(Supplier<LayoutModel> layoutModelSupplier, Supplier<String> selectionIdSupplier,
			Supplier<RenderingParameters> renderingParametersSupplier, IMediaRenderer mediaRenderer) {

		this.layoutModelSupplier = layoutModelSupplier;
		this.selectionIdSupplier = selectionIdSupplier;
		this.renderingParametersSupplier = renderingParametersSupplier;
		this.mediaRenderer = mediaRenderer;
	}

	@Override
	public void paintControl(PaintEvent e) {

		Rectangle drawingArea = ((Canvas) e.widget).getClientArea();

		Color whiteColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		e.gc.setBackground(whiteColor);
		e.gc.fillRectangle(drawingArea);

		LayoutModel layout = layoutModelSupplier.get();
		IMedia media = layout.getMedia();
		RenderingParameters renderingParameters = renderingParametersSupplier.get();
		mediaRenderer.renderMedia(media, e.gc, drawingArea, renderingParameters);

		List<IDesignEntity> designEntityList = layout.getDesignEntityList();
		Set<String> enabledPatternIdSet = getEnabledPatternIdSet(designEntityList);
		Map<String, LayoutPattern> patternMap = layout.getPatternMap(renderingParameters, drawingArea);
		drawPatterns(e.gc, patternMap, selectionIdSupplier.get(), enabledPatternIdSet);
	}

	private Set<String> getEnabledPatternIdSet(List<IDesignEntity> designEntityList) {

		Set<String> enabledPatternIdSet = new HashSet<>(designEntityList.size());
		for (IDesignEntity entity : designEntityList) {
			if (entity.isEnabled()) {
				enabledPatternIdSet.add(entity.getId());
			}
		}
		return enabledPatternIdSet;
	}

	private void drawPatterns(GC gc, Map<String, LayoutPattern> patternMap, String selectedPatternId,
			Set<String> enabledPatternIdSet) {

		Color enabledColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		Color disabledColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		Color activeColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);

		for (Entry<String, LayoutPattern> entry : patternMap.entrySet()) {
			LayoutPattern pattern = entry.getValue();
			gc.setForeground(entry.getKey() == selectedPatternId ? activeColor
					: (enabledPatternIdSet.contains(pattern.getId()) ? enabledColor : disabledColor));
			for (LayoutPattern.Line line : pattern.getLines()) {
				gc.drawLine(line.x0, line.y0, line.x1, line.y1);
			}
		}
	}
}
