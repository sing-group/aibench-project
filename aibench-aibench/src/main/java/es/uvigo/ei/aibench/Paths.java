/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part of the AIBench Project. 

AIBench Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AIBench Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with AIBench Project.  If not, see <http://www.gnu.org/licenses/>.
*/

/*  
 * Paths.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 14 Out, 2009
 */
package es.uvigo.ei.aibench;

import java.io.File;

/**
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
	 * @return the log4jConfigurationPath
	 */
	public String getLog4jConfigurationPath() {
		if (this.log4jConfigurationPath == null) {
			this.createLog4jConfigurationPath();
		}
		return this.log4jConfigurationPath;
	}

	/**
	 * 
	 */
	private synchronized void createLog4jConfigurationPath() {
		if (this.log4jConfigurationPath == null) {
			this.log4jConfigurationPath = System.getProperty(
				"aibench.paths.log4j.conf", 
				Paths.DEFAULT_LOG4J_CONFIGURATION_PATH
			);
		}
	}

	/**
	 * @return the pluginManagerConfigurationPath
	 */
	public String getPluginManagerConfigurationPath() {
		if (this.pluginManagerConfigurationPath == null) {
			this.createPluginManagerConfigurationPath();
		}
		return this.pluginManagerConfigurationPath;
	}

	/**
	 * 
	 */
	private synchronized void createPluginManagerConfigurationPath() {
		if (this.pluginManagerConfigurationPath == null) {
			this.pluginManagerConfigurationPath = System.getProperty(
				"aibench.paths.pluginmanager.conf", 
				Paths.DEFAULT_PLUGIN_MANAGER_CONFIGURATION_PATH
			);
		}
	}

	/**
	 * @return the pluginsConfigurationPath
	 */
	public String getPluginsConfigurationPath() {
		if (this.pluginsConfigurationPath == null) {
			this.createPluginsConfigurationPath();
		}
		return this.pluginsConfigurationPath;
	}

	/**
	 * 
	 */
	private synchronized void createPluginsConfigurationPath() {
		if (this.pluginsConfigurationPath == null) {
			this.pluginsConfigurationPath = System.getProperty(
				"aibench.paths.plugins.conf", 
				Paths.DEFAULT_PLUGINS_CONFIGURATION_PATH
			);
		}
	}
}
