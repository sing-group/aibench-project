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
 * ShellUtils.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import bsh.EvalError;
import bsh.Interpreter;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.Clipboard;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;


/**
 * @author Daniel Gonzalez Peña
 *
 */
public class ShellUtils {
	public static void joinAllHandlers(){
		for (ShellProgressHandler handler : ShellProgressHandler.HANDLERS){
			handler.waitFinished("");
		}
	}
	
	public static void clearClipboard(){
		Clipboard clipper = Core.getInstance().getClipboard();
		List<ClipboardItem> items = clipper.getAllItems();
		
		for (ClipboardItem item : items){
			clipper.removeClipboardItem(item);
		}
	}
	
	public static void loadScript(String path) throws FileNotFoundException, IOException, EvalError {
		Interpreter interpreter = AIBenchInterpreter.getInstance().getInterpreter();
		
		interpreter.source(path);
	}
}
