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
package com.github.librecut.internal.cutter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.cutter.spi.ICutterProvider;
import com.github.librecut.api.cutter.spi.ICutterStatusListener;

public final class CutterCore implements ICutterStatusListener {

	private static CutterCore instance;

	private final AtomicReference<List<ICutter>> cuttersRef;
	private final List<ICutterProvider> providerList;

	private CutterCore() {

		this.cuttersRef = new AtomicReference<>();
		this.providerList = new ArrayList<>();
	}

	public static void startup() {

		if (instance != null) {
			return;
		}

		instance = new CutterCore();

		CutterProviderRegistry registry = new CutterProviderRegistry();
		Collection<ICutterProviderDescriptor> descriptors = registry.readCutterProviderDescriptors();
		for (ICutterProviderDescriptor descriptor : descriptors) {
			try {
				ICutterProvider provider = descriptor.createCutterProvider();
				if (provider != null) {
					SafeRunner.run(new ISafeRunnable() {

						@Override
						public void run() throws Exception {

							provider.addStatusListener(instance);
							provider.startup();
							instance.providerList.add(provider);
						}

						@Override
						public void handleException(Throwable e) {
							handleProviderStartupException(e);
						}
					});
				} else {
					handleProviderRegistryConfigurationError();
				}
			} catch (CoreException e) {
				handleProviderRegistryException(e);
			}
		}
	}

	public static void shutdown() {

		if (instance == null) {
			return;
		}

		for (ICutterProvider provider : instance.providerList) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {

					provider.removeStatusListener(instance);
					provider.shutdown();
				}

				@Override
				public void handleException(Throwable e) {
					handleProviderShutdownException(e);
				}
			});
		}

		instance.providerList.clear();

		instance = null;
	}

	private static void handleProviderRegistryConfigurationError() {
		// TODO implement logging
	}

	private static void handleProviderRegistryException(CoreException e) {
		// TODO implement logging
	}

	private static void handleProviderStartupException(Throwable e) {
		// TODO implement logging
	}

	private static void handleProviderShutdownException(Throwable e) {
		// TODO implement logging
	}

	public static Collection<ICutter> getCutters() {

		CutterCore instance = CutterCore.instance;
		if (instance == null) {
			return Collections.emptyList();
		}

		Collection<ICutter> cutters = instance.cuttersRef.get();
		return cutters;
	}

	public static Collection<ICutterDescriptor> getSupportedCutters() {

		CutterProviderRegistry registry = new CutterProviderRegistry();
		Collection<ICutterProviderDescriptor> descriptors = registry.readCutterProviderDescriptors();
		List<ICutterDescriptor> resultList = new ArrayList<ICutterDescriptor>();
		for (ICutterProviderDescriptor descriptor : descriptors) {
			try {
				ICutterProvider provider = descriptor.createCutterProvider();
				if (provider != null) {
					resultList.addAll(provider.getSupportedCutters());
				}
			} catch (CoreException e) {
				handleProviderRegistryException(e);
			}
		}

		Collections.sort(resultList, new Comparator<ICutterDescriptor>() {

			@Override
			public int compare(ICutterDescriptor d1, ICutterDescriptor d2) {
				return d1.getName().compareTo(d2.getName());
			}
		});
		return resultList;
	}

	@Override
	public synchronized void handleNewCutter(ICutter cutter) {

		Collection<ICutter> oldCutters = cuttersRef.get();
		List<ICutter> newCutterList = new ArrayList<>(oldCutters);
		newCutterList.add(cutter);
		Collections.sort(newCutterList, new Comparator<ICutter>() {

			@Override
			public int compare(ICutter c1, ICutter c2) {
				return c1.getDescriptor().getName().compareTo(c2.getDescriptor().getName());
			}
		});
		cuttersRef.set(newCutterList);
	}

	@Override
	public synchronized void handleLostCutter(String cutterId) {

		Collection<ICutter> oldCutters = cuttersRef.get();
		List<ICutter> newCutterList = new ArrayList<>(oldCutters.size());
		for (ICutter cutter : oldCutters) {
			if (!cutter.getId().equals(cutterId)) {
				newCutterList.add(cutter);
			}
		}
		cuttersRef.set(newCutterList);
	}

	@Override
	public synchronized void handleStatusChange(ICutter cutter) {

		Collection<ICutter> oldCutters = cuttersRef.get();
		List<ICutter> newCutterList = new ArrayList<>(oldCutters.size());
		for (ICutter oldCutter : oldCutters) {
			if (cutter.getId().equals(oldCutter.getId())) {
				newCutterList.add(cutter);
			} else {
				newCutterList.add(oldCutter);
			}
		}
		cuttersRef.set(newCutterList);
	}
}
