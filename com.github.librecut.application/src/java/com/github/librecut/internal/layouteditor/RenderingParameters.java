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

import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.gui.IRenderingParameters;
import com.github.librecut.common.design.model.Point;

public class RenderingParameters implements IRenderingParameters {

	private final double offsetX;
	private final double offsetY;
	private final LoadingDirection direction;
	private final double dpi;

	public RenderingParameters(double offsetX, double offsetY, LoadingDirection direction, double dpi) {

		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.direction = direction;
		this.dpi = dpi;
	}

	@Override
	public IPoint getFrontLeftCorner() {
		return new Point(offsetX, offsetY);
	}

	@Override
	public LoadingDirection getLoadingDirection() {
		return direction;
	}

	@Override
	public double getDpi() {
		return dpi;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		long temp;
		temp = Double.doubleToLongBits(dpi);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(offsetX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(offsetY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RenderingParameters other = (RenderingParameters) obj;
		if (direction != other.direction)
			return false;
		if (Double.doubleToLongBits(dpi) != Double.doubleToLongBits(other.dpi))
			return false;
		if (Double.doubleToLongBits(offsetX) != Double.doubleToLongBits(other.offsetX))
			return false;
		if (Double.doubleToLongBits(offsetY) != Double.doubleToLongBits(other.offsetY))
			return false;
		return true;
	}
}
