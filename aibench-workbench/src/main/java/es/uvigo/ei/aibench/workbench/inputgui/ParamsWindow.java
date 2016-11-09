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
 * ParamsWindow.java
 *
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 12/09/2006
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.help.HelpBroker;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.CoreUtils;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.InputGUI;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;


/**
 * This class is the default input GUI for AIBench's operations. It implements a dynamic JDialog adding components for each incoming port of the operation
 * 
 * @author Daniel Glez-Peña
 *
 */
public class  ParamsWindow extends JDialog implements InputGUI {
	private static final long serialVersionUID = 1L;
	
	private static final String BUTTONICON_CANCEL = "paramswindow.buttonicon.cancel";
	private static final String BUTTONTEXT_CANCEL = "paramswindow.buttontext.cancel";
	private static final String BUTTONTEXT_OK = "paramswindow.buttontext.ok";
	private static final String BUTTONICON_OK = "paramswindow.buttonicon.ok";
	
	static Logger logger = Logger.getLogger(ParamsWindow.class.getName());

	//// COMPONENTS
	JTextArea textArea = null;
	JPanel buttonsPanel = null;
	
	private OperationDefinition<?> operation;
	private Object operationObject;
	private List<ParamProvider> providers= new ArrayList<ParamProvider>();
	private boolean wasCancelled = false;

	private ParamsReceiver receiver;

	public static ClipboardItem preferredClipboardItem;
	private JPanel inputComponents;

	public ParamsWindow() {
		super(Workbench.getInstance().getMainFrame());
	}

	public JPanel createInputComponents() {
		if (inputComponents != null) {
			throw new RuntimeException("Can't create input components twice (app bug)");
		}

		List<Port> incomingPorts = getIncomingPorts();
		Map<Port, Class<?>> portClass = getPortClasses(incomingPorts);

		List<Port> incomingVisiblePorts = new ArrayList<Port>();
		List<Port> incomingAdvancedPorts = new ArrayList<Port>();
		for (Port p : incomingPorts) {
			if (p.advanced()) {
				incomingAdvancedPorts.add(p);
			} else {
				incomingVisiblePorts.add(p);
			}
		}

		JPanel advancedPortsPane = null;
		if (!incomingAdvancedPorts.isEmpty()) {
			advancedPortsPane = createParamsPanel(incomingAdvancedPorts, portClass);
		}
		JPanel basicPortsPane = createParamsPanel(incomingVisiblePorts, portClass); 

		JPanel toret = new JPanel(new BorderLayout());
		toret.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		toret.add(basicPortsPane, BorderLayout.NORTH);

		if (!incomingAdvancedPorts.isEmpty()) {
			toret.add(createAdvancedOptionsTaskPaneContainer(advancedPortsPane), BorderLayout.CENTER);
		}

		return toret;
	}

	protected List<Port> getIncomingPorts() {
		List<Port> incomingPorts = new ArrayList<Port>();
		for (Port p : this.operation.getPorts()) {
			if (p.direction() != Direction.OUTPUT) {
				incomingPorts.add(p);
			}
		}
		return incomingPorts;
	}

	private Map<Port, Class<?>> getPortClasses(List<Port> incomingPorts) {
		Map<Port, Class<?>> portClass = new HashMap<>();
		int i = 0;
		for (Class<?> clazz : this.operation.getIncomingArgumentTypes()) {
			portClass.put(incomingPorts.get(i++), clazz);
		}
		return portClass;
	}

