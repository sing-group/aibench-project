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
 * PluginDependencyVersion.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 03/04/2009
 */
package org.platonos.pluginengine.version;

/**
 * @author Miguel Reboiro Jato
 *
 */
public final class PluginDependencySingleVersion extends PluginSingleVersion {
	/**
	 * 
	 */
	public PluginDependencySingleVersion() {
		super();
	}
	
	/**
	 * @param releaseVersion
	 */
	public PluginDependencySingleVersion(int releaseVersion) {
		super(releaseVersion);
	}
	
	/**
	 * @param releaseVersion
	 * @param updateVersion
	 */
	public PluginDependencySingleVersion(int releaseVersion, int updateVersion) {
		super(releaseVersion, updateVersion);
	}

	/**
	 * @param releaseVersion
	 * @param updateVersion
	 * @param patchVersion
	 */
	public PluginDependencySingleVersion(int releaseVersion, int updateVersion,
			int patchVersion) {
		super(releaseVersion, updateVersion, patchVersion);
	}

	@Override
	public int compareTo(PluginDependencyMinMaxVersion version) {
		return -version.compareTo(this);
	}

	@Override
	public int compareTo(PluginDependencySingleVersion version) {
		int thisRelease = this.getReleaseVersion();
		int otherRelease = version.getReleaseVersion();
		if (thisRelease == -1 || otherRelease == -1) {
			return 0;
		} else if (thisRelease == otherRelease) {
			int thisUpdate = this.getUpdateVersion();
			int otherUpdate = version.getUpdateVersion();
			if (thisUpdate == -1 || otherUpdate == -1) {
				return 0;
			} else if (thisUpdate == otherUpdate) {
				int thisPatch = this.getPatchVersion();
				int otherPatch = version.getPatchVersion();
				if (thisPatch == -1 || otherPatch == -1 || thisPatch == otherPatch) {
					return 0;
				} else {
					return (thisPatch < otherPatch)?-1:1;
				}
			} else {
				return (thisUpdate < otherUpdate)?-1:1;
			}
		} else {
			return (thisRelease < otherRelease)?-1:1;
		}
	}
	
	@Override
	public int compareTo(PluginInstanceVersion version) {
		int thisRelease = this.getReleaseVersion();
		int otherRelease = version.getReleaseVersion();
		if (thisRelease == -1) {
			return 0;
		} else if (thisRelease == otherRelease) {
			int thisUpdate = this.getUpdateVersion();
			int otherUpdate = version.getUpdateVersion();
			if (thisUpdate == -1) {
				return 0;
			} else if (thisUpdate == otherUpdate) {
				int thisPatch = this.getPatchVersion();
				int otherPatch = version.getPatchVersion();
				if (thisPatch == -1 || thisPatch == otherPatch) {
					return 0;
				} else {
					return (thisPatch < otherPatch)?-1:1;
				}
			} else {
				return (thisUpdate < otherUpdate)?-1:1;
			}
		} else {
			return (thisRelease < otherRelease)?-1:1;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.platonos.pluginengine.PluginVersion#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof PluginDependencySingleVersion) {
			PluginDependencySingleVersion version = (PluginDependencySingleVersion) object;
			return version.getReleaseVersion() == this.getReleaseVersion()
				&& version.getUpdateVersion() == this.getUpdateVersion()
				&& version.getPatchVersion() == this.getPatchVersion();
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.platonos.pluginengine.PluginVersion#toString()
	 */
	@Override
	public String toString() {
		String release = (this.getReleaseVersion() < 0)?"*":Integer.toString(this.getReleaseVersion());
		String update = (this.getUpdateVersion() < 0)?"*":Integer.toString(this.getUpdateVersion());
		String patch = (this.getPatchVersion() < 0)?"*":Integer.toString(this.getPatchVersion());
		return String.format("%s.%s.%s", release, update, patch);
	}
}
