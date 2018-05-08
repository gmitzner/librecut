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
package com.github.librecut.resource.model;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.common.design.model.Point;

public class DesignEntity implements IDesignEntity {

	private final String id;
	private final IDesign design;

	private IPoint position;
	private double rotationAngle;
	private double scale;
	private boolean enabled;

	public DesignEntity(String id, IDesign design) {

		this.id = id;
		this.design = design;

		this.position = new Point(0.0d, 0.0d);
		this.rotationAngle = 0.0d;
		this.scale = 1.0d;
		this.enabled = true;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IDesign getDesign() {
		return design;
	}

	@Override
	public IPoint getPosition() {
		return position;
	}

	@Override
	public void setPosition(IPoint position) {
		this.position = position;
	}

	@Override
	public double getRotationAngle() {
		return rotationAngle;
	}

	@Override
	public void setRotationAngle(double rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	@Override
	public double getScale() {
		return scale;
	}

	@Override
	public void setScale(double scale) {
		this.scale = scale;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
