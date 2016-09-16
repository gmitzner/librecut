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
package com.github.librecut.internal.svg.importer;

import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.svg.SVGDocument;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.spi.IDesignImporter;
import com.github.librecut.internal.svg.Constants;
import com.github.librecut.internal.svg.Design;
import com.github.librecut.internal.svg.IShapeCollector;
import com.github.librecut.internal.svg.ShapeExtractingGraphics2D;
import com.github.librecut.internal.svg.ShapeExtractor;
import com.github.librecut.internal.svg.SvgParser;

public class SvgImporter implements IDesignImporter {

	@Override
	public boolean isSupported(InputStream contents, String fileExtension, IProgressMonitor monitor)
			throws IOException, InterruptedException {

		byte[] svgContents = readAllBytes(contents, monitor);

		GraphicsNode rootNode;
		try {
			handleProgressMonitorCancellation(monitor);

			SVGDocument document = SvgParser.loadDocument(new ByteArrayInputStream(svgContents));

			handleProgressMonitorCancellation(monitor);

			rootNode = SvgParser.buildGvtTree(document);
		} catch (IOException e) {
			return false;
		}

		handleProgressMonitorCancellation(monitor);

		final AtomicBoolean readable = new AtomicBoolean(true);
		ShapeExtractingGraphics2D graphics = new ShapeExtractingGraphics2D(new IShapeCollector() {

			@Override
			public void handleException(Exception exception) {
				readable.set(false);
			}

			@Override
			public void addShape(Shape shape) {
				// nothing to do
			}
		});
		rootNode.primitivePaint(graphics);

		return readable.get();
	}

	@Override
	public IImportResult importDesign(InputStream contents, String fileExtension, IProgressMonitor monitor)
			throws IOException, InterruptedException {

		byte[] svgContents = readAllBytes(contents, monitor);

		GraphicsNode rootNode;
		try {
			handleProgressMonitorCancellation(monitor);

			SVGDocument document = SvgParser.loadDocument(new ByteArrayInputStream(svgContents));

			handleProgressMonitorCancellation(monitor);

			rootNode = SvgParser.buildGvtTree(document);
		} catch (IOException e) {
			return createFailedImportResult(new Status(IStatus.ERROR, Constants.PLUGIN_ID, e.getMessage(), e));
		}

		handleProgressMonitorCancellation(monitor);

		List<Shape> shapeList = new ArrayList<Shape>();
		IStatus status = ShapeExtractor.extractShapeList(rootNode, shapeList);
		if (!status.isOK()) {
			return createFailedImportResult(status);
		}

		Shape[] shapes = shapeList.toArray(new Shape[shapeList.size()]);
		Design design = new Design(svgContents, shapes);
		return createSuccessfulImportResult(design);
	}

	private static byte[] readAllBytes(InputStream contents, IProgressMonitor monitor)
			throws IOException, InterruptedException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int length = contents.read(buffer);
		while (length != -1) {
			os.write(buffer, 0, length);

			handleProgressMonitorCancellation(monitor);

			length = contents.read(buffer);
		}
		return os.toByteArray();
	}

	private static IImportResult createSuccessfulImportResult(final IDesign design) {

		return new IImportResult() {

			@Override
			public IStatus getStatus() {
				return Status.OK_STATUS;
			}

			@Override
			public IDesign getDesign() {
				return design;
			}
		};
	}

	private static IImportResult createFailedImportResult(final IStatus status) {

		return new IImportResult() {

			@Override
			public IStatus getStatus() {
				return status;
			}

			@Override
			public IDesign getDesign() {
				return null;
			}
		};
	}

	private static void handleProgressMonitorCancellation(IProgressMonitor monitor) throws InterruptedException {

		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
	}
}
