/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;
import java.util.Set;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceFactory;
import org.vanda.types.Type;
import org.vanda.util.Message;
import org.vanda.util.MetaRepository;
import org.vanda.util.Observable;
import org.vanda.workflows.elements.Tool;

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
	 * Returns the repository of tool interface repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<Tool> getToolMetaRepository();
	
	MetaRepository<DataSourceFactory> getDataSourceMetaRepository();
	
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

	/**
	 * Return file name for a given resource name.
	 * 
	 * @param value
	 * @return
	 */
	String findFile(String value);

}
