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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.model.IPolyline;

public class LayoutPattern {

	private final String id;
	private final List<Line> lineList;

	public static LayoutPattern createPattern(String id, IPattern pattern, Point offset) {

		List<Line> lineList = new ArrayList<Line>();
		for (IPolyline polyline : pattern.getPolylines(true)) {
			Iterator<IPoint> iterator = polyline.iterator();
			IPoint p0 = iterator.next();
			while (iterator.hasNext()) {
				IPoint p1 = iterator.next();
				lineList.add(new Line((int) Math.round(p0.getX()) + offset.x, (int) Math.round(p0.getY()) + offset.y,
						(int) Math.round(p1.getX()) + offset.x, (int) Math.round(p1.getY()) + offset.y));
				p0 = p1;
			}
		}

		return new LayoutPattern(id, lineList);
	}

	private LayoutPattern(String id, List<Line> lineList) {

		this.id = id;
		this.lineList = lineList;
	}

	public String getId() {
		return id;
	}

	public Collection<Line> getLines() {
		return lineList;
	}

	public static class Line {

		public final int x0;
		public final int y0;
		public final int x1;
		public final int y1;

		private final int dx;
		private final int dy;
		private final double q;

		public Line(int x0, int y0, int x1, int y1) {

			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.dx = x1 - x0;
			this.dy = y1 - y0;
			this.q = this.dx * this.dx + this.dy * this.dy;
		}

		public boolean contains(int x, int y, double epsilon) {

			if (x0 > x1) {
				if ((x < x1 - (int) epsilon) || (x > x0 + (int) epsilon)) {
					return false;
				}
			} else {
				if ((x < x0 - (int) epsilon) || (x > x1 + (int) epsilon)) {
					return false;
				}
			}

			if (y0 > y1) {
				if ((y < y1 - (int) epsilon) || (y > y0 + (int) epsilon)) {
					return false;
				}
			} else {
				if ((y < y0 - (int) epsilon) || (y > y1 + (int) epsilon)) {
					return false;
				}
			}

			double f = ((x - x0) * dx + (y - y0) * dy) / q;

			double xo = x0 + f * dx;
			double yo = y0 + f * dy;

			return (xo - x) * (xo - x) + (yo - y) * (yo - y) <= epsilon * epsilon;
		}
	}
}
