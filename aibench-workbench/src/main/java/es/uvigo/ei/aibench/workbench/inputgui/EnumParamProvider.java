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

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.utilities.PortExtras;

public class EnumParamProvider extends AbstractParamProvider {
	private final static Logger LOGGER = Logger.getLogger(EnumParamProvider.class);

	public static final String PROPERTY_MODE 			= "mode";
	public static final String PROPERTY_MODE_COMBO 		= "combo";
	public static final String PROPERTY_MODE_BUTTONS 	= "radiobuttons";
	public static final String PROPERTY_BUTTONS_ROWS 	= "numrows";
	public static final String PROPERTY_BUTTONS_COLUMNS = "numcolumns";

	private static final String[] KNOWN_PROPERTIES = {
		PROPERTY_MODE, PROPERTY_BUTTONS_ROWS, PROPERTY_BUTTONS_COLUMNS };

	public static final int DEFAULT_NUM_ROWS 	= 0;
	public static final int DEFAULT_NUM_COLUMNS = 1;

	private String mode = PROPERTY_MODE_COMBO;
	private JComboBox<Object> combo = new JComboBox<Object>();
	private ButtonGroup group = new ButtonGroup();
	private JPanel buttonsPanel;
	private int buttonsPanelRows = DEFAULT_NUM_ROWS;
	private int buttonsPanelColumns = DEFAULT_NUM_COLUMNS;

	public EnumParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p, clazz, operationObject);

		this.initComponent();
	}

	private void initComponent() {
		parseExtras();
		if (isComboMode()) {
			createCombobox();
		} else {
			createRadioButtons();
		}
	}

	private void createCombobox() {
		for (Object o : clazz.getEnumConstants()) {
			combo.addItem(o);
			if (port.defaultValue().equals(o.toString())) {
				combo.setSelectedItem(o);
			}
			combo.addActionListener(this);
			combo.addKeyListener(this);
		}
	}

	private void createRadioButtons() {
		this.buttonsPanel = new JPanel(
			new GridLayout(this.buttonsPanelRows, this.buttonsPanelColumns));
		for (Object o : clazz.getEnumConstants()) {
			JRadioButton button = new JRadioButton(o.toString());
			button.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					actionPerformed(null);
				}
			});
			group.add(button);
			this.buttonsPanel.add(button);
			if (port.defaultValue().equals(o.toString())) {
				button.setSelected(true);
			}
		}
	}

	private boolean isComboMode() {
		return mode.equalsIgnoreCase(PROPERTY_MODE_COMBO);
	}

	private void parseExtras() {
		PortExtras extras = PortExtras.parse(port.extras());
		if (extras.containsProperty(PROPERTY_MODE)) {
			String mode = extras.getPropertyValue(PROPERTY_MODE);
			if (mode.equalsIgnoreCase(PROPERTY_MODE_BUTTONS) || mode.equalsIgnoreCase(PROPERTY_MODE_COMBO)) {
				this.mode = mode;
			} else {
				LOGGER.warn("Unknown mode property: " + mode + ". Using default mode: " + PROPERTY_MODE_COMBO);
			}
		}
		if(!isComboMode()) {
			if (extras.containsProperty(PROPERTY_BUTTONS_ROWS)) {
				String rows = extras.getPropertyValue(PROPERTY_BUTTONS_ROWS);
				try {
					this.buttonsPanelRows = Integer.valueOf(rows);
				} catch (Exception e) {
					LOGGER.warn("Invalid " + PROPERTY_BUTTONS_ROWS + " property: " + rows + ". It must be an integer.");
				}
			}

			if (extras.containsProperty(PROPERTY_BUTTONS_COLUMNS)) {
				String columns = extras.getPropertyValue(PROPERTY_BUTTONS_COLUMNS);
				try {
					this.buttonsPanelColumns = Integer.valueOf(columns);
				} catch (Exception e) {
					LOGGER.warn("Invalid " + PROPERTY_BUTTONS_COLUMNS + " property: " + columns + ". It must be an integer.");
				}
			}

			if(this.buttonsPanelRows == 0 && this.buttonsPanelColumns == 0) {
				this.buttonsPanelRows = DEFAULT_NUM_ROWS;
				this.buttonsPanelColumns = DEFAULT_NUM_COLUMNS;
				LOGGER.warn("Warning: " + PROPERTY_BUTTONS_ROWS + " and " + PROPERTY_BUTTONS_COLUMNS + " cannot both be zero. Using default values instead.");
			}
		}
		PortExtras.warnUnknownExtraProperties(extras, LOGGER, true, KNOWN_PROPERTIES);
	}

	@Override
	public JComponent getComponent() {
		return isComboMode() ? combo : buttonsPanel;
	}

	@Override
	public ParamSpec getParamSpec() {
		return new ParamSpec(this.port.name(), clazz, getSelectedItem(), ParamSource.ENUM);
	}

	private Object getSelectedItem() {
		return isComboMode() ? getSelectedComboItem() : getSelectedRadioItem();
	}

	private Object getSelectedComboItem() {
		return combo.getSelectedItem();
	}

	private Object getSelectedRadioItem() {
		int i = 0;
		Enumeration<AbstractButton> buttons = group.getElements();
		while (buttons.hasMoreElements()) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected()) {
				return clazz.getEnumConstants()[i];

			}
			i++;
		}
		return null;
	}

	@Override
	public boolean isValidValue() {
		if (!this.port.allowNull()) {
			return getSelectedItem() != null;
		} else {
			return true;
		}
	}
}
