
package org.platonos.pluginengine;

import org.platonos.pluginengine.logging.LoggerLevel;
import org.platonos.pluginengine.version.PluginVersion;

/**
 * Describes a dependency one Plugin has on another Plugin. The Dependency is required unless {@link #setOptional(boolean)}is set
 * to true.
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
	 * @param resolveToPluginUID Plugin UID that the dependent Plugin is dependent upon.
	 */
	public Dependency (Plugin dependentPlugin, String resolveToPluginUID) {
		if (dependentPlugin == null) throw new NullPointerException("Invalid argument: dependentPlugin");
		if (resolveToPluginUID == null) throw new NullPointerException("Invalid argument: resolveToPluginUID");
		this.dependentPlugin = dependentPlugin;
		this.resolveToPluginUID = resolveToPluginUID;
	}

	/**
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
	 * Returns the dependent Plugin that this Dependency applies to.
	 */
	public Plugin getDependentPlugin () {
		return dependentPlugin;
	}

	/**
	 * Returns the Plugin that this Dependency is resolved to or null if this Dependency is unresolved.
	 */
	public Plugin getResolvedToPlugin () {
		return resolvedToPlugin;
	}
	
	/**
	 * Return the UID of the Plugin that this Dependency has to be resolved.
	 * 
	 * @author Miguel Reboiro Jato
	 */
	public String getResolveToPluginUID() {
		return this.resolveToPluginUID;
	}

	/**
	 * Returns true if this Dependency is resolved.
	 */
	public boolean isResolved () {
		return resolvedToPlugin != null;
	}

	/**
	 * Returns the exact required version or null if there is none.
	 */
	public PluginVersion getRequiredVersion () {
		return requiredVersion;
	}
//
//	/**
//	 * Returns the minimum required version or null if there is none.
//	 */
//	public PluginVersion getMinVersion () {
//		return minVersion;
//	}
//
//	/**
//	 * Returns the maximum required version or null if there is none.
//	 */
//	public PluginVersion getMaxVersion () {
//		return maxVersion;
//	}

	/**
	 * Sets whether this Dependency is required for the dependent Plugin to function. Dependencies are required by default. If this
	 * Dependency is optional and is resolved then setting it to be required will cause the dependent Plugin to be unresolved.
	 * @see Plugin#addExtension(Extension)
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
	 * Returns whether this Dependency is required for the dependent Plugin to function.
	 */
	public boolean isOptional () {
		return isOptional;
	}

	/**
	 * Resolves this Dependency.
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
	 * Returns true if the specified Plugin meets this Dependency's requirements.
	 */
	public boolean isCompatible (Plugin plugin) {
		return resolveToPluginUID.equals(plugin.getUID()) && isVersionCompatible(plugin.getVersion());
	}

	/**
	 * Returns true if the specified version matches this Dependency's version rules.
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
