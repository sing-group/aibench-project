/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
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
	
//	private final ReadWriteLock pluginVersionLock = new ReentrantReadWriteLock();
	
	/**
	 * 
	 * @param pluginID
	 */
	public PluginInfo(String pluginID, String host) {
		if (pluginID == null)
			throw new NullPointerException("The pluginID is null");
		if (host == null)
			throw new NullPointerException("The host is null");
		this.pluginID = pluginID;
		this.host = host;
	}
	
	/**
	 * @param pluginID
	 * @param uid
	 * @param name
	 * @param version
	 * @param needs
	 * @param file
	 */
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

	/**
	 * @return the pluginID
	 */
	public final String getPluginID() {
		return this.pluginID;
	}
	
	/**
	 * 
	 * @return the host
	 */
	public final String getHost() {
		return this.host;
	}

	/**
	 * @return the uid
	 */
	public String getUID() {
		return this.uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUID(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
//		try {
//			this.pluginVersionLock.readLock().lock();
			
			return this.version;
//		} finally {
//			this.pluginVersionLock.readLock().unlock();
//		}
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
//		try {
//			this.pluginVersionLock.writeLock().lock();

			this.version = version;
			this.pluginVersion = null;
//		} finally {
//			this.pluginVersionLock.writeLock().unlock();
//		}
	}

	/**
	 * @return the needs
	 */
	public String getNeeds() {
		return (this.needs==null)?"":this.needs;
	}

	/**
	 * @param needs the needs to set
	 */
	public void setNeeds(String needs) {
		this.needs = needs;
	}
	
	/**
	 * 
	 * @return
	 */
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

	/**
	 * @return the file
	 */
	public String getFile() {
		return this.file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @param md5
	 */
	public void setMd5(String md5) {
		this.md5 = md5.trim();
	}
	
	/**
	 * 
	 * @param md5
	 * @return
	 */
	public String getMd5() {
		return (this.md5 == null || this.md5.length() == 0)?null:this.md5;
	}
	
	/**
	 * 
	 * @return
	 */
	public InstallInfo getInstallInfo() {
		return new InstallInfo(
			this.getFile(),
			null,
			this.version,
			this.getMd5()
		);
	}
	
	/**
	 * 
	 * @param updatePlugin
	 * @return
	 */
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
//		try {
//			this.pluginVersionLock.writeLock().lock();
//			
			if (this.pluginVersion == null && this.version != null) {
				this.pluginVersion = PluginVersion.createInstanceVersion(this.getVersion());
			}
//		} finally {
//			this.pluginVersionLock.writeLock().unlock();
//		}
	}
	
	public PluginVersion getPluginVersion()
	throws PluginEngineException {
//		try {
//			this.pluginVersionLock.readLock().lock();
			if (this.pluginVersion == null) {
//				this.pluginVersionLock.readLock().unlock();
				this.createPluginVersion();
//				this.pluginVersionLock.readLock().lock();
			}
			
			return this.pluginVersion;
//		} finally {
//			this.pluginVersionLock.readLock().unlock();
//		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String lineFormat = String.format("%s.%%s=%%s\n", this.pluginID);
		String toret = String.format(lineFormat, "uid", this.getUID());
		toret += String.format(lineFormat, "host", this.getHost());
		toret += String.format(lineFormat, "name", (this.getName()==null)?"":this.getVersion());
		toret += String.format(lineFormat, "version", (this.getVersion()==null)?"":this.getVersion());
		toret += String.format(lineFormat, "needs", (this.getNeeds()==null)?"":this.getNeeds());
		toret += String.format(lineFormat, "file", (this.getFile()==null)?"":this.getFile());
		toret += String.format("%s.%s=%s\n", this.pluginID, "md5", (this.getMd5() == null)?"":this.getMd5());
		return toret;
	}
}
