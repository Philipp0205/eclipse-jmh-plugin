package com.example.jmhplugin.runconfig.ui;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

public class LunchTabGroup implements ILaunchConfigurationTabGroup {

	private ILaunchConfigurationTab[] tabs;

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		tabs = new ILaunchConfigurationTab[] { 
				new JmhTab(), 
				new JavaJRETab(),
				new JavaClasspathTab(),
				new CommonTab(),
				new JavaArgumentsTab()
		};
	}

	@Override
	public ILaunchConfigurationTab[] getTabs() {
		return tabs;
	}

	@Override
	public void dispose() {
		if (tabs != null) {
			for (ILaunchConfigurationTab tab : tabs) {
				tab.dispose();
			}
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.setDefaults(configuration);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.initializeFrom(configuration);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.performApply(configuration);
		}
	}

	@Override
	public void launched(ILaunch launch) {
		// TODO Auto-generated method stub
	}
}
