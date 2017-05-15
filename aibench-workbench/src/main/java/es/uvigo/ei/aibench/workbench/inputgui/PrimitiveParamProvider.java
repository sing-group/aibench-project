/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

public class PrimitiveParamProvider extends AbstractParamProvider {
	private final JTextField field = new JTextField();
	private final JCheckBox booleanCheck = new JCheckBox();

	public PrimitiveParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p,clazz,operationObject);
		this.field.setPreferredSize(new Dimension(150, this.field.getPreferredSize().height));
		this.field.setText(p.defaultValue().toString());
		
		this.field.addKeyListener(this);
		this.booleanCheck.addActionListener(this);
		
		this.field.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (e.getComponent() instanceof JTextField) {
					((JTextField) e.getComponent()).selectAll();
				}
			};
		});
	}
	
	public JComponent getComponent() {
		if (this.isBoolean()){
			if (this.port.defaultValue().equals("true")){
				this.booleanCheck.setSelected(true);
			}
			return booleanCheck;
		} else {
			return this.field;
		}
	}

	private boolean isBoolean() {
		return this.clazz == Boolean.class || this.clazz == Boolean.TYPE;
	}

	private boolean isChar() {
		return this.clazz == Character.class || this.clazz == Character.TYPE;
	}
	
	private boolean isByte() {
		return this.clazz == Byte.class || this.clazz == Byte.TYPE;
	}
	
	private boolean isShort() {
		return this.clazz == Short.class || this.clazz == Short.TYPE;
	}
	
	private boolean isInteger() {
		return this.clazz == Integer.class || this.clazz == Integer.TYPE;
	}
	
	private boolean isLong() {
		return this.clazz == Long.class || this.clazz == Long.TYPE;
	}
	
	private boolean isFloat() {
		return this.clazz == Float.class || this.clazz == Float.TYPE;
	}
	
	private boolean isDouble() {
		return this.clazz == Double.class || this.clazz == Double.TYPE;
	}

	public ParamSpec getParamSpec() {
		if (!isBoolean()){
			//if (field.getText().equals("")) throw new IllegalArgumentException("The Param can't be null");
			String text = field.getText();
			if (this.isChar() && text.length() == 2 && text.startsWith("\\")){
				//the user enters an special character
				if (text.charAt(1) == 'n') {
					text = "\n";
				} else if (text.charAt(1) == 't') {
					text = "\t";
				} else if (text.charAt(1) == 'b') {
					text = "\b";
				} else if (text.charAt(1) == 'f') {
					text = "\f";
				} else if (text.charAt(1) == 'r') {
					text = "\r";
				} else if (text.charAt(1) == '"') {
					text = "\"";
				} else if (text.charAt(1) == '\\') {
					text = "\\";
				}
			}
			return new ParamSpec(this.port.name(),clazz, field.getText(), ParamSource.STRING_CONSTRUCTOR);
		}else{
			return new ParamSpec(this.port.name(),clazz, booleanCheck.isSelected()?"true":"false", ParamSource.STRING_CONSTRUCTOR);
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public boolean isValidValue() {
		if (!this.port.allowNull() && !this.isBoolean()) {
			if (this.isChar()) {
				return this.field.getText().length() == 1 || (this.field.getText().length() == 2 &&  this.field.getText().startsWith("\\"));
			} else {
				try {
					if (this.isByte()) {
						Byte.parseByte(this.field.getText());
					} else if (this.isShort()) {
						Short.parseShort(this.field.getText());
					} else if (this.isInteger()) {
						Integer.parseInt(this.field.getText());
					} else if (this.isLong()) {
						Long.parseLong(this.field.getText());
					} else if (this.isFloat()) {
						Float.parseFloat(this.field.getText());
					} else if (this.isDouble()) {
						Double.parseDouble(this.field.getText());
					}
				} catch (NumberFormatException nfe) {
					return false;
				}
			}
		}
		
		return true;
	}
}