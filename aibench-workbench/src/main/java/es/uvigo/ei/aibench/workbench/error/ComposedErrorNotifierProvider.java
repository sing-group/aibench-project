package es.uvigo.ei.aibench.workbench.error;

public class ComposedErrorNotifierProvider implements ErrorNotifierProvider {

	@Override
	public ErrorNotifier createErrorNotifier() {
		ErrorNotifier errorNotifier = new PropertiesErrorNotifierProvider().createErrorNotifier();
		
		if (errorNotifier == null)
			errorNotifier = new PluginErrorNotifierProvider().createErrorNotifier();
		
		if (errorNotifier == null)
			errorNotifier = new DefaultErrorNotifierProvider().createErrorNotifier();
		
		return errorNotifier;
	}

}
