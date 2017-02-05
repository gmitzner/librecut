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
import java.nio.IntBuffer;
import java.util.BitSet;

import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

public class UsbDeviceHandle {

	private final BitSet claimedInterfaceIdSet;
	private final BitSet kernelDetachedSet;

	private volatile DeviceHandle handle;

	public UsbDeviceHandle(DeviceHandle handle) {

		this.claimedInterfaceIdSet = new BitSet();
		this.kernelDetachedSet = new BitSet();

		this.handle = handle;
	}

	public void claimInterface(int interfaceId) throws UsbException {

		if (handle == null) {
			throw new UsbException("Device already closed.", 0);
		}

		boolean alreadyClaimed;
		synchronized (this) {
			alreadyClaimed = claimedInterfaceIdSet.get(interfaceId);
		}

		if (!alreadyClaimed) {
			if (LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER)
					&& (LibUsb.kernelDriverActive(handle, interfaceId) == 1)) {
				int result = LibUsb.detachKernelDriver(handle, interfaceId);
				synchronized (this) {
					kernelDetachedSet.set(interfaceId, result == LibUsb.SUCCESS);
				}
				if (result != LibUsb.SUCCESS) {
					throw new UsbException("Cannot detach kernel driver from interface.", result);
				}
			}

			int result = LibUsb.claimInterface(handle, interfaceId);
			synchronized (this) {
				claimedInterfaceIdSet.set(interfaceId, result == LibUsb.SUCCESS);
			}
			if (result != LibUsb.SUCCESS) {

				boolean kernelDetached;
				synchronized (this) {
					kernelDetached = kernelDetachedSet.get(interfaceId);
				}
				if (kernelDetached) {
					if (LibUsb.attachKernelDriver(handle, interfaceId) == LibUsb.SUCCESS) {
						synchronized (this) {
							kernelDetachedSet.set(interfaceId, false);
						}
					}
				}

				throw new UsbException("Cannot claim interface.", result);
			}
		}
	}

	public void releaseInterface(int interfaceId) throws UsbException {

		if (handle == null) {
			throw new UsbException("Device already closed.", 0);
		}
		releaseInterface(handle, interfaceId);
	}

	private void releaseInterface(DeviceHandle handle, int interfaceId) throws UsbException {

		boolean alreadyClaimed;
		boolean kernelDetached;
		synchronized (this) {
			alreadyClaimed = claimedInterfaceIdSet.get(interfaceId);
			kernelDetached = kernelDetachedSet.get(interfaceId);
		}

		if (alreadyClaimed) {
			int result = LibUsb.releaseInterface(handle, interfaceId);
			if (result != LibUsb.SUCCESS) {
				throw new UsbException("Cannot free interface.", result);
			}

			synchronized (this) {
				claimedInterfaceIdSet.set(interfaceId, false);
			}
		}

		if (kernelDetached) {
			int result = LibUsb.attachKernelDriver(handle, interfaceId);
			if (result != LibUsb.SUCCESS) {
				throw new UsbException("Cannot restore attached kernel driver for interface.", result);
			}

			synchronized (this) {
				kernelDetachedSet.set(interfaceId, false);
			}
		}
	}

	private DeviceHandle getHandle() {
		return handle;
	}

	public void close() {

		DeviceHandle handle;
		BitSet activeInterfaceIdSet = new BitSet();
		synchronized (this) {
			handle = this.handle;
			activeInterfaceIdSet.or(kernelDetachedSet);
			activeInterfaceIdSet.or(claimedInterfaceIdSet);

			this.handle = null;
		}

		if (handle != null) {
			for (int i = activeInterfaceIdSet.nextSetBit(0); i >= 0; i = activeInterfaceIdSet.nextSetBit(i + 1)) {
				try {
					releaseInterface(handle, i);
				} catch (UsbException e) {
					// can we do anything in here?!?
				}
			}

			LibUsb.close(handle);
		}
	}

	public void bulkWrite(byte endpointId, byte[] data, int timeoutMillis) throws UsbException {

		dumpBuffer("Write", data);

		// TODO fix timeout handling
		IntBuffer lengthBuffer = IntBuffer.allocate(1);
		for (int offset = 0; offset < data.length; offset += lengthBuffer.get(0)) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(data.length - offset);
			buffer.put(data, offset, data.length - offset);
			int result = LibUsb.bulkTransfer(getHandle(), endpointId, buffer, lengthBuffer, timeoutMillis);
			if (result != LibUsb.SUCCESS) {
				throw new UsbException("Cannot write data in bulk mode.", result);
			}
		}
	}

	public byte[] bulkRead(byte endpointId, int timeoutMillis) throws UsbException {

		IntBuffer lengthBuffer = IntBuffer.allocate(1);
		ByteBuffer buffer = ByteBuffer.allocateDirect(64);
		int result = LibUsb.bulkTransfer(getHandle(), endpointId, buffer, lengthBuffer, timeoutMillis);
		if (result != LibUsb.SUCCESS) {
			throw new UsbException("Cannot read data in bulk mode.", result);
		}
		byte[] data = new byte[lengthBuffer.get(0)];
		buffer.get(data, 0, data.length);

		dumpBuffer("Read", data);

		return data;
	}

	private static void dumpBuffer(String prefix, byte[] buffer) {

		System.out.println(prefix + " (" + buffer.length + "): ");
		for (byte b : buffer) {
			System.out.print(String.format("%02x", b & 0xFF));
		}
		System.out.println();
	}
}
