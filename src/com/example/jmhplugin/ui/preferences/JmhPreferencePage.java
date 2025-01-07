package com.example.jmhplugin.ui.preferences;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.example.jmhplugin.JmhPlugin;

public class JmhPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {


    public JmhPreferencePage() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPath workspaceRootPath = workspace.getRoot().getLocation();
        getPreferenceStore().setDefault("jmhDefaultOutputPath", workspaceRootPath.toString());
        addField(new DirectoryFieldEditor("jmhDefaultOutputPath", "JMH output location", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        IPreferenceStore preferenceStore = JmhPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(preferenceStore);
    }
    
    @Override
    public boolean performOk() {
        boolean result = super.performOk();

        // Log the updated value
        String updatedPath = getPreferenceStore().getString("jmhDefaultOutputPath");
        System.out.println("Preference updated in JmhPreferencePage: " + updatedPath);
        // Or use a logger
        // Logger.getLogger(JmhPreferencePage.class.getName()).info("Preference updated in JmhPreferencePage: " + updatedPath);
        return result;
    }
}
