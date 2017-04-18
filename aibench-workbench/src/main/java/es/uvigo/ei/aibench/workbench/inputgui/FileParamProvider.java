/*
 * #%L
 * The AIBench Workbench Plugin
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
import es.uvigo.ei.aibench.workbench.utilities.PortExtras;

public class FileParamProvider extends AbstractParamProvider {
	private final static Logger LOGGER = Logger.getLogger(FileParamProvider.class);

	private final JTextField field = new JTextField();
	private final JButton findButton = new JButton(Common.ICON_FILE_OPEN);

	private final FileChooserConfiguration fcConfiguration;

	public static final boolean DEFAULT_CASE_SENSITIVE = true;
	public static final String CASE_SENSITIVE_FILTERS_EXTRAS_PROPERTY = "caseSensitiveFilters";
	public static final String FILTERS_EXTRAS_PROPERTY = "filters";
	public static final String FILTERS_EXTRAS_ALLOWALL_FILTER = "allowAll";
	public static final String FILTERS_EXTRAS_SELECTION_MODE = "selectionMode";

	private static final String SELECTION_MODE_FILES_AND_DIRECTORIES = "filesAndDirectories";
	private static final String SELECTION_MODE_DIRECTORIES = "directories";
	private static final String SELECTION_MODE_FILES = "files";

	private static final String[] KNOWN_PROPERTIES = {
		CASE_SENSITIVE_FILTERS_EXTRAS_PROPERTY,
		FILTERS_EXTRAS_PROPERTY,
		FILTERS_EXTRAS_SELECTION_MODE
	};

	private JComponent component = null;
	private FileFilter lastFileFilter = null;

	public FileParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p, clazz, operationObject);
		this.fcConfiguration = new FileChooserConfiguration(p.extras());
		this.initComponent(p);
	}

	private void initComponent(Port p) {
		this.field.setEditable(false);
		
		if (p.defaultValue().length() > 0) {
			this.field.setText(p.defaultValue());
		}
	}

	public static void configureFileChooser(Port p, JFileChooser fc) {
		FileChooserConfiguration.configure(p.extras(), fc);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.findButton) {
			this.showFileChooser();
		}

		super.actionPerformed(e);
	}

	private void showFileChooser() {
		JFileChooser fileChooser = Common.SINGLE_FILE_CHOOSER;
		this.fcConfiguration.configureFileChooser(fileChooser);
		configureLastFileFilter(fileChooser, this.lastFileFilter);

		final File selectedFile = this.getSelectedFile();
		if (selectedFile != null) {
			fileChooser.setSelectedFile(selectedFile);
		}

		final int option = fileChooser.showDialog(Workbench.getInstance().getMainFrame(), "Select");
		this.lastFileFilter = fileChooser.getFileFilter();
		if (option == JFileChooser.APPROVE_OPTION) {
			setSelectedFile(fileChooser.getSelectedFile());
		}
	}

	private static void configureLastFileFilter(JFileChooser fileChooser, FileFilter lastFileFilter) {
		if (lastFileFilter != null) {
			fileChooser.setFileFilter(lastFileFilter);
		}
	}

	private static class ExtensionFileFilter extends FileFilter {
		private final String nameregexp;
		private final String description;
		private boolean caseSensitive;

		public ExtensionFileFilter(String nameregexp, String desc, boolean caseSensitive) {
			this.nameregexp = nameregexp;
			this.description = desc;
			this.caseSensitive = caseSensitive;
		}

		@Override
		public boolean accept(File pathname) {
			return (pathname.isDirectory() || pathname.getName().matches(getRegex()));
		}

		private String getRegex() {
			return caseSensitive == true ? nameregexp : ("(?i)" + nameregexp);
		}

		@Override
		public String getDescription() {
			return this.description;
		}
	}

	private static class FileChooserConfiguration {
		public static final String FILE_FILTERS_DELIMITER = ";";
		public static final String FILE_FILTERS_ASSIGNMENT = ":";

		private final List<ExtensionFileFilter> filters;
		private int selectionMode;
		private boolean allowAll;

		public static void configure(String configuration, JFileChooser fc) {
			new FileChooserConfiguration(configuration).configureFileChooser(fc);
		}

		public FileChooserConfiguration(String configuration) {
			this.filters = new ArrayList<>();
			this.selectionMode = JFileChooser.FILES_AND_DIRECTORIES;
			this.allowAll = true;

			parseConfiguration(configuration);
		}

		private void parseConfiguration(String configuration) {
			PortExtras extras = PortExtras.parse(configuration);

			boolean caseSensitive = DEFAULT_CASE_SENSITIVE;
			if (extras.containsProperty(CASE_SENSITIVE_FILTERS_EXTRAS_PROPERTY)) {
				caseSensitive = parseCaseSensitiveFilter(
					extras.getPropertyValue(CASE_SENSITIVE_FILTERS_EXTRAS_PROPERTY)
				);
			}

			if (extras.containsProperty(FILTERS_EXTRAS_PROPERTY)) {
				parseFilters(extras.getPropertyValue(FILTERS_EXTRAS_PROPERTY), caseSensitive);
			}

			if (extras.containsProperty(FILTERS_EXTRAS_SELECTION_MODE)) {
				parseSelectionMode(extras.getPropertyValue(FILTERS_EXTRAS_SELECTION_MODE));
			}

			PortExtras.warnUnknownExtraProperties(extras, LOGGER, true, KNOWN_PROPERTIES);
		}

		private boolean parseCaseSensitiveFilter(String caseSensitiveValue) {
			if (caseSensitiveValue.equalsIgnoreCase("false")) {
				return false;
			} else if (caseSensitiveValue.equalsIgnoreCase("true")) {
				return true;
			} else {
				LOGGER.warn("Invalid value for property: " + CASE_SENSITIVE_FILTERS_EXTRAS_PROPERTY);
			}
			return DEFAULT_CASE_SENSITIVE;
		}

		private void parseFilters(String value, boolean caseSensitive) {
			this.allowAll = false;

			final String[] filters = value.split(FILE_FILTERS_DELIMITER);
			for (String filter : filters) {
				final String[] nameExpAndDesc = filter.trim().split(FILE_FILTERS_ASSIGNMENT);

				if (nameExpAndDesc.length == 2) {
					final String nameExtension = nameExpAndDesc[0].trim();
					final String description = nameExpAndDesc[1].trim();

					this.filters.add(new ExtensionFileFilter(nameExtension, description, caseSensitive));
				} else if (nameExpAndDesc.length == 1) {
					final String nameExtension = nameExpAndDesc[0].trim();

					if (FILTERS_EXTRAS_ALLOWALL_FILTER.equalsIgnoreCase(nameExtension)) {
						this.allowAll = true;
					} else {
						LOGGER.warn("Unable to parse a filter in extras: " + filter);
					}
				} else {
					LOGGER.warn("Unable to parse a filter in extras: " + filter);
				}
			}
		}

		private void parseSelectionMode(String value) {
			if (SELECTION_MODE_FILES.equalsIgnoreCase(value)) {
				this.selectionMode = JFileChooser.FILES_ONLY;
			} else if (SELECTION_MODE_DIRECTORIES.equalsIgnoreCase(value)) {
				this.selectionMode = JFileChooser.DIRECTORIES_ONLY;
			} else if (SELECTION_MODE_FILES_AND_DIRECTORIES.equalsIgnoreCase(value)) {
				this.selectionMode = JFileChooser.FILES_AND_DIRECTORIES;
			} else {
				LOGGER.warn("Unknown value for " + FILTERS_EXTRAS_SELECTION_MODE + " filter: " + value);

				this.selectionMode = JFileChooser.FILES_AND_DIRECTORIES;
			}
		}
		public void configureFileChooser(JFileChooser fc) {
			final File selectedFile = fc.getSelectedFile();
			fc.setSelectedFile(null);
			
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileSelectionMode(this.selectionMode);

			fc.resetChoosableFileFilters();
			for (ExtensionFileFilter filter : this.filters) {
				fc.addChoosableFileFilter(filter);
			}
			fc.setAcceptAllFileFilterUsed(this.allowAll);

			// Solves a bug on the JFileChooser that makes the dialog to lose
			// the selected file when the file selection mode is changed,
			// even when the selected file is still compatible with the
			// selection mode.
			fc.setSelectedFile(selectedFile);
		}
	}
	
	private File getSelectedFile() {
		final String text = this.field.getText().trim();
		
		return text.isEmpty() ? null : new File(text);
	}

	/**
	 * <p>
	 * Establishes the selected file. Note that if {@code file} is {@code null},
	 * then the selection is cleared.
	 * </p>
	 * 
	 * <p>
	 * Calling this method marks this {@code Observable} object as having been
	 * changed and notifies observers.
	 * </p>
	 * 
	 * @param file the selected file
	 */
	public void setSelectedFile(File file) {
		if (file == null) {
			this.field.setText("");
		} else {
			this.field.setText(file.getAbsolutePath());
		}
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Returns the {@code JTextField} used to display the selected file.
	 * 
	 * @return the {@code JTextField} used to display the selected file
	 */
	public JTextField getField() {
		return this.field;
	}

	@Override
	public JComponent getComponent() {
		if (this.component == null) {
			this.component = new JPanel();
			this.component.setLayout(new BorderLayout());

			this.field.setPreferredSize(new Dimension(150, field.getPreferredSize().height));
			this.findButton.setToolTipText("Browse the local filesystem to select a file");

			this.component.add(this.field, BorderLayout.CENTER);
			this.component.add(this.findButton, BorderLayout.EAST);
			new FileDrop(
				this.field, new FileDrop.Listener() {
					public void filesDropped(File[] files) {
						if (files.length > 0) {
							FileParamProvider.this.setSelectedFile(files[0]);
						}
					}
				}
			);

			this.field.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						FileParamProvider.this.showFileChooser();
					}
				}
			);
			this.findButton.addActionListener(this);
		}

		return this.component;
	}

	@Override
	public ParamSpec getParamSpec() {
		final File selectedFile = this.getSelectedFile();
		
		if (selectedFile == null) {
			return new ParamSpec(this.port.name(), clazz, null, ParamSource.CLIPBOARD);
		} else {
			return new ParamSpec(this.port.name(), this.clazz, selectedFile.getAbsolutePath(), ParamSource.STRING_CONSTRUCTOR);
		}
	}

	@Override
	public boolean isValidValue() {
		return this.port.allowNull() || this.getSelectedFile() != null;
	}
}