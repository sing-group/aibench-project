/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

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
 * ShellFrame.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;
import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import bsh.EvalError;
import bsh.util.JConsole;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.workbench.utilities.FileDrop;

public class ShellFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private OperationsTextArea operationsTextArea = null;
	private JPanel jPanel = null;
	private JScrollPane jScrollPane = null;
	private JSplitPane jSplitPane = null;

	private AIBenchInterpreter interpreter =null;

	
	
	/**
	 * This method initializes
	 *
	 */
	public ShellFrame(AIBenchInterpreter interpreter) {
		super();
		this.interpreter=interpreter;
		//this.interpreter.setConsole(this.getConsole());
		initialize();
		Core.getInstance().getHistory().addHistoryListener(this.getOperationsTextArea());
		

	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		this.setTitle("AIBench Shell by Lipido");
       // this.setSize(new java.awt.Dimension(515,459));
        this.setContentPane(getJPanel());
       // this.getJSplitPane().setDividerLocation(0.5f);
        //this.getJSplitPane().setResizeWeight(0.5f);


	}

	/**
	 * This method initializes operationsTextArea
	 *
	 * @return es.uvigo.ei.sing.aibench.shell.OperationsTextArea
	 */
	private OperationsTextArea getOperationsTextArea() {
		if (operationsTextArea == null) {
			operationsTextArea = new OperationsTextArea();
			operationsTextArea.setBackground(java.awt.Color.lightGray);
			operationsTextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
		}
		return operationsTextArea;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(getJPanel1(), java.awt.BorderLayout.NORTH);
			jPanel.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getOperationsTextArea());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jSplitPane
	 *
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
			jSplitPane.setOneTouchExpandable(true);
			jSplitPane.setBottomComponent(getJPanel3());
			jSplitPane.setTopComponent(getJPanel2());
		}
		return jSplitPane;
	}



	private JPanel jPanel1 = null;
	private JTextArea jTextArea = null;
	
	private JPanel jPanel2 = null;
	private JPanel jPanel3 = null;
	private JTextArea jTextArea2 = null;
	private JPanel jPanel4 = null;
	
	private JButton jButton = null;
	private JToolBar jToolBar1 = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;

	private JConsole getConsole(){
		/*if (jConsole==null){
			jConsole = new JConsole();
	        jConsole.setFocusable(true);

		}
		return jConsole;*/
		new FileDrop(interpreter.getConsole(), new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				if (files.length > 0) {
					// Run the first file
					final File f = files[0];
					new Thread() {
						public void run() {
							try {
								interpreter.getInterpreter().source(
										f.getAbsolutePath());
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (EvalError e1) {
								e1.printStackTrace();
							}
						}
					}.start();
				}
			}
		}); 
		return interpreter.getConsole();

	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			
			
			jPanel1.add(getJToolBar1(), java.awt.BorderLayout.NORTH);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTextArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setEditable(false);
			jTextArea.setBackground(java.awt.Color.black);
			jTextArea.setForeground(java.awt.Color.white);
			jTextArea.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
			jTextArea.setText("Operation Log");
		}
		return jTextArea;
	}

	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BorderLayout());
			JPanel composite = new JPanel();
			composite.setLayout(new BorderLayout());
			composite.add(getJTextArea(), BorderLayout.NORTH);
			
			//composite.add(getJTextArea1(), java.awt.BorderLayout.CENTER);
			
			jPanel2.add(composite, java.awt.BorderLayout.NORTH);
			jPanel2.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new BorderLayout());
			jPanel3.add(getConsole(), java.awt.BorderLayout.CENTER);
			jPanel3.add(getJPanel4(), java.awt.BorderLayout.NORTH);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jTextArea2
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextArea2() {
		if (jTextArea2 == null) {
			jTextArea2 = new JTextArea();
			jTextArea2.setEditable(false);
			jTextArea2.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
			jTextArea2.setText("Console");
			jTextArea2.setBackground(java.awt.Color.black);
			jTextArea2.setForeground(java.awt.Color.white);
		}
		return jTextArea2;
	}

	/**
	 * This method initializes jPanel4
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new JPanel();
			jPanel4.setLayout(new BorderLayout());
			jPanel4.add(getJTextArea2(), java.awt.BorderLayout.SOUTH);
			//jPanel4.add(getJToolBar(), java.awt.BorderLayout.NORTH);
		}
		return jPanel4;
	}

	

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Load script...");
			jButton.setIcon(new ImageIcon(getClass().getResource("/icons/open.gif")));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					if (chooser.showOpenDialog(ShellFrame.this)==JFileChooser.APPROVE_OPTION){
						final File f = chooser.getSelectedFile();
							new Thread(){
							public void run(){
								try {
								interpreter.getInterpreter().source(f.getAbsolutePath());
								} catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (EvalError e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							}.start();
					}

				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jToolBar1
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar1() {
		if (jToolBar1 == null) {
			jToolBar1 = new JToolBar();
			jToolBar1.add(getJButton());
			jToolBar1.add(getJButton1());
			jToolBar1.add(getJButton2());
		}
		return jToolBar1;
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Save script...");
			jButton1.setIcon(new ImageIcon(getClass().getResource("/icons/saveas.gif")));
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					if( chooser.showSaveDialog(ShellFrame.this) == JFileChooser.APPROVE_OPTION ){
						File file = chooser.getSelectedFile();
						try {
							PrintStream ps = new PrintStream(new FileOutputStream(file));
							ps.print(getOperationsTextArea().getText());
							ps.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}


					}
				}
			});
		}
		return jButton1;
	}
	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Clear script...");
			jButton2.setIcon(new ImageIcon(getClass().getResource("/icons/cancel.png")));
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getOperationsTextArea().setText("");
				}
			});
		}
		return jButton2;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
