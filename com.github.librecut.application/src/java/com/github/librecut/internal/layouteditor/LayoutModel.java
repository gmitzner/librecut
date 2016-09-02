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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.internal.resource.model.IDesignEntity;
import com.github.librecut.internal.resource.model.ILayout;

public class LayoutModel implements ILayout {

	private final ILayout wrappedLayout;

	private Map<String, LayoutPattern> cachedPatternMap;
	private RenderingParameters cachedRenderingParameters;
	private Point cachedTopLeftCornerDrawableArea;

	public LayoutModel(ILayout layout) {
		this.wrappedLayout = layout;
	}

	public Map<String, LayoutPattern> getPatternMap(RenderingParameters renderingParameters, Rectangle drawableArea) {

		synchronized (this) {
			if ((cachedPatternMap != null) && renderingParameters.equals(cachedRenderingParameters)
					&& (cachedTopLeftCornerDrawableArea.x == drawableArea.x)
					&& (cachedTopLeftCornerDrawableArea.y == drawableArea.y)) {
				return cachedPatternMap;
			}
		}

		List<IDesignEntity> designEntityList = wrappedLayout.getDesignEntityList();
		boolean mirrorDesigns = wrappedLayout.isMirrored();
		IPoint corner = renderingParameters.getFrontLeftCorner();
		Map<String, LayoutPattern> patternMap = createPatternMap(designEntityList, mirrorDesigns, corner.getX(),
				corner.getY(), renderingParameters.getDpi(), drawableArea);

		synchronized (this) {
			cachedPatternMap = patternMap;
			cachedRenderingParameters = renderingParameters;
			cachedTopLeftCornerDrawableArea = new Point(drawableArea.x, drawableArea.y);

			return patternMap;
		}
	}

	private static Map<String, LayoutPattern> createPatternMap(List<IDesignEntity> designEntityList,
			boolean mirrorDesigns, double offsetX, double offsetY, double dpi, Rectangle drawableArea) {

		Map<String, LayoutPattern> patternMap = new HashMap<>(designEntityList.size());
		for (IDesignEntity entity : designEntityList) {
			IDesign design = entity.getDesign();
			IPattern pattern = design.createPattern(entity.getRotationAngle(), entity.getScale(), mirrorDesigns, dpi,
					dpi);
			IPoint positionInches = entity.getPosition();
			Point offset = new Point((int) ((positionInches.getX() - offsetX) * dpi) + drawableArea.x,
					(int) ((positionInches.getY() - offsetY) * dpi) + drawableArea.y);
			patternMap.put(entity.getId(), LayoutPattern.createPattern(entity.getId(), pattern, offset));
		}
		return patternMap;
	}

	@Override
	public synchronized String getName() {
		return wrappedLayout.getName();
	}

	@Override
	public synchronized void setName(String name) {
		wrappedLayout.setName(name);
	}

	@Override
	public synchronized IMedia getMedia() {
		return wrappedLayout.getMedia();
	}

	@Override
	public synchronized void setMedia(IMedia media) {

		cachedPatternMap = null;
		wrappedLayout.setMedia(media);
	}

	@Override
	public List<IDesignEntity> getDesignEntityList() {
		return Collections.synchronizedList(wrappedLayout.getDesignEntityList());
	}

	public synchronized void notifyDesignEntitiesChanged() {
		cachedPatternMap = null;
	}

	@Override
	public synchronized boolean isMirrored() {
		return wrappedLayout.isMirrored();
	}

	@Override
	public synchronized void setMirrored(boolean enable) {

		cachedPatternMap = null;
		wrappedLayout.setMirrored(enable);
	}
}
