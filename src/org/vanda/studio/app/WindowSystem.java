/**
 * 
 */
package org.vanda.studio.app;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.vanda.studio.util.Action;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface WindowSystem {
	/**
	 * Call this *before* adding c as a contentWindow.
	 */
	void addAction(JComponent c, Action a);

	/**
	 * Creates a new tab in the main pane.
	 * The action a is invoked when
	 * the little x-button within the tab title is clicked. If a null action is
	 * used, the tab will simply be removed from the display.
	 */
	void addContentWindow(Icon i, JComponent c, Action a);

	/**
	 */
	void addSeparator();
	
	/**
	 */
	void addToolWindow(JComponent window, Icon i, JComponent c);

	/**
	 */
	void disableAction(Action a);
	
	/**
	 */
	void enableAction(Action a);
	
	/**
	 */
	void focusContentWindow(JComponent c);
	
	void focusToolWindow(JComponent c);
	
	/**
	 */
	void removeContentWindow(JComponent c); 

	/**
	 */
	void removeToolWindow(JComponent window, JComponent c);

}
