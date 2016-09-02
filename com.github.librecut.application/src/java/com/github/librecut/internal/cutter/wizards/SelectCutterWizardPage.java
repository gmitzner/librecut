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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.model.ICutter.DeviceState;
import com.github.librecut.internal.cutter.CutterCore;

public class SelectCutterWizardPage extends WizardPage {

	protected static final long CUTTER_UPDATE_SLEEPING_TIME_MILLIS = 5000;

	private final ICutter[] selectedCutters;

	private final ICutter[] cutters;

	private TableViewer tableViewer;
	private ISelectionChangedListener listener;

	private final Map<ICutter, DeviceState> stateMap;

	private Job cutterUpdateJob;

	public SelectCutterWizardPage(ICutter[] selectedCutters) {

		super("CutterSelection", "Cutter selection", null);
		this.selectedCutters = selectedCutters;

		Collection<ICutter> cutters = CutterCore.getCutters();
		this.cutters = cutters.toArray(new ICutter[cutters.size()]);

		this.stateMap = new ConcurrentHashMap<ICutter, DeviceState>(cutters.size());

		setDescription("Please select the desired cutter.\nImportant: The cutter status must be 'Ready'.");
	}

	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		tableViewer = new TableViewer(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.getTable().setHeaderVisible(true);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true);
		gridData.widthHint = 420;
		gridData.heightHint = 250;
		tableViewer.getTable().setLayoutData(gridData);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn columnName = new TableViewerColumn(tableViewer, SWT.LEFT);
		columnName.getColumn().setText("Cutter");
		columnName.getColumn().setWidth(300);
		columnName.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object obj) {

				if (obj instanceof ICutter) {
					return ((ICutter) obj).getDescriptor().getName();
				}
				return super.getText(obj);
			}
		});

		TableViewerColumn columnState = new TableViewerColumn(tableViewer, SWT.LEFT);
		columnState.getColumn().setText("Status");
		columnState.getColumn().setWidth(100);
		columnState.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object obj) {

				if (obj instanceof ICutter) {
					DeviceState state = stateMap.get(obj);
					if (state == null) {
						return "Unknown";
					}
					return state.name();
				}
				return super.getText(obj);
			}
		});

		listener = new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				selectedCutters[0] = null;
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					if (obj instanceof ICutter) {
						DeviceState state = stateMap.get(obj);
						if (state == DeviceState.Ready) {
							selectedCutters[0] = (ICutter) obj;
						}
					}
				}
				setPageComplete(selectedCutters[0] != null);
			}
		};
		tableViewer.addSelectionChangedListener(listener);

		tableViewer.setInput(cutters);

		setControl(container);

		setPageComplete(false);

		cutterUpdateJob = new Job("Updating cutter status...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					for (ICutter cutter : cutters) {
						DeviceState state = cutter.getDeviceState(monitor);
						stateMap.put(cutter, state);
					}
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							tableViewer.refresh();
						}
					});
					return Status.OK_STATUS;
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				} finally {
					schedule(CUTTER_UPDATE_SLEEPING_TIME_MILLIS);
				}
			}
		};
		cutterUpdateJob.schedule();
	}

	@Override
	public void dispose() {

		cutterUpdateJob.cancel();
		tableViewer.removeSelectionChangedListener(listener);

		super.dispose();
	}
}
