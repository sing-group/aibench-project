/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
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

/**
 * Handles lifecycle events for a Plugin. This class allows a Plugin to have code executed before any of the Plugin's classes can
 * be accessed. It allows a Plugin to cleanup when it is stopped, which is essential for the Plugin to be unloaded/reloaded
 * properly. It also allows a Plugin to react to Extensions that are resolved after the Plugin has been started. Not all Plugins
 * require a PluginLifecycle, such as plugins that provide only extensions or resources such as images or html pages. <br>
 * <br>
 * The PluginLifecycle is constructed and the {@link #initialize() initialize} method is invoked immediately after the Plugin is
 * started. A Plugin is started the first time a Class within the Plugin is accessed or as soon as the Plugin has resolved if the
 * Plugin is set to start when it is resolved (see {@link Plugin#setStartWhenResolved(boolean)}). If a Plugin is started by class
 * access, the class request will block until the initialize method returns.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public abstract class PluginLifecycle {
	private Plugin plugin;

	/**
	 * Returns the Plugin this PluginLifecycle is managing.
	 * 
	 * @return the Plugin this PluginLifecycle is managing.
	 */
	public Plugin getPlugin () {
		if (plugin == null) plugin = PluginEngine.getPlugin(getClass());
		return plugin;
	}

	/**
	 * Returns an ExtensionPoint defined in the Plugin this PluginLifecycle manages or {@code null} if the ExtensionPoint does not exist.
	 * 
	 * @param name the name of the extension point.
	 * @return an ExtensionPoint defined in the Plugin this PluginLifecycle manages or {@code null} if the ExtensionPoint does not exist.
	 */
	public ExtensionPoint getExtensionPoint (String name) {
		return getPlugin().getExtensionPoint(name);
	}

	/**
	 * Returns the PluginEngine of the Plugin this PluginLifecycle is managing.
	 * 
	 * @return the PluginEngine of the Plugin this PluginLifecycle is managing.
	 */
	public PluginEngine getPluginEngine () {
		return getPlugin().getPluginEngine();
	}

	/**
	 * Use this method to do setup that must happen before other Plugins can access this Plugin. This method is invoked each time
	 * the Plugin is started. During this method, classes from other Plugins may be accessed and other Plugins can access this
	 * Plugin's classes, but <b>only </b>from the thread that invoked this method because a lock is kept on both the Plugin and its
	 * classloader. This mechanism allows this PluginLifecycle to perform setup its Plugin needs that requires other Plugins,
	 * before other Plugins can make unwanted use of this Plugin's classes. This method should not block for long.<br>
	 * <br>
	 * Any non-setup work that this Plugin needs should happen in the {@link PluginLifecycle#start()}method because there it will
	 * not have the threading restriction.
	 */
	protected void initialize () {
	}

	/**
	 * Use this method to begin the Plugin's work. This method is invoked in a seperate thread as soon as possible after the
	 * initialize method has returned. Before this method is called, this should be completely setup and ready to use by the
	 * initialize method. This Plugin may access classes from other Plugins and other Plugins may access this Plugin's classes,
	 * there are no restrictions. Note that this method is invoked as soon as possible after the initialize method has returned,
	 * but other Plugins may be able to make use of this Plugin's classes before this method is invoked. Because of this, the
	 * initialize method should be used for any setup that needs to happen before other Plugins have access to this Plugin. This
	 * method is provided as an entry point for the Plugin to begin its work. This method should not block for long.
	 */
	protected void start () {
	}

	/**
	 * Invoked immediately before the Plugin is unresolved. A Plugin is unresolved when it or one of its required Dependencies are
	 * about to be unloaded from the PluginEngine. Clean up must be done to free all resources, especially resources and references
	 * that belong to other Plugins. Handling this clean up is essential for a Plugin to be unloaded properly. If clean up is not
	 * done, this and/or other Plugins will not be able to unload/reload or unresolve/resolve properly.
	 */
	protected void stop () {
	}

	/**
	 * Invoked when an Extension from another Plugin is resolved to an ExtensionPoint that this Plugin provides. This allows code
	 * that manages the ExtensionPoint to take appropriate action when Extensions are resolved. For example, in a GUI application,
	 * when a menu item Extension is resolved, the ExtensionPoint code would want to add the menu item to the UI. <br>
	 * <br>
	 * This method will only be invoked for Extensions resolved after the Plugin is started. Extensions from Plugins that are
	 * required Dependencies are guaranteed to be resovled before this Plugin is started.
	 * @param extensionPoint The ExtensionPoint that the Extension has resolved to.
	 * @param extension The Extension that has resolved.
	 * @see Plugin#getExtensions()
	 * @see Plugin#getExtensionPoints()
	 */
	protected void extensionResolved (ExtensionPoint extensionPoint, Extension extension) {
	}

	/**
	 * Invoked when an Extension from another Plugin is unresolved from an ExtensionPoint that this Plugin provides. This allows
	 * code that manages the ExtensionPoint to take appropriate action when Extensions are removed. For example, in a GUI
	 * application, this could be the removal of an Extension that provides a menu item. The ExtensionPoint code would want to
	 * remove the menu item from the GUI.
	 * @param extensionPoint The ExtensionPoint that the Extension is about to be unresolved from.
	 * @param extension The Extension that is about to be unresolved.
	 */
	protected void extensionUnresolved (ExtensionPoint extensionPoint, Extension extension) {
	}
}
