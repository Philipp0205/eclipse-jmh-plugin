package com.example.jmhplugin.launchconfig;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;

public class JMHLaunchHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            // Get the launch configuration manager
            ILaunchConfigurationType configType = DebugPlugin.getDefault()
                    .getLaunchManager()
                    .getLaunchConfigurationType("com.example.jmhplugin.launchconfig");

            // Create a new launch configuration working copy
            ILaunchConfigurationWorkingCopy workingCopy = configType.newInstance(
                    null, "New JMH Benchmark");

            // Set attributes specific to the configuration
            workingCopy.setAttribute("exampleAttribute", "exampleValue"); // Example attribute

            // Save the configuration
            ILaunchConfiguration config = workingCopy.doSave();

            // Optionally execute or open the configuration in the "Run Configurations" dialog
            DebugUITools.openLaunchConfigurationDialog(null, config, "run", null);

        } catch (CoreException e) {
            throw new ExecutionException("Failed to create JMH launch configuration", e);
        }

        return null;
    }
}
