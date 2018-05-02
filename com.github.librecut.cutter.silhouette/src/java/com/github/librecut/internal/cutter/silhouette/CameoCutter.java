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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.design.model.IPattern;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.model.IPolyline;
import com.github.librecut.common.usb.Usb;
import com.github.librecut.common.usb.UsbDeviceDescriptor;
import com.github.librecut.common.usb.UsbDeviceHandle;
import com.github.librecut.common.usb.UsbException;

public class CameoCutter implements IStatefulCutter {

	private static final int INTERFACE_ID = 0;

	private static final byte ENDPOINT_ID_COMMAND = (byte) 0x01;
	private static final byte ENDPOINT_ID_STATUS = (byte) 0x82;

	private static final byte GET_STATUS_COMMAND[] = new byte[] { (byte) 0x1b, (byte) 0x05 };
	private static final byte RESET_COMMAND[] = new byte[] { (byte) 0x1b, (byte) 0x04 };

	private static final String COMMAND_ENCODING = "UTF-8";
	private static final byte COMMAND_DELIMITER = (byte) 0x03;

	private static final String HOME_CUTTER_1 = "TT";
	private static final String HOME_CUTTER_2 = "H";
	private static final String SET_MEDIA_TYPE = "FW%d";
	private static final String SET_CUTTING_SPEED = "!%d";
	private static final String SET_CUTTING_PRESSURE = "FX%d";
	private static final String SET_CUTTING_OFFSET = "FC%d";
	private static final String SET_TRACK_ENHANCING = "FY%d";
	private static final String SET_MEDIA_ORIENTATION = "FN%d";
	private static final String FEED_OUT = "FO0";
	private static final String START_CUTTING_0 = "TB50,0";
	private static final String START_CUTTING_1 = "FE0,0";
	private static final String START_CUTTING_2 = "FF0,0,0";
	private static final String SET_LOWER_LEFT = "\\%d,%d";
	private static final String SET_UPPER_RIGHT = "Z%d,%d";
	private static final String SET_LINE_TYPE = "L%d";

	private static final int MINIMUM_COMMAND_LENGTH = 100;

	private static final int GET_STATUS_TIMEOUT_MILLIS = 10000;
	private static final int RESET_TIMEOUT_MILLIS = 10000;
	private static final int WRITE_COMMAND_TIMEOUT_MILLIS = 1000;

	private final String id;
	private final Usb usb;
	private final UsbDeviceDescriptor descriptor;

	private volatile DeviceState state;

