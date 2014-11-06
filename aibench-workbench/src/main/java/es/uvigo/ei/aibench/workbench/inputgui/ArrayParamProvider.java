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
 * ArrayParamProvider.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 10/05/2007
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.FileDrop;

public class ArrayParamProvider extends AbstractParamProvider implements Observer {
	/**
	 * 
	 */
	private static final int PREFERRED_FIELD_WIDTH = 200;
	final static ImageIcon ICON_ADD = new ImageIcon(ArrayParamProvider.class.getResource("images/add.png"));
	final static ImageIcon ICON_REMOVE = new ImageIcon(ArrayParamProvider.class.getResource("images/remove.png"));
	
	final static ImageIcon ICON_ADD_ONE = new ImageIcon(ArrayParamProvider.class.getResource("images/addOne.png"));
	final static ImageIcon ICON_ADD_ALL = new ImageIcon(ArrayParamProvider.class.getResource("images/addAll.png"));
	final static ImageIcon ICON_REMOVE_ONE = new ImageIcon(ArrayParamProvider.class.getResource("images/removeOne.png"));
	final static ImageIcon ICON_REMOVE_ALL = new ImageIcon(ArrayParamProvider.class.getResource("images/removeAll.png"));
	
	/**
	 * 
	 */
	private static final String CARD_CLIPBOARD = "clipboard";
	/**
	 * 
	 */
	private static final String CARD_NEW = "new";

	private class ParamSpecWrapper {
		private final String name;
		private final ParamSpec paramSpec;
		
		/**
		 * @param paramSpec
		 */
		public ParamSpecWrapper(ParamSpec paramSpec) {
			this.paramSpec = paramSpec;
			this.name = (paramSpec.getValue() == null)?"null":paramSpec.getValue().toString();
		}

		public ParamSpecWrapper(ParamSpec paramProvider, String name) {
			this.paramSpec = paramProvider;
			this.name = name;
		}
		
		/**
		 * @return the paramSpec
		 */
		public ParamSpec getParamSpec() {
			return this.paramSpec;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	private DefaultListModel<Object> model = new DefaultListModel<Object>();
	private final JPanel panel = new JPanel(new BorderLayout());
	private final JPanel newArrayPanel = new JPanel(new BorderLayout());
	private final JPanel arraySelector = new JPanel();
	private final JPanel cardPanel = new JPanel();

	private final CardLayout cardLayout = new CardLayout();

	private ArrayParamListener listener = null;
	
	private boolean componentInitialized = false;
	private boolean showingAll = false;
	boolean createNew = false;
	private String currentView = ArrayParamProvider.CARD_CLIPBOARD;
	private final ClipboardParamProvider selectArray;

	int selectHeight;
	int newHeight;
	int preferredWidth;

	public ArrayParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p, clazz, operationObject);

		this.selectArray = new ClipboardParamProvider(receiver, p, clazz, operationObject);
		this.cardPanel.setLayout(this.cardLayout);
		this.selectArray.addObserver(this);
	}

	private ParamProvider newArrayParamProvider;
	private final JRadioButton radioCreate = new JRadioButton("create array");
	private final JRadioButton radioFromClipboard = new JRadioButton("select from clipboard");