	private Component createAdvancedOptionsTaskPaneContainer(JPanel advancedPortsPane) {
		JXTaskPaneContainer advancedOptionsTaskPaneContainer =
				new JXTaskPaneContainer();
		advancedOptionsTaskPaneContainer.setOpaque(false);
		
		JXTaskPane advancedOptionsTaskPane = new JXTaskPane();
		advancedOptionsTaskPane.setTitle(getAdvancedOptionsTitle());
		advancedOptionsTaskPane.setCollapsed(true);
		advancedOptionsTaskPane.addPropertyChangeListener("collapsed",
				new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				_pack();
			}
		});
		advancedOptionsTaskPane.add(advancedPortsPane, BorderLayout.CENTER);
		advancedOptionsTaskPaneContainer.add(advancedOptionsTaskPane);

		return advancedOptionsTaskPaneContainer;
	}

	private String getAdvancedOptionsTitle() {
		String showHelp = Workbench.CONFIG.getProperty("paramswindow.advancedoptionstitle");
		if (showHelp != null) {
			return showHelp;
		} else {
			return "Advanced options";
		}
	}

	protected final JPanel createParamsPanel(List<Port> ports, Map<Port, Class<?>> portClass) {
		JPanel toret = new JPanel();
		
		GridBagLayout layout = new GridBagLayout();
		toret.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();

		int i = 0;
		boolean first = true;
		for (Port incomingPort: ports){

			//Name label
			JLabel nameLabel = new JLabel(incomingPort.name());
			nameLabel.setHorizontalAlignment(JLabel.RIGHT);
			nameLabel.setVerticalAlignment(JLabel.TOP);

			nameLabel.setAlignmentX(JLabel.TOP_ALIGNMENT);
			//nameLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
			c.gridx = 0;
			c.gridy = i + 1;
			c.gridwidth = 1;
			c.weightx = 0.0f;
			c.weighty = 1.0f;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(10, 12, 5, 5);

			layout.setConstraints(nameLabel, c);

			toret.add(nameLabel);

			//Input Component
			ParamProvider provider = getParamProvider(incomingPort, portClass.get(incomingPort), operationObject);
			if (provider != null) {
				provider.init();
				
				this.providers.add(provider);
				JComponent component = provider.getComponent();
				
				if (component != null) {
					//Add a component listener to pack window if the size of the components changes dynamically
					component.addComponentListener(new ComponentAdapter(){
						public void componentResized(ComponentEvent arg0) {					
							_pack();
						}
					});
					
					c.gridx = 1;
					c.gridy = i + 1;
					c.weightx = 1.0f;
					c.weighty = 0.0f;
					c.fill = GridBagConstraints.HORIZONTAL;
					c.insets = new Insets(5, 12, 5, 5);
					layout.setConstraints(component, c);
					
					// request focus on first, not working yet!
					toret.add(component);
					if (first) {
						component.requestFocus();
						first = false;
					}
					
					// Description label
					JComponent descriptionComponent = null;
					String showHelp = Workbench.CONFIG
							.getProperty("paramswindow.showhelpicon");
					if (	showHelp != null && 
							showHelp.equalsIgnoreCase("true") && 
							incomingPort.description()!=null && 
                             incomingPort.description().length()>0
					) {
						JLabel iconLabel = new JLabel();
						String iconFile = Workbench.CONFIG.getProperty("paramswindow.helpicon");
						URL imageURL = Util.getGlobalResourceURL(iconFile);
						
						iconLabel.setIcon(new ImageIcon(iconFile == null ? 
							getClass().getResource("dialog-help.png") : imageURL));
						iconLabel.setToolTipText(
								incomingPort.description());
						
						descriptionComponent = iconLabel;
					} else {
						descriptionComponent = new JLabel(incomingPort.description());
					}
					
					c.gridx = 2;
					c.gridy = i + 1;
					c.anchor = GridBagConstraints.CENTER;
					c.weightx = 0.0f;
					
					layout.setConstraints(descriptionComponent, c);
					
					toret.add(descriptionComponent);
					this.setResizable(true);
					i++;					
				}
			}
		}

		return toret;
	}

	/**
	 * Override this method to change the component used in one port
	 * @param dialog
	 * @param p
	 * @param c
	 * @param op
	 * @return
	 */
	protected ParamProvider getParamProvider(Port p, Class<?> c, Object op){ 
		return ParamProviderFactory.createParamProvider(this.receiver, p , c, op);
	}
	
	private JTextArea getDescriptionPane() {
		if (this.textArea == null){
			this.textArea = new JTextArea(this.operation.getDescription());
			this.textArea.setMargin(new Insets(10,10,10,10));
	
			this.textArea.setWrapStyleWord(true);
			this.textArea.setLineWrap(true);
			this.textArea.setEditable(false);
			this.textArea.setBackground(Color.WHITE);
			this.textArea.setOpaque(true);
		}
		return this.textArea;
		
	}
	
	private class JButtonOk extends JButton implements Observer, ActionListener, ClipboardListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
