/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;

import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.workflows.Compiler;
import org.vanda.studio.model.workflows.Linker;
import org.vanda.studio.model.workflows.Tool;
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
	 * Returns an immutable combined view of the compiler repositories.
	 */
	Repository<Compiler<?, ?>> getCompilerRepository();

	/**
	 * Returns the repository of compiler repositories. Modules should add or
	 * remove their own repositories here.
	 */
	MetaRepository<Compiler<?, ?>> getCompilerRR();

	/**
	 * Returns an immutable combined view of the linker repositories.
	 */
	Repository<Linker<?, ?, ?>> getLinkerRepository();

	/**
	 * Returns the repository of linker repositories. Modules should add or
	 * remove their own repositories here.
	 */
	MetaRepository<Linker<?, ?, ?>> getLinkerRR();

	/**
	 * Returns an immutable combined view of the profile repositories.
	 */
	Repository<Profile> getProfileRepository();

	/**
	 * Returns the repository of profile repositories. Modules should add or
	 * remove their own repositories here.
	 */
	MetaRepository<Profile> getProfileRR();

	/**
	 * Returns an immutable combined view of the tool repositories.
	 */
	Repository<Tool<?, ?>> getToolRepository();

	/**
	 * Returns the repository of tool repositories. Modules should add or remove
	 * their own repositories here.
	 */
	MetaRepository<Tool<?, ?>> getToolRR();
	
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
	void setUIMode(UIMode m);

	/**
	 * Quit the application.
	 */
	void shutdown();

}
