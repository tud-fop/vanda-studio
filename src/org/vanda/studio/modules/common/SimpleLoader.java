/**
 * 
 */
package org.vanda.studio.modules.common;

import java.io.File;
import java.io.FilenameFilter;

import org.vanda.studio.model.VObject;
import org.vanda.studio.util.Observer;

/**
 * some parts by hjholtz
 * @author buechse
 * 
 */
public class SimpleLoader<T extends VObject> implements Loader<VObject> {
	
	protected final ModuleInstance<T> mod;
	protected final FilenameFilter filter;
	protected final VObjectFactory<T> factory;
	
	public SimpleLoader(
		ModuleInstance<T> mod,
		FilenameFilter filter,
		VObjectFactory<T> factory)
	{
		this.mod = mod;
		this.filter = filter;
		this.factory = factory;
	}
	
	public static FilenameFilter createExtensionFilter(String extension) {
		return new ExtensionFilter(extension);
	}
	
	@Override
	public void load(Observer<VObject> o) {
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
				VObject g = factory.createInstance(mod, file);
				o.notify(g);
			}
			catch (Exception e) {
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
