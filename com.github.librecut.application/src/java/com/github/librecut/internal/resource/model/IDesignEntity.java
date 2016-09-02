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
package com.github.librecut.internal.resource.model;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPoint;

public interface IDesignEntity {

	/**
	 * Returns a unique entity id.
	 * 
	 * @return a unique entity id.
	 */
	String getId();

	/**
	 * Returns the design handle.
	 * 
	 * @return the design handle.
	 */
	IDesign getDesign();

	/**
	 * Returns the distance of the design entity in inches relative to the
	 * reference corner of the media. The reference corner of the media is that
	 * one which gets first loaded by the cutter during media loading and which
	 * is on the left side of the media loading direction. Y-coordinate is
	 * counted in media loading direction.
	 * 
	 * @return the distance of the design entity in inches relative to the
	 *         reference corner.
	 */
	IPoint getPosition();

	void setPosition(IPoint position);

	/**
	 * Returns the rotation angle measured in radians. The angle is counter
	 * clock wise.
	 * 
	 * @return the rotation angle measured in radians.
	 */
	double getRotationAngle();

	void setRotationAngle(double rotationAngle);

	double getScale();

	void setScale(double scale);

	boolean isEnabled();

	void setEnabled(boolean enabled);
}
