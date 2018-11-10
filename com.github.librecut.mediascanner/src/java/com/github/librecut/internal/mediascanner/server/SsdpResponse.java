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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SsdpResponse implements ISsdpResponse {

	private final List<Header> headerList;

	private int statusCode;
	private String message;
	private byte[] content;

	public SsdpResponse() {

		this.headerList = new ArrayList<Header>();
		this.statusCode = -1;
	}

	@Override
	public void setStatusLine(int statusCode, String message) {

		this.statusCode = statusCode;
		this.message = message;
	}

	@Override
	public void addHeader(String key, String value) {
		headerList.add(new Header(key, value));
	}

	@Override
	public Collection<Header> getHeaders() {
		return Collections.unmodifiableCollection(headerList);
	}

	@Override
	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public byte[] getContent() {
		return content;
	}

	@Override
	public byte[] toByteArray() {

		if (statusCode < 0) {
			throw new IllegalStateException("Status code must be set");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("HTTP/1.1 ");
		builder.append(statusCode);
		builder.append(' ');
		builder.append(message);
		builder.append("\r\n");
		for (Header header : headerList) {
			builder.append(header.getKey());
			builder.append(": ");
			builder.append(header.getValue());
			builder.append("\r\n");
		}
		builder.append("\r\n");
		byte[] head = null;
		try {
			head = builder.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// cannot happen
		}
		byte[] buffer = new byte[head.length + (content != null ? content.length : 0)];
		System.arraycopy(head, 0, buffer, 0, head.length);
		if (content != null) {
			System.arraycopy(content, 0, buffer, head.length, content.length);
		}
		return buffer;
	}
}
