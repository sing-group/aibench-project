package es.uvigo.ei.aibench.workbench.error;

import es.uvigo.ei.aibench.workbench.WorkbenchExtensionTools;

public class PluginErrorNotifierProvider implements ErrorNotifierProvider {
	
	@Override
	public ErrorNotifier createErrorNotifier() {
		final String className = PropertiesErrorNotifierProvider.getErrorNotifierClassName();
		
		return WorkbenchExtensionTools.getWorkbenchErrorNotifierExtensions(className);
	}

}
