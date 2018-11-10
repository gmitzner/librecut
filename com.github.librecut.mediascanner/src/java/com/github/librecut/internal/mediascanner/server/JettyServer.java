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

import java.io.IOException;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class JettyServer {

	private final Server server;
	private final Class<? extends Servlet> servletClass;

	private String scheme = null;
	private String host = null;
	private int httpPort = -1;

	private JettyServer(Server server, Class<? extends Servlet> servletClass) {

		this.server = server;
		this.servletClass = servletClass;
	}

	public static JettyServer createServer(Class<? extends Servlet> servletClass) {

		Server server = new Server(0);
		return new JettyServer(server, servletClass);
	}

	public void start() throws IOException {

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(servletClass, "/*");

		boolean startUpFailed = false;
		try {
			server.start();

			Connector[] connectors = server.getConnectors();
			for (int i = 0; (httpPort < 0) && (i < connectors.length); ++i) {
				if (connectors[i] instanceof AbstractNetworkConnector) {
					AbstractNetworkConnector connector = (AbstractNetworkConnector) connectors[i];
					if ("HTTP/1.1".equals(connector.getDefaultProtocol())) {
						scheme = "http";
						host = connector.getHost();
						httpPort = connector.getLocalPort();
					}
				}
			}

			if (httpPort <= 0) {
				throw new IOException("No local HTTP port allocated");
			}
		} catch (IOException e) {
			startUpFailed = true;
			throw e;
		} catch (Exception e) {
			startUpFailed = true;
			throw new IOException(e);
		} finally {
			if (startUpFailed) {
				try {
					server.stop();
				} catch (Exception e) {
					// there is already an exceptional state => ignore follow up
					// exception
				}
			}
		}
	}

	public void stop() throws IOException {

		try {
			server.stop();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public String getHost() {
		return host;
	}

	public int getHttpPort() {

		if (httpPort < 0) {
			throw new IllegalStateException("HTTP server hasn't yet opened a HTTP port");
		}
		return httpPort;
	}

	public String getScheme() {
		return scheme;
	}
}
