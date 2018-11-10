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

package com.github.librecut.internal.mediascanner.server;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.common.cutter.model.Borders;
import com.github.librecut.common.cutter.model.MediaSize;
import com.github.librecut.internal.mediascanner.Activator;
import com.github.librecut.internal.mediascanner.Constants;
import com.github.librecut.internal.mediascanner.ImageBasedMediaFactory;
import com.github.librecut.internal.mediascanner.LayoutEditorInput;
import com.github.librecut.internal.mediascanner.media.ImageBasedMedia;
import com.github.librecut.resource.model.Layout;

public class MediaScannerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if ("/mediascanner/description.xml".equals(request.getRequestURI())) {
			response.setContentType("application/xml");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("<?xml version=\"1.0\"?>" + "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">"
					+ "	<specVersion>" + "		<major>1</major>" + "		<minor>0</minor>" + "	</specVersion>"
					+ "	<URLBase>base URL for all relative URLs</URLBase>" + "	<device>"
					+ "		<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>"
					+ "		<friendlyName>short user-friendly title</friendlyName>"
					+ "		<manufacturer>manufacturer name</manufacturer>"
					+ "		<manufacturerURL>URL to manufacturer site</manufacturerURL>"
					+ "		<modelDescription>long user-friendly title</modelDescription>"
					+ "		<modelName>model name</modelName>" + "		<modelNumber>model number</modelNumber>"
					+ "		<modelURL>URL to model site</modelURL>"
					+ "		<serialNumber>manufacturer's serial number</serialNumber>" + "		<UDN>uuid:UUID</UDN>"
					+ "		<UPC>Universal Product Code</UPC>" + "		<iconList>" + "			<icon>"
					+ "				<mimetype>image/format</mimetype>"
					+ "				<width>horizontal pixels</width>"
					+ "				<height>vertical pixels</height>" + "				<depth>color depth</depth>"
					+ "				<url>URL to icon</url>" + "			</icon>" + "		</iconList>"
					+ "		<presentationURL>URL for presentation</presentationURL>" + "	</device>" + "</root>");
		} else if ("/mediascanner".equals(request.getRequestURI())) {
			String mediaScannerId = Activator.getDefault().getMediaScannerId().replace('"', '\'');
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println("{\"description\": \"" + mediaScannerId + "\"}");
		} else {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("<h1>Not found</h1>");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		if ("/media".equals(path)) {
			handleMediaCreation(request, response);
		} else {
			super.doPost(request, response);
		}
	}

	private void handleMediaCreation(HttpServletRequest request, HttpServletResponse response) throws IOException {

		BufferedImage image = ImageIO.read(request.getInputStream());
		if (image == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported image format");
			return;
		}

		ImageBasedMediaFactory backgroundImageFactory = new ImageBasedMediaFactory();
		IMediaSize mediaSize = new MediaSize("Silhoutte Cutting Mat", 12.0, 12.0);
		IBorders borders = new Borders(0.0, 0.0, 0.0, 0.0);
		double[] physicalReferencePoints = getPhysicalReferencePoints();
		ImageBasedMedia media = backgroundImageFactory.createMedia(mediaSize, borders, image, physicalReferencePoints);
		if (media == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image processing has failed");
			return;
		}

		openEditor(media);

		// TODO improve HTTP REST response
		response.setStatus(HttpServletResponse.SC_CREATED);
		// response.setHeader("Location", value);
		// response.getWriter().println("<h1>Hello from
		// MediaScannerServlet</h1>");
	}

	private double[] getPhysicalReferencePoints() {

		final double INCH = 25.4;
		// alle Angaben in mm
		return new double[] { 5.0 / INCH, -14.5 / INCH, 12.0 - 5.0 / INCH, -14.5 / INCH, 12.0 - 5.0 / INCH,
				12.0 + 14.5 / INCH, 5.0 / INCH, 12.0 + 14.5 / INCH };
	}

	private void openEditor(IMedia media) {

		Layout layout = new Layout();
		layout.setName("unnamed layout");
		layout.setMedia(media);

		LayoutEditorInput editorInput = new LayoutEditorInput(layout);

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					page.openEditor(editorInput, Constants.LAYOUT_EDITOR_ID, true);
				} catch (PartInitException e) {
					StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
				}
			}
		});

	}
}
