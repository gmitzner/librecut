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
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.librecut.internal.mediascanner.server.ISsdpMessage.Header;

public class SsdpServer {

	private final String scheme;
	private final String host;
	private final int port;
	private final String pathDescriptionXml;

	private Thread workerThread;
	private volatile boolean stopWorker;

	private SsdpServer(String scheme, String host, int port, String pathDescriptionXml) {

		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.pathDescriptionXml = pathDescriptionXml;
	}

	public static SsdpServer createServer(String scheme, String host, int port, String pathDescriptionXml) {
		return new SsdpServer(scheme, host, port, pathDescriptionXml);
	}

	public void start() throws IOException {

		InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");
		final int port = 1900;
		final MulticastSocket socket = new MulticastSocket(port);
		socket.setReuseAddress(true);
		socket.setSoTimeout(1000);
		socket.joinGroup(multicastAddress);

		workerThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					byte[] buffer = new byte[8192];
					while (!stopWorker) {
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						try {
							socket.receive(packet);

							DatagramSocket responseSocket = new DatagramSocket();
							responseSocket.setReuseAddress(true);
							responseSocket.setSoTimeout(1000);
							responseSocket.connect(packet.getAddress(), packet.getPort());

							try {
								ISsdpRequest request = parseRequest(packet.getAddress(), packet.getPort(),
										packet.getData(), packet.getOffset(), packet.getLength());
								handleRequest(request, responseSocket);
							} finally {
								responseSocket.close();
							}
						} catch (SocketTimeoutException e) {
							// ignore this one
						} catch (IOException e) {
							System.err.println(e.getMessage());
							stopWorker = true;
						}
					}
				} finally {
					socket.close();
				}
			}
		});
		workerThread.setDaemon(true);
		workerThread.start();
	}

	public void stop() throws IOException {

		stopWorker = true;
		workerThread.interrupt();
	}

	private void handleRequest(ISsdpRequest request, DatagramSocket responseSocket) {

		System.out.println(request.getRequestLine());
		for (Header header : request.getHeaders()) {
			System.out.println(String.format("%s: %s", header.getKey(), header.getValue()));
		}
		System.out.println(String.format("content length = %d", request.getContent().length));

		String requestLine = request.getRequestLine();
		if (!"M-SEARCH * HTTP/1.1".equals(requestLine)) {
			return;
		}
		if (!"239.255.255.250:1900".equals(findHeader(request, "HOST"))) {
			return;
		}
		if (!"\"ssdp:discover\"".equals(findHeader(request, "MAN"))) {
			return;
		}

		String mxHeader = findHeader(request, "MX");
		if (mxHeader == null) {
			return;
		}
		try {
			int maxResponseDelaySeconds = Integer.parseInt(mxHeader);
			if (maxResponseDelaySeconds < 0) {
				return;
			}
		} catch (NumberFormatException e) {
			return;
		}

		String searchTarget = findHeader(request, "ST");
		if (!"ssdp:all".equals(searchTarget) && !"upnp:rootdevice".equals(searchTarget)) {
			return;
		}

		// TODO
		// - honor MX header (variable response times)
		// - replace SERVER dummy value
		// - is USN value valid?

		SsdpResponse response = new SsdpResponse();
		response.setStatusLine(200, "OK");
		response.addHeader("CACHE-CONTROL", "max-age=60");
		response.addHeader("DATE", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
		response.addHeader("EXT", "");
		String location = String.format("%s://%s:%d%s", scheme,
				host == null ? responseSocket.getLocalAddress().getHostAddress() : host, port, pathDescriptionXml);
		response.addHeader("LOCATION", location);
		response.addHeader("SERVER", "SomeOs/1.0 UPnP/1.0 LibreCutMediaScanner/1.0");
		response.addHeader("ST", "upnp:rootdevice");
		response.addHeader("USN",
				String.format("uuid:LibreCut Mediascanner %s-%d::upnp:rootdevice",
						responseSocket.getLocalAddress().getHostAddress().replace('.', '-').replace(':', '-'),
						responseSocket.getLocalPort()));

		byte[] buffer = response.toByteArray();
		try {
			responseSocket.send(new DatagramPacket(buffer, 0, buffer.length, responseSocket.getInetAddress(),
					responseSocket.getPort()));
		} catch (IOException e) {
			// nothing to do
		}
	}

	private String findHeader(ISsdpRequest request, String key) {

		for (Header header : request.getHeaders()) {
			if (header.getKey().equals(key)) {
				return header.getValue();
			}
		}
		return null;
	}

	private ISsdpRequest parseRequest(final InetAddress sourceAddress, final int sourcePort, byte[] buffer, int offset,
			int length) {

		List<String> lineList = new ArrayList<String>();
		int lineLength = getLineLength(buffer, offset, length);
		while (lineLength > 0) {
			try {
				lineList.add(new String(buffer, offset, lineLength, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
			offset += lineLength + 2;
			length -= lineLength + 2;
			lineLength = getLineLength(buffer, offset, length);
		}
		if (lineLength < 0) {
			return null;
		}
		offset += 2;
		length -= 2;

		if (lineList.isEmpty() || (length < 0)) {
			return null;
		}

		final String requestLine = lineList.get(0);
		String[] requestLineTokens = requestLine.split(" ");
		if (requestLineTokens.length != 3) {
			return null;
		}
		if (!"HTTP/1.1".equals(requestLineTokens[2])) {
			return null;
		}

		final List<Header> headerList = new ArrayList<Header>(lineList.size() - 1);
		for (int i = 1; i < lineList.size(); ++i) {
			String headerLine = lineList.get(i);
			int j = headerLine.indexOf(':');
			if (j <= 0) {
				return null;
			}
			headerList.add(new Header(headerLine.substring(0, j).trim(), headerLine.substring(j + 1).trim()));
		}

		final byte[] content = new byte[length];
		System.arraycopy(buffer, offset, content, 0, length);

		return new ISsdpRequest() {

			@Override
			public String getRequestLine() {
				return requestLine;
			}

			@Override
			public Collection<Header> getHeaders() {
				return headerList;
			}

			@Override
			public byte[] getContent() {
				return content;
			}

			@Override
			public InetAddress getSourceAddress() {
				return sourceAddress;
			}

			@Override
			public int getSourcePort() {
				return sourcePort;
			}
		};
	}

	private int getLineLength(byte[] buffer, int offset, int length) {

		for (int i = offset; i < offset + length - 1; ++i) {
			if ((buffer[i] == '\r') && (buffer[i + 1] == '\n')) {
				return i - offset;
			}
		}
		return -1;
	}
}
