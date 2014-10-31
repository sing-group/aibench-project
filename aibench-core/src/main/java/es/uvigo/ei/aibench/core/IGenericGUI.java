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
 * IGenericGUI.java
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 25/09/2005
 */
package es.uvigo.ei.aibench.core;

/**
 * This class defines the interface between the AIBench Core plugin and its GUI
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 */
public interface IGenericGUI {

	/**
	 * Called to give the item the chance to init itself. This method is called before any other
	 */
	public void init();
	
	/**
	 * Called when some operation is executed and the status of the GUI could change
	 */
	public void update();

	/**
	 * Called to show a message to the user
	 * @param info the message
	 */
	public void info(String info);
	
	/**
	 * Called to show a warning message
	 * @param warning the message
	 */
	public void warn(String warning);
	
	/**
	 * Called to show an error message
	 * @param error the message
	 */
	public void error(Throwable error);
	
	/**
	 * Called to show an error message
	 * @param error the message
	 */
	public void error(String error);

	/**
	 * Called to establish some status message
	 * @param text the status text
	 */
	public void setStatusText(String text);
	
}
