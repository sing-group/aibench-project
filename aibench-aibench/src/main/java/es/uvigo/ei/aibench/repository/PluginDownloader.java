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
package es.uvigo.ei.aibench.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.repository.info.PluginInfo;

/**
 * Class that manages the plugin download process.
 * 
 * @author Miguel Reboiro Jato
 * 
 */
public class PluginDownloader {
	private final static Logger logger = Logger.getLogger(PluginDownloader.class);
	
	private final static Map<String, Object> DOWNLOAD_LOCKS = new Hashtable<String, Object>();
	
	private static final int BUFFER_SIZE = 8192;

	private static final String DEFAULT_INFO_FILE = "plugins.dat";

	private static int DOWNLOAD_ID_COUNTER = 0;
	
	private final String host;
	private final String infoFile;
	private final File installDir;
	private final Map<String, PluginInfo> infoPlugins;
	private final Map<Integer, DownloadThread> downloadThreads;
	
	private final List<PluginDownloadListener> downloadListeners;
	private final ExecutorService notifierThreads;
	
	public PluginDownloader(String host, String installDir) {
		this(host, PluginDownloader.DEFAULT_INFO_FILE, installDir);
	}
	
	public PluginDownloader(String host, String infoFile, String installDir) {
		this.host = host;
		this.infoFile = infoFile;
		this.installDir = new File(installDir);
		this.infoPlugins = new Hashtable<String, PluginInfo>();
		this.downloadThreads = new Hashtable<Integer, DownloadThread>();
		this.downloadListeners = new Vector<PluginDownloadListener>();
		this.notifierThreads = Executors.newSingleThreadExecutor();
	}
	
	private final static Object getDownloadLock(String directory) {
		if (!PluginDownloader.DOWNLOAD_LOCKS.containsKey(directory)) {
			synchronized (PluginDownloader.DOWNLOAD_LOCKS) {
				if (!PluginDownloader.DOWNLOAD_LOCKS.containsKey(directory)) {
					// Any object may be used as lock, so we use the directory itself as lock.
					PluginDownloader.DOWNLOAD_LOCKS.put(directory, directory); 
				}
			}
		}
		return PluginDownloader.DOWNLOAD_LOCKS.get(directory);
	}
	
	public boolean addDownloadListener(PluginDownloadListener listener) {
		synchronized(this.downloadListeners) {
			if (this.downloadListeners.contains(listener)) {
				return false;
			} else {
				return this.downloadListeners.add(listener);
			}
		}
	}
	
	public boolean removeDownloadListener(PluginDownloadListener listener) {
		synchronized (this.downloadListeners) {
			return this.downloadListeners.remove(listener);
		}
	}
	
