/**
 * 
 */
package org.vanda.studio.core;

import java.io.File;
import java.util.ArrayList;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.Observer;

/**
 * The Module Manager is responsible for loading available Vanda Composer
 * Application Modules, for resolving dependencies among them. It is furthermore
 * responsible for initializing, starting and stopping all modules in the
 * correct order with respect to dependencies.
 * 
 * @author buechse, rmueller
 * @version 0.1
 */
public class ModuleManager {

	/** Vanda Composer Application root object */
	protected Application application;

	/** ModuleLoader that is responsible for loading modules, i.e. jar's */
	protected ModuleLoader moduleLoader;

	/** modules */
	protected ArrayList<Module> modules;
	
	/** module instances */
	protected ArrayList<Object> instances;

	/**
	 * @param application
	 *            Vanda Composer Application root object
	 */
	public ModuleManager(Application application) {
		this.application = application;
		modules = new ArrayList<Module>();
		instances = new ArrayList<Object>();
		application.getShutdownObservable().addObserver(
			new Observer<Application>() {
				@Override
				public void notify(Application a) {
					finalizeModules();
				}
			});
	}
	
	/**
	 * allows to explicitly specify a module
	 */
	public void loadModule(Module m) {
		modules.add(m);
	}
	
	/**
	 * Loads all available modules and resolves module dependencies.
	 */
	public void loadModules() {
		ModuleLoader.loadJars("." + File.separator + "modules", modules);
	}

	/**
	 * Initializes all previously loaded modules.
	 */
	public void initModules() {
		instances.clear();
		instances.ensureCapacity(modules.size());
		
		for (Module m : modules)
			instances.add(m.createInstance(application));
	}

	/**
	 * Signals all modules to stop and cleanup their respective resources.
	 */
	public void finalizeModules() {
		instances.clear();
	}
}
