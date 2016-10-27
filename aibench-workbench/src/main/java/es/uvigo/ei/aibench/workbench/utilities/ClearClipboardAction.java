package es.uvigo.ei.aibench.workbench.utilities;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.Clipboard;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;

/**
 * An action that removes all clipboard items.
 * 
 * @author Hugo López-Fernández
 *
 */
public class ClearClipboardAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	public static final String DESCRIPTION = "Removes all items in the clipboard";

	public static final ImageIcon ICON_16 = new ImageIcon(
		ClearClipboardAction.class.getResource("images/trash16.png"));
	public static final ImageIcon ICON_24 = new ImageIcon(
		ClearClipboardAction.class.getResource("images/trash24.png"));
	public static final ImageIcon ICON_32 = new ImageIcon(
		ClearClipboardAction.class.getResource("images/trash32.png"));
	
	private static final JOptionPaneMessage DONT_SHOW_AGAIN = 
		new JOptionPaneMessage("All items in the clipboard will be deleted. Do you want to continue?");
	
	public ClearClipboardAction() {
		this(DESCRIPTION, ICON_32);
	}
	
	public ClearClipboardAction(String shortDescription) {
		this(shortDescription, ICON_32);
	}
	
	public ClearClipboardAction(ImageIcon icon) {
		this(DESCRIPTION, icon);
	}
	
	public ClearClipboardAction(String shortDescription, ImageIcon icon) {
		super(shortDescription, icon);
		this.putValue(Action.SHORT_DESCRIPTION, shortDescription);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(askRemoveClipboardItems()){
			removeClipboardItems();
		}
	}

	private boolean askRemoveClipboardItems() {
		return 	!DONT_SHOW_AGAIN.shouldBeShown() || 
				JOptionPane.showConfirmDialog(
					null, DONT_SHOW_AGAIN.getMessage(),
					"Remove all items", JOptionPane.YES_NO_OPTION
				) == JOptionPane.OK_OPTION;
	}

	protected void removeClipboardItems() {
		for (ClipboardItem item : getClipboardItemsToRemove()) {
			Clipboard clipboard = Core.getInstance().getClipboard();
			if (item.getUserData() != null) {
				clipboard.removeClipboardItem(item);
			}
		}
	}

	protected List<ClipboardItem> getClipboardItemsToRemove() {
		return Core.getInstance().getClipboard().getAllItems();
	}
}
