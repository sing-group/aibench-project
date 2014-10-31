/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

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
 * IViewFactory.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
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