	private void initNewArrayComponent() {
		this.newArrayPanel.removeAll();
		
		final JList<Object> itemsList = new JList<Object>(this.model);

		JPanel upper = new JPanel(new BorderLayout()); // here will be placed
														// the component and the
														// add button

		final Class<?> arrayType = clazz.getComponentType();
		assert arrayType != null;

		JButton removeButton = new JButton(ArrayParamProvider.ICON_REMOVE_ONE);
		removeButton.setToolTipText("Remove selected items from list");
		removeButton.addActionListener(new ActionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				for (Object value : itemsList.getSelectedValuesList()) {
					model.removeElement(value);
				}

				ArrayParamProvider.this.actionPerformed(e);
			}
		});
		
		JButton removeAllButton = new JButton(ArrayParamProvider.ICON_REMOVE_ALL);
		removeAllButton.setToolTipText("Clear list");
		removeAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.removeAllElements();
				ArrayParamProvider.this.actionPerformed(e);
			}
		});

		if (arrayType.equals(File.class)) {
			JButton selectMultipleFiles = new JButton("select files...", Common.ICON_FILE_OPEN);
			selectMultipleFiles.setToolTipText("Browse the local filesystem to select multiple files");
			selectMultipleFiles.addActionListener(new ActionListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				public void actionPerformed(ActionEvent e) {
					FileParamProvider.configureFileChooser(port, Common.MULTIPLE_FILE_CHOOSER);
					
					int option = Common.MULTIPLE_FILE_CHOOSER.showDialog(Workbench.getInstance().getMainFrame(), "Select Files");

					if (option == JFileChooser.APPROVE_OPTION) {
						File[] selected = Common.MULTIPLE_FILE_CHOOSER.getSelectedFiles();
//						FileParamProvider.FILE_CHOOSER.setCurrentDirectory(FILE_CHOOSER.getCurrentDirectory());
						for (File file : selected) {
							model.addElement(new ParamSpecWrapper(new ParamSpec(
								port.name(),
								arrayType, 
								file.getAbsolutePath(),
								ParamSource.STRING_CONSTRUCTOR)
							));
						}
					}
					
					ArrayParamProvider.this.actionPerformed(e);
				}
			});

			final JPanel buttons = new JPanel(new GridLayout(1, 2, 0, 0));
			buttons.add(removeButton);
			buttons.add(removeAllButton);
			
			upper.add(selectMultipleFiles, BorderLayout.NORTH);
			upper.add(buttons, BorderLayout.SOUTH);
		} else {
			newArrayParamProvider = ParamProviderFactory.createParamProvider(
				this.getReceiver(), this.port, arrayType, operationObject
			);
			
			final boolean clipboardItems = (this.newArrayParamProvider instanceof ClipboardParamProvider);
			
			final JPanel buttons = new JPanel(new GridLayout(1, (clipboardItems)?4:3, 0, 0));

			final JButton addButton = new JButton(ArrayParamProvider.ICON_ADD_ONE);
			final ActionListener addActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (newArrayParamProvider.isValidValue()) {
						if (clipboardItems) {
							ParamSpec param = newArrayParamProvider.getParamSpec();
							
							model.addElement(new ParamSpecWrapper(
								param,
								(param.getValue() == null)?"<NULL>":param.getValue().toString()
							));
						} else {
							model.addElement(new ParamSpecWrapper(newArrayParamProvider.getParamSpec()));					
						}
						
					} else {
						JOptionPane.showMessageDialog(
							ArrayParamProvider.this.getComponent(), 
							"Invalid value",
							"Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
					
					ArrayParamProvider.this.actionPerformed(e);
				}
			};
			
			addButton.setToolTipText("Add item to the list");
			addButton.addActionListener(addActionListener);
			
			
			buttons.add(addButton);
			
			
			if (clipboardItems) {
				JButton addAllButton = new JButton(ArrayParamProvider.ICON_ADD_ALL);
				addAllButton.setToolTipText("Add available items to the list");
				addAllButton.addActionListener(new ActionListener() {
					/* (non-Javadoc)
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					public void actionPerformed(ActionEvent e) {
						for (ParamSpec spec:((ClipboardParamProvider) newArrayParamProvider).listParamSpecs()) {
							model.addElement(new ParamSpecWrapper(spec, spec.getValue().toString()));
						}
						
						ArrayParamProvider.this.actionPerformed(e);
					}
				});

				buttons.add(addAllButton);
			}
			
			buttons.add(removeButton);
			buttons.add(removeAllButton);
			upper.add(newArrayParamProvider.getComponent(),	BorderLayout.NORTH);
			upper.add(buttons, BorderLayout.SOUTH);
			
			/*JPanel buttons = new JPanel(new GridLayout(1, 2, 0, 0));
			newArrayParamProvider = ParamProviderFactory.createParamProvider(
				this.getReceiver(), this.port, arrayType, operationObject
			);

			JButton addButton = new JButton(ArrayParamProvider.ICON_ADD);
			removeButton.setToolTipText("Add item to the list");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (newArrayParamProvider.isValidValue()) {
						if (newArrayParamProvider instanceof ClipboardParamProvider) {
							model.addElement(new ParamSpecWrapper(
									newArrayParamProvider.getParamSpec(),
									((ClipboardParamProvider) newArrayParamProvider).getCurrentString()
							));
						} else {
							model.addElement(new ParamSpecWrapper(newArrayParamProvider.getParamSpec()));					
						}						
					} else {
						JOptionPane.showMessageDialog(
							ArrayParamProvider.this.getComponent(), 
							"Invalid value",
							"Error",
							JOptionPane.ERROR_MESSAGE
						);
					}
					
					
					ArrayParamProvider.this.actionPerformed(e);
				}
			});

			buttons.add(addButton);
			buttons.add(removeButton);
			upper.add(newArrayParamProvider.getComponent(),	BorderLayout.CENTER);
			upper.add(buttons, BorderLayout.EAST);*/
		}
		this.newArrayPanel.add(upper, BorderLayout.NORTH);
		if (arrayType.equals(File.class)) {
		  new  FileDrop( itemsList, new FileDrop.Listener(){   
			  public void  filesDropped( java.io.File[] files ){   
	    	  if (files.length > 0){
	    		  for (File file : files) {
						model.addElement(new ParamSpecWrapper(new ParamSpec(
							port.name(),
							arrayType, 
							file.getAbsolutePath(),
							ParamSource.STRING_CONSTRUCTOR)
						));
					}
	    		  	ArrayParamProvider.this.setChanged();
	    			ArrayParamProvider.this.notifyObservers();
				}
	    	  
	          }   
	      }); 
		}
		this.newArrayPanel.add(new JScrollPane(itemsList), BorderLayout.CENTER);
	}

	private void initArraySelector() {
		ButtonGroup group = new ButtonGroup();
		this.arraySelector.removeAll();

		radioCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (radioCreate.isSelected()) {
					createNew = true;
					ArrayParamProvider.this.changeCurrentView(ArrayParamProvider.CARD_NEW, false);
					ArrayParamProvider.this.actionPerformed(e);
				}
			}
		});
		radioFromClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (radioFromClipboard.isSelected()) {
					createNew = false;
					ArrayParamProvider.this.changeCurrentView(ArrayParamProvider.CARD_CLIPBOARD, false);
					ArrayParamProvider.this.actionPerformed(e);
				}
			}
		});
		group.add(radioFromClipboard);
		radioFromClipboard.setSelected(true);
		group.add(radioCreate);
		this.arraySelector.add(radioFromClipboard);
		this.arraySelector.add(radioCreate);
	}

	public JComponent getComponent() {
		if (!this.componentInitialized) {
			synchronized (this.panel) {
				if (!this.componentInitialized) {
					this.initComponent();
					this.componentInitialized = true;
				}
			}
		}
		return this.panel;
	}

	// Evalueates if the array can be retrieved from the clipboard.
	//TODO: Is there a better way to do this?
	protected boolean classHasClipboardItems() {
		return this.selectArray.countClipboardItems() > 0;
	}
	
	// Evalueates if the array can be created with operations.
	//TODO: There is a better way to do this.
	protected boolean classHasOperations() {
		return this.selectArray.classHasOperations(this.clazz);
	}
	
	private synchronized void reloadComponent() {
		boolean classHasClipboardItems = this.classHasClipboardItems();
		boolean classHasOperations = this.classHasOperations();
		if (this.showingAll) {
			if (!classHasClipboardItems && !classHasOperations) {
				this.arraySelector.setVisible(false);
				this.changeCurrentView(ArrayParamProvider.CARD_NEW, true);
				this.showingAll = false;
				this.createNew = true;
				this.panel.repaint();
			}
		} else {
			if (classHasClipboardItems || classHasOperations) {
				this.arraySelector.setVisible(true);
				this.showingAll = true;
				this.panel.repaint();
			}
		}
	}
	
	private void changeCurrentView(String constraint, boolean updateCheckboxes) {
		if ((constraint.equals(ArrayParamProvider.CARD_NEW) || constraint.equals(ArrayParamProvider.CARD_CLIPBOARD)) 
			&& !this.currentView.equals(constraint)) {
			this.currentView = constraint;
			this.cardLayout.show(this.cardPanel, this.currentView);
			if (constraint.equals(ArrayParamProvider.CARD_NEW)) {
				cardPanel.setPreferredSize(new Dimension(this.preferredWidth, this.newHeight));
				this.createNew = true;
				if (updateCheckboxes) this.radioCreate.setSelected(true);
			} else if (constraint.equals(ArrayParamProvider.CARD_CLIPBOARD)) {
				cardPanel.setPreferredSize(new Dimension(this.preferredWidth, this.selectHeight));
				this.createNew = false;
				if (updateCheckboxes) this.radioFromClipboard.setSelected(true);
			}
		}
		
	}
	
	private void initComponent() {
		this.panel.removeAll();
		this.initNewArrayComponent();
		this.initArraySelector();

		JPanel selectArrayPanel = new JPanel();
		selectArrayPanel.setLayout(new BorderLayout());
		selectArrayPanel.add(this.selectArray.getComponent(), BorderLayout.NORTH);
		this.cardPanel.add(ArrayParamProvider.CARD_CLIPBOARD, selectArrayPanel);

		this.newHeight = (int) this.newArrayPanel.getPreferredSize().getHeight();
		this.cardPanel.add(ArrayParamProvider.CARD_NEW, this.newArrayPanel);


		this.panel.add(this.arraySelector, BorderLayout.NORTH);
		this.panel.add(this.cardPanel, BorderLayout.CENTER);

		this.selectHeight = (int) selectArrayPanel.getPreferredSize().getHeight();
		this.preferredWidth = (int) selectArrayPanel.getPreferredSize().getWidth();
		if (this.preferredWidth < ArrayParamProvider.PREFERRED_FIELD_WIDTH) this.preferredWidth = ArrayParamProvider.PREFERRED_FIELD_WIDTH;
		this.cardPanel.setMinimumSize(new Dimension(ArrayParamProvider.PREFERRED_FIELD_WIDTH, this.selectArray.getCurrentComponent().getMinimumSize().height));
		
		if (this.listener == null) {
			this.listener = new ArrayParamListener();
			Core.getInstance().getClipboard().addClipboardListener(this.listener);
		}

		this.showingAll = true;
		if (this.classHasClipboardItems()) {
			this.changeCurrentView(ArrayParamProvider.CARD_CLIPBOARD, true);
			cardPanel.setPreferredSize(new Dimension(this.preferredWidth, this.selectHeight));
		} else {
			this.changeCurrentView(ArrayParamProvider.CARD_NEW, true);
		}
		this.reloadComponent();
	}

	public synchronized ParamSpec getParamSpec() {
		if (this.createNew) {
			final ArrayList<ParamSpec> specs = new ArrayList<ParamSpec>();

			final Enumeration<?> e = model.elements();
			while (e.hasMoreElements()) {
				specs.add(((ParamSpecWrapper) e.nextElement()).getParamSpec());
			}
//			ParamSpec[] specs = new ParamSpec[specsArrayList.size()];
//			int i = 0;
//			for (ParamSpec spec : specsArrayList) {
//				specs[i++] = spec;
//			}

			return new ParamSpec(this.port.name(), clazz, specs.toArray(new ParamSpec[specs.size()]));
		} else {
			return selectArray.getParamSpec();
		}
	}
	
	private final class ArrayParamListener implements ClipboardListener {
		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementAdded(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
		 */
		public void elementAdded(ClipboardItem item) {
			ArrayParamProvider.this.reloadComponent();
		}
		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementRemoved(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
		 */
		public void elementRemoved(ClipboardItem item) {
			ArrayParamProvider.this.reloadComponent();
		}
	}

	public void finish() {
		if (this.selectArray != null) {
			this.selectArray.finish();
		}
		if (this.newArrayParamProvider != null) {
			this.newArrayParamProvider.finish();
		}
		if (this.listener != null) {
			Core.getInstance().getClipboard().removeClipboardListener(this.listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public boolean isValidValue() {
		if (!this.port.allowNull() && this.currentView == ArrayParamProvider.CARD_CLIPBOARD) {
			return this.selectArray.isValidValue();
		}
		if (!this.port.allowNull() && this.currentView == ArrayParamProvider.CARD_NEW) {
			return !this.model.isEmpty();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		this.setChanged();
		this.notifyObservers();
	}
}
