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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.model.IPolyline;
import com.github.librecut.common.design.model.Point;

public class Pattern implements IPattern {

	private final List<Polyline> polylineList;
	private final BitSet enabledPolylineFlags;
	private final double dpiX;
	private final double dpiY;

	public Pattern(List<Polyline> polylineList, BitSet enabledPolylineFlags, double dpiX, double dpiY) {

		this.polylineList = polylineList;
		this.enabledPolylineFlags = enabledPolylineFlags;
		this.dpiX = dpiX;
		this.dpiY = dpiY;
	}

	@Override
	public IPoint getMinXY(boolean enabledOnly) {

		Collection<IPolyline> polylines = getPolylines(enabledOnly);
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		for (IPolyline polyline : polylines) {
			for (IPoint point : polyline.getPointList()) {
				if (point.getX() < minX) {
					minX = point.getX();
				}
				if (point.getY() < minY) {
					minY = point.getY();
				}
			}
		}
		return new Point(minX, minY);
	}

	@Override
	public IPoint getMaxXY(boolean enabledOnly) {

		Collection<IPolyline> polylines = getPolylines(enabledOnly);
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (IPolyline polyline : polylines) {
			for (IPoint point : polyline.getPointList()) {
				if (point.getX() > maxX) {
					maxX = point.getX();
				}
				if (point.getY() > maxY) {
					maxY = point.getY();
				}
			}
		}
		return new Point(maxX, maxY);
	}

	@Override
	public double getDpiX() {
		return dpiX;
	}

	@Override
	public double getDpiY() {
		return dpiY;
	}

	@Override
	public Collection<IPolyline> getPolylines(boolean enabledOnly) {

		if (!enabledOnly) {
			return new ArrayList<IPolyline>(polylineList);
		}

		List<IPolyline> resultList = new ArrayList<IPolyline>(polylineList.size());
		for (int i = 0; i < polylineList.size(); ++i) {
			if (enabledPolylineFlags.get(i)) {
				resultList.add(polylineList.get(i));
			}
		}
		return resultList;
	}
}
