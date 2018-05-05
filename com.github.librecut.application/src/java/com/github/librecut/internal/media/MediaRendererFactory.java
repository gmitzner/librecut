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

package com.github.librecut.internal.media;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;

import com.github.librecut.api.gui.spi.IMediaRenderer;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.internal.layouteditor.DefaultMediaRenderer;

public class MediaRendererFactory {

	public IMediaRenderer create(IMedia media) {

		MediaRendererRegistry registry = new MediaRendererRegistry();
		Collection<IMediaRendererDescriptor> descriptors = registry.readMediaRendererDescriptors();
		for (IMediaRendererDescriptor descriptor : descriptors) {
			try {
				IMediaRenderer mediaRenderer = descriptor.createMediaRenderer();
				if (mediaRenderer.isMediaSupported(media)) {
					return mediaRenderer;
				}
			} catch (CoreException e) {
				// TODO error handling
			}
		}

		IMediaRenderer mediaRenderer = new DefaultMediaRenderer();
		if (mediaRenderer.isMediaSupported(media)) {
			return mediaRenderer;
		}

		return null;
	}

	public boolean isDefaultRenderer(IMediaRenderer mediaRenderer) {
		return mediaRenderer instanceof DefaultMediaRenderer;
	}
}
