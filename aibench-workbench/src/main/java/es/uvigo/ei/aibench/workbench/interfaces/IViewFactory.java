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

/**
 * @author   Ruben Dominguez Carbajales 05-oct-2005
 */
public interface IViewFactory {
	public JComponent getComponent(Object data);
	
	public Class<?> getComponentClass();

	public ImageIcon getViewIcon();

	public void setViewIcon(ImageIcon icon);

	public String getViewName();

	public void setViewName(String name);

	public Class<?> getDataType();
	public void setDataType(Class<?> className);

	public int getViewPreferredPosition();

	public void setViewPreferredPosition(int i);
	
	public String getPluginName();
	public void setPluginName(String name);
	public String getPluginUID();
	public void setPluginUID(String uid);
	public String getHelp();
	public void setHelp(String help);


}
