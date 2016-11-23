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

import java.awt.FlowLayout;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

public class EnumParamProvider extends AbstractParamProvider{

	private ButtonGroup group= new ButtonGroup();
	private JPanel buttonPanel = new JPanel();
	private Object[] enumConstants;
	


	public EnumParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject){
		super(receiver, p,clazz, operationObject);

		this.buttonPanel.setLayout(new FlowLayout());

		this.enumConstants = clazz.getEnumConstants();
		for (Object o : this.enumConstants){
			JRadioButton button =new JRadioButton(o.toString());
			button.addActionListener(EnumParamProvider.this);
			group.add(button);
			buttonPanel.add(button);
			if (p.defaultValue().equals(o.toString())){
				button.setSelected(true);
			}
		}

	}
	
	public JComponent getComponent() {
		return buttonPanel;
	}


	public ParamSpec getParamSpec() {
		int i =0;
		Enumeration<AbstractButton> buttons = group.getElements();
		while(buttons.hasMoreElements()){
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()){
				return new ParamSpec(this.port.name(), clazz, enumConstants[i], ParamSource.ENUM);

			}
			i++;
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public boolean isValidValue() {
		return true;
	}
}
