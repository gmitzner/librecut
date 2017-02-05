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
package com.github.librecut.internal.cutter.silhouette;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.github.librecut.common.usb.Usb;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "com.github.librecut.cutter.silhouette";

	private static Activator current;

	private Usb usb;

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		usb = Usb.create();

		current = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		current = null;

		Usb.destroy(usb);
		usb = null;
	}

	public static Activator getDefault() {
		return current;
	}

	public Usb getUsb() {
		return usb;
	}
}
