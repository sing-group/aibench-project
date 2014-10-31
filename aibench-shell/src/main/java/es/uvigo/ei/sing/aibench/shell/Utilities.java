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
 * Utilities.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 27/03/2007
 */
package es.uvigo.ei.sing.aibench.shell;

import java.util.List;
import java.util.Vector;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author Daniel Gonzalez Peña
 *
 */
public class Utilities {
	private static boolean isComplex(ClipboardItem item){
		Object data = item.getUserData();
		
		if (data!=null){
			if (data.getClass().isArray()) return true;
			Datatype annot = data.getClass().getAnnotation(Datatype.class);
			if (annot== null) return false;
			if (annot.structure()!=Structure.SIMPLE) return true;
			return false;
		}
		return false;
	}
	public static Vector<Integer> calculateComplexRoute(ClipboardItem root, ClipboardItem searched){
		Vector<Integer> route = new Vector<Integer>();
		if (!isComplex(root) && root!=searched) return null; //not found
		else if (root == searched) return route;
		else{
			//recursively find
			List<ClipboardItem> subItems = Core.getInstance().getClipboard().getArraySubItems(root);
			if (subItems == null) subItems = Core.getInstance().getClipboard().getComplexSubItems(root);
			if (subItems == null) subItems = Core.getInstance().getClipboard().getListSubItems(root);
			
			if (subItems == null) return null;
			else{
		
				int i = 0;
				for (ClipboardItem item : subItems){
					Vector<Integer> subRoute = calculateComplexRoute(item, searched);
					if (subRoute != null){
						// found!!
						route.add(i);
						route.addAll(subRoute);
						return route;
					}
					i++;
				}
			
			}
			
			//not found
			return null;
		}
	
	}

}
