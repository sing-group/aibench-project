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
 * ShellProgressHandler.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ProgressHandler;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;


public class ShellProgressHandler implements ProgressHandler {
	static Logger logger = Logger.getLogger("AIBench Shell");

	public static Vector<ShellProgressHandler> HANDLERS = new Vector<ShellProgressHandler>();
	private boolean finished = false;
	private boolean error = false;
	
	private String name;

	private List<ClipboardItem> results = null;

	private HashMap<Object, ClipboardItem> clipboardItemsMapping = new HashMap<Object, ClipboardItem>();

	public ShellProgressHandler(String operationName) {
		this.name = operationName;
		
		ClipboardListener listener =new ClipboardListener() {

			public void elementRemoved(ClipboardItem item){
				if (results == null){
					//maybe the operation finished with error.
					return;
				}
				for (int i = results.size()-1; i>=0; i--){
					if (results.get(i)==item.getUserData()){
						
						
						results.remove(i);
					}							
				}
				
				clipboardItemsMapping.remove(item.getUserData());
				
			}
			public void elementAdded(ClipboardItem arg0) {
				clipboardItemsMapping.put(arg0.getUserData(), arg0);
			}
		};
		Core.getInstance().getClipboard().addClipboardListener(listener);
		HANDLERS.add(this);
				
	}

	public void validationError(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	public void operationStart(Object arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	public synchronized void waitFinished(String callerName) {
		if (finished == false)
			try {
				if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug(callerName + " ("+Thread.currentThread()+") waiting for \"" + this.name +" ("+this+") "
						+ "\" to finish");
				wait();
				if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug(callerName +" ("+Thread.currentThread()+") waked up ("+this+")");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public synchronized Object getResultInClipboard(int[] route){
		return getResultInClipboard(route, "<unknown>");
	}
	public synchronized Object getResultInClipboard(int[] route, String callerName) {
		this.waitFinished(callerName);
		
		if (error){
			logger.warn("Getting results from an operation who finished with error, a crash may happen.........");
		}
		
		ClipboardItem current = this.results.get(route[0]);
		
		for (int i = 1; i<route.length; i++){
			List<ClipboardItem> subItems = Core.getInstance().getClipboard().getArraySubItems(current);
			if (subItems == null) subItems = Core.getInstance().getClipboard().getListSubItems(current);
			if (subItems == null) subItems = Core.getInstance().getClipboard().getComplexSubItems(current);
			if (subItems == null) throw new RuntimeException("ClipboardItem not found in this route");
			
			current = subItems.get(route[i]);
		}
		return current;
	}

	public synchronized void operationFinished(List<Object> arg0,
			List<ClipboardItem> arg1) {
		
		finished = true;
		this.results = new ArrayList<ClipboardItem>();
		for (ClipboardItem item : arg1) {
			if (Core.getInstance().getClipboard().getRootItems().indexOf(item) != -1){ 
				this.results.add(item);			
			}else{
				//System.err.println("not a root element: "+item.getUserData());
			}
		}
		notifyAll();
	}

	public void operationError(Throwable arg0) {
		error = true;
	}

	public void elementAdded(ClipboardItem arg0) {
		// TODO Auto-generated method stub

	}

}
