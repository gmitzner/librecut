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
package com.github.librecut.internal.application.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;

import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.spi.IDesignConsumer;
import com.github.librecut.api.design.spi.IDesignImporter;
import com.github.librecut.api.design.spi.IDesignImporter.IImportResult;
import com.github.librecut.internal.application.Activator;
import com.github.librecut.internal.importer.DesignImporterRegistry;
import com.github.librecut.internal.importer.IDesignImporterDescriptor;

public class OpenDesignHandler extends AbstractHandler {

	private static final String PREFERENCE_LAST_DESIGN_LOCATION = "lastDesignLocation";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		DesignImporterRegistry registry = new DesignImporterRegistry();
		Collection<IDesignImporterDescriptor> descriptors = registry.readDesignImporterDescriptors();

		String[] fileExtensions = getSupportedFileExtensions(descriptors);

		FileDialog dialog = new FileDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), SWT.OPEN);
		dialog.setFilterExtensions(fileExtensions);
		String designLocation = loadLastDesignLocation();
		dialog.setFilterPath(designLocation);
		String filePath = dialog.open();
		if (filePath != null) {
			storeLastDesignLocation(filePath);

			IDesign design = readDesignFile(filePath, descriptors);

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = page.getActiveEditor();

			if (editor instanceof IDesignConsumer) {
				((IDesignConsumer) editor).consume(design);
			}
		}
		return null;
	}

	private static String loadLastDesignLocation() {

		String designLocation = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(PREFERENCE_LAST_DESIGN_LOCATION,
				System.getProperty("user.home"));
		return designLocation;
	}

	private static void storeLastDesignLocation(String filePath) {

		InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).put(PREFERENCE_LAST_DESIGN_LOCATION,
				new File(filePath).getParent());
		try {
			InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			// nothing to do
		}
	}

	private IDesign readDesignFile(String filePath, Collection<IDesignImporterDescriptor> descriptors)
			throws ExecutionException {

		BufferedInputStream contents = null;
		try {
			String fileExtension = getFileExtension(filePath);
			Collection<IDesignImporter> candidates = getImporterCandidates(fileExtension, descriptors);
			for (IDesignImporter importer : candidates) {
				contents = new BufferedInputStream(new FileInputStream(filePath));
				try {
					if (importer.isSupported(contents, fileExtension, new NullProgressMonitor())) {
						contents.close();
						contents = new BufferedInputStream(new FileInputStream(filePath));
						IImportResult result = importer.importDesign(contents, fileExtension,
								new NullProgressMonitor());
						if (!result.getStatus().isOK()) {
							throw new ExecutionException(result.getStatus().getMessage(),
									result.getStatus().getException());
						}
						return result.getDesign();
					}
				} finally {
					contents.close();
				}
			}

			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File format is not supported.");
			throw new CoreException(status);
		} catch (IOException e) {
			throw new ExecutionException("Cannot import design file.", e);
		} catch (CoreException e) {
			throw new ExecutionException("Cannot import design file.", e);
		} catch (InterruptedException e) {
			// cannot happen
			return null;
		}
	}

	private static String[] getSupportedFileExtensions(Collection<IDesignImporterDescriptor> descriptors) {

		Set<String> extensionSet = new HashSet<String>(descriptors.size());
		for (IDesignImporterDescriptor descriptor : descriptors) {
			for (String extension : descriptor.getExtensions()) {
				extensionSet.add("*." + extension);
			}
		}

		List<String> extensionList = new ArrayList<String>(extensionSet.size());
		extensionList.addAll(extensionSet);
		Collections.sort(extensionList);
		extensionList.add("*");
		return extensionList.toArray(new String[extensionList.size()]);
	}

	private static String getFileExtension(String path) {

		File file = new File(path);
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		String fileExtension = fileName.substring(i + 1);
		return fileExtension;
	}

	private static Collection<IDesignImporter> getImporterCandidates(String fileExtension,
			Collection<IDesignImporterDescriptor> descriptors) throws CoreException {

		List<IDesignImporter> candidateList = new ArrayList<IDesignImporter>(descriptors.size());
		for (IDesignImporterDescriptor descriptor : descriptors) {
			if (isFileExtensionSupported(fileExtension, descriptor)) {
				candidateList.add(descriptor.create());
			}
		}
		return candidateList;
	}

	private static boolean isFileExtensionSupported(String fileExtension, IDesignImporterDescriptor descriptor) {

		for (String extension : descriptor.getExtensions()) {
			if (fileExtension.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
}
