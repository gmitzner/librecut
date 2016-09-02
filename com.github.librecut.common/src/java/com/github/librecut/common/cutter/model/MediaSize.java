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
package com.github.librecut.common.cutter.model;

import com.github.librecut.api.media.model.IMediaSize;

public class MediaSize implements IMediaSize {

	private final String name;
	private final double width;
	private final double height;

	/**
	 * 
	 * @param name
	 *            the media name
	 * @param width
	 *            the media width in inches
	 * @param height
	 *            the media height in inches
	 */
	public MediaSize(String name, double width, double height) {

		this.name = name;
		this.width = width;
		this.height = height;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}
}
