/*
 * #%L
 * The AIBench Shell Plugin
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
package es.uvigo.ei.sing.aibench.shell;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.history.HistoryElement;
import es.uvigo.ei.aibench.core.history.HistoryListener;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

public class OperationsTextArea extends JTextArea implements HistoryListener, ClipboardListener{
	private static final long serialVersionUID = 1L;
	
	static Logger logger = Logger.getLogger("AIBench Shell");
	public OperationsTextArea(){
		this.setEditable(true);
		this.addLine("// Operation Log started at "+new Date());
		Core.getInstance().getClipboard().addClipboardListener(this);

	}

	private int paramCounter=0;
	private int operationCounter=0;
	private void addLine(String line){
		this.setEditable(true);
		this.setText(this.getText()+"\n"+line);
		this.setEditable(false);
	}

	private HashMap<HistoryElement, String> handlerVariables = new HashMap<HistoryElement, String>();
	int handlerCounter=0;
	

	private Map<ClipboardItem, HashSet<String>> whoLocks = new HashMap<ClipboardItem, HashSet<String>>();
	private Map<String, List<ClipboardItem>> handlerNames = Collections.synchronizedMap(new HashMap<String, List<ClipboardItem>>());
	public synchronized void historyElementAdded(final HistoryElement historyElement) {

		if (!SwingUtilities.isEventDispatchThread()){
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					historyElementAdded(historyElement);
				}
			});
		}else{

			if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("History element added");

			this.addLine("//***** "+historyElement.getOperation().getName()+" ********");
			String operationName = "operation_"+(operationCounter++);
			String findOperation = operationName+" = findOperation(\""+historyElement.getOperation().getID()+"\");";
			this.addLine(findOperation);
			
			String handlerName = "handler_"+(handlerCounter++);
			
			String handlerCreation = handlerName+" = new ShellProgressHandler("+operationName+".getName());";
			this.addLine(handlerCreation);
			
			this.addLine("class "+operationName+"_thread extends Thread{public void run(){");
			
			


			//			resolve dependencies with previous operations in order to create locks waiting for their results
			
			HashSet<String> noRepeat = new HashSet<String>();
			int counter = 0;
			for(ParamSpec param : historyElement.getParams()){
				if (param.getSource()==ParamSource.CLIPBOARD){
					
					
					//ClipboardItem item = (ClipboardItem) param.getValue();
					/*HistoryElement parent = Core.getInstance().getHistory().getSourceOfClipboardItem(item);
					String handler = handlerVariables.get(parent);
					if (handler!=null && noRepeat.indexOf(handler)==-1){
						noRepeat.addElement(handler);
						this.addLine(handler+".waitFinished("+operationName+".getName());");
					}*/
					// if some past operation used this item to write, we should stop and wait it finishes
					for (HistoryElement history : handlerVariables.keySet()){
						for (ParamSpec param2:history.getParams()){
							if (param2.getSource() == ParamSource.CLIPBOARD){
								String name = param2.getName();
								List<Port> ports = (List<Port>)history.getOperation().getPorts();
								for (Port port : ports){									
									if (port.name().equals(name) && param2.getValue()!=null && param.getValue()!=null && param2.getValue().equals(param.getValue()) && port.lock() && !noRepeat.contains(handlerVariables.get(history))){
										noRepeat.add(handlerVariables.get(history));
										this.addLine(handlerVariables.get(history)+".waitFinished("+operationName+".getName()); // who is locking the variable I need...");
									}
								}
								
							}
						}
					}
					// if some past operation used this item to read, but I want to write, we should stop and wait it finishes
					if (((Port)historyElement.getOperation().getPorts().get(counter)).lock()){
						
						//lock this item and all its parents (complex items)
						HashSet<String> otherHandlers= addLocks((ClipboardItem)param.getValue(), handlerName);
						
						//wait for other handlers who locked this item or a parent
						for (String otherHandler:otherHandlers){
							
							if (!noRepeat.contains(otherHandler)){
								this.addLine(otherHandler+".waitFinished("+operationName+".getName()); // who is locking an object common parent");
								noRepeat.add(otherHandler);
							}
						}
						
						for (HistoryElement history : handlerVariables.keySet()){
							for (ParamSpec param2:history.getParams()){
								if (param2.getSource() == ParamSource.CLIPBOARD){
									String name = param2.getName();
									List<Port> ports = (List<Port>)history.getOperation().getPorts();
									for (Port port : ports){
										if (port.name().equals(name) && param2.getValue()!=null && param.getValue()!=null && param2.getValue().equals(param.getValue()) && !noRepeat.contains(handlerVariables.get(history))){
											noRepeat.add(handlerVariables.get(history));
											this.addLine(handlerVariables.get(history)+".waitFinished("+operationName+".getName()); // who is reading or writing the variable I want write...");
										}
									}
									
								}
							}
						}
					}
					
					// TODO: BUGFIX, check if this clibpoard item has any parent which is in any previous operation as an locked input. in that
					// case we should wait for this handler.
				}
				counter ++;
			}

			this.handlerVariables.put(historyElement,handlerName);

			String name = createParams(operationName, 0,0,historyElement.getParams(),historyElement.getOperation().getName());



			
			
			
			
			String coreInvocation = "Core.getInstance().executeOperation("+operationName+", "+handlerName+", "+name+");";
