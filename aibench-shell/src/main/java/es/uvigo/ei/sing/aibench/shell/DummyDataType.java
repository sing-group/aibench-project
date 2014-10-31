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
 * DummyDataType.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Property;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author lipido
 *
 */
@Datatype(structure=Structure.COMPLEX, viewable=true)
public class DummyDataType extends Observable implements Serializable {

	public int counter = 0;
	private static final long serialVersionUID = 1L;
	private List<Object> list = new ArrayList<Object>();
	private String name="unnamed";
	private String allwaysNull = null;
	public DummyDataType(){
		
		strings.add("hola");
		strings.add("adios");
		list.add(new String("un elemento"));
		list.add(new String("segundo"));
	}
	
	
	private Object pay=null;
	
	
	public Object getpay(){ return null; }
	
	@Clipboard(name="a child")
	public Object getpay2(){ return pay; }
	
	@Clipboard(name="allways null")
	public Object getpay3(){ return allwaysNull; }
	
	Integer[] array = new Integer[]{1,2,3,4,5,5};
	
	
	List<String> strings = new ArrayList<String>();
	
	@ListElements
	public List<String> getArr(){
		return strings;
	}
	
	@Property(name = "name")
	public String getName(){
		
		return this.name;
	}
	
	
	
	public void setName(String name){		
		this.name = name;
		this.pay = ""+Math.random();
		strings.set(1, "carallo");
		this.setChanged();
		
		
		
		this.notifyObservers();
		
	}
	public String toString(){
		return "counter value: "+counter;
	}

}
