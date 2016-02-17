/**
 * 
 */
package org.vanda.studio.core;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.RCChecker;

/**
 * Creates lots of `Module`s, connects them to a new `ModuleManager`
 * that is then registered with the `Application`. 
 */
public final class Launcher implements Runnable {

	@Override
	public void run() {
		Application app = new Application();
		Module[] ms = {
				new org.vanda.studio.modules.messages.MessageModule(),
				new org.vanda.studio.modules.tools.ToolsModule(),
				new org.vanda.studio.modules.previews.PreviewsModule(),
				new org.vanda.studio.modules.workflows.WorkflowModule(),
				new org.vanda.studio.modules.datasources.DataSourceModule() };

		ModuleManager moduleManager = new ModuleManager(app);
		moduleManager.loadModules();
		for (Module m : ms)
			moduleManager.loadModule(m);
		moduleManager.initModules();
		app.setModuleManager(moduleManager);
		
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(app));
	}

	/**
	 * @param args
	 *            Command line Arguments
	 */
	public static void main(String[] args) {
		RCChecker.readRC();
		if (args.length > 1
				&& (args[0].equals("-r") || args[0].equals("--run"))) {
			runWorkflow(args[1]);
		} else {
			displayGUI();
		}

	}

	public static void displayGUI() {
		SwingUtilities.invokeLater(new Launcher());
	}

	/**
	 * executes a workflow without loading the GUI // TODO or rather it should do so...
	 * 
	 * @param fileName
	 *            workflow to be executed
	 */
	public static void runWorkflow(String fileName) {
		// TODO there was some copypasted code here... but it was all commented out
		//      so... maybe implement this? or remove the method altogether?
	}

	/**
	 * Basically calls: Application.sendMessage(new ExceptionMessage(e))
	 */
	public static class ExceptionHandler implements
			Thread.UncaughtExceptionHandler {
		private final Application app;

		public ExceptionHandler(Application app) {
			this.app = app;
		}

		public void uncaughtException(final Thread t, final Throwable e) {
			if (SwingUtilities.isEventDispatchThread()) {
				app.sendMessage(new ExceptionMessage(e));
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							app.sendMessage(new ExceptionMessage(e));
						}
					});
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} catch (InvocationTargetException ite) {
					// not much more we can do here except log the exception
					ite.getCause().printStackTrace();
				}
			}
		}

	}
}
