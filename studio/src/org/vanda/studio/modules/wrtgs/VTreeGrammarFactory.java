/**
 * 
 */
package org.vanda.studio.modules.wrtgs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
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
 * some parts by hjholtz
 * @author buechse
 * 
 */
public class VTreeGrammarFactory implements VObjectFactory<VTreeGrammar> {
	
	@Override
	public VTreeGrammar createInstance(
		ModuleInstance<VTreeGrammar> mod,
		File f)
	{
		return new VTreeGrammarImpl(mod, f);
	}
	
	protected static class VTreeGrammarImpl implements VTreeGrammar {
		
		protected static final String[] inports = { };
		protected static final String[] outports = { "wrtg" };
		
		ModuleInstance<VTreeGrammar> mod;
		File file;
		String author;
		String category;
		String date;
		String description;
		String id;
		String name;
		
		public VTreeGrammarImpl(ModuleInstance<VTreeGrammar> mod, File file) {
			this.mod = mod;
			this.file = file;
			category = "Grammars";
			// TODO: retrieve metadata
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));

				String line = reader.readLine();
				if (!line.trim().equals("%% Vanda WRTG")) {
					throw new UnsupportedOperationException("cannot load wrtg");
				}
				line = reader.readLine();
				while (line.startsWith("%")) {
					if (line.substring(1).trim().startsWith("ID:")) {
						id = line.substring(line.indexOf(":") + 1).trim();
					}
					else if (line.substring(1).trim().startsWith("Name:")) {
						name = line.substring(line.indexOf(":") + 1).trim();
					}
					else if (line.substring(1).trim().startsWith("Author:")) {
						author = line.substring(line.indexOf(":") + 1).trim();
					}
					/*else if (line.substring(1).trim()
							.startsWith("Semiring:")) {
						line = line.substring(line.indexOf(":") + 1).trim();
						String[] parts = line.split(",");
						semiring = new Semiring(parts[0].trim(),
								parts[1].trim(), parts[2].trim());
					}*/
					else if (line.startsWith("%%")
							&& line.substring(2).trim()
									.startsWith("Description:")) {
						line = reader.readLine();
						while (line.startsWith("%") && !line.startsWith("%%")) {
							description += line.substring(3).trim();
							line = reader.readLine();
						}
					}

					line = reader.readLine();
				}
				if (id == null /*|| semiring == null*/) {
					// TODO: log
					//throw new UnsupportedOperationException("cannot read wrtg");
					id = file.getPath();
				}
				if (name == null) {
					name = id;
				}
				if (author == null) {
					author = "";
				}
				if (description == null) {
					description = "";
				}
			}
			catch (Exception e) {
				// TODO: log
			}
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
						mod.openEditor(VTreeGrammarImpl.this);
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
			return inports;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		@Override
		public String[] getOutputPorts() {
			return outports;
		}

		@Override
		public void selectRenderer(RendererSelection rs) {
			rs.selectGrammarRenderer();
		}
		
	}

}
