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
 * SampleGUI.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import javax.swing.JOptionPane;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.InputGUI;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

public class SampleGUI implements InputGUI{

	private ParamsReceiver rec;
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {
		rec = arg0;
		rec.paramsIntroduced(new ParamSpec[]{new ParamSpec("an array", String.class, JOptionPane.showInputDialog("give me a string: "), ParamSource.STRING_CONSTRUCTOR)});
		
	}

	public void onValidationError(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

}
