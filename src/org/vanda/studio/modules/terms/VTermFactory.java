/**
 * 
 */
package org.vanda.studio.modules.terms;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleVObjectInstance;
import org.vanda.studio.modules.common.VObjectFactory;
import org.vanda.studio.util.Observer;

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
		
		protected static final String[] ports = new String[0];
		
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
		public VObjectInstance createInstance() {
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
		public String[] getInputPorts() {
			return ports;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		@Override
		public String[] getOutputPorts() {
			return ports;
		}

		@Override
		public void selectRenderer(RendererSelection rs) {
			rs.selectTermRenderer();
		}
		
	}

}
