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

import java.io.File;

/**
 * A singleton class to access AIBench main paths.
 *
 * @author Miguel Reboiro Jato
 *
 */
public class Paths {
	private static Paths instance;
	
	private static synchronized void createInstance() {
		if (Paths.instance == null) {
			Paths.instance = new Paths();
		}
	}

	/**
	 * Gets the instance of the singleton
	 *
	 * @return The unique instance
	 */
	public static Paths getInstance() {
		if (Paths.instance == null) {
			Paths.createInstance();
		}
		return Paths.instance;
	}
	
	private final static String DEFAULT_PATH = "conf";
	private final static String DEFAULT_AIBENCH_CONFIGURATION_PATH = 
		Paths.DEFAULT_PATH + File.separator + "aibench.conf";
	private final static String DEFAULT_LOG4J_CONFIGURATION_PATH = 
		Paths.DEFAULT_PATH + File.separator + "log4jconfig";
	private final static String DEFAULT_PLUGIN_MANAGER_CONFIGURATION_PATH = 
		Paths.DEFAULT_PATH + File.separator + "pluginmanager.conf";
	private final static String DEFAULT_PLUGINS_CONFIGURATION_PATH = 
		Paths.DEFAULT_PATH + File.separator + "plugins.conf";
	
	private String aibenchConfigurationPath;
	private String log4jConfigurationPath;
	private String pluginManagerConfigurationPath;
	private String pluginsConfigurationPath;
	
	private Paths() {}

	/**
	 * Gets the aibench basic runtime configuration file path
	 *
	 * @return The aibench basic runtime configuration file path
	 */
	public String getAibenchConfigurationPath() {
		if (this.aibenchConfigurationPath == null) {
			this.createAibenchConfPath();
		}
		return this.aibenchConfigurationPath;
	}


	private synchronized void createAibenchConfPath() {
		if (this.aibenchConfigurationPath == null) {
			this.aibenchConfigurationPath = System.getProperty(
				"aibench.paths.aibench.conf", 
				Paths.DEFAULT_AIBENCH_CONFIGURATION_PATH
			);
		}
	}

	/**
	 * Gets the Log4J configuration file path
	 * @return The Log4J configuration file path
	 */
	public String getLog4jConfigurationPath() {
		if (this.log4jConfigurationPath == null) {
			this.createLog4jConfigurationPath();
		}
		return this.log4jConfigurationPath;
	}

	private synchronized void createLog4jConfigurationPath() {
		if (this.log4jConfigurationPath == null) {
			this.log4jConfigurationPath = System.getProperty(
				"aibench.paths.log4j.conf", 
				Paths.DEFAULT_LOG4J_CONFIGURATION_PATH
			);
		}
	}

	/**
	 * Gets the plugin manager configuration file path
	 *
	 * @return The plugin manager configuration file path
	 */
	public String getPluginManagerConfigurationPath() {
		if (this.pluginManagerConfigurationPath == null) {
			this.createPluginManagerConfigurationPath();
		}
		return this.pluginManagerConfigurationPath;
	}

	private synchronized void createPluginManagerConfigurationPath() {
		if (this.pluginManagerConfigurationPath == null) {
			this.pluginManagerConfigurationPath = System.getProperty(
				"aibench.paths.pluginmanager.conf", 
				Paths.DEFAULT_PLUGIN_MANAGER_CONFIGURATION_PATH
			);
		}
	}

	/**
	 * Gets the plugins configuration file path
	 * @return The plugins configuration file path
	 */
	public String getPluginsConfigurationPath() {
		if (this.pluginsConfigurationPath == null) {
			this.createPluginsConfigurationPath();
		}
		return this.pluginsConfigurationPath;
	}

	private synchronized void createPluginsConfigurationPath() {
		if (this.pluginsConfigurationPath == null) {
			this.pluginsConfigurationPath = System.getProperty(
				"aibench.paths.plugins.conf", 
				Paths.DEFAULT_PLUGINS_CONFIGURATION_PATH
			);
		}
	}
}
