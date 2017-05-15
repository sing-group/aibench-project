/*
 * #%L
 * The AIBench Plugin Manager Plugin
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
package es.uvigo.ei.sing.aibench.pluginmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.platonos.pluginengine.IPluginConfiguration;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginEngineException;
import org.platonos.pluginengine.version.PluginVersion;

import es.uvigo.ei.aibench.Launcher;
import es.uvigo.ei.aibench.Paths;
import es.uvigo.ei.aibench.repository.NotInitializedException;
import es.uvigo.ei.aibench.repository.PluginDownloadAdapter;
import es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent;
import es.uvigo.ei.aibench.repository.PluginDownloader;
import es.uvigo.ei.aibench.repository.PluginInstaller;
import es.uvigo.ei.aibench.repository.info.InstallInfo;
import es.uvigo.ei.aibench.repository.info.PluginInfo;

/**
 * @author Miguel Reboiro Jato
 *
 */
public final class PluginManager extends PluginDownloadAdapter {
	/**
	 * 
	 */
	private static final String PROPERTY_PLUGININSTALLER_DELETE_INVALID_INSTALLS = "plugininstaller.delete_invalid_installs";
	/**
	 * 
	 */
	private static final String PROPERTY_PLUGININSTALLER_IGNORE_DIRS = "plugininstaller.ignore_dirs";
	private static Logger logger = Logger.getLogger(PluginManager.class); 
	/**
	 * 
	 */
	private static final String PROPERTIES_FILE = Paths.getInstance().getPluginManagerConfigurationPath();//"conf/pluginmanager.conf";
	/**
	 * 
	 */
	private static final String PROPERTY_PLUGINREPOSITORY_HOST = "pluginrepository.host";
	/**
	 * 
	 */
	private static final String PROPERTY_PLUGINREPOSITORY_INFOFILE = "pluginrepository.infofile";
	/**
	 * 
	 */
	private static final String PROPERTY_PLUGININSTALLER_DIR = "plugininstaller.dir";
	
	private static PluginManager instance;
	private synchronized final static void createInstance() {
		if (PluginManager.instance == null) {
			PluginManager.instance = new PluginManager();
		}
	}
	
	public final static PluginManager getInstance() {
		if (PluginManager.instance == null) {
			PluginManager.createInstance();
		}
		return PluginManager.instance;
	}
	
	private final ReadWriteLock downloaderLock = new ReentrantReadWriteLock();
	private final ReadWriteLock installerLock = new ReentrantReadWriteLock();
	private final ReadWriteLock installDirectoryLock = new ReentrantReadWriteLock();
	
	private final PluginEngine pluginEngine;
	private final IPluginConfiguration pluginConfiguration;
	private final Properties properties;
	
	private String host;
	private String infoFile;
	private String installDir;
	private String[] ignoreDirs;
	private boolean deleteInvalidInstalls;
	
	private PluginDownloader downloader;
	private PluginInstaller installer;
	
	private final List<PluginManagerListener> listeners;
	
