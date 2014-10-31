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
 * MonitorizeDialog.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;

/**
 * @author Daniel Glez-Peña 23-sep-2006
 *
 */
public class MonitorizeDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int REFRESH_MILLIS = 10;

	private Object bean;
	private Object operationID;

	private OperationDefinition<?> definition;
	
	private boolean finish = false;

	private PropertyDescriptor[] descriptors;

	public MonitorizeDialog(JFrame parent) {
		this(parent, null, null, null);
	}
	
	public MonitorizeDialog(JFrame parent, Object bean, Object operationID, OperationDefinition<?> definition){
		super(parent);
		this.bean = bean;
		this.operationID = operationID;
		this.definition = definition;
		initialize();
		//this.setModal(true);
		this.setTitle("Progress...");
		this.setSize(new Dimension(300, 200));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.centerOnOwner();
		
		this.pack();
		
		this.setResizable(false);
		
	}

	Timer t = null;
	@SuppressWarnings("serial")
	private void initialize(){
		
		this.setLayout(new BorderLayout());

		JLabel working = new JLabel("Working");
		working.setOpaque(true);
		working.setBackground(Color.WHITE);
		working.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		working.setPreferredSize(new Dimension(200,50));
		working.setIcon(new ImageIcon(getClass().getResource("/images/longtask.gif")));
		this.add(working, BorderLayout.NORTH);
		if (this.bean !=null) putBeanInfo(bean);

		
		if(definition.getCancelMethod()!=null){
		
			final JButton stopButton = new JButton("STOP");
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Core.getInstance().cancelOperation(operationID);
	
				}
			});
			
	
			this.add(new JPanel(){{this.add(stopButton);}}, BorderLayout.SOUTH);
		}
	}

	interface  RenderComponent {
		public abstract void updateValue(Object value);
	}

	private class JProgressBarRenderer extends JProgressBar implements RenderComponent {
		private static final long serialVersionUID = 1L;
		JProgressBarRenderer() {
			this.setMaximum(100);
			this.setValue(0);
		}
		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.workbench.MonitorizeDialog.RenderComponent#updateValue(java.lang.Object)
		 */
		public void updateValue(Object value) {
			if (value instanceof Float){
				if (((Float) value).floatValue()<0){
					this.setValue(0);
					this.setIndeterminate(true);
				}else{
					this.setIndeterminate(false);
					this.setValue((int) (((Float) value).floatValue() * (float) 100));
					
				}
			}
		}
	}
	private class JLabelRenderer extends JLabel implements RenderComponent{
		private static final long serialVersionUID = 1L;
		JLabelRenderer(){
			super("");
		}
		public void updateValue(Object value) {
			if (value==null) return;
			this.setText(value.toString());

		}

	}

	private HashMap<PropertyDescriptor, RenderComponent> mapping = new HashMap<PropertyDescriptor, RenderComponent>();


	private void putBeanInfo(Object bean){
		try {
			JPanel beanInfoPanel = new JPanel();
			beanInfoPanel.setLayout(new GridLayout(2,2));
			
			GridBagLayout newLayout = new GridBagLayout();
			beanInfoPanel.setLayout(newLayout);
			
			
			BeanInfo info = Introspector.getBeanInfo(bean.getClass());
			this.descriptors = info.getPropertyDescriptors();
			int descriptorsWithRead = 0;
			for (PropertyDescriptor desc: descriptors){
				if (desc.getReadMethod()!=null && !desc.getName().equals("class")) descriptorsWithRead ++;
			}
			
			int i = 0;
			for (PropertyDescriptor descriptor: this.descriptors ){
				GridBagConstraints c = new GridBagConstraints();
				if (descriptor.getReadMethod()!=null  && !descriptor.getName().equals("class")){
					i++;

					JLabel label = new JLabel(descriptor.getName());
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
						component = new JProgressBarRenderer();
					}else{
						component = new JLabelRenderer();
					}
					mapping.put(descriptor, (RenderComponent) component);
					
					c.gridx=1;
					c.weightx=1.0;
					
					newLayout.setConstraints(component, c);
					beanInfoPanel.add(component);
				}
			}
			
			//beanInfoPanel.setLayout(new GridLayout(i, 2));
			
			this.add(beanInfoPanel, BorderLayout.CENTER);
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		//Thread to monitorize (best with one cpu due to high priority)
		Thread dummy = new Thread(){
			public void run(){
				while(!finish){
				try {
					sleep(REFRESH_MILLIS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				update();
				}
			}
		};
		dummy.setPriority(Thread.MAX_PRIORITY); // more priority than the operation process
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
	private void update(){

		for (PropertyDescriptor descriptor: this.descriptors){
			try {
				
				Method readMethod = descriptor.getReadMethod();
				if (readMethod!=null && !descriptor.getName().equals("class")){
					if (mapping.get(descriptor)!=null){
						mapping.get(descriptor).updateValue(readMethod.invoke(bean,new Object[]{}));
					}

				}

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
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
         setLocation((rectOwner.x + (rectOwner.width / 2)) - (rectDialog.width / 2), (rectOwner.y + (rectOwner.height / 2)) - (rectDialog.height / 2));
	 }

	public void setVisible(boolean flag){
		if (flag == false && t!=null){
			t.stop();
			
		}
		super.setVisible(flag);
	}
	/**
	 * @param finish the finish to set
	 */
	public void setFinish(boolean finish) {
		this.finish = finish;
	}

}
