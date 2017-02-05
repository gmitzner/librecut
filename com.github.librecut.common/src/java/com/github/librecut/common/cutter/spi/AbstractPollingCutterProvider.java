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
package com.github.librecut.common.cutter.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.spi.ICutterProvider;
import com.github.librecut.api.cutter.spi.ICutterStatusListener;

public abstract class AbstractPollingCutterProvider implements ICutterProvider {

	private final String searchJobDescription;
	private final ListenerList<ICutterStatusListener> listenerList;
	private final AtomicReference<Collection<ICutter>> cuttersRef;

	private int pollingIntervalMillis;
	private Job searchJob;

	public AbstractPollingCutterProvider(String searchJobDescription, int pollingIntervalMillis) {

		this.searchJobDescription = searchJobDescription;
		this.listenerList = new ListenerList<>();
		this.cuttersRef = new AtomicReference<>(Collections.emptyList());
		this.pollingIntervalMillis = pollingIntervalMillis;
	}

	protected abstract Collection<ICutter> detectCutters(IProgressMonitor monitor) throws InterruptedException;

	protected void setPollingInterval(int pollingIntervalMillis) {
		this.pollingIntervalMillis = pollingIntervalMillis;
	}

	@Override
	public void startup() {

		if (searchJob != null) {
			searchJob.cancel();
		}

		searchJob = new Job(searchJobDescription) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					Collection<ICutter> newCutters = detectCutters(monitor);
					Collection<ICutter> oldCutters = cuttersRef.get();

					List<String> lostCutterIdList = new ArrayList<>();
					for (ICutter oldCutter : oldCutters) {
						ICutter newCutter = getCutter(oldCutter.getId(), newCutters);
						if (newCutter == null) {
							lostCutterIdList.add(oldCutter.getId());
						}
					}

					List<ICutter> newCutterList = new ArrayList<>();
					List<ICutter> changedCutterList = new ArrayList<>();
					for (ICutter newCutter : newCutters) {
						ICutter oldCutter = getCutter(newCutter.getId(), oldCutters);
						if (oldCutter == null) {
							newCutterList.add(newCutter);
						} else if (!newCutter.getDeviceState().equals(oldCutter.getDeviceState())) {
							changedCutterList.add(newCutter);
						}
					}

					cuttersRef.set(newCutters);

					for (String cutterId : lostCutterIdList) {
						for (ICutterStatusListener listener : listenerList) {
							notifyLostCutter(cutterId, listener);
						}
					}

					for (ICutter cutter : newCutterList) {
						for (ICutterStatusListener listener : listenerList) {
							notifyNewCutter(cutter, listener);
						}
					}

					for (ICutter cutter : changedCutterList) {
						for (ICutterStatusListener listener : listenerList) {
							notifyStatusChange(cutter, listener);
						}
					}

					return Status.OK_STATUS;
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				} finally {
					schedule(pollingIntervalMillis);
				}
			}

			private ICutter getCutter(String id, Collection<ICutter> cutters) {

				for (ICutter cutter : cutters) {
					if (id.equals(cutter.getId())) {
						return cutter;
					}
				}
				return null;
			}
		};
		searchJob.schedule();
	}

	@Override
	public void shutdown() {
		searchJob.cancel();
	}

	@Override
	public void addStatusListener(ICutterStatusListener listener) {

		listenerList.add(listener);
		Collection<ICutter> cutters = cuttersRef.get();
		for (ICutter cutter : cutters) {
			notifyNewCutter(cutter, listener);
		}
	}

	@Override
	public void removeStatusListener(ICutterStatusListener listener) {
		listenerList.remove(listener);
	}

	private void notifyNewCutter(final ICutter cutter, ICutterStatusListener listener) {

		SafeRunner.run(new ISafeRunnable() {

			@Override
			public void run() throws Exception {
				listener.handleNewCutter(cutter);
			}

			@Override
			public void handleException(Throwable exception) {
				// nothing to do
			}
		});
	}

	private void notifyLostCutter(final String cutterId, ICutterStatusListener listener) {

		SafeRunner.run(new ISafeRunnable() {

			@Override
			public void run() throws Exception {
				listener.handleLostCutter(cutterId);
			}

			@Override
			public void handleException(Throwable exception) {
				// nothing to do
			}
		});
	}

	private void notifyStatusChange(final ICutter cutter, ICutterStatusListener listener) {

		SafeRunner.run(new ISafeRunnable() {

			@Override
			public void run() throws Exception {
				listener.handleStatusChange(cutter);
			}

			@Override
			public void handleException(Throwable exception) {
				// nothing to do
			}
		});
	}
}
