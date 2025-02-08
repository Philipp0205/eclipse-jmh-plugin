package com.example.jmhplugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Utils {
	private static final String PLUGIN_ID = "com.example.jmh";
	private static ILog log = Platform.getLog(Platform.getBundle(PLUGIN_ID));

	public static void logError(String message, Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
		log.log(status);
	}

	public static void logInfo(String message) {
		IStatus status = new Status(IStatus.INFO, PLUGIN_ID, message);
		log.log(status);
	}
}
