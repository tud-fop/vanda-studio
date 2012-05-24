/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.util.Message;
import org.vanda.studio.util.Observable;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface Application {

	/**
	 */
	String createUniqueId();

	/**
	 * Returns the repository of converter tool repositories. Converter tools
	 * may be used by some linkers to convert port types. A converter tool
	 * must have exactly one input port and exactly one output port.
	 * Modules should add or remove their own repositories here.
	 */
	MetaRepository<Tool> getConverterToolMetaRepository();

	/**
	 * Returns the repository of linker repositories. Modules should add or
	 * remove their own repositories here.
	 */
	MetaRepository<Linker> getLinkerMetaRepository();

	/**
	 * Returns the repository of profile repositories. Modules should add or
	 * remove their own repositories here.
	 */
	MetaRepository<Profile> getProfileMetaRepository();

	/**
	 * Returns the repository of tool repositories. Modules should add or remove
	 * their own repositories here.
	 */
	MetaRepository<Tool> getToolMetaRepository();
	
	/**
	 * Returns an immutable combined view of the workflow repositories.
	 *
	Repository<WorkflowDescription> getWorkflowRepository();	

	 **
	 * Returns the repository of workflow repositories. Modules should add or
	 * remove their own repositories here.
	 * 
	Repository<Repository<WorkflowDescription>> getWorkflowRR();
	*/
	
	Observable<Message> getMessageObservable();

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
	void sendMessage(Message m);

	/**
	 */
	void setUIMode(UIMode m);

	/**
	 * Quit the application.
	 */
	void shutdown();

}
