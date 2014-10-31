package org.platonos.pluginengine;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.platonos.pluginengine.event.IPluginEngineListener;
import org.platonos.pluginengine.event.PluginEngineEvent;
import org.platonos.pluginengine.event.PluginEngineEventType;
import org.platonos.pluginengine.logging.DefaultLogger;
import org.platonos.pluginengine.logging.ILogger;
import org.platonos.pluginengine.logging.LoggerLevel;
import org.platonos.pluginengine.version.PluginVersion;

/**
 * Loads, unloads, and manages Plugins. This is the core of the Plugin framework. Here is a simple usage example: <br>
 * <br>
 * <tt>
 * PluginEngine pluginEngine = new PluginEngine("example");<br>
 * pluginEngine.{@link #loadPlugins(String) loadPlugins}(&quot;C:/plugins&quot;);<br>
 * pluginEngine.{@link #start() start}();<br>
 * </tt>
 * 
 * @see Plugin
 * @see ExtensionPoint
 * @see Extension
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public final class PluginEngine {
	private final IPluginConfiguration configuration;
	
	private final String uid;

	private boolean engineStarted = false;

	private final ILogger logger;

	StartPluginQueue startPluginQueue;

	int startPluginThreadCount = 3;

	private String tempDirectory;

	/**
	 * Using the Plugin UID as a key, this stores a List of Plugins sorted by version from highest to lowest.
	 */
	private final Map<String, List<Plugin>> plugins = new HashMap<String, List<Plugin>>(50);
	
	/**
	 * 
	 * @author Miguel Reboiro Jato
	 */
	private final List<Plugin> unloadedPlugins = new ArrayList<Plugin>(10);

	/**
	 * Stores Plugins that cannot resolve because they have unresolved required Dependencies.
	 */
	private final List<Plugin> unresolvedPlugins = new ArrayList<Plugin>(50);

	/**
	 * Stores unresolved optional Dependencies that belong to resolved Plugins. These optional Dependencies cannot resolve because
	 * the Plugin they resolve to is unresolved or not loaded.
	 */
	private final List<Dependency> unresolvedDependencies = new ArrayList<Dependency>(50);

	/**
	 * Stores one or more extension names that archived plugin files may end with. The defaults are ".jar" and ".par" if no others
	 * are specified.
	 */
	private final List<String> archiveExtensions = new ArrayList<String>(5);

	/**
	 * Stores application defined name/value pairs for use by all plugins. These are used to retreive application wide Strings and
	 * for replacing tokens in plugin.xml files.
	 */
	private final Map<String, String> tokens = new HashMap<String, String>(100);

	/**
	 * List of PluginEngineListeners for PluginEngineEvents.
	 */
	private final List<IPluginEngineListener> pluginEngineListeners = new ArrayList<IPluginEngineListener>(2);

	/**
	 * Filters out all files that do not have plugin archive extensions.
	 */
	final FileFilter archiveFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName();
			for (String extension:getArchiveExtensions()) {
				if (name.toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	};

	/**
	 * Filters out all files that are not directories.
	 */
	private final FileFilter directoryFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};
	
	/**
	 * Creates a new PluginEngine instance using the DefaultLogger.
	 * 
	 * @param uid
	 *          A String that uniquely identifies this PluginEngine instance.
	 */
	public PluginEngine(String uid) {
		this(uid, (IPluginConfiguration) null, new DefaultLogger());
	}

	/**
	 * Creates a new PluginEngine instance which uses the specified logger.
	 * 
	 * @see ILogger
	 * @param uid
	 *          A String that uniquely identifies this PluginEngine instance.
	 */
	public PluginEngine(String uid, ILogger logger) {
		this(uid, (IPluginConfiguration) null, logger);
	}

	public PluginEngine(String uid, File configurationFile) {
		this(uid, configurationFile, new DefaultLogger());
	}
	
	public PluginEngine(String uid, File configurationFile, ILogger logger) {
		if (uid == null) {
			throw new NullPointerException("Invalid argument: uid");
		} else if (logger == null) {
			throw new NullPointerException("Invalid argument: logger");
		}
		
		this.uid = uid;
		this.logger = logger;
		
		IPluginConfiguration configuration = null;
		try {
			configuration = new PluginFileConfiguration(configurationFile);
		} catch (Exception e) {
			configuration = null;
			this.logger.log(LoggerLevel.WARNING, String.format("Invalid configuration file: %s.", configurationFile), e);
		}
		this.configuration = configuration;
		
		this.tempDirectory = String.format("%s%splatonos%s%s%s", 
			System.getProperty("java.io.tmpdir"), 
			File.separator,
			File.separator,
			uid,
			File.separator
		);

		if (!PluginXmlParser.isValidationSupported()) {
			logger.log(
				LoggerLevel.FINE,
				"SAX parser does not support JAXP 1.2. Extension XML and plugin.xml files cannot be validated.",
				null
			);
		}
	}
	
	public PluginEngine(String uid, IPluginConfiguration configuration) {
		this(uid, configuration, new DefaultLogger());
	}
	
	public PluginEngine(String uid, IPluginConfiguration configuration, ILogger logger) {
		if (uid == null) {
			throw new NullPointerException("Invalid argument: uid");
		} else if (logger == null) {
			throw new NullPointerException("Invalid argument: logger");
		}
		
		this.uid = uid;
		this.configuration = configuration;
		this.logger = logger;
		
		this.tempDirectory = String.format("%s%splatonos%s%s%s", 
			System.getProperty("java.io.tmpdir"), 
			File.separator,
			File.separator,
			uid,
			File.separator
		);

		if (!PluginXmlParser.isValidationSupported()) {
			logger.log(
				LoggerLevel.FINE,
				"SAX parser does not support JAXP 1.2. Extension XML and plugin.xml files cannot be validated.",
				null
			);
		}
	}

	/**
	 * 
	 * @author Miguel Reboiro Jato
	 * @param plugin
	 * @return
	 */
	private boolean isLoadPlugin(Plugin plugin) {
		return this.configuration == null || this.configuration.isLoadPlugin(plugin);
	}

	/**
	 * 
	 * @author Miguel Reboiro Jato
	 * @param plugin
	 * @return
	 */
	private boolean isEnabledPlugin(Plugin plugin) {
		return this.configuration == null || this.configuration.isEnabledPlugin(plugin);
	}

	/**
	 * 
	 * @author Miguel Reboiro Jato
	 * @param plugin
	 * @return
	 */
	private boolean isActivePlugin(Plugin plugin) {
		return this.configuration == null || this.configuration.isActivePlugin(plugin);
	}

	/**
	 * Starts the PluginEngine. No Plugins are started until the PluginEngine is started. Plugins loaded before the PluginEngine
	 * starts that should be started when resolved (see {@link Plugin#setStartWhenResolved(boolean)}) will not be started until the
	 * PluginEngine is started.
	 */
	public void start() {
		startPluginQueue = new StartPluginQueue(this);

		getLogger().log(LoggerLevel.INFO, "PluginEngine started.", null);

		if (plugins.values().isEmpty()) {
			getLogger().log(LoggerLevel.FINE, "No Plugins were found.", null);
		} else {
			// Resolves all plugins that were loaded before the engine started and starts all plugins that need to.
			resolvePlugins();
		}

		engineStarted = true;
		firePluginEngineEvent(new PluginEngineEvent(PluginEngineEventType.STARTUP,
				this));
	}
	
	/**
	 * Shuts down the PluginEngine. This will unload all Plugins for this PluginEngine and reset the engine so it may be started
	 * again.
	 */
	public void shutdown() {
		synchronized (this) {
			if (!engineStarted) {
				return;
			}
			engineStarted = false;
		}

		// Unload all plugins.
		for (Plugin plugin:getPlugins()) {
			unloadPlugin(plugin);
		}

		startPluginQueue.shutdown();

		getLogger().log(LoggerLevel.INFO, "PluginEngine shutdown.", null);
		firePluginEngineEvent(new PluginEngineEvent(PluginEngineEventType.SHUTDOWN,
				this));
	}

	/**
	 * Loads the Plugin at the specified location. Allows an application or Plugin to dynamically load a Plugin at runtime.
	 * 
	 * @param location
	 *          The location to look for the Plugin to load. Can point to a Plugin archive or a directory containing an unzipped
	 *          Plugin archive.
	 * @throws PluginEngineException
	 *           if the Plugin could not be loaded.
	 */
	public void loadPlugin(File location) throws PluginEngineException {
		if (location == null) {
			throw new NullPointerException("Invalid argument: location");
		}
		if (!location.exists()) {
			throw new PluginEngineException("Plugin location doesn't not exist: "
					+ location);
		}

		if (location.isDirectory()) {
			// An exploded plugin directory.
			File pluginXML = new File(location, "plugin.xml");
			if (!pluginXML.exists()) {
				throw new PluginEngineException(
						"Error getting plugin.xml URL for Plugin directory: " + location,
						null);
			}

			loadPluginXML(pluginXML);
		} else {
			// A plugin archive.
			URL pluginXmlUrl;
			try {
				pluginXmlUrl = new URL("jar", "", "file:" + location.getAbsolutePath()
						+ "!/plugin.xml");
			}
			catch (MalformedURLException ex) {
				/*throw new PluginEngineException(
						"Error getting plugin.xml URL for plugin archive: " + location, ex);*/
				// lipido
				return;
			}
			loadPluginXML(pluginXmlUrl);
		}
	}

	/**
	 * Equivolent to <code>loadPlugin(new File(location));</code>.
	 * 
	 * @see #loadPlugin(File)
	 */
	public void loadPlugin(String location) throws PluginEngineException {
		if (location == null) {
			throw new NullPointerException("Invalid argument: location");
		}
		loadPlugin(new File(location));
	}

	/**
	 * Loads the Plugins at the specified location. Allows an application or Plugin to dynamically load Plugins at runtime. Failure
	 * to load one or more Plugins will be logged as SEVERE.
	 * 
	 * @param directory
	 *          The directory to look for plugins to load. Can point a directory containing Plugin archives or a directory with
	 *          subdirectories each containing an unzipped Plugin archive.
	 */
	public void loadPlugins(File directory) {
		if (directory == null) {
			throw new NullPointerException("Invalid argument: directory");
		}

		if (!directory.exists()) {
			throw new IllegalArgumentException("Plugin directory does not exist: "
					+ directory.getAbsolutePath());
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(
					"The specified File must be a directory: "
							+ directory.getAbsolutePath());
		}

		// Determine if the directory chosen happens to be a single expanded directory
		// The call to loadPlugin() takes care of this for us, as that call will only
		// try to load a single plugin either as an expanded disk plugin OR a PAR
		// plugin.
		try {
			loadPlugin(directory);
		}
		catch (Exception e) {
			// ignore the exception, continue
			// trying below to load plugin(s).
		}

		// Load any plugin archives in the directory.
		File[] pluginArchives = directory.listFiles(archiveFilter);
		for (int i = 0; i < pluginArchives.length; i++) {
			try {
				loadPlugin(pluginArchives[i]);
			}
			catch (PluginEngineException ex) {
				logger.log(LoggerLevel.SEVERE,
						"Error loading Plugin archive in directory: "
								+ directory.getAbsolutePath(), ex);
			}
		}

		// Load any subdirectories that are plugin directories.
		File[] directories = directory.listFiles(directoryFilter);
		for (int i = 0; i < directories.length; i++) {
			File pluginXML = new File(directories[i], "plugin.xml");
			if (pluginXML.exists()) {
				try {
					loadPluginXML(pluginXML);
				}
				catch (PluginEngineException ex) {
					logger.log(LoggerLevel.SEVERE, "Error loading Plugin in directory: "
							+ directory.getAbsolutePath(), ex);
				}
			}
		}
	}

	/**
	 * Equivolent to <code>loadPlugins(new File(directory));</code>.
	 * 
	 * @see #loadPlugins(File)
	 */
	public void loadPlugins(String directory) {
		if (directory == null) {
			throw new NullPointerException("Invalid argument: directory");
		}
		loadPlugins(new File(directory));
	}

	/**
	 * Marshals the specified plugin.xml into a Plugin instance and adds it to this PluginEngine.
	 */
	private void loadPluginXML(File pluginXML) throws PluginEngineException {
		URL pluginXmlURL;
		try {
			pluginXmlURL = pluginXML.toURI().toURL();
		}
		catch (MalformedURLException ex) {
			throw new PluginEngineException("Error getting URL for plugin.xml file: "
					+ pluginXML, ex);
		}
		loadPluginXML(pluginXmlURL);
	}

	/**
	 * Marshals the specified plugin.xml into a Plugin instance and adds it to this PluginEngine.
	 */
	private void loadPluginXML(URL pluginXmlURL) throws PluginEngineException {
		// Marshal the plugin.xml file into a Plugin instance.
		
		Plugin plugin = null;
		try{
			plugin = PluginXmlParser.parse(this, pluginXmlURL);
		}catch(PluginEngineException e){
			if (e.getCause() instanceof java.io.FileNotFoundException){
				logger.log(LoggerLevel.FINE, "Ignoring .jar due to lack of plugin.xml",null);
				return;
			}else{
				throw e;
			}
		}

		if (plugin == null) {
			throw new PluginEngineException("Error parsing plugin.xml at URL: "
					+ pluginXmlURL, null);
			
		}

		loadPlugin(plugin);
	}

	/**
	 * Loads the specifed Plugin into this PluginEngine. Normally Plugins are loaded by {@link #loadPlugin(File)}or
	 * {@link #loadPlugins(File)}but this method allows Plugins to be constructed and added to the PluginEngine manually.
	 * 
	 * @throws PluginEngineException
	 *           if the Plugin could not be added.
	 */
	public void loadPlugin(Plugin plugin) throws PluginEngineException {
		if (plugin == null) {
			throw new NullPointerException("Invalid argument: plugin");
		}

		if (plugin.getUID() == null) {
			throw new IllegalArgumentException("Plugin must have a UID.");
		}

		if (plugin.getPluginEngine() != this) {
			throw new IllegalArgumentException(
					"Plugin is for the wrong PluginEngine instance: " + plugin.getUID());
		}
		
		if (plugin.isLoaded()) {
			return;
		}

		if (this.isLoadPlugin(plugin)) {
			String pluginUID = plugin.getUID();
			
			List<Plugin> pluginList = plugins.get(pluginUID);
			if (pluginList == null) {
				// This is the first plugin to be loaded with this UID.
				pluginList = new ArrayList<Plugin>(1);
				plugins.put(pluginUID, pluginList);
			} else {
				// Check to see if this plugin is already loaded.
				for (Plugin existingPlugin:pluginList) {
					if (existingPlugin.getVersion().equals(plugin.getVersion())) {
						throw new PluginEngineException("Ignoring already loaded plugin \""
								+ pluginUID + "\", version \"" + plugin.getVersion() + "\".");
					}
				}
			}
	
			pluginList.add(plugin);
	
			// Sort by PluginVersion highest to lowest.
			Collections.sort(pluginList, Collections.reverseOrder());
	
			plugin.isLoaded = true;
			
			getLogger().log(LoggerLevel.FINE, "Loading Plugin: " + plugin, null);
	
			PluginEngineEvent event = new PluginEngineEvent(PluginEngineEventType.PLUGIN_LOADED, this);
			event.setPayload(plugin);
			firePluginEngineEvent(event);
			
			plugin.isActive = this.isActivePlugin(plugin);
	
			//TODO: MIGUEL Check
			if (this.isEnabledPlugin(plugin)) {
				unresolvedPlugins.add(plugin);
		
				// If the engine is not started, the Plugins will resolve and be started when it does start.
				if (engineStarted) {
					resolvePlugins();
				}
			} else {
				plugin.isDisabled = true;
				
				getLogger().log(LoggerLevel.FINE, "Plugin not loaded by configuration: " + plugin, null);
				
				event = new PluginEngineEvent(PluginEngineEventType.PLUGIN_DISABLED, this);
				event.setPayload(plugin);
				firePluginEngineEvent(event);
			}
		} else {
			plugin.isLoaded = false;
			plugin.isActive = false;
			plugin.isDisabled = !this.isEnabledPlugin(plugin);
			
			this.unloadedPlugins.add(plugin);
		}
	}

	/**
	 * Tries to resolve unresolved Plugins.
	 */
	private synchronized void resolvePlugins() {
		// Collect all Plugins that are able to resolve.
		Set<Plugin> resolvablePlugins = new HashSet<Plugin>();
		for (Iterator<Plugin> iter = unresolvedPlugins.iterator(); iter.hasNext();) {
			Plugin plugin = iter.next();
			if (isResolvable(plugin, new HashSet<Plugin>(resolvablePlugins))) {
				iter.remove(); // Remove from the unresolvedPlugins List.
				resolvablePlugins.add(plugin);
				plugin.isResolved = true;
			}
		}

		// Resolve each Plugins' Dependencies.
		for (Plugin plugin:resolvablePlugins) {
			logger.log(LoggerLevel.FINE, "Resolving Plugin: " + plugin, null);

			// Resolve Dependencies to other Plugins.
			for (Dependency dependency:plugin.getDependencies()) {
				Plugin resolveToPlugin = getPlugin(dependency);
				if (resolveToPlugin == null || !resolveToPlugin.isResolved) {
					// If a dependency can't resolve, it is either optional or will be resolved when another Plugin resolves.
					unresolvedDependencies.add(dependency);
				} else {
					dependency.resolve(resolveToPlugin);
				}
			}
		}

		// Resolve any unresolved Dependencies that can resolve.
		for (Iterator<Dependency> iter = unresolvedDependencies.iterator(); iter.hasNext();) {
			Dependency dependency = iter.next();
			Plugin resolveToPlugin = getPlugin(dependency);

			if (resolveToPlugin == null) {
				continue;
			}

			iter.remove(); // Remove from the unresolvedDependencies List.
			dependency.resolve(resolveToPlugin);
		}

		// Resolve each Plugins' Extensions. This must happen after all Plugins that can resolve have resolved their Dependencies
		// because two Plugins may have required Dependencies on each other.
		for (Plugin plugin:resolvablePlugins) {
			// Resolve any Extensions that resolve to this Plugin.
			for (Plugin dependentPlugin:plugin.getDependentPlugins()) {
				resolveExtensions(dependentPlugin);
			}

			// Resolve Extensions to other Plugins. This could cause this Plugin to start, so it must be resolved by now.
			resolveExtensions(plugin);

			PluginEngineEvent engineEvent = new PluginEngineEvent(
					PluginEngineEventType.PLUGIN_RESOLVED, this);
			engineEvent.setPayload(plugin);
			firePluginEngineEvent(engineEvent);
		}

		// Start all Plugins that should start when resolved.
		ArrayList<Plugin> startOrderResolvablePlugins = new ArrayList<Plugin>(resolvablePlugins);
		Collections.sort(startOrderResolvablePlugins, StartPluginQueue.PLUGIN_START_ORDER_COMPARATOR);
		// The plugins won't be started until they all are in the queue.
		for (Plugin plugin:startOrderResolvablePlugins) {
			if (plugin.getStartWhenResolved()) {
				plugin.start();
			}
		}
	}

	/**
	 * Returns true if the specified Plugin and all its required Dependencies can be resolved.
	 * 
	 * @param ignorePlugins
	 *          Plugins in this Set will be ignored in figuring if the specified Plugin can resolve. These Plugins are those that
	 *          are only resolvable if the specified Plugin is resolvable.
	 */
	private boolean isResolvable(Plugin plugin, Set<Plugin> ignorePlugins) {
		ignorePlugins.add(plugin);

		try {
			// Check required Dependencies can resolve.
			for (Dependency dependency:plugin.getDependencies()) {
				if (dependency.isOptional()) {
					continue;
				}

				Plugin resolveToPlugin = plugin.getPluginEngine().getPlugin(dependency);

				if (resolveToPlugin == null) {
					return false;
				}

				if (!resolveToPlugin.isResolved()) {
					if (!ignorePlugins.contains(resolveToPlugin)) {
						if (!isResolvable(resolveToPlugin, ignorePlugins)) {
							return false;
						}
					}
				}
			}
			return true;
		}
		finally {
			ignorePlugins.remove(plugin);
		}
	}

	/**
	 * Tries to resolve all unresolved Extensions for the specified Plugin.
	 */
	private void resolveExtensions(Plugin plugin) {
		for (Extension extension:plugin.getExtensions()) {
			if (extension.getExtensionPoint() != null) {
				continue; // Skip resolved extensions.
			}

			Dependency dependency = plugin.getDependency(extension
					.getExtensionPointPluginUID());

			if (!dependency.isResolved()) {
				continue; // Possible if dependency is optional and isn't resolved yet.
			}

			ExtensionPoint extensionPoint = dependency.getResolvedToPlugin()
					.getExtensionPoint(extension.getExtensionPointName());

			if (extensionPoint == null) {
				continue; // Possible if dependency is optional and doesn't contain the extensionpoint.
			}

			extension.resolve(extensionPoint);
		}
	}

	/**
	 * Unresolves the specified Plugin. This will cause other Plugins that have a required Dependency on this Plugin to also be
	 * unresolved.
	 */
	synchronized void unresolvePlugin(Plugin plugin, boolean resolveAgain) {
		unresolvePlugin(plugin, new HashSet<Plugin>());

		// If two versions of the same Plugin are loaded and one was just unresolved, this allows another Plugins to resolve to the
		// other version.
		if (resolveAgain) {
			resolvePlugins();
		}
	}

	/**
	 * Unresolves the specified Plugin.
	 * 
	 * @param unresolvingPlugins
	 *          Contains the Plugins that are already being unresolved and no attempt should be made to unresolve them again.
	 */
	synchronized void unresolvePlugin(Plugin plugin, Set<Plugin> unresolvingPlugins) {
		if (!plugin.isResolved()) {
			return;
		}

		PluginEngineEvent engineEvent = new PluginEngineEvent(
				PluginEngineEventType.PLUGIN_UNRESOLVED, this);
		engineEvent.setPayload(plugin);
		firePluginEngineEvent(engineEvent);

		// This Set prevents infinite recursion of Plugins dependent upon each other.
		unresolvingPlugins.add(plugin);

		// For each plugin dependent on the unloading plugin, unresolve the Dependency.
		String pluginUID = plugin.getUID();
		for (Plugin dependentPlugin:plugin.getDependentPlugins()) {
			// The dependent Plugin may have been unresolved indirectly by a different dependent Plugin being unresolved.
			if (!dependentPlugin.isResolved()) {
				continue;
			}

			// If the dependent Plugin has a required Dependency on the unloading Plugin, it must be unresolved.
			Dependency dependency = dependentPlugin.getDependency(pluginUID);
			if (dependency.isOptional()) {
				dependency.unresolve();
				unresolvedDependencies.add(dependency);
			} else if (!unresolvingPlugins.contains(dependentPlugin)) {
				// If the dependent Plugin that has a required dependency is not already unresolving, unresolve it.
				logger
						.log(LoggerLevel.FINE,
								"Unresolving Plugin with a required dependency: "
										+ dependentPlugin, null);
				unresolvePlugin(dependentPlugin, unresolvingPlugins);
			}
		}

		unresolvingPlugins.remove(plugin);

		// Unresolve each extension.
		for (Extension extension:plugin.getExtensions()) {
			if (extension.getExtensionPoint() != null) {
				extension.unresolve();
			}
		}

		// For each extension point, unresolve the attached extensions.
		for (ExtensionPoint extensionPoint:plugin.getExtensionPoints()) {
			for (Extension extension:extensionPoint.getExtensions()) {
				extension.unresolve();
			}
		}

		// Stop the Plugin if it has been started.
		plugin.stop();

		// Unresolve each resolved Dependency.
		for (Dependency dependency:plugin.getDependencies()) {
			if (dependency.isResolved()) {
				dependency.unresolve();
			} else {
				unresolvedDependencies.remove(dependency);
			}
		}

		// This abandons the old classloader which will cause all the classes to be unloaded.
		plugin.pluginClassloader = PluginClassLoader.createClassLoader(plugin);

		// This call to System.gc() will cause native libraries that any Plugins had loaded to be released.
		System.gc();

		plugin.isResolved = false;

		// Put the Plugin in the pool of Plugins that can be resolved.
		unresolvedPlugins.add(plugin);
	}

	/**
	 * Unresolves the specified Plugin and prohibits it from becoming resolved again.
	 */
	synchronized public void disablePlugin(Plugin plugin) {
		if (plugin.isDisabled()) {
			return;
		}

		getLogger().log(LoggerLevel.FINE, "Disabling Plugin: " + plugin, null);

		unresolvePlugin(plugin, false);
		unresolvedPlugins.remove(plugin);

		plugin.isDisabled = true;
		//TODO: MIGUEL Check
		PluginEngineEvent event = new PluginEngineEvent(PluginEngineEventType.PLUGIN_DISABLED, this);
		event.setPayload(plugin);
		firePluginEngineEvent(event);
	}

	/**
	 * Allows the specified previously disabled Plugin to become resolved again.
	 */
	synchronized public void enablePlugin(Plugin plugin) {
		if (!plugin.isDisabled())
			return;

		plugin.isDisabled = false;
		
		getLogger().log(LoggerLevel.FINE, "Enabling Plugin: " + plugin, null);
		//TODO: MIGUEL Check
		PluginEngineEvent event = new PluginEngineEvent(PluginEngineEventType.PLUGIN_ENABLED, this);
		event.setPayload(plugin);
		firePluginEngineEvent(event);

		unresolvedPlugins.add(plugin);
		if (engineStarted)
			resolvePlugins();
	}

	/**
	 * Unloads the specified Plugin. This will cause other Plugins that have a required Dependency on the specified Plugin to become
	 * unresolved.
	 */
	public synchronized void unloadPlugin(Plugin plugin) {
		if (plugin == null) {
			throw new NullPointerException("Invalid argument: plugin");
		}
		
		if (!plugin.isLoaded()) {
			return;
		}

		String pluginUID = plugin.getUID();
		List<Plugin> pluginList = plugins.get(pluginUID);
		if (pluginList == null || !pluginList.contains(plugin)) {
			throw new IllegalArgumentException("Plugin is not loaded: " + plugin);
		}

		getLogger().log(LoggerLevel.FINE, "Unloading Plugin: " + plugin, null);

		unresolvePlugin(plugin, false);
		unresolvedPlugins.remove(plugin);
		unloadedPlugins.add(plugin);

		// Remove plugin from registry.
		pluginList.remove(plugin);
		if (pluginList.isEmpty()) {
			plugins.remove(pluginUID);
		}
		
		plugin.isLoaded = false;

		PluginEngineEvent engineEvent = new PluginEngineEvent(
				PluginEngineEventType.PLUGIN_UNLOADED, this);
		engineEvent.setPayload(plugin);
		firePluginEngineEvent(engineEvent);
		engineEvent = null;

		System.gc();
		System.gc();
	}
	
	/**
	 * Return a plugin that is not loaded.
	 * @param pluginUID
	 * @return
	 * @author Miguel Reboiro Jato
	 */
	public Plugin getUnloadedPlugin(String pluginUID) {
		if (pluginUID == null)
			return null;
		
		if (this.getPlugin(pluginUID) != null)
			return null;

		for (Plugin plugin:this.unloadedPlugins) {
			if (plugin.getUID().equals(pluginUID)) {
				return plugin;
			}
		}
		
		return null;
	}

	/**
	 * Returns the Plugin with the highest version that has the specified UID or null if it could not be found.
	 */
	public Plugin getPlugin(String pluginUID) {
		if (pluginUID == null) {
			return null;
		}

		List<Plugin> pluginList = plugins.get(pluginUID);

		if (pluginList == null) {
			return null;
		}

		return pluginList.get(0);
	}

	/**
	 * Returns the Plugin with the highest version that matches the specified version and UID or null if it could not be found.
	 */
	public Plugin getPlugin(String pluginUID, PluginVersion requiredVersion) {
		if (requiredVersion == null) {
			throw new NullPointerException("Invalid argument: requiredVersion");
		}

		if (pluginUID == null) {
			return null;
		}

		List<Plugin> pluginList = plugins.get(pluginUID);

		if (pluginList == null) {
			return null;
		}

		for (Plugin plugin:pluginList) {
			if (plugin.getVersion().compareTo(requiredVersion) == 0) {
				return plugin;
			}
		}

		return null;
	}
