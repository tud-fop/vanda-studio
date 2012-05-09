/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;

/**
 * @author buechse
 * 
 */
public class DictionaryModule implements SimpleModule<ShellView, VDictionary> {
	
	@Override
	public Editor<ShellView, VDictionary> createEditor(Application app) {
		return new DictionaryEditor(app);
	}
	
	@Override
	public ModuleInstance<ShellView, VDictionary> createInstance(Application app) {
		return new SimpleModuleInstance<ShellView, VDictionary>(app, this);
	}
	
	@Override
	public ToolFactory<ShellView, VDictionary> createFactory()
	{
		return new VDictionaryFactory();
	}
	
	@Override
	public String getExtension() {
		return ".csv";
	}
	
	@Override
	public String getName() {
		return "Dictionaries"; // " Module for Vanda Studio";
	}

}
