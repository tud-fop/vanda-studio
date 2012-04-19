/**
 * 
 */
package org.vanda.studio.modules.algorithms;

import java.util.List;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.Port;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.Tool;
import org.vanda.studio.model.ToolInstance;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.SimpleToolInstance;
import org.vanda.studio.modules.common.ToolFactory;

/**
 * @author buechse
 * 
 */
public class AlgorithmsModule implements SimpleModule<Tool> {
	
	@Override
	public Editor<Tool> createEditor(Application app) {
		return null; //new WrtgEditor(app);
	}
	
	@Override
	public ModuleInstance<Tool> createInstance(Application app) {
		return new AlgorithmModuleInstance(app, this);
	}
	
	@Override
	public ToolFactory<Tool> createFactory()
	{
		return null;
	}
	
	@Override
	public String getExtension() {
		return ".algorithm";
	}
	
	@Override
	public String getName() {
		return "Dictionaries"; // " Module for Vanda Studio";
	}
	
	protected static class AlgorithmModuleInstance
	extends SimpleModuleInstance<Tool> {
		public AlgorithmModuleInstance(Application a, SimpleModule<Tool> m) {
			super(a, m);
			repository.addItem(
				new Tool() {
					final Port[] inports = { new Port("parallel corpus", "parallel corpus") };
					final Port[] outports = { new Port("dictionary", "dictionary") };
					
					@Override
					public void appendActions(List<Action> as) {
					}
				
					@Override
					public ToolInstance createInstance() {
						return new SimpleToolInstance();
					}
				
					@Override
					public String getAuthor() {
						return "buechse";
					}
					
					@Override
					public String getCategory() {
						return "Algorithms";
					}
					
					@Override
					public String getDate() {
						return "date";
					}
				
					@Override
					public String getDescription() {
						return "IBM model 1 dictionary training";
					}
				
					@Override
					public String getId() {
						return "ibm1training";
					}
				
					//public StringBuilder generateCode(String[] args);
				
					@Override
					public Port[] getInputPorts() {
						return inports;
					}
				
					@Override
					public String getName() {
						return "Dictionary Training";
					}
				
					@Override
					public Port[] getOutputPorts() {
						return outports;
					}
					
					@Override
					public void selectRenderer(RendererSelection rs) {
						rs.selectAlgorithmRenderer();
					}
				});
			
			repository.addItem(
				new Tool() {
					final Port[] inports = { new Port("dictionary", "dictionary") };
					final Port[] outports = { };
					
					@Override
					public void appendActions(List<Action> as) {
					}
				
					@Override
					public ToolInstance createInstance() {
						return new SimpleToolInstance();
					}
				
					@Override
					public String getAuthor() {
						return "buechse";
					}
					
					@Override
					public String getCategory() {
						return "Sinks";
					}
					
					@Override
					public String getDate() {
						return "date";
					}
				
					@Override
					public String getDescription() {
						return "dictionary sink";
					}
				
					@Override
					public String getId() {
						return "dictsink";
					}
				
					//public StringBuilder generateCode(String[] args);
				
					@Override
					public Port[] getInputPorts() {
						return inports;
					}
				
					@Override
					public String getName() {
						return "Dictionary Sink";
					}
				
					@Override
					public Port[] getOutputPorts() {
						return outports;
					}
					
					@Override
					public void selectRenderer(RendererSelection rs) {
						rs.selectSinkRenderer();
					}
				});
		}
	}
}
