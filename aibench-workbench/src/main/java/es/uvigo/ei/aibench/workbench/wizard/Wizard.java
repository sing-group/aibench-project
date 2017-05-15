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
package es.uvigo.ei.aibench.workbench.wizard;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Wizard extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel buttonsPanel = null;

	private JButton nextButton = null;

	private JButton cancelButton = null;

	private JPanel stepContentPanel = null;

	private Object userObject;

	private WizardStep[] steps;

	private int currentStep = 0;

	private int initialWidth = 552;

	private int initialHeight = 411;

	private CardLayout assistantLayout;

	private JLabel labelTitle = new JLabel();

	private JButton backButton = null;

	private boolean wasCancelled = false;

	/**
	 * This is the default constructor.
	 * 
	 * @param owner the frame owner of this dialog.
	 * @param userObject the user object that will be configured.
	 * @param steps the wizard steps.
	 */
	public Wizard(Frame owner, Object userObject, WizardStep[] steps) {
		super(owner);
		this.steps = steps;
		associateSteps();
		this.userObject = userObject;
		initialize();
		this.setModal(true);
		this.centerOnOwner();
	}

	/**
	 * Constructor that allows setting the width and height of the dialog.
	 * 
	 * @param owner the frame owner of this dialog.
	 * @param userObject the user object that will be configured.
	 * @param steps the wizard steps.
	 * @param width the initial width of this dialog.
	 * @param height the initial height of this dialog.
	 */
	public Wizard(Frame owner, Object userObject, WizardStep[] steps,
			int width, int height) {
		super(owner);
		this.steps = steps;
		associateSteps();
		this.userObject = userObject;
		this.initialWidth = width;
		this.initialHeight = height;
		initialize();
		this.setModal(true);
		this.centerOnOwner();
	}

	/**
	 * @return the user-defined object.
	 */
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * @return {@code true} if the user cancelled the operation,
	 *         {@code false} otherwise.
	 */
	public boolean wasCancelled() {
		return wasCancelled;
	}

	/**
	 * This method initializes jButton6.
	 *
	 * @return javax.swing.JButton.
	 */
	private JButton getBackButton() {
		if (backButton == null) {
			backButton = new JButton();
			backButton.setText("Back");
			backButton.setEnabled(false);
			backButton.setIcon(new ImageIcon(getClass().getResource(
					"/images/atras.png")));
			backButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					assistantLayout.previous(getStepContentPanel());
					currentStep--;

					if (currentStep < (steps.length - 1)) {
						getNextButton().setText("Next");
					}

					steps[currentStep].onEnter();
					labelTitle.setIcon(steps[currentStep].getHeaderIcon());

					if (currentStep == 0) {
						backButton.setEnabled(false);
					}

					if (steps.length > 0) {
						nextButton.setEnabled(true);
					}
				}
			});
		}

		return backButton;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(flowLayout1);
			flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
			buttonsPanel.add(getBackButton(), null);
			buttonsPanel.add(getNextButton(), null);
			buttonsPanel.add(getCancelButton(), null);
		}

		return buttonsPanel;
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.setIcon(new ImageIcon(getClass().getResource(
					"/images/cancel.png")));
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					wasCancelled = true;
					setVisible(false);
				}
			});
		}

		return cancelButton;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			labelTitle.setOpaque(true);
			labelTitle.setBackground(java.awt.Color.WHITE);
			labelTitle.setIcon(steps[currentStep].getHeaderIcon());
			jContentPane.add(labelTitle, java.awt.BorderLayout.NORTH);
			jContentPane.add(getButtonsPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getStepContentPanel(),
					java.awt.BorderLayout.CENTER);
		}

		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton();

			nextButton.setText("Next");
			if (currentStep == (steps.length - 1)) {
				nextButton.setText("Finish");
			}

			if (steps.length == 0) {
				nextButton.setEnabled(false);
			}

			nextButton.setIcon(new ImageIcon(getClass().getResource(
					"/images/adelante.png")));
			nextButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (steps[currentStep].onNext()) {
						assistantLayout.next(getStepContentPanel());

						if (currentStep == (steps.length - 1)) {
							setVisible(false);

							return;
						}

						currentStep++;
						steps[currentStep].onEnter();
						labelTitle.setIcon(steps[currentStep].getHeaderIcon());
						backButton.setEnabled(true);
					}

					if (currentStep == (steps.length - 1)) {
						nextButton.setText("Finish");
					}

					// if(currentStep == steps.length-1)
					// nextButton.setEnabled(false);
				}
			});
		}

		return nextButton;
	}

	/**
	 * @return Returns the stepContentPanel.
	 */
	private JPanel getStepContentPanel() {
		if (stepContentPanel == null) {
			stepContentPanel = new JPanel();
			stepContentPanel.setOpaque(true);
			stepContentPanel.setBackground(java.awt.Color.WHITE);
			assistantLayout = new CardLayout();
			stepContentPanel.setLayout(assistantLayout);

			for (int i = 0; i < steps.length; i++) {
				stepContentPanel.add(steps[i].getContentComponent(), "Step-"
						+ i);
			}

			if (steps.length > 0) {
				steps[0].onEnter();
			}

			stepContentPanel.setBackground(java.awt.Color.white);
			stepContentPanel.setBorder(javax.swing.BorderFactory
					.createMatteBorder(0, 0, 1, 0, java.awt.Color.gray));
		}

		return stepContentPanel;
	}

	private void associateSteps() {
		for (int i = 0; i < this.steps.length; i++) {
			steps[i].setWizard(this);
		}
	}

	private void centerOnOwner() {
		Rectangle rectOwner;
		Rectangle rectDialog;
		rectOwner = this.getParent().getBounds();
		rectDialog = this.getBounds();
		setLocation((rectOwner.x + (rectOwner.width / 2))
				- (rectDialog.width / 2),
				(rectOwner.y + (rectOwner.height / 2))
						- (rectDialog.height / 2));

	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setSize(initialWidth, initialHeight);

		if (steps == null) {
			return;
		}

		this.setContentPane(getJContentPane());
		this.pack();
	}
} // @jve:decl-index=0:visual-constraint="10,10"
