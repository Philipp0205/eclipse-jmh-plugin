package com.example.jmhplugin.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class JmhDynamicMenuContributor extends ContributionItem {
	@Override
    public void fill(Menu menu, int index) {
		System.out.println("filling menu");
        IMethod selectedMethod = getSelectedMethod();

        MenuItem sampleItem = new MenuItem(menu, SWT.PUSH, index);
        sampleItem.setText("test");
        sampleItem.addListener(SWT.Selection, e -> {
        	System.out.println("Test Item selected");
        });

//        if (selectedMethod != null) {
//            try {
//                ILaunchConfiguration existingConfig = findExistingConfiguration(selectedMethod);
//                if (existingConfig != null) {
//                    // Add the existing configuration to the menu
//                    MenuItem item = new MenuItem(menu, SWT.PUSH, index);
//                    item.setText("Run JMH Benchmark (" + existingConfig.getName() + ")");
//                    item.addListener(SWT.Selection, e -> {
//                        try {
//                            existingConfig.launch(ILaunchManager.RUN_MODE, null);
//                        } catch (CoreException ex) {
//                            ex.printStackTrace();
//                        }
//                    });
//                }
//            } catch (CoreException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private IMethod getSelectedMethod() {
        // Implement logic to extract the selected method from the context menu selection
        // This could involve checking the ISelection from the workbench
        return null; // Replace with actual implementation
    }

    private ILaunchConfiguration findExistingConfiguration(IMethod method) throws CoreException {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType("your.launch.config.type");

        for (ILaunchConfiguration config : manager.getLaunchConfigurations(type)) {
            String methodKey = config.getAttribute("methodKey", "");
            if (methodKey.equals(method.getKey())) {
                return config;
            }
        }
        return null;
    }
}