//			String coreInvocation = "ShellParallelizer.run("+dependencyString+", operation, "+handlerName+", "+name+");";

			List<ClipboardItem> results = new Vector<ClipboardItem>();
			handlerNames.put(handlerName, results);
			for (ClipboardItem result: historyElement.getClipboardItems()){
				if (Core.getInstance().getClipboard().getRootItems().indexOf(result)!=-1) results.add(result);
			}
			this.addLine(coreInvocation);

			this.addLine("}};");
			this.addLine("new "+operationName+"_thread().start();");
			this.addLine("//***********************");
			this.addLine("");

		}

	}
	/**
	 * @param value
	 */
	private HashSet<String> addLocks(ClipboardItem item, String handler) {
		HashSet<String> otherHandlers = new HashSet<String>();
		while (item!=null){
			HashSet<String> handlers = this.whoLocks.get(item);
			if (handlers ==null){
				handlers = new HashSet<String>();
				this.whoLocks.put(item, handlers);
			}else{
				otherHandlers.addAll(handlers);
			}
			handlers.add(handler);
					
			item = Core.getInstance().getClipboard().getParent(item);
			
		}
		return otherHandlers;
		
	}
	
	
	private String createGetHandlerResult(ClipboardItem clipboardObject, String operationName){
		for(Object key: this.handlerNames.keySet()){
			int i=0;
			for(ClipboardItem o: this.handlerNames.get(key)){

				Vector<Integer> route = Utilities.calculateComplexRoute(o, clipboardObject);
				if (route != null){
					
					
					route.insertElementAt(i, 0);
					String retString = key+".getResultInClipboard(new int[]{";
					int counter = 0;
					for (Integer j : route){
						retString+=""+j;						
						counter++;
						if (counter < route.size())
							retString+=", ";
					}
					retString+="}, \""+operationName+"\")";
					
					
					return retString;
				}
				
				
					
				
				/*if (clipboardObject == o){
					return key+".getResultInClipboard("+i+")";
				}*/
				i++;
			}
		}
		return "null";
	}
	private String createParams(String opName, int global, int deep,ParamSpec[] params, String operationName){		
		int paramCount =0;
		Vector<String> paramNames = new Vector<String>();
		for (ParamSpec spec: params){
			if (spec.getSource()==ParamSource.MIXED){
				String type = opName+".getIncomingArgumentTypes().get("+(deep==0?(paramCount):global)+")";
				String arrayName = createParams(opName, paramCount, deep+1, (ParamSpec[])spec.getValue(), operationName);
				String paramName ="param_"+(paramCounter++);
				paramNames.add(paramName);
				this.addLine(paramName+" = new ParamSpec(\""+spec.getName()+"\", "+type+","+arrayName+");");
			}else{
				String paramName ="param_"+(paramCounter++);
				paramNames.add(paramName);
				String type = opName+".getIncomingArgumentTypes().get("+(deep==0?(paramCount):global)+")";
				for(int j =0; j<deep; j++){
					type+=".getComponentType()";
				}
				this.addLine("type = "+type+";");
				this.addLine(paramName+" = " +getStringToCreate(spec,"type", operationName));
				
				if (spec.getTransformerSignature()!=null){
					this.addLine(paramName+".setTransformerSignature(\""+spec.getTransformerSignature()+"\");");
				}
				
				
				
			}
			paramCount++;
		}

		String paramsCreation = "ParamSpec[] params = new ParamSpec[]{";
		int i=0; for(String name:paramNames){
			paramsCreation+=name;
			if ((i++)<paramNames.size()-1) paramsCreation+=", ";
		}
		paramsCreation+="};";

		this.addLine(paramsCreation);
		return "params";
	}

	private String getStringToCreate(ParamSpec spec, String type, String operationName){
		if(spec.getSource()==ParamSource.MIXED) throw new IllegalArgumentException();

		if (spec.getSource()==ParamSource.CLIPBOARD){

			//return "new ParamSpec(\""+spec.getName()+"\", "+type+",Core.getInstance().getClipboard().findByID("+((ClipboardItem)spec.getValue()).getID()+"), ParamSource."+spec.getSource()+");";
			return "new ParamSpec(\""+spec.getName()+"\", "+type+","+createGetHandlerResult( (ClipboardItem) spec.getValue(), operationName)+", ParamSource."+spec.getSource()+");";
		}else if (spec.getSource()==ParamSource.ENUM){
			return "new ParamSpec(\""+spec.getName()+"\", "+type+",findEnum("+type+",\""+((Enum<?>)spec.getValue()).name()+"\"), ParamSource.ENUM);";
		}

		else{
			return "new ParamSpec(\""+spec.getName()+"\", "+type+",\""+spec.getValue().toString().replaceAll("\\\\", "_____barra_____").replaceAll("_____barra_____", "\\\\\\\\")+"\", ParamSource."+spec.getSource()+");";
		}
	}
	public synchronized void elementAdded(ClipboardItem arg0) {
		// TODO Auto-generated method stub
		
	}
	public synchronized void elementRemoved(ClipboardItem arg0) {
		for (String key:this.handlerNames.keySet()){
			List<ClipboardItem> result = this.handlerNames.get(key);
			for (int i =result.size()-1; i>=0; i--){
				if (result.get(i)==arg0.getUserData()){
					if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("shell removing data due to clipboard deletion");
					result.remove(i);
				}
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.history.HistoryListener#historyElementRemoved(es.uvigo.ei.aibench.core.history.HistoryElement)
	 */
	public void historyElementRemoved(HistoryElement history) {
		// TODO Auto-generated method stub
		
	}
	
	

}

