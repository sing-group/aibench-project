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
 * PluginSingleVersion.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 08/04/2009
 */
package org.platonos.pluginengine.version;

/**
 * @author Miguel Reboiro Jato
 *
 */
public abstract class PluginSingleVersion extends PluginVersion {
	private final int releaseVersion;
	private final int updateVersion;
	private final int patchVersion;

	public PluginSingleVersion () {
		this(-1, -1, -1);
	}

	public PluginSingleVersion(int releaseVersion) {
		this(releaseVersion, -1, -1);
	}

	public PluginSingleVersion(int releaseVersion, int updateVersion) {
		this(releaseVersion, updateVersion, -1);
	}

	public PluginSingleVersion(int releaseVersion, int updateVersion, int patchVersion) {
		this.releaseVersion = releaseVersion;
		this.updateVersion = updateVersion;
		this.patchVersion = patchVersion;
	}

	/**
	 * @return the releaseVersion
	 */
	public final int getReleaseVersion() {
		return this.releaseVersion;
	}

	/**
	 * @return the updateVersion
	 */
	public final int getUpdateVersion() {
		return this.updateVersion;
	}

	/**
	 * @return the patchVersion
	 */
	public final int getPatchVersion() {
		return this.patchVersion;
	}
	
	/**
	 * Returns a String representation of this version, excluding the build version.
	 */
	public String getFullVersion () {
		return String.format("%d.%d.%d", this.releaseVersion, this.updateVersion, this.patchVersion);
	}
}
