/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;

/**
 * @author buechse
 * 
 */
public class DictionaryModule implements SimpleModule<Object, VDictionary> {
	
	@Override
	public Editor<Object, VDictionary> createEditor(Application app) {
		return new DictionaryEditor(app);
	}
	
	@Override
	public ModuleInstance<Object, VDictionary> createInstance(Application app) {
		return new SimpleModuleInstance<Object, VDictionary>(app, this);
	}
	
	@Override
	public ToolFactory<Object, VDictionary> createFactory()
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
