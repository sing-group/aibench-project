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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.platonos.pluginengine.event.PluginEngineEvent;
import org.platonos.pluginengine.event.PluginEngineEventType;
import org.platonos.pluginengine.logging.LoggerLevel;
import org.platonos.pluginengine.version.PluginInstanceVersion;
import org.platonos.pluginengine.version.PluginVersion;

import es.uvigo.ei.aibench.Util;

/**
 * Provides a feature or ability through Java classes, ExtensionPoints, and/or Extensions. A Plugin is defined in the plugin.xml
 * file. Here is an example plugin.xml with links to the corresponding methods: <br>
 * <br>
 * <tt>&lt;plugin {@link #setStartWhenResolved(boolean) start}="true"&gt;<br>
 * 	&lt;{@link #setName(String) name}&gt;Example Plugin&lt;/{@link #setName(String) name}&gt;<br>
 * 	&lt;{@link #setVersion(PluginVersion) version}&gt;1.0.0&lt;/{@link #setVersion(PluginVersion) version}&gt;<br>
 * 	&lt;{@link #setUID(String) uid}&gt;example&lt;/{@link #setUID(String) uid}&gt;<br>
 * 	&lt;{@link #setLifecycleClassName(String) lifecycleclass}&gt;example.ExamplePluginLifecycle&lt;/{@link #setLifecycleClassName(String) lifecycleclass}&gt;<br>
 * 	&lt;dependencies&gt;<br>
 * 		&lt;{@link #addDependency(Dependency) dependency} uid="example.someOtherPlugin" minversion="0.5" maxversion="1" /&gt;<br>
 * 		&lt;{@link #addDependency(Dependency) dependency} uid="example.otherPlugin" version="1.0" {@link org.platonos.pluginengine.Dependency#setOptional(boolean) optional}="true" /&gt;<br>
 * 	&lt;/dependencies&gt;<br>
 * 	&lt;extensionpoints&gt;<br>
 * 		&lt;{@link #addExtensionPoint(ExtensionPoint) extensionpoint} name="startup" interface="example.IStartup" /&gt;<br>
 * 	&lt;/extensionpoints&gt;<br>
 * 	&lt;extensions&gt;<br>
 * 		&lt;{@link #addExtension(Extension) extension} uid="example.otherPlugin" name="help" class="example.HelpAction" /&gt;<br>
 * 	&lt;/extensions&gt;<br>
 * &lt;/plugin&gt;</tt><br>
 * <br>
 * The "uid" element is required, all other elements and attributes are optional.
 * @see PluginLifecycle
 * @see Dependency
 * @see ExtensionPoint
 * @see Extension
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public class Plugin implements Comparable<Object> {
	private final boolean isArchive;
	private final URL pluginURL;
	private final PluginEngine pluginEngine;
	PluginClassLoader pluginClassloader;
	private final Map<String, ExtensionPoint> extensionPoints = new HashMap<String, ExtensionPoint>(5);
	private final List<Extension> extensions = new ArrayList<Extension>(8);
	private final Map<String, Dependency> dependencies = new HashMap<String, Dependency>(8);
	private final List<Plugin> dependentPlugins = new ArrayList<Plugin>(8);
	private String uid;
	private String name;
	private String lifecycleClassName;
	PluginLifecycle lifecycleInstance;
	private PluginXmlNode metadataXmlNode;
	private boolean isStarted = false;
	boolean isDisabled = false;
	boolean isActive = true; // Lifecycle must be created and started?
	boolean isResolved = false;
	boolean isLoaded = false; // TODO: MIGUEL Check
	private boolean startWhenResolved = false;
	private int startOrder = Integer.MAX_VALUE; // TODO: Miguel check
	private boolean dependentPluginLookup = false;
	private PluginVersion version = new PluginInstanceVersion();
	private final long constructionTime = System.currentTimeMillis();

	/**
	 * Creates a new Plugin that cannot load classes and resources.
	 * 
	 * @param pluginEngine the PluginEngine associated with this Plugin.
	 */
	public Plugin (PluginEngine pluginEngine) {
		this(pluginEngine, null);
	}

	/**
	 * Creates a new Plugin that can load classes and resources at the specified Plugin URL.
	 * 
	 * @param pluginEngine the PluginEngine associated with this Plugin.
	 * @param pluginURL The location this Plugin uses to lookup class files and resources.
	 */
	public Plugin (PluginEngine pluginEngine, URL pluginURL) {
		if (pluginEngine == null)
			throw new NullPointerException("Invalid argument: pluginEngine");
		
		this.pluginEngine = pluginEngine;
		this.pluginURL = pluginURL;
		
		if (pluginURL == null) {
			isArchive = false;
		} else {
			File file = Util.urlToFile(pluginURL);
			if (!file.exists())
				throw new IllegalArgumentException("Plugin URL location does not exist: " + pluginURL);
			isArchive = !file.isDirectory();
		}

		pluginClassloader = PluginClassLoader.createClassLoader(this);
	}

	/**
	 * Starts this Plugin if it is not already started. A Plugin will not start if it has required Dependencies that are not
	 * resolved or if it has an unresolved Extension to a required Dependency.
	 * 
	 * @return whether this Plugin is started or not.
	 */
	public synchronized boolean start () {
		if (isDisabled || !isResolved) return false;
		if (isStarted) return true;

		synchronized (pluginClassloader) {
			getPluginEngine().getLogger().log(LoggerLevel.FINE, "Starting Plugin: " + this, null);

			isStarted = true;

			// Get the PluginLifecycle instance.
			if (lifecycleInstance == null) {
				if (lifecycleClassName != null) {
					// Get PluginLifecycle class.
					Class<?> pluginLifecycleClass;
					try {
						pluginLifecycleClass = pluginClassloader.loadClassFromClassPath(lifecycleClassName);
						if (pluginLifecycleClass == null)
							throw new ClassNotFoundException("Unable to find lifecycle class: " + lifecycleClassName);
					} catch (ClassNotFoundException ex) {
						isStarted = false;
						getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
							"Unable to find lifecycle class for Plugin \"" + this + "\" during start: " + lifecycleClassName, ex);
						return false;
					}

					if (!PluginLifecycle.class.isAssignableFrom(pluginLifecycleClass)) {
						isStarted = false;
						getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
							"Lifecycle class for Plugin \"" + this + "\" does not extend PluginLifecycle: " + lifecycleClassName,
							null);
						return false;
					}

					if (this.isActive) {
						// Instantiate PluginLifecycle class.
						try {
							lifecycleInstance = (PluginLifecycle)pluginLifecycleClass.newInstance();
						} catch (Exception ex) {
							isStarted = false;
							getPluginEngine().getLogger().log(LoggerLevel.SEVERE,
								"Error creating lifecycle class for Plugin \"" + this + "\": " + lifecycleClassName, ex);
							return false;
						}
					}
				}
			}
			
			if (lifecycleInstance != null) lifecycleInstance.initialize();
		}

		// Store this Plugin in a queue where its start method will be invoked by a different thread as soon as possible.
		getPluginEngine().startPluginQueue.add(this);

		return true;
	}

	/**
	 * Stops this Plugin if it is not already stopped.
	 */
	public void stop () {
		if (!isStarted) return;

		PluginEngineEvent engineEvent = new PluginEngineEvent(PluginEngineEventType.PLUGIN_STOPPED, getPluginEngine());
		engineEvent.setPayload(this);
		getPluginEngine().firePluginEngineEvent(engineEvent);

		if (lifecycleInstance != null) {
			lifecycleInstance.stop();
		}

		isStarted = false;
	}
	
	/**
	 * Loads this plugin.
	 * 
	 * @author Miguel Reboiro Jato
	 * @throws PluginEngineException if the this plugin could not be loaded.
	 */
	public void load() throws PluginEngineException {
		//TODO: MIGUEL Check
		getPluginEngine().loadPlugin(this);
	}

	/**
	 * Unloads this Plugin. This will cause other Plugins that have a required Dependency on the specified Plugin to become
	 * unresolved.
	 */
	public void unload () {
		getPluginEngine().unloadPlugin(this);
	}

	/**
	 * Unresolves this specified Plugin and prohibits it from becoming resolved again.
	 */
	public void disable () {
		getPluginEngine().disablePlugin(this);
	}

	/**
	 * Allows this previously disabled Plugin to become resolved again.
	 */
	public void enable () {
		getPluginEngine().enablePlugin(this);
	}

	/**
	 * Adds an ExtensionPoint to this Plugin. ExtensionPoints must be added before a Plugin is added to the PluginEngine.
	 * 
	 * @param extensionPoint the ExtensionPoint to be added.
	 */
	synchronized public void addExtensionPoint (ExtensionPoint extensionPoint) {
		if (extensionPoint == null) throw new NullPointerException("Invalid argument: extensionPoint");
		String name = extensionPoint.getName();
		if (name == null || name.length() == 0) throw new IllegalArgumentException("Invalid ExtensionPoint name:" + name);
		if (extensionPoints.containsKey(name))
			throw new IllegalArgumentException("ExtensionPoint already exists in plugin:" + name);

		extensionPoints.put(name, extensionPoint);
	}

	/**
	 * Unresolves all Extensions attached to the specified ExtensionPoint and removes it from this Plugin.
	 * 
	 * @param extensionPoint the ExtensionPoint whose Extensions will be unresolved and removed.
	 */
	synchronized public void removeExtensionPoint (ExtensionPoint extensionPoint) {
		if (extensionPoint == null) throw new NullPointerException("Invalid argument: extensionPoint");
		if (!extensionPoints.containsValue(extensionPoint)) return;

		// Unresolve attached Extensions.
		for (Extension extension:extensionPoint.getExtensions()) {
			extension.unresolve();
		}

		extensionPoints.remove(extensionPoint.getName());
	}

	/**
	 * Returns an ExtensionPoint defined in this Plugin or {@code null} if the ExtensionPoint does not exist in this Plugin.
	 * 
	 * @param name the name of the ExtensionPoint.
	 * @return the ExtensionPoint with the name provided.
	 */
	synchronized public ExtensionPoint getExtensionPoint (String name) {
		if (name == null)
			throw new NullPointerException("Invalid argument: name");
		return (ExtensionPoint) extensionPoints.get(name);
	}

	/**
	 * Returns all ExtensionPoints for this Plugin.
	 * 
	 * @return all ExtensionPoints for this Plugin.
	 */
	synchronized public List<ExtensionPoint> getExtensionPoints () {
		return new ArrayList<>(extensionPoints.values());
	}

	/**
	 * Adds an Extension to this Plugin. Extensions must be added before a Plugin is added to the PluginEngine. If no Dependency
	 * exists to the Plugin that the Extension attaches to, then an optional Dependency will be created to the other Plugin.
	 * 
	 * @param extension the Extension to be added.
	 */
	synchronized public void addExtension (Extension extension) {
		if (extension == null) throw new NullPointerException("Invalid argument: extension");

		extensions.add(extension);

		if (getDependency(extension.getExtensionPointPluginUID()) == null) {
			Dependency dependency = new Dependency(this, extension.getExtensionPointPluginUID());
			dependency.setOptional(true);
			addDependency(dependency);
		}
	}

	/**
	 * Unresolves the specified Extension and removes it from this Plugin. If the Dependency on the Plugin the Extension was
	 * attached to is optional and this Plugin has no other Extensions to that Plugin, then the Dependency will be removed.
	 * 
	 * @param extension the Extension to be unresolved and removed.
	 */
	synchronized public void removeExtension (Extension extension) {
		if (extension == null) throw new NullPointerException("Invalid argument: extension");
		if (!extensions.contains(extension)) return;

		if (extension.getExtensionPoint() != null) extension.unresolve();
		extensions.remove(extension);

		Dependency dependency = getDependency(extension.getExtensionPointPluginUID());
		if (dependency.isOptional()) {
			// Return if any remaining Extensions are attached to the same Plugin.
			for (Extension otherExtension:getExtensions()) {
				if (otherExtension.getExtensionPointPluginUID().equals(dependency.resolveToPluginUID)) {
					return;
				}
			}
			
			// The Dependency is optional and no other Extensions are attached to the other Plugin. Remove the Dependency.
			removeDependency(dependency);
		}
	}

	/**
	 * Returns a list of all Extension for this Plugin.
	 * 
	 * @return a list of all Extension for this Plugin.
	 */
	synchronized public List<Extension> getExtensions () {
		return new ArrayList<Extension>(extensions);
	}

	/**
	 * Adds a Dependency that represents a Plugin that this Plugin is dependent upon. Dependencies must be added before a Plugin is
	 * added to the PluginEngine. If the Dependency already exists it will be overwritten.
	 * 
	 * @param dependency the Dependency to be added.
	 */
	synchronized public void addDependency (Dependency dependency) {
		if (dependency == null) throw new NullPointerException("Invalid argument: dependency");
		if (dependency.getDependentPlugin() != this)
			throw new IllegalArgumentException("Dependency can only be applied to Plugin: " + dependency.getDependentPlugin());
		if (dependency.isCompatible(this)) throw new IllegalArgumentException("Plugin cannot depend on itself.");

		dependencies.put(dependency.resolveToPluginUID, dependency);
	}

	/**
	 * Unresolves the specified Dependency and removes it from this Plugin. If the Dependency is required and is resolved then this
	 * Plugin will be unresolved. If this Plugin has an Extension to the Dependency's plugin then the old Dependency will be
	 * replaced by an optional Dependency (see {@link #addExtension(Extension)}).
	 * 
	 * @param dependency the Dependency to be unresolved and removed.
	 */
	synchronized public void removeDependency (Dependency dependency) {
		if (dependency == null) throw new NullPointerException("Invalid argument: dependency");
		if (!dependencies.containsKey(dependency.resolveToPluginUID)) return;

		// If the Dependency is required and is resolved then this Plugin will be unresolved.
		if (!dependency.isOptional() && dependency.isResolved()) {
			getPluginEngine().unresolvePlugin(this, true);
		} else {
			dependency.unresolve();
			// If there is an Extension to the Dependency's plugin then the old Dependency will be replaced by an optional one.
			for (Extension extension:getExtensions()) {
				if (extension.getExtensionPointPluginUID().equals(dependency.resolveToPluginUID)) {
					Dependency optionalDependency = new Dependency(this, dependency.resolveToPluginUID);
					optionalDependency.setOptional(true);
					addDependency(optionalDependency);
				}
			}
		}

		dependencies.remove(dependency.resolveToPluginUID);
	}

	/**
	 * Returns a list of this Plugin's Dependencies.
	 * 
	 * @return a list of this Plugin's Dependencies.
	 */
	synchronized public List<Dependency> getDependencies () {
		return new ArrayList<Dependency>(dependencies.values());
	}

	/**
	 * Returns the Dependency for the specified Plugin UID or {@code null} if no Dependency was found.
	 *
	 * @param pluginUID the Plugin UID of the Dependency.
	 * @return the Dependency for the specified Plugin UID or {@code null} if no Dependency was found.
	 */
	synchronized public Dependency getDependency (String pluginUID) {
		if (pluginUID == null) throw new NullPointerException("Invalid argument: pluginUID");
		return (Dependency)dependencies.get(pluginUID);
	}

	/**
	 * Returns a list of Plugins that are dependent upon this Plugin.
	 * 
	 * @return a List of Plugins that are dependent upon this Plugin.
	 */
	synchronized public List<Plugin> getDependentPlugins () {
		return new ArrayList<Plugin>(dependentPlugins);
	}

	/**
	 * Returns the ClassLoader used to load classes within this Plugin. This is not the ClassLoader that loaded the Plugin class.
	 * 
	 * @return the ClassLoader used to load classes within this Plugin.
	 */
	public ClassLoader getPluginClassLoader () {
		// Note that PluginClassloader should remain package-private. There is no need to access it outside the PluginEngine.
		return pluginClassloader;
	}

	/**
	 * Returns {@code true} if the specified Plugin is version compatible with a Dependency of this Plugin or if no Dependency exists.
	 * 
	 * @param plugin the plugin to check.
	 * @return {@code true} if the specified Plugin is version compatible with a Dependency of this Plugin or if no Dependency exists.
	 */
	public boolean isCompatible (Plugin plugin) {
		if (plugin == null) throw new NullPointerException("Invalid argument: plugin");
		Dependency dependency = getDependency(plugin.getUID());
		if (dependency == null) return true;
		// If this Plugin has a dependency on the specified Plugin, make sure the versions are compatible.
		return dependency.isVersionCompatible(plugin.getVersion());
	}

	/**
	 * Returns an absolute path to the resource with the given name. If a Plugin is an archive the resource will be extracted to
	 * the user's temp directory. Each Plugin is given its own extracted resource location. Extraction of a specific resource is
	 * only done once during the lifetime of the Plugin. This method either returns a path or throws IOException, it never returns
	 * {@code null}.

	 * @param name the name of a resource.
	 * @return an absolute path to the resource with the given name.
	 * @throws IOException if the resource does not exist or could not be extracted.
	 */
	public String getExtractedResourcePath (String name) throws IOException {
		if (name == null) throw new NullPointerException("Invalid argument: name");
		if (!isArchive()) {
			URL resource = pluginClassloader.getResource(name);
			if (resource == null) return null;
			File file = new File(resource.getFile());
			if (!file.exists()) throw new FileNotFoundException("Unable to find resource in plugin \"" + this + "\": " + name);
			return file.getAbsolutePath();
		}

		String extractedPath = getPluginEngine().getTempDirectory() + getUID() + '-' + getVersion().getFullVersion()
			+ File.separator + name;
		File extractedFile = new File(extractedPath);
		// Extract the file if it doesn't exist or was created before this PluginClassLoader was constructed.
		if (!extractedFile.exists() || extractedFile.lastModified() < constructionTime) {
			InputStream resourceStream = pluginClassloader.getResourceAsStream(name);
			if (resourceStream == null)
				throw new FileNotFoundException("Unable to find resource in plugin \"" + this + "\": " + name);
			try {
				// Create the resource's directories, if necessary.
				File outputDirectory = extractedFile.getParentFile();
				if (outputDirectory != null) outputDirectory.mkdirs();
				// Output the resource file (overwriting the file if it exists).
				FileOutputStream outputStream = new FileOutputStream(extractedPath);
				try {
					byte[] buffer = new byte[1024];
					int bytes;
					while ((bytes = resourceStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytes);
					}
				} finally {
					outputStream.close();
				}
			} finally {
				resourceStream.close();
			}
			getPluginEngine().getLogger().log(LoggerLevel.FINE, "Extracted resource from Plugin \"" + this + "\": " + name,
				null);
		}
		return extractedPath;
	}

	/**
	 * Replaces the specified token with a PluginEngine token, System property, or String for the plugin.properties resource file.
	 * 
	 * @param token a String starting and ending with "%%" for a PluginEngine token or System property, or with "$$" for a
	 *           plugin.properties resource.
	 * @return the generated token.
	 */
	public String replaceToken (String token) {
		if (token == null) return null;
		if (token.length() < 5) return token;

		String tokenKey = token.substring(2, token.length() - 2);
		if (token.startsWith("%%") && token.endsWith("%%")) {
			// Try an application defined token.
			String value = getPluginEngine().getToken(tokenKey);
			if (value != null) return value;

			// Try a system defined token.
			value = System.getProperty(tokenKey.toLowerCase());
			if (value != null) return value;

		} else if (token.startsWith("$$") && token.endsWith("$$")) {
			// Try a plugin properties file token.
			try {
				ResourceBundle bundle = ResourceBundle.getBundle("plugin", Locale.getDefault(), pluginClassloader);
				if (bundle != null) return bundle.getString(tokenKey.toLowerCase());
			} catch (Exception ex) {
				getPluginEngine().getLogger().log(LoggerLevel.WARNING,
					"Error finding token properties in Plugin property file:" + tokenKey, ex);
			}
		}

		// Can't recognize token start/end or couldn't find a token match, so return the token as it is.
		return token;
	}

	/**
	 * Sets the name of the lifecycle Class that manages this Plugin's lifecycle.
	 * 
	 * @param lifecycleClassName the name of the lifecycle Class that manages this Plugin's lifecycle.
	 * @see PluginLifecycle
	 */
	public void setLifecycleClassName (String lifecycleClassName) {
		if (lifecycleClassName == null) throw new NullPointerException("Invalid argument: lifecycleClassName");
		this.lifecycleClassName = lifecycleClassName;
	}

	/**
	 * Returns the name of the lifecycle Class that manages this Plugin's lifecycle or {@code null} if there is no lifecycle class.
	 * 
	 * @return the name of the lifecycle Class that manages this Plugin's lifecycle or {@code null} if there is no lifecycle class.
	 * @see PluginLifecycle
	 */
	public String getLifecycleClassName () {
		return lifecycleClassName;
	}

	/**
	 * Starts the Plugin if not started and returns the lifecycle instance that manages this Plugin's lifecycle or {@code null} if there
	 * is no lifecycle class or the Plugin could not be started.
	 * 
	 * @return the PluginLifecycle instance of this Plugin. {@code null} if there is no lifecycle class associated of the Plugin could not
	 * be started.
	 * @see PluginLifecycle
	 */
	public PluginLifecycle getLifecycleInstance () {
		if (!start()) {
			getPluginEngine().getLogger().log(LoggerLevel.WARNING,
				"Unable to start Plugin \"" + this + "\". Lifecycle instance cannot be acquired.", null);
			return null;
		}
		return lifecycleInstance;
	}

	/**
	 * Sets the root PluginXmlNode for this Plugin's metadata.
	 * 
	 * @param metadataXmlNode the root PluginXmlNode for this Plugin's metadata.
	 */
	public void setMetadataXmlNode (PluginXmlNode metadataXmlNode) {
		this.metadataXmlNode = metadataXmlNode;
	}

	/**
	 * Returns the PluginXmlNode that represents the "metadata" XML element in the plugin.xml file or null if there is none. This
	 * can contain any XML that the Plugin wants to provide, such as vendor name, copyright, description, etc.
	 * 
	 * @return metadataXmlNode the root PluginXmlNode for this Plugin's metadata.
	 */
	public PluginXmlNode getMetadataXmlNode () {
		return metadataXmlNode;
	}

	/**
	 * Sets the friendly name of this Plugin.
	 * 
	 * @param name the friendly name of this Plugin.
	 */
	public void setName (String name) {
		this.name = name;
	}

	/**
	 * Returns the friendly name given to this Plugin. This can be used for display in lists, logging, etc. If the name is null or
	 * unset the UID will be returned.
	 * 
	 * @return the friendly name given to this Plugin.
	 */
	public String getName () {
		if (name == null) return uid;
		return name;
	}

	/**
	 * Returns {@code true} if this Plugin resides in an archive file. Returns {@code false} if it is an exploded directory on disk.
	 * 
	 * @return {@code true} if this Plugin resides in an archive file. Returns {@code false} if it is an exploded directory on disk.
	 */
	public boolean isArchive () {
		return isArchive;
	}

	/**
	 * Returns the location this Plugin uses to lookup class files and resources. If the Plugin resides on disk in an exploded
	 * directory, this will be the absolute path to the directory. If the Plugin is an archive file, this will point directly to
	 * the file on disk.
	 * 
	 * @return the location this Plugin uses to lookup class files and resources.
	 * @see #isArchive()
	 */
	public URL getPluginURL () {
		return pluginURL;
	}

	/**
	 * Returns {@code true} if this Plugin has been started.
	 * 
	 * @return {@code true} if this Plugin has been started.
	 * @see #start()
	 * @see PluginLifecycle#start()
	 */
	public synchronized boolean isStarted () {
		return isStarted;
	}

	/**
	 * Returns {@code true} if this Plugin has been resolved. A Plugin is resolved as soon as all of its required Dependencies have
	 * resolved.
	 * 
	 * @return {@code true} if this Plugin has been resolved. A Plugin is resolved as soon as all of its required Dependencies have
	 * resolved.
	 */
	public boolean isResolved () {
		return isResolved;
	}

	/**
	 * Returns {@code true} if this Plugin has been disabled.
	 * 
	 * @return {@code true} if this Plugin has been disabled.
	 * @see PluginEngine#disablePlugin(Plugin)
	 */
	public boolean isDisabled () {
		return isDisabled;
	}
	
	/**
	 * Returns {@code true} if the lifecycle of this Plugin must be created and started.
	 * 
	 * @return {@code true} if the lifecycle of this Plugin must be created and started.
	 * @see PluginEngine#loadPlugin(Plugin)
	 */
	public boolean isActive() {
		return this.isActive;
	}
	
	/**
	 * Returns {@code true} if this Plugin is loaded.
	 * 
	 * @return {@code true} if this Plugin is loaded.
	 * @author Miguel Reboiro Jato
	 */
	public boolean isLoaded() {
		return this.isLoaded;
	}

	/**
	 * Returns the unique ID for this Plugin. This is used to obtain a reference to this Plugin through the PluginEngine.
	 * 
	 * @return the unique ID for this Plugin.
	 */
	public String getUID () {
		return uid;
	}

	/**
	 * Sets the unique ID for this Plugin.
	 * 
	 * @param uid the unique ID for this Plugin.
	 */
	public void setUID (String uid) {
		if (uid == null) throw new NullPointerException("Invalid argument: uid");
		this.uid = uid;
	}

	/**
	 * If true, this Plugin will be started as soon as possible after being resolved. If the PluginEngine is not started yet, the
	 * Plugin won't be started until the PluginEngine is started.
	 * 
	 * @param startWhenResolved whether the Plugin should be started when it is resolved or not.
	 * @see Plugin#start()
	 * @see PluginLifecycle#start()
	 */
	public void setStartWhenResolved (boolean startWhenResolved) {
		this.startWhenResolved = startWhenResolved;
	}

	/**
	 * Returns {@code true} if this Plugin will be started as soon as possible after being loaded into the PluginEngine.
	 * 
	 * @return {@code true} if this Plugin will be started as soon as possible after being loaded into the PluginEngine.
	 */
	public boolean getStartWhenResolved () {
		return startWhenResolved;
	}
	
	/**
	 * Sets the order in which the lifecycle class "start" method should be called.
	 * 
	 * @param startOrder the start order of this Plugin.
	 */
	public void setStartOrder(int startOrder) {
		this.startOrder = startOrder;
	}
	
	/**
	 * Returns order in which the lifecycle class "start" method should be called.
	 * 
	 * @return the start order of this Plugin.
	 */
	public int getStartOrder() {
		return this.startOrder;
	}

	/**
	 * If {@code true}, when this Plugin is unable to find a class within its own classpath and its Dependencies, it first will look
	 * in the Plugins that are dependent upon this Plugin for the class before delegating to the class loader that loaded the PluginEngine
	 * class. Defaults to {@code false}. This feature is very rarely needed but can be useful when code that is not modifiable (such as a
	 * third party library) does a class lookup without allowing the classloader to be specified.
	 * 
	 * @param dependentPluginLookup whether this Plugin should look for its dependencies in the dependent Plugins.
	 */
	public void setDependentPluginLookup (boolean dependentPluginLookup) {
		this.dependentPluginLookup = dependentPluginLookup;
	}

	/**
	 * Returns {@code true} if this Plugin will look for classes in Plugins dependent upon it.
	 * 
	 * @return {@code true} if this Plugin will look for classes in Plugins dependent upon it.
	 * @see #setDependentPluginLookup(boolean)
	 */
	public boolean getDependentPluginLookup () {
		return dependentPluginLookup;
	}

	/**
	 * Sets the version of this Plugin.
	 * 
	 * @param version the version of this Plugin.
	 */
	public void setVersion (PluginVersion version) {
		if (version == null) throw new NullPointerException("Invalid argument: version");
		this.version = version;
	}

	/**
	 * Returns the version of this Plugin.
	 * 
	 * @return the version of this Plugin.
	 */
	public PluginVersion getVersion () {
		return version;
	}

	/**
	 * Implements hashCode by using the hash of the objects used in the equals() comparison and multiplying by prime numbers.
	 * 
	 * @return the hash code of this instance.
	 */
	@Override
	public int hashCode () {
		int hash = super.hashCode();
		hash += (hash * 23) + getName().hashCode();
		hash += (hash * 17) + getVersion().getFullVersion().hashCode();
		hash += (hash * 11) + getUID().hashCode();
		return hash;
	}

	/**
	 * Returns this Plugin's friendly name.
	 * 
	 * @return this Plugin's friendly name.
	 */
	@Override
	public String toString () {
		return getName();
	}

	/**
	 * Returns the PluginEngine instance that this Plugin is associated with.
	 * 
	 * @return the PluginEngine instance that this Plugin is associated with.
	 */
	public PluginEngine getPluginEngine () {
		return pluginEngine;
	}

	/**
	 * Compares this Plugin's version with the specified Plugin's version. The provided object must be a Plugin.
	 * 
	 * @param object the Plugin to be compared. A Plugin instance is required.
	 * @return a negative int, zero, or a positive int if this Plugin's version is less than, equal to, or greater than the
	 *         specified Plugin's version.
	 * @see #compareTo(Plugin)
	 */
	@Override
	public int compareTo (Object object) {
		return compareTo((Plugin) object);
	}

	/**
	 * Compares this Plugin's version with the specified Plugin's version.
	 * 
	 * @param plugin the Plugin to be compared.
	 * @return a negative int, zero, or a positive int if this Plugin's version is less than, equal to, or greater than the
	 *         specified Plugin's version.
	 */
	public int compareTo (Plugin plugin) {
		if (plugin == null) throw new NullPointerException("Invalid argument: plugin");
		return getVersion().compareTo(plugin.getVersion());
	}

	/**
	 * Invoked when an Extension is resolved to an ExtensionPoint this Plugin owns. Notifies the lifeycle class if necessary.
	 * 
	 * @param extension a resolved Extension.
	 */
	void extensionResolved (Extension extension) {
		if (isStarted && lifecycleInstance != null)
			lifecycleInstance.extensionResolved(extension.getExtensionPoint(), extension);
	}

	/**
	 * Invoked when an Extension is unresolved from an ExtensionPoint this Plugin owns. Notifies the lifeycle class if necessary.
	 * 
	 * @param extension an unresolved Extension.
	 */
	void extensionUnresolved (Extension extension) {
		if (isStarted && lifecycleInstance != null)
			lifecycleInstance.extensionUnresolved(extension.getExtensionPoint(), extension);
	}

	/**
	 * Invoked when this Plugin is made available for use by another Plugin that depends on this Plugin. Notifies the lifeycle
	 * class if necessary.
	 * 
	 * @param plugin a resolved dependent Plugin.
	 */
	void dependentPluginResolved (Plugin plugin) {
		dependentPlugins.add(plugin);
	}

	/**
	 * Invoked when this Plugin is no longer available for use by another Plugin that depends on this Plugin. Notifies the lifeycle
	 * class if necessary.
	 * 
	 * @param plugin an unresolved dependent Plugin.
	 */
	void dependentPluginUnresolved (Plugin plugin) {
		dependentPlugins.remove(plugin);
	}
}
