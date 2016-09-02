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
package com.github.librecut.api.gui;

import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.gui.spi.IMediaRenderer;

/**
 * An instance of this interface provides important rendering parameters for an
 * {@link IMediaRenderer} implementation. The parameters describe the desired
 * detail of the media.
 */
public interface IRenderingParameters {

	/**
	 * Returns the front left corner of the area for this rendering job. The
	 * orientation depends on the loading direction.
	 * 
	 * @return the front left corner of the area measured in inches.
	 */
	IPoint getFrontLeftCorner();

	/**
	 * Returns the loading direction as orientation.
	 * 
	 * @return the loading direction.
	 */
	LoadingDirection getLoadingDirection();

	/**
	 * Returns the dots per inch for this rendering job.
	 * 
	 * @return the dots per inch.
	 */
	double getDpi();
}
