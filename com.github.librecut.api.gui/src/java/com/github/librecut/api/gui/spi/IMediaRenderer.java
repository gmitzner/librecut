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
package com.github.librecut.api.gui.spi;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import com.github.librecut.api.gui.IRenderingParameters;
import com.github.librecut.api.media.model.IMedia;

/**
 * An interface for a service which renders a media visualization within a
 * client window.
 */
public interface IMediaRenderer {

	/**
	 * Checks if the provided media instance will be supported for rendering.
	 * 
	 * @param media
	 *            the media instance.
	 * @return {@code true} if the media instance will be supported,
	 *         {@code false} otherwise.
	 */
	boolean isMediaSupported(IMedia media);

	/**
	 * Renders the visible part of the provided media instance within the
	 * drawing area.
	 * 
	 * @param media
	 *            the media instance.
	 * @param gc
	 *            the SWT graphics context for the drawing operations.
	 * @param drawingArea
	 *            the drawing area.
	 * @param renderingParameters
	 *            important rendering parameters describing the desired detail.
	 */
	void renderMedia(IMedia media, GC gc, Rectangle drawingArea, IRenderingParameters renderingParameters);
}
