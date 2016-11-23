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
package es.uvigo.ei.aibench;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.ExtensionPoint;
import org.platonos.pluginengine.IPluginConfiguration;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginFileConfiguration;

import es.uvigo.ei.aibench.repository.PluginInstaller;

/**
 * @author Ruben Dominguez Carbajales 13-feb-2006 - 2006
 */
public class Launcher {
	public static Properties CONFIG = new Properties();

	public static String pluginsDir;
	public static PluginEngine pluginEngine;

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

	public static List<Object> getExtensionPointInstances(String pluginID,
			String extensionPoint) {
		Plugin plugin = getPluginEngine().getPlugin(pluginID);
		List<Object> toret = new ArrayList<Object>();
		ExtensionPoint extPoint = plugin.getExtensionPoint(extensionPoint);
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
		} else {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				/**
				 * Performs the plugin engine shutdown.
				 */
				@Override
				public void run() {
					Launcher.getPluginEngine().shutdown();
				}
			});
		}

		engine.setStartPluginThreadCount(1);
		engine.loadPlugins(Launcher.pluginsDir);
		engine.start();
	}

	/**
	 * Reads the config from <AIBench_directory>/conf/aibench.conf
	 */
	private static void readConfig() {
		try {
			Launcher.CONFIG.load(Util.getGlobalResourceURL(Paths.getInstance().getAibenchConfigurationPath()).openStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
