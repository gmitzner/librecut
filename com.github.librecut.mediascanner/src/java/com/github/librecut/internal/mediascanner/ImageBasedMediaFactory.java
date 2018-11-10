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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.internal.mediascanner.media.ImageBasedMedia;

public class ImageBasedMediaFactory {

	private static final int MAX_IMAGE_SIZE = 1280;
	private static final double CONTOUR_APPROX_FACTOR_SQUARES = 0.05;
	private static final double MAX_COSINE_APPROX_90DEGREE = 0.3;

	public ImageBasedMedia createMedia(IMediaSize mediaSize, IBorders borders, BufferedImage bufferedImage,
			double[] physicalReferencePointsInInches) {

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		Mat image = new Mat(height, width, CvType.CV_8UC3);
		int[] rgbData = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int value = rgbData[y * width + x];
				image.put(y, x, new byte[] { (byte) (value & 0xff), (byte) ((value >> 8) & 0xff),
						(byte) ((value >> 16) & 0xff) });
			}
		}

		image = createPreprocessedImage(image);

		Point[] cornerPoints = findCornerPoints(image, 0.05, 0.96, 0.05);
		if ((cornerPoints != null) && (cornerPoints[0] != null) && (cornerPoints[1] != null)
				&& (cornerPoints[2] != null) && (cornerPoints[3] != null)) {
			int bufferSize = image.channels() * image.cols() * image.rows();
			byte[] imageData = new byte[bufferSize];
			image.get(0, 0, imageData);
			return new ImageBasedMedia(mediaSize, borders, image.cols(), image.rows(), imageData,
					new double[] { cornerPoints[0].x, cornerPoints[0].y, cornerPoints[1].x, cornerPoints[1].y,
							cornerPoints[2].x, cornerPoints[2].y, cornerPoints[3].x, cornerPoints[3].y },
					physicalReferencePointsInInches);
		}
		return null;
	}

	private static Mat createPreprocessedImage(Mat image) {

		Mat result;
		if ((image.width() > MAX_IMAGE_SIZE) || (image.height() > MAX_IMAGE_SIZE)) {
			int width;
			int height;
			if (image.width() >= image.height()) {
				width = MAX_IMAGE_SIZE;
				height = (int) (image.height() * ((double) MAX_IMAGE_SIZE / image.width()));
				if (height % 2 != 0) {
					++height;
				}
			} else {
				height = MAX_IMAGE_SIZE;
				width = (int) (image.width() * ((double) MAX_IMAGE_SIZE / image.height()));
				if (width % 2 != 0) {
					++width;
				}
			}

			result = new Mat(height, width, image.type());
			Imgproc.resize(image, result, new Size(width, height));
		} else {
			result = new Mat(image.height(), image.width(), image.type());
			image.copyTo(result);
		}

		return result;
	}

	private static Point[] findCornerPoints(Mat image, double minThreshold, double maxThreshold, double thresholdStep) {

		List<Point[]> candidateList = new ArrayList<Point[]>();
		for (double threshold = minThreshold; threshold <= maxThreshold; threshold += thresholdStep) {
			Point[] points = findCornerPoints(image, threshold);
			if (points != null) {
				candidateList.add(points);
			}
		}

		if (candidateList.isEmpty()) {
			return null;
		}
		if (candidateList.size() == 1) {
			return candidateList.get(0);
		}

		Point[] result = new Point[4];
		for (int i = 0; i < result.length; ++i) {
			Collection<Set<Point>> cornerGroups = findCornerGroups(candidateList, i);

			List<Set<Point>> biggestCornerGroupList = new ArrayList<Set<Point>>(cornerGroups.size());
			for (Set<Point> cornerGroup : cornerGroups) {
				if (biggestCornerGroupList.isEmpty()) {
					biggestCornerGroupList.add(cornerGroup);
				} else if (cornerGroup.size() >= biggestCornerGroupList.get(0).size()) {
					if (cornerGroup.size() > biggestCornerGroupList.get(0).size()) {
						biggestCornerGroupList.clear();
					}
					biggestCornerGroupList.add(cornerGroup);
				}
			}

			if (!biggestCornerGroupList.isEmpty()) {
				// TODO what if there is more than one biggestCornerGroupList?!?
				Set<Point> cornerGroup = biggestCornerGroupList.get(0);
				Point avgPoint = new Point(0.0, 0.0);
				for (Point point : cornerGroup) {
					avgPoint.x += point.x;
					avgPoint.y += point.y;
				}
				avgPoint.x /= cornerGroup.size();
				avgPoint.y /= cornerGroup.size();

				double minDistance = Double.MAX_VALUE;
				for (Point point : cornerGroup) {
					double distance = getDistance(point, avgPoint);
					if (distance < minDistance) {
						minDistance = distance;
						result[i] = point;
					}
				}
			}
		}
		return result;
	}

	private static Collection<Set<Point>> findCornerGroups(List<Point[]> candidateList, int cornerIndex) {

		Collection<Set<Point>> cornerGroups = new ArrayList<Set<Point>>();
		for (int j = 0; j < candidateList.size(); ++j) {
			Point candidate = candidateList.get(j)[cornerIndex];
			if (candidate != null) {
				boolean groupFound = false;
				for (Set<Point> cornerGroup : cornerGroups) {
					Iterator<Point> iterator = cornerGroup.iterator();
					boolean found = false;
					while (!found && iterator.hasNext()) {
						Point contender = iterator.next();
						double distance = getDistance(candidate, contender);
						double quality = Math.exp(-distance * distance / 36.0);
						if (quality >= 0.1) {
							found = true;
						}
					}
					if (found) {
						groupFound = true;
						cornerGroup.add(candidate);
					}
				}

				if (!groupFound) {
					Set<Point> cornerGroup = new HashSet<Point>();
					cornerGroup.add(candidate);
					cornerGroups.add(cornerGroup);
				}
			}
		}

		return cornerGroups;
	}

	private static Point[] findCornerPoints(Mat image, double threshold) {

		Mat contourMat = createGrayScaleImage(image);
		applyThreshold(contourMat, threshold);

		List<List<Point>> contourList = findContours(contourMat);

		List<Point> squareList = findSquares(contourList, 50.0, 1000.0);
		if ((squareList.size() < 2) && (squareList.size() > 4)) {
			return null;
		}

		List<Point> arrow = findArrowContour(contourList, 50.0, 1000.0, squareList, 20.0);
		if (arrow == null) {
			return null;
		}

		Rect boundingRect = Imgproc.boundingRect(new MatOfPoint(arrow.toArray(new Point[arrow.size()])));
		Point[] cornerPoints = getCornerPoints(squareList,
				new Point(boundingRect.x + boundingRect.width / 2, boundingRect.y + boundingRect.height / 2));
		return cornerPoints;
	}

	private static Mat createGrayScaleImage(Mat image) {

		Mat result = new Mat();
		Imgproc.cvtColor(image, result, Imgproc.COLOR_BGR2GRAY);
		return result;
	}

	private static void applyThreshold(Mat image, double relativeThreshold) {

		Imgproc.threshold(image, image, 255.0 * relativeThreshold, 255.0, Imgproc.THRESH_BINARY);
	}

	private static List<List<Point>> findContours(Mat image) {

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		List<List<Point>> resultList = new ArrayList<List<Point>>(contours.size());
		for (MatOfPoint contour : contours) {
			MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
			MatOfPoint2f approxContour = new MatOfPoint2f();
			double contourLength = Imgproc.arcLength(contour2f, true);
			Imgproc.approxPolyDP(contour2f, approxContour, contourLength * CONTOUR_APPROX_FACTOR_SQUARES, true);

			resultList.add(approxContour.toList());
		}
		return resultList;
	}

	private static List<Point> findSquares(List<List<Point>> contourList, double minAreaSize, double maxAreaSize) {

		List<List<Point>> candidateList = new ArrayList<List<Point>>();
		for (List<Point> contour : contourList) {
			if (contour.size() == 4) {
				MatOfPoint mat = new MatOfPoint(contour.toArray(new Point[contour.size()]));
				if (Imgproc.isContourConvex(mat)) {
					double areaSize = Math.abs(Imgproc.contourArea(mat));
					if ((areaSize >= minAreaSize) && (areaSize <= maxAreaSize)) {
						double maxCosine = 0.0;
						for (int i = 2; i < 5; ++i) {
							double cosine = Math
									.abs(getAngle(contour.get(i % 4), contour.get(i - 2), contour.get(i - 1)));
							maxCosine = Math.max(maxCosine, cosine);
						}
						if (maxCosine < MAX_COSINE_APPROX_90DEGREE) {
							candidateList.add(contour);
						}
					}
				}
			}
		}

		List<Point> squareList = new ArrayList<Point>(candidateList.size());
		for (List<Point> candidate : candidateList) {
			Rect boundingRect = Imgproc.boundingRect(new MatOfPoint(candidate.toArray(new Point[candidate.size()])));
			squareList
					.add(new Point(boundingRect.x + boundingRect.width / 2, boundingRect.y + boundingRect.height / 2));
		}
		return squareList;
	}

	private static List<Point> findArrowContour(List<List<Point>> contourList, double minAreaSize, double maxAreaSize,
			List<Point> squareList, double maxDistance) {

		List<Point> arrowCandidateList = new ArrayList<Point>();
		for (int i = 0; i < squareList.size() - 1; ++i) {
			Point p1 = squareList.get(i);
			for (int j = i + 1; j < squareList.size(); ++j) {
				Point p2 = squareList.get(j);
				arrowCandidateList.add(new Point(p1.x + (p2.x - p1.x) / 2, p1.y + (p2.y - p1.y) / 2));
			}
		}

		List<Point> result = null;
		double minDistance = Double.MAX_VALUE;
		for (List<Point> contour : contourList) {
			if (contour.size() >= 4) {
				MatOfPoint mat = new MatOfPoint(contour.toArray(new Point[contour.size()]));
				if (Imgproc.isContourConvex(mat)) {
					double areaSize = Math.abs(Imgproc.contourArea(mat));
					if ((areaSize >= minAreaSize) && (areaSize <= maxAreaSize)) {
						Rect boundingRect = Imgproc
								.boundingRect(new MatOfPoint(contour.toArray(new Point[contour.size()])));
						Point point = new Point(boundingRect.x + boundingRect.width / 2,
								boundingRect.y + boundingRect.height / 2);

						for (Point candidate : arrowCandidateList) {
							double distance = getDistance(point, candidate);
							if ((distance < maxDistance) && (distance < minDistance)) {
								minDistance = distance;
								result = contour;
							}
						}
					}
				}
			}
		}
		return result;
	}

	private static Point[] getCornerPoints(List<Point> squareList, Point arrowPoint) {

		Point bestP1 = null;
		Point bestP2 = null;
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < squareList.size() - 1; ++i) {
			Point p1 = squareList.get(i);
			for (int j = i + 1; j < squareList.size(); ++j) {
				Point p2 = squareList.get(j);
				double distance = getDistance(arrowPoint,
						new Point(p1.x + (p2.x - p1.x) / 2, p1.y + (p2.y - p1.y) / 2));
				if (distance < minDistance) {
					minDistance = distance;
					bestP1 = p1;
					bestP2 = p2;
				}
			}
		}

		Point[] result = new Point[4];
		double dx = bestP2.x - bestP1.x;
		double dy = bestP2.y - bestP1.y;
		if (dy * (arrowPoint.x - bestP1.x) - dx * (arrowPoint.y - bestP1.y) < 0) {
			result[0] = bestP1;
			result[1] = bestP2;
		} else {
			result[0] = bestP2;
			result[1] = bestP1;
		}
		double defaultDistance = getDistance(bestP1, bestP2);

		Point p3 = new Point(result[1].x + (result[0].y - result[1].y), result[1].y - (result[0].x - result[1].x));
		minDistance = Double.MAX_VALUE;
		for (int i = 0; i < squareList.size(); ++i) {
			Point p = squareList.get(i);
			double distance = getDistance(p, p3);
			if ((distance < minDistance)
					&& (Math.abs(getAngle(result[0], p, result[1])) < MAX_COSINE_APPROX_90DEGREE)) {
				double p2Distance = getDistance(p, result[1]);
				if ((p2Distance >= 0.6 * defaultDistance) && (p2Distance <= 1.4 * defaultDistance)) {
					minDistance = distance;
					result[2] = p;
				}
			}
		}

		Point p4 = new Point(result[0].x - (result[1].y - result[0].y), result[0].y + (result[1].x - result[0].x));
		minDistance = Double.MAX_VALUE;
		for (int i = 0; i < squareList.size(); ++i) {
			Point p = squareList.get(i);
			double distance = getDistance(p, p4);
			if ((distance < minDistance)
					&& (Math.abs(getAngle(result[1], p, result[0])) < MAX_COSINE_APPROX_90DEGREE)) {
				double p1Distance = getDistance(p, result[0]);
				if ((p1Distance >= 0.6 * defaultDistance) && (p1Distance <= 1.4 * defaultDistance)) {
					minDistance = distance;
					result[3] = p;
				}
			}
		}

		return result;
	}

	private static double getDistance(Point p1, Point p2) {

		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static double getAngle(Point p1, Point p2, Point pt0) {

		double dx1 = p1.x - pt0.x;
		double dy1 = p1.y - pt0.y;
		double dx2 = p2.x - pt0.x;
		double dy2 = p2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}
}
