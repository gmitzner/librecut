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
package com.github.librecut.internal.cutter.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.model.IPolyline;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.common.design.model.Point;
import com.github.librecut.internal.application.Activator;
import com.github.librecut.internal.resource.model.IDesignEntity;
import com.github.librecut.internal.resource.model.ILayout;

public class CutWizard extends Wizard {

	private static final int STATUS_CODE_PATTERN_VALIDATION_FAILED = -1;

	private SelectCutterWizardPage selectCutterPage;
	private CuttingParameterWizardPage cuttingParameterPage;
	private final ICutter[] selectedCutters;

	public CutWizard() {

		super();
		this.selectedCutters = new ICutter[1];
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {

		selectCutterPage = new SelectCutterWizardPage(selectedCutters);
		addPage(selectCutterPage);

		cuttingParameterPage = new CuttingParameterWizardPage(selectedCutters);
		addPage(cuttingParameterPage);
	}

	@Override
	public boolean performFinish() {

		final ICutter cutter = getSelectedCutter();
		ILayout layout = getCurrentLayout();
		if (layout == null) {
			return false;
		}

		final Map<String, Object> parameterMap = cuttingParameterPage.getParameterMap();

		final IPattern pattern = createPattern(layout, cutter.getDescriptor());

		if (!validatePattern(pattern, layout, cutter.getDescriptor())) {
			// TODO improve error reporting (show details of validation error)
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, STATUS_CODE_PATTERN_VALIDATION_FAILED,
					Messages.CutWizard_PatternValidationError,
					null);
			StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
			return false;
		}

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					SubMonitor subMonitor = SubMonitor.convert(monitor, "Cutting", 100);
					try {
						cutter.cut(pattern, parameterMap, subMonitor.newChild(100));
					} finally {
						monitor.done();
					}
				}
			});
			return true;
		} catch (InvocationTargetException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}

	private ICutter getSelectedCutter() {
		return selectedCutters[0];
	}

	private ILayout getCurrentLayout() {

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return null;
		}
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput == null) {
			return null;
		}
		ILayout layout = editorInput.getAdapter(ILayout.class);
		return layout;
	}

	private IPattern createPattern(ILayout layout, ICutterDescriptor cutterDescriptor) {

		final double dpiX = cutterDescriptor.getDpiX();
		final double dpiY = cutterDescriptor.getDpiY();

		// TODO take care of loading direction and cutter specific coordinate
		// system

		final Collection<IPolyline> polylineList = createCutterPolylines(layout, dpiX, dpiY);

		// TODO check borders and loading direction

		return new IPattern() {

			@Override
			public IPoint getMinXY(boolean enabledOnly) {
				// TODO
				throw new UnsupportedOperationException();
			}

			@Override
			public IPoint getMaxXY(boolean enabledOnly) {
				// TODO
				throw new UnsupportedOperationException();
			}

			@Override
			public double getDpiX() {
				return dpiX;
			}

			@Override
			public double getDpiY() {
				return dpiY;
			}

			@Override
			public Collection<IPolyline> getPolylines(boolean enabledOnly) {
				return polylineList;
			}
		};
	}

	private static Collection<IPolyline> createCutterPolylines(ILayout layout, double dpiX, double dpiY) {

		boolean mirrored = layout.isMirrored();
		List<IPolyline> transformedPolylineList = new ArrayList<IPolyline>();
		for (IDesignEntity entity : layout.getDesignEntityList()) {
			IDesign design = entity.getDesign();
			IPattern pattern = design.createPattern(entity.getRotationAngle(), entity.getScale(), mirrored, dpiX, dpiY);
			IPoint positionInches = entity.getPosition();

			Point offset = new Point((int) (positionInches.getX() * dpiX), (int) (positionInches.getY() * dpiY));
			Collection<IPolyline> polylines = pattern.getPolylines(true);
			for (IPolyline polyline : polylines) {
				// TODO apply offset and coordinate transformation
				transformedPolylineList.add(transformPolyline(polyline, offset));
			}
		}
		return transformedPolylineList;
	}

	private static IPolyline transformPolyline(IPolyline polyline, IPoint offset) {

		final List<IPoint> transformedPointList = new ArrayList<IPoint>(polyline.getPointList().size());
		for (IPoint point : polyline.getPointList()) {
			transformedPointList.add(new Point(point.getX() + offset.getX(), point.getY() + offset.getY()));
		}
		return new IPolyline() {

			@Override
			public List<IPoint> getPointList() {
				return transformedPointList;
			}

			@Override
			public Iterator<IPoint> iterator() {
				return transformedPointList.iterator();
			}

			@Override
			public void setEnabled(boolean enabled) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}

	private boolean validatePattern(IPattern pattern, ILayout layout, ICutterDescriptor descriptor) {

		Collection<IPolyline> polylines = pattern.getPolylines(true);

		IMedia mediaFormat = layout.getMedia();
		IMediaSize mediaSize = mediaFormat.getMediaSize();
		double maxX = mediaSize.getWidth() * descriptor.getDpiX();
		double maxY = mediaSize.getHeight() * descriptor.getDpiY();

		for (IPolyline polyline : polylines) {
			for (IPoint point : polyline.getPointList()) {
				if ((point.getX() < 0) || (point.getX() >= maxX) || (point.getY() < 0) || (point.getY() >= maxY)) {
					return false;
				}
			}
		}
		return true;
	}
}
