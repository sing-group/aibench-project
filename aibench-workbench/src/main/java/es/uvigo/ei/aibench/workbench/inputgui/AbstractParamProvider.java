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
 * AbstractParamProvider.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 10/05/2007
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;

import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

public abstract class  AbstractParamProvider extends Observable implements ParamProvider, ActionListener, KeyListener {
	protected Class<?> clazz;
	
	protected Port port;

	protected Object operationObject;

	private ParamsReceiver receiver;

	protected AbstractParamProvider() {}
	
	public AbstractParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject){
		this.receiver = receiver;
		this.clazz = clazz;
		this.operationObject=operationObject;
	/*	this.paramName = name;*/
		this.port = p;

	}
	
	public Port getPort(){
		return this.port;
	}
	
	protected ParamsReceiver getReceiver() {
		return this.receiver;
	}
	
	public void init() {}
	public void finish() {}
	
	public void actionPerformed(ActionEvent e) {
		this.setChanged();
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		this.setChanged();
		this.notifyObservers();
	}
}