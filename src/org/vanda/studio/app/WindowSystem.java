/**
 * 
 */
package org.vanda.studio.app;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.util.Action;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface WindowSystem {
	static final Integer CENTER = 0;
	static final Integer NORTH = 1;
	static final Integer NORTHWEST = 2;
	static final Integer WEST = 3;
	static final Integer SOUTHWEST = 4;
	static final Integer SOUTH = 5;
	static final Integer SOUTHEAST = 6;
	static final Integer EAST = 7;
	static final Integer NORTHEAST = 8;
	
	/**
	 * Call this *before* adding c as a contentWindow.
	 */
	void addAction(JComponent c, Action a, KeyStroke keyStroke);

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
	void addToolWindow(JComponent window, Icon i, JComponent c, Integer layer);

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
