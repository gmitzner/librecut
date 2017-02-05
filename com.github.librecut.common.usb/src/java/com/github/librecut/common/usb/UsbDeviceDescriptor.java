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

public class UsbDeviceDescriptor {

	private final int productId;
	private final int vendorId;
	private final int deviceClass;
	private final int deviceSubClass;
	private final String busAddress;

	public UsbDeviceDescriptor(int productId, int vendorId, int deviceClass, int deviceSubClass, String busAddress) {

		this.productId = productId;
		this.vendorId = vendorId;
		this.deviceClass = deviceClass;
		this.deviceSubClass = deviceSubClass;
		this.busAddress = busAddress;
	}

	public int getProductId() {
		return productId;
	}

	public int getVendorId() {
		return vendorId;
	}

	public int getDeviceClass() {
		return deviceClass;
	}

	public int getDeviceSubClass() {
		return deviceSubClass;
	}

	public String getBusAddress() {
		return busAddress;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((busAddress == null) ? 0 : busAddress.hashCode());
		result = prime * result + deviceClass;
		result = prime * result + deviceSubClass;
		result = prime * result + productId;
		result = prime * result + vendorId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UsbDeviceDescriptor other = (UsbDeviceDescriptor) obj;
		if (busAddress == null) {
			if (other.busAddress != null) {
				return false;
			}
		} else if (!busAddress.equals(other.busAddress)) {
			return false;
		}
		if (deviceClass != other.deviceClass) {
			return false;
		}
		if (deviceSubClass != other.deviceSubClass) {
			return false;
		}
		if (productId != other.productId) {
			return false;
		}
		if (vendorId != other.vendorId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UsbDeviceDescriptor [productId=" + productId + ", vendorId=" + vendorId + ", deviceClass=" + deviceClass
				+ ", deviceSubClass=" + deviceSubClass + ", busAddress=" + busAddress + "]";
	}
}
