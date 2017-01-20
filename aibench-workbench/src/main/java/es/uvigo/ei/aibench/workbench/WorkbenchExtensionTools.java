package es.uvigo.ei.aibench.workbench;

import java.util.ArrayList;
import java.util.List;

import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.ExtensionPoint;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginXmlNode;

import es.uvigo.ei.aibench.workbench.error.ErrorNotifier;

public final class WorkbenchExtensionTools {
	public static final String EXTENSION_POINT_UID_AIBENCH_WORKBENCH_VIEW = "aibench.workbench.view";
	public static final String EXTENSION_POINT_UID_AIBENCH_WORKBENCH_ERROR_NOTIFIER = "aibench.workbench.error.notifier";

	private WorkbenchExtensionTools() {}

	public static List<Extension> getWorkbenchViewExtensions() {
		return getExtensions(EXTENSION_POINT_UID_AIBENCH_WORKBENCH_VIEW);
	}

	public static List<PluginXmlNode> getWorkbenchViewExtensionsChildren() {
		final List<PluginXmlNode> children = new ArrayList<>();
		
		for (Extension extension : getWorkbenchViewExtensions()) {
			children.addAll(extension.getExtensionXmlNode().getChildren());
		}
		
		return children;
	}
	
	public static ErrorNotifier getWorkbenchErrorNotifierExtensions() {
		return getWorkbenchErrorNotifierExtensions(null);
	}
	
	public static ErrorNotifier getWorkbenchErrorNotifierExtensions(String className) {
		for (Extension extension : getExtensions(EXTENSION_POINT_UID_AIBENCH_WORKBENCH_ERROR_NOTIFIER)) {
			if (className == null) {
				return (ErrorNotifier) extension.getExtensionInstance();
			} else if (extension.getExtensionClass().getName().equals(className)) {
				return (ErrorNotifier) extension.getExtensionInstance();
			}
		}
		
		return null;
	}
	
	private static List<Extension> getExtensions(String name) {
		final Plugin plugin = PluginEngine.getPlugin(WorkbenchExtensionTools.class);
		final ExtensionPoint point = plugin.getExtensionPoint(name);
		
		return point.getExtensions();
	}
}