//
//	/**
//	 * Returns the Plugin with the highest version that matches the specified version range and UID or null if it could not be
//	 * found.
//	 */
//	public Plugin getPlugin(String pluginUID, PluginVersion minVersion,
//			PluginVersion maxVersion) {
//		if (pluginUID == null) {
//			return null;
//		}
//
//		List<Plugin> pluginList = plugins.get(pluginUID);
//		if (pluginList == null) {
//			return null;
//		}
//
//		for (Plugin plugin:pluginList) {
//			if (plugin.getVersion().compareTo(minVersion, maxVersion) == 0) {
//				return plugin;
//			}
//		}
//
//		return null;
//	}

	/**
	 * Returns the Plugin with the highest version that matches the specified Dependency or null if it could not be found.
	 */
	public Plugin getPlugin(Dependency dependency) {
		if (dependency == null) {
			throw new NullPointerException("Invalid argument: dependency");
		}
		if (dependency.requiredVersion != null) {
			return getPlugin(dependency.resolveToPluginUID,
					dependency.requiredVersion);
		}/* else {
			return getPlugin(dependency.resolveToPluginUID, dependency.minVersion,
					dependency.maxVersion);
		}*/
		else return this.getPlugin(dependency.resolveToPluginUID);
	}

	/**
	 * Returns the Plugin for the specified URL or null if it could not be found.
	 */
	public Plugin getPlugin(URL url) {
		if (url == null) {
			throw new NullPointerException("Invalid argument: url");
		}

		for (Plugin plugin:getPlugins()) {
			if (plugin.getPluginURL().equals(url)) {
				return plugin;
			}
		}

		return null;
	}

	/**
	 * Returns a List of all loaded Plugins. These Plugins are not necessarily loaded, resolved, started, or enabled.
	 */
	public List<Plugin> getPlugins() {
		// This is inefficient because of how the plugins are stored. Fortunately this method is rarely used.
		ArrayList<Plugin> allPlugins = new ArrayList<Plugin>(plugins.size());
		for (List<Plugin> pluginList:plugins.values()) {
			allPlugins.addAll(pluginList);
		}
		
		return allPlugins;
	}
	
	/**
	 * Returns the list of unresolved Plugins.
	 * @author Miguel Reboiro Jato
	 */
	public List<Plugin> getUnresolvedPlugins() {
		return Collections.unmodifiableList(this.unresolvedPlugins);
	}
	
	/**
	 * Returns the list of resolved Plugins.
	 * @author Miguel Reboiro Jato
	 */
	public List<Plugin> getResolvedPlugins() {
		List<Plugin> resolvedPlugins = this.getPlugins();
		resolvedPlugins.removeAll(this.getUnresolvedPlugins());
		return Collections.unmodifiableList(resolvedPlugins);
	}
	
	/**
	 * Return the list of unloaded Plugins.
	 * @author Miguel Reboiro Jato
	 */
	public List<Plugin> getUnloadedPlugins() {
		return Collections.unmodifiableList(this.unloadedPlugins);
	}
	
	/**
	 * Return the list of loaded Plugins.
	 * @author Miguel Reboiro Jato
	 */
	public List<Plugin> getLoadedPlugins() {
		List<Plugin> loadedPlugins = this.getPlugins();
		loadedPlugins.remove(this.getUnloadedPlugins());
		return Collections.unmodifiableList(loadedPlugins);
	}
	
	/**
	 * Determines if a plugin is loaded in this Plugin Engine.
	 * @author Miguel Reboiro Jato
	 * @param plugin the plugin.
	 * @return <code>true</code> if the plugin is loaded, <code>false</code> otherwise.
	 */
	public boolean isLoaded(Plugin plugin) {
		if (plugin == null) return false;
		else return !this.getUnloadedPlugins().contains(plugin);
	}
	
	/**
	 * Determines if a plugin is enabled in this Plugin Engine.
	 * @author Miguel Reboiro Jato
	 * @param plugin the plugin.
	 * @return <code>true</code> if the plugin is loaded, <code>false</code> otherwise.
	 */
	public boolean isEnabled(Plugin plugin) {
		if (plugin == null) return false;
		else return !this.getUnresolvedPlugins().contains(plugin);
	}
	
	/**
	 * Determines if a plugin is loaded in this Plugin Engine.
	 * @author Miguel Reboiro Jato
	 * @param plugin the plugin UID.
	 * @return <code>true</code> if the plugin is loaded, <code>false</code> otherwise.
	 */
	public boolean isLoaded(String pluginUID) {
		Plugin plugin = this.getPlugin(pluginUID);
		if (plugin == null) return false;
		else return !this.getUnloadedPlugins().contains(plugin);
	}
	
	/**
	 * Determines if a plugin is enabled in this Plugin Engine.
	 * @author Miguel Reboiro Jato
	 * @param plugin the plugin UID.
	 * @return <code>true</code> if the plugin is loaded, <code>false</code> otherwise.
	 */
	public boolean isEnabled(String pluginUID) {
		Plugin plugin = this.getPlugin(pluginUID);
		if (plugin == null) return false;
		else return !this.getUnresolvedPlugins().contains(plugin);
	}

	/**
	 * Adds a supported Plugin archive extension to this PluginEngine. Only files with these extensions will be found as Plugins by
	 * {@link #loadPlugin(File)}and {@link #loadPlugins(File)}. If the list is empty when the engine is started, the defaults of
	 * ".jar" and ".par" will be used.
	 */
	public void addArchiveExtension(String archiveExtension) {
		if (archiveExtension == null) {
			throw new NullPointerException("Invalid argument: archiveExtension");
		}

		if (!archiveExtension.startsWith(".")) {
			archiveExtension = '.' + archiveExtension;
		}

		archiveExtensions.add(archiveExtension.toLowerCase());
	}

	/**
	 * Returns a List of Strings representing the Plugin archive filename extensions that are supported by this PluginEngine.
	 */
	public List<String> getArchiveExtensions() {
		// Add default ".jar" and ".par" file extension if the list is empty.
		if (archiveExtensions.isEmpty()) {
			getLogger()
					.log(
							LoggerLevel.FINE,
							"No archive extensions specified. Using the defaults \".jar\" and \".par\".",
							null);
			addArchiveExtension(".jar");
			addArchiveExtension(".par");
		}

		return new ArrayList<String>(archiveExtensions);
	}

	/**
	 * Returns the value for the specified key or null if the key has no value.
	 * 
	 * @see #addToken(String, String)
	 */
	public String getToken(String key) {
		if (key == null) {
			throw new NullPointerException("Invalid argument: key");
		}

		return (String) tokens.get(key.toLowerCase());
	}

	/**
	 * Adds a token that can be used by any Plugin. For Plugins to use these tokens as replacements in their plugin.xml, the tokens
	 * must be set before the Plugins are loaded (usually before the PluginEngine is started).
	 * 
	 * @see Plugin#replaceToken(String)
	 */
	public void addToken(String key, String token) {
		if (key == null) {
			throw new NullPointerException("Invalid argument: key");
		}

		tokens.put(key.toLowerCase(), token);
	}

	/**
	 * Returns the logger for this PluginEngine. Any Plugin can make use of this for PluginEngine related logging purposes.
	 */
	public ILogger getLogger() {
		return logger;
	}

	/**
	 * Returns the unique ID for this PluginEngine or an empty String if this PluginEngine was created without one.
	 */
	public String getUID() {
		return uid;
	}

	/**
	 * Adds a listener to receive PluginEngineEvents.
	 */
	public void addPluginEngineListener(IPluginEngineListener listener) {
		if (listener == null) {
			throw new NullPointerException("Invalid argument: listener");
		}

		pluginEngineListeners.add(listener);
	}

	/**
	 * Removes a listener from receiving PluginEngineEvents.
	 */
	public void removePluginEngineListener(IPluginEngineListener listener) {
		if (listener == null) {
			throw new NullPointerException("Invalid argument: listener");
		}

		pluginEngineListeners.remove(listener);
	}

	/**
	 * Fires a PluginEngineEvent.
	 */
	void firePluginEngineEvent(PluginEngineEvent event) {
		for (IPluginEngineListener listener:pluginEngineListeners) {
			listener.handlePluginEngineEvent(event);
		}
	}

	/**
	 * Sets the temporary directory that this PluginEngine will use for scratch and extracted Plugin resources. The default is
	 * "/user's temp dir/platonos/engine uid/". Classes and other resources are extracted from Plugin archives directly into memory,
	 * they are not written to disk except for in special cases, such as for native libraries.
	 * 
	 * @see Plugin#getExtractedResourcePath(String)
	 */
	public void setTempDirectory(String tempDirectory) {
		if (tempDirectory == null) {
			throw new NullPointerException("Invalid argument: tempDirectory");
		}

		this.tempDirectory = tempDirectory;
	}

	/**
	 * Gets the temporary directory that this PluginEngine will use for scratch and extracted Plugin resources.
	 * 
	 * @see #setTempDirectory(String)
	 */
	public String getTempDirectory() {
		return tempDirectory;
	}
	
	/**
	 * 
	 * @author Miguel Reboiro Jato
	 * @return
	 */
	public IPluginConfiguration getPluginConfiguration() {
		return this.configuration;
	}

	/**
	 * Sets the size of the thread pool used to invoke the {@link PluginLifecycle#start()}method. The default is 3. If set to zero,
	 * the {@link PluginLifecycle#start()}method will never be invoked for any plugins. This must be set before the PluginEngine
	 * starts.
	 * 
	 * @see PluginLifecycle#start()
	 */
	public void setStartPluginThreadCount(int startPluginThreadCount) {
		if (startPluginThreadCount < 0) {
			throw new IndexOutOfBoundsException(
					"Invalid argument: startPluginThreadCount");
		}

		this.startPluginThreadCount = startPluginThreadCount;
	}

	/**
	 * Enables or disables validation of plugin.xml files. Validation is enabled by default, but requires the JAXP 1.2+ libraries to
	 * be in the classpath for Java 1.4. Java 1.5 includes the necessary JAXP libraries. This must be set before the PluginEngine
	 * starts.
	 */
	public void setPluginValidationEnabled(boolean isValidationEnabled) {
		PluginXmlParser.isValidationEnabled = isValidationEnabled;
	}

	/**
	 * Returns the Plugin that the specified ClassLoader belongs to or null if the ClassLoader does not belong to a Plugin.
	 */
	static public Plugin getPlugin(ClassLoader classLoader) {
		if (classLoader == null) {
			throw new NullPointerException("Invalid argument: classLoader");
		}
		if (classLoader instanceof PluginClassLoader) {
			return ((PluginClassLoader) classLoader).getPlugin();
		} else if (classLoader instanceof ExtensionClassLoader) {
			return ((ExtensionClassLoader) classLoader).extension.getPlugin();
		} else {
			return null;
		}
	}

	/**
	 * Returns the Plugin that the specified class was loaded from or null if the class was not loaded from a Plugin. This is
	 * useful, for example, when an extension instance needs to reference its Plugin without using a lifecycle class.
	 */
	static public Plugin getPlugin(Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Invalid argument: clazz");
		}
		return getPlugin(clazz.getClassLoader());
	}
}
