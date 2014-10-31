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
 * PluginDownloadListener.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 10/04/2009
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
