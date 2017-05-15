/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.platonos.pluginengine;

import java.util.HashMap;
import java.util.Map;

import org.platonos.pluginengine.logging.LoggerLevel;

/**
 * Adds functionality to an ExtensionPoint or extends functionality that an ExtensionPoint provides. Extensions belong to a Plugin
 * and "extend" an ExtensionPoint by specifying the ExtensionPoint in the plugin.xml file. Here is an example: <br>
 * <br>
 * <tt>&lt;extensions&gt;&lt;extension uid="app" name="example"/&gt;&lt;/extensions&gt;</tt><br>
 * <br>
 * This adds an empty Extension to the "example" ExtensionPoint in the "app" Plugin. Extensions can be useful in two ways. The
 * Extension can provide an extension class name, a bit of XML, or both. <br>
 * <br>
 * Here is an Extension that has an associated class: <br>
 * <br>
 * <tt>&lt;extensions&gt;&lt;extension uid="app" name="example" class="com.something.MyClass"/&gt;&lt;/extensions&gt;</tt><br>
 * <br>
 * The extension class is instantiated the first time {@link #getExtensionInstance}is invoked. Due to classloading issues, an
 * extension class cannot access package-private classes and members. The ExtensionPoint can optionally enfore extension classes
 * to implement or extend a specific Class (see {@link ExtensionPoint}).<br>
 * <br>
 * Here is an Extension that has associated XML: <br>
 * <br>
 * <tt>&lt;extensions&gt;&lt;extension uid="app" name="example"&gt;&lt;somenode someattribute="12"/&gt;&lt;/extension&gt;&lt;/extensions&gt;</tt>
 * <br>
 * <br>
 * The attributes and XML within the "extension" element are not restricted in any way except that they must be valid XML. The
 * ExtensionPoint should document exactly the XML format it requires. An Extension's attributes and XML are accessed through
 * {@link #getExtensionXmlNode}.<br>
 * <br>
 * If no Dependency exists on the other Plugin that the Extension attaches to, then an optional Dependency will be created on the
 * other Plugin.
 * @see Plugin
 * @see ExtensionPoint
 * @see Dependency
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public class Extension {
	private final Plugin plugin;
	private final String extensionPointPluginUID;
	private final String extensionPointName;
	private final String extensionClassName;
	private ExtensionPoint resolvedToExtensionPoint;
	private Object extensionInstance;
	private PluginXmlNode extensionXmlNode;
	private String extensionXml;
	private Map<String, Object> singleClassInstances;
	ExtensionClassLoader extensionClassLoader;

	public Extension (Plugin plugin, String extensionPointPluginUID, String extensionPointName) {
		this(plugin, extensionPointPluginUID, extensionPointName, null);
	}

	public Extension (Plugin plugin, String extensionPointPluginUID, String extensionPointName, String extensionClassName) {
		if (extensionPointPluginUID == null || extensionPointPluginUID.length() == 0)
			throw new IllegalArgumentException("Invalid argument: extensionPointPluginUID");
		if (extensionPointName == null || extensionPointName.length() == 0)
			throw new IllegalArgumentException("Invalid argument: extensionPointName");

		this.plugin = plugin;
		this.extensionPointPluginUID = extensionPointPluginUID;
		this.extensionPointName = extensionPointName;
		this.extensionClassName = extensionClassName;

		extensionClassLoader = new ExtensionClassLoader(this);
	}

	/**
	 * @return the class name of the extension class or null if there is no extension class. The "class" attribute is optional for
	 * the "extension" element in the plugin.xml.
	 */
	public String getExtensionClassName () {
		return extensionClassName;
	}

	/**
	 * @return the extension class or null if there is no extension class.
	 * @see #getExtensionClassName()
	 */
	public Class<?> getExtensionClass () {
		if (extensionClassName == null) return null;
		// Use the PluginClassLoader rather than the ExtensionClassLoader. The PCL walks through its list of ECLs sequentially, which
		// means that multiple Extensions using the same class will always find the class in the same ECL, even if that ECL is not
		// the ECL for the Extension.
		try {
			return plugin.pluginClassloader.loadClass(extensionClassName);
		} catch (ClassNotFoundException ex) {
			getPluginEngine().getLogger().log(
				LoggerLevel.WARNING,
				"Extension class from Plugin \"" + getPlugin() + "\" for ExtensionPoint \"" + extensionPointPluginUID + ", "
					+ extensionPointName + "\" was not found.", null);
			return null;
		}
	}

	/**
	 * @return the extension class instance or null if there is no extension class. The Extension instance is not created until
	 * this method is called for the first time. The extension class will only be created once for an Extension. If an instance is
	 * returned, the Extension's Plugin is sure to be started by the Class access. If null is returned, the Extension's Plugin may
	 * or may not be started. Due to classloading issues, an extension class cannot access package-private classes and members.
	 * @see #getExtensionClassName()
	 * @see Plugin#start()
	 */
	public synchronized Object getExtensionInstance () {
		if (resolvedToExtensionPoint == null) return null;
		if (extensionInstance != null) return extensionInstance;

		Class<?> extensionClass = getExtensionClass();

		if (!resolvedToExtensionPoint.isExtensionClassCompatible(extensionClass)) {
			if (extensionClass == null) {
				getPluginEngine().getLogger().log(
					LoggerLevel.SEVERE,
					"Required extension class from Plugin \"" + getPlugin() + "\" for ExtensionPoint \"" + extensionPointPluginUID + ", "
						+ extensionPointName + "\" was not found.", null);
			} else {
				getPluginEngine().getLogger().log(
					LoggerLevel.SEVERE,
					"Extension class from Plugin \"" + getPlugin() + "\" for ExtensionPoint \"" + extensionPointPluginUID + ", "
						+ extensionPointName + "\" does not implement ExtensionPoint interface: "
						+ resolvedToExtensionPoint.getInterfaceClassName(), null);
			}
			return null;
		}

		if (extensionClass == null) return null;

		try {
			extensionInstance = extensionClass.newInstance();
		} catch (InstantiationException ex) {
			getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
				"Could not instantiate extension class: " + getExtensionClassName(), ex);
			return null;
		} catch (IllegalAccessException ex) {
			getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
				"Could not instantiate extension class: " + getExtensionClassName(), ex);
			return null;
		}
		return extensionInstance;
	}

	/**
	 * @return the Plugin that this Extension belongs to.
	 */
	public Plugin getPlugin () {
		return plugin;
	}

	/**
	 * @return the PluginEngine instance that this Extension belongs to.
	 */
	public PluginEngine getPluginEngine () {
		return plugin.getPluginEngine();
	}

	/**
	 * @return the ExtensionPoint that this Extension is resolved to or null if it is not yet resolved.
	 */
	public ExtensionPoint getExtensionPoint () {
		return resolvedToExtensionPoint;
	}

	/**
	 * Resolves this Extension to the specified ExtensionPoint.
	 * 
	 * @param resolvedToExtensionPoint the ExtensionPoint to which this Extension is resolved.
	 */
	void resolve (ExtensionPoint resolvedToExtensionPoint) {
		if (this.resolvedToExtensionPoint != null)
			throw new IllegalStateException("Extension is already resolved to an ExtensionPoint.");
		this.resolvedToExtensionPoint = resolvedToExtensionPoint;
		resolvedToExtensionPoint.addResolvedExtension(this);
		resolvedToExtensionPoint.getPlugin().extensionResolved(this);
	}

	/**
	 * Unresolves this Extension with its ExtensionPoint.
	 */
	void unresolve () {
		if (resolvedToExtensionPoint == null)
			throw new IllegalStateException("Extension is not resolved to an ExtensionPoint.");
		resolvedToExtensionPoint.getPlugin().extensionUnresolved(this);
		resolvedToExtensionPoint.removeResolvedExtension(this);
		resolvedToExtensionPoint = null;
		extensionInstance = null;
		extensionClassLoader = new ExtensionClassLoader(this);
	}

	/**
	 * @return the XML that represents the "extension" XML element and its child elements in the plugin.xml file. This is more
	 * efficient than calling <code>getExtensionXmlNode().toXML()</code>.
	 */
	public String getExtensionXml () {
		return extensionXml;
	}

	/**
	 * @return the PluginXmlNode that represents the "extension" XML element in the plugin.xml file. This allows for use of the XML
	 * provided by the Extension.
	 */
	public PluginXmlNode getExtensionXmlNode () {
		return extensionXmlNode;
	}

	/**
	 * Sets the root PluginXmlNode for this Extension.
	 * 
	 * @param extensionXmlNode the PluginXmlNode for this extension.
	 */
	public void setExtensionXmlNode (PluginXmlNode extensionXmlNode) {
		if (extensionXmlNode == null) throw new NullPointerException("Invalid argument: extensionXmlNode");
		this.extensionXmlNode = extensionXmlNode;
		extensionXml = extensionXmlNode.toXML();
	}

	/**
	 * @return the Plugin UID of the ExtensionPoint that this Extension resolves to.
	 */
	public String getExtensionPointPluginUID () {
		return extensionPointPluginUID;
	}

	/**
	 * @return the name of the ExtensionPoint that this Extension resolves to.
	 */
	public String getExtensionPointName () {
		return extensionPointName;
	}

	/**
	 * @param className the name of the class of the returned instance.
	 * @return a new instance of the specified class name using the classloader of this Extension's Plugin. Use this method to
	 * obtain a new instance of a class defined in this Extension's Plugin. This is often done when the Extension provides
	 * classnames within its XML (see {@link #getExtensionXmlNode()}).
	 * @throws ClassNotFoundException if the provided class could not be loaded.
	 * @throws InstantiationException if if was not possible to create an instance of the provided class.
	 * @throws IllegalAccessException if the class or its nullary constructor is not accessible. 
	 */
	public Object getNewInstance (String className) throws ClassNotFoundException, InstantiationException,
		IllegalAccessException {
		Class<?> clazz = Class.forName(className, true, getPlugin().pluginClassloader);
		return clazz.newInstance();
	}

	/**
	 * @param className the name of the class of the returned instance.
	 * @return an instance of the specified class name using the classloader from this Extension's Plugin. Subsequent calls with
	 * the same class name will return the same instance.
	 * @throws ClassNotFoundException if the provided class could not be loaded.
	 * @throws InstantiationException if if was not possible to create an instance of the provided class.
	 * @throws IllegalAccessException if the class or its nullary constructor is not accessible. 
	 * @see #getNewInstance(String)
	 */
	public Object getSingleInstance (String className) throws ClassNotFoundException, InstantiationException,
		IllegalAccessException {
		if (singleClassInstances == null) singleClassInstances = new HashMap<String, Object>(8);
		Object instance = singleClassInstances.get(className);
		if (instance == null) {
			instance = getNewInstance(className);
			singleClassInstances.put(className, instance);
		}
		return instance;
	}

	public String toString () {
		return "Plugin: " + plugin.getUID() + ", ExtensionPoint: " + extensionPointPluginUID + ", " + extensionPointName;
	}
}
