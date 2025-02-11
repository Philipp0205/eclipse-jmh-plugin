package com.example.jmhplugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JmhPlugin extends AbstractUIPlugin {
	private static JmhPlugin plugin;
	
	public JmhPlugin() {
		plugin = this;
	}
	
	public static JmhPlugin getDefault() {
		return plugin;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("JmhPlugin started");
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}
