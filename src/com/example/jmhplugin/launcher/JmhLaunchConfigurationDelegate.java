package com.example.jmhplugin.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.openjdk.jmh.results.format.ResultFormatType;

public class JmhLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, org.eclipse.core.runtime.IProgressMonitor monitor) throws CoreException {
    	String outputFileTypeString = configuration.getAttribute("outputFileType", "JSON");
    	String absoluteProjectPath = configuration.getAttribute("absoluteProjectPath", "");
    	String outputFolderPath = configuration.getAttribute("outputFolderPath",  absoluteProjectPath + "/result.json");
    	String mainQyalifiedName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
    	String outputFileName = configuration.getAttribute("outputFileName", "result.json"); 
    	
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
					outputFolderPath + "/" + outputFileName + "." + outputFileType.toString().toLowerCase()
					);

			System.out.println(String.join(" ", builder.command()));
			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, "JMH Benchmark Process");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	private ResultFormatType getOutputFileType(String outputFileTypeString) {
		return ResultFormatType.valueOf(outputFileTypeString);
	}
    
    private String getClasspath(ILaunchConfiguration configuration) throws CoreException {
    	List<String> classPathEntries = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, new ArrayList<String>());
    	return String.join(File.pathSeparator, classPathEntries);
    }
    
    private MessageConsole findOrCreateConsole(String name) {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        for (IConsole console : consoleManager.getConsoles()) {
            if (console.getName().equals(name)) {
                return (MessageConsole) console;
            }
        }
        MessageConsole console = new MessageConsole(name, null);
        consoleManager.addConsoles(new IConsole[]{console});
        return console;
    }

    private void pipeOutput(Process process, MessageConsoleStream consoleStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consoleStream.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace(new PrintStream(consoleStream));
        }
    }
}
