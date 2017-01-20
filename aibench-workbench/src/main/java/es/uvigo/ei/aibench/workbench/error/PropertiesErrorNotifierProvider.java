package es.uvigo.ei.aibench.workbench.error;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.workbench.Workbench;

public class PropertiesErrorNotifierProvider implements ErrorNotifierProvider {
	private final static Logger LOG = Logger.getLogger(PropertiesErrorNotifierProvider.class.getName());
	
	public static final String PROPERTY_ERROR_NOTIFIER_CLASS = "error.notifier.class";
	public static final String VALUE_USE_DEFAULT = "default";
	
	public static String getErrorNotifierClassName() {
		return Workbench.CONFIG.getProperty(PROPERTY_ERROR_NOTIFIER_CLASS);
	}

	@Override
	public ErrorNotifier createErrorNotifier() {
		if (Workbench.CONFIG.containsKey(PROPERTY_ERROR_NOTIFIER_CLASS)) {
			final String enClassName = Workbench.CONFIG.getProperty(PROPERTY_ERROR_NOTIFIER_CLASS);
			
			if (VALUE_USE_DEFAULT.equals(enClassName)) {
				return new DefaultErrorNotifierProvider().createErrorNotifier();
			} else {
				try {
					final Class<?> enClass = Class.forName(enClassName);
					
					if (ErrorNotifier.class.isAssignableFrom(enClass)) {
						return (ErrorNotifier) enClass.newInstance();
					} else {
						throw new ClassCastException(String.format("The custom error notifier class (%s) is not a valid (%s) implementation.",
							enClassName, ErrorNotifier.class.getName()));
					}
				} catch (Exception e) {
					LOG.error("Error creating the error notifier configured in the Workbench configuration: " + enClassName, e);
					
					return null;
				}
			}
		} else {
			return null;
		}
	}

}
