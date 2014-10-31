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
 * PluginDownloadInfoEvent.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on Apr 17, 2010
 */
package es.uvigo.ei.aibench.repository;

/**
 * @author Miguel Reboiro-Jato
 *
 */
public class PluginDownloadInfoEvent {
	private final PluginDownloader source;
	private final String url;
	private final Exception exception;
	
	/**
	 * @param source
	 * @param url
	 * @param exception
	 */
	public PluginDownloadInfoEvent(PluginDownloader source, String url) {
		this(source, url, null);
	}	
	
	/**
	 * @param source
	 * @param url
	 * @param exception
	 */
	public PluginDownloadInfoEvent(PluginDownloader source, String url,
			Exception exception) {
		super();
		this.source = source;
		this.url = url;
		this.exception = exception;
	}

	/**
	 * @return the source
	 */
	public PluginDownloader getSource() {
		return this.source;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return this.exception;
	}
}
