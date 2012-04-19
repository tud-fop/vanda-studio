/**
 * 
 */
package org.vanda.studio.app;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.vanda.studio.model.Action;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface WindowSystem {
	/**
	*/
	void addAction(Action a);

	/**
	 */
	void addContentWindow(String id, String title, Icon i, JComponent c);

	/**
	 */
	void addToolWindow(String id, String title, Icon i, JComponent c);

	/**
	 */
	void focusContentWindow(JComponent c);
	
	/**
	 */
	void removeContentWindow(JComponent c); 

	/**
	 */
	void removeToolWindow(JComponent c);

}
