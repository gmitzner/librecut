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
package com.github.librecut.api.cutter.model;

/**
 * An instance of this interface provides information about borders of rectangle
 * areas. The unit of all returned values is inch.
 */
public interface IBorders {

	/**
	 * Returns the distance to the top border in inches.
	 * 
	 * @return the distance in inches.
	 */
	double getTopBorder();

	/**
	 * Returns the distance to the bottom border in inches.
	 * 
	 * @return the distance in inches.
	 */
	double getBottomBorder();

	/**
	 * Returns the distance to the left border in inches.
	 * 
	 * @return the distance in inches.
	 */
	double getLeftBorder();

	/**
	 * Returns the distance to the right border in inches.
	 * 
	 * @return the distance in inches.
	 */
	double getRightBorder();
}
