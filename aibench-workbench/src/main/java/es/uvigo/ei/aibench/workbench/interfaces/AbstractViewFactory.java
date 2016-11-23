/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench.interfaces;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public abstract class AbstractViewFactory implements IViewFactory {

	protected String name = "unamed";

	
	protected ImageIcon icon = null;

	protected int position = -1;

	protected Class<?> dataType;
	
	protected String pluginName;
	
	protected String pluginUID;
	
	protected String help;

	/**
	 * @return Returns the dataType.
	 */
	public Class<?> getDataType() {
		return this.dataType;
	}

	/**
	 * @param dataType The dataType to set.
	 */
	public void setDataType(Class<?> dataType) {
		this.dataType = dataType;
	}

	public final ImageIcon getViewIcon() {
		return this.icon;
	}

	public final void setViewIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public final String getViewName() {
		return this.name;
	}

	public final void setViewName(String name) {
		this.name = name;
	}

	public int getViewPreferredPosition(){
		return this.position;
	}

	public void setViewPreferredPosition(int position){
		this.position = position;
	}

	public abstract JComponent getComponent(Object data);

	public String getPluginName() {
		return this.pluginName;
	}

	public String getPluginUID() {
		return this.pluginUID;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public void setPluginUID(String pluginUID) {
		this.pluginUID = pluginUID;
	}

	public final String getHelp() {
		return this.help;
	}

	public final void setHelp(String help) {
		this.help = help;
	}
}
