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
package es.uvigo.ei.aibench.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class MemoryMonitor extends JPanel {

	private static final long	serialVersionUID	= 3977294434511893552L;

	MonitorComponent			mc;

	public MemoryMonitor() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(new EtchedBorder(), "Memory Monitor"));
		mc = new MonitorComponent();
		add(mc);
        JButton btnGC = new JButton("Collect Garbage");
        add("South", btnGC);
        btnGC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.gc();
            }
        });
	}

	public static void main(String[] args) {
		MemoryMonitor.monitorizeMemory();
	}

	public static void monitorizeMemory() {
		JFrame frame = new JFrame("Memoria");
		frame.setResizable(true);
		frame.getContentPane().add("Center", new MemoryMonitor());

		JButton btnGC = new JButton("Recoger Basura");
		frame.getContentPane().add("South", btnGC);
		btnGC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.gc();
			}
		});
		frame.pack();

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	private class MonitorComponent extends JPanel implements Runnable {
		/**
		 * Serial Version UID
		 */
		private static final long	serialVersionUID	= 3688790271391445552L;

		public Thread				thread;

		private int					w;

		private int					h;

		private BufferedImage		bimg;

		private Graphics2D			big;

		private Font				font				= new Font("Times New Roman", Font.PLAIN, 11);

		private Runtime				r					= Runtime.getRuntime();

		private int					columnInc;

		private int[]				pts;

		private int					ptNum;

		private int					ascent;

		private int					descent;


		private Rectangle			graphOutlineRect	= new Rectangle();

		private Rectangle2D			mfRect				= new Rectangle2D.Float();

		private Rectangle2D			muRect				= new Rectangle2D.Float();

		private Line2D				graphLine			= new Line2D.Float();

		private Color				graphColor			= new Color(46, 139, 87);

		private Color				mfColor				= new Color(0, 100, 0);

		public MonitorComponent() {
			setBackground(Color.black);
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (thread == null) {
						start();
					} else {
						stop();
					}
				}
			});
			start();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getPreferredSize() {
			return new Dimension(100, 100);
		}

		public void paint(Graphics g) {
			if (big == null) {
				return;
			}

			big.setBackground(getBackground());
			big.clearRect(0, 0, w, h);

			float freeMemory = (float) r.freeMemory();
			float totalMemory = (float) r.totalMemory();

			// .. Draw allocated and used strings ..
			big.setColor(Color.green);
			big.drawString(String.valueOf((int) totalMemory / 1024) + "K allocated", 4.0f, (float) ascent + 0.5f);
			big.drawString(String.valueOf(((int) (totalMemory - freeMemory)) / 1024) + "K used", 4, h - descent);

			// Calculate remaining size
			float ssH = ascent + descent;
			float remainingHeight = (float) (h - (ssH * 2) - 0.5f);
			float blockHeight = remainingHeight / 10;
			float blockWidth = 20.0f;


			// .. Memory Free ..
			big.setColor(mfColor);

			int MemUsage = (int) ((freeMemory / totalMemory) * 10);
			int i = 0;

			for (; i < MemUsage; i++) {
				mfRect.setRect(5, (float) ssH + (i * blockHeight), blockWidth, (float) blockHeight - 1);
				big.fill(mfRect);
			}

			// .. Memory Used ..
			big.setColor(Color.green);

			for (; i < 10; i++) {
				muRect.setRect(5, (float) ssH + (i * blockHeight), blockWidth, (float) blockHeight - 1);
				big.fill(muRect);
			}

			// .. Draw History Graph ..
			big.setColor(graphColor);

			int graphX = 30;
			int graphY = (int) ssH;
			int graphW = w - graphX - 5;
			int graphH = (int) remainingHeight;
			graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
			big.draw(graphOutlineRect);

			int graphRow = graphH / 10;

			// .. Draw row ..
			for (int j = graphY; j <= (graphH + graphY); j += graphRow) {
				graphLine.setLine(graphX, j, graphX + graphW, j);
				big.draw(graphLine);
			}

			// .. Draw animated column movement ..
			int graphColumn = graphW / 15;

			if (columnInc == 0) {
				columnInc = graphColumn;
			}

			for (int j = graphX + columnInc; j < (graphW + graphX); j += graphColumn) {
				graphLine.setLine(j, graphY, j, graphY + graphH);
				big.draw(graphLine);
			}

			--columnInc;

			if (pts == null) {
				pts = new int[graphW];
				ptNum = 0;
			} else if (pts.length != graphW) {
				int[] tmp = null;

				if (ptNum < graphW) {
					tmp = new int[ptNum];
					System.arraycopy(pts, 0, tmp, 0, tmp.length);
				} else {
					tmp = new int[graphW];
					System.arraycopy(pts, pts.length - tmp.length, tmp, 0, tmp.length);
					ptNum = tmp.length - 2;
				}

				pts = new int[graphW];
				System.arraycopy(tmp, 0, pts, 0, tmp.length);
			} else {
				big.setColor(Color.yellow);
				pts[ptNum] = (int) (graphY + (graphH * (freeMemory / totalMemory)));

				for (int j = (graphX + graphW) - ptNum, k = 0; k < ptNum; k++, j++) {
					if (k != 0) {
						if (pts[k] != pts[k - 1]) {
							big.drawLine(j - 1, pts[k - 1], j, pts[k]);
						} else {
							big.fillRect(j, pts[k], 1, 1);
						}
					}
				}

				if ((ptNum + 2) == pts.length) {
					// throw out oldest point
					for (int j = 1; j < ptNum; j++) {
						pts[j - 1] = pts[j];
					}

					--ptNum;
				} else {
					ptNum++;
				}
			}

			g.drawImage(bimg, 0, 0, this);
		}

		public void run() {
			Thread me = Thread.currentThread();

			while (((thread == me) && !isShowing()) || (getSize().width == 0)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					thread = null;

					return;
				}
			}

			while ((thread == me) && isShowing()) {
				Dimension d = getSize();

				if ((d.width != w) || (d.height != h)) {
					w = d.width;
					h = d.height;
					bimg = (BufferedImage) createImage(w, h);
					big = bimg.createGraphics();
					big.setFont(font);

					FontMetrics fm = big.getFontMetrics(font);
					ascent = (int) fm.getAscent();
					descent = (int) fm.getDescent();
				}

				repaint();

				try {
					Thread.sleep(999);
				} catch (InterruptedException e) {
					break;
				}
			}

			thread = null;
		}

		public void start() {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.setName("MemoryMonitor");
			thread.start();
		}

		public synchronized void stop() {
			thread = null;
			notify();
		}
	}
}
