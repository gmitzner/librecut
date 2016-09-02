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

import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.internal.resource.model.IDesignEntity;

public interface IDesignEntityChangeListener {

	void changePosition(IDesignEntity entity, IPoint position);

	void changeRotationAngle(IDesignEntity entity, double angle);

	void changeScale(IDesignEntity entity, double scale);

	void removeEntity(IDesignEntity entity);
}
