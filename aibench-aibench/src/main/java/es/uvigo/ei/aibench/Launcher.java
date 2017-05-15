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
package es.uvigo.ei.aibench;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.ExtensionPoint;
import org.platonos.pluginengine.IPluginConfiguration;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginFileConfiguration;

import es.uvigo.ei.aibench.repository.PluginInstaller;

/**
 * The main class of AIBench.
 *
 * This class launches the Platonos plugin engine.
 *
 * @author Ruben Dominguez Carbajales
 * @author Daniel Glez-Peña
 */
public class Launcher {
	private static Logger logger = Logger.getLogger(Launcher.class);

	/**
	 * Configuration of the aibench basic runtime. By default, the configuration is located in aibench.conf file
	 */
	public static Properties CONFIG = new Properties();

	/**
	 * Plugins base directory. By default, it is "plugins_bin".
	 */
	public static String pluginsDir;


	/**
	 * The Platonos plugin engine
	 */
	public static PluginEngine pluginEngine;

	/**
	 * Initializes (if not yet) and gets the plugin engine
	 *
	 * @return The plugin engine
	 */
	public static PluginEngine getPluginEngine() {
		if (pluginEngine == null) {
			IPluginConfiguration configuration = null;
			try {
				configuration = new PluginFileConfiguration(Util.getGlobalResourceURL(Paths.getInstance().getPluginsConfigurationPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (Boolean.parseBoolean(CONFIG.getProperty("pluginengine.debug"))) {
				pluginEngine = new PluginEngine("AIBench Engine", configuration); // More debug
			} else {
				org.platonos.pluginengine.logging.ILogger ilog = new AIBenchLogger();
				pluginEngine = new PluginEngine("AIBench Engine", configuration, ilog);
			}

		}
		return pluginEngine;
	}

	/**
	 * Gets objects implementing an extension point of a given plugin
	 *
	 * @param pluginID The id of the plugin
	 * @param extensionPoint The name of the extension point
	 * @return A list of objects implementing the extension point
	 */
	public static List<Object> getExtensionPointInstances(String pluginID,
			String extensionPoint) {

		Plugin plugin = getPluginEngine().getPlugin(pluginID);
		ExtensionPoint extPoint = plugin.getExtensionPoint(extensionPoint);

		List<Object> toret = new ArrayList<>();
		if (extPoint != null) {
			for (Extension extension : extPoint.getExtensions()) {
				toret.add(extension.getExtensionInstance());
			}
		}

		return toret;
	}
	
	private static void installUpdates() {
		if (Boolean.parseBoolean(Launcher.CONFIG.getProperty("startup.install_plugins", "false"))) {
			Properties properties = new Properties();
			try {
				properties.load(Util.getGlobalResourceURL(Paths.getInstance().getPluginManagerConfigurationPath()).openStream());
				String installerDir = properties.getProperty("plugininstaller.dir");
				if (installerDir == null) {
					System.err.println("Installer directory property ('plugininstaller.dir') isn't set at file 'conf/pluginmanager.conf'.");
				} else {
					PluginInstaller installer;
					Boolean deleteInvalidInstalls = Boolean.parseBoolean(properties.getProperty("plugininstaller.delete_invalid_installs", "true"));
					String ignoreDirs = properties.getProperty("plugininstaller.ignore_dirs");
					if (ignoreDirs != null && ignoreDirs.trim().length() > 0) {
						installer = new PluginInstaller(Launcher.pluginsDir, installerDir, ignoreDirs.trim().split(";"));
					} else {
						installer = new PluginInstaller(Launcher.pluginsDir, installerDir);
					}
					
					installer.installPlugins(deleteInvalidInstalls);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void configure() {
		URL url = Util.getGlobalResourceURL(Paths.getInstance().getLog4jConfigurationPath());
		PropertyConfigurator.configure(url);
	}

	/**
	 * Starts AIBench.
	 *
	 * This also shows a loading splash screen (see {@link SplashFrame}).
	 * You can use the system property "aibench.nogui" to remove the splash
	 * screen.
	 *
	 * @param args The directory of plugins. By default, it is "plugins_bin"
	 */
	public static void main(String[] args) {
		readConfig();
		Launcher.pluginsDir = (args.length >= 1) ? args[0] : "plugins_bin";

		SplashFrame splash = null;
		if (System.getProperty("aibench.nogui") == null) {
			splash = new SplashFrame();
			splash.setVisible(true);
		}
		// Launcher.setLookAndFeel();
		Launcher.configure();
		Launcher.installUpdates();
		
		PluginEngine engine = Launcher.getPluginEngine();

		if (System.getProperty("aibench.nogui") == null) {
			engine.addPluginEngineListener(splash);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down plugin engine");
				Launcher.getPluginEngine().shutdown();
			}
		});


		engine.setStartPluginThreadCount(1);
		engine.loadPlugins(Launcher.pluginsDir);
		logger.info("Plugins loaded");
		engine.start();
	}

	private static void readConfig() {
		try {
			Launcher.CONFIG.load(Util.getGlobalResourceURL(Paths.getInstance().getAibenchConfigurationPath()).openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Launcher() {
	}
}
