/*
 * #%L
 * The AIBench basic runtime and plugin engine
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
package es.uvigo.ei.aibench;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.event.IPluginEngineListener;
import org.platonos.pluginengine.event.PluginEngineEvent;
import org.platonos.pluginengine.event.PluginEngineEventType;

/**
 * The AIBench starting splash frame
 *
 * This class shows a loading application splash image, by also showing the plugin loading progress.
 *
 * @author Rubén Domínguez Carbajales
 * @author Daniel Glez-Peña
 */
public class SplashFrame extends JFrame implements IPluginEngineListener {

	private static Logger logger = Logger.getLogger(SplashFrame.class);

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JProgressBar progress = new JProgressBar();

	private Image fImage;

	private int current = 0, toStart = 0;

	/**
	 * Creates the splash frame. Do not shows it. Call {@link #setVisible(boolean) setVisible} to do so.
	 */
	public SplashFrame() {
		super();
		initialize();

		this.progress.setStringPainted(true);
		this.progress.setAlignmentX(JProgressBar.LEFT_ALIGNMENT);
		this.progress.setMaximum(20);
		this.progress.setString("Initializing plugins...");
	}

	private void initialize() {

		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setSize(fImage.getWidth(null), fImage.getHeight(null) + 10);
		this.add(progress, BorderLayout.SOUTH);
		this.setTitle("Welcome to AIBench");

		// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Rectangle window = getBounds();
		// setLocation((screen.width - window.width) / 2, (screen.height -
		// window.height) / 2);

		this.setAlwaysOnTop(true);
		this.setUndecorated(true);
		this.pack();

		this.setLocationRelativeTo(null);
	}

	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {

			URL imageURL = null;
			if (Launcher.CONFIG.getProperty("splashimage") != null) {
				imageURL = Launcher.class.getProtectionDomain().getCodeSource()
						.getLocation();
				try {
					if (imageURL.getFile().endsWith(".jar")) {
						imageURL = new URL(imageURL.toString().substring(0,
								imageURL.toString().lastIndexOf('/'))
								+ "/../"
								+ Launcher.CONFIG.getProperty("splashimage"));
					} else {
						imageURL = new URL(imageURL + "../"
								+ Launcher.CONFIG.getProperty("splashimage"));
					}

				} catch (MalformedURLException e1) {
					System.err
							.println("Not found the specified splash image, searching in url: "
									+ imageURL.getFile() + "using default");
					imageURL = SplashFrame.class.getResource("/splash.jpg");
				}

			} else {
				imageURL = SplashFrame.class.getResource("/splash.jpg");
			}

			// fImage = Toolkit.getDefaultToolkit().getImage(imageURL);
			MediaTracker mt = new MediaTracker(this);
			fImage = Toolkit.getDefaultToolkit().getImage(imageURL);

			mt.addImage(fImage, 0);

			try {
				mt.waitForAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			jContentPane = new javax.swing.JPanel() {

				private static final long serialVersionUID = 1L;

				public void paintComponent(Graphics g) {
					super.paintComponent(g);

					if (fImage != null) {
						int imwidth = fImage.getWidth(null);
						int imheight = fImage.getHeight(null);

						if ((imwidth > 0) && (imheight > 0)) {
							for (int y = 0; y < getHeight(); y += imheight) {
								for (int x = 0; x < getWidth(); x += imwidth) {
									g.drawImage(fImage, x, y, null);
								}
							}
						}
					}
				}
			};
			jContentPane.setLayout(null);
			jContentPane.setOpaque(true);
			jContentPane.setPreferredSize(new java.awt.Dimension(fImage
					.getWidth(null), fImage.getHeight(null)));
		}

		return jContentPane;
	}

	/**
	 * Handles plugin engine events, interested in loading events in order to update the progress bar
	 *
	 * @param evt The plugin engine event
	 */
	@Override
	public void handlePluginEngineEvent(PluginEngineEvent evt) {
		if (evt.getEventType().equals(PluginEngineEventType.STARTUP)) {

		} else if (evt.getEventType().equals(PluginEngineEventType.PLUGIN_LOADED)) {
			Plugin plugin = (Plugin) evt.getPayload();
			if (!plugin.isDisabled() && plugin.getStartWhenResolved()) {
				this.toStart++;
				this.progress.setMaximum(this.toStart);
			}
			logger.info("Loaded " + plugin.getName() + "   [version " + plugin.getVersion() + "]");
		} else if (evt.getEventType().equals(PluginEngineEventType.PLUGIN_STARTING)) {
			this.progress.setString("Starting " + evt.getPayload().toString());
		} else if (evt.getEventType().equals(PluginEngineEventType.PLUGIN_STARTED)
				|| evt.getEventType().equals(PluginEngineEventType.PLUGIN_DISABLED)) {
			this.progress.setValue(++this.current);
			
			if (this.toStart == this.current) {
				this.setVisible(false);
				this.dispose();
			}
		}
	}
}
