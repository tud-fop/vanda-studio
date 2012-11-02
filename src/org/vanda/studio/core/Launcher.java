/**
 * 
 */
package org.vanda.studio.core;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.RCChecker;

public final class Launcher implements Runnable {

	private Launcher() {
		// utility class
	}

	@Override
	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			// UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			// UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
			/*
			 * for (UIManager.LookAndFeelInfo info : UIManager
			 * .getInstalledLookAndFeels()) {
			 * System.out.println(info.getName()); if
			 * ("Nimbus".equals(info.getName())) {
			 * UIManager.setLookAndFeel(info.getClassName()); break; } }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Application app = new ApplicationImpl();
		Module[] ms = { new org.vanda.studio.modules.messages.MessageModule(),
				new org.vanda.studio.modules.algorithms.AlgorithmsModule(),
				new org.vanda.studio.modules.profile.ProfileModule(),
				new org.vanda.studio.modules.dictionaries.DictionaryModule(),
				// new org.vanda.studio.modules.wrtgs.WrtgModule(),
				// new org.vanda.studio.modules.terms.TermModule(),
				new org.vanda.studio.modules.workflows.WorkflowModule() };
		app.registerPreviewFactory(null, new DefaultPreviewFactory(app));

		ModuleManager moduleManager = new ModuleManager(app);
		moduleManager.loadModules();
		for (Module m : ms)
			moduleManager.loadModule(m);
		moduleManager.initModules();

		app.getToolMetaRepository().getRepository().refresh();
		System.out.println(app.getToolMetaRepository().getRepository()
				.getItems());
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(app));
		// throw new NullPointerException("brain is null");
	}

	/**
	 * @param args
	 *            Command line Arguments
	 */
	public static void main(String[] args) {
		RCChecker.readRC();
		SwingUtilities.invokeLater(new Launcher());
	}

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
