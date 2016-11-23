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
public interface PluginDownloadListener {
	public abstract void downloadInfoStarted(PluginDownloadInfoEvent event);
	public abstract void downloadInfoFinished(PluginDownloadInfoEvent event);
	public abstract void downloadInfoError(PluginDownloadInfoEvent event);
	public abstract void downloadStarted(PluginDownloadEvent event);
	public abstract void downloadStep(PluginDownloadEvent event);
	public abstract void downloadFinished(PluginDownloadEvent event);
	public abstract void downloadError(PluginDownloadEvent event);
}
