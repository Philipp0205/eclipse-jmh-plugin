package com.example.jmhplugin.launcher;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class JmhLaunchActionDelegate extends LaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, org.eclipse.core.runtime.IProgressMonitor monitor) {
        try {
            // Retrieve configuration attributes
            String project = configuration.getAttribute("jmh.project", "");
            String testClass = configuration.getAttribute("jmh.testClass", "");
            String testMethod = configuration.getAttribute("jmh.testMethod", "");

            // Construct and execute the JMH command
            String command = String.format("jmh.launch.command --project %s --class %s --method %s", project, testClass, testMethod);
            ProcessBuilder builder = new ProcessBuilder(command.split(" "));
            builder.inheritIO().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
