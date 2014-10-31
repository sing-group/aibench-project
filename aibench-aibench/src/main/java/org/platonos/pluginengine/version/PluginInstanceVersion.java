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
 * PluginInstanceVersion.java
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
public final class PluginInstanceVersion extends PluginSingleVersion {
	private final String buildVersion;
	
	/**
	 * 
	 */
	public PluginInstanceVersion() {
		this(0, 0, 0, "");
	}

	/**
	 * @param releaseVersion
	 */
	public PluginInstanceVersion(int releaseVersion) {
		this(releaseVersion, 0, 0, "");
	}

	/**
	 * @param releaseVersion
	 * @param updateVersion
	 */
	public PluginInstanceVersion(int releaseVersion, int updateVersion) {
		this(releaseVersion, updateVersion, 0, "");
	}

	/**
	 * @param releaseVersion
	 * @param updateVersion
	 * @param patchVersion
	 */
	public PluginInstanceVersion(int releaseVersion, int updateVersion,
			int patchVersion) {
		this(releaseVersion, updateVersion, patchVersion, "");
	}

	/**
	 * @param releaseVersion
	 * @param updateVersion
	 * @param patchVersion
	 * @param buildVersion
	 */
	public PluginInstanceVersion(int releaseVersion, int updateVersion,
			int patchVersion, String buildVersion) {
		super(releaseVersion, updateVersion, patchVersion);
		this.buildVersion = buildVersion.trim();
	}
	
	public final String getBuildVersion() {
		return this.buildVersion;
	}

	/**
	 * Returns a String representation of this version, excluding the build version.
	 */
	public String getFullVersion () {
		return String.format("%d.%d.%d", this.getReleaseVersion(), this.getUpdateVersion(), this.getPatchVersion());
	}

	@Override
	public int compareTo(PluginDependencyMinMaxVersion version) {
		return -version.compareTo(this);
	}

	@Override
	public int compareTo(PluginDependencySingleVersion version) {
		return -version.compareTo(this);
	}

	@Override
	public int compareTo(PluginInstanceVersion version) {
		int thisRelease = this.getReleaseVersion();
		int otherRelease = version.getReleaseVersion();
		if (thisRelease == otherRelease) {
			int thisUptade = this.getUpdateVersion();
			int otherUpdate = version.getUpdateVersion();
			if (thisUptade == otherUpdate) {
				int thisPatch = this.getPatchVersion();
				int otherPatch = version.getPatchVersion();
				if (thisPatch == otherPatch) {
					return 0;
				} else {
					return (thisPatch < otherPatch)?-1:1;
				}
			} else {
				return (thisUptade < otherUpdate)?-1:1;
			}
		} else {
			return (thisRelease  < otherRelease)?-1:1;
		}
	}

	/**
	 * Returns false if the specified Object is not a PluginVersion instance or it has release, update, patch, or build versions
	 * that differ from this PluginVersion.
	 */
	@Override
	public boolean equals (Object object) {
		if (object instanceof PluginInstanceVersion) {
			PluginInstanceVersion version = (PluginInstanceVersion) object;
			return version.getReleaseVersion() == this.getReleaseVersion()
				&& version.getUpdateVersion() == this.getUpdateVersion()
				&& version.getPatchVersion() == this.getPatchVersion()
				&& version.getBuildVersion().equals(this.getBuildVersion());
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.platonos.pluginengine.PluginVersion#toString()
	 */
	@Override
	public String toString() {
		String release = (this.getReleaseVersion() <= 0)?"0":Integer.toString(this.getReleaseVersion());
		String update = (this.getUpdateVersion() <= 0)?"0":Integer.toString(this.getUpdateVersion());
		String patch = (this.getPatchVersion() <= 0)?"0":Integer.toString(this.getPatchVersion());
		String build = this.getBuildVersion().trim();
		if (build == null || build.length() == 0) {
			return String.format("%s.%s.%s", release, update, patch);
		} else {
			return String.format("%s.%s.%s %s", release, update, patch, build);
		}
	}
}
