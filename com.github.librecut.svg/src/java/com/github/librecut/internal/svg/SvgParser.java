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
package com.github.librecut.internal.svg;

import java.io.IOException;
import java.io.InputStream;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

public final class SvgParser {

	private SvgParser() {
		super();
	}

	public static SVGDocument loadDocument(InputStream contents) throws IOException {

		String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parserClassName);
		SVGDocument document = factory.createSVGDocument("local:///", contents);
		return document;
	}

	public static GraphicsNode buildGvtTree(SVGDocument document) throws IOException {

		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext context = new BridgeContext(userAgent, loader);
		context.setDynamicState(BridgeContext.STATIC);
		GVTBuilder builder = new GVTBuilder();
		try {
			GraphicsNode rootNode = builder.build(context, document);
			return rootNode;
		} catch (BridgeException e) {
			throw new IOException("SVG document parsing error", e);
		}
	}
}
