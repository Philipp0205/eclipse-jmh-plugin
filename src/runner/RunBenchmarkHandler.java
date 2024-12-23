package runner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RunBenchmarkHandler extends AbstractHandler {

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        try {
            // Extract the fully qualified class and method name from the stack trace
            String fullyQualifiedName = getCallingClassAndMethodName();
            if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
                MessageDialog.openError(shell, "Build Benchmark", "Class and method name could not be determined.");
                return null;
            }

            // Display the fully qualified name
            MessageDialog.openInformation(shell, "Build Benchmark", "Fully Qualified Name: " + fullyQualifiedName);
        } catch (Exception e) {
            MessageDialog.openError(shell, "Build Benchmark", "An error occurred: " + e.getMessage());
        }
        return null;
    }

    private String getCallingClassAndMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // The calling class and method are at index 3 in the stack trace
        if (stackTrace.length > 3) {
            StackTraceElement element = stackTrace[3];
            return element.getClassName() + " " + element.getMethodName();
        }
        return null;
    } 
}
