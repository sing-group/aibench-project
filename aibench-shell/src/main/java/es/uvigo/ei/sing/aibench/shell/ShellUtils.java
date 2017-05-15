/*
 * #%L
 * The AIBench Shell Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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
