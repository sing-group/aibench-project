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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.core.operation.annotation.ProgressProperty;

/**
 * A class that monitors the progress of an operation based on the information
 * obtained from {@code Progress} and {@code ProgressProperty} annotations.
 * 
 * @author Hugo López-Fernández
 * @author Daniel Glez-Peña
 * 
 * @see Progress
 * @see ProgressProperty
 */
public class MonitorizeDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public static final String WORKING_PROGRESS_ICON = "progress.workingicon";

	private static final Logger LOGGER = Logger.getLogger(MonitorizeDialog.class);
	private static final int REFRESH_MILLIS = 10;

	private Object bean;
	private Object operationID;
	private OperationDefinition<?> definition;
	private boolean finish = false;
	private PropertyDescriptor[] descriptors;
	private HashMap<PropertyDescriptor, RenderComponent> mapping = new HashMap<PropertyDescriptor, RenderComponent>();
	private Timer t = null;
	private JPanel contentPane;

	public MonitorizeDialog(JFrame parent) {
		this(parent, null, null, null);
	}

	public MonitorizeDialog(JFrame parent, Object bean, Object operationID, OperationDefinition<?> definition) {
		super(parent);

		this.bean = bean;
		this.operationID = operationID;
		this.definition = definition;

		this.initialize();
	}

	private void initialize(){
		this.setModal(getIsModal());
		this.setTitle(getDialogTitle());
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setContentPane(getDialogContentPane());
		this.pack();
		this.setPreferredSize();
		this.centerOnOwner();
		this.setResizable(false);
	}

	private void setPreferredSize() {
		int preferredWidth = getPreferredWidth();
		int preferredHeight = getPreferredHeight();
		if(preferredHeight != Progress.DEFAULT_PREFERRED_SIZE &&
				preferredWidth != Progress.DEFAULT_PREFERRED_SIZE) {
			this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		}
	}

	private int getPreferredWidth() {
		if (getProgress() != null) {
			return getProgress().preferredWidth();
		} else {
			return Progress.DEFAULT_PREFERRED_SIZE;
		}
	}

	private int getPreferredHeight() {
		if (getProgress() != null) {
			return getProgress().preferredHeight();
		} else {
			return Progress.DEFAULT_PREFERRED_SIZE;
		}
	}

	private Container getDialogContentPane() {
		contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(Color.WHITE);
		contentPane.add(getWorkingPanel(), BorderLayout.NORTH);
		addBeanProgressPropertiesPanel();
		addCancelButtonPanel();

		return contentPane;
	}

	private JPanel getWorkingPanel() {
		JPanel workingPanel = new JPanel(new GridBagLayout());
		workingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		workingPanel.setBackground(Color.WHITE);
		JLabel working = new JLabel(getWorkingLabelText());
		working.setOpaque(false);
		working.setBackground(Color.WHITE);
		working.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		working.setIcon(getProgressIcon());
		workingPanel.add(working);

		return workingPanel;
	}

	private Icon getProgressIcon() {
		String iconFile = Workbench.CONFIG.getProperty(WORKING_PROGRESS_ICON);
		if (iconFile != null) {
			URL imageURL = Util.getGlobalResourceURL(iconFile);
			return new ImageIcon(imageURL);
		}
		return new ImageIcon(getClass().getResource("/images/longtask.gif"));
	}

	private boolean getIsModal() {
		return getProgress() == null ? Progress.DEFAULT_DIALOG_MODAL : getProgress().modal();
	}

	private String getDialogTitle() {
		return getProgress() == null ? Progress.DEFAULT_DIALOG_TITLE : getProgress().progressDialogTitle();
	}

	private Progress getProgress() {
		if(this.definition.getMonitorBeanMethod() != null) {
			return this.definition.getMonitorBeanMethod().getAnnotation(Progress.class);
		} else {
			return null;
		}
	}

	private String getWorkingLabelText() {
		return getProgress() == null ? Progress.DEFAULT_WORKING_LABEL : getProgress().workingLabel();
	}

	interface RenderComponent {
		public abstract void updateValue(Object value);
	}

	private class JProgressBarRenderer extends JProgressBar implements RenderComponent {
		private static final long serialVersionUID = 1L;

		JProgressBarRenderer(boolean stringPainted) {
			this.setMaximum(100);
			this.setValue(0);
			this.setStringPainted(stringPainted);
		}

		public void updateValue(Object value) {
			if (value instanceof Float) {
				if (((Float) value).floatValue() < 0) {
					this.setValue(0);
					this.setIndeterminate(true);
				} else {
					this.setIndeterminate(false);
					this.setValue((int) (((Float) value).floatValue() * (float) 100));

				}
			}
		}
	}

	private class JLabelRenderer extends JLabel implements RenderComponent {
		private static final long serialVersionUID = 1L;

		JLabelRenderer() {
			super("");
		}

		public void updateValue(Object value) {
			if (value == null)
				return;
			this.setText(value.toString());
		}
	}

	private void addBeanProgressPropertiesPanel() {
		if (this.bean == null) {
			return;
		}

		try {
			JPanel beanInfoPanel = new JPanel();
			beanInfoPanel.setBackground(Color.WHITE);
			beanInfoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			beanInfoPanel.setLayout(new GridLayout(2, 2));

			GridBagLayout newLayout = new GridBagLayout();
			beanInfoPanel.setLayout(newLayout);
			
			BeanInfo info = Introspector.getBeanInfo(bean.getClass());
			this.descriptors = getPropertyDescriptors(info);

			int i = 0;
			for (PropertyDescriptor descriptor : this.descriptors) {
				GridBagConstraints c = new GridBagConstraints();
				i++;

				JLabel label = new JLabel(getDescriptorName(descriptor));
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = i;
				c.weightx = 0;
				c.anchor = GridBagConstraints.WEST;
				c.insets = new Insets(5, 5, 5, 5);
				newLayout.setConstraints(label, c);
				beanInfoPanel.add(label);


				JComponent component =null;
				if (descriptor.getPropertyType().equals(float.class) || descriptor.getPropertyType().equals(Float.class) ){
					component = new JProgressBarRenderer(getDescriptorStringPainted(descriptor));
				}else{
					component = new JLabelRenderer();
				}
				mapping.put(descriptor, (RenderComponent) component);
				
				c.gridx=1;
				c.weightx=1.0;
				
				newLayout.setConstraints(component, c);
				beanInfoPanel.add(component);
			}

			contentPane.add(beanInfoPanel, BorderLayout.CENTER);
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		/**
		 * Thread to monitorize (best with one cpu due to high priority) 
		 */
		Thread dummy = new Thread() {
			public void run() {
				while (!finish) {
					try {
						sleep(REFRESH_MILLIS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					update();
				}
			}
		};
		dummy.setPriority(Thread.MAX_PRIORITY); /** more priority than the operation process **/
		dummy.start();
		
		// Timer to monitorize (more professional)
		/*t = new Timer(REFRESH_MILLIS, new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				update();

			}
		}
		);
		t.setCoalesce(true);
		t.start();*/
	}

	/**
	 * Returns a list of valid {@code PropertyDescriptor} for {@code info}
	 * object. A descriptor is valid if its name is different from {@code class}
	 * and it has a read method associated.
	 * 
	 * Please, note that descriptors with read method but without write method
	 * are considered valid. In this cases, a warning will be shown in the log
	 * to warn developers.
	 * 
	 * @param info the {@code BeanInfo}
	 * @return an array of valid descriptors
	 */
	protected PropertyDescriptor[] getPropertyDescriptors(BeanInfo info) {
		List<PropertyDescriptor> descriptors = new LinkedList<>();

		for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
			if (descriptor.getName().equals("class")) {
				continue;
			}

			if (descriptor.getReadMethod() != null) {
				ProgressProperty progressProperty = descriptor.getReadMethod().getAnnotation(ProgressProperty.class);
				if (progressProperty == null || !progressProperty.ignore()) {
					descriptors.add(descriptor);

					if (descriptor.getWriteMethod() == null) {
						LOGGER.warn("Warning: progress bean property " + descriptor.getName()
								+ " does not have a write method associated so it probably can't be updated");
					}
				}
			} else {
				LOGGER.warn("Ignoring progress bean property " + descriptor.getName()
						+ " since it does not have a read method associated");
			}
		}

		return orderDescriptors(descriptors);
	}

	private PropertyDescriptor[] orderDescriptors(List<PropertyDescriptor> propertyDescriptors) {
		if(getProgress() == null) {
			return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
		}

		Map<Integer, List<PropertyDescriptor>> descriptorsOrder = new HashMap<>();

		for(PropertyDescriptor descriptor : propertyDescriptors) {
				ProgressProperty descriptorProperty = descriptor.getReadMethod().getAnnotation(ProgressProperty.class);
				int order;
				if (descriptorProperty == null) {
					order = 0;
				} else {
					order = descriptorProperty.order();
				}

				if(!descriptorsOrder.containsKey(order)) {
					descriptorsOrder.put(order, new LinkedList<PropertyDescriptor>());
				}
				descriptorsOrder.get(order).add(descriptor);
		}

		List<Integer> order = new LinkedList<>(descriptorsOrder.keySet());
		Collections.sort(order);

		List<PropertyDescriptor> orderedDescriptors = new LinkedList<>();
		for (Integer o : order) {
			orderedDescriptors.addAll(descriptorsOrder.get(o));
			if(descriptorsOrder.get(o).size() > 1) {
				LOGGER.warn("Several progress properties at the same position (" + o +"). Thay may appear in a random order.");
			}
		}

		return orderedDescriptors.toArray(new PropertyDescriptor[orderedDescriptors.size()]);
	}

	private String getDescriptorName(PropertyDescriptor descriptor) {
		ProgressProperty descriptorProperty = descriptor.getReadMethod().getAnnotation(ProgressProperty.class);
		if (descriptorProperty != null) {
			if (descriptorProperty.label().equals(ProgressProperty.DEFAULT_LABEL)) {
				return descriptor.getName();
			} else {
				return descriptorProperty.label();
			}
		} else {
			return descriptor.getName();
		}
	}

	private boolean getDescriptorStringPainted(PropertyDescriptor descriptor) {
		ProgressProperty descriptorProperty = descriptor.getReadMethod().getAnnotation(ProgressProperty.class);
		return descriptorProperty.showProgressBarLabel();
	}
	
	private void addCancelButtonPanel() {
		if (definition.getCancelMethod() != null) {
			Cancel cancel = definition.getCancelMethod().getAnnotation(Cancel.class);

			final JButton stopButton = new JButton(cancel.cancelButtonLabel());
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Core.getInstance().cancelOperation(operationID);
				}
			});

			contentPane.add(new JPanel() {
				private static final long serialVersionUID = 1L;

				{
					this.setBackground(Color.WHITE);
					this.add(stopButton);
				}
			}, BorderLayout.SOUTH);
		}
	}

	private void update() {
		for (PropertyDescriptor descriptor : this.descriptors) {
			try {
				Method readMethod = descriptor.getReadMethod();
				if (readMethod != null && mapping.get(descriptor) != null) {
					mapping.get(descriptor).updateValue(readMethod.invoke(bean, new Object[] {}));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		this.pack();
	}

	private void centerOnOwner() {
		Rectangle rectOwner;
		Rectangle rectDialog;
		rectOwner = this.getParent().getBounds();
		rectDialog = this.getBounds();
		setLocation(
			(rectOwner.x + (rectOwner.width / 2)) - (rectDialog.width / 2),
			(rectOwner.y + (rectOwner.height / 2)) - (rectDialog.height / 2)
		);
	}

	public void setVisible(boolean flag) {
		if (flag == false && t != null) {
			t.stop();

		}
		super.setVisible(flag);
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}
}
