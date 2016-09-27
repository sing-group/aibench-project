package es.uvigo.ei.aibench.workbench.utilities;

import javax.help.HelpBroker;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.workbench.Workbench;

public class HelpButton extends JButton {
	private static final long serialVersionUID = 1L;

	public HelpButton() {
		this(new ImageIcon(HelpButton.class.getResource("images/helpbutton.png")));
	}
	
	public HelpButton(ImageIcon icon) {
		super(icon);
		
		this.initComponent();
	}

	private void initComponent() {
		final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
		if (helpBroker != null) {
			helpBroker.enableHelpOnButton(
				this, "top", helpBroker.getHelpSet()
			);

			helpBroker.enableHelpKey(
				Workbench.getInstance().getMainFrame(), 
				"top", 
				helpBroker.getHelpSet()
			);
		}
	}
}
