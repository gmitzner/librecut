package com.github.librecut.internal.common.opencv;

import org.opencv.core.Core;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
}
