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

import es.uvigo.ei.aibench.repository.info.PluginInfo;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginDownloadEvent {
	private final PluginDownloader source;
	private final int downloadId; // Identifies the downloading thread.
	private final String uid;
	private final String host;
	private final String file;
	private final int downloaded;
	private final int total;
	private final Throwable error;
	
	/**
	 * 
	 * @param info
	 * @param total
	 * @param downloaded
	 */
	public PluginDownloadEvent(PluginDownloader source, int downloadId, PluginInfo info, int total, int downloaded) {
		this(source, downloadId, info.getUID(), info.getHost(), info.getFile(), total, downloaded, null);		
	}
	
	/**
	 * 
	 * @param info
	 * @param total
	 * @param downloaded
	 * @param exception
	 */
	public PluginDownloadEvent(PluginDownloader source, int downloadId, PluginInfo info, int total, int downloaded, Throwable exception) {
		this(source, downloadId, info.getUID(), info.getHost(), info.getFile(), total, downloaded, exception);
	}
	
	/**
	 * @param host
	 * @param file
	 * @param total
	 * @param downloaded
	 */
	public PluginDownloadEvent(PluginDownloader source, int downloadId, String uid, String host, String file, int total,
			int downloaded) {
		this(source, downloadId, uid, host, file, total, downloaded, null);
	}
	
	/**
	 * 
	 * @param source TODO
	 * @param host
	 * @param file
	 * @param total
	 * @param downloaded
	 * @param error
	 */
	public PluginDownloadEvent(PluginDownloader source, int downloadId, String uid, String host, String file,
			int total, int downloaded, Throwable error) {
		this.source = source;
		this.downloadId = downloadId;
		this.uid = uid;
		this.host = host;
		this.file = file;
		this.downloaded = downloaded;
		this.total = total;
		this.error = error;
	}
	
	/**
	 * @return the source
	 */
	public PluginDownloader getSource() {
		return this.source;
	}
	
	public final boolean isCompleted() {
		return this.total <= this.downloaded;
	}
	
	/**
	 * @return the downloadId
	 */
	public final int getDownloadId() {
		return this.downloadId;
	}
	
	/**
	 * @return the uid
	 */
	public final String getUid() {
		return this.uid;
	}
	
	/**
	 * @return the host
	 */
	public final String getHost() {
		return this.host;
	}
	/**
	 * @return the file
	 */
	public final String getFile() {
		return this.file;
	}
	/**
	 * @return the downloaded
	 */
	public final int getDownloaded() {
		return this.downloaded;
	}
	/**
	 * @return the total
	 */
	public final int getTotal() {
		return this.total;
	}

	/**
	 * @return the error
	 */
	public final Throwable getError() {
		return this.error;
	}
}
