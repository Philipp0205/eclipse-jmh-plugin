package com.example.jmhplugin.launchconfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openjdk.jmh.results.format.ResultFormatType;

import com.example.jmhplugin.JmhPlugin;
import com.example.jmhplugin.Utils;

public class JmhLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    ILog log = Platform.getLog(Platform.getBundle("com.example.jmh"));
	
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, org.eclipse.core.runtime.IProgressMonitor monitor) throws CoreException {
        IPreferenceStore store = JmhPlugin.getDefault().getPreferenceStore();
    	String outputFileTypeString = configuration.getAttribute("outputFileType", "JSON");
    	String absoluteProjectPath = configuration.getAttribute("javaProjectPath", "");
    	String mainQyalifiedName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
    	String launchConfigOutputFolderPath = configuration.getAttribute("outputFolderPath", "");
    	String launchConfigOutputFileName = configuration.getAttribute("outputFileName", "result.json"); 
    	String preferencesPath = store.getString("jmhDefaultOutputPath"); 
    	boolean stexMode = store.getBoolean("stexMode");
    	System.out.println("stexMode: " + stexMode);

    	if (configuration.getAttribute("isMethod", false)) {
    		mainQyalifiedName = mainQyalifiedName + "$";
    	} 

		ResultFormatType outputFileType = getOutputFileType(outputFileTypeString);
		String pathToBenchmarkJarString = absoluteProjectPath + "/" + "target/benchmarks.jar";
   	
		if (stexMode) {
			createBenchmarkJar(absoluteProjectPath, absoluteProjectPath);
			runInStex(mainQyalifiedName, launchConfigOutputFileName, preferencesPath, outputFileType, absoluteProjectPath);
		} 

		try {

			ProcessBuilder builder = normalRun(mainQyalifiedName, launchConfigOutputFileName, preferencesPath, outputFileType,
					pathToBenchmarkJarString, launchConfigOutputFolderPath, launchConfigOutputFileName);

			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, "JMH Benchmark Process");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ProcessBuilder normalRun(String mainQyalifiedName, String outputFileName, String preferencesPath,
			ResultFormatType outputFileType, String pathToBenchmarkJarString, String launchConfigOutputFolderPath, String launchConfigOutputFileName) {
		
		Utils.logInfo("Output path " + preferencesPath + "/" + outputFileName + "." + outputFileType.toString());
		
		String resultFilePath = preferencesPath + "/" + outputFileName + "." + outputFileType.toString().toLowerCase();
		if (!launchConfigOutputFolderPath.isEmpty()) {
			resultFilePath = launchConfigOutputFolderPath + "/" + launchConfigOutputFileName + "." + outputFileType.toString().toLowerCase();
		}

		ProcessBuilder builder = new ProcessBuilder("java", //
				"-jar", //
				pathToBenchmarkJarString, //
				mainQyalifiedName, //
				"-rf", //
				outputFileType.toString().toLowerCase(), //
				"-rff", //
				resultFilePath //
		);
		return builder;
	}
    
	private void runInStex(String mainQyalifiedName, String outputFileName, String preferencesPath,
			ResultFormatType outputFileType, String projectPath) {
		
		createBenchmarkJar(projectPath, projectPath);
	}

	public record FqnParts(String packageName, String className, String methodName) {
	}

	public FqnParts extractComponents(String mainTypeName) {
		if (mainTypeName != null) {
			// Split by the last dot to separate class and method
			int methodIndex = mainTypeName.lastIndexOf('.');
			String classPart = methodIndex > 0 ? mainTypeName.substring(0, methodIndex) : mainTypeName;
			String methodName = methodIndex > 0 ? mainTypeName.substring(methodIndex + 1) : null;

			// Split class part by the last dot to get package and class name
			int classIndex = classPart.lastIndexOf('.');
			String packageName = classIndex > 0 ? classPart.substring(0, classIndex) : "";
			String className = classIndex > 0 ? classPart.substring(classIndex + 1) : classPart;

			return new FqnParts(packageName, className, methodName);
		} else {
			throw new IllegalArgumentException("Main type name cannot be null.");
		}
	}

	private ResultFormatType getOutputFileType(String outputFileTypeString) {
		return ResultFormatType.valueOf(outputFileTypeString);
	}
	
	private void createBenchmarkJar(String projectPath, String jarOutputPath) {
	    File metaInfFolder = new File(projectPath, "bin/META-INF");
	    File libFolder = new File(projectPath, "lib");
	    File jarFile = new File(jarOutputPath, "target/benchmarks.jar");

	    Utils.logInfo("Creating JAR file for JMH at: " + jarOutputPath);
	    Utils.logInfo("META-INF folder: " + metaInfFolder.getAbsolutePath());

	    // Dynamically find the JMH generated folder
	    File aptGeneratedFolder = findJmhGeneratedFolder(new File(projectPath, ".apt_generated"));
	    if (aptGeneratedFolder == null) {
	        aptGeneratedFolder = findJmhGeneratedFolder(new File(projectPath, ".apt_generated_test"));
	    }

	    if (aptGeneratedFolder == null) {
	        Utils.logError(".apt_generated folder with JMH generated code not found", 
	                new IOException(".apt_generated folder with JMH generated code not found"));
	        return;
	    }

	    Utils.logInfo("JMH generated folder: " + aptGeneratedFolder.getAbsolutePath());

	    // Validate META-INF
	    if (!validateFolder(metaInfFolder, "META-INF")) {
	        Utils.logError("META-INF folder not found", new IOException("META-INF folder not found"));
	        return;
	    }

	    // Create JAR and add folders
	    try (FileOutputStream fos = new FileOutputStream(jarFile);
	         BufferedOutputStream bos = new BufferedOutputStream(fos);
	         ZipOutputStream zos = new ZipOutputStream(bos)) {

	        addManifestToZip(zos);
	        addFolderToZip(metaInfFolder, metaInfFolder.getParentFile(), zos, "");
	        addFolderToZip(aptGeneratedFolder, aptGeneratedFolder.getParentFile(), zos, "benchmark/");
	        extractAndAddJarsToZip(libFolder, zos);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    System.out.println("Jar file created at: " + jarFile.getAbsolutePath());
	}

	/**
	 * Recursively finds a folder containing 'jmh_generated' inside the given root directory.
	 */
	private File findJmhGeneratedFolder(File rootDir) {
	    if (!rootDir.exists() || !rootDir.isDirectory()) {
	        return null;
	    }

	    File[] files = rootDir.listFiles();
	    if (files != null) {
	        for (File file : files) {
	            if (file.isDirectory()) {
	                if (file.getName().equals("jmh_generated")) {
	                    return file;
	                } else {
	                    File found = findJmhGeneratedFolder(file);
	                    if (found != null) {
	                        return found;
	                    }
	                }
	            }
	        }
	    }
	    return null;
	}	

	private boolean validateFolder(File folder, String name) {
	    if (!folder.exists() || !folder.isDirectory()) {
	        System.err.println(name + " folder not found at: " + folder.getAbsolutePath());
	        return false;
	    }
	    return true;
	}

	private void addFolderToZip(File folder, File baseFolder, ZipOutputStream zos, String prefix) throws IOException {
		Utils.logInfo("Adding " + folder.getAbsolutePath() + " to JAR file");
	    File[] files = folder.listFiles();
	    if (files == null) return;

	    for (File file : files) {
	        String entryName = prefix + file.getAbsolutePath().substring(baseFolder.getAbsolutePath().length() + 1);

	        if (file.isDirectory()) {
	            addFolderToZip(file, baseFolder, zos, prefix);
	        } else {
	            try (FileInputStream fis = new FileInputStream(file);
	                 BufferedInputStream bis = new BufferedInputStream(fis)) {
	                
	                ZipEntry entry = new ZipEntry(entryName);
	                zos.putNextEntry(entry);
	                byte[] buffer = new byte[1024];
	                int bytesRead;
	                while ((bytesRead = bis.read(buffer)) != -1) {
	                    zos.write(buffer, 0, bytesRead);
	                }
	                zos.closeEntry();
	            }
	        }
	    }
	}
	
	private void extractAndAddJarsToZip(File libFolder, ZipOutputStream zos) throws IOException {
	    File[] jarFiles = libFolder.listFiles((dir, name) -> name.endsWith(".jar"));
	    if (jarFiles == null) return;

	    Set<String> addedEntries = new HashSet<>();
	    
	    for (File jarFile : jarFiles) {
	        try (JarFile jar = new JarFile(jarFile)) {
	            Enumeration<JarEntry> entries = jar.entries();
	            while (entries.hasMoreElements()) {
	                JarEntry entry = entries.nextElement();
	                String entryName = entry.getName();
	                
	                if ((entryName.startsWith("org/openjdk/jmh") || 
	                    entryName.startsWith("org/apache/commons/math3") || 
	                    entryName.startsWith("joptsimple")) && !addedEntries.contains(entryName)) {
	                    
	                    zos.putNextEntry(new ZipEntry(entryName));
	                    addedEntries.add(entryName);
	                    
	                    try (InputStream is = jar.getInputStream(entry)) {
	                        byte[] buffer = new byte[1024];
	                        int bytesRead;
	                        while ((bytesRead = is.read(buffer)) != -1) {
	                            zos.write(buffer, 0, bytesRead);
	                        }
	                    }
	                    zos.closeEntry();
	                }
	            }
	        }
	    }
	}	
	
	private void addManifestToZip(ZipOutputStream zos) throws IOException {
	    ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
	    zos.putNextEntry(manifestEntry);
	    String manifestContent = "Manifest-Version: 1.0\n" +
	                             "Build-Jdk-Spec: 1.8\n" +
	                             "Created-By: JMH Eclipse Plugin\n" +
	                             "Main-Class: org.openjdk.jmh.Main\n";
	    zos.write(manifestContent.getBytes(StandardCharsets.UTF_8));
	    zos.closeEntry();
	}
}
