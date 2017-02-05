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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.common.cutter.spi.AbstractPollingCutterProvider;
import com.github.librecut.common.usb.Usb;
import com.github.librecut.common.usb.Usb.IDeviceMatcher;
import com.github.librecut.common.usb.UsbDeviceDescriptor;
import com.github.librecut.common.usb.UsbException;

public class CutterProvider extends AbstractPollingCutterProvider {

	private static final int VENDORID_GRAPHTEC = 0x0b4d;
	private static final int PRODUCTID_SILHOUETTE_CAMEO = 0x1121;

	private static final int TIME_BETWEEN_UPDATES_MILLIS = 15000;

	public CutterProvider() {
		super(Messages.CutterProvider_CutterSearchJobDescription, TIME_BETWEEN_UPDATES_MILLIS);
	}

	@Override
	public Collection<ICutterDescriptor> getSupportedCutters() {
		return Collections.<ICutterDescriptor>singletonList(new CameoCutterDescriptor());
	}

	@Override
	protected Collection<ICutter> detectCutters(IProgressMonitor monitor) throws InterruptedException {

		Usb usb = Activator.getDefault().getUsb();
		List<ICutter> cutterList = new ArrayList<>();
		try {
			List<UsbDeviceDescriptor> descriptorList = usb.findDevices(new IDeviceMatcher() {

				@Override
				public boolean matches(UsbDeviceDescriptor descriptor) {

					return (descriptor.getVendorId() == VENDORID_GRAPHTEC)
							&& (descriptor.getProductId() == PRODUCTID_SILHOUETTE_CAMEO);
				}
			});

			for (UsbDeviceDescriptor descriptor : descriptorList) {
				IStatefulCutter cutter = createCutter(descriptor, usb);
				cutter.updateDeviceState(new NullProgressMonitor());
				cutterList.add(cutter);
			}

		} catch (UsbException e) {
			handleUsbDeviceListingError(e);
		}
		return cutterList;
	}

	private IStatefulCutter createCutter(UsbDeviceDescriptor descriptor, Usb usb) {

		switch (descriptor.getProductId()) {
		case PRODUCTID_SILHOUETTE_CAMEO:
			String instanceName = MessageFormat.format(Messages.CutterProvider_UsbCutterNamePattern, new CameoCutterDescriptor().getDescription(),
					descriptor.getBusAddress());
			return new CameoCutter(instanceName, usb, descriptor);
		default:
			return null;
		}
	}

	private void handleUsbDeviceListingError(UsbException e) {
		// TODO implement error logging
	}
}
