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
 * PluginDependencyMinMaxVersion.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 06/04/2009
 */
package org.platonos.pluginengine.version;

/**
 * @author Miguel Reboiro Jato
 *
 */
public final class PluginDependencyMinMaxVersion extends PluginVersion {
	public static enum Type {MIN, MAX};
	private final PluginDependencySingleVersion minVersion;
	private final PluginDependencySingleVersion maxVersion;
	
	public PluginDependencyMinMaxVersion(PluginDependencySingleVersion version, Type type) {
		if (version == null) throw new NullPointerException("Invalid argument: version");
		this.minVersion = (type == Type.MIN)?version:null;
		this.maxVersion = (type == Type.MAX)?version:null;
	}
	
	public PluginDependencyMinMaxVersion(
		PluginDependencySingleVersion minVersion, 
		PluginDependencySingleVersion maxVersion
	) {
		if (minVersion == null) throw new NullPointerException("Invalid argument: minVersion");
		if (maxVersion == null) throw new NullPointerException("Invalid argument: maxVersion");
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
	}
	
	public PluginDependencySingleVersion getMinVersion() {
		return this.minVersion;
	}
	
	public PluginDependencySingleVersion getMaxVersion() {
		return this.maxVersion;
	}

	/* (non-Javadoc)
	 * @see org.platonos.pluginengine.PluginVersion#getFullVersion()
	 */
	@Override
	public String getFullVersion() {
		return this.toString();
	}

	@Override
	public int compareTo(PluginDependencyMinMaxVersion version) {
		PluginDependencySingleVersion thisMinVersion = this.getMinVersion();
		PluginDependencySingleVersion thisMaxVersion = this.getMaxVersion();
		PluginDependencySingleVersion otherMinVersion = version.getMinVersion();
		PluginDependencySingleVersion otherMaxVersion = version.getMaxVersion();
		if (thisMinVersion == null) {
			if (otherMinVersion == null || this.getMaxVersion().compareTo(otherMinVersion) >= 0) {
				return 0;
			} else {
				return -1;
			}
		} else if (thisMaxVersion == null) {
			if (otherMaxVersion == null || this.getMinVersion().compareTo(otherMaxVersion) <= 0) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (otherMinVersion != null && this.maxVersion.compareTo(otherMinVersion) < 0) {
				return -1;
			} else if (otherMaxVersion != null && this.minVersion.compareTo(otherMaxVersion) > 0) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public int compareTo(PluginDependencySingleVersion version) {
		if (this.getMinVersion() != null) {
			int cmpMinVersion = version.compareTo(this.getMinVersion());
			if (cmpMinVersion <= 0) {
				return cmpMinVersion;
			}
		}
		if (this.getMaxVersion() != null) {
			int cmpMaxVersion = version.compareTo(this.getMaxVersion());
			if (cmpMaxVersion >= 0) {
				return cmpMaxVersion;
			}
		}
		
		return 0;
	}

	@Override
	public int compareTo(PluginInstanceVersion version) {
		PluginDependencySingleVersion thisMinVersion = this.getMinVersion();
		PluginDependencySingleVersion thisMaxVersion = this.getMaxVersion();
		if (thisMinVersion == null) {
			return Math.min(thisMaxVersion.compareTo(version), 0);
		} else if (thisMaxVersion == null) {
			return Math.max(thisMinVersion.compareTo(version), 0);
		} else {
			if (thisMaxVersion.compareTo(version) >= 0) {
				return Math.max(thisMinVersion.compareTo(version), 0);
			} else {
				return -1;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PluginDependencyMinMaxVersion) {
			PluginDependencyMinMaxVersion version = (PluginDependencyMinMaxVersion) obj;
			if (this.getMinVersion() == null && version.getMinVersion() == null) {
				return this.getMaxVersion().equals(version.getMaxVersion());
			} else if (this.getMaxVersion() == null && version.getMaxVersion() == null) {
				return this.getMinVersion().equals(version.getMinVersion());
			} else {
				return this.getMaxVersion().equals(version.getMaxVersion()) &&
						this.getMinVersion().equals(version.getMinVersion());
			}
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.platonos.pluginengine.PluginVersion#toString()
	 */
	@Override
	public String toString() {
		if (this.getMinVersion() != null && this.getMaxVersion() != null) {
			return String.format("%s, %s", this.getMinVersion().toString(), this.getMaxVersion().toString());
		} else if (this.getMinVersion() != null) {
			return String.format("%s+", this.getMinVersion());
		} else {
			return String.format("%s-", this.getMaxVersion());
		}
	}
}
