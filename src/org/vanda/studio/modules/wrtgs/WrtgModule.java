/**
 * 
 */
package org.vanda.studio.modules.wrtgs;

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
public class WrtgModule implements SimpleModule<VTreeGrammar> {
	
	@Override
	public Editor<VTreeGrammar> createEditor(Application app) {
		return new WrtgEditor(app);
	}
	
	@Override
	public ModuleInstance<VTreeGrammar> createInstance(Application app) {
		return new SimpleModuleInstance<VTreeGrammar>(app, this);
	}
	
	@Override
	public ToolFactory<VTreeGrammar> createFactory()
	{
		return new VTreeGrammarFactory();
	}
	
	@Override
	public String getExtension() {
		return ".wrtg";
	}
	
	@Override
	public String getName() {
		return "WRTG"; // " Module for Vanda Studio";
	}

}
