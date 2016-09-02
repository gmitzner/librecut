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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.github.librecut.api.cutter.spi.ICutterProvider;

public class CutterProviderRegistry {

	private static final String EXTENSION_POINT_ID = "com.github.librecut.api.cutterProviders";
	private static final String ELEMENT_ID_PROVIDER = "provider";
	private static final String ATTR_ID = "id";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_VENDOR = "vendor";
	private static final String ATTR_CLASS = "class";

	public Collection<ICutterProviderDescriptor> readCutterProviderDescriptors() {

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		List<ICutterProviderDescriptor> resultList = new ArrayList<ICutterProviderDescriptor>(elements.length);
		for (IConfigurationElement element : elements) {
			if (ELEMENT_ID_PROVIDER.equals(element.getName())) {
				ICutterProviderDescriptor descriptor = createCutterProviderDescriptor(element);
				resultList.add(descriptor);
			}
		}
		return resultList;
	}

	private static ICutterProviderDescriptor createCutterProviderDescriptor(final IConfigurationElement element) {

		return new ICutterProviderDescriptor() {

			@Override
			public String getId() {
				return element.getAttribute(ATTR_ID);
			}

			@Override
			public String getName() {
				return element.getAttribute(ATTR_NAME);
			}

			@Override
			public String getVendor() {
				return element.getAttribute(ATTR_VENDOR);
			}

			@Override
			public ICutterProvider createCutterProvider() throws CoreException {

				Object obj = element.createExecutableExtension(ATTR_CLASS);
				if (obj instanceof ICutterProvider) {
					return (ICutterProvider) obj;
				}
				return null;
			}
		};
	}
}
