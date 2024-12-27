package com.example.jmhplugin.runconfig.ui;

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
	private Text outputFileNameText;
	private Combo fileTypeCombo;
	private Button browseButton;

    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(5).applyTo(mainComposite); 
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
        setControl(mainComposite);
        
        // Output Label
        Label outputLabel = new Label(mainComposite, SWT.NONE);
        outputLabel.setText("Output Folder / File Name / Type:");
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(5,1).applyTo(outputLabel);

        // Path Folder Text Field
        outputFileText = new Text(mainComposite, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(outputFileText);
        
        Label slash = new Label(mainComposite, SWT.NONE);
        slash.setText("/");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(false, false).applyTo(slash);

        // File Name Text Field
        outputFileNameText = new Text(mainComposite, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(outputFileNameText); 
        
        // Add listeners or additional configurations as needed
        outputFileText.addModifyListener(e -> updateLaunchConfigurationDialog());
        outputFileNameText.addModifyListener(e -> updateLaunchConfigurationDialog());
        
        // File Type Selection
        fileTypeCombo = createFileTypeTropDown(mainComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(fileTypeCombo); 

        // Browse Button
        browseButton = createFileButton(mainComposite, "Browse...", () -> {
            handleBrowse(); // Custom logic for browsing
        });
    }

    private Combo createFileTypeTropDown(Composite mainComposite) {
		Combo combo = new Combo(mainComposite, SWT.READ_ONLY);
		combo.setItems(Arrays.stream(ResultFormatType.values()).map(ResultFormatType::name).toArray(String[]::new));
		combo.select(0);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(combo);
		
		combo.addModifyListener(e -> {
            updateLaunchConfigurationDialog(); 
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
        text.addModifyListener(e -> { updateLaunchConfigurationDialog(); });
        return text;
    }

    private Button createFileButton(Composite parent, String text, Runnable onClickAction) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(button);
        button.addListener(SWT.Selection, event -> {
            onClickAction.run();
            updateLaunchConfigurationDialog();
        });
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
			configuration.setAttribute("outputFolderPath", "default-output-directory");
			configuration.getAttribute("absoluteProjectPath", "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }

    @Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fileTypeCombo.setText(configuration.getAttribute("outputFileType", "JSON"));
			outputFileText.setText(configuration.getAttribute("outputFolderPath", "default-output-directory"));
			outputFileNameText.setText(configuration.getAttribute("outputFileName", "default-file-name"));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

    @Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute("outputFolderPath", outputFileText.getText());
		configuration.setAttribute("outputFileType", fileTypeCombo.getText());
		configuration.setAttribute("outputFileName", outputFileNameText.getText());
	}

    @Override
    public String getName() {
        return "JMH Launch Tab";
    }
}

