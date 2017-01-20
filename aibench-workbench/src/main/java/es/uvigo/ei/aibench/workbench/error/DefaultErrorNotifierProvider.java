package es.uvigo.ei.aibench.workbench.error;

public class DefaultErrorNotifierProvider implements ErrorNotifierProvider {
	
	@Override
	public ErrorNotifier createErrorNotifier() {
		return new DefaultErrorNotifier();
	}

}
