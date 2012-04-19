/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;

import org.vanda.studio.model.Repository;
import org.vanda.studio.model.Tool;
import org.vanda.studio.util.Observable;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface Application {
	
	/**
	 */
	void addRepository(Repository<Tool> r);
	
	/**
	 */
	String createUniqueId();
	
	/**
	*/
	void focusObject(Tool o);
	
	/**
	 */
	Observable<Application> getFocusChangeObservable();
	
	/**
	 */
	Observable<Application> getFocusedObjectModifiedObservable();
	
	/**
	*/
	Tool getFocusedObject();
	
	/**
	 */
	Repository<Tool> getGlobalRepository();
	
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
	void removeRepository(Repository<Tool> r);
	
	/**
	 */
	void setUIMode(UIMode m);
	
	/**
	 * Quit the application.
	 */
	void shutdown();

}