	public CameoCutter(String id, Usb usb, UsbDeviceDescriptor descriptor) {

		this.id = id;
		this.usb = usb;
		this.descriptor = descriptor;

		this.state = DeviceState.Initializing;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ICutterDescriptor getDescriptor() {
		return new CameoCutterDescriptor();
	}

	@Override
	public DeviceState getDeviceState() {
		return state;
	}

	@Override
	public void updateDeviceState(IProgressMonitor monitor) throws InterruptedException {

		UsbDeviceHandle device = null;
		try {
			device = usb.openDevice(descriptor);
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			if (device == null) {
				state = DeviceState.Off;
				return;
			}

			device.claimInterface(INTERFACE_ID);
			try {
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
				state = queryDeviceState(device, monitor);
			} finally {
				device.releaseInterface(INTERFACE_ID);
			}
		} catch (UsbException e) {
			// TODO log error
			state = DeviceState.Error;
		} finally {
			if (device != null) {
				device.close();
			}
		}
	}

	@Override
	public IStatus reset(IProgressMonitor monitor) {

		UsbDeviceHandle device = null;
		try {
			device = usb.openDevice(descriptor);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (device == null) {
				return handleDeviceNotAvailable();
			}
			device.claimInterface(INTERFACE_ID);
			try {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				device.bulkWrite(ENDPOINT_ID_COMMAND, RESET_COMMAND, RESET_TIMEOUT_MILLIS);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				boolean resetFinished = false;
				while (!resetFinished) {
					device.bulkWrite(ENDPOINT_ID_COMMAND, GET_STATUS_COMMAND, GET_STATUS_TIMEOUT_MILLIS);
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					byte[] response = device.bulkRead(ENDPOINT_ID_STATUS, GET_STATUS_TIMEOUT_MILLIS);
					if ((response.length != 2) || (response[1] != (byte) 0x03)) {
						// TODO log error
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid status response received");
					}
					switch (response[0] & (byte) 0x03) {
					case 0:
						resetFinished = true;
						break;
					case 1:
						break;
					case 2:
						resetFinished = true;
						break;
					default:
						// TODO log error
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid status response received");
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
				}
			} finally {
				device.releaseInterface(INTERFACE_ID);
			}
		} catch (UsbException e) {
			// TODO log error
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "USB communication has failed.", e);
		} finally {
			if (device != null) {
				device.close();
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus cut(IPattern pattern, Map<String, Object> parameterMap, IProgressMonitor monitor) {

		if (!validateParameters(parameterMap)) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid parameters");
		}

		UsbDeviceHandle device = null;
		try {
			device = usb.openDevice(descriptor);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (device == null) {
				return handleDeviceNotAvailable();
			}
			device.claimInterface(INTERFACE_ID);
			try {
				DeviceState deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}

				// TODO query plotter firmware version for debugging

				sendCutterHome(device, monitor);
				deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}

				sendCuttingParameter(device, parameterMap, monitor);
				deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}

				sendCuttingInitializationSequence(device, monitor);
				deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}

				sendCuttingData(device, pattern, monitor);
				deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}

				feedMediaOut(device, monitor);
				deviceState = waitWhileBusy(device, monitor);
				if (deviceState != DeviceState.Ready) {
					return handleDeviceNotReady(deviceState);
				}
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			} finally {
				device.releaseInterface(INTERFACE_ID);
			}
		} catch (UsbException e) {
			// TODO log error
			System.err.println(e.getMessage());
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "USB communication has failed.", e);
		} finally {
			if (device != null) {
				device.close();
			}
		}
		return Status.OK_STATUS;
	}

	private boolean validateParameters(Map<String, Object> parameterMap) {

		Object obj = parameterMap.get(CameoCutterDescriptor.PARAM_MEDIA);
		if (!(obj instanceof String)) {
			return false;
		}
		try {
			MediaType mediaType = MediaType.valueOf((String) obj);
			if ((mediaType.getMediaValue() < 100) || (mediaType.getMediaValue() > 138)) {
				return false;
			}
			if ((mediaType.getCuttingOffset() != 0) && (mediaType.getCuttingOffset() != 18)) {
				return false;
			}
		} catch (IllegalArgumentException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}

		obj = parameterMap.get(CameoCutterDescriptor.PARAM_SPEED);
		if (!(obj instanceof Integer)) {
			return false;
		}
		int value = (Integer) obj;
		if (value < 1) {
			return false;
		}
		if (value > 10) {
			return false;
		}

		obj = parameterMap.get(CameoCutterDescriptor.PARAM_PRESSURE);
		if (!(obj instanceof Integer)) {
			return false;
		}
		value = (Integer) obj;
		if (value < 1) {
			return false;
		}
		if (value > 33) {
			return false;
		}

		return true;
	}

	private void sendCuttingInitializationSequence(UsbDeviceHandle device, IProgressMonitor monitor)
			throws UsbException, InterruptedException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		appendCommand(buffer, START_CUTTING_0);
		// appendCommand(buffer, String.format(SET_LOWER_LEFT, 30, 30));
		appendCommand(buffer, START_CUTTING_1);
		appendCommand(buffer, START_CUTTING_2);
		appendCommand(buffer, String.format(SET_LINE_TYPE, 0));
		finishCommand(buffer);

		device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(), WRITE_COMMAND_TIMEOUT_MILLIS);
	}

	private void sendCuttingData(UsbDeviceHandle device, IPattern pattern, IProgressMonitor monitor)
			throws UsbException, InterruptedException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int commandCounter = 0;
		Collection<IPolyline> polylines = pattern.getPolylines(true);
		SubMonitor subMonitor = SubMonitor.convert(monitor, polylines.size());
		try {
			for (IPolyline polyline : polylines) {
				commandCounter += writePolylineCuttingData(polyline, buffer);

				if (commandCounter >= MINIMUM_COMMAND_LENGTH) {
					device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(),
							WRITE_COMMAND_TIMEOUT_MILLIS * commandCounter);
					waitWhileBusy(device, subMonitor);
					commandCounter = 0;
					buffer = new ByteArrayOutputStream();
				}

				subMonitor.worked(1);
			}

			if (buffer.size() > 0) {
				device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(),
						WRITE_COMMAND_TIMEOUT_MILLIS * commandCounter);
				waitWhileBusy(device, subMonitor);
			}
		} finally {
			monitor.done();
		}
	}

	private int writePolylineCuttingData(IPolyline polyline, ByteArrayOutputStream buffer) {

		Iterator<IPoint> iterator = polyline.iterator();

		IPoint point = iterator.next();
		int x = getX(point);
		int y = getY(point);
		String command = String.format("M%d.00,%d.00", x, y);
		appendCommand(buffer, command);

		int commandCounter = 1;
		int lastX = x;
		int lastY = y;

		while (iterator.hasNext()) {
			point = iterator.next();
			x = getX(point);
			y = getY(point);
			if ((x != lastX) || (y != lastY)) {
				command = String.format("D%d.00,%d.00", x, y);
				appendCommand(buffer, command);
				++commandCounter;
				lastX = x;
				lastY = y;
			}
		}
		finishCommand(buffer);

		return commandCounter;
	}

	private int getX(IPoint point) {

		final int offsetX = 0;
		final int minX = 0;
		final int maxX = 6095;

		int x = (int) Math.round(point.getY()) + offsetX;
		if (x < minX) {
			x = minX;
		}
		if (x > maxX) {
			x = maxX;
		}
		return x;
	}

	private int getY(IPoint point) {

		final int offsetY = 0;
		final int minY = 0;
		final int maxY = 6095;

		int y = (int) Math.round(point.getX()) + offsetY;
		if (y < minY) {
			y = minY;
		}
		if (y > maxY) {
			y = maxY;
		}
		return y;
	}

	private void feedMediaOut(UsbDeviceHandle device, IProgressMonitor monitor) throws UsbException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		appendCommand(buffer, FEED_OUT);
		appendCommand(buffer, HOME_CUTTER_2);
		finishCommand(buffer);
		device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(), WRITE_COMMAND_TIMEOUT_MILLIS);
	}

	private void sendCuttingParameter(UsbDeviceHandle device, Map<String, Object> parameterMap,
			IProgressMonitor monitor) throws UsbException {

		// final int MEDIA_CODE = 112; // thin media (100-138, 300)
		// final int CUTTING_SPEED = 8; // 1-10
		// final int CUTTING_PRESSURE = 1; // 1-33
		// final int CUTTING_OFFSET = 18; // 18 = cutter; 0 = pen
		MediaType mediaType = MediaType.valueOf((String) parameterMap.get(CameoCutterDescriptor.PARAM_MEDIA));
		int mediaCode = mediaType.getMediaValue();
		int cuttingOffset = mediaType.getCuttingOffset();
		int cuttingSpeed = (Integer) parameterMap.get(CameoCutterDescriptor.PARAM_SPEED);
		int cuttingPressure = (Integer) parameterMap.get(CameoCutterDescriptor.PARAM_PRESSURE);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		appendCommand(buffer, String.format(SET_MEDIA_TYPE, mediaCode));
		appendCommand(buffer, String.format(SET_CUTTING_SPEED, cuttingSpeed));
		appendCommand(buffer, String.format(SET_CUTTING_PRESSURE, cuttingPressure));
		appendCommand(buffer, String.format(SET_CUTTING_OFFSET, cuttingOffset));
		appendCommand(buffer, String.format(SET_TRACK_ENHANCING, 0));
		appendCommand(buffer, String.format(SET_MEDIA_ORIENTATION, 0));
		finishCommand(buffer);
		device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(), WRITE_COMMAND_TIMEOUT_MILLIS);
	}

	private void sendCutterHome(UsbDeviceHandle device, IProgressMonitor monitor) throws UsbException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		appendCommand(buffer, HOME_CUTTER_1);
		finishCommand(buffer);
		device.bulkWrite(ENDPOINT_ID_COMMAND, buffer.toByteArray(), WRITE_COMMAND_TIMEOUT_MILLIS);
	}

	private static void appendCommand(ByteArrayOutputStream buffer, String command) {
		try {
			if (buffer.size() > 0) {
				buffer.write(",".getBytes(COMMAND_ENCODING));
			}
			buffer.write(command.getBytes(COMMAND_ENCODING));
		} catch (IOException e) {
			// cannot happen
		}
	}

	private static void finishCommand(ByteArrayOutputStream buffer) {
		buffer.write(COMMAND_DELIMITER);
	}

	private DeviceState waitWhileBusy(UsbDeviceHandle device, IProgressMonitor monitor)
			throws UsbException, InterruptedException {

		DeviceState state = queryDeviceState(device, monitor);
		while ((state == DeviceState.Busy) || (state == DeviceState.Initializing)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// nothing to do
			}
			state = queryDeviceState(device, monitor);
		}
		return state;
	}

	private DeviceState queryDeviceState(UsbDeviceHandle device, IProgressMonitor monitor)
			throws UsbException, InterruptedException {

		device.bulkWrite(ENDPOINT_ID_COMMAND, GET_STATUS_COMMAND, GET_STATUS_TIMEOUT_MILLIS);
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
		byte[] response = device.bulkRead(ENDPOINT_ID_STATUS, GET_STATUS_TIMEOUT_MILLIS);
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}

		if ((response.length != 2) || (response[1] != (byte) 0x03)) {
			// TODO log error
			return DeviceState.Error;
		}
		switch (response[0] & (byte) 0x03) {
		case 0:
			return DeviceState.Ready;
		case 1:
			return DeviceState.Busy;
		case 2:
			return DeviceState.WaitingForMedia;
		default:
			// TODO log error
			return DeviceState.Error;
		}
	}

	private static IStatus handleDeviceNotReady(DeviceState deviceState) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				String.format("Device is not ready (%s)", deviceState.name()));
	}

	private static IStatus handleDeviceNotAvailable() {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Device is not available");
	}
}
