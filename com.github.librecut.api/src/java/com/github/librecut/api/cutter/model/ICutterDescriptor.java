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

import java.util.Collection;

import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;

public interface ICutterDescriptor {

	// TODO add comments

	String getName();

	String getDescription();

	Collection<IMedia> getDefaultMediaFormats();

	IBorders getBorders(IMediaSize mediaSize);

	LoadingDirection getLoadingDirection();

	double getDpiX();

	double getDpiY();

	Collection<IParameterDescriptor<?>> getCuttingParameters();
}