	private PluginManager() {
		this.pluginEngine = Launcher.getPluginEngine();
		this.pluginConfiguration = this.pluginEngine.getPluginConfiguration();
		this.listeners = new Vector<PluginManagerListener>();
		
		this.properties = new Properties();
//		try {
//			this.properties.load(new FileInputStream(PluginManager.PROPERTIES_FILE));
//			this.host = this.properties.getProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_HOST);
//			this.infoFile = this.properties.getProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_INFOFILE);
//			this.installDir = this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_DIR);
//			this.deleteInvalidInstalls = Boolean.parseBoolean(this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_DELETE_INVALID_INSTALLS, "true"));
//			
//			String ignoreDirs = this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_IGNORE_DIRS);
//			if (ignoreDirs != null && ignoreDirs.trim().length() > 0) {
//				this.ignoreDirs = ignoreDirs.split(";");
//			} else {
//				this.ignoreDirs = null;
//			}
//			
//			
//			this.initPluginInstaller();
//			this.initPluginDownloader();
//		} catch (FileNotFoundException fnfe) {
//			PluginManager.logger.error("Properties file not found: " + PluginManager.PROPERTIES_FILE, fnfe);
//		} catch (IOException ioe) {
//			PluginManager.logger.error("Error in plugin downloader: " + ioe.getMessage(), ioe);
//		} catch (IllegalArgumentException iae) {
//			PluginManager.logger.error("Error in plugin installer: " + iae.getMessage(), iae);
//		}
		
		try {
			this.properties.load(new FileInputStream(PluginManager.PROPERTIES_FILE));
			this.host = this.properties.getProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_HOST);
			this.infoFile = this.properties.getProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_INFOFILE);
			this.installDir = this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_DIR);
			this.deleteInvalidInstalls = Boolean.parseBoolean(this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_DELETE_INVALID_INSTALLS, "true"));
			
			String ignoreDirs = this.properties.getProperty(PluginManager.PROPERTY_PLUGININSTALLER_IGNORE_DIRS);
			if (ignoreDirs != null && ignoreDirs.trim().length() > 0) {
				this.ignoreDirs = ignoreDirs.split(";");
			} else {
				this.ignoreDirs = null;
			}

			this.initPluginInstaller();
			this.initPluginDownloader(this.host, this.infoFile, this.installDir);
		} catch (FileNotFoundException fnfe) {
			PluginManager.logger.error("Properties file not found: " + PluginManager.PROPERTIES_FILE, fnfe);
		} catch (IOException ioe) {
			PluginManager.logger.error("Error in plugin downloader: " + ioe.getMessage(), ioe);
		} catch (IllegalArgumentException iae) {
			PluginManager.logger.error("Error in plugin installer: " + iae.getMessage(), iae);
		}
	}
	
	/*-************************* PLUGIN MANAGER *************************-*/
	public String getHost() {
		return this.host;
	}
	
	public String getInfoFile() {
		return this.infoFile;
	}
	
	public String getInstallDir() {
		return this.installDir;
	}
	
	private void storeProperties() {
		try {
			this.properties.store(new FileOutputStream(PluginManager.PROPERTIES_FILE), null);
		} catch (IOException ioe) {
			PluginManager.logger.error("Properties file not found: " + PluginManager.PROPERTIES_FILE, ioe);
		}
	}
	/*-*********************** END PLUGIN MANAGER ***********************-*/
	
	
	/*-************************* PLUGIN ENGINE *************************-*/
	/**
	 * @return the pluginEngine
	 */
	public PluginEngine getPluginEngine() {
		return this.pluginEngine;
	}
	
	public Plugin getInstalledPlugin(String pluginUID) {
		Plugin plugin = this.pluginEngine.getPlugin(pluginUID);
		if (plugin == null) {
			for (Plugin p:this.pluginEngine.getUnloadedPlugins()) {
				if (p.getUID().equalsIgnoreCase(pluginUID)) {
					plugin = p;
					break;
				}
			}
		}
		return plugin;
	}

	public Plugin getPlugin(String pluginUID) {
		return this.pluginEngine.getPlugin(pluginUID);
	}
	
	public List<Plugin> getPlugins() {
		return this.pluginEngine.getPlugins();
	}
	
	public List<Plugin> getActivePlugins() {
		ArrayList<Plugin> active = new ArrayList<Plugin>(this.pluginEngine.getLoadedPlugins());
		active.retainAll(this.pluginEngine.getResolvedPlugins());
		
		return active;
	}
	
	public List<Plugin> getInactivePlugins() {
		ArrayList<Plugin> inactive = new ArrayList<Plugin>(this.pluginEngine.getUnloadedPlugins());
		inactive.addAll(this.pluginEngine.getUnresolvedPlugins());
		
		return inactive;
	}
	
	public boolean pluginExists(String uid) {
		for (Plugin plugin:this.pluginEngine.getLoadedPlugins()) {
			if (plugin.getUID().equalsIgnoreCase(uid)) {
				return true;
			}
		}
		for (Plugin plugin:this.pluginEngine.getUnloadedPlugins()) {
			if (plugin.getUID().equalsIgnoreCase(uid)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isActive(Plugin plugin) {
		return this.pluginEngine.isLoaded(plugin) && this.pluginEngine.isEnabled(plugin);
	}
	
	public boolean isActive(String pluginUID) {
		return this.pluginEngine.isLoaded(pluginUID) && this.pluginEngine.isEnabled(pluginUID);
	}
	
	public boolean isCurrentlyEnabled(String uid) {
		Plugin plugin = this.getInstalledPlugin(uid);
		if (plugin == null) {
			return false;
		} else {
			return !plugin.isDisabled();
		}
	}
	
	public boolean isCurrentlyDisabled(String uid) {
		Plugin plugin = this.getInstalledPlugin(uid);
		if (plugin == null) {
			return false;
		} else {
			return plugin.isDisabled();
		}
	}
	
	public boolean isEnabled(Plugin plugin) {
		return this.pluginConfiguration.isLoadPlugin(plugin) && this.pluginConfiguration.isEnabledPlugin(plugin);
	}
	
	public boolean isEnabled(String pluginUID) {
		return this.pluginConfiguration.isLoadPlugin(pluginUID) && this.pluginConfiguration.isEnabledPlugin(pluginUID);
	}
	
	public void setEnabled(Plugin plugin, boolean active) {
		synchronized (this.pluginConfiguration) {
			this.pluginConfiguration.setLoadPlugin(plugin, active);
			this.pluginConfiguration.setEnabledPlugin(plugin, active);
		}
	}
	
	public void setEnabled(String pluginUID, boolean active) {
		synchronized (this.pluginConfiguration) {
			this.pluginConfiguration.setLoadPlugin(pluginUID, active);
			this.pluginConfiguration.setEnabledPlugin(pluginUID, active);
		}
	}
	/*-*********************** END PLUGIN ENGINE ***********************-*/
	
	/*-*********************** PLUGIN DOWNLOADER ***********************-*/
	private void initPluginDownloader(String host, String infoFile, String installDir) {
		try {
			this.downloaderLock.writeLock().lock();
			
			if (host == null) {
				throw new NotInitializedException("Error creating PluginDownloader: Host isn't set.");
			} else if (installDir == null) {
				throw new NotInitializedException("Error creating PluginDownloader: Install directory isn't set.");
			} else {
				final PluginDownloader downloader;
				if (infoFile == null) {
					downloader = new PluginDownloader(host, installDir);
				} else {
					downloader = new PluginDownloader(host, infoFile, installDir);
				}
				
				downloader.addDownloadListener(this);
				new Thread() {
					public void run() {
						try {
							downloader.downloadInfo();
						} catch (IOException ioe) {
//							PluginManager.logger.error("Error downloading repository info: " + ioe.getMessage(), ioe);
						}
					};
				}.start();
			}
		} finally {
			this.downloaderLock.writeLock().unlock();
		}
//		try {
//			this.downloaderLock.writeLock().lock();
//			
//			if (this.host == null) {
//				throw new NotInitializedException("Error creating PluginDownloader: Host isn't set.");
//			} else if (this.installDir == null) {
//				throw new NotInitializedException("Error creating PluginDownloader: Install directory isn't set.");
//			} else {
//				if (this.infoFile == null) {
//					this.downloader = new PluginDownloader(this.host, this.installDir);
//				} else {
//					this.downloader = new PluginDownloader(this.host, this.infoFile, this.installDir);
//				}
//				
//				new Thread() {
//					public void run() {
//						try {
//							PluginManager.this.downloader.downloadInfo();
//						} catch (IOException ioe) {
//							PluginManager.logger.error("Error downloading repository info: " + ioe.getMessage(), ioe);
//							PluginManager.this.downloader = null;
//						}
//					};
//				}.start();
//			}
//		} finally {
//			this.downloaderLock.writeLock().unlock();
//		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoError(es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent)
	 */
	public void downloadInfoError(PluginDownloadInfoEvent event) {
		this.notifyDownloaderChangeError(event.getSource(), event.getException());
		PluginManager.logger.error("Error downloading repository info: " + event.getException().getMessage(), event.getException());
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoFinished(es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent)
	 */
	public void downloadInfoFinished(PluginDownloadInfoEvent event) {
		try {
			this.downloaderLock.writeLock().lock();
			
			if (event.getSource() != this.downloader) {
				final PluginDownloader old = this.downloader;
				
				if (old != null)
					old.cancelDownloads();
				
				this.downloader = event.getSource();
				this.host = this.downloader.getHost();
				this.infoFile = this.downloader.getInfoFile();
				this.properties.setProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_HOST, this.host);
				this.properties.setProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_INFOFILE, this.infoFile);
				this.storeProperties();
				
				this.notifyDownloaderChanged(old);
				
				if (old == null)
					PluginManager.logger.info("Download manager initialized: " + this.host);
				else
					PluginManager.logger.info("Download manager changed. New URL: " + this.host);
			}
		} finally {
			this.downloaderLock.writeLock().unlock();			
		}			
	}
	
	public void setPluginRepository(String host) {
//		String currentHost = this.host;
		try {
			this.downloaderLock.writeLock().lock();
			
			
//			this.host = host;
			this.initPluginDownloader(host, null, this.installDir);
		
//			this.properties.setProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_HOST, this.host);
//			this.storeProperties();
//		} catch(IOException ioe) {
//			this.downloader = null;
//			this.host = currentHost;
//			try {
//				this.initPluginDownloader();
//			} catch(IOException e) {}
//			throw ioe;
		} finally {
			this.downloaderLock.writeLock().unlock();
		}
	}
	
	public void setPluginRepository(String host, String infoFile) {
		try {
			this.downloaderLock.writeLock().lock();
			
//			this.host = host;
//			this.infoFile = infoFile;
//			this.properties.setProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_HOST, this.host);
//			this.properties.setProperty(PluginManager.PROPERTY_PLUGINREPOSITORY_INFOFILE, this.infoFile);
//			this.storeProperties();
		
			this.initPluginDownloader(host, infoFile, this.installDir);
//		} catch(IOException ioe) {
//			this.downloader = null;
//			throw ioe;
		} finally {
			this.downloaderLock.writeLock().unlock();
		}
	}
	
	public boolean isDownloaderActive() {
		try {
			this.downloaderLock.readLock().lock();
			
			return this.downloader != null && this.downloader.hasInfo();
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public PluginDownloader getPluginDownloader() 
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			if (this.isDownloaderActive()) {
				return this.downloader;
			} else {
				throw new NotInitializedException("PluginDownloader not initialized.");
			}
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public Collection<PluginInfo> getRepositoryInfo() 
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			return this.getPluginDownloader().getPluginsInfo();
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public PluginInfo getDownloadPluginInfo(String uid)
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			return this.getPluginDownloader().getPluginInfo(uid);
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public PluginVersion getUpdateVersion(String pluginUID) 
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			if (this.downloader == null || !this.downloader.hasInfo()) {
				return null;
			} else {
				PluginInfo info = this.downloader.getPluginInfo(pluginUID);
				if (info == null) {
					return null;
				} else {
					try {
						return info.getPluginVersion();
					} catch (PluginEngineException e) {
						PluginManager.logger.error(
							String.format("Invalid plugin version (%s) for the plugin %s on the repository %s",
								info.getVersion(),
								pluginUID,
								this.downloader.getHost()
							), 
							e
						);
						return null;
					}
				}
			}
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public boolean hasUpdateAvailable(Plugin plugin)
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			PluginDownloader downloader = this.getPluginDownloader();
			PluginInfo info = downloader.getPluginInfo(plugin.getUID());
			if (info == null) {
				return false;
			} else {
				try {
					PluginVersion repositoryVersion = info.getPluginVersion();
					return repositoryVersion.compareTo(plugin.getVersion()) > 0;
				} catch (PluginEngineException e) {
					PluginManager.logger.error(
						String.format("Invalid plugin version (%s) for the plugin %s on the repository %s",
							info.getVersion(),
							plugin.getUID(),
							downloader.getHost()
						), 
						e
					);
					return false;
				}
			}
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public boolean hasUpdateAvailable(String pluginUID)
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			
			if (this.downloader == null || !this.downloader.hasInfo()) {
				return false;
			} else {
				PluginInfo info = this.downloader.getPluginInfo(pluginUID);
				if (info == null) {
					return false;
				} else {
					Plugin plugin = this.getInstalledPlugin(pluginUID);
					if (plugin == null) {
						return true;
					} else {
						try {
							PluginVersion repositoryVersion = info.getPluginVersion();
							return repositoryVersion.compareTo(plugin.getVersion()) > 0;
						} catch (PluginEngineException e) {
							PluginManager.logger.error(
								String.format("Invalid plugin version (%s) for the plugin %s on the repository %s",
									info.getVersion(),
									pluginUID,
									this.downloader.getHost()
								), 
								e
							);
							return false;
						}
					}
				}
			}
		} finally {
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void downloadPlugin(PluginInfo plugin) 
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().downloadPlugin(plugin);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void downloadPlugin(String uid) 
	throws NotInitializedException, IllegalArgumentException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().downloadPlugin(uid);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void downloadPlugin(PluginInfo plugin, String updatePlugin) 
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().downloadPlugin(plugin, updatePlugin);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void downloadPlugin(String uid, String updatePlugin) 
	throws NotInitializedException, IllegalArgumentException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().downloadPlugin(uid, updatePlugin);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void cancelDownload(int downloadId)
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().cancelDownload(downloadId);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	
	public void cancelDownloads()
	throws NotInitializedException {
		try {
			this.downloaderLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginDownloader().cancelDownloads();
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.downloaderLock.readLock().unlock();
		}
	}
	/*-********************* END PLUGIN DOWNLOADER *********************-*/
	
	/*-*********************** PLUGIN INSTALLER ************************-*/
	private void initPluginInstaller()
	throws IllegalArgumentException {
		try {
			this.installerLock.writeLock().lock();
			final PluginInstaller old = this.installer;
			
			if (this.installDir != null && Launcher.pluginsDir != null) {
				if (this.ignoreDirs == null) {
					this.installer = new PluginInstaller(Launcher.pluginsDir, this.installDir);
				} else {
					this.installer = new PluginInstaller(Launcher.pluginsDir, this.installDir, this.ignoreDirs);
				}
			}
			
			this.notifyInstallerChanged(old);
		} catch (IllegalArgumentException iae) {
			this.notifyInstallerChangeError(null, iae);
			
			PluginManager.logger.error("Error creating PluginInstaller: " + iae.getMessage(), iae);
			this.installer = null;
			throw iae;
		} finally {
			this.installerLock.writeLock().unlock();
		}
	}
	
	public void setInstallDir(String installDir)
	throws IOException, IllegalArgumentException {
		try {
			this.downloaderLock.writeLock().lock();
			this.installerLock.writeLock().lock();
			
			this.installDir = installDir;
			this.properties.setProperty(PluginManager.PROPERTY_PLUGININSTALLER_DIR, this.installDir);
			this.storeProperties();

			this.initPluginDownloader(this.host, this.infoFile, this.installDir);
			this.initPluginInstaller();				
		} finally {
			this.installerLock.writeLock().unlock();
			this.downloaderLock.writeLock().unlock();
		}
	}
	
	public boolean isPluginInstallerActive() {
		try {
			this.installerLock.readLock().lock();
			
			return this.installer != null;
		} finally {
			this.installerLock.readLock().unlock();
		}
	}
	
	public PluginInstaller getPluginInstaller()
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			
			if (this.isPluginInstallerActive()) {
				return this.installer;
			} else {
				throw new NotInitializedException("PluginInstaller not initialized.");
			}
		} finally {
			this.installerLock.readLock().unlock();
		}
	}
	
	public List<String> getInstallUIDs() 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.readLock().lock();
			
			return this.getPluginInstaller().getValidInstallUIDs();
		} finally {
			this.installDirectoryLock.readLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	
	public boolean hasInstallFiles(String uid) 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.readLock().lock();
			
			return this.getPluginInstaller().isValidInstall(uid);
		} finally {
			this.installDirectoryLock.readLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	
	public InstallInfo getPluginInstallInfo(String uid) 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.readLock().lock();
			
			return this.getPluginInstaller().getInstallInfo(uid);
		} finally {
			this.installDirectoryLock.readLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	
	public boolean deleteInstallFiles(String uid) 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			return this.getPluginInstaller().deleteInstall(uid);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	
	public void clearInvalidInstallFiles() 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginInstaller().clearInvalidInstalls();
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	
	public void installPlugins() 
	throws NotInitializedException {
		try {
			this.installerLock.readLock().lock();
			this.installDirectoryLock.writeLock().lock();
			
			this.getPluginInstaller().installPlugins(this.deleteInvalidInstalls);
		} finally {
			this.installDirectoryLock.writeLock().unlock();
			this.installerLock.readLock().unlock();
		}
	}
	/*-********************* END PLUGIN INSTALLER **********************-*/

	private void notifyInstallerChanged(PluginInstaller old) {
		synchronized(this.listeners) {
			final PluginManagerEvent event = new PluginManagerEvent(old, this.installer);
			
			for (PluginManagerListener listener:this.listeners) {
				listener.installerChanged(event);
			}
		}
	}
	
	private void notifyInstallerChangeError(PluginInstaller other, Exception exception) {
		synchronized(this.listeners) {
			final PluginManagerEvent event = new PluginManagerEvent(other, this.installer, exception);
			
			for (PluginManagerListener listener:this.listeners) {
				listener.installerChangeError(event);
			}
		}
	}
	
	private void notifyDownloaderChanged(PluginDownloader old) {
		synchronized(this.listeners) {
			final PluginManagerEvent event = new PluginManagerEvent(old, this.downloader);
			
			for (PluginManagerListener listener:this.listeners) {
				listener.downloaderChanged(event);
			}
		}
	}
	
	private void notifyDownloaderChangeError(PluginDownloader other, Exception exception) {
		synchronized(this.listeners) {
			final PluginManagerEvent event = new PluginManagerEvent(other, this.downloader, exception);
			
			for (PluginManagerListener listener:this.listeners) {
				listener.downloaderChangeError(event);
			}
		}
	}
	
	public boolean addPluginManagerListener(PluginManagerListener listener) {
		return this.listeners.add(listener);
	}
	
	public boolean removePluginManagerListener(PluginManagerListener listener) {
		return this.listeners.remove(listener);
	}
	
	public List<PluginManagerListener> listeners() {
		return new ArrayList<PluginManagerListener>(this.listeners);
	}
	
//	public void installPlugin() {
//		try {
//			this.loadRepositoryInfo();
//			PluginInfo info = this.downloader.getPluginInfo("aibench.serialization");
//			if (info == null) {
//				System.err.println("Info null");
//			} else {
//				try {
//					File updateDir = new File(this.installDir);
//					this.downloader.downloadPlugin(info, updateDir);
//					PluginInstaller fileManager = new PluginInstaller(Launcher.pluginsDir, this.installDir);
//					if (fileManager.isValidInstall("aibench.serialization")) {
//						if (fileManager.installPlugin("aibench.serialization")) {
//							System.err.println("Plugin installed");
//						} else {
//							System.err.println("Error on install");
//						}
//					} else {
//						System.err.println("Invalid uid");
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//	}
	
//	public List<Dependency> recursiveDependencies(Plugin plugin) {
//		ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
//		ArrayList<Dependency> revised = new ArrayList<Dependency>();
//		
//		this.recursiveDependencies(dependencies, revised, plugin);
//		
//		return dependencies;
//	}
//	
//	private void recursiveDependencies(List<Dependency> dependencies, List<Dependency> revised, Plugin plugin) {
//		if (plugin == null) return;
//		Dependency match;
//		boolean newDependency;
//		for (Dependency dependency:plugin.getDependencies()) {
//			if (revised.contains(dependency)) {
//				return;
//			} else {
//				revised.add(dependency);
//				match = null;
//				newDependency = true;
//				for (Dependency d:dependencies) {
//					if (dependency.getResolveToPluginUID().equals(d.getResolveToPluginUID())) {
//						Dependency strongerDependency = this.getStrongerDependency(d, dependency);
//						if (dependency == strongerDependency) {
//							match = d;
//						} else {
//							newDependency = false;
//						}
//						break;
//					}
//				}
//				dependencies.remove(match);
//				if (newDependency) {
//					dependencies.add(dependency);
//					this.recursiveDependencies(dependencies, revised, this.getPlugin(dependency.getResolveToPluginUID()));
//				}
//			}
//		}
//	}
//	
//	private Dependency getStrongerDependency(Dependency d1, Dependency d2) {
//		if (d1.isOptional() && !d2.isOptional()) {
//			return d2;
//		} else if (!d1.isOptional() && d2.isOptional()) {
//			return d1;
//		} else {
//			PluginVersion pv1, pv2;
//			if (d1.getRequiredVersion() != null) {
//				pv1 = d1.getRequiredVersion();
//			} else if (d1.getMaxVersion() != null) {
//				pv1 = d1.getMaxVersion();
//			} else if (d1.getMinVersion() != null) {
//				pv1 = d1.getMinVersion();
//			} else {
//				pv1 = null;
//			}
//			if (d2.getRequiredVersion() != null) {
//				pv2 = d2.getRequiredVersion();
//			} else if (d2.getMaxVersion() != null) {
//				pv2 = d2.getMaxVersion();
//			} else if (d2.getMinVersion() != null) {
//				pv2 = d2.getMinVersion();
//			} else {
//				pv2 = null;
//			}
//			
//			return (pv1.compareTo(pv2) >= 0)?d1:d2;
//		}
//	}
}
