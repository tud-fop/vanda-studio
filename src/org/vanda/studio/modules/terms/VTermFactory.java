/**
 * 
 */
package org.vanda.studio.modules.terms;

import java.io.File;
import java.util.List;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.Port;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.ToolInstance;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleVObjectInstance;
import org.vanda.studio.modules.common.VObjectFactory;

/**
 * @author buechse
 * 
 */
public class VTermFactory implements VObjectFactory<VTerm> {
	
	@Override
	public VTerm createInstance(ModuleInstance<VTerm> mod, File f) {
		return new VTermImpl(mod, f);
	}
	
	protected static class VTermImpl implements VTerm {
		
		protected static final Port[] ports = new Port[0];
		
		ModuleInstance<VTerm> mod;
		File file;
		String author;
		String category;
		String date;
		String description;
		String id;
		String name;
		
		public VTermImpl(ModuleInstance<VTerm> mod, File file) {
			this.mod = mod;
			this.file = file;
			// TODO: retrieve metadata
			id = toString();
			name = "test";
			description = "test";
			date = "test";
			category = "Terms";
			author = "buechse";
		}
		
		@Override
		public Term load() {
			// open file as a ByteStream and construct the Term
			// TODO
			return new Term();
		}
		
		@Override
		public void save(Term t) {
			// open file as a ByteStream and so on
			// TODO
			// broadcast that this object has been modified
			mod.getModifyObserver().notify(VTermImpl.this);
		}
		
		@Override
		public void appendActions(List<Action> as) {
			as.add(
				new Action() {
					@Override
					public String getName() {
						return "Edit";
					}
					
					@Override
					public void invoke() {
						mod.openEditor(VTermImpl.this);
					}
				});
		}

		@Override
		public ToolInstance createInstance() {
			return new SimpleVObjectInstance();
		}

		@Override
		public String getAuthor() {
			return author;
		}
		
		@Override
		public String getCategory() {
			return category;
		}
		
		@Override
		public String getDate() {
			return date;
		}
	
		@Override
		public String getDescription() {
			return description;
		}
	
		@Override
		public String getId() {
			return id;
		}
	
		@Override
		public Port[] getInputPorts() {
			return ports;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		@Override
		public Port[] getOutputPorts() {
			return ports;
		}

		@Override
		public void selectRenderer(RendererSelection rs) {
			rs.selectTermRenderer();
		}
		
	}

}
