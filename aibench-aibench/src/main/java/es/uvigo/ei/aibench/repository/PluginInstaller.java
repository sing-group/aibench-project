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
package es.uvigo.ei.aibench.repository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.repository.info.InstallInfo;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginInstaller {
	private final static Logger logger = Logger.getLogger(PluginInstaller.class);
	
	private final static String INSTALL_FILE_FORMAT = ".%s.install"; 
	private final static String OLD_FILE_FORMAT = ".%s.old"; 
	/**
	 * 
	 */
	private static final String DEFAULT_INFO_FILE = "plugins.dat";
	
	/**
	 * 
	 */
	private static final int BUFFER_SIZE = 8192;
	
	private final String[] ignoreDirs;
	private final File pluginsDir;
	private final File installDir;
	
	private final FileFilter directoryFilter  = new FileFilter() {
		
		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				if (PluginInstaller.this.ignoreDirs != null) {
					for (String ignore:PluginInstaller.this.ignoreDirs) {
						if (ignore.equals(pathname.getName())) {
							return false;
						}
					}
				}
				
				return true;
			} else {
				return false;
			}
			
		}
	};
	
	public PluginInstaller(String pluginsDir, String installDir)
	throws IllegalArgumentException {
		this(pluginsDir, installDir, null);
	}

	public PluginInstaller(String pluginsDir, String installDir, String[] ignoreDirs) 
	throws IllegalArgumentException {
		this.pluginsDir = new File(pluginsDir);
		this.installDir = new File(installDir);
		this.ignoreDirs = ignoreDirs;
		
		if (!this.pluginsDir.isDirectory() && !this.pluginsDir.mkdir())
			throw new IllegalArgumentException(String.format("File '%s' isn't a directory.", pluginsDir));
		if (!this.installDir.isDirectory() && !this.installDir.mkdir())
			throw new IllegalArgumentException(String.format("File '%s' isn't a directory.", installDir));
	}
	
	/**
	 * @return the pluginsDir
	 */
	public File getPluginsDir() {
		return this.pluginsDir;
	}

	/**
	 * @return the pluginsDir
	 */
	public File getInstallDir() {
		return this.installDir;
	}
	
	public List<String> getInstallUIDs() {
		File[] files = this.installDir.listFiles(this.directoryFilter);
		ArrayList<String> uids = new ArrayList<String>(files.length);
		for (File file:files) {
			uids.add(file.getName());
		}
		return uids;
	}
	
	public List<String> getValidInstallUIDs() {
		File[] files = this.installDir.listFiles(this.directoryFilter);
		ArrayList<String> uids = new ArrayList<String>(files.length);
		for (File file:files) {
			if (this.isValidInstall(file.getName())) {
				uids.add(file.getName());
			}
		}
		return uids;
	}
	
	public InstallInfo getInstallInfo(String uid) {
		if (this.isValidInstall(uid)) {
			try {
				return new InstallInfo(new File(new File(uid), PluginInstaller.DEFAULT_INFO_FILE));
			} catch (IOException e) {
				PluginInstaller.logger.error("Error retrieving plugin install information: " + uid, e);
			}
		}
		return null;
	}
	
	public boolean isValidInstall(String uid) {
		File updateDir = new File(this.installDir, uid);
		if (updateDir.exists() && updateDir.isDirectory()) {
			try {
				InstallInfo info = new InstallInfo(updateDir);
				if (info.file == null) 
					return false;
				if (!Inflater.inflatableFile(info.file))
					return false;
				File pluginFile = new File(updateDir, info.file);
				if (!pluginFile.exists()) {
					return false;
				}
				if (info.md5 == null) {
					return true;
				} else {
					return PluginInstaller.checkMD5(info.md5, pluginFile);
				}
			} catch (IOException e) {
				PluginInstaller.logger.error("Error validating install files: " + uid, e);
				return false;
			} catch (NoSuchAlgorithmException e) {
				PluginInstaller.logger.error("Error validating md5 checksum for plugin install files: " + uid, e);
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean deleteInstall(String uid) {
		PluginInstaller.logger.info("Deleting install files: " + uid);
		File file = new File(this.installDir, uid);
		if (file.exists() && file.isDirectory() && PluginInstaller.deleteFile(file)) {
			PluginInstaller.logger.info("Install files deleted: " + uid);
			return true;
		} else {
			PluginInstaller.logger.warn("Install files could not be deleted: " + uid);
			return false;
		}
	}
	
	public void clearInvalidInstalls() {
		for (File file:this.installDir.listFiles(this.directoryFilter)) {
			if (!this.isValidInstall(file.getName())) {
				PluginInstaller.deleteFile(file);
			}
		}
	}
	
	public void installPlugins() {
		this.installPlugins(true);
	}
	
	public void installPlugins(boolean deleteInvalidUpdates) {
		for (String uid:this.getInstallUIDs()) {
			PluginInstaller.logger.info("Installing plugin: " + uid);
			if (this.isValidInstall(uid)) {
				try {
					if (this.installPlugin(uid)) {
						PluginInstaller.logger.info("Plugin installed: " + uid);
					} else {
						PluginInstaller.logger.error("Plugin can't be installed: " + uid);
						if (deleteInvalidUpdates) {
							PluginInstaller.logger.error(
								String.format("Error installing plugin %s. The plugin install files will be deleted.", uid)
							);
							this.deleteInstall(uid);
						}
					}
				} catch (IOException ioe) {
					if (deleteInvalidUpdates) {
						PluginInstaller.logger.error(
							String.format("Error installing plugin %s. The plugin install files will be deleted.", uid),
							ioe
						);
						this.deleteInstall(uid);
					} else {
						PluginInstaller.logger.error("Error installing plugin: " + uid, ioe);
					}
				}
			} else {
				PluginInstaller.logger.warn("Install files not valid: " + uid);
				if (deleteInvalidUpdates) 
					this.deleteInstall(uid);
			}
		}
	}
	
	public boolean installPlugin(String uid)
	throws IOException {
		File uidInstallDir = new File(this.installDir, uid);
		
		InstallInfo info = new InstallInfo(uidInstallDir);
		if (info.file.endsWith("jar")) {
			File jarFile = new File(uidInstallDir, info.file);
			if (info.updatePlugin == null) {
				if (jarFile.renameTo(new File(this.pluginsDir, info.file))) {
					PluginInstaller.deleteFile(uidInstallDir);
					return true;
				} else {
					return false;
				}
			} else {
				File updatePluginFile = new File(this.pluginsDir, info.updatePlugin);
				File updatePluginOld = new File(
					this.pluginsDir, 
					String.format(PluginInstaller.OLD_FILE_FORMAT, info.updatePlugin)
				);
				if (updatePluginFile.renameTo(updatePluginOld) 
					&& jarFile.renameTo(new File(this.pluginsDir, info.file))) {
					PluginInstaller.deleteFile(uidInstallDir);
					PluginInstaller.deleteFile(updatePluginOld);
					return true;
				} else {
					return false;
				}
			}
		} else {
			File installFile = new File(uidInstallDir, info.file);
			if (info.updatePlugin == null) {
				File pluginDir = new File(this.pluginsDir, uid);
				File outputDir = new File(
					this.pluginsDir, 
					String.format(PluginInstaller.INSTALL_FILE_FORMAT, uid)
				);
				if (Inflater.inflate(installFile, outputDir) &&
					outputDir.renameTo(pluginDir)) {
					PluginInstaller.deleteFile(uidInstallDir);
					return true;
				} else {
					return false;
				}
			} else {
				File updatePluginFile = new File(this.pluginsDir, info.updatePlugin);
				File pluginDir = new File(this.pluginsDir, uid);
				File updatePluginOld = new File(
					this.pluginsDir, 
					String.format(PluginInstaller.OLD_FILE_FORMAT, info.updatePlugin)
				);
				File outputDir = new File(
					this.pluginsDir, 
					String.format(PluginInstaller.INSTALL_FILE_FORMAT, uid)
				);
				if (updatePluginFile.renameTo(updatePluginOld) 
					&& Inflater.inflate(installFile, outputDir) 
					&& outputDir.renameTo(pluginDir)) {
					PluginInstaller.deleteFile(uidInstallDir);
					PluginInstaller.deleteFile(updatePluginOld);
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	private static boolean checkMD5(String testMD5, File testFile)
	throws IOException, NoSuchAlgorithmException {
		BufferedInputStream bis = null;
		try {
			if (testMD5 == null) {
				return false;
			} else {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				bis = new BufferedInputStream(new FileInputStream(testFile), PluginInstaller.BUFFER_SIZE);
				byte[] data = new byte[PluginInstaller.BUFFER_SIZE];
				int len;
				while ((len = bis.read(data)) != -1) {
					md5.update(data, 0, len);
				}
				
				BigInteger bigInt = new BigInteger(1, md5.digest());
				String testFileMD5 = bigInt.toString(16);
				if (testMD5.equalsIgnoreCase(testFileMD5)) {
					return true;
				} else {
					PluginInstaller.logger.info(String.format("MD5 test not passed (MD5: %s; File: %s)", testMD5, testFile.getPath()));
					return false;
				}
			}
		} finally {
			try {
				if (bis != null) bis.close();
			} catch (IOException e) {}
		}
	}
	
	public static boolean deleteFile(File file) {
		if (file.isDirectory()) {
			for (File childFile:file.listFiles()) {
				PluginInstaller.deleteFile(childFile);
			}
		}
		return file.delete();
	}
}
