/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;
import java.util.Set;

import org.vanda.studio.model.types.Type;
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

	/* outdated
	 * Returns the repository of converter tool repositories. Converter tools
	 * may be used by some linkers to convert port types. A converter tool
	 * must have exactly one input port and exactly one output port.
	 * Modules should add or remove their own repositories here.
	 * 
	MetaRepository<Tool> getConverterToolMetaRepository();
	 */

	/**
	 * Returns the repository of profile repositories. Modules should add or
	 * remove their own repositories here.
	 */
	// MetaRepository<Profile> getProfileMetaRepository();
	
	PreviewFactory getPreviewFactory(Type type);

	Set<Type> getTypes();
	
	/**
	 * Returns the repository of semantics module repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<SemanticsModule> getSemanticsModuleMetaRepository();
	
	/**
	 * Returns the repository of tool factory repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<ToolFactory> getToolFactoryMetaRepository();
	
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
	String getProperty(String key);
	
	/**
	 */
	void setProperty(String key, String value);

	/**
	 */
	Collection<UIMode> getUIModes();

	/**
	 */
	WindowSystem getWindowSystem();
	
	/**
	 * if type is null, pf will be regarded as fallback
	 * 
	 * @param type
	 * @param pf
	 */
	void registerPreviewFactory(Type type, PreviewFactory pf);
	
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
