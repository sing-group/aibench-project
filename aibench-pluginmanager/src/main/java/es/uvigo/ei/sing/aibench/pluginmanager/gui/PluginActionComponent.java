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
 * PluginManagerProvider.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 11/04/2009
 */
package es.uvigo.ei.sing.aibench.pluginmanager.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.platonos.pluginengine.PluginEngineException;

import es.uvigo.ei.aibench.repository.NotInitializedException;
import es.uvigo.ei.aibench.repository.PluginDownloadEvent;
import es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent;
import es.uvigo.ei.aibench.repository.PluginDownloadListener;
import es.uvigo.ei.aibench.repository.info.PluginInfo;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManager;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginActionComponent extends JPanel implements RolloverRenderer, PluginDownloadListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum State { EMPTY, DOWNLOADABLE, DOWNLOADING, FINISHED, ERROR };
	
	private State state;
	private Throwable error;
	
	private final PluginInfo pluginInfo;
	private final String update;
	
	private final JXHyperlink linkInstall;
	private final JXHyperlink linkError;
	private final DownloadPanel panelDownload;
	private final JLabel lblMessage;
	
	private final List<Container> containers = new ArrayList<Container>();
	
	public PluginActionComponent() {
		super();
		this.state = State.EMPTY;
		
		this.pluginInfo = null;
		this.update = null;
		this.error = null;
		
		this.linkInstall = null;
		this.linkError = null;
		this.panelDownload = null;
		
		this.lblMessage = new JLabel();
		this.add(this.lblMessage);
	}
	
	public PluginActionComponent(PluginInfo pluginInfo) {
		this(pluginInfo, null);
	}
	
	public PluginActionComponent(PluginInfo pluginInfo, String update) {
		super(new BorderLayout());
		this.pluginInfo = pluginInfo;
		this.update = update;
		this.state = State.DOWNLOADABLE;
		this.error = null;
		
		this.linkInstall = new JXHyperlink(new DownloadAction());
		this.linkError = new JXHyperlink(new ErrorAction());
		this.panelDownload = new DownloadPanel();
		this.lblMessage = new JLabel();
		
		this.linkInstall.setHorizontalTextPosition(SwingConstants.CENTER);
		this.linkError.setHorizontalTextPosition(SwingConstants.CENTER);
		this.linkError.setForeground(Color.red);
		
		this.add(this.linkInstall, BorderLayout.CENTER);
	}
	
	public State getState() {
		return this.state;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.rollover.RolloverRenderer#doClick()
	 */
	public void doClick() {
		switch(this.state) {
		case DOWNLOADABLE:
			this.linkInstall.doClick();
			break;
		case DOWNLOADING:
			this.panelDownload.doClick();
			break;
		case ERROR:
			this.linkError.doClick();
			break;
		}
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.rollover.RolloverRenderer#isEnabled()
	 */
	public boolean isEnabled() {
		return this.state != State.EMPTY;
	}
	
	public boolean isUpdate() {
		return this.update != null;
	}

	public ButtonModel getButtonModel() {
		if (this.state == State.DOWNLOADABLE) {
			return this.linkInstall.getModel();
		} else if (this.state == State.DOWNLOADING) {
			return this.panelDownload.getCancelButtonModel();
		} else {
			return null;
		}
	}
	
	public synchronized void addContainer(Container container) {
		if (!this.containers.contains(container)) {
			Set<Container> notValid = new HashSet<Container>();
			for (Container c:this.containers) {
				if (SwingUtilities.isDescendingFrom(container, c)) {
					return;
				} else if (SwingUtilities.isDescendingFrom(c, container)) {
					notValid.add(c);
				}
			}
			this.containers.removeAll(notValid);
			this.containers.add(container);
		}
	}
	
	private synchronized void repaintContainers() {
		for (Container container:this.containers) {
			container.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadStarted(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadStarted(final PluginDownloadEvent event) {
		if (this.state == State.EMPTY) return;
		
		if (event.getUid().equalsIgnoreCase(this.pluginInfo.getUID())) {
			SwingUtilities.invokeLater(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					PluginActionComponent.this.state = State.DOWNLOADING;
					PluginActionComponent.this.panelDownload.setDownloadId(event.getDownloadId());
					PluginActionComponent.this.panelDownload.setTotal(event.getTotal());
					PluginActionComponent.this.removeAll();
					PluginActionComponent.this.add(PluginActionComponent.this.panelDownload);
					PluginActionComponent.this.repaintContainers();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadStep(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadStep(final PluginDownloadEvent event) {
		if (this.state == State.EMPTY) return;

		if (event.getUid().equalsIgnoreCase(this.pluginInfo.getUID())) {
			PluginActionComponent.this.panelDownload.setDownloaded(event.getDownloaded());
			SwingUtilities.invokeLater(new Runnable()  {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					PluginActionComponent.this.repaintContainers();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadFinished(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadFinished(final PluginDownloadEvent event) {
		if (this.state == State.EMPTY) return;

		if (event.getUid().equalsIgnoreCase(this.pluginInfo.getUID())) {
			SwingUtilities.invokeLater(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					if (event.isCompleted()) {
						PluginActionComponent.this.state = State.FINISHED;
						PluginActionComponent.this.lblMessage.setText("Reboot to Install");
						PluginActionComponent.this.removeAll();
						PluginActionComponent.this.add(PluginActionComponent.this.lblMessage, BorderLayout.CENTER);
						PluginActionComponent.this.repaintContainers();						
					} else {
						PluginActionComponent.this.state = State.DOWNLOADABLE;
						PluginActionComponent.this.panelDownload.reset();
						PluginActionComponent.this.removeAll();
						PluginActionComponent.this.add(PluginActionComponent.this.linkInstall, BorderLayout.CENTER);
						PluginActionComponent.this.repaintContainers();
					}
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadError(es.uvigo.ei.aibench.repository.PluginDownloadEvent)
	 */
	public void downloadError(final PluginDownloadEvent event) {
		if (this.state == State.EMPTY) return;

		if (event.getUid().equalsIgnoreCase(this.pluginInfo.getUID())) {
			SwingUtilities.invokeLater(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					PluginActionComponent.this.state = State.ERROR;
					PluginActionComponent.this.error = event.getError();
					PluginActionComponent.this.removeAll();
					PluginActionComponent.this.add(PluginActionComponent.this.linkError, BorderLayout.CENTER);
					PluginActionComponent.this.repaintContainers();
				}
			});
		}
	}
	
	private final class ErrorAction extends AbstractActionExt {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ErrorAction() {
			super("[ERROR]");
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (PluginActionComponent.this.state == State.ERROR && PluginActionComponent.this.error != null) {
				String message = String.format("Error downloading: \n%s", PluginActionComponent.this.error.toString());
				JOptionPane.showMessageDialog(PluginActionComponent.this, message, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private final class DownloadAction extends AbstractActionExt {
		private final static String NAME_INSTALL = "[Install - Version %s]";
		private final static String NAME_UPDATE = "[Update  - Version %s]";
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DownloadAction() {
			super();
			try {
				this.setName(String.format(
					(PluginActionComponent.this.isUpdate()?NAME_UPDATE:NAME_INSTALL), 
					PluginActionComponent.this.pluginInfo.getPluginVersion().toString())
				);
			} catch (PluginEngineException e) {
				this.setName(String.format(
					(PluginActionComponent.this.isUpdate()?NAME_UPDATE:NAME_INSTALL), 
					PluginActionComponent.this.pluginInfo.getVersion())
				);
			}
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent event) {
			if (PluginActionComponent.this.state == State.DOWNLOADABLE) {
				try {
					if (PluginActionComponent.this.isUpdate()) {
						PluginManager.getInstance().downloadPlugin(PluginActionComponent.this.pluginInfo, PluginActionComponent.this.update);
					} else {
						PluginManager.getInstance().downloadPlugin(PluginActionComponent.this.pluginInfo);					
					}
				} catch (NotInitializedException e) {
					JOptionPane.showMessageDialog(
						PluginActionComponent.this, 
						"Error downloading the plugin: the plugin downloader isn't initialized.", 
						"Error", 
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		}
	}
	
	private final class DownloadPanel extends JPanel implements RolloverRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JLabel lblDownload;
		private final JProgressBar prgDownload;
		private final JButton btnCancel;
		
		private int downloadId;
		public DownloadPanel() {
			super(new BorderLayout());
			
			this.lblDownload = new JLabel("0,0%");
			this.prgDownload = new JProgressBar(0, 1);
			this.btnCancel = new JButton("X");
			this.btnCancel.setToolTipText("Cancel download");
			
			this.downloadId = -1;
			
			this.btnCancel.addActionListener(new ActionListener() {
				/* (non-Javadoc)
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				public void actionPerformed(ActionEvent e) {
					DownloadPanel.this.cancelDownload();
				}
			});

			this.add(this.lblDownload, BorderLayout.WEST);
			this.add(this.prgDownload, BorderLayout.CENTER);
			this.add(this.btnCancel, BorderLayout.EAST);
		}
		
		private void cancelDownload() {
			if (this.downloadId != -1 && PluginActionComponent.this.state == State.DOWNLOADING) {
				PluginManager.getInstance().cancelDownload(this.downloadId);
			}
		}
		
		public void setDownloadId(int downloadId) {
			this.downloadId = downloadId;
		}
		
		public void setDownloaded(int value) {
			this.prgDownload.setValue(value);
			this.lblDownload.setText(String.format("%.1f%%", this.prgDownload.getPercentComplete()*100));
		}
		
		public void setTotal(int total) {
			this.prgDownload.setMaximum(total);
			this.lblDownload.setText(String.format("%.1f%%", this.prgDownload.getPercentComplete()*100));
		}
		
		public void reset() {
			this.setDownloadId(-1);
			this.setDownloaded(0);
			this.setTotal(1);
		}
		
		public ButtonModel getCancelButtonModel() {
			return this.btnCancel.getModel();
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.rollover.RolloverRenderer#doClick()
		 */
		public void doClick() {
			this.btnCancel.doClick();
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoError(es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent)
	 */
	public void downloadInfoError(PluginDownloadInfoEvent event) {
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoFinished(es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent)
	 */
	public void downloadInfoFinished(PluginDownloadInfoEvent event) {
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.repository.PluginDownloadListener#downloadInfoStarted(es.uvigo.ei.aibench.repository.PluginDownloadInfoEvent)
	 */
	public void downloadInfoStarted(PluginDownloadInfoEvent event) {
	}
}