	public void notifyInfoDownloadStarted(final PluginDownloadInfoEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadInfoStarted(event);
					}
				}
			});
		}
	}
	
	public void notifyInfoDownloadFinished(final PluginDownloadInfoEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadInfoFinished(event);
					}
				}
			});
		}
	}
	
	public void notifyInfoDownloadError(final PluginDownloadInfoEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadInfoError(event);
					}
				}
			});
		}
	}
	
	public void notifyDownloadStarted(final PluginDownloadEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadStarted(event);
					}
				}
			});
		}
	}
	
	public void notifyDownloadStep(final PluginDownloadEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadStep(event);
					}
				}
			});
		}
	}
	
	public void notifyDownloadError(final PluginDownloadEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadError(event);
					}
				}
			});
		}
	}
	
	public void notifyDownloadFinished(final PluginDownloadEvent event) {
		synchronized (this.downloadListeners) {
			this.notifierThreads.execute(new Runnable() {
				public void run() {
					for (PluginDownloadListener listener:PluginDownloader.this.downloadListeners) {
						listener.downloadFinished(event);
					}
				}
			});
		}
	}
	
	private final static String getPluginFromKey(String key) {
		return key.substring(0, key.lastIndexOf('.'));
	}
	
	private final static String getPropertyFromKey(String key) {
		return key.substring(key.lastIndexOf('.')+1, key.length());
	}
	
	private final String getFileURL(String file) {
		return String.format("%s/%s", this.host, file);
	}
	
	/**
	 * @return the host.
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * @return the info file.
	 */
	public String getInfoFile() {
		return this.infoFile;
	}
	
	/**
	 * @return the URL of the info file.
	 */
	public String getInfoFileURL() {
		return this.getFileURL(this.infoFile);
	}

	/**
	 * @return {@code true} if contains plugin information.
	 */
	public synchronized boolean hasInfo() {
		return !this.infoPlugins.isEmpty();
	}
	
	public synchronized void downloadInfo() 
	throws IOException {
		try {
			this.notifyInfoDownloadStarted(new PluginDownloadInfoEvent(this, this.getInfoFile()));
			
			Map<String, PluginInfo> infos = new HashMap<String, PluginInfo>();
			URL url = new URL(this.getInfoFileURL());
			Properties properties = new Properties();
			
			properties.load(url.openStream());
			String property, value, keyString, keyPlugin;
			for (Object key:properties.keySet()) {
				keyString = key.toString();
				keyPlugin = PluginDownloader.getPluginFromKey(keyString);
				property = PluginDownloader.getPropertyFromKey(keyString);
				
				value = properties.getProperty(keyString);
				if (!infos.containsKey(keyPlugin)) {
					infos.put(keyPlugin, new PluginInfo(keyPlugin, this.getHost()));
				}
				infos.get(keyPlugin).setValue(property, value);
			}
			
			this.infoPlugins.clear();
			for (PluginInfo info:infos.values()) {
				if (info.getUID() == null) {
					PluginDownloader.logger.warn("Incomplete Plugin Info: " + info);
				} else {
					this.infoPlugins.put(info.getUID(), info);
				}
			}
			this.notifyInfoDownloadFinished(new PluginDownloadInfoEvent(this, this.getInfoFile()));
		} catch (IOException ioe) {
			this.notifyInfoDownloadError(new PluginDownloadInfoEvent(this, this.getInfoFile(), ioe));
			throw ioe;
		}
	}
	
	public PluginInfo getPluginInfo(String uid) {
		return this.infoPlugins.get(uid);
	}
	
	public Collection<PluginInfo> getPluginsInfo() {
		return this.infoPlugins.values();
	}
	
	public void downloadPlugin(String uid)
	throws NotInitializedException, IllegalArgumentException {
		if (!this.hasInfo()) {
			throw new NotInitializedException();
		} else {
			PluginInfo info = this.getPluginInfo(uid);
			if (info == null) {
				throw new IllegalArgumentException("There isn't information for the plugin: " + uid);
			} else {
				this.downloadPlugin(info);
			}
		}
	}
	
	public void downloadPlugin(PluginInfo plugin) 
	throws NotInitializedException {
		if (this.hasInfo()) {
			synchronized (this.downloadThreads) {
				DownloadThread thread = new DownloadThread(plugin);
				this.downloadThreads.put(thread.getDownloadId(), thread);
				thread.start();
			}
		} else {
			throw new NotInitializedException();
		}
	}
	
	public void downloadPlugin(String uid, String updatePlugin)
	throws NotInitializedException, IllegalArgumentException {
		if (!this.hasInfo()) {
			throw new NotInitializedException();
		} else {
			PluginInfo info = this.getPluginInfo(uid);
			if (info == null) {
				throw new IllegalArgumentException("There isn't information for the plugin: " + uid);
			} else {
				this.downloadPlugin(info, updatePlugin);
			}
		}
	}
	
	public void downloadPlugin(PluginInfo plugin, String updatePlugin) 
	throws NotInitializedException {
		if (this.hasInfo()) {
			synchronized (this.downloadThreads) {
				DownloadThread thread = new DownloadThread(plugin, updatePlugin);
				this.downloadThreads.put(thread.getDownloadId(), thread);
				thread.start();
			}
		} else {
			throw new NotInitializedException();
		}
	}
	
	public void cancelDownload(int downloadId) {
		synchronized (this.downloadThreads) {
			DownloadThread thread = this.downloadThreads.get(downloadId);
			if (thread != null && thread.isAlive()) {
				thread.stopDownload();
			}			
		}
	}
	
	public void cancelDownloads() {
		synchronized (this.downloadThreads) {
			for (DownloadThread thread:this.downloadThreads.values()) {
				thread.stopDownload();
			}
			this.downloadThreads.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		this.cancelDownloads();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toString = "";
		for (String key:this.infoPlugins.keySet()) {
			toString += this.infoPlugins.get(key);
			toString += '\n';
		}
		return toString;
	}
	
	private class DownloadThread extends Thread {
		private final int downloadId;
		private final PluginInfo plugin;
		private final String updatePlugin;
		private final File directory;
		private final Object lock;
		
		private int total, downloaded;
		private boolean stopDownload;
		
		public DownloadThread(PluginInfo plugin) {
			this(plugin, null);
		}
		public DownloadThread(PluginInfo plugin, String updatePlugin) {
			this.downloadId = PluginDownloader.DOWNLOAD_ID_COUNTER++;
			this.plugin = plugin;
			this.updatePlugin = updatePlugin;
			this.directory = new File(PluginDownloader.this.installDir, plugin.getUID());
			
			this.total = 0;
			this.downloaded = 0;
			this.stopDownload = false;
			
			Object lock;
			try {
				lock = PluginDownloader.getDownloadLock(this.directory.getCanonicalPath());
			} catch (IOException ioe) {
				PluginDownloader.logger.warn(
					"Canonical Path couldn't be used. Using Absolute Path: " + this.directory.getAbsolutePath()
				);
				lock = PluginDownloader.getDownloadLock(this.directory.getAbsolutePath());
			}
			this.lock = lock;
		}
		
		public final int getDownloadId() {
			return this.downloadId;
		}
		
		public void stopDownload() {
			this.stopDownload = true;
		}
		
		private void notifyDownloadStarted() {
			PluginDownloader.this.notifyDownloadStarted(new PluginDownloadEvent(PluginDownloader.this, this.downloadId, this.plugin, this.total, this.downloaded));
		}
		private void notifyDownloadStep() {
			PluginDownloader.this.notifyDownloadStep(new PluginDownloadEvent(PluginDownloader.this, this.downloadId, this.plugin, this.total, this.downloaded));
		}
		private void notifyDownloadFinished() {
			PluginDownloader.this.notifyDownloadFinished(new PluginDownloadEvent(PluginDownloader.this, this.downloadId, this.plugin, this.total, this.downloaded));
		}
		private void notifyDownloadError(IOException error) {
			PluginDownloader.this.notifyDownloadError(new PluginDownloadEvent(PluginDownloader.this, this.downloadId, this.plugin, this.total, this.downloaded, error));
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			InputStream is = null;
			FileOutputStream fos = null;
			this.downloaded = 0;
			this.total = 0;
			synchronized (this.lock) {
				try {
					if (!this.stopDownload && this.directory.mkdir()) {
						File installFile = new File(this.directory, this.plugin.getFile());
						
						URL url = new URL(PluginDownloader.this.getFileURL(this.plugin.getFile()));
						URLConnection connection = url.openConnection();
						is = connection.getInputStream();
						fos = new FileOutputStream(installFile);
						
						this.total = connection.getContentLength() + 1; // The last one is when the install.info file is stored.
						
						this.notifyDownloadStarted();
						byte[] data = new byte[PluginDownloader.BUFFER_SIZE];
						int len;
						while (!this.stopDownload && (len = is.read(data)) != -1) {
							fos.write(data, 0, len);
							this.downloaded += len;
							this.notifyDownloadStep();
						}
						
						if (!this.stopDownload) {
							if (this.updatePlugin == null) {
								this.plugin.getInstallInfo().store(this.directory);
							} else {
								this.plugin.getInstallInfo(this.updatePlugin).store(this.directory);
							}
							this.downloaded++;
						}
						
						this.notifyDownloadFinished();
					}
				} catch (IOException ioe) {
					this.stopDownload = true; // To delete de downloaded files.
					this.notifyDownloadError(ioe);
				} finally {
					PluginDownloader.this.downloadThreads.remove(this.downloadId);
					try {
						if (fos != null) {
							fos.flush();
							fos.close();
						}
					} catch (IOException e) {}
					try {
						if (is != null) is.close();
					} catch (IOException e) {}
					if (this.stopDownload) 
						PluginInstaller.deleteFile(this.directory);
				}
			}
		}
	}
}
