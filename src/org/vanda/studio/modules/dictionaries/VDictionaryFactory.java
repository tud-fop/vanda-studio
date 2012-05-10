/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;
import org.vanda.studio.util.Action;

/**
 * some parts by hjholtz
 * @author buechse
 * 
 */
public class VDictionaryFactory implements ToolFactory<Object, VDictionary> {
	
	@Override
	public VDictionary createInstance(
		ModuleInstance<Object, VDictionary> mod,
		File f)
	{
		return new VDictionaryImpl(mod, f);
	}
	
	protected static class VDictionaryImpl extends VDictionary {
		
		protected static final List<Port> inports;
		protected static final List<Port> outports;
		
		static {
			inports = new ArrayList<Port>();
			outports = new ArrayList<Port>();
			outports.add(new Port("dictionary", "dictionary"));
		}
		
		ModuleInstance<Object, VDictionary> mod;
		File file;
		String author;
		String category;
		String date;
		String description;
		String id;
		String name;
		
		public VDictionaryImpl(ModuleInstance<Object, VDictionary> mod, File file) {
			this.mod = mod;
			this.file = file;
			author = "unknown";
			category = "Dictionaries";
			date = "file.getDate..."; // FIXME
			description = "Dictionary";
			id = file.getAbsolutePath();
			name = file.getName();
			name = name.substring(0,name.length()-4);
			//System.out.println(file.getPath());
		}
		
		@Override
		public void appendActions(List<Action> as) {
			as.add(
				new Action() {
					@Override
					public String getName() {
						return "View";
					}
					
					@Override
					public void invoke() {
						// open Torsten's viewer
						mod.openEditor(VDictionaryImpl.this);
					}
				});
		}
		
		@Override
		public Element clone() {
			return this;
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
		public List<Port> getInputPorts() {
			return inports;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		@Override
		public List<Port> getOutputPorts() {
			return outports;
		}
		
		@Override
		public Dictionary load() throws IOException {
			return new Dictionary(file.getAbsolutePath(), '\t');
		}

		@Override
		public <R> R selectRenderer(RendererAssortment<R> rs) {
			return rs.selectGrammarRenderer();
		}

		/*@Override
		public <T extends ArtifactConn, A extends Artifact<T>, F> A createArtifact(
				ArtifactFactory<T, A, F, ShellView> af) {
			return null;
		}*/

		@Override
		public Class<Object> getFragmentType() {
			return Object.class;
		}
		
	}

}
