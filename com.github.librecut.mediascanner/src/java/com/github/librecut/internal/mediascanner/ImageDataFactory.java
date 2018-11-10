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

package com.github.librecut.internal.mediascanner;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.internal.mediascanner.media.ImageBasedMedia;

public class ImageDataFactory {

	private final MatOfPoint2f sourceMatrix;
	private final double[] physRefPointsInInches;
	private final Mat image;

	private int cachedWidth;
	private int cachedHeight;
	private LoadingDirection cachedDirection;
	private double cachedDpi;
	private byte[] cachedImageData;

	public ImageDataFactory(ImageBasedMedia media) {

		double[] imageRefPoints = media.getReferencePoints();
		this.sourceMatrix = new MatOfPoint2f(new Point(imageRefPoints[0], imageRefPoints[1]),
				new Point(imageRefPoints[2], imageRefPoints[3]), new Point(imageRefPoints[4], imageRefPoints[5]),
				new Point(imageRefPoints[6], imageRefPoints[7]));
		this.physRefPointsInInches = media.getPhysicalReferencePointsInInches();
		this.image = new Mat(media.getImageHeight(), media.getImageWidth(), CvType.CV_8UC3);
		this.image.put(0, 0, media.getImageData());

		this.cachedWidth = -1;
	}

	public byte[] create(int width, int height, LoadingDirection direction, double dpi) {

		if ((width == cachedWidth) && (height == cachedHeight) && direction.equals(cachedDirection)
				&& (dpi == cachedDpi) && (cachedImageData != null)) {
			return cachedImageData;
		}

		Mat resultMatrix = Mat.zeros(width, height, CvType.CV_8UC3);
		Mat destinationMatrix = new MatOfPoint2f(
				new Point(physRefPointsInInches[0] * dpi, physRefPointsInInches[1] * dpi),
				new Point(physRefPointsInInches[2] * dpi, physRefPointsInInches[3] * dpi),
				new Point(physRefPointsInInches[4] * dpi, physRefPointsInInches[5] * dpi),
				new Point(physRefPointsInInches[6] * dpi, physRefPointsInInches[7] * dpi));
		Mat transform = Imgproc.getPerspectiveTransform(sourceMatrix, destinationMatrix);
		Imgproc.warpPerspective(image, resultMatrix, transform, resultMatrix.size());
		int bufferSize = resultMatrix.channels() * resultMatrix.cols() * resultMatrix.rows();
		cachedImageData = new byte[bufferSize];
		resultMatrix.get(0, 0, cachedImageData);

		cachedWidth = width;
		cachedHeight = height;
		cachedDirection = direction;
		cachedDpi = dpi;

		return cachedImageData;
	}
}
