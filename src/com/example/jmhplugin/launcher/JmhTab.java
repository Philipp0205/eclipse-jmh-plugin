package com.example.jmhplugin.launcher;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmh.results.format.ResultFormatType;

public class JmhTab extends AbstractLaunchConfigurationTab {
	private Text outputFileText;
	private Combo fileTypeCombo;

    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(3).applyTo(mainComposite); // 3 columns
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
        setControl(mainComposite);

        // Output File Selection 
        createLabel(mainComposite, "Output file :");
        outputFileText = createTextInput(mainComposite);
        createFileButton(mainComposite, "Browse...");
        
        // File Type Selection
        createLabel(mainComposite, "File type :");
        fileTypeCombo = createFileTypeTropDown(mainComposite);
    }

    private Combo createFileTypeTropDown(Composite mainComposite) {
		Combo combo = new Combo(mainComposite, SWT.READ_ONLY);
		combo.setItems(Arrays.stream(ResultFormatType.values()).map(ResultFormatType::name).toArray(String[]::new));
		combo.select(0);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(combo);
		
		combo.addModifyListener(e -> {
            setDirty(true);
            updateLaunchConfigurationDialog(); // Notify framework
		});
		return combo;
	}

	private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
        return label;
    }

    private Text createTextInput(Composite parent) {
        Text text = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
        text.addModifyListener(e -> {
            setDirty(true);
            updateLaunchConfigurationDialog(); // Notify framework
        });
        return text;
    }
    
    private Button createFileButton(Composite parent, String text) {
    	Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(button);
        
        button.addListener(SWT.Selection, event -> handleBrowse());
        return button;
    }
    

	private void handleBrowse() {
		DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
		directoryDialog.setMessage("Select a folder for the output file:");
		String selectedDirectory = directoryDialog.open();

		if (selectedDirectory != null) {
			outputFileText.setText(selectedDirectory);
		}
	}

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    	try {
			configuration.getAttribute("absoluteProjectPath", "");
			configuration.setAttribute("outputFilePath", "default-output-directory");
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
    	 try {
             fileTypeCombo.setText(configuration.getAttribute("outputFileType", "JSON")); // Initialize the combo box
         } catch (CoreException e) {
             outputFileText.setText("default-output-directory"); // Fallback default
         }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	try {
			String outputFilePath = configuration.getAttribute("outputFilePath",  "error");
			configuration.setAttribute("outputFilePath", outputFilePath);
		} catch (CoreException e) {
		}
        configuration.setAttribute("outputFileType", fileTypeCombo.getText());
    }

    @Override
    public String getName() {
        return "JMH Launch Tab";
    }
}

