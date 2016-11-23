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