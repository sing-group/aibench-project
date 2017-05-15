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

import java.util.ArrayList;
import java.util.List;

import org.platonos.pluginengine.PluginEngineException;
import org.platonos.pluginengine.version.PluginVersion;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginInfo {
	private final String pluginID;
	private final String host;
	private String uid;
	private String name;
	private String version;
	private String needs;
	private String file;
	private String md5;
	
	private PluginVersion pluginVersion;
	
	public PluginInfo(String pluginID, String host) {
		if (pluginID == null)
			throw new NullPointerException("The pluginID is null");
		if (host == null)
			throw new NullPointerException("The host is null");
		this.pluginID = pluginID;
		this.host = host;
	}
	
	public PluginInfo(String pluginID, String host, String uid, String name, String version,
			String needs, String file, String md5) {
		this(pluginID, host);
		this.uid = uid;
		this.name = name;
		this.version = version;
		this.needs = needs;
		this.file = file;
		this.md5 = md5;
	}
	
	public void setValue(String property, String value) {
		if (property.equalsIgnoreCase("uid")) {
			this.setUID(value);
		} else if (property.equalsIgnoreCase("name")) {
			this.setName(value);
		} else if (property.equalsIgnoreCase("version")) {
			this.setVersion(value);
		} else if (property.equalsIgnoreCase("needs")) {
			this.setNeeds(value);
		} else if (property.equalsIgnoreCase("file")) {
			this.setFile(value);
		} else if (property.equalsIgnoreCase("md5")) {
			this.setMd5(value);
		}
	}

	public final String getPluginID() {
		return this.pluginID;
	}
	
	public final String getHost() {
		return this.host;
	}

	public String getUID() {
		return this.uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
		this.pluginVersion = null;
	}

	public String getNeeds() {
		return (this.needs==null)?"":this.needs;
	}

	public void setNeeds(String needs) {
		this.needs = needs;
	}
	
	public List<DependencyInfo> getListNeeds() {
		if (this.needs == null || this.needs.trim().length() == 0) 
			return new ArrayList<DependencyInfo>(0);
		
		String[] dependencies = needs.trim().split(";");
		List<DependencyInfo> info = new ArrayList<DependencyInfo>(dependencies.length);
		for (String dependency:dependencies) {
			try {
				info.add(new DependencyInfo(dependency));
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			}
		}
		return info;
	}

	public String getFile() {
		return this.file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setMd5(String md5) {
		this.md5 = md5.trim();
	}
	
	public String getMd5() {
		return (this.md5 == null || this.md5.length() == 0)?null:this.md5;
	}
	
	public InstallInfo getInstallInfo() {
		return new InstallInfo(
			this.getFile(),
			null,
			this.version,
			this.getMd5()
		);
	}
	
	public InstallInfo getInstallInfo(String updatePlugin) {
		return new InstallInfo(
			this.getFile(),
			updatePlugin,
			this.version,
			this.getMd5()
		);
	}
	
	private void createPluginVersion()
	throws PluginEngineException {
		if (this.pluginVersion == null && this.version != null) {
			this.pluginVersion = PluginVersion.createInstanceVersion(this.getVersion());
		}
	}
	
	public PluginVersion getPluginVersion()
	throws PluginEngineException {
		if (this.pluginVersion == null) {
			this.createPluginVersion();
		}
		
		return this.pluginVersion;
	}
	
	@Override
	public String toString() {
		String lineFormat = String.format("%s.%%s=%%s\n", this.pluginID);
		String toret = String.format(lineFormat, "uid", this.getUID());
		toret += String.format(lineFormat, "host", this.getHost());
		toret += String.format(lineFormat, "name", (this.getName() == null) ? "" : this.getVersion());
		toret += String.format(lineFormat, "version", (this.getVersion() == null) ? "" : this.getVersion());
		toret += String.format(lineFormat, "needs", (this.getNeeds() == null) ? "" : this.getNeeds());
		toret += String.format(lineFormat, "file", (this.getFile() == null) ? "" : this.getFile());
		toret += String.format("%s.%s=%s\n", this.pluginID, "md5", (this.getMd5() == null) ? "" : this.getMd5());
		return toret;
	}
}
