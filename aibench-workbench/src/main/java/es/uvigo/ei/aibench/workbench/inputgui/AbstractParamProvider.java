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
		this.port = p;
	}

	protected ParamsReceiver getReceiver() {
		return this.receiver;
	}

	@Override
	public Port getPort(){
		return this.port;
	}

	@Override
	public void init() {}
	
	@Override
	public void finish() {}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.setChanged();
		this.notifyObservers();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		this.setChanged();
		this.notifyObservers();
	}
}