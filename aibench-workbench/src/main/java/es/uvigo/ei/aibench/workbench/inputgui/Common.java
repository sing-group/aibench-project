/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part of the AIBench Project. 

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
 * Common.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on Jan 5, 2010
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

/**
 * @author Miguel Reboiro-Jato
 *
 */
public final class Common {
	final static ImageIcon ICON_FILE_OPEN = new ImageIcon(FileParamProvider.class.getResource("images/fileopen.png"));
	public final static JFileChooser SINGLE_FILE_CHOOSER = new JFileChooser(new File("."));
	public final static JFileChooser MULTIPLE_FILE_CHOOSER = new JFileChooser(new File("."));	
	
	
	private Common(){}
	
	private final static ComponentAdapter COMPONENT_ADAPTER = new ComponentAdapter() {
		public void componentHidden(ComponentEvent e) {
			if (e.getComponent() == Common.SINGLE_FILE_CHOOSER) {
				if (!Common.MULTIPLE_FILE_CHOOSER.isVisible()) {
					Common.MULTIPLE_FILE_CHOOSER.setCurrentDirectory(
						Common.SINGLE_FILE_CHOOSER.getCurrentDirectory()
					);
				}
			} else if (e.getComponent() == Common.MULTIPLE_FILE_CHOOSER) {
				if (!Common.SINGLE_FILE_CHOOSER.isVisible()) {
					Common.SINGLE_FILE_CHOOSER.setCurrentDirectory(
						Common.MULTIPLE_FILE_CHOOSER.getCurrentDirectory()
					);
				}
			}
		}
	};
	
	static {
		Common.MULTIPLE_FILE_CHOOSER.setMultiSelectionEnabled(true);
		Common.MULTIPLE_FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		Common.SINGLE_FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		Common.SINGLE_FILE_CHOOSER.setMultiSelectionEnabled(false);
		
		Common.SINGLE_FILE_CHOOSER.addComponentListener(Common.COMPONENT_ADAPTER);
		Common.MULTIPLE_FILE_CHOOSER.addComponentListener(Common.COMPONENT_ADAPTER);
	}
}
