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
package es.uvigo.ei.aibench.repository.info;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.platonos.pluginengine.PluginEngineException;
import org.platonos.pluginengine.version.PluginVersion;

/**
 * 
 * @author Miguel Reboiro Jato
 *
 */
public final class DependencyInfo {
	private final static String TEXT_PLUGIN_PATTERN = 
		String.format("^(\\p{Alnum}|_|-)+(\\.(\\p{Alnum}|_|-)+)*\\p{Space}*(\\[(%s|%s|%s|%s)\\])?$", 
			PluginVersion.TEXT_VERSION_PATTERN,
			PluginVersion.TEXT_INTERVAL_VERSION_PATTERN,
			PluginVersion.TEXT_MIN_VERSION_PATTERN,
			PluginVersion.TEXT_MAX_VERSION_PATTERN
		);
	private final static Pattern PLUGIN_PATTERN = 
		Pattern.compile(DependencyInfo.TEXT_PLUGIN_PATTERN);
	
	private final String plugin;
	private final String uid;
	private final String version;
	private final String exactVersion;
	private final String minVersion;
	private final String maxVersion;
	
	private final PluginVersion dependencyVersion;
	
	public DependencyInfo(String plugin) throws IllegalArgumentException {
		if (DependencyInfo.PLUGIN_PATTERN.matcher(plugin).matches()) {
			this.plugin = plugin;
			this.uid = plugin.substring(0, plugin.indexOf('['));
			this.version = plugin.substring(plugin.indexOf('[') + 1, plugin.length()-1);
			try {
				this.dependencyVersion = PluginVersion.createDependencyVersion(this.version);
			} catch (PluginEngineException e) {
				throw new IllegalArgumentException("Illegal dependency version: " + this.version, e);
			}
			
			if (PluginVersion.VERSION_PATTERN.matcher(this.version).matches()) {
				Matcher matcher = PluginVersion.VERSION_PATTERN.matcher(this.version);
				this.exactVersion = (matcher.find())?matcher.group():null;
			} else {
				this.exactVersion = null;
			}
			
			if (PluginVersion.INTERVAL_VERSION_PATTERN.matcher(this.version).matches()) {
				Matcher matcher = PluginVersion.VERSION_PATTERN.matcher(this.version);
				this.minVersion = (matcher.find())?matcher.group():null;
				this.maxVersion = (matcher.find())?matcher.group():null;
			} else {
				if (PluginVersion.MIN_VERSION_PATTERN.matcher(this.version).matches()) {
					Matcher matcher = PluginVersion.VERSION_PATTERN.matcher(this.version);
					this.minVersion = (matcher.find())?matcher.group():null;
					this.maxVersion = null;
				} else {
					this.minVersion = null;
					if (PluginVersion.MAX_VERSION_PATTERN.matcher(this.version).matches()) {
						Matcher matcher = PluginVersion.VERSION_PATTERN.matcher(this.version);
						this.maxVersion = (matcher.find())?matcher.group():null;
					} else {
						this.maxVersion = null;
					}
				}
			}
		} else {
			throw new IllegalArgumentException(plugin);
		}
	}
	
	public String getPlugin() {
		return this.plugin;
	}

	public String getUid() {
		return this.uid;
	}

	public String getExactVersion() {
		return this.exactVersion;
	}
	
	public String getMinVersion() {
		return this.minVersion;
	}
	
	public String getMaxVersion() {
		return this.maxVersion;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public PluginVersion getDependencyVersion() {
		return this.dependencyVersion;
	}
	
	@Override
	public String toString() {
		return this.plugin;
	}
}