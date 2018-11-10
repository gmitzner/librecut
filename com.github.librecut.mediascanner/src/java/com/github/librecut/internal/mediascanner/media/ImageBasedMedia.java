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

package com.github.librecut.internal.mediascanner.media;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;

public class ImageBasedMedia implements IMedia {

	private final IMediaSize mediaSize;
	private final IBorders borders;
	private final int imageWidth;
	private final int imageHeight;
	private final byte[] imageData;
	private final double[] referencePoints;
	private final double[] physicalReferencePointsInInches;

	public ImageBasedMedia(IMediaSize mediaSize, IBorders borders, int imageWidth, int imageHeight, byte[] imageData,
			double[] referencePoints, double[] physicalReferencePointsInInches) {

		this.mediaSize = mediaSize;
		this.borders = borders;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.imageData = imageData;
		this.referencePoints = referencePoints;
		this.physicalReferencePointsInInches = physicalReferencePointsInInches;
	}

	@Override
	public IMediaSize getMediaSize() {
		return mediaSize;
	}

	@Override
	public IBorders getBorders() {
		return borders;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public byte[] getImageData() {
		return imageData;
	}

	// return array with 4 coordinates (2 * double) for the identified
	// reference points within the image
	public double[] getReferencePoints() {
		return referencePoints;
	}

	// return array with 4 coordinates (2 * double) for the real world
	// reference points on the media (in inches)
	public double[] getPhysicalReferencePointsInInches() {
		return physicalReferencePointsInInches;
	}
}
