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
package com.github.librecut.common.usb;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;

public class Usb {

	private final Context context;

	private Usb(Context context) {

		this.context = context;
	}

	public static Usb create() throws UsbException {

		Context context = new Context();
		int result = LibUsb.init(context);
		if (result != LibUsb.SUCCESS) {
			throw new UsbException("Cannot initialize libusb.", result);
		}

		return new Usb(context);
	}

	public static void destroy(Usb usb) {

		LibUsb.exit(usb.context);
	}

	public List<UsbDeviceDescriptor> findDevices(IDeviceMatcher matcher) throws UsbException {

		List<UsbDeviceDescriptor> resultList = new ArrayList<UsbDeviceDescriptor>();

		DeviceList deviceList = new DeviceList();
		int result = LibUsb.getDeviceList(context, deviceList);
		if (result < 0) {
			throw new UsbException("Cannot retrieve USB device list.", result);
		}
		try {
			for (Device device : deviceList) {
				UsbDeviceDescriptor usbDescriptor = createDescriptor(device);
				if (matcher.matches(usbDescriptor)) {
					resultList.add(usbDescriptor);
				}
			}
		} finally {
			LibUsb.freeDeviceList(deviceList, true);
		}
		return resultList;
	}

	public UsbDeviceHandle openDevice(UsbDeviceDescriptor descriptor) throws UsbException {

		DeviceList deviceList = new DeviceList();
		int result = LibUsb.getDeviceList(context, deviceList);
		if (result < 0) {
			throw new UsbException("Cannot retrieve USB device list.", result);
		}
		try {
			for (Device device : deviceList) {
				UsbDeviceDescriptor usbDescriptor = createDescriptor(device);
				if (usbDescriptor.equals(descriptor)) {
					DeviceHandle handle = new DeviceHandle();
					result = LibUsb.open(device, handle);
					if (result != LibUsb.SUCCESS) {
						throw new UsbException("Cannot open USB device.", result);
					}
					return new UsbDeviceHandle(handle);
				}
			}
		} finally {
			LibUsb.freeDeviceList(deviceList, true);
		}
		return null;
	}

	private static UsbDeviceDescriptor createDescriptor(Device device) throws UsbException {

		DeviceDescriptor descriptor = new DeviceDescriptor();
		int result = LibUsb.getDeviceDescriptor(device, descriptor);
		if (result != LibUsb.SUCCESS) {
			throw new UsbException("Unable to read USB device descriptor.", result);
		}
		ByteBuffer portNumbers = ByteBuffer.allocateDirect(16);
		int portNumbersLength = LibUsb.getPortNumbers(device, portNumbers);
		if (result < 0) {
			throw new UsbException("Unable to read USB port numbers.", result);
		}
		UsbDeviceDescriptor usbDescriptor = createDescriptor(descriptor, LibUsb.getBusNumber(device), portNumbers,
				portNumbersLength);
		return usbDescriptor;
	}

	private static UsbDeviceDescriptor createDescriptor(DeviceDescriptor descriptor, int busNumber,
			ByteBuffer portNumbers, int portNumbersLength) {

		StringBuilder builder = new StringBuilder();
		builder.append(busNumber);
		for (int i = 0; i < portNumbersLength; ++i) {
			builder.append('-');
			builder.append(portNumbers.get(i));
		}
		String busAddress = builder.toString();

		return new UsbDeviceDescriptor(descriptor.idProduct(), descriptor.idVendor(), descriptor.bDeviceClass(),
				descriptor.bDeviceSubClass(), busAddress);
	}

	public interface IDeviceMatcher {

		boolean matches(UsbDeviceDescriptor descriptor);
	}
}
