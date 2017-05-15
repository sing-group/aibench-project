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

import es.uvigo.ei.aibench.repository.PluginDownloader;
import es.uvigo.ei.aibench.repository.PluginInstaller;

/**
 * @author Daniel Glez-Peña
 *
 */
public class PluginManagerEvent {
	public static enum Type {
		DOWNLOADER_CHANGED, INSTALLER_CHANGED, ERROR_CHANGING_DOWNLOADER, ERROR_CHANGING_INSTALLER;
		
		public boolean isDownload() {
			return this == Type.DOWNLOADER_CHANGED || this == Type.ERROR_CHANGING_DOWNLOADER;
		}
		
		public boolean isInstaller() {
			return this == Type.INSTALLER_CHANGED || this == Type.ERROR_CHANGING_INSTALLER;
		}
		
		public boolean isError() {
			return this == Type.ERROR_CHANGING_DOWNLOADER || this == Type.ERROR_CHANGING_INSTALLER;
		}
	};
	
	private final Type type;
	private final PluginDownloader otherDownloader, currentDownloader;
	private final PluginInstaller otherInstaller, currentInstaller;
	private final Throwable exception;
	
	public PluginManagerEvent(
		PluginDownloader otherDownloader,
		PluginDownloader currentDownloader) {
		this.type = Type.DOWNLOADER_CHANGED;
		this.otherDownloader = otherDownloader;
		this.currentDownloader = currentDownloader;
		this.otherInstaller = null;
		this.currentInstaller = null;
		this.exception = null;
	}

	public PluginManagerEvent(PluginInstaller otherInstaller,
			PluginInstaller currentInstaller) {
		this.type = Type.INSTALLER_CHANGED;
		this.otherInstaller = otherInstaller;
		this.currentInstaller = currentInstaller;
		this.otherDownloader = null;
		this.currentDownloader = null;
		this.exception = null;
	}
	
	public PluginManagerEvent(
			PluginDownloader otherDownloader,
			PluginDownloader currentDownloader,
			Throwable exception) {
		this.type = Type.ERROR_CHANGING_DOWNLOADER;
		this.otherDownloader = otherDownloader;
		this.currentDownloader = currentDownloader;
		this.otherInstaller = null;
		this.currentInstaller = null;
		this.exception = exception;
	}
	
	public PluginManagerEvent(PluginInstaller otherInstaller,
			PluginInstaller currentInstaller,
			Throwable exception) {
		this.type = Type.ERROR_CHANGING_INSTALLER;
		this.otherInstaller = otherInstaller;
		this.currentInstaller = currentInstaller;
		this.otherDownloader = null;
		this.currentDownloader = null;
		this.exception = exception;
	}

	public Object getOther() {
		return (this.type.isDownload())?this.otherDownloader:this.otherInstaller;
	}
	
	public Object getCurrent() {
		return (this.type.isDownload())?this.currentDownloader:this.currentInstaller;
	}
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * @return the oldDownloader
	 */
	public PluginDownloader getOtherDownloader() {
		return this.otherDownloader;
	}

	/**
	 * @return the currentDownloader
	 */
	public PluginDownloader getCurrentDownloader() {
		return this.currentDownloader;
	}

	/**
	 * @return the oldInstaller
	 */
	public PluginInstaller getOtherInstaller() {
		return this.otherInstaller;
	}

	/**
	 * @return the currentInstaller
	 */
	public PluginInstaller getCurrentInstaller() {
		return this.currentInstaller;
	}
	
	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return this.exception;
	}
}
