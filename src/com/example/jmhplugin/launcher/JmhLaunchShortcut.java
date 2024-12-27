package com.example.jmhplugin.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class JmhLaunchShortcut implements ILaunchShortcut2 {
	
	private static String LAUNCH_CONFIG_TYPE = "com.example.jmhplugin.launchconfigType";

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Launch shortcut activated: " + selection);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		if (element != null) {
			IMember selectedMember = resolveSelectedMemberName(editor, element);

			if (selectedMember != null) {
				launch(selectedMember, mode);
			} 
		}
	}
	
	
	private void launch(Object element, String mode) {
		IJavaElement elementToLaunch = null;

		try {
			if (element instanceof IJavaElement) {
				IJavaElement javaElement = (IJavaElement) element;

				if (javaElement.getElementType() == IJavaElement.METHOD || 
						javaElement.getElementType() == IJavaElement.TYPE) {
					elementToLaunch = javaElement;
				}
			}
			performLaunch(elementToLaunch, mode);
		} catch (InterruptedException | CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	private void performLaunch(IJavaElement element, String mode) throws InterruptedException, CoreException {
		ILaunchConfigurationWorkingCopy launchConfiguration = createLaunchConfiguration(element, null);
		ILaunchConfiguration config = findExistingLaunchConfiguration(launchConfiguration, mode);
		
		if (config == null ) {
			config = launchConfiguration.doSave();
		}
		
		DebugUITools.launch(config, ILaunchManager.RUN_MODE);
		NullProgressMonitor monitor = new NullProgressMonitor();
		DebugUITools.buildAndLaunch(config, ILaunchManager.RUN_MODE, monitor);
	}
	
	
	
	
	private ILaunchConfiguration findExistingLaunchConfiguration(ILaunchConfigurationWorkingCopy wc, String mode) throws CoreException, InterruptedException {
		List<ILaunchConfiguration> candidateConfigs = findExistingLaunchConfigurations(wc);
		
		if (candidateConfigs.size() == 1) {
			return candidateConfigs.get(0);
		} else if (candidateConfigs.size() > 1) {
			return chooseConfiguration(candidateConfigs, mode);
		}
		return null;
	}
	
	private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) throws InterruptedException {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle("Choose Launch Configuration");
		dialog.setMessage("Choose a existing launch configuration");

		dialog.setMultipleSelection(false);
		int result= dialog.open();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		throw new InterruptedException(); // cancelled by user
	}
	
	private Shell getShell() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		return workbench.getActiveWorkbenchWindow().getShell();
	}

	private List<ILaunchConfiguration> findExistingLaunchConfigurations(ILaunchConfigurationWorkingCopy wc)
			throws CoreException {
		ILaunchConfiguration[] launchConfigs = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurations(wc.getType());
		String candiateProjectName = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		String candiateMainTypeName = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");

		ArrayList<ILaunchConfiguration> candidateConfigs = new ArrayList<>(launchConfigs.length);
		for (ILaunchConfiguration launchConfig : launchConfigs) {
			String projectName = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			String mainTypeName = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
			if (projectName.equals(candiateProjectName) && mainTypeName.equals(candiateMainTypeName)) {
				candidateConfigs.add(launchConfig);
			}
		}
		return candidateConfigs;
	}
	
	private ILaunchConfiguration[] findExistingLaunchConfigurations(Object candidate) {
		if (!(candidate instanceof IJavaElement) && candidate instanceof IAdaptable) {
			candidate = ((IAdaptable) candidate).getAdapter(IJavaElement.class);
		}
		if (candidate instanceof IJavaElement) {
			IJavaElement element = (IJavaElement) candidate;
			IJavaElement elementToLaunch = null;
			try {
				if (element.getElementType() == IJavaElement.METHOD || element.getElementType() == IJavaElement.TYPE) {
					elementToLaunch = element;
				}
				if (elementToLaunch == null) {
					return null;
				}
				ILaunchConfigurationWorkingCopy workingCopy = createLaunchConfiguration(elementToLaunch, null);
				List<ILaunchConfiguration> list = findExistingLaunchConfigurations(workingCopy);
				return list.toArray(new ILaunchConfiguration[list.size()]);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element, String testName) {
		String mainTypeQyalifiedName = "";
		boolean isMethod = false;

		if (element.getElementType() == IJavaElement.METHOD) {
			IMethod method = (IMethod) element;
			testName = method.getElementName();
			IType declaringType = method.getDeclaringType();
			mainTypeQyalifiedName += declaringType.getFullyQualifiedName() + "." + testName;
			isMethod = true;
		}

		if (element.getElementType() == IJavaElement.TYPE) {
			mainTypeQyalifiedName = ((IType) element).getFullyQualifiedName('.');
		}

		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType launchConfigType = launchManager.getLaunchConfigurationType(LAUNCH_CONFIG_TYPE);		
			String configName = launchManager.generateLaunchConfigurationName(suggestLaunchConfigurationName(element, testName));
			ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, configName);

			IPath absoluteProjectPath = element.getJavaProject().getProject().getLocation();
			wc.setAttribute("absoluteProjectPath", absoluteProjectPath.toOSString() );
			wc.setAttribute("isMethod", isMethod);
			wc.setAttribute("outputFolderPath", absoluteProjectPath.toOSString());
			wc.setAttribute("outputFileName", configName);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQyalifiedName);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					element.getJavaProject().getElementName());

			return wc;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String suggestLaunchConfigurationName(IJavaElement element, String fullTestName) {
		switch (element.getElementType()) {
		case IJavaElement.METHOD:
			IMethod method = (IMethod) element;
			String methodName = method.getElementName();
			return method.getDeclaringType().getElementName() + "." + methodName;
		case IJavaElement.TYPE:
			return ((IType) element).getElementName();
		default:
			throw new IllegalArgumentException("Unexpected value: " + element.getClass().getName());
		}
	}
	
	/**
	 * Resolves the member name of the selected element in the editor
	 * 
	 * @param editor  the editor
	 * @param element the element
	 * @return the member of the selected element in the editor
	 */
	private IMember resolveSelectedMemberName(IEditorPart editor, ITypeRoot element) {
		try {
			ISelectionProvider selectionProvider = editor.getSite().getSelectionProvider();
			if (selectionProvider == null)
				return null;

			ISelection selection = selectionProvider.getSelection();
			if (!(selection instanceof ITextSelection))
				return null;

			ITextSelection textSelection = (ITextSelection) selection;

			IJavaElement elementAtOffset = SelectionConverter.getElementAtOffset(element, textSelection);
			if (!(elementAtOffset instanceof IMethod) && !(elementAtOffset instanceof IType))
				return null;

			IMember member = (IMember) elementAtOffset;

			ISourceRange nameRange = member.getNameRange();
			if (nameRange.getOffset() <= textSelection.getOffset() && textSelection.getOffset()
					+ textSelection.getLength() <= nameRange.getOffset() + nameRange.getLength())
				return member;
		} catch (JavaModelException e) {
			// ignore
		}
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				return findExistingLaunchConfigurations(ss.getFirstElement());
			}
		}
		return null;
	}


	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editor) {
		final ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());

		if (element != null) {
			IMember selectedMember = null;
			if (Display.getCurrent() == null) {
				final AtomicReference<IMember> temp = new AtomicReference<>();
				Runnable runnable = () -> temp.set(resolveSelectedMemberName(editor, element));
				Display.getDefault().syncExec(runnable);
				selectedMember = temp.get();
			} else {
				selectedMember = resolveSelectedMemberName(editor, element);
			}
			Object candidate = element;
			if (selectedMember != null) {
				candidate = selectedMember;
			}
			return findExistingLaunchConfigurations(candidate);
		}
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		System.out.println("getLaunchableResource: " + selection);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		System.out.println("getLaunchableResource: " + editorpart);
		// TODO Auto-generated method stub
		return null;
	}
}
