package com.example.jmhplugin.launchconfig;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openjdk.jmh.results.format.ResultFormatType;

import com.example.jmhplugin.JmhPlugin;

public class JmhLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, org.eclipse.core.runtime.IProgressMonitor monitor) throws CoreException {
        IPreferenceStore store = JmhPlugin.getDefault().getPreferenceStore();
    	String outputFileTypeString = configuration.getAttribute("outputFileType", "JSON");
    	String absoluteProjectPath = configuration.getAttribute("javaProjectPath", "");
    	String mainQyalifiedName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
    	String outputFileName = configuration.getAttribute("outputFileName", "result.json"); 
    	String preferencesPath = store.getString("jmhDefaultOutputPath"); 

    	if (configuration.getAttribute("isMethod", false)) {
    		mainQyalifiedName = mainQyalifiedName + "$";
    	} 

		try {
			ResultFormatType outputFileType = getOutputFileType(outputFileTypeString);
			String pathToBenchmarkJarString = absoluteProjectPath + "/" + "target/benchmarks.jar";
			ProcessBuilder builder = new ProcessBuilder(
					"java",
					"-jar",
					pathToBenchmarkJarString,
					mainQyalifiedName,
					"-rf",
					outputFileType.toString().toLowerCase(),
					"-rff",
					preferencesPath + "/" + outputFileName + "." + outputFileType.toString().toLowerCase()
					);

			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, "JMH Benchmark Process");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	private ResultFormatType getOutputFileType(String outputFileTypeString) {
		return ResultFormatType.valueOf(outputFileTypeString);
	}
}
