/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;

/**
 * @author buechse
 * 
 */
public class DictionaryModule implements SimpleModule<ShellView, ToolInstance, VDictionary> {
	
	@Override
	public Editor<ShellView, ToolInstance, VDictionary> createEditor(Application app) {
		return new DictionaryEditor(app);
	}
	
	@Override
	public ModuleInstance<ShellView, ToolInstance, VDictionary> createInstance(Application app) {
		return new SimpleModuleInstance<ShellView, ToolInstance, VDictionary>(app, this);
	}
	
	@Override
	public ToolFactory<ShellView, ToolInstance, VDictionary> createFactory()
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
