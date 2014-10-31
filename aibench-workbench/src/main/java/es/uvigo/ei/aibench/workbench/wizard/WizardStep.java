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
 * WizardStep.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.workbench.wizard;

import javax.swing.ImageIcon;
import javax.swing.JComponent;



public abstract class WizardStep {
    private Wizard wizard;

    public WizardStep() {
        super();
    }

    /**
     * Gives the component to render in the current step. It will be deployed
     * in the Wizard window.
     *
     * @return The component to deploy.
     */
    public abstract JComponent getContentComponent();

    /**
     * Gives a image icon to put in the header of the Wizard window when this step is visible.
     *
     * @return The header icon.
     */
    public abstract ImageIcon getHeaderIcon();

    /**
	 * @param wizard   The wizard to set.
	 */
    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
    }

    /**
     * This method is called when the wizard reaches this step (or when the user went back to it).
     */
    public abstract void onEnter();

    /**
     * This method is called when the wizard is just exiting this step. If the method returns
     * <code>false</code> the wizard will not advance to the next step.
     * @return <code>true</code> if the wizard should advance to the next step, <code>false</code> if it should keep visible the current step.
     */
    public abstract boolean onNext();

    /**
	 * @return   Returns the wizard.
	 */
    public Wizard getWizard() {
        return wizard;
    }
}
