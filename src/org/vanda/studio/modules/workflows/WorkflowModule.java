package org.vanda.studio.modules.workflows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.hyper.Serialization;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.util.Action;

public class WorkflowModule implements Module {

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}
	
	protected static class WorkflowModuleInstance {

		Application app;
		
		private static class Plain2Snt extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("English", "Sentence Corpus"));
				inputPorts.add(new Port("French", "Sentence Corpus"));
				outputPorts.add(new Port("Parallel", "GIZA.snt"));
				outputPorts.add(new Port("English.vcb", "GIZA.vcb"));
				outputPorts.add(new Port("French.vcb", "GIZA.vcb"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "alignments";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "Converts two plain text files into GIZA++ files";
			}
			public String getId() {
				return "plain2snt";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "plain2snt";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		private static class GIZA extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Parallel", "GIZA.snt"));
				inputPorts.add(new Port("English.vcb", "GIZA.vcb"));
				inputPorts.add(new Port("French.vcb", "GIZA.vcb"));
				outputPorts.add(new Port("Alignments", "GIZA Alignments"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "alignments";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "computes alignments and translation tables";
			}
			public String getId() {
				return "GIZA";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "GIZA";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		private static class Berkeley extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", "Sentence Corpus"));
				inputPorts.add(new Port("Grammar", "BerkeleyGrammar.sm6"));
				outputPorts.add(new Port("Tree Corpus", "Penn Tree Corpus"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "parsing";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "Berkeley parser using a state-split grammar";
			}
			public String getId() {
				return "BerkeleyParser";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "Berkeley Parser";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		private static class Tokenizer extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", "Sentence Corpus"));
				outputPorts.add(new Port("Tokenized Corpus", "Sentence Corpus"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "corpus tools";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "Converts some special characters into tokens, such as ( into -LLB-";
			}
			public String getId() {
				return "BerkeleyTokenizer";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "Berkeley Tokenizer";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		private static class PennToInt extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Tree Corpus", "Penn Tree Corpus"));
				outputPorts.add(new Port("Integerized Tree Corpus", "Penn Tree Corpus"));
				outputPorts.add(new Port("Conversion Table", "TokenMap"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "corpus tools";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "Converts words into integers, creates a conversion table";
			}
			public String getId() {
				return "PennToInt";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "PennToInt";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		private static class GHKM extends Tool<Object> {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Alignment", "GIZA Alignments"));
				inputPorts.add(new Port("Integerized Tree Corpus", "Penn Tree Corpus"));
				inputPorts.add(new Port("Conversion Table", "TokenMap"));
				outputPorts.add(new Port("Rules", "GHKM Hypergraph"));
				outputPorts.add(new Port("Conversion Table", "TokenMap"));
			}
			
			public String getAuthor() {
				return "buechse";
			}
			public String getCategory() {
				return "rule extraction";
			}
			public String getDate() {
				return "date";
			}
			public String getDescription() {
				return "Extracts GHKM rules from a GIZA alignment and an integizered tree corpus";
			}
			public String getId() {
				return "HyperGHKM";
			}
			public List<Port> getInputPorts() {
				return inputPorts;
			}
			public String getName() {
				return "GHKM";
			}
			public List<Port> getOutputPorts() {
				return outputPorts;
			}
			public Class<Object> getFragmentType() {
				return Object.class;
			}
			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}
			public void appendActions(List<Action> as) {
			}
		}

		public WorkflowModuleInstance(Application a) {
			app = a;

			//app.getWindowSystem().addSeparator();			
			
			SimpleRepository<Object,Tool<Object>> tr 
				= new SimpleRepository<Object,Tool<Object>>(
						null);
			
			tr.addItem(new Plain2Snt());
			tr.addItem(new GIZA());
			tr.addItem(new Berkeley());
			tr.addItem(new Tokenizer());
			tr.addItem(new PennToInt());
			tr.addItem(new GHKM());
			
			Tool<Object> sinkTool = new Tool<Object>() {	
				public String getAuthor() {
					return "afischer";
				}
				public String getCategory() {
					return "testCategory";
				}
				public String getDate() {
					return "date";
				}
				public String getDescription() {
					return "TestSink";
				}
				public String getId() {
					return "sinkId";
				}
				public List<Port> getInputPorts() {
					Port p = new Port("inputPort", "portType");
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}
				public String getName() {
					return "Sink";
				}
				public List<Port> getOutputPorts() {
					return new ArrayList<Port>();
				}
				public Class<Object> getFragmentType() {
					return Object.class;
				}
				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}
				public void appendActions(List<Action> as) {
				}
			};
			
			tr.addItem(sinkTool);
			
			app.getToolMetaRepository().addRepository(tr);
			
			/*
			 * Action save = new SaveWorkflowAction(editor);
			 * app.getWindowSystem().addAction(save);
			 * app.getWindowSystem().disableAction(save);
			 * 
			 * app.getWindowSystem().addSeparator();
			 * 
			 * Action close = new CloseWorkflowAction(editor);
			 * app.getWindowSystem().addAction(close);
			 * app.getWindowSystem().disableAction(close);
			 * 
			 * app.getWindowSystem().addSeparator();
			 * 
			 * app.getWindowSystem().addAction(new OpenWorkflowAction());
			 */
			app.getWindowSystem().addAction(null, new OpenWorkflowAction());
			app.getWindowSystem().addAction(null, new NewWorkflowAction());
		}

		/*
		 * determines the active WorkflowEditorTab of the specified
		 * WorkflowEditor
		 * 
		 * @param we
		 * @return
		 *
		protected static WorkflowEditorTab getActiveTab(WorkflowEditor we) {
			JTabbedPane tabbedPane = (JTabbedPane) we.tabs.values().iterator()
					.next().getComponent().getParent();
			JSplitPane splitPane = (JSplitPane) tabbedPane
					.getSelectedComponent();

			WorkflowEditorTab weTab = null;
			for (String s : we.tabs.keySet()) {
				if (we.tabs.get(s).mainpane.equals(splitPane)) {
					weTab = we.tabs.get(s);
				}
			}

			return weTab;
		}*/

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Workflow";
			}

			@Override
			public void invoke() {
				new WorkflowEditor(app, new MutableWorkflow<Object>(Object.class));
			}
		}

		protected class OpenWorkflowAction implements Action {
			@Override
			public String getName() {
				return "Open Workflow";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Hyperworkflows (*.hwf)", "hwf"));
				chooser.setVisible(true);
				int result = chooser.showOpenDialog(null);

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					String filePath = chosenFile.getPath();
					MutableWorkflow<?> hwf = Serialization.load(filePath, app);
					new WorkflowEditor(app, hwf);
				}
			}
		}

	}
}