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
package com.github.librecut.internal.layouteditor;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.gui.spi.IMediaRenderer;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.internal.resource.model.IDesignEntity;

public class LayoutCanvas extends Canvas {

	private static final char KEY_DELETE = '\u007F';

	private static final int SIZE_LOADING_ARROW = 23;
	private static final int SIZE_MEDIA_SHADOW = 3;

	private final Supplier<LayoutModel> layoutSupplier;

	private LoadingDirection defaultLoadingDirection;

	private volatile IDesignEntity selectedEntity;
	private volatile String selectedEntityId;

	private volatile RenderingParameters renderingParameters;

	public LayoutCanvas(Composite parent, int style, final IDesignEntityChangeListener designEntityChangeListener,
			final Supplier<LayoutModel> layoutSupplier, IMediaRenderer mediaRenderer) {

		super(parent, style | SWT.BORDER);

		this.layoutSupplier = layoutSupplier;

		this.selectedEntity = null;
		this.selectedEntityId = null;

		this.addPaintListener(new LayoutCanvasPaintListener(layoutSupplier, () -> selectedEntityId,
				() -> renderingParameters, mediaRenderer));

		this.addMouseListener(new LayoutCanvasMouseListener(this, layoutSupplier, () -> renderingParameters));

		this.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.character == KEY_DELETE) {
					IDesignEntity designEntity = getSelectedDesignEntity();
					if (designEntity != null) {
						designEntityChangeListener.removeEntity(designEntity);
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// nothing to do
			}
		});
	}

	public synchronized void setDefaultLoadingDirection(LoadingDirection loadingDirection) {
		this.defaultLoadingDirection = loadingDirection;
	}

	public synchronized IDesignEntity getSelectedDesignEntity() {
		return selectedEntity;
	}

	synchronized void setSelectedPatternId(String id) {

		LayoutModel layout = layoutSupplier.get();
		List<IDesignEntity> entityList = layout.getDesignEntityList();
		for (IDesignEntity entity : entityList) {
			if (entity.getId().equals(id)) {
				this.selectedEntity = entity;
				this.selectedEntityId = id;
				redraw();
				return;
			}
		}

		this.selectedEntity = null;
		this.selectedEntityId = null;
	}

	private void updateRenderingParameters() {

		LayoutModel layout = layoutSupplier.get();

		Rectangle drawingArea = getClientArea();
		IMedia media = layout.getMedia();
		double mediaWidth = media.getMediaSize().getWidth();
		double mediaHeight = media.getMediaSize().getHeight();
		double dpi = getDpi(drawingArea, mediaWidth, mediaHeight);

		renderingParameters = new RenderingParameters(-SIZE_LOADING_ARROW / dpi, -SIZE_LOADING_ARROW / dpi,
				defaultLoadingDirection, dpi);
	}

	private static double getDpi(Rectangle drawingArea, double width, double height) {

		int w = drawingArea.width - 2 * SIZE_LOADING_ARROW - SIZE_MEDIA_SHADOW;
		int h = drawingArea.height - 2 * SIZE_LOADING_ARROW - SIZE_MEDIA_SHADOW;

		double dpiX = w / width;
		double dpiY = h / height;

		return dpiX < dpiY ? dpiX : dpiY;
	}

	@Override
	public synchronized void setSize(Point size) {

		super.setSize(size);
		updateRenderingParameters();
		redraw();
	}

	@Override
	public synchronized void setSize(int width, int height) {

		super.setSize(width, height);
		updateRenderingParameters();
		redraw();
	}

	@Override
	public void setBounds(Rectangle rect) {

		super.setBounds(rect);
		updateRenderingParameters();
		redraw();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {

		super.setBounds(x, y, width, height);
		updateRenderingParameters();
		redraw();
	}
}
