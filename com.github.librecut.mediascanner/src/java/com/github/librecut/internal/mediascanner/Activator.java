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

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.github.librecut.internal.mediascanner.server.JettyServer;
import com.github.librecut.internal.mediascanner.server.MediaScannerServlet;
import com.github.librecut.internal.mediascanner.server.SsdpServer;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "com.github.librecut.mediascanner"; //$NON-NLS-1$

	private static final String PREF_NODE_MEDIA_SCANNER_ID = "scannerId"; //$NON-NLS-1$
	private static final String UNKNOWN_HOST_SCANNER_ID = "LibreCut media scanner"; //$NON-NLS-1$

	private static Activator instance;

	private JettyServer jettyServer;
	private SsdpServer ssdpServer;

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		jettyServer = JettyServer.createServer(MediaScannerServlet.class);
		jettyServer.start();

		String host = jettyServer.getHost();
		int httpPort = jettyServer.getHttpPort();

		System.out.println(String.format("%s:%d", host == null ? "*" : host, httpPort));
		// *:37052

		ssdpServer = SsdpServer.createServer(jettyServer.getScheme(), jettyServer.getHost(), jettyServer.getHttpPort(),
				"/mediascanner/description.xml");
		ssdpServer.start();

		instance = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		instance = null;

		ssdpServer.stop();
		jettyServer.stop();
	}

	public static Activator getDefault() {
		return instance;
	}

	public String getMediaScannerId() {

		String defaultMediaScannerId = getDefaultMediaScannerId();
		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(PLUGIN_ID);
		String mediaScannerId = preferences.get(PREF_NODE_MEDIA_SCANNER_ID, defaultMediaScannerId);
		return mediaScannerId;
	}

	private String getDefaultMediaScannerId() {

		String host = jettyServer.getHost();
		if (host != null) {
			return host;
		}
		return UNKNOWN_HOST_SCANNER_ID;
	}
}
