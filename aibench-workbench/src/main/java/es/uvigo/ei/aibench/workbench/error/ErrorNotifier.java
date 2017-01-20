package es.uvigo.ei.aibench.workbench.error;

import es.uvigo.ei.aibench.workbench.MainWindow;

public interface ErrorNotifier {
	
	public void showError(MainWindow mainWindow, Throwable exception);
	
	public void showError(MainWindow mainWindow, Throwable exception, String message);
	
}
