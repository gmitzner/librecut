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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.common.cutter.model.Borders;
import com.github.librecut.common.cutter.model.DefaultMediaSize;
import com.github.librecut.internal.application.Activator;
import com.github.librecut.internal.layouteditor.LayoutEditor;
import com.github.librecut.internal.layouteditor.LayoutEditorInput;
import com.github.librecut.internal.resource.model.Layout;

public class NewLayoutHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Layout layout = new Layout();
		layout.setName("unnamed layout");
		layout.setMedia(getDefaultMediaFormat());

		LayoutEditorInput editorInput = new LayoutEditorInput(layout);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.openEditor(editorInput, LayoutEditor.ID, true);
		} catch (PartInitException e) {
			StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
		}

		return null;
	}

	private IMedia getDefaultMediaFormat() {

		// TODO replace this dummy implementation

		return new IMedia() {

			@Override
			public IMediaSize getMediaSize() {
				return DefaultMediaSize.A4_Landscape;
			}

			@Override
			public IBorders getBorders() {
				return new Borders(0.196850394d, 0.196850394d, 0.196850394d, 0.196850394d);
			}
		};
	}
}
