/**
 * 
 */
package org.vanda.studio.modules.terms;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;

/**
 * @author buechse
 * 
 */
public class TermModule implements SimpleModule<VTerm> {
	
	@Override
	public Editor<VTerm> createEditor(Application app) {
		return new TermEditor(app);
	}
	
	@Override
	public ToolFactory<VTerm> createFactory() {
		return new VTermFactory();
	}
	
	@Override
	public ModuleInstance<VTerm> createInstance(Application app) {
		return new TermModuleInstance(app, this);
	}
	
	@Override
	public String getExtension() {
		return ".term";
	}

	@Override
	public String getName() {
		return "Terms"; // Module for Vanda Studio";
	}
	
	protected static class TermModuleInstance
	extends SimpleModuleInstance<VTerm> {
		
		public TermModuleInstance(Application a, SimpleModule<VTerm> m) {
			super(a, m);
			
			app.getWindowSystem().addAction(new NewTermAction());
		}
		
		protected class NewTermAction implements Action {
			@Override
			public String getName() {
				return "New term";
			}
			
			@Override
			public void invoke() {
				// create term (file)
				VTerm t = factory.createInstance(TermModuleInstance.this, null);
				// do something with the repository
				//repository.addItem(t); FIXME
				// open editor for term
				openEditor(t);
			}
		}
	}
	
}
