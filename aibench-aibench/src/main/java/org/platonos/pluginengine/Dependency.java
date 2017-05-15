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

import org.platonos.pluginengine.logging.LoggerLevel;
import org.platonos.pluginengine.version.PluginVersion;

/**
 * Describes a dependency one Plugin has on another Plugin. The Dependency is required unless
 * {@link #setOptional(boolean)} is set to {@code true}.
 * @author Nathan Sweet (misc@n4te.com)
 */
public class Dependency {
	final private Plugin dependentPlugin;
	final String resolveToPluginUID;
	PluginVersion requiredVersion;
//	PluginVersion minVersion;
//	PluginVersion maxVersion;
	private boolean isOptional = false;
	private Plugin resolvedToPlugin;

	/**
	 * @param dependentPlugin the plugin to which this dependency depends.
	 * @param resolveToPluginUID Plugin UID that the dependent Plugin is dependent upon.
	 */
	public Dependency (Plugin dependentPlugin, String resolveToPluginUID) {
		if (dependentPlugin == null) throw new NullPointerException("Invalid argument: dependentPlugin");
		if (resolveToPluginUID == null) throw new NullPointerException("Invalid argument: resolveToPluginUID");
		this.dependentPlugin = dependentPlugin;
		this.resolveToPluginUID = resolveToPluginUID;
	}

	/**
	 * @param dependentPlugin the plugin to which this dependency depends.
	 * @param resolveToPluginUID Plugin UID that the Plugin is dependent upon.
	 * @param requiredVersion The Plugin that the dependent Plugin depends upon must be of this version.
	 */
	public Dependency (Plugin dependentPlugin, String resolveToPluginUID, PluginVersion requiredVersion) {
		this(dependentPlugin, resolveToPluginUID);
		if (requiredVersion == null) throw new NullPointerException("Invalid argument: requiredVersion");
		this.requiredVersion = requiredVersion;
	}
//
//	/**
//	 * @param resolveToPluginUID Plugin UID that the Plugin is dependent upon.
//	 * @param minVersion The Plugin that the dependent Plugin depends upon must be of atleast this version. Can be null for no
//	 *           minimum.
//	 * @param maxVersion The Plugin that the dependent Plugin depends upon must be of atmost this version. Can be null for no
//	 *           maximum.
//	 */
//	public Dependency (Plugin dependentPlugin, String resolveToPluginUID, PluginVersion minVersion, PluginVersion maxVersion) {
//		this(dependentPlugin, resolveToPluginUID);
//		if (minVersion == null) throw new NullPointerException("Invalid argument: minVersion");
//		if (maxVersion == null) throw new NullPointerException("Invalid argument: maxVersion");
//		this.minVersion = minVersion;
//		this.maxVersion = maxVersion;
//	}

	/**
	 * @return the dependent Plugin that this Dependency applies to.
	 */
	public Plugin getDependentPlugin () {
		return dependentPlugin;
	}

	/**
	 * @return the Plugin that this Dependency is resolved to or null if this Dependency is unresolved.
	 */
	public Plugin getResolvedToPlugin () {
		return resolvedToPlugin;
	}
	
	/**
	 * @return the UID of the Plugin that this Dependency has to be resolved.
	 * 
	 * @author Miguel Reboiro Jato
	 */
	public String getResolveToPluginUID() {
		return this.resolveToPluginUID;
	}

	/**
	 * @return true if this Dependency is resolved.
	 */
	public boolean isResolved () {
		return resolvedToPlugin != null;
	}

	/**
	 * @return the exact required version or null if there is none.
	 */
	public PluginVersion getRequiredVersion () {
		return requiredVersion;
	}
//
//	/**
//	 * @return the minimum required version or null if there is none.
//	 */
//	public PluginVersion getMinVersion () {
//		return minVersion;
//	}
//
//	/**
//	 * @return the maximum required version or null if there is none.
//	 */
//	public PluginVersion getMaxVersion () {
//		return maxVersion;
//	}

	/**
	 * Sets whether this Dependency is required for the dependent Plugin to function. Dependencies are required by default. If this
	 * Dependency is optional and is resolved then setting it to be required will cause the dependent Plugin to be unresolved.
	 * @see Plugin#addExtension(Extension)
	 * 
	 * @param isOptional {@code true} if this dependency is required for the dependent Plugin to function. {@code false} otherwise. 
	 */
	synchronized public void setOptional (boolean isOptional) {
		if (dependentPlugin != null) {
			// If changing from optional to required and this Dependency is unresolved, unresolve the Plugin if it resolved.
			if (this.isOptional && !isOptional && resolvedToPlugin == null && dependentPlugin.isResolved()) {
				dependentPlugin.getPluginEngine().getLogger().log(LoggerLevel.FINE,
					"Unresolving Plugin because an unresolved optional Dependency was changed to required: " + dependentPlugin,
					null);
				dependentPlugin.getPluginEngine().unresolvePlugin(dependentPlugin, true);
			}
		}
		this.isOptional = isOptional;
	}

	/**
	 * @return whether this Dependency is required for the dependent Plugin to function.
	 */
	public boolean isOptional () {
		return isOptional;
	}

	/**
	 * Resolves this Dependency.
	 * 
	 * @param resolvedToPlugin the Plugin to which this Dependency is resolved.
	 */
	void resolve (Plugin resolvedToPlugin) {
		this.resolvedToPlugin = resolvedToPlugin;
		resolvedToPlugin.dependentPluginResolved(dependentPlugin);
	}

	/**
	 * Unresolves this Dependency.
	 */
	void unresolve () {
		resolvedToPlugin.dependentPluginUnresolved(dependentPlugin);
		resolvedToPlugin = null;
	}

	/**
	 * Checks if a plugin is compatible with this dependency.
	 * 
	 * @param plugin the Plugin to be checked.
	 * @return {@code true} if the specified Plugin meets this Dependency's requirements.
	 */
	public boolean isCompatible (Plugin plugin) {
		return resolveToPluginUID.equals(plugin.getUID()) && isVersionCompatible(plugin.getVersion());
	}

	/**
	 * Checks if a plugin version is compatible with this dependency.
	 * 
	 * @param dependentPluginVersion the plugin version to be checked.
	 * @return {@code true} if the specified version matches this Dependency's version rules.
	 */
	boolean isVersionCompatible (PluginVersion dependentPluginVersion) {
		if (dependentPluginVersion == null) throw new NullPointerException("Invalid argument: dependentPluginVersion");
		// Exact match.
		if (requiredVersion != null) return dependentPluginVersion.compareTo(requiredVersion) == 0;
		// Range match (or no match of both min and max versions are null).
//		return dependentPluginVersion.compareTo(minVersion, maxVersion) == 0;
		return true;
	}

	public String toString () {
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("dependentPlugin: ");
		buffer.append(dependentPlugin);
		buffer.append(", resolveToPluginUID: ");
		buffer.append(resolveToPluginUID);
		if (requiredVersion != null) {
			buffer.append(", requiredVersion: ");
			buffer.append(requiredVersion);
		}/* else {
			if (minVersion != null) {
				buffer.append(", minVersion: ");
				buffer.append(minVersion);
			}
			if (maxVersion != null) {
				buffer.append(", maxVersion: ");
				buffer.append(maxVersion);
			}
		}*/
		return buffer.toString();
	}
}
