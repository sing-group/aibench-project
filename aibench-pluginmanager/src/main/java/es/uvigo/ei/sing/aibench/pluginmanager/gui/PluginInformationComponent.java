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
 * PluginInformationComponent.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 07/04/2009
 */
package es.uvigo.ei.sing.aibench.pluginmanager.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import es.uvigo.ei.aibench.repository.NotInitializedException;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManager;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerAdapter;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerEvent;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginInformationComponent extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton btnChangeRepository;
	private final JButton btnViewRepository;
	private final JLabel lblRepository;
	
	public PluginInformationComponent() {
		super(new BorderLayout());
		
		this.lblRepository = new JLabel("Repository: " + PluginManager.getInstance().getHost());
		this.btnChangeRepository = new JButton("Change Repository");
		this.btnViewRepository = new JButton("View Repository");
		
		final JPanel panelRepository = new JPanel();
		
		panelRepository.add(lblRepository);
		panelRepository.add(this.btnChangeRepository);
		panelRepository.add(this.btnViewRepository);

		final PluginInformationPane informationPane = new PluginInformationPane();
		
		this.add(panelRepository, BorderLayout.NORTH);
		this.add(informationPane, BorderLayout.CENTER);
		
		this.btnChangeRepository.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				String newRepository = JOptionPane.showInputDialog(
					"Introduce the URL of the repository:", 
					PluginManager.getInstance().getHost()
				);
				if (newRepository != null) {
//					try {
						PluginInformationComponent.this.btnChangeRepository.setEnabled(false);
						PluginInformationComponent.this.btnViewRepository.setEnabled(false);
						
						PluginManager.getInstance().setPluginRepository(newRepository);
//						lblRepository.setText("Repository: " + newRepository);
//						PluginActionProvider.pluginDownloaderChanged();
//						informationPane.repaint();
//					} catch (IOException e1) {
//					}
				}
//				btnViewRepository.setEnabled(PluginManager.getInstance().isDownloaderActive());
			}
		});
		
		this.btnViewRepository.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					RepositoryInformationPane pane = new RepositoryInformationPane();
					JFrame frmRepository = new JFrame("Repository: " + PluginManager.getInstance().getHost());
					frmRepository.getContentPane().add(pane);
					frmRepository.pack();
					pane.packColumns();
					frmRepository.setLocationRelativeTo(null);
					frmRepository.setVisible(true);
				} catch (NotInitializedException nie) {
					JOptionPane.showMessageDialog(null, "Repository not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		PluginManager.getInstance().addPluginManagerListener(new PluginManagerAdapter() {
			/* (non-Javadoc)
			 * @see es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerAdapter#downloaderChanged(es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerEvent)
			 */
			@Override
			public void downloaderChanged(PluginManagerEvent event) {
				PluginInformationComponent.this.btnChangeRepository.setEnabled(true);
				PluginInformationComponent.this.btnViewRepository.setEnabled(PluginManager.getInstance().isDownloaderActive());
				PluginInformationComponent.this.lblRepository.setText("Repository: " + PluginManager.getInstance().getHost());
			}
			
			/* (non-Javadoc)
			 * @see es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerAdapter#downloaderChangeError(es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerEvent)
			 */
			@Override
			public void downloaderChangeError(PluginManagerEvent event) {
				Workbench.getInstance().error(event.getException(), "Error changing repository");
//				JOptionPane.showMessageDialog(
//					Workbench.getInstance().error(message), 
//					"Error changing repository host: " + event.getException().getMessage(), 
//					"Error", 
//					JOptionPane.ERROR_MESSAGE
//				);
					
				PluginInformationComponent.this.btnChangeRepository.setEnabled(true);
				PluginInformationComponent.this.btnViewRepository.setEnabled(PluginManager.getInstance().isDownloaderActive());
			}
		});
		
		this.btnViewRepository.setEnabled(PluginManager.getInstance().isDownloaderActive());
	}
}
