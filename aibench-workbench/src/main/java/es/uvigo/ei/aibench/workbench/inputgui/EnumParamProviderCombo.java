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
 * EnumParamProviderCombo.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 10/05/2007
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

public class EnumParamProviderCombo extends AbstractParamProvider {
	private JComboBox<Object> combo = new JComboBox<Object>();

	public EnumParamProviderCombo(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject){
		super(receiver, p,clazz, operationObject);

		for (Object o : clazz.getEnumConstants()){			
			combo.addItem(o);		
			if (p.defaultValue().equals(o.toString())){
				combo.setSelectedItem(o);
			}
			combo.addActionListener(this);
			combo.addKeyListener(this);
		}
	}
	
	public JComponent getComponent() {
		return combo;
	}

	public ParamSpec getParamSpec() {
		return new ParamSpec(this.port.name(), clazz, combo.getSelectedItem(), ParamSource.ENUM);
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public boolean isValidValue() {
		if (!this.port.allowNull()) {
			return this.combo.getSelectedItem() != null;
		}
		return true;
	}
}