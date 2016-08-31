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
package com.github.librecut.api.design.spi;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.github.librecut.api.design.model.IDesign;

public interface IDesignImporter {

	// TODO add comments

	boolean isSupported(InputStream contents, String fileExtension, IProgressMonitor monitor)
			throws IOException, InterruptedException;

	IImportResult importDesign(InputStream contents, String fileExtension, IProgressMonitor monitor)
			throws IOException, InterruptedException;

	public interface IImportResult {

		IStatus getStatus();

		IDesign getDesign();
	}
}
