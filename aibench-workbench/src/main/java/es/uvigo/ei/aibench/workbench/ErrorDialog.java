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
package es.uvigo.ei.aibench.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.text.html.HTMLEditorKit;

import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * @author Daniel Glez-Peña
 *
 */
public class ErrorDialog extends JDialog{
	
	
	private static final long serialVersionUID = 1L;

	private final static int WIDTH = 500;
	
	private final String message;
	private final Throwable error;

	
	
	// components
	private JPanel buttonsPanel;
	private JPanel infoPanel;
	
	public ErrorDialog(JFrame parent, Throwable error) {
		this(parent, error, error.getMessage());
	}
	
	public ErrorDialog(JFrame parent, Throwable error, String message) {
		super(parent);
		this.error = error;
		this.message = message;
		this.initialize();
	}

	private void initialize() {
		this.setModal(true);
		BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		layout.setVgap(5);
		this.getContentPane().setLayout(layout);
		this.add(getButtonsPanel(), BorderLayout.SOUTH);
		this.setTitle("Error during operation");
		
		//JTextArea errorArea = new JTextArea("<b>There were an error during process:</b><br>"+this.error.getMessage());
		JTextPane errorArea = new JTextPane();
	
		errorArea.setEditorKit(new HTMLEditorKit());
		
		
		errorArea.setText("<font face='Dialog' size='7px'><b>There were an error during process:</b><br><font color='red'>"+this.message+"</font></font>");
		
		this.add(new JScrollPane(errorArea), BorderLayout.NORTH);
		
		errorArea.setEditable(false);
		errorArea.setMargin(new Insets(5, 5, 5, 5));
		errorArea.setPreferredSize(new Dimension(ErrorDialog.WIDTH, 50));
		
		this.pack();
		Utilities.centerOnOwner(this);
	}
	
	private JPanel getButtonsPanel(){
		if(buttonsPanel == null){
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout());
			buttonsPanel.setPreferredSize(new Dimension(ErrorDialog.WIDTH, 40));
			final JToggleButton moreInfoButton = new JToggleButton("Show Details...");
			
			moreInfoButton.addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent e)  {
					if (moreInfoButton.isSelected()){
						moreInfoButton.setText("Hide Details...");
						showInfo();
					}
					else{
						moreInfoButton.setText("Show Details...");
						hideInfo();
					}
					
				}});
			
			buttonsPanel.add(moreInfoButton);
			
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					ErrorDialog.this.setVisible(false);
					
			}});
			
			buttonsPanel.add(okButton);
	
		}
		return buttonsPanel;
	}
	
	private JPanel getInfoPanel(){
		if (this.infoPanel==null){
			this.infoPanel = new JPanel();
			JTextArea area = new JTextArea();
			
			area.setText(getDetails());
			area.setCaretPosition(0);
			JScrollPane scroll = new JScrollPane(area);
			scroll.setPreferredSize(new Dimension(ErrorDialog.WIDTH, 200));
			BorderLayout layout = new BorderLayout();
			layout.setHgap(5);
			this.infoPanel.setLayout(layout);
			this.infoPanel.add(new JLabel(), BorderLayout.EAST);
			this.infoPanel.add(new JLabel(), BorderLayout.WEST);
			this.infoPanel.add(scroll, BorderLayout.CENTER);
		}
		return this.infoPanel;
	}
	
	private String getDetails(){
		
		String message = "\n   Type of error: "+this.error.getClass().getSimpleName();

		String back = "";
		for (StackTraceElement el : this.error.getStackTrace()){
			back+="\n      "+el.toString();
		}
		message += "\n   Back trace: "+back;
		return message;
	}
	
	private void showInfo(){
		this.add(getInfoPanel(), BorderLayout.CENTER);
		this.setLocation(this.getLocation());
		this.pack();
	}
	
	private void hideInfo(){
		this.remove(getInfoPanel());
		this.setLocation(this.getLocation());
		this.pack();
	}
}
