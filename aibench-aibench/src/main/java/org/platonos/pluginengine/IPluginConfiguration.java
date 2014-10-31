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
 * IPluginConfiguration.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 30/03/2009
 */
package org.platonos.pluginengine;

/**
 * @author Miguel Reboiro Jato
 *
 */
public interface IPluginConfiguration {

	public abstract boolean isLoadPlugin(Plugin plugin);

	public abstract boolean isLoadPlugin(String pluginUID);

	public abstract boolean isEnabledPlugin(Plugin plugin);

	public abstract boolean isEnabledPlugin(String pluginUID);

	public abstract boolean isActivePlugin(Plugin plugin);
	
	public abstract boolean isActivePlugin(String pluginUID);
	
	public abstract void setLoadPlugin(Plugin plugin, boolean value);

	public abstract void setLoadPlugin(String pluginUID, boolean value);

	public abstract void setEnabledPlugin(Plugin plugin, boolean value);

	public abstract void setEnabledPlugin(String pluginUID, boolean value);
	
	public abstract void setActivePlugin(Plugin plugin, boolean value);
	
	public abstract void setActivePlugin(String pluginUID, boolean value);
}