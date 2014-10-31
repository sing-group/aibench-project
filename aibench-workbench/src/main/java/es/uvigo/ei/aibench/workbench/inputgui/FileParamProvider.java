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
 * FileParamProvider.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 10/05/2007
 */
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.FileDrop;

public class FileParamProvider extends AbstractParamProvider {
	private final static Logger LOG = Logger.getLogger(FileParamProvider.class);
	
	private final JTextField field = new JTextField();
	private final JButton findButton = new JButton(Common.ICON_FILE_OPEN);
	
	private FileChooserConfiguration fcConfiguration = new FileChooserConfiguration();
	
	public static final String FILTERS_EXTRAS_PROPERTY = "filters";
	public static final String FILTERS_EXTRAS_ALLOWALL_FILTER= "allowAll";
	public static final String FILTERS_EXTRAS_SELECTION_MODE = "selectionMode";
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.findButton) {
			this.showFileChooser();
		}
		
		super.actionPerformed(e);
	};
	
	private void showFileChooser() {
		this.fcConfiguration.configureFileChooser(Common.SINGLE_FILE_CHOOSER);
		
		int option = Common.SINGLE_FILE_CHOOSER.showDialog(Workbench.getInstance().getMainFrame(), "Select");
		if (option == JFileChooser.APPROVE_OPTION) {
			File selected = Common.SINGLE_FILE_CHOOSER.getSelectedFile();
			if (selected != null)
				FileParamProvider.this.field.setText(selected.getPath());
		}
		
		this.fcConfiguration.clearFileChooser(Common.SINGLE_FILE_CHOOSER);
	};
	
	public FileParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p,clazz,operationObject);
		this.field.setEditable(false);
		
		if (p.defaultValue().length() > 0) {
			this.field.setText(p.defaultValue());
		}
		createFilters(p);
	}
	
	public static void configureFileChooser(Port p, JFileChooser fc) {
		final FileChooserConfiguration configuration = 
			new FileChooserConfiguration(p.extras());
		
		configuration.configureFileChooser(fc);
	}
	
	static class ExtensionFileFilter extends FileFilter{
		String nameregexp;
		String description;
		public ExtensionFileFilter(String nameregexp, String desc) {
			this.nameregexp = nameregexp;
			this.description = desc;
		}
	
		@Override
		public boolean accept(File pathname) {
			return (pathname.isDirectory() || pathname.getName().matches(this.nameregexp));
		}
		
		@Override
		public String getDescription() {		
			return this.description;
		}
	}
	
	static class FileChooserConfiguration {
		private final List<ExtensionFileFilter> filters;
		private int selectionMode;
		private boolean allowAll;
		
		public FileChooserConfiguration() {
			this("");
		}
		
		public FileChooserConfiguration(String configuration) {
			this.filters = new ArrayList<FileParamProvider.ExtensionFileFilter>();
			
			this.reset();
			this.configure(configuration);
		}
		
		public void configure(String configurationString) {
			if (configurationString == null || configurationString.trim().isEmpty())
				return;
			
			String[] props = configurationString.split(",");
			for (String prop : props) {
				String[] propValue = prop.split("=");
				if (propValue.length == 2) {
					propValue[0] = propValue[0].trim();
					propValue[1] = propValue[1].trim();
					if (propValue[0].equalsIgnoreCase(FILTERS_EXTRAS_PROPERTY)) {
						this.setAllowAll(false);

						String[] filters = propValue[1].split(";");
						for (String filter : filters) {
							String[] nameExpAndDesc = filter.split(":");
							if (nameExpAndDesc.length == 2) {
								nameExpAndDesc[0] = nameExpAndDesc[0].trim();
								nameExpAndDesc[1] = nameExpAndDesc[1].trim();
								
								this.getFilters().add(new ExtensionFileFilter(nameExpAndDesc[0], nameExpAndDesc[1]));
							} else if (nameExpAndDesc.length == 1) {
								nameExpAndDesc[0] = nameExpAndDesc[0].trim();
								if (nameExpAndDesc[0]
										.equalsIgnoreCase(FILTERS_EXTRAS_ALLOWALL_FILTER)) {
									this.setAllowAll(true);
								} else
									LOG.warn("unable to parse a filter in extras: "	+ filter);
							}
						}
					} else if (propValue[0]
							.equalsIgnoreCase(FILTERS_EXTRAS_SELECTION_MODE)) {
						if (propValue[1].equalsIgnoreCase("files")) {
							this.setSelectionMode(JFileChooser.FILES_ONLY);
						} else if (propValue[1].equalsIgnoreCase("directories")) {
							this.setSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						} else if (propValue[2]
								.equalsIgnoreCase("filesAndDirectories")) {
							this.setSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						} else {
							LOG.warn("unknown value for "
									+ FILTERS_EXTRAS_ALLOWALL_FILTER
									+ " filter: " + propValue[1]);
							this.setSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						}
					} else {
						LOG.warn("unknown filter: " + propValue[0]);
					}
				} else {
					LOG.warn("unable to parse a property in extras: " + prop);
				}
			}
		}
		
		public void reset() {
			this.filters.clear();
			this.selectionMode = JFileChooser.FILES_AND_DIRECTORIES;
			this.allowAll = true;
		}
		
		public List<ExtensionFileFilter> getFilters() {
			return this.filters;
		}
		
		public int getSelectionMode() {
			return this.selectionMode;
		}
		
		public void setSelectionMode(int selectionMode) {
			this.selectionMode = selectionMode;
		}

		public boolean isAllowAll() {
			return this.allowAll;
		}

		public void setAllowAll(boolean allowAll) {
			this.allowAll = allowAll;
		}
		
		public void configureFileChooser(JFileChooser fc) {
			fc.setAcceptAllFileFilterUsed(this.isAllowAll());
			fc.setFileSelectionMode(this.getSelectionMode());
			
			for (ExtensionFileFilter filter : this.getFilters()){
				fc.addChoosableFileFilter(filter);			
			}
		}
		
		public void clearFileChooser(JFileChooser fc) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setAcceptAllFileFilterUsed(true);
			for (ExtensionFileFilter filter : this.getFilters()) {
				fc.removeChoosableFileFilter(filter);
			}
		}
	}
	
	/**
	 * @param p
	 */
	private void createFilters(Port p) {
		this.fcConfiguration.reset();
		this.fcConfiguration.configure(p.extras());
	}

	public JComponent getComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		this.field.setPreferredSize(new Dimension(150, field.getPreferredSize().height));
		this.findButton.setToolTipText("Browse the local filesystem to select a file");
		
		panel.add(this.field, BorderLayout.CENTER);
		panel.add(this.findButton, BorderLayout.EAST);
		new  FileDrop( this.field, new FileDrop.Listener(){   
		  public void  filesDropped( java.io.File[] files ){   
    	  if (files.length > 0){
    		  FileParamProvider.this.field.setText(files[0].getPath());
    		  FileParamProvider.this.setChanged();
    		  FileParamProvider.this.notifyObservers();
			}
          }   
		}); 
		this.field.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				FileParamProvider.this.showFileChooser();
				FileParamProvider.this.setChanged();
				FileParamProvider.this.notifyObservers();
			}
		});
		this.findButton.addActionListener(this);
		
		return panel;
	}


	public ParamSpec getParamSpec() {
		if (field.getText().equals("")){
			return new ParamSpec(this.port.name(), clazz, null, ParamSource.CLIPBOARD);
		}
		return new ParamSpec(this.port.name(), this.clazz, field.getText(), ParamSource.STRING_CONSTRUCTOR);
	}

	/**
	 * @return the field
	 */
	public JTextField getField() {
		return this.field;
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public boolean isValidValue() {
		if (!this.port.allowNull()) {
			return this.field.getText().trim().length() > 0;
		}
		return true;
	}
}