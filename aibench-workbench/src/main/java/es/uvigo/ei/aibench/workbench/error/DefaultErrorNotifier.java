package es.uvigo.ei.aibench.workbench.error;

import es.uvigo.ei.aibench.workbench.MainWindow;

public class DefaultErrorNotifier implements ErrorNotifier {
	@Override
	public void showError(MainWindow mainWindow, Throwable exception) {
		new ErrorDialog(mainWindow, exception).setVisible(true);
	}

	@Override
	public void showError(MainWindow mainWindow, Throwable exception, String message) {
		new ErrorDialog(mainWindow, exception, message).setVisible(true);
	}
}
