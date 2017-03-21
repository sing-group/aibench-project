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

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginDownloadAdapter implements PluginDownloadListener {
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadError(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadError(PluginDownloadEvent event) {}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadFinished(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadFinished(PluginDownloadEvent event) {}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadStarted(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadStarted(PluginDownloadEvent event) {}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadStep(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadStep(PluginDownloadEvent event) {}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoError(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadInfoError(PluginDownloadInfoEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoFinished(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadInfoFinished(PluginDownloadInfoEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoStarted(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadInfoStarted(PluginDownloadInfoEvent event) {
		
	}
}
