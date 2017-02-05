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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.cutter.model.IParameterDescriptor;
import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;

public class CameoCutterDescriptor implements ICutterDescriptor {

	public static final String PARAM_MEDIA = "media";
	public static final String PARAM_SPEED = "speed";
	public static final String PARAM_PRESSURE = "pressure";

	private static final double DPI = 25.4 * 20.0;

	@Override
	public String getName() {
		return Messages.CameoCutterDescriptor_GraphtecSilhouetteCameoName;
	}

	@Override
	public String getDescription() {
		// TODO implement this one!
		return null;
	}

	@Override
	public Collection<IMedia> getDefaultMediaFormats() {
		// TODO implement this one!
		return null;
	}

	@Override
	public IBorders getBorders(IMediaSize mediaSize) {
		// TODO implement this one!
		return null;
	}

	@Override
	public LoadingDirection getLoadingDirection() {
		return LoadingDirection.Top;
	}

	@Override
	public double getDpiX() {
		return DPI;
	}

	@Override
	public double getDpiY() {
		return DPI;
	}

	@Override
	public Collection<IParameterDescriptor<?>> getCuttingParameters() {

		List<IParameterDescriptor<?>> resultList = new ArrayList<IParameterDescriptor<?>>(3);
		resultList.add(new MediaParameterDescriptor(PARAM_MEDIA, Messages.CameoCutterDescriptor_Media));
		resultList.add(new RangeParameterDescriptor(PARAM_SPEED, Messages.CameoCutterDescriptor_Speed, 1, 10));
		resultList.add(new RangeParameterDescriptor(PARAM_PRESSURE, Messages.CameoCutterDescriptor_Pressure, 1, 33));
		return resultList;
	}

	private static class MediaParameterDescriptor implements IParameterDescriptor<String> {

		private final String name;
		private final String label;
		private final Map<String, String> valueMap;

		public MediaParameterDescriptor(String name, String label) {

			this.name = name;
			this.label = label;
			this.valueMap = new HashMap<String, String>(MediaType.values().length);
			for (MediaType mediaType : MediaType.values()) {
				this.valueMap.put(mediaType.name(), mediaType.getLabel());
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<String> getType() {
			return String.class;
		}

		@Override
		public boolean isValid(Object value) {
			return valueMap.containsKey(value);
		}

		@Override
		public String getMinValue() {
			return null;
		}

		@Override
		public String getMaxValue() {
			return null;
		}

		@Override
		public Collection<String> getValues() {
			return valueMap.keySet();
		}

		@Override
		public String getLabel(Object value, Locale locale) {
			if (name.equals(value)) {
				return label;
			}
			return valueMap.get(value);
		}
	}

	private static class RangeParameterDescriptor implements IParameterDescriptor<Integer> {

		private final String name;
		private final String label;
		private final int minValue;
		private final int maxValue;

		public RangeParameterDescriptor(String name, String label, int minValue, int maxValue) {

			this.name = name;
			this.label = label;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}

		@Override
		public boolean isValid(Object obj) {

			if (!(obj instanceof Integer)) {
				return false;
			}
			int value = (Integer) obj;
			return (minValue <= value) && (value <= maxValue);
		}

		@Override
		public Integer getMinValue() {
			return minValue;
		}

		@Override
		public Integer getMaxValue() {
			return maxValue;
		}

		@Override
		public Collection<Integer> getValues() {
			return null;
		}

		@Override
		public String getLabel(Object value, Locale locale) {
			if (name.equals(value)) {
				return label;
			}
			return value.toString();
		}
	}
}
