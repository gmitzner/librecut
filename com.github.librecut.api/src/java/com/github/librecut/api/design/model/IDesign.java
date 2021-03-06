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
package com.github.librecut.api.design.model;

/**
 * An instance of this interface represents a handle to a design.
 */
public interface IDesign {

	/**
	 * Creates a pattern based on this design and the supplied rendering
	 * parameters.
	 * 
	 * @param rotationAngle
	 *            the rotation angle of the design in radian.
	 * @param scaling
	 *            the scale of the design.
	 * @param mirrorPattern
	 *            flag indicating if the design must be mirrored.
	 * @param dpiX
	 *            the DPI for X coordinate.
	 * @param dpiY
	 *            the DPI for Y coordinate.
	 * @return the pattern based on this design.
	 */
	IPattern createPattern(double rotationAngle, double scaling, boolean mirrorPattern, double dpiX, double dpiY);
}
