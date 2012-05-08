/**
 * 
 */
package org.vanda.studio.modules.algorithms;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.model.workflows.Tool;
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
public class AlgorithmsModule implements
		SimpleModule<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> {

	@Override
	public Editor<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> createEditor(
			Application app) {
		return null; // new WrtgEditor(app);
	}

	@Override
	public ModuleInstance<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> createInstance(
			Application app) {
		return new AlgorithmModuleInstance(app, this);
	}

	@Override
	public ToolFactory<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> createFactory() {
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
			extends
			SimpleModuleInstance<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> {
		public AlgorithmModuleInstance(
				Application a,
				SimpleModule<ShellView, ToolInstance, Tool<ShellView, ToolInstance>> m) {
			super(a, m);
			/*
			 * repository.addItem( new Tool() { final Port[] inports = { new
			 * Port("parallel corpus", "parallel corpus") }; final Port[]
			 * outports = { new Port("dictionary", "dictionary") };
			 * 
			 * @Override public void appendActions(List<Action> as) { }
			 * 
			 * @Override public ToolInstance createInstance() { return new
			 * SimpleToolInstance(); }
			 * 
			 * @Override public String getAuthor() { return "buechse"; }
			 * 
			 * @Override public String getCategory() { return "Algorithms"; }
			 * 
			 * @Override public String getDate() { return "date"; }
			 * 
			 * @Override public String getDescription() { return
			 * "IBM model 1 dictionary training"; }
			 * 
			 * @Override public String getId() { return "ibm1training"; }
			 * 
			 * //public StringBuilder generateCode(String[] args);
			 * 
			 * @Override public Port[] getInputPorts() { return inports; }
			 * 
			 * @Override public String getName() { return
			 * "Dictionary\nTraining"; }
			 * 
			 * @Override public Port[] getOutputPorts() { return outports; }
			 * 
			 * @Override public void selectRenderer(RendererSelection rs) {
			 * rs.selectAlgorithmRenderer(); } });
			 * 
			 * repository.addItem( new Tool() { final Port[] inports = { new
			 * Port("dictionary", "dictionary") }; final Port[] outports = { };
			 * 
			 * @Override public void appendActions(List<Action> as) { }
			 * 
			 * @Override public ToolInstance createInstance() { return new
			 * SimpleToolInstance(); }
			 * 
			 * @Override public String getAuthor() { return "buechse"; }
			 * 
			 * @Override public String getCategory() { return "Sinks"; }
			 * 
			 * @Override public String getDate() { return "date"; }
			 * 
			 * @Override public String getDescription() { return
			 * "dictionary sink"; }
			 * 
			 * @Override public String getId() { return "dictsink"; }
			 * 
			 * //public StringBuilder generateCode(String[] args);
			 * 
			 * @Override public Port[] getInputPorts() { return inports; }
			 * 
			 * @Override public String getName() { return "Dictionary Sink"; }
			 * 
			 * @Override public Port[] getOutputPorts() { return outports; }
			 * 
			 * @Override public void selectRenderer(RendererSelection rs) {
			 * rs.selectSinkRenderer(); } });
			 */
		}
	}
}
