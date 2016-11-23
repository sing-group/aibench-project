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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author Miguel Reboiro Jato
 *
 */
public class InstallInfo {
	/**
	 * 
	 */
	private static final String PROPERTY_VERSION = "version";
	/**
	 * 
	 */
	private static final String INSTALL_INFO_FILE = "install.info";
	/**
	 * 
	 */
	private static final String PROPERTY_FILE = "file";
	/**
	 * 
	 */
	private static final String PROPERTY_UPDATEPLUGIN = "updateplugin";
	/**
	 * 
	 */
	private static final String PROPERTY_MD5 = "md5";
	
	public final String file;
	public final String updatePlugin;
	public final String version;
	public final String md5;
	
	public InstallInfo(String file, String updatePlugin, String version, String md5) {
		this.file = file;
		this.updatePlugin = updatePlugin;
		this.version = version;
		this.md5 = md5;
	}
	
	public InstallInfo(File dir) 
	throws IOException {
		Properties properties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(dir, InstallInfo.INSTALL_INFO_FILE));
			properties.load(fis);
			
			this.file = properties.getProperty(InstallInfo.PROPERTY_FILE);
			this.updatePlugin = properties.getProperty(InstallInfo.PROPERTY_UPDATEPLUGIN);
			this.version = properties.getProperty(InstallInfo.PROPERTY_VERSION);
			this.md5 = properties.getProperty(InstallInfo.PROPERTY_MD5);
		} finally {
			try {
				if (fis != null) fis.close();
			} catch(IOException ioe) {}
		}
	}
	
	public void store(File dir) 
	throws IOException {
		Properties properties = new Properties();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dir, InstallInfo.INSTALL_INFO_FILE));
			
			if (this.file != null)
				properties.setProperty(InstallInfo.PROPERTY_FILE, this.file);
			if (this.updatePlugin != null)
				properties.setProperty(InstallInfo.PROPERTY_UPDATEPLUGIN, this.updatePlugin);
			if (this.version != null)
				properties.setProperty(InstallInfo.PROPERTY_VERSION, this.version);
			if (this.md5 != null)
				properties.setProperty(InstallInfo.PROPERTY_MD5, this.md5);
			
			properties.store(fos, null);
		} finally {
			try {
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch(IOException ioe) {}
		}
	}
}