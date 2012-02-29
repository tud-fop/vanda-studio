/**
 * 
 */
package org.vanda.studio.modules.algorithms;

import java.util.List;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.Loader;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleLoader;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.SimpleVObjectInstance;
import org.vanda.studio.modules.common.VObjectFactory;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public class AlgorithmsModule implements SimpleModule<VObject> {
	
	@Override
	public Editor<VObject> createEditor(Application app) {
		return null; //new WrtgEditor(app);
	}
	
	@Override
	public ModuleInstance<VObject> createInstance(Application app) {
		return new AlgorithmModuleInstance(app, this);
	}
	
	@Override
	public VObjectFactory<VObject> createFactory()
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
	extends SimpleModuleInstance<VObject> {
		public AlgorithmModuleInstance(Application a, SimpleModule<VObject> m) {
			super(a, m);
			repository.addItem(
				new VObject() {
					final String[] inports = { "parallel corpus" };
					final String[] outports = { "dictionary" };
					
					@Override
					public void appendActions(List<Action> as) {
					}
				
					@Override
					public VObjectInstance createInstance() {
						return new SimpleVObjectInstance();
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
					public String[] getInputPorts() {
						return inports;
					}
				
					@Override
					public String getName() {
						return "Dictionary Training";
					}
				
					@Override
					public String[] getOutputPorts() {
						return outports;
					}
					
					@Override
					public void selectRenderer(RendererSelection rs) {
						rs.selectAlgorithmRenderer();
					}
				});
			
			repository.addItem(
				new VObject() {
					final String[] inports = { "dictionary" };
					final String[] outports = { };
					
					@Override
					public void appendActions(List<Action> as) {
					}
				
					@Override
					public VObjectInstance createInstance() {
						return new SimpleVObjectInstance();
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
					public String[] getInputPorts() {
						return inports;
					}
				
					@Override
					public String getName() {
						return "Dictionary Sink";
					}
				
					@Override
					public String[] getOutputPorts() {
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
