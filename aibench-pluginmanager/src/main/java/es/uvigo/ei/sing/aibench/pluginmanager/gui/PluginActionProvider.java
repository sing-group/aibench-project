/*
 * #%L
 * The AIBench Plugin Manager Plugin
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
package es.uvigo.ei.sing.aibench.pluginmanager.gui;

import java.awt.Point;
import java.io.File;
import java.util.Hashtable;

import javax.swing.ButtonModel;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.platonos.pluginengine.Plugin;

import es.uvigo.ei.aibench.repository.info.PluginInfo;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManager;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerEvent;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManagerListener;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginActionProvider extends ComponentProvider<PluginActionComponent> implements RolloverRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static Hashtable<String, PluginActionComponent> UID_PAC = new Hashtable<String, PluginActionComponent>();
	private final static PluginActionComponent emptyPAC = new PluginActionComponent();
	
	static {
		PluginManager.getInstance().addPluginManagerListener(new PluginManagerListener() {
			
			public void installerChanged(PluginManagerEvent event) {}
			
			public void downloaderChanged(PluginManagerEvent event) {
				PluginActionProvider.UID_PAC.clear();
			}
			
			public void downloaderChangeError(PluginManagerEvent event) {}
			
			public void installerChangeError(PluginManagerEvent event) {}
		});
	}
//
//	public final static void pluginDownloaderChanged() {
//		PluginActionProvider.UID_PAC.clear();
//	}
	
	private final static PluginActionComponent getPluginActionComponent(String uid) {
		if (uid == null || !PluginManager.getInstance().isDownloaderActive())
			return PluginActionProvider.emptyPAC;
		
		if (!PluginActionProvider.UID_PAC.containsKey(uid)) {
			synchronized(PluginActionProvider.UID_PAC) {
				if (!PluginActionProvider.UID_PAC.containsKey(uid)) {
					PluginInfo info = PluginManager.getInstance().getDownloadPluginInfo(uid);
					if (info == null) {
						PluginActionProvider.UID_PAC.put(uid, PluginActionProvider.emptyPAC);
					} else {
						Plugin plugin = PluginManager.getInstance().getInstalledPlugin(uid);
						boolean hasUpdate = PluginManager.getInstance().hasUpdateAvailable(uid);
						if (plugin == null) { // Install
							PluginActionComponent pac = new PluginActionComponent(info);
							PluginManager.getInstance().getPluginDownloader().addDownloadListener(pac);
							PluginActionProvider.UID_PAC.put(uid, pac);
						} else if (hasUpdate) { // Update
							File file = new File(plugin.getPluginURL().getPath());
							PluginActionComponent pac = new PluginActionComponent(info, file.getName());
							PluginManager.getInstance().getPluginDownloader().addDownloadListener(pac);
							PluginActionProvider.UID_PAC.put(uid, pac);							
						} else {
							PluginActionProvider.UID_PAC.put(uid, PluginActionProvider.emptyPAC);
						}
					}
				}
			}
		}
		
		return PluginActionProvider.UID_PAC.get(uid);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#getRendererComponent(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	public PluginActionComponent getRendererComponent(CellContext context) {
		if (context.getValue() instanceof String) {
			String uid = (String) context.getValue();
			this.rendererComponent = PluginActionProvider.getPluginActionComponent(uid);
			this.rendererComponent.addContainer(context.getComponent());
		} else {
			this.rendererComponent = PluginActionProvider.emptyPAC;
		}
		return super.getRendererComponent(context);
	}
	
	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#createRendererComponent()
	 */
	@Override
	protected PluginActionComponent createRendererComponent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#configureState(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void configureState(CellContext context) {
		ButtonModel model = this.rendererComponent.getButtonModel();

		if (model != null && context.getComponent() != null) {
			Point p = (Point) context.getComponent().getClientProperty(
					RolloverProducer.ROLLOVER_KEY);
			if (/* hasFocus || */(p != null && (p.x >= 0)
					&& (p.x == context.getColumn()) && (p.y == context.getRow()))) {
				if (!model.isRollover())
					model.setRollover(true);
			} else {
				if (model.isRollover())
					model.setRollover(false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#format(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void format(CellContext context) {}


	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.rollover.RolloverRenderer#doClick()
	 */
	public void doClick() {
		if (this.rendererComponent != null) {
			this.rendererComponent.doClick();
		}
	}


	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.rollover.RolloverRenderer#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
//		if (this.rendererComponent != null) {
//			return this.rendererComponent.isEnabled();
//		} else {
//			return false;
//		}
	}

}
