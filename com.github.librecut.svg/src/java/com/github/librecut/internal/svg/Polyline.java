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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.model.IPolyline;

public class Polyline implements IPolyline {

	private final List<IPoint> pointList;
	private final Design design;
	private final int polylineIndex;

	public Polyline(List<IPoint> pointList, int polylineIndex, Design design) {

		this.pointList = pointList;
		this.polylineIndex = polylineIndex;
		this.design = design;
	}

	@Override
	public Iterator<IPoint> iterator() {
		return pointList.iterator();
	}

	@Override
	public List<IPoint> getPointList() {
		return Collections.unmodifiableList(pointList);
	}

	@Override
	public boolean isEnabled() {
		return design.getPolylineEnabledFlags().get(polylineIndex);
	}

	@Override
	public void setEnabled(boolean enabled) {
		design.getPolylineEnabledFlags().set(polylineIndex, enabled);
	}
}