//		private boolean finished = false;

		public JButtonOk(String label) {
			super(label);
			this.addActionListener(this);
			for (ParamProvider provider:ParamsWindow.this.providers) {
				if (provider instanceof Observable) {
					((Observable) provider).addObserver(this);
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
//			this.finished = true;
			if ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0) {
				okButton.setEnabled(false);
				ParamSpec[] spec = ParamsWindow.this.getParamSpec();
				//ParamsWindow.this.finish(); //TODO: Remove this
				receiver.paramsIntroduced(spec);				
			}
		}
		
		public void checkEnabled() {
//			if (this.finished) return;
			
			boolean enabled = true;
			for (ParamProvider provider:ParamsWindow.this.providers) {
				if (!provider.isValidValue()) {
					enabled = false;
				}
			}
			
			this.setEnabled(enabled/* && !this.finished*/);
		}
		
		/* (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable o, Object arg) {
			this.checkEnabled();
		}

		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementAdded(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
		 */
		public void elementAdded(ClipboardItem item) {
			this.checkEnabled();
		}

		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementRemoved(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
		 */
		public void elementRemoved(ClipboardItem item) {
			this.checkEnabled();
		}
	}
	
	private JPanel getButtonsPane(){
		if (this.buttonsPanel == null){
	//		 OK and CANCEL Buttons
			this.buttonsPanel = new JPanel();
			this.buttonsPanel.setLayout(new FlowLayout());
			
			JButton okButton = getOKButton();
			this.getRootPane().setDefaultButton(okButton);
	
			String cancelButtonLabel = Workbench.CONFIG.getProperty(BUTTONTEXT_CANCEL);
			if (cancelButtonLabel == null) {
				cancelButtonLabel = "Cancel";
			}
			JButton cancelButton = new JButton(cancelButtonLabel);
			
			String iconFile = Workbench.CONFIG.getProperty(BUTTONICON_CANCEL);
			if (iconFile != null) {
				URL imageURL = Util.getGlobalResourceURL(iconFile);
				cancelButton.setIcon(new ImageIcon(imageURL));
			}
			
			cancelButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					ParamsWindow.this.wasCancelled = true;
					ParamsWindow.this.dispose();
					receiver.cancel();
	
				}
			});
			
			//Detect the close button
			this.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					ParamsWindow.this.wasCancelled = true;
				}
			});
	
			this.buttonsPanel.add(okButton);
			this.buttonsPanel.add(cancelButton);
			
			if (this.operation.getHelp() != null && !this.operation.getHelp().trim().equals("")) {
				if (CoreUtils.isValidURL(this.operation.getHelp())) {
					JButton helpButton = new JButton(new AbstractAction("Help") {
						private static final long serialVersionUID = 1L;

						public void actionPerformed(ActionEvent e) {
							try {
								CoreUtils.openURL(ParamsWindow.this.operation.getHelp());
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(
									Workbench.getInstance().getMainFrame(), 
									"The help URL(" + ParamsWindow.this.operation.getHelp() + ") couldn't be opened.", 
									"Help Unavailable", 
									JOptionPane.ERROR_MESSAGE
								);
							}
						}
					});
					this.buttonsPanel.add(helpButton);
				} else {
					JButton helpButton = new JButton("Help");
					HelpBroker helpBroker = Core.getInstance().getHelpBroker();
					if (helpBroker != null) {
						helpBroker.enableHelpOnButton(helpButton, this.operation.getHelp(), helpBroker.getHelpSet());
						helpBroker.enableHelpKey(this.getContentPane(), this.operation.getHelp(), helpBroker.getHelpSet());
						this.buttonsPanel.add(helpButton);
					}
				}
			}
		}
		return this.buttonsPanel;
	}
	
	private void initialize(){
		this.setLayout(new BorderLayout());

		this.add(getDescriptionPane(), BorderLayout.NORTH);
		
		inputComponents = createInputComponents();
		JScrollPane scroll = new JScrollPane(inputComponents);
		/*scroll.getViewport().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent arg0) {
		
				//_pack();
				
			}
			
		});*/
		
		this.add(scroll, BorderLayout.CENTER);
		this.add(getButtonsPane(), BorderLayout.SOUTH);
	}
	
	private JButton okButton;
	private JButton getOKButton(){
		if (okButton == null){
			String okButtonLabel = Workbench.CONFIG.getProperty(BUTTONTEXT_OK);
			if(okButtonLabel == null) {
				okButtonLabel = "OK";
			}
			this.okButton = new JButtonOk(okButtonLabel);

			String iconFile = Workbench.CONFIG.getProperty(BUTTONICON_OK);
			if (iconFile != null) {
				URL imageURL = Util.getGlobalResourceURL(iconFile);
				this.okButton.setIcon(new ImageIcon(imageURL));
			}
			
			((JButtonOk) this.okButton).checkEnabled();
//			okButton=  new JButton("OK");
//			okButton.addActionListener(new ActionListener(){
//				public void actionPerformed(ActionEvent e) {
//					receiver.paramsIntroduced(ParamsWindow.this.getParamSpec());
//					okButton.setEnabled(false);
//					/*if(checkParams()){
//						ParamsWindow.this.dispose();
//					}*/
//				}
//			});
		}
		return okButton;
	}
	
	//Needed to detect the ESCAPE KEY to cancel and close
	protected JRootPane createRootPane() {
		  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		  JRootPane rootPane = new JRootPane();
		  rootPane.registerKeyboardAction(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ParamsWindow.this.wasCancelled=true;
				ParamsWindow.this.dispose();
			}

		  }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		  return rootPane;
	}

	private ParamSpec[] getParamSpec(){
		if (this.wasCancelled)
			return null;
		ParamSpec[] ret = new ParamSpec[this.providers.size()];

		int i=0;
		for (ParamProvider provider : this.providers){
			ret[i++] = provider.getParamSpec();
		}
		
		return ret;
	}
	
	private void centerOnOwner() {
         Rectangle rectOwner;
         Rectangle rectDialog;
         rectOwner = this.getParent().getBounds();
         rectDialog = this.getBounds();
         setLocation((rectOwner.x + (rectOwner.width / 2)) - (rectDialog.width / 2), (rectOwner.y + (rectOwner.height / 2)) - (rectDialog.height / 2));
	 }
	 
	private boolean finished = false;
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#finish()
	 */
	public synchronized void finish() {
		if (!this.finished) {
			this.finished = true;
			this.setVisible(false);
			if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("finishing params window");
			for (ParamProvider p : this.providers){
				if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("finishing param provider of class "+p.getClass());	
				p.finish();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#init(es.uvigo.ei.aibench.workbench.ParamsReceiver)
	 */
	public void init(ParamsReceiver receiver, OperationDefinition<?> operation) {
		this.receiver = receiver;
		this.operation = operation;
		this.setTitle(operation.getName());
		//this.operationObject=operationObject;
		this.setModal(true);
		
		initialize();
	
		_pack();
		
		this.setSize(new Dimension(this.getWidth()+20, this.getHeight()+40));
		this.centerOnOwner();
		
		if (operation.getIncomingArgumentTypes().isEmpty()) {
			this.getOKButton().doClick();
		} else {
			this.setVisible(true);
		}
		
	}
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#onValidationError(java.lang.Throwable)
	 */
	public void onValidationError(Throwable t) {
		JOptionPane.showMessageDialog(this, t.getMessage(), "Input not valid", JOptionPane.WARNING_MESSAGE);
		okButton.setEnabled(true);
	}
	
	private boolean autoresize=true;
	
	private void _pack(){
		if (!autoresize) return;
		
		
		this.pack();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// too much height
		if (this.getHeight()>(ge.getMaximumWindowBounds().height-100)){
			autoresize=false;	
			this.setSize(this.getWidth()+50, ge.getMaximumWindowBounds().height-100);
		}
		
	
		if (this.getWidth()>(ge.getMaximumWindowBounds().width-100)){
			autoresize=false;
			this.setSize(ge.getMaximumWindowBounds().width-100, this.getHeight()+50);
		}
	}
}
