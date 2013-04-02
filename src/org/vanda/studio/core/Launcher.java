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

public final class Launcher implements Runnable {

	private Launcher() {
		// utility class
	}

	@Override
	public void run() {
		try {
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		Module[] ms = {
				new org.vanda.studio.modules.messages.MessageModule(),
				new org.vanda.studio.modules.tools.ToolsModule(),
				new org.vanda.studio.modules.previews.PreviewsModule(),
				new org.vanda.studio.modules.workflows.WorkflowModule(),
				new org.vanda.studio.modules.workflows.datasources.DataSourceModule() };
		// app.registerPreviewFactory(null, new DefaultPreviewFactory(app));

		ModuleManager moduleManager = new ModuleManager(app);
		moduleManager.loadModules();
		for (Module m : ms)
			moduleManager.loadModule(m);
		moduleManager.initModules();

		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(app));
		// throw new NullPointerException("brain is null");
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
	 * executes a workflow without loading the GUI
	 * 
	 * @param fileName
	 *            workflow to be executed
	 */
	public static void runWorkflow(String fileName) {
		/** SCHROTT: Wieso copy&paste aus dem Profiles-Modul?
		Application app = new ApplicationImpl(false);

		// only load AlgorithmsModule, nothing else is needed
		ModuleManager moduleManager = new ModuleManager(app);
		moduleManager.loadModules();
		moduleManager
				.loadModule(new org.vanda.studio.modules.algorithms.AlgorithmsModule());
		moduleManager.initModules();

		Profiles profiles = new ProfilesImpl();
		SimpleRepository<FragmentCompiler> compilers = new SimpleRepository<FragmentCompiler>(
				null);
		compilers.addItem(new HaskellCompiler());
		compilers.addItem(new ShellCompiler());
		profiles.getFragmentCompilerMetaRepository().addRepository(compilers);

		SimpleRepository<FragmentLinker> linkers = new SimpleRepository<FragmentLinker>(
				null);
		linkers.addItem(new IdentityLinker());
		linkers.addItem(new HaskellLinker());
		profiles.getFragmentLinkerMetaRepository().addRepository(linkers);
		Profile profile = new ProfileImpl(app, profiles);

		try {
			MutableWorkflow hwf = Serialization.load(app, fileName);
			for (ToolFactory tf : app.getToolFactoryMetaRepository()
					.getRepository().getItems())
				tf.instantiate(null, new Model(hwf));
			ImmutableWorkflow iwf = hwf.freeze();
			iwf.typeCheck();
			Fragment frag = profile.generate(iwf);
			Process process = Runtime.getRuntime().exec(
					RCChecker.getOutPath() + "/"
							+ Fragment.normalize(frag.name), null, null);
			InputStream stdin = process.getInputStream();
			StreamGobbler sgIn = new StreamGobbler(stdin);
			sgIn.start();
			InputStream stderr = process.getErrorStream();
			StreamGobbler sgErr = new StreamGobbler(stderr);
			sgErr.start();
			process.waitFor();
			stdin.close();
			stderr.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	private static class StreamGobbler extends Thread {
		private final InputStream is;

		public StreamGobbler(InputStream is) {
			this.is = is;
		}

		public void run() {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

		
		**/
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
