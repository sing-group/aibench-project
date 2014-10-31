
package org.platonos.pluginengine;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.platonos.pluginengine.logging.LoggerLevel;
import org.xml.sax.SAXParseException;

/**
 * Hook for adding or extending functionality through Extensions. ExtensionPoints belong to a Plugin and are defined in the
 * plugin.xml file. A Plugin is not required to define ExtensionPoints. ExtensionPoints have a name that must be unique within the
 * Plugin. Here is an example: <br>
 * <br>
 * <tt>&lt;extensionpoints&gt;&lt;extensionpoint name="startup" interface="example.IStartup"/&gt;&lt;/extensionpoints&gt;</tt>
 * <br>
 * <br>
 * The optional "interface" attribute defines a class that all Extensions must implement or extend. <br>
 * <br>
 * Other Plugins may attach Extensions to an ExtensionPoint. The ExtensionPoint's Plugin or any other code in the application can
 * obtain the ExtensionPoint by using its name and Plugin UID and make use of the attached Extensions by parsing the Extension's
 * XML ({@link Extension#getExtensionXmlNode}), using the extension class instance ({@link Extension#getExtensionInstance}),
 * or both.
 * @see Plugin
 * @see Extension
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public class ExtensionPoint {
	private final Plugin plugin;

	private final String name;

	private final String interfaceClassName;

	private final List<Extension> resolvedExtensions = new ArrayList<Extension>();

	private String schemaFilename;

	public ExtensionPoint (Plugin plugin, String name) {
		this(plugin, name, null);
	}

	public ExtensionPoint (Plugin plugin, String name, String interfaceClassName) {
		if (plugin == null) {
			throw new NullPointerException("Invalid argument: plugin");
		}

		if (plugin.getUID() == null) {
			throw new IllegalStateException("The plugin's UID must be set: " + plugin);
		}

		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Invalid argument: name");
		}

		this.plugin = plugin;
		this.name = name;
		this.interfaceClassName = interfaceClassName;
	}

	public void setSchemaFilename (String schemaFilename) {
		if (schemaFilename == null) {
			throw new NullPointerException("Invalid argument: schemaFilename");
		}
		this.schemaFilename = schemaFilename;
	}

	/**
	 * Returns the name of the class all extension classes are required to implement or null if they are not required to implement
	 * a specific class.
	 */
	public String getInterfaceClassName () {
		return interfaceClassName;
	}

	/**
	 * Returns the class all extension classes are required to implement or null if they are not required to implement a specific
	 * class.
	 */
	public Class<?> getInterfaceClass () {
		if (getInterfaceClassName() == null) {
			return null;
		}

		try {
			return Class.forName(getInterfaceClassName(), true, getPlugin().pluginClassloader);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Returns the Plugin that this ExtensionPoint is defined in.
	 */
	public Plugin getPlugin () {
		return plugin;
	}

	/**
	 * Returns the PluginEngine instance that this ExtensionPoint is associated with.
	 */
	public PluginEngine getPluginEngine () {
		return plugin.getPluginEngine();
	}

	/**
	 * Returns the name of this ExtensionPoint. This is used to obtain a reference to this ExtensionPoint through the Plugin.
	 */
	public String getName () {
		return name;
	}

	public String toString () {
		return getName();
	}

	/**
	 * Returns a List of Extensions that have resolved to this ExtensionPoint.
	 */
	public List<Extension> getExtensions () {
		return new ArrayList<Extension>(resolvedExtensions);
	}

	/**
	 * Resolves an Extension to this ExtensionPoint.
	 */
	void addResolvedExtension (Extension extension) {
		if (resolvedExtensions.contains(extension))
			throw new IllegalArgumentException("Extension is already resolved to this ExtensionPoint.");

		resolvedExtensions.add(extension);
	}

	/**
	 * Unresolves an Extension from this ExtensionPoint.
	 */
	void removeResolvedExtension (Extension extension) {
		if (!resolvedExtensions.remove(extension))
			throw new IllegalArgumentException("Extension is not resolved to this ExtensionPoint.");
	}

	/**
	 * Returns true if the specified Extension is compatible with this ExtensionPoint. This method accesses the extension class,
	 * which will cause the Extension's Plugin to be started if the Extension has an extension class.
	 */
	public boolean isExtensionCompatible (Extension extension) {
		if (extension == null) {
			throw new NullPointerException("Invalid argument: extension");
		}

		if (!isExtensionClassCompatible(extension.getExtensionClass())) {
			return false;
		}

		if (!isExtensionXmlCompatible(extension)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the specified extension class is compatible with this ExtensionPoint's interface class.
	 */
	boolean isExtensionClassCompatible (Class<?> extensionClass) {
		// If the extension point has an interface specified then the extension's
		// class must be assignable from it.
		Class<?> interfaceClass = getInterfaceClass();
		if (interfaceClass != null) {
			if (extensionClass == null) {
				return false;
			}
			if (!interfaceClass.isAssignableFrom(extensionClass)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the specified Extension's XML is compatible with this ExtensionPoint's XML schema.
	 */
	boolean isExtensionXmlCompatible (Extension extension) {
		if (schemaFilename != null) {
			URL schemaURL = plugin.pluginClassloader.getResource(schemaFilename);
			if (schemaURL == null) {
				getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
					"Unable to find schema for ExtensionPoint \"" + this + "\": " + schemaFilename, null);
				return false;
			}
			try {
				PluginXmlParser.validate(getPluginEngine(), schemaURL, extension.getExtensionXml());
			} catch (SAXParseException ex) {
				getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
					"Extension XML failed schema validation for ExtensionPoint \"" + this + "\": " + extension, ex);
				return false;
			}
		}
		return true;
	}
}
