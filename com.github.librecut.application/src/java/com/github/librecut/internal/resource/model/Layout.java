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

import java.util.ArrayList;
import java.util.List;

import com.github.librecut.api.media.model.IMedia;

public class Layout implements ILayout {

	private String name;
	private IMedia media;
	private final List<IDesignEntity> designEntityList;
	private boolean mirrored;

	public Layout() {
		this.designEntityList = new ArrayList<IDesignEntity>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IMedia getMedia() {
		return this.media;
	}

	@Override
	public void setMedia(IMedia format) {
		this.media = format;
	}

	@Override
	public List<IDesignEntity> getDesignEntityList() {
		return designEntityList;
	}

	@Override
	public boolean isMirrored() {
		return mirrored;
	}

	@Override
	public void setMirrored(boolean enable) {
		mirrored = enable;
	}
}
