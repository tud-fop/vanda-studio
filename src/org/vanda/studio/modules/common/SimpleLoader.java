/**
 * 
 */
package org.vanda.studio.modules.common;

import java.io.File;
import java.io.FilenameFilter;

import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.util.Observer;

/**
 * some parts by hjholtz
 * 
 * @author buechse
 * 
 */
public class SimpleLoader<V, T extends Tool<V>>
		implements Loader<T> {

	protected final ModuleInstance<V, T> mod;
	protected final FilenameFilter filter;
	protected final ToolFactory<V, T> factory;

	public SimpleLoader(ModuleInstance<V, T> mod, FilenameFilter filter,
			ToolFactory<V, T> factory) {
		this.mod = mod;
		this.filter = filter;
		this.factory = factory;
	}

	public static FilenameFilter createExtensionFilter(String extension) {
		return new ExtensionFilter(extension);
	}

	@Override
	public void load(Observer<T> o) {
		File directory = new File(mod.getPath());
		if (!directory.isDirectory()) {
			if (directory.exists()) {
				// TODO
			}
			directory.mkdirs();
		}
		System.out.println(directory.getAbsolutePath());
		for (File file : directory.listFiles(filter)) {
			System.out.println(file.getAbsolutePath());
			try {
				T g = factory.createInstance(mod, file);
				o.notify(g);
			} catch (Exception e) {
				// TODO: log
			}
		}
	}

	protected static class ExtensionFilter implements FilenameFilter {
		protected String extension;

		public ExtensionFilter(String extension) {
			this.extension = extension;
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}
