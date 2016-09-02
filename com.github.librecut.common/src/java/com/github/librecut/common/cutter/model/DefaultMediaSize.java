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

public final class DefaultMediaSize {

	public static final IMediaSize A3_Landscape = new MediaSize("A3 Landscape", 16.5354330709d, 11.6929133858d);
	public static final IMediaSize A3_Portrait = new MediaSize("A3 Portrait", 11.6929133858d, 16.5354330709d);
	public static final IMediaSize A4_Landscape = new MediaSize("A4 Landscape", 11.6929133858d, 8.2677165354d);
	public static final IMediaSize A4_Portrait = new MediaSize("A4 Portrait", 8.2677165354d, 11.6929133858d);

	private DefaultMediaSize() {
		super();
	}
}
