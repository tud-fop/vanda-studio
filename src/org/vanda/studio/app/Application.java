/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.Repository;
import org.vanda.studio.model.VObject;
import org.vanda.studio.util.Observable;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface Application {
	
	/**
	 */
	void addRepository(Repository<VObject> r);
	
	/**
	 */
	String createUniqueId();
	
	/**
	*/
	void focusObject(VObject o);
	
	/**
	 */
	Observable<Application> getFocusChangeObservable();
	
	/**
	 */
	Observable<Application> getFocusedObjectModifiedObservable();
	
	/**
	*/
	VObject getFocusedObject();
	
	/**
	 */
	Repository<VObject> getGlobalRepository();
	
	/**
	 */
	Observable<Application> getShutdownObservable();
	
	/**
	 */
	Observable<Application> getUIModeObservable();
	
	/**
	 */
	UIMode getUIMode();
	
	/**
	 */
	Collection<UIMode> getUIModes();
	
	/**
	 */
	WindowSystem getWindowSystem();
	
	/**
	 */
	void removeRepository(Repository<VObject> r);
	
	/**
	 */
	void setUIMode(UIMode m);
	
	/**
	 * Quit the application.
	 */
	void shutdown();

}
