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
package es.uvigo.ei.aibench.workbench.utilities;

import javax.help.HelpBroker;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * A button that opens the associated Java help.
 * 
 * @author Hugo López-Fernández
 *
 */
public class HelpButton extends JButton {
	private static final long serialVersionUID = 1L;

	public HelpButton() {
		this(new ImageIcon(HelpButton.class.getResource("images/helpbutton.png")));
	}
	
	public HelpButton(ImageIcon icon) {
		super(icon);
		
		this.initComponent();
	}

	private void initComponent() {
		final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
		if (helpBroker != null) {
			helpBroker.enableHelpOnButton(
				this, "top", helpBroker.getHelpSet()
			);

			helpBroker.enableHelpKey(
				Workbench.getInstance().getMainFrame(), 
				"top", 
				helpBroker.getHelpSet()
			);
		}
	}
}
